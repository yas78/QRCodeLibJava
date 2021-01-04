package ys.qrcode.encoder;

import ys.qrcode.EncodingMode;
import ys.qrcode.format.ModeIndicator;
import ys.qrcode.misc.BitSequence;

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
        int wd = convertCharCode(c);

        if (_charCounter % 2 == 0) {
            _codeWords.add(wd);
        } else {
            int temp = _codeWords.get(_codeWords.size() - 1);
            temp *= 45;
            temp += wd;
            _codeWords.set(_codeWords.size() - 1, temp);
        }

        int ret = getCodewordBitLength(c);
        _bitCounter += ret;
        _charCounter++;

        return ret;
    }

    /**
     * 指定の文字をエンコードしたコード語のビット数を返します。
     */
    @Override
    public int getCodewordBitLength(char c) {
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
        if ('0' <= c && c <= '9') {
            return c - 48;
        }
        if (c == ':') {
            return 44;
        }
        if ('A' <= c && c <= 'Z') {
            return c - 55;
        }

        return -1;
    }

    /**
     * 指定した文字が、このモードの文字集合に含まれる場合は true を返します。
     */
    public static boolean inSubset(char c) {
        return convertCharCode(c) > -1;
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public static boolean inExclusiveSubset(char c) {
        if (NumericEncoder.inSubset(c)) {
            return false;
        }

        return inSubset(c);
    }
}
