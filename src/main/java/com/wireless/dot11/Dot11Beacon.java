package com.wireless.dot11;

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

    public Dot11Beacon(int channel, byte[] BSSID, String SSID, Bandwidth BW, Band band, Tag vhtTag, Tag htTag,
            Tag ehtTag, Tag heTag, List<Tag> tags, Protocol protocol, int frameCount, int staCount) {
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
        System.out.println();
        System.out.println();
    }
}
