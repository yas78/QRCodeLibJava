package ys.qrcode.encoder;

import java.nio.charset.Charset;

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
    public NumericEncoder(Charset charset) {
        super(charset);
    }

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
        int wd = Integer.parseInt(String.valueOf(c));

        if (_charCounter % 3 == 0) {
            _codeWords.add(wd);
        } else {
            int temp = _codeWords.get(_codeWords.size() - 1);
            temp *= 10;
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

        for (int i = 0; i < _codeWords.size() - 1; i++) {
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
    public boolean inSubset(char c) {
        return '0' <= c && c <= '9';
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public boolean inExclusiveSubset(char c) {
        return inSubset(c);
    }
}
