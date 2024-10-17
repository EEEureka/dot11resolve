package com.wireless.dot11;

public enum Bandwidth {
    BW20,
    BW20OR40,
    BW40,
    BW80,
    BW160,
    BW80_80,
    BW320;

    private Bandwidth() {
    }

    public int getBandwidth() {
        switch (this) {
            case BW20:
                return 20;
            case BW20OR40:
                return -1;
            case BW40:
                return 40;
            case BW80:
                return 80;
            case BW160:
                return 160;
            case BW80_80:
                return -1;
            case BW320:
                return 320;
            default:
                return 0;
        }
    }
}
