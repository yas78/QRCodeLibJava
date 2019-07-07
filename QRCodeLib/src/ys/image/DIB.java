package ys.image;

import java.awt.Color;

public class DIB {

    public static byte[] build1bppDIB(
            byte[] bitmapData, int width, int height, Color foreColor, Color backColor) {

        BITMAPFILEHEADER bfh = new BITMAPFILEHEADER();
        bfh.bfType          = 0x4D42;
        bfh.bfSize          = 62 + bitmapData.length;
        bfh.bfReserved1     = 0;
        bfh.bfReserved2     = 0;
        bfh.bfOffBits       = 62;

        BITMAPINFOHEADER bih = new BITMAPINFOHEADER();
        bih.biSize          = 40;
        bih.biWidth         = width;
        bih.biHeight        = height;
        bih.biPlanes        = 1;
        bih.biBitCount      = 1;
        bih.biCompression   = 0;
        bih.biSizeImage     = 0;
        bih.biXPelsPerMeter = 3780; // 96dpi
        bih.biYPelsPerMeter = 3780; // 96dpi
        bih.biClrUsed       = 0;
        bih.biClrImportant  = 0;

        RGBQUAD[] palette = new RGBQUAD[] { new RGBQUAD(), new RGBQUAD() };

        palette[0].rgbBlue      = (byte) foreColor.getBlue();
        palette[0].rgbGreen     = (byte) foreColor.getGreen();
        palette[0].rgbRed       = (byte) foreColor.getRed();
        palette[0].rgbReserved  = 0;

        palette[1].rgbBlue      = (byte) backColor.getBlue();
        palette[1].rgbGreen     = (byte) backColor.getGreen();
        palette[1].rgbRed       = (byte) backColor.getRed();
        palette[1].rgbReserved  = 0;

        byte[] ret = new byte[62 + bitmapData.length];

        byte[] bytes;
        int offset = 0;

        bytes = bfh.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = bih.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = palette[0].getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = palette[1].getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = bitmapData;
        System.arraycopy(bytes, 0, ret, offset, bytes.length);

        return ret;
    }

    public static byte[] build24bppDIB(byte[] bitmapData, int width, int height) {
        BITMAPFILEHEADER bfh = new BITMAPFILEHEADER();
        bfh.bfType      = 0x4D42;
        bfh.bfSize      = 54 + bitmapData.length;
        bfh.bfReserved1 = 0;
        bfh.bfReserved2 = 0;
        bfh.bfOffBits   = 54;

        BITMAPINFOHEADER bih = new BITMAPINFOHEADER();
        bih.biSize          = 40;
        bih.biWidth         = width;
        bih.biHeight        = height;
        bih.biPlanes        = 1;
        bih.biBitCount      = 24;
        bih.biCompression   = 0;
        bih.biSizeImage     = 0;
        bih.biXPelsPerMeter = 3780; // 96dpi
        bih.biYPelsPerMeter = 3780; // 96dpi
        bih.biClrUsed       = 0;
        bih.biClrImportant  = 0;

        byte[] ret = new byte[54 + bitmapData.length];

        byte[] bytes;
        int offset = 0;

        bytes = bfh.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = bih.getBytes();
        System.arraycopy(bytes, 0, ret, offset, bytes.length);
        offset += bytes.length;

        bytes = bitmapData;
        System.arraycopy(bytes, 0, ret, offset, bytes.length);

        return ret;
    }
}
