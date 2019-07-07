package ys.qrcode.misc;

public class BitConverter {
    public static byte[] getBytes(short value) {
        byte[] ret = new byte[2];
        ret[0] = (byte) ((value & 0xFF));
        ret[1] = (byte) ((value & 0xFF00) >>> 8);

        return ret;
    }

    public static byte[] getBytes(int value) {
        byte[] ret = new byte[4];
        ret[0] = (byte) ((value & 0xFF));
        ret[1] = (byte) ((value & 0xFF00) >>> 8);
        ret[2] = (byte) ((value & 0xFF0000) >>> 16);
        ret[3] = (byte) ((value & 0xFF000000) >>> 24);

        return ret;
    }
}
