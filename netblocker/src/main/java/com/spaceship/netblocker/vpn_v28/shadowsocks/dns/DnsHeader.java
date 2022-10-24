package com.spaceship.netblocker.vpn_v28.shadowsocks.dns;

import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods;

import java.nio.ByteBuffer;


public class DnsHeader {
    private static final short offset_ID = 0;
    private static final short offset_Flags = 2;
    private static final short offset_QuestionCount = 4;
    private static final short offset_ResourceCount = 6;
    private static final short offset_AResourceCount = 8;
    private static final short offset_EResourceCount = 10;
    public short id;
    public short questionCount;
    public short resourceCount;
    short aResourceCount;
    short eResourceCount;
    private DnsFlags dnsFlags;
    private byte[] Data;
    private int Offset;

    private DnsHeader(byte[] data, int offset) {
        this.Offset = offset;
        this.Data = data;
    }

    static DnsHeader fromBytes(ByteBuffer buffer) {
        DnsHeader header = new DnsHeader(buffer.array(), buffer.arrayOffset() + buffer.position());
        header.id = buffer.getShort();
        header.dnsFlags = DnsFlags.Parse(buffer.getShort());
        header.questionCount = buffer.getShort();
        header.resourceCount = buffer.getShort();
        header.aResourceCount = buffer.getShort();
        header.eResourceCount = buffer.getShort();
        return header;
    }

    void ToBytes(ByteBuffer buffer) {
        buffer.putShort(this.id);
        buffer.putShort(this.dnsFlags.ToShort());
        buffer.putShort(this.questionCount);
        buffer.putShort(this.resourceCount);
        buffer.putShort(this.aResourceCount);
        buffer.putShort(this.eResourceCount);
    }

    public short getId() {
        return CommonMethods.readShort(Data, Offset + offset_ID);
    }

    public void setId(short value) {
        CommonMethods.writeShort(Data, Offset + offset_ID, value);
    }

    public short getQuestionCount() {
        return CommonMethods.readShort(Data, Offset + offset_QuestionCount);
    }

    public void setQuestionCount(short value) {
        CommonMethods.writeShort(Data, Offset + offset_QuestionCount, value);
    }

    public short getResourceCount() {
        return CommonMethods.readShort(Data, Offset + offset_ResourceCount);
    }

    public void setResourceCount(short value) {
        CommonMethods.writeShort(Data, Offset + offset_ResourceCount, value);
    }

    public short getAResourceCount() {
        return CommonMethods.readShort(Data, Offset + offset_AResourceCount);
    }

    public void setAResourceCount(short value) {
        CommonMethods.writeShort(Data, Offset + offset_AResourceCount, value);
    }

    public short getDnsFlags() {
        return CommonMethods.readShort(Data, Offset + offset_Flags);
    }

    public void setDnsFlags(short value) {
        CommonMethods.writeShort(Data, Offset + offset_Flags, value);
    }

    public short geteResourceCount() {
        return CommonMethods.readShort(Data, Offset + offset_EResourceCount);
    }

    public void seteResourceCount(short value) {
        CommonMethods.writeShort(Data, Offset + offset_EResourceCount, value);
    }
}
