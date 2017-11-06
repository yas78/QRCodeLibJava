package ys.qrcode.encoder;

import ys.qrcode.EncodingMode;
import ys.qrcode.format.ModeIndicator;
import ys.qrcode.util.BitSequence;

/**
 * 英数字モードエンコーダー
 */
public class AlphanumericEncoder extends QRCodeEncoder {
    /**
     * インスタンスを初期化します。
     */
    public AlphanumericEncoder() { }

    /**
     * 符号化モードを取得します。
     */
    @Override
    public EncodingMode getEncodingMode() {
        return EncodingMode.ALPHA_NUMERIC;
    }

    /**
     * モード指示子を取得します。
     */
    @Override
    public int getModeIndicator() {
        return ModeIndicator.ALPAHNUMERIC_VALUE;
    }

    /**
     * 文字を追加します。
     *
     * @return 追加した文字のビット数
     */
    @Override
    public int append(char c) {
        assert isInSubset(c);

        int wd = convertCharCode(c);
        int ret;

        if (_charCounter % 2 == 0) {
            _codeWords.add(wd);
            ret = 6;

        } else {
            int temp = _codeWords.get(_codeWords.size() - 1);
            temp *= 45;
            temp += wd;
            _codeWords.set(_codeWords.size() - 1, temp);

            ret = 5;
        }

        _charCounter++;
        _bitCounter += ret;

        return ret;
    }

    /**
     * 指定の文字をエンコードしたコード語のビット数を返します。
     */
    @Override
    public int getCodewordBitLength(char c) {
        assert isInSubset(c);

        if (_charCounter % 2 == 0) {
            return 6;
        } else {
            return 5;
        }
    }

    /**
     * エンコードされたデータのバイト配列を返します。
     */
    @Override
    public byte[] getBytes() {

        BitSequence bs = new BitSequence();
        int bitLength = 11;

        for (int i = 0; i <= (_codeWords.size() - 1) - 1; i++) {
            bs.append(_codeWords.get(i), bitLength);
        }

        if ((_charCounter % 2) == 0) {
            bitLength = 11;
        } else {
            bitLength = 6;
        }

        bs.append(_codeWords.get(_codeWords.size() - 1), bitLength);

        return bs.getBytes();
    }

    /**
     * 指定した文字の、英数字モードにおけるコード値を返します。
     */
    private static int convertCharCode(char c) {

        if (c >= 'A' && c <= 'Z') {
            return c - 55;
        }

        if (c >= '0' && c <= '9') {
            return c - 48;
        }

        if (c == ' ') {
            return 36;
        }

        if (c == '$' || c == '%') {
            return c + 1;
        }

        if (c == '*' || c == '+') {
            return c - 3;
        }

        if (c == '-' || c == '.') {
            return c - 4;
        }

        if (c == '/') {
            return 43;
        }

        if (c == ':') {
            return 44;
        }

        throw new IllegalArgumentException("c");
    }

    /**
     * 指定した文字が、このモードの文字集合に含まれる場合は true を返します。
     */
    public static boolean isInSubset(char c) {
        return c >= 'A' && c <= 'Z' ||
               c >= '0' && c <= '9' ||
               c == ' '             ||
               c == '.'             ||
               c == '-'             ||
               c == '$'             ||
               c == '%'             ||
               c == '*'             ||
               c == '+'             ||
               c == '/'             ||
               c == ':';
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public static boolean isInExclusiveSubset(char c) {
        if (NumericEncoder.isInSubset(c)) {
            return false;
        }

        if (isInSubset(c)) {
            return true;
        }

        return false;
    }
}
