package com.spaceship.netblocker.vpn_v28.shadowsocks.dns;

import java.nio.ByteBuffer;

public class DnsPacket {
    public DnsHeader header;
    public Question[] questions;
    public Resource[] resources;
    public int size;
    private Resource[] aResources;
    private Resource[] eResources;

    public static DnsPacket fromBytes(ByteBuffer buffer) {
        if (buffer.limit() < 12)
            return null;
        if (buffer.limit() > 512)
            return null;

        DnsPacket packet = new DnsPacket();
        packet.size = buffer.limit();
        packet.header = DnsHeader.fromBytes(buffer);

        if (packet.header.questionCount > 2 || packet.header.resourceCount > 50 || packet.header.aResourceCount > 50 || packet.header.eResourceCount > 50) {
            return null;
        }

        packet.questions = new Question[packet.header.questionCount];
        packet.resources = new Resource[packet.header.resourceCount];
        packet.aResources = new Resource[packet.header.aResourceCount];
        packet.eResources = new Resource[packet.header.eResourceCount];

        for (int i = 0; i < packet.questions.length; i++) {
            packet.questions[i] = Question.fromBytes(buffer);
        }

        for (int i = 0; i < packet.resources.length; i++) {
            packet.resources[i] = Resource.fromBytes(buffer);
        }

        for (int i = 0; i < packet.aResources.length; i++) {
            packet.aResources[i] = Resource.fromBytes(buffer);
        }

        for (int i = 0; i < packet.eResources.length; i++) {
            packet.eResources[i] = Resource.fromBytes(buffer);
        }

        return packet;
    }

    static String readDomain(ByteBuffer buffer, int dnsHeaderOffset) {
        StringBuilder sb = new StringBuilder();
        int len = 0;
        while (buffer.hasRemaining() && (len = (buffer.get() & 0xFF)) > 0) {
            if ((len & 0xc0) == 0xc0)
            {
                int pointer = buffer.get() & 0xFF;
                pointer |= (len & 0x3F) << 8;

                ByteBuffer newBuffer = ByteBuffer.wrap(buffer.array(), dnsHeaderOffset + pointer, dnsHeaderOffset + buffer.limit());
                sb.append(readDomain(newBuffer, dnsHeaderOffset));
                return sb.toString();
            } else {
                while (len > 0 && buffer.hasRemaining()) {
                    sb.append((char) (buffer.get() & 0xFF));
                    len--;
                }
                sb.append('.');
            }
        }

        if (len == 0 && sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    static void writeDomain(String domain, ByteBuffer buffer) {
        if (domain == null || domain.equals("")) {
            buffer.put((byte) 0);
            return;
        }

        String[] arr = domain.split("\\.");
        for (String item : arr) {
            if (arr.length > 1) {
                buffer.put((byte) item.length());
            }

            for (int i = 0; i < item.length(); i++) {
                buffer.put((byte) item.codePointAt(i));
            }
        }
    }
}
