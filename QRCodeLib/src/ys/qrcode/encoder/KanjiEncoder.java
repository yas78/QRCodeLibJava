package ys.qrcode.encoder;

import java.nio.charset.Charset;

import ys.qrcode.EncodingMode;
import ys.qrcode.format.ModeIndicator;
import ys.qrcode.util.BitSequence;

/**
 * 漢字モードエンコーダー
 */
public class KanjiEncoder extends QRCodeEncoder {
    private static final Charset _charSet = Charset.forName("Shift_JIS");

    /**
     * インスタンスを初期化します。
     */
    public KanjiEncoder() { }

    /**
     * 符号化モードを取得します。
     */
    @Override
    public EncodingMode getEncodingMode() {
        return EncodingMode.KANJI;
    }

    /**
     * モード指示子を取得します。
     */
    @Override
    public int getModeIndicator() {
        return ModeIndicator.KANJI_VALUE;
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

        int wd = (Byte.toUnsignedInt(charBytes[0]) << 8) | Byte.toUnsignedInt(charBytes[1]);

        if (wd >= 0x8140 && wd <= 0x9FFC) {
            wd -= 0x8140;
        } else if (wd >= 0xE040 && wd <= 0xEBBF) {
            wd -= 0xC140;
        } else {
            throw new IllegalArgumentException("c");
        }

        wd = ((wd >> 8) * 0xC0) + (wd & 0xFF);

        _codeWords.add(wd);
        _charCounter++;
        _bitCounter += 13;

        return 13;
    }

    /**
     * 指定の文字をエンコードしたコード語のビット数を返します。
     */
    @Override
    public int getCodewordBitLength(char c) {
        assert isInSubset(c);

        return 13;
    }

    /**
     * エンコードされたデータのバイト配列を返します。
     */
    @Override
    public byte[] getBytes() {
        BitSequence bs = new BitSequence();

        for (int i = 0; i < _codeWords.size(); i++) {
            bs.append(_codeWords.get(i), 13);
        }

        return bs.getBytes();
    }

    /**
     * 指定した文字が、このモードの文字集合に含まれる場合は true を返します。
     */
    public static boolean isInSubset(char c) {

        byte[] charBytes = String.valueOf(c).getBytes(_charSet);

        if (charBytes.length != 2) {
            return false;
        }

        int code = (Byte.toUnsignedInt(charBytes[0]) << 8) | Byte.toUnsignedInt(charBytes[1]);

        if (code >= 0x8140 && code <= 0x9FFC || code >= 0xE040 && code <= 0xEBBF) {
            return Byte.toUnsignedInt(charBytes[1]) >= 0x40 &&
                   Byte.toUnsignedInt(charBytes[1]) <= 0xFC &&
                   Byte.toUnsignedInt(charBytes[1]) != 0x7F;

        } else {
            return false;
        }
    }

    /**
     * 指定した文字が、このモードの排他的部分文字集合に含まれる場合は true を返します。
     */
    public static boolean isInExclusiveSubset(char c) {
        return isInSubset(c);
    }
}
