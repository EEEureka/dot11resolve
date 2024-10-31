package com.wireless.dot11;

import java.util.ArrayList;
import java.util.List;

public class Dot11Beacon {
    public int channel;
    public byte[] BSSID;
    public String SSID;
    public Bandwidth BW;
    public Band band;
    public Protocol protocol;
    public Tag vhtTag = null;
    public Tag htTag = null;
    public Tag ehtTag = null;
    public Tag heTag = null;
    public List<Tag> tags;
    public int frameSerial;
    public int stationCount;
    public int channelUsage; // 信道利用率，保存的值为0-255， 利用率 = channelUsage / 255
    public WPAInfo wpaInfo = null;
    public RSN rsnInfo = null;
    public List<String> authMethods = null;
    public int timestamp;
    public int beaconInterval;
    public byte[] capabilities;
    public boolean isESS;

    // public Dot11Beacon(int channel, byte[] BSSID, String SSID, Bandwidth BW, Band band, Tag vhtTag, Tag htTag, Tag ehtTag, Tag heTag, List<Tag> tags, Protocol protocol, int frameCount, int staCount, int channelUsage, WPAInfo wpaInfo, RSN rsnInfo, int timestamp, int beaconInterval, byte[] capabilities) {
    public Dot11Beacon(
        int channel, 
        byte[] BSSID, 
        String SSID, 
        Bandwidth BW, 
        Band band, 
        Tag vhtTag, 
        Tag htTag, 
        Tag ehtTag, 
        Tag heTag, 
        List<Tag> tags, 
        Protocol protocol, 
        int frameCount, 
        int staCount, 
        int channelUsage, 
        WPAInfo wpaInfo, 
        RSN rsnInfo, 
        int timestamp, 
        int beaconInterval, 
        byte[] capabilities
    ) {
        this.channel = channel;
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.BW = BW;
        this.band = band;
        this.vhtTag = vhtTag;
        this.htTag = htTag;
        this.ehtTag = ehtTag;
        this.heTag = heTag;
        this.tags = tags;
        this.protocol = protocol;
        this.frameSerial = frameCount;
        this.stationCount = staCount;
        this.channelUsage = channelUsage;
        this.wpaInfo = wpaInfo;
        this.rsnInfo = rsnInfo;
        this.timestamp = timestamp;
        this.beaconInterval = beaconInterval;
        this.capabilities = capabilities;
        this.isESS = this.capabilities != null?(this.capabilities[1] & 0x01) == 1:false;
        this.authMethods = this.getAuthMethods();
    }

    private void printBSSID() {
        System.out.print("BSSID: ");
        int i = 0;
        for (byte b : BSSID) {
            if (i++ == 5) {
                System.out.print(Integer.toHexString(b & 0xFF));
            } else {
                System.out.print(Integer.toHexString(b & 0xFF) + ":");
            }
        }
        System.out.println();
    }

    public void printThis() {
        System.out.println("No." + frameSerial);
        System.out.println("Channel: " + channel);
        this.printBSSID();
        System.out.println("SSID: " + SSID);
        System.out.println("BW: " + BW);
        System.out.println("Band: " + band);
        System.out.println("Protocol: " + protocol);
        this.printAuthMethods();
        System.out.println();
        System.out.println();
    }
    
    private List<String> getAuthMethods(){
        List<String> authMethods = new ArrayList<String>();
        boolean wpaAuth = false;
        boolean rsnAuth = false;
        if(this.wpaInfo != null && (this.wpaInfo.akmSuite.get(0)[3] & 0xff) == 2){
            // System.out.println("called");
            authMethods.add("WPA-PSK");
            wpaAuth = true;
        }
        if(this.wpaInfo != null && (this.wpaInfo.akmSuite.get(0)[3] & 0xff) == 1){
            authMethods.add("WPA");
            wpaAuth = true;
        }
        if(this.rsnInfo != null){
            authMethods.add(this.rsnInfo.akm.getAlias());
            rsnAuth = true;
        }

        if (!wpaAuth && !rsnAuth) {
            authMethods.add("OPEN");
        }
        return authMethods;
    }

    private void printAuthMethods(){
        System.out.print("Auth Methods: ");
        for(String authMethod : authMethods){
            System.out.print(authMethod + " ");
        }
        System.out.println();
    }

    public String getAuthMethod(){
        String result = "";
        if(this.authMethods.size() == 0){
            return "[OPEN]";
        }
        for(String authMethod : this.authMethods){
            result += "[" + authMethod + "]";
        }
        return result;
    }
}