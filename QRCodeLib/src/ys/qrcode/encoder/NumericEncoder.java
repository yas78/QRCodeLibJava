package ys.qrcode.encoder;

import ys.qrcode.EncodingMode;
import ys.qrcode.format.ModeIndicator;
import ys.qrcode.misc.BitSequence;

/**
 * 数字モードエンコーダー
 */
public class NumericEncoder extends QRCodeEncoder {

    /**
     * インスタンスを初期化します。
     */
    public NumericEncoder() { }

    /**
     * 符号化モードを取得します。
     */
    @Override
    public EncodingMode getEncodingMode() {
        return EncodingMode.NUMERIC;
    }

    /**
     * モード指示子を取得します。
     */
    @Override
    public int getModeIndicator() {
        return ModeIndicator.NUMERIC_VALUE;
    }

    /**
     * 文字を追加します。
     *
     * @param c
     *            追加する文字
     * @return 追加した文字のビット数
     */
    @Override
    public int append(char c) {
        assert isInSubset(c);

        int wd = Integer.parseInt(String.valueOf(c));
        int ret;

        if (_charCounter % 3 == 0) {
            _codeWords.add(wd);
            ret = 4;
        } else {
            int temp = _codeWords.get(_codeWords.size() - 1);
            temp *= 10;
            temp += wd;
            _codeWords.set(_codeWords.size() - 1, temp);
            ret = 3;
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

        if (_charCounter % 3 == 0) {
            return 4;
        } else {
            return 3;
        }
    }

    /**
     * エンコードされたデータのバイト配列を返します。
     */
    @Override
    public byte[] getBytes() {
        BitSequence bs = new BitSequence();
        int bitLength = 10;

        for (int i = 0; i <= (_codeWords.size() - 1) - 1; i++) {
            bs.append(_codeWords.get(i), bitLength);
        }

        switch (_charCounter % 3) {
        case 1:
            bitLength = 4;
            break;
        case 2:
            bitLength = 7;
            break;
        default:
            bitLength = 10;
            break;
        }

        bs.append(_codeWords.get(_codeWords.size() - 1), bitLength);

        return bs.getBytes();
    }

    /**
     * 指定した文字が、このモードの文字集合に含まれる場合は true を返します。
     */
    public static boolean isInSubset(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public static boolean isInExclusiveSubset(char c) {
        return isInSubset(c);
    }
}
