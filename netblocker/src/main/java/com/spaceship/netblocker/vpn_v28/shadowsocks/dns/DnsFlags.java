package com.spaceship.netblocker.vpn_v28.shadowsocks.dns;

class DnsFlags {
    private boolean qr;//1 bits
    private int opCode;//4 bits
    private boolean aa;//1 bits
    private boolean tc;//1 bits
    private boolean rd;//1 bits
    private boolean ra;//1 bits
    private int zero;//3 bits
    private int rCode;//4 bits

    static DnsFlags Parse(short value) {
        int flags = value & 0xFFFF;
        DnsFlags dnsFlags = new DnsFlags();
        dnsFlags.qr = ((flags >> 7) & 0x01) == 1;
        dnsFlags.opCode = (flags >> 3) & 0x0F;
        dnsFlags.aa = ((flags >> 2) & 0x01) == 1;
        dnsFlags.tc = ((flags >> 1) & 0x01) == 1;
        dnsFlags.rd = (flags & 0x01) == 1;
        dnsFlags.ra = (flags >> 15) == 1;
        dnsFlags.zero = (flags >> 12) & 0x07;
        dnsFlags.rCode = ((flags >> 8) & 0xF);
        return dnsFlags;
    }

    short ToShort() {
        int flags = 0;
        flags |= (this.qr ? 1 : 0) << 7;
        flags |= (this.opCode & 0x0F) << 3;
        flags |= (this.aa ? 1 : 0) << 2;
        flags |= (this.tc ? 1 : 0) << 1;
        flags |= this.rd ? 1 : 0;
        flags |= (this.ra ? 1 : 0) << 15;
        flags |= (this.zero & 0x07) << 12;
        flags |= (this.rCode & 0x0F) << 8;
        return (short) flags;
    }
}
