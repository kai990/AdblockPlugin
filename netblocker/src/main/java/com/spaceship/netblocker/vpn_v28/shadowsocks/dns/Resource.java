package com.spaceship.netblocker.vpn_v28.shadowsocks.dns;

import java.nio.ByteBuffer;

public class Resource {
    public String domain;
    public short type;
    public short clz;
    public int ttl;
    public short dataLength;
    public byte[] data;

    private int offset;

    static Resource fromBytes(ByteBuffer buffer) {
        Resource r = new Resource();
        r.offset = buffer.arrayOffset() + buffer.position();
        r.domain = DnsPacket.readDomain(buffer, buffer.arrayOffset());
        r.type = buffer.getShort();
        r.clz = buffer.getShort();
        r.ttl = buffer.getInt();
        r.dataLength = buffer.getShort();
        r.data = new byte[r.dataLength & 0xFFFF];
        buffer.get(r.data);
        return r;
    }
}
