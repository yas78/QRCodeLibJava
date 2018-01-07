package ys.image;

import ys.qrcode.misc.BitConverter;

/**
 * BITMAPINFOHEADER構造体
 */
public class BITMAPINFOHEADER {
    public int   biSize          = 0;
    public int   biWidth         = 0;
    public int   biHeight        = 0;
    public short biPlanes        = 0;
    public short biBitCount      = 0;
    public int   biCompression   = 0;
    public int   biSizeImage     = 0;
    public int   biXPelsPerMeter = 0;
    public int   biYPelsPerMeter = 0;
    public int   biClrUsed       = 0;
    public int   biClrImportant  = 0;

    /**
     * この構造体のバイト配列を返します。
     */
    public byte[] getBytes() {
        byte[] ret = new byte[40];

        System.arraycopy(BitConverter.getBytes(biSize), 0, ret, 0, 4);
        System.arraycopy(BitConverter.getBytes(biWidth), 0, ret, 4, 4);
        System.arraycopy(BitConverter.getBytes(biHeight), 0, ret, 8, 4);
        System.arraycopy(BitConverter.getBytes(biPlanes), 0, ret, 12, 2);
        System.arraycopy(BitConverter.getBytes(biBitCount), 0, ret, 14, 2);
        System.arraycopy(BitConverter.getBytes(biCompression), 0, ret, 16, 4);
        System.arraycopy(BitConverter.getBytes(biSizeImage), 0, ret, 20, 4);
        System.arraycopy(BitConverter.getBytes(biXPelsPerMeter), 0, ret, 24, 4);
        System.arraycopy(BitConverter.getBytes(biYPelsPerMeter), 0, ret, 28, 4);
        System.arraycopy(BitConverter.getBytes(biClrUsed), 0, ret, 32, 4);
        System.arraycopy(BitConverter.getBytes(biClrImportant), 0, ret, 36, 4);

        return ret;
    }
}
