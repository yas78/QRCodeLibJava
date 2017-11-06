package ys.qrcode;

/**
 * 符号化モード
 */
public enum EncodingMode {
    UNKNOWN(0),
    NUMERIC(1),
    ALPHA_NUMERIC(2),
    EIGHT_BIT_BYTE(3),
    KANJI(4);

    private int _value;

    private EncodingMode(int value) {
        _value = value;
    }

    public int toInt() {
        return _value;
    }

    public static EncodingMode getEnum(int value) {
        EncodingMode[] encodingModes = EncodingMode.values();

        for (EncodingMode encodingMode : encodingModes) {
            if (encodingMode.toInt() == value) {
                return encodingMode;
            }
        }

        return null;
    }
}
