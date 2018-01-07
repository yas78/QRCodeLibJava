package ys.image;

import ys.qrcode.misc.BitConverter;

/**
 * BITMAPFILEHEADER構造体
 */
public class BITMAPFILEHEADER {
    public Short bfType      = 0;
    public int   bfSize      = 0;
    public short bfReserved1 = 0;
    public short bfReserved2 = 0;
    public int   bfOffBits   = 0;

    /**
     * この構造体のバイト配列を返します。
     */
    public byte[] getBytes() {
        byte[] ret = new byte[14];

        System.arraycopy(BitConverter.getBytes(bfType), 0, ret, 0, 2);
        System.arraycopy(BitConverter.getBytes(bfSize), 0, ret, 2, 4);
        System.arraycopy(BitConverter.getBytes(bfReserved1), 0, ret, 6, 2);
        System.arraycopy(BitConverter.getBytes(bfReserved2), 0, ret, 8, 2);
        System.arraycopy(BitConverter.getBytes(bfOffBits), 0, ret, 10, 4);

        return ret;
    }
}
