package com.wireless.dot11;

public class Tag {
    public int tagNumber;
    public int tagLength;
    public TagType tagType;
    public byte[] tagValue;

    public Tag(int tagNumber, int tagLength, TagType tagType, byte[] tagValue) {
        this.tagNumber = tagNumber;
        this.tagLength = tagLength;
        this.tagType = tagType;
        this.tagValue = tagValue;
    }
}
