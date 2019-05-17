package ys.qrcode.format;

import ys.qrcode.EncodingMode;

/**
 * 文字数指示子
 */
public class CharCountIndicator {
    /**
     * 文字数指示子のビット数を返します。
     *
     * @param version
     *            型番
     * @param encMode
     *            符号化モード
     */
    public static int getLength(int version, EncodingMode encMode) {
        if (1 <= version && version <= 9) {
            switch (encMode) {
            case NUMERIC:
                return 10;
            case ALPHA_NUMERIC:
                return 9;
            case EIGHT_BIT_BYTE:
                return 8;
            case KANJI:
                return 8;
            default:
                throw new IllegalArgumentException("encMode");
            }
        }

        if (10 <= version && version <= 26) {
            switch (encMode) {
            case NUMERIC:
                return 12;
            case ALPHA_NUMERIC:
                return 11;
            case EIGHT_BIT_BYTE:
                return 16;
            case KANJI:
                return 10;
            default:
                throw new IllegalArgumentException("encMode");
            }
        }

        if (27 <= version && version <= 40) {
            switch (encMode) {
            case NUMERIC:
                return 14;
            case ALPHA_NUMERIC:
                return 13;
            case EIGHT_BIT_BYTE:
                return 16;
            case KANJI:
                return 12;
            default:
                throw new IllegalArgumentException("encMode");
            }
        }

        throw new IllegalArgumentException("version");
    }
}
