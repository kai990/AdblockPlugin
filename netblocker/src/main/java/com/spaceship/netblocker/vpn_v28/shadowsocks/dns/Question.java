package com.spaceship.netblocker.vpn_v28.shadowsocks.dns;

import java.nio.ByteBuffer;

public class Question {
    public String domain;
    public short type;
    public short clz;

    private int offset;
    private int length;

    static Question fromBytes(ByteBuffer buffer) {
        Question q = new Question();
        q.offset = buffer.arrayOffset() + buffer.position();
        q.domain = DnsPacket.readDomain(buffer, buffer.arrayOffset());
        q.type = buffer.getShort();
        q.clz = buffer.getShort();
        q.length = buffer.arrayOffset() + buffer.position() - q.offset;
        return q;
    }

    public int offset() {
        return offset;
    }

    public int length() {
        return length;
    }
}
