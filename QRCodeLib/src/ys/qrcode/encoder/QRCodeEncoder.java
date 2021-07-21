package ys.qrcode.encoder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import ys.qrcode.EncodingMode;

/**
 * エンコーダーの基本抽象クラス
 */
public abstract class QRCodeEncoder {
    List<Integer> _codeWords = new ArrayList<Integer>();

    int _charCounter = 0;
    int _bitCounter  = 0;

    final Charset _charset;

    public QRCodeEncoder(Charset charset) {
        _charset = charset;
    }

    /**
     * 符号化モードを取得します。
     */
    public abstract EncodingMode getEncodingMode();

    /**
     * モード指示子を取得します。
     */
    public abstract int getModeIndicator();

    /**
     * 文字を追加します。
     */
    public int getCharCount() {
        return _charCounter;
    }

    /**
     * データビット数を取得します。
     */
    public int getBitCount() {
        return _bitCounter;
    }

    /**
     * 文字を追加します。
     */
    public abstract void append(char c);

    /**
     * 指定の文字をエンコードしたコード語のビット数を返します。
     */
    public abstract int getCodewordBitLength(char c);

    /**
     * エンコードされたデータのバイト配列を返します。
     */
    public abstract byte[] getBytes();

    /**
     * 指定した文字が、このモードの文字集合に含まれる場合は true を返します。
     */
    public abstract boolean inSubset(char c);

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public abstract boolean inExclusiveSubset(char c);
}
