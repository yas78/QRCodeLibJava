package ys.qrcode.util;

public class BitConverter {

    public static byte[] getBytes(short value) {
        byte[] temp = new byte[2];
        temp[0] = (byte) ((value & 0xFF));
        temp[1] = (byte) ((value & 0xFF00) >>> 8);

        return temp;
    }

    public static byte[] getBytes(int value) {
        byte[] temp = new byte[4];
        temp[0] = (byte) ((value & 0xFF));
        temp[1] = (byte) ((value & 0xFF00) >>> 8);
        temp[2] = (byte) ((value & 0xFF0000) >>> 16);
        temp[3] = (byte) ((value & 0xFF000000) >>> 24);

        return temp;
    }

}
