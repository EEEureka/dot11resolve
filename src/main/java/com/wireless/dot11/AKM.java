package com.wireless.dot11;

public enum AKM {
    DOT1X("802.1x"),
    PSK("WPA2-PSK"),
    FTDOT1X("Fast Transition 802.1x"),
    FTPSK("Fast Transition WPA2-PSK"),
    DOT1XSHA256("802.1x SHA256"),
    PSKSHA256("PSK SHA256"),
    TDLS("TDLS"),
    WPA3SAE("WPA3-SAE"),
    FTWPA3SAE("Fast Transition WPA3-SAE"),
    WPA("WPA"),
    OWE("Oppotunistic Wireless Encryption");
    /*
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

    private AKM(String alias) {
        this.alias = alias;
    }

    private String alias;

    public String getAlias() {
        return alias;
    }
}
