//package com.spaceship.netblocker.vpn_v28.shadowsocks.core;
//
//import android.util.SparseIntArray;
//
//import com.spaceship.netblocker.vpn_v28.shadowsocks.tcpip.CommonMethods;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//
//class ChinaIpMaskManager {
//
//    private static SparseIntArray CHINA_IP_MASK_DICT = new SparseIntArray(3000);
//    private static SparseIntArray MASK_DICT = new SparseIntArray();
//
//    static boolean isIPInChina(int ip) {
//        boolean found = false;
//        for (int i = 0; i < MASK_DICT.size(); i++) {
//            int mask = MASK_DICT.keyAt(i);
//            int networkIP = ip & mask;
//            int mask2 = CHINA_IP_MASK_DICT.get(networkIP);
//            if (mask2 == mask) {
//                found = true;
//                break;
//            }
//        }
//        return found;
//    }
//
//    static void loadFromFile(InputStream inputStream) {
//        int count;
//        try {
//            byte[] buffer = new byte[4096];
//            while ((count = inputStream.read(buffer)) > 0) {
//                for (int i = 0; i < count; i += 8) {
//                    int ip = CommonMethods.readInt(buffer, i);
//                    int mask = CommonMethods.readInt(buffer, i + 4);
//                    CHINA_IP_MASK_DICT.put(ip, mask);
//                    MASK_DICT.put(mask, mask);
//                    //System.out.printf("%s/%s\n", CommonMethods.IP2String(ip),CommonMethods.IP2String(mask));
//                }
//            }
//            inputStream.close();
//            System.out.printf("ChinaIpMask records count: %d\n", CHINA_IP_MASK_DICT.size());
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//    }
//}
