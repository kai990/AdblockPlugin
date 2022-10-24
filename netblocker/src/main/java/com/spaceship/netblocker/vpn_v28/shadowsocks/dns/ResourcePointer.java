package com.spaceship.netblocker.vpn_v28.shadowsocks.dns;

import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods;

public class ResourcePointer {
    private static final short offset_Domain = 0;
    private static final short offset_Type = 2;
    private static final short offset_Class = 4;
    private static final int offset_TTL = 6;
    private static final short offset_DataLength = 10;
    private static final int offset_IP = 12;

    private byte[] data;
    private int offset;

    public ResourcePointer(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    public void setDomain(short value) {
        CommonMethods.writeShort(data, offset + offset_Domain, value);
    }

    public short getType() {
        return CommonMethods.readShort(data, offset + offset_Type);
    }

    public void setType(short value) {
        CommonMethods.writeShort(data, offset + offset_Type, value);
    }

    public short getClass(short value) {
        return CommonMethods.readShort(data, offset + offset_Class);
    }

    public void setClass(short value) {
        CommonMethods.writeShort(data, offset + offset_Class, value);
    }

    public int getTTL() {
        return CommonMethods.readInt(data, offset + offset_TTL);
    }

    public void setTTL(int value) {
        CommonMethods.writeInt(data, offset + offset_TTL, value);
    }

    public short getDataLength() {
        return CommonMethods.readShort(data, offset + offset_DataLength);
    }

    public void setDataLength(short value) {
        CommonMethods.writeShort(data, offset + offset_DataLength, value);
    }

    public int getIP() {
        return CommonMethods.readInt(data, offset + offset_IP);
    }

    public void setIP(int value) {
        CommonMethods.writeInt(data, offset + offset_IP, value);
    }
}
