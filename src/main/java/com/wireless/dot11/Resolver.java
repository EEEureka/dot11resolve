package com.wireless.dot11;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.DataLinkType;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Clipboard;
import java.util.Arrays;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class Resolver implements Serializable {
    public String pcapFilePath;
    public List<Packet> packets = new ArrayList<Packet>();
    public List<Dot11Beacon> beaconSourceData = new ArrayList<Dot11Beacon>();
    public List<Dot11Beacon> uniqueSSIDs = new ArrayList<Dot11Beacon>();
    public List<Dot11Beacon> uniqueBSSIDs;

    public Resolver(String pcapFilePath) {
        this.pcapFilePath = pcapFilePath;
    }

    public boolean isBeacon(Packet packet) {
        // 判断是否是beacon帧
        byte[] rawData = packet.getRawData();
        int pointer = getTagParaPosition(rawData) - 36;
        if (pointer >= rawData.length) {
            return false;
        }
        byte frameType = rawData[pointer];
        if ((frameType & 0xff) == 0x80 && (rawData[pointer + 1] & 0xff) == 0x00) {
            return true;
        }
        return false;
    }

    public void getPackets() {
        if (this.pcapFilePath == null) {
            System.out.println("Please provide a pcap file path");
            return;
        }
        try {
            PcapHandle handle = Pcaps.openOffline(this.pcapFilePath);
            DataLinkType dlt = handle.getDlt();
            if (!dlt.equals(DataLinkType.IEEE802_11_RADIO)) {
                System.out.println("Please provide a pcap file with IEEE802_11_RADIO data link type");
                return;
            }
            Packet packet;
            while ((packet = handle.getNextPacket()) != null) {
                if (isBeacon(packet)) {
                    packets.add(packet);
                } else {
                    packets.add(null);
                }
            }
            // System.out.println(packets.get(0));
            handle.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resolve() {
        this.getPackets();
        for (int i = 0; i < packets.size(); i++) {
            if (packets.get(i) == null) {
                continue;
            }
            this.beaconSourceData.add(packet2Obj(this.packets.get(i), i + 1));
        }
    }

    public byte[] little2BigEndianOrder(byte[] data) {
        int length = data.length;
        byte[] h = new byte[length];
        for (int i = 0; i < length; i++) {
            h[i] = data[length - i - 1];
        }
        return h;
    }

    /*
     * 将byte[]转换为数值
     */
    public int bytesToInt(byte[] bytes) {
        int num = 0;
        for (int i = 0; i < bytes.length; i++) {
            num <<= 8;
            num |= (bytes[i] & 0xff);
        }
        return num;
    }

    public int getTagParaPosition(byte[] packet) {
        int pointer = 0;
        int radioTapLength = bytesToInt(little2BigEndianOrder(new byte[] { packet[2], packet[3] }));
        // System.out.println("radioTapLength: " + radioTapLength);
        pointer = radioTapLength + 36;
        // System.out.println("pointer: " + pointer);
        return pointer;
    }

    public List<Tag> getTags(byte[] packet) {
        int pointer = getTagParaPosition(packet);
        List<Tag> tags = new ArrayList<Tag>();
        while (pointer < packet.length) {
            int tagNumber;
            int tagLength;
            // System.out.println("pointer: " + Integer.toHexString(pointer));
            if ((packet[pointer] & 0xff) == 0xff) {
                TagType tagType = TagType.EXT_TAG;
                pointer++;
                if (pointer >= packet.length) {
                    break;
                }
                tagLength = bytesToInt(new byte[] { packet[pointer] }) - 1;
                pointer++;
                tagNumber = bytesToInt(new byte[] { packet[pointer] });
                pointer++;
                byte[] content = new byte[tagLength];
                for (int i = pointer; i < pointer + tagLength; i++) {
                    content[i - pointer] = packet[i];
                }
                pointer += tagLength;
                tags.add(new Tag(tagNumber, tagLength, tagType, content));
            } else {
                TagType tagType = TagType.TAG;
                tagNumber = (packet[pointer] & 0xff);
                pointer++;
                tagLength = (packet[pointer] & 0xff);
                pointer++;
                byte[] content = new byte[tagLength];
                try { // 可能存在有些beacon帧没有tag
                    for (int i = pointer; i < pointer + tagLength; i++) {
                        // System.out.println("i: " + i);
                        content[i - pointer] = packet[i];
                    }
                } catch (Exception e) {
                    continue;
                }
                pointer += tagLength;
                tags.add(new Tag(tagNumber, tagLength, tagType, content));
            }
        }
        return tags;
    }

    /*
     * 获取VHT带宽, 用于802.11ac
     * 根据已知文档，channel width字段等于2或3的情况已经废弃，只有为0时代表20/40, 为1代表80/160/80+80
     * 目前判断频宽的方法是
     * channel width为0， 频宽为20/40,
     * channel width为1, CCFS1为0， 频宽为80
     * channel width为1, CCFS1 > 0, |CCFS1-CCFS0| = 8, 频宽为160， |CCFS1-CCFS0| > 8,
     * 频宽为80+80
     */
    public Bandwidth getVHTBandwidth(byte[] vhtContent) {
        int bandwidth = (vhtContent[0] & 0x03);
        if (bandwidth == 0) {
            return Bandwidth.BW20OR40;
        } else {
            int CCFS0 = (vhtContent[1] & 0xff);
            int CCFS1 = (vhtContent[2] & 0xff);
            if (CCFS1 == 0) {
                return Bandwidth.BW80;
            }
            if (Math.abs(CCFS1 - CCFS0) == 8) {
                return Bandwidth.BW160;
            }
            if (Math.abs(CCFS1 - CCFS0) > 8) {
                return Bandwidth.BW80_80;
            }
        }
        return null;
    }

    /*
     * 获取HT带宽, 用于802.11n
     */
    public Bandwidth getHTBandwidth(byte[] htContent) {
        int secondaryChannelOffset = (htContent[1] & 0x03);
        switch (secondaryChannelOffset) {
            case 0:
                return Bandwidth.BW20;
            default:
                return Bandwidth.BW40;
        }
    }

    /*
     * 获取HT信道, 用于802.11n, 一般信道获取也使用这个方法
     */
    public int getHTChannel(byte[] htContent) {
        byte primaryChannel = htContent[0];
        return bytesToInt(new byte[] { primaryChannel });
    }

    public int[] QBSSAnalyse(Tag[] tags) {
        Tag QBSSLoadElement = null;
        boolean haveQBSSTag = false;

        for (Tag tag : tags) {
            if (tag.tagNumber == 11) {
                QBSSLoadElement = tag;
                haveQBSSTag = true;
                break;
            }
        }
        int stationCount = 0;
        int channelUsage = -1;
        if (!haveQBSSTag) {
            stationCount = -1;
        }
        if (QBSSLoadElement != null) {
            byte[] stationCountBytes = new byte[2];
            stationCountBytes[0] = QBSSLoadElement.content[0];
            stationCountBytes[1] = QBSSLoadElement.content[1];
            stationCount = bytesToInt(little2BigEndianOrder(stationCountBytes));
            channelUsage = QBSSLoadElement.content[2] & 0xff;
            // System.out.println("station count: " + stationCount);
        }
        return new int[] { stationCount, channelUsage };
    }

    public Protocol getProtocolFromTag(List<Tag> tags) {
        boolean haveEht = false;
        boolean haveHe = false;
        boolean haveVht = false;
        boolean haveHt = false;
        for (Tag tag : tags) {
            switch (tag.tagNumber) {
                case 106:
                case 107:
                case 108:
                    haveEht = tag.tagType == TagType.EXT_TAG;
                    break;
                case 36:
                case 35:
                    haveHe = tag.tagType == TagType.EXT_TAG;
                    break;
                case 192:
                case 191:
                    haveVht = tag.tagType == TagType.TAG;
                    break;
                case 61:
                case 45:
                    haveHt = tag.tagType == TagType.TAG;
                    break;
                default:
                    break;
            }
        }
        return haveEht ? Protocol.DOT11BE
                : haveHe ? Protocol.DOT11AX : haveVht ? Protocol.DOT11AC : haveHt ? Protocol.DOT11N : Protocol.DOT11G;
    }

    public Tag[] getProtocolOpTags(List<Tag> tags) {
        Tag htInfoTag = null;
        Tag vhtOpTag = null;
        Tag ehtOpTag = null;
        Tag heOpTag = null;
        for (int i = 0; i < tags.size(); i++) {
            Tag targetTag = tags.get(i);
            switch (targetTag.tagNumber) {
                case 108:
                    if (targetTag.tagType == TagType.EXT_TAG) {
                        ehtOpTag = targetTag;
                    }
                    break;
                case 36:
                    if (targetTag.tagType == TagType.EXT_TAG) {
                        heOpTag = targetTag;
                    }
                    break;
                case 192:
                    if (targetTag.tagType == TagType.TAG) {
                        vhtOpTag = targetTag;
                    }
                    break;
                case 61:
                    if (targetTag.tagType == TagType.TAG) {
                        htInfoTag = targetTag;
                    }
                    break;
                default:
                    break;
            }
        }
        return new Tag[] { htInfoTag, vhtOpTag, ehtOpTag, heOpTag };
    }

    public String getSSID(List<Tag> tags) {
        for (int i = 0; i < tags.size(); i++) {
            Tag targetTag = tags.get(i);
            if (targetTag.tagNumber == 0) {
                byte[] ssid = targetTag.content;
                return new String(ssid, StandardCharsets.UTF_8);
            }
        }
        return "HIDDEN SSID";
    }

    public Bandwidth getBandwidthFromTag(List<Tag> tags, Tag vhtOpTag, Tag htInfoTag) {
        Bandwidth bandwidth = null;
        if (vhtOpTag != null) {
            bandwidth = getVHTBandwidth(vhtOpTag.content);
        }
        if (bandwidth == null || bandwidth == Bandwidth.BW20OR40) {
            if (htInfoTag != null) {
                bandwidth = getHTBandwidth(htInfoTag.content);
            }
        }
        return bandwidth;
    }

    /**
     * Extracts RSN (Robust Security Network) information from a list of tags.
     *
     * @param tags the list of tags to search for RSN information
     * @return an RSN object containing the parsed RSN information, or null if no RSN tag is found
     */
    public RSN getRSNInfo(List<Tag> tags) {
        Tag targetTag = null;
        int position = 0;
        for (Tag tag : tags) {
            if (tag.tagNumber == 48 && tag.tagType == TagType.TAG) {
                targetTag = tag;
                break;
            }
        }
        if (targetTag == null) {
            return null;
        }
        byte[] rsnContent = targetTag.content;
        int RSNVersion = bytesToInt(little2BigEndianOrder(new byte[] { rsnContent[position + 0], rsnContent[position + 1] }));
        position += 2;
        byte[] groupCipherSuite = new byte[4];
        for (int i = 0; i < 4; i++) {
            groupCipherSuite[i] = rsnContent[position + i];
        }
        position += 4;
        int pairwiseCipherSuiteCount = bytesToInt(little2BigEndianOrder(new byte[] { rsnContent[position], rsnContent[position + 1] }));
        position += 2;
        // now position is 8
        List<byte[]> pairwiseCipherSuite = new ArrayList<byte[]>();
        for (int i = 0; i < pairwiseCipherSuiteCount; i++) {
            byte[] pairwiseCipher = new byte[4];
            for (int j = 0; j < 4; j++) {
                pairwiseCipher[j] = rsnContent[position + j];
            }
            pairwiseCipherSuite.add(pairwiseCipher);
            position += 4;
        }
        // now position is 8 + 4 * pairwiseCipherSuiteCount
        int akmSuiteCount = bytesToInt(little2BigEndianOrder(new byte[] { rsnContent[position], rsnContent[position + 1] }));
        position += 2;
        List<Integer> akmSuiteType = new ArrayList<Integer>();
        for (int i = 0; i < akmSuiteCount; i++) {
            byte[] akmSuite = new byte[4];
            for (int j = 0; j < 4; j++) {
                akmSuite[j] = rsnContent[position + j];
            }
            akmSuiteType.add(akmSuite[3] & 0xff);
            position += 4;
        }
        // now position is 8 + 4 * pairwiseCipherSuiteCount + 2 + 4 * akmSuiteCount
        byte[] RSNCapability = new byte[2];
        for (int i = 0; i < 2; i++) {
            RSNCapability[i] = rsnContent[position + i];
        }
        AKM akm = null;
        switch (akmSuiteType.get(0)) {
            case 1:
                akm = AKM.DOT1X;
                break;
            case 2:
                akm = AKM.PSK;
                break;
            case 3:
                akm = AKM.FTDOT1X;
                break;
            case 4:
                akm = AKM.FTPSK;
                break;
            case 5:
                akm = AKM.DOT1XSHA256;
                break;
            case 6:
                akm = AKM.PSKSHA256;
                break;
            case 7:
                akm = AKM.TDLS;
                break;
            case 8:
                akm = AKM.WPA3SAE;
                break;
            case 9:
                akm = AKM.FTWPA3SAE;
                break;
            case 18:
                akm = AKM.OWE;
                break;
            default:
                break;
        }
        return new RSN(RSNVersion, groupCipherSuite, pairwiseCipherSuite, akmSuiteType, RSNCapability, akm);
    }

    /**
     * Extracts WPA information from a list of tags.
     *
     * This method searches through the provided list of tags to find a specific tag
     * that matches the WPA identifier. Once found, it extracts various WPA-related
     * information such as the WPA version, unicast cipher suite, and AKM suite type.
     *
     * @param tags the list of tags to search through
     * @return a WPAInfo object containing the extracted WPA information, or null if the WPA tag is not found
     */
    public WPAInfo getWPAInfo(List<Tag> tags) {
        Tag targetTag = null;
        int position = 0;
        for (Tag tag : tags) {
            if (tag.tagNumber == 221 &&
                    tag.tagType == TagType.TAG &&
                    tag.content[0] == (byte)0x00 &&
                    tag.content[1] == (byte)0x50 &&
                    tag.content[2] == (byte)0xf2 &&
                    tag.content[3] == (byte)0x01) {
                targetTag = tag;
                break;
            }
        }
        if (targetTag == null) {
            return null;
        }
        position += 4;
        int WPAVersion = bytesToInt(little2BigEndianOrder(new byte[] { targetTag.content[position], targetTag.content[position + 1] }));
        position += 2;
        byte[] unicasstCipherSuite = new byte[4];
        for (int i = 0; i < 4; i++) {
            unicasstCipherSuite[i] = targetTag.content[position + i];
        }
        position += 4;
        int unicastCipherSuiteCount = bytesToInt(
                little2BigEndianOrder(new byte[] { targetTag.content[position], targetTag.content[position + 1] }));
        position += 2;
        List<byte[]> unicastCipherSuite = new ArrayList<byte[]>();
        for (int i = 0; i < unicastCipherSuiteCount; i++) {
            byte[] unicastCipher = new byte[4];
            for (int j = 0; j < 4; j++) {
                unicastCipher[j] = targetTag.content[position + j];
            }
            unicastCipherSuite.add(unicastCipher);
            position += 4;
        }
        int akmSuiteCount = bytesToInt(little2BigEndianOrder(new byte[] { targetTag.content[position], targetTag.content[position + 1] }));
        position += 2;
        List<byte[]> akmSuiteType = new ArrayList<byte[]>();
        for (int i = 0; i < akmSuiteCount; i++) {
            byte[] akmSuite = new byte[4];
            for (int j = 0; j < 4; j++) {
                akmSuite[j] = targetTag.content[position + j];
            }
            akmSuiteType.add(akmSuite);
            position += 4;
        }
        return new WPAInfo(WPAVersion, unicasstCipherSuite, unicastCipherSuite, akmSuiteType);
    }

    /*
     * 将packet转换为Dot11Frame对象
     */
    public Dot11Beacon packet2Obj(Packet packet, int packetNum) {
        byte[] packetData = packet.getRawData();
        int tagPos = getTagParaPosition(packetData);
        int beaconHeader = tagPos - 36;
        int sourceAdd = beaconHeader + 16;
        int fixedPara = tagPos - 12;

        byte[] byteTimestamp = new byte[8];
        byte[] beaconInterval = new byte[2];
        byte[] capbilitiesInfomation = new byte[2];
        for (int i = 0; i < 8; i++) { // 需要转换为主机字节序
            byteTimestamp[i] = packetData[fixedPara + i];
        }
        byteTimestamp = little2BigEndianOrder(byteTimestamp);

        int timestamp = bytesToInt(byteTimestamp);

        for (int i = 0; i < 2; i++) { // 需要转换为主机字节序
            beaconInterval[i] = packetData[fixedPara + 8 + i];
        }
        beaconInterval = little2BigEndianOrder(beaconInterval);
        int interval = bytesToInt(beaconInterval); // 单位TU， 1TU = 1024us = 1.024ms

        for (int i = 0; i < 2; i++) {
            capbilitiesInfomation[i] = packetData[fixedPara + 10 + i];
        }

        byte[] BSSID = new byte[6];
        for (int i = 0; i < 6; i++) {
            BSSID[i] = packetData[sourceAdd + i];
        }

        List<Tag> tags = getTags(packetData);

        // 解析QBSSLoadElement, 获取station count和channel usage
        int[] tagResult = QBSSAnalyse(tags.toArray(new Tag[tags.size()]));
        int stationCount = tagResult[0];
        int channelUsage = tagResult[1];

        // 解析各标准的参数
        Tag htInfoTag = null;
        Tag vhtOpTag = null;
        Tag ehtOpTag = null;
        Tag heOpTag = null;
        Tag[] protocolOpTags = getProtocolOpTags(tags);
        htInfoTag = protocolOpTags[0];
        vhtOpTag = protocolOpTags[1];
        ehtOpTag = protocolOpTags[2];
        heOpTag = protocolOpTags[3];
        Protocol frameProtocol = null;
        Bandwidth bandwidth = getBandwidthFromTag(tags, vhtOpTag, htInfoTag);
        // 获取SSID
        String SSID = getSSID(tags);

        // 获取信道
        int channel = htInfoTag != null ? getHTChannel(htInfoTag.content) : 0;

        // 检查对各类标准的支持
        frameProtocol = this.getProtocolFromTag(tags);

        // 获取WPA信息
        WPAInfo wpaInfo = getWPAInfo(tags);

        // 获取RSN信息
        RSN rsnInfo = getRSNInfo(tags);

        // 检查加密方式

        return new Dot11Beacon(channel, BSSID, SSID, bandwidth, channel <= 14 ? Band.BAND2G : Band.BAND5G, vhtOpTag,
                htInfoTag, ehtOpTag, heOpTag, tags, frameProtocol, packetNum, stationCount, channelUsage, wpaInfo,
                rsnInfo, timestamp, interval, capbilitiesInfomation);
    }

    /*
     * 计算某个BSSID的邻频干扰
     */
    public List<Dot11Beacon> calculateAdjacentChannelInterference(List<Dot11Beacon> dot11beacon, String bssid) {
        Dot11Beacon targetBeacon = null;
        for (Dot11Beacon beacon : dot11beacon) {
            if (bytes2bssidStr(beacon.BSSID).equals(bssid)) {
                targetBeacon = beacon;
                break;
            }
        }
        int channel = targetBeacon.channel;
        int bandwidth = targetBeacon.BW.getBandwidth();
        if (bandwidth == -1) {
            return null;
        }
        int lowerChannel = channel - bandwidth / 10;
        int upperChannel = channel + bandwidth / 10;
        Map<String, Dot11Beacon> adjacentBeacons = new HashMap<String, Dot11Beacon>();
        for (Dot11Beacon beacon : dot11beacon) {
            int beaconUpperChannel = beacon.channel + beacon.BW.getBandwidth() != -1 ? beacon.BW.getBandwidth() / 10
                    : 0;
            int beaconLowerChannel = beacon.channel - beacon.BW.getBandwidth() != -1 ? beacon.BW.getBandwidth() / 10
                    : 0;
            if (beaconUpperChannel >= lowerChannel && beaconLowerChannel <= upperChannel
                    && !adjacentBeacons.containsKey(bytes2bssidStr(beacon.BSSID))) {
                adjacentBeacons.put(bytes2bssidStr(beacon.BSSID), beacon);
            }
        }
        List<Dot11Beacon> adjacentBeaconList = new ArrayList<Dot11Beacon>();
        for (String key : adjacentBeacons.keySet()) {
            adjacentBeaconList.add(adjacentBeacons.get(key));
        }
        return adjacentBeaconList;
    }

    /*
     * 对获取到的Dot11Beacon按照BSSID去重
     */
    public List<Dot11Beacon> bssidDeduplication() {
        Map<String, Dot11Beacon> result = new HashMap<String, Dot11Beacon>();
        this.uniqueBSSIDs = new ArrayList<Dot11Beacon>();
        for (Dot11Beacon beacon : this.beaconSourceData) {
            String BSSID = bytes2bssidStr(beacon.BSSID);
            result.put(BSSID, beacon);
        }
        for (String key : result.keySet()) {
            this.uniqueBSSIDs.add(result.get(key));
        }
        return uniqueBSSIDs;
    }

    /**
     * This method performs SSID deduplication on a list of Dot11Beacon objects.
     * It iterates through the list of beacons and adds each unique SSID to a map.
     * If an SSID is already present in the map, it is skipped.
     * Finally, it converts the map values to a list and returns the list of unique SSIDs.
     *
     * @return A list of Dot11Beacon objects with unique SSIDs.
     */
    public List<Dot11Beacon> ssidDeduplication() {
        Map<String, Dot11Beacon> result = new HashMap<String, Dot11Beacon>();
        this.uniqueSSIDs = new ArrayList<Dot11Beacon>();
        for (Dot11Beacon beacon : this.uniqueBSSIDs == null ? this.beaconSourceData
                : this.uniqueBSSIDs) {
            if (!result.containsKey(beacon.SSID)) {
                result.put(beacon.SSID, beacon);
            }
        }
        // List<Dot11Beacon> resultList = new ArrayList<Dot11Beacon>();
        for (String key : result.keySet()) {
            this.uniqueSSIDs.add(result.get(key));
        }
        return this.uniqueSSIDs;
    }

    /*
     * 将byte[]转换为bssid
     */
    public static String bytes2bssidStr(byte[] inputBytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : inputBytes) {
            if (Integer.toHexString(b & 0xFF).length() == 1) {
                sb.append("0");
            }
            sb.append(Integer.toHexString(b & 0xFF));
            sb.append(":");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString().toUpperCase();
    }

    public static void countProtocol(List<Dot11Beacon> dot11beacon) {
        int[] protoccolNum = new int[5];
        Map<String, Dot11Beacon> BEMap = new HashMap<String, Dot11Beacon>();
        Map<String, Dot11Beacon> AXMap = new HashMap<String, Dot11Beacon>();
        Map<String, Dot11Beacon> ACMap = new HashMap<String, Dot11Beacon>();
        Map<String, Dot11Beacon> GMap = new HashMap<String, Dot11Beacon>();
        Map<String, Dot11Beacon> ElseMap = new HashMap<String, Dot11Beacon>();
        for (Dot11Beacon beacon : dot11beacon) {
            String BSSID = bytes2bssidStr(beacon.BSSID);
            if (beacon.protocol == Protocol.DOT11BE && !BEMap.containsKey(BSSID)) {
                BEMap.put(BSSID, beacon);
            }
            if (beacon.protocol == Protocol.DOT11AX && !AXMap.containsKey(BSSID)) {
                AXMap.put(BSSID, beacon);
            }
            if (beacon.protocol == Protocol.DOT11AC && !ACMap.containsKey(BSSID)) {
                ACMap.put(BSSID, beacon);
            }
            if (beacon.protocol == Protocol.DOT11G && !GMap.containsKey(BSSID)) {
                GMap.put(BSSID, beacon);
            }
            if (beacon.protocol != Protocol.DOT11BE && beacon.protocol != Protocol.DOT11AX
                    && beacon.protocol != Protocol.DOT11AC && beacon.protocol != Protocol.DOT11G
                    && !ElseMap.containsKey(BSSID)) {
                ElseMap.put(BSSID, beacon);
            }
            switch (beacon.protocol) {
                case DOT11N:
                    protoccolNum[1]++;
                    break;
                case DOT11AC:
                    protoccolNum[2]++;
                    break;
                case DOT11AX:
                    protoccolNum[3]++;
                    break;
                case DOT11BE:
                    protoccolNum[4]++;
                    break;
                default:
                    protoccolNum[0]++;
                    break;
            }
        }
        // for (String key : bssidMap.keySet()) {
        // bssidMap.get(key).printThis();
        // }
        System.out.println("number of .11be: " + BEMap.size());
        System.out.println("number of .11ax: " + AXMap.size());
        System.out.println("number of .11ac: " + ACMap.size());
        System.out.println("number of .11g: " + GMap.size());
        System.out.println("number of other: " + ElseMap.size());
        // dot11beacon.get(dot11beacon.size() - 2).printThis();
    }

    public static List<Dot11Beacon> ssidFilter(List<Dot11Beacon> dot11beacon, String ssid) {
        Map<String, Dot11Beacon> result = new HashMap<String, Dot11Beacon>();
        for (Dot11Beacon beacon : dot11beacon) {
            // System.out.println(beacon.SSID);
            String BSSID = bytes2bssidStr(beacon.BSSID);
            if (beacon.SSID.equals(ssid) && !result.containsKey(BSSID)) {
                result.put(BSSID, beacon);
            }
        }
        List<Dot11Beacon> resultList = new ArrayList<Dot11Beacon>();
        for (String key : result.keySet()) {
            resultList.add(result.get(key));
        }
        return resultList;
    }

    public static List<Dot11Beacon> bssidFilter(List<Dot11Beacon> dot11beacon, byte[] bssid) {
        List<Dot11Beacon> result = new ArrayList<Dot11Beacon>();
        for (Dot11Beacon beacon : dot11beacon) {
            if (bytes2bssidStr(beacon.BSSID).equals(bytes2bssidStr(bssid))) {
                result.add(beacon);
            }
        }
        return result;
    }

    public static void serializeObject(Resolver resolver, String path) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(resolver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}