package ys.image;

/**
 * RGBQUAD構造体
 */
public class RGBQUAD {
    public byte rgbBlue     = 0;
    public byte rgbGreen    = 0;
    public byte rgbRed      = 0;
    public byte rgbReserved = 0;

    /**
     * この構造体のバイト配列を返します。
     */
    public byte[] getBytes() {
        return new byte[] { rgbBlue, rgbGreen, rgbRed, rgbReserved };
    }

}
