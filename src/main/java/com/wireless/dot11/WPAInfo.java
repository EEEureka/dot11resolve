package com.wireless.dot11;

import java.util.List;

public class WPAInfo {
    public String OUI = "00:50:f2";
    public int wpaVersion;
    public byte[] multicastCipherSuite;
    public List<byte[]> unicastCipherSuite;
    public List<byte[]> akmSuite;

    public WPAInfo(int wpaVersion, byte[] multicastCipherSuite, List<byte[]> unicastCipherSuite,
            List<byte[]> akmSuite) {
        this.wpaVersion = wpaVersion;
        this.multicastCipherSuite = multicastCipherSuite;
        this.unicastCipherSuite = unicastCipherSuite;
        this.akmSuite = akmSuite;
    }
}
