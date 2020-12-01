package ys.qrcode.encoder;

import java.nio.charset.Charset;

import ys.qrcode.EncodingMode;
import ys.qrcode.format.ModeIndicator;

/**
 * バイトモードエンコーダー
 */
public class ByteEncoder extends QRCodeEncoder {
    private final Charset _charset;

    /**
     * インスタンスを初期化します。
     */
    public ByteEncoder() {
        this(Charset.forName("Shift_JIS"));
    }

    /**
     * インスタンスを初期化します。
     *
     * @param charset
     *            文字エンコーディング
     */
    public ByteEncoder(Charset charset) {
        _charset = charset;
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
        byte[] charBytes = String.valueOf(c).getBytes(_charset);

        for (byte value : charBytes) {
            _codeWords.add((int) value);
        }

        int ret = 8 * charBytes.length;
        _bitCounter += ret;
        _charCounter += charBytes.length;

        return ret;
    }

    /**
     * 指定の文字をエンコードしたコード語のビット数を返します。
     */
    @Override
    public int getCodewordBitLength(char c) {
        byte[] charBytes = String.valueOf(c).getBytes(_charset);

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
    public static boolean inSubset(char c) {
        return true;
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public static boolean inExclusiveSubset(char c) {
        if (AlphanumericEncoder.inSubset(c)) {
            return false;
        }

        if (KanjiEncoder.inSubset(c)) {
            return false;
        }

        return inSubset(c);
    }
}
