package com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip;

public class IPHeader {

    public static final short IP = 0x0800;
    public static final byte ICMP = 1;
    public static final byte TCP = 6;
    public static final byte UDP = 17;

    static final int offset_src_ip = 12; // 12: Source address
    static final int offset_op_pad = 20; // 20: Option + Padding
    private static final byte offset_ver_ihl = 0; // 0: Version (4 bits) + Internet header length (4// bits)
    private static final byte offset_tos = 1; // 1: Type of service
    private static final short offset_tlen = 2; // 2: Total length
    private static final short offset_identification = 4; // :4 Identification
    private static final short offset_flags_fo = 6; // 6: Flags (3 bits) + Fragment offset (13 bits)
    private static final byte offset_ttl = 8; // 8: Time to live
    private static final byte offset_proto = 9; // 9: Protocol
    private static final short offset_crc = 10; // 10: Header checksum
    private static final int offset_dest_ip = 16; // 16: Destination address
    public byte[] data;
    public int offset;

    public IPHeader(byte[] data, int offset) {
        this.data = data;
        this.offset = offset;
    }

    public void Default() {
        setHeaderLength(20);
        setTos((byte) 0);
        setTotalLength(0);
        setIdentification(0);
        setFlagsAndOffset((short) 0);
        setTTL((byte) 64);
    }

    public int getDataLength() {
        return this.getTotalLength() - this.getHeaderLength();
    }

    public int getHeaderLength() {
        return (data[offset + offset_ver_ihl] & 0x0F) * 4;
    }

    public void setHeaderLength(int value) {
        data[offset + offset_ver_ihl] = (byte) ((4 << 4) | (value / 4));
    }

    public byte getTos() {
        return data[offset + offset_tos];
    }

    public void setTos(byte value) {
        data[offset + offset_tos] = value;
    }

    public int getTotalLength() {
        return CommonMethods.readShort(data, offset + offset_tlen) & 0xFFFF;
    }

    public void setTotalLength(int value) {
        CommonMethods.writeShort(data, offset + offset_tlen, (short) value);
    }

    public int getIdentification() {
        return CommonMethods.readShort(data, offset + offset_identification) & 0xFFFF;
    }

    public void setIdentification(int value) {
        CommonMethods.writeShort(data, offset + offset_identification, (short) value);
    }

    public short getFlagsAndOffset() {
        return CommonMethods.readShort(data, offset + offset_flags_fo);
    }

    public void setFlagsAndOffset(short value) {
        CommonMethods.writeShort(data, offset + offset_flags_fo, value);
    }

    public byte getTTL() {
        return data[offset + offset_ttl];
    }

    public void setTTL(byte value) {
        data[offset + offset_ttl] = value;
    }

    public byte getProtocol() {
        return data[offset + offset_proto];
    }

    public void setProtocol(byte value) {
        data[offset + offset_proto] = value;
    }

    public short getCrc() {
        return CommonMethods.readShort(data, offset + offset_crc);
    }

    public void setCrc(short value) {
        CommonMethods.writeShort(data, offset + offset_crc, value);
    }

    public int getSourceIP() {
        return CommonMethods.readInt(data, offset + offset_src_ip);
    }

    public void setSourceIP(int value) {
        CommonMethods.writeInt(data, offset + offset_src_ip, value);
    }

    public int getDestinationIP() {
        return CommonMethods.readInt(data, offset + offset_dest_ip);
    }

    public void setDestinationIP(int value) {
        CommonMethods.writeInt(data, offset + offset_dest_ip, value);
    }

    @Override
    public String toString() {
        return String.format("%s->%s Pro=%s,HLen=%d", CommonMethods.ipIntToString(getSourceIP()), CommonMethods.ipIntToString(getDestinationIP()), getProtocol(), getHeaderLength());
    }

}
