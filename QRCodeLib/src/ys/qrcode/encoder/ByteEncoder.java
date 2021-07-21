package ys.qrcode.encoder;

import java.nio.charset.Charset;

import ys.qrcode.EncodingMode;
import ys.qrcode.format.ModeIndicator;

/**
 * バイトモードエンコーダー
 */
public class ByteEncoder extends QRCodeEncoder {
    private final AlphanumericEncoder _encAlpha;
    private final KanjiEncoder        _encKanji;

    /**
     * インスタンスを初期化します。
     *
     * @param charset
     *            文字セット
     */
    public ByteEncoder(Charset charset) {
        super(charset);
        _encAlpha = new AlphanumericEncoder(charset);

        if (charset.name().toLowerCase().equals("shift_jis")) {
            _encKanji = new KanjiEncoder(charset);
        } else {
            _encKanji = null;
        }
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
    public void append(char c) {
        byte[] charBytes = String.valueOf(c).getBytes(_charset);

        for (byte value : charBytes) {
            _codeWords.add((int) value);
        }

        _bitCounter += getCodewordBitLength(c);
        _charCounter += charBytes.length;
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
    public boolean inSubset(char c) {
        return true;
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public boolean inExclusiveSubset(char c) {
        if (_encAlpha.inSubset(c)) {
            return false;
        }

        if (_encKanji != null) {
            if (_encKanji.inSubset(c)) {
                return false;
            }
        }

        return inSubset(c);
    }
}
