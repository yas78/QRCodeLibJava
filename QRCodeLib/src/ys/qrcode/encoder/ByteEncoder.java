package ys.qrcode.encoder;

import java.nio.charset.Charset;

import ys.qrcode.EncodingMode;
import ys.qrcode.format.ModeIndicator;

/**
 * バイトモードエンコーダー
 */
public class ByteEncoder extends QRCodeEncoder {
    private final Charset _charSet;

    /**
     * インスタンスを初期化します。
     */
    public ByteEncoder() {
        this(Charset.forName("Shift_JIS"));
    }

    /**
     * インスタンスを初期化します。
     *
     * @param charSet
     *            文字エンコーディング
     */
    public ByteEncoder(Charset charSet) {
        _charSet = charSet;
    }

    /**
     * 符号化モードを取得します。
     */
    @Override
    public EncodingMode getEncodingMode() {
        return EncodingMode.EIGHT_BIT_BYTE;
    }

    /**
     * モード指示子を取得します。
     */
    @Override
    public int getModeIndicator() {
        return ModeIndicator.BYTE_VALUE;
    }

    /**
     * 文字を追加します。
     *
     * @return 追加した文字のビット数
     */
    @Override
    public int append(char c) {
        assert isInSubset(c);

        byte[] charBytes = String.valueOf(c).getBytes(_charSet);
        int ret = 0;

        for (int i = 0; i < charBytes.length; i++) {
            _codeWords.add((int) charBytes[i]);
            _charCounter++;
            _bitCounter += 8;
            ret += 8;
        }

        return ret;
    }

    /**
     * 指定の文字をエンコードしたコード語のビット数を返します。
     */
    @Override
    public int getCodewordBitLength(char c) {
        assert isInSubset(c);

        byte[] charBytes = String.valueOf(c).getBytes(_charSet);

        return 8 * charBytes.length;
    }

    /**
     * エンコードされたデータのバイト配列を返します。
     */
    @Override
    public byte[] getBytes() {
        byte[] ret = new byte[_charCounter];

        for (int i = 0; i < _codeWords.size(); i++) {
            ret[i] = _codeWords.get(i).byteValue();
        }

        return ret;
    }

    /**
     * 指定した文字が、このモードの文字集合に含まれる場合は true を返します。
     */
    public static boolean isInSubset(char c) {
        return true;
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public static boolean isInExclusiveSubset(char c) {
        if (NumericEncoder.isInSubset(c)) {
            return false;
        }

        if (AlphanumericEncoder.isInSubset(c)) {
            return false;
        }

        if (KanjiEncoder.isInSubset(c)) {
            return false;
        }

        if (isInSubset(c)) {
            return true;
        }

        return false;
    }
}
