package com.wireless.dot11;

import java.util.List;

public class RSN {
    /*
     * 逻辑:
     * 1. 先查看是否有id为221的厂商tag， 且OUI为00:50:f2, vendor specific OUI tag:1, 检查akm字段为2
     * 2. 再检查是否有id为48的RSN tag， 检查akm字段如下进行解析
     * 
     * 1: DOT1X
     * 2: PSK
     * 3: FT-DOT1X
     * 4: FT-PSK
     * 5: DOT1X-SHA256
     * 6: PSK-SHA256
     * 7: TDLS
     * 8: WPA3-SAE
     * 9: FT-WPA3-SAE
     * 18: OWE
     */

    public String OUI = "00:50:f2";
    public int RSNVersion;
    public byte[] groupCipherSuite;
    public List<byte[]> pairwiseCipherSuite;
    public List<Integer> akmSuiteType;
    public byte[] RSNCapability;
    public AKM akm;

    public RSN(int RSNVersion, byte[] groupCipherSuite, List<byte[]> pairwiseCipherSuite, List<Integer> akmSuiteType,
            byte[] RSNCapability, AKM akm) {
        this.RSNVersion = RSNVersion;
        this.groupCipherSuite = groupCipherSuite;
        this.pairwiseCipherSuite = pairwiseCipherSuite;
        this.akmSuiteType = akmSuiteType;
        this.RSNCapability = RSNCapability;
        this.akm = akm;
    }
}