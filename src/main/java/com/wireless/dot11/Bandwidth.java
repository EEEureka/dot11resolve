package com.wireless.dot11;

public enum Bandwidth {
    BW20(20),
    BW20OR40(-1),
    BW40(40),
    BW80(80),
    BW160(160),
    BW80_80(-1),
    BW320(320);

    private Bandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    private int bandwidth;

    public int getBandwidth() {
        return this.bandwidth;
    }
}
