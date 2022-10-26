package com.spaceship.netblocker.utils;


public class BitOperationUtils {

    public static int shortAndInt(short a, int b) {
        return a & b;
    }

    public static int shortToInt(short a) {
        return a & 0xFFFF;
    }

    public static int byteAndInt(byte a, int b) {
        return a & b;
    }
}
