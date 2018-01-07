package ys.qrcode.format;

/**
 * モード指示子
 */
public class ModeIndicator {
    public static final int LENGTH = 4;

    public static final int TERMINATOR_VALUE        = 0x0;
    public static final int NUMERIC_VALUE           = 0x1;
    public static final int ALPAHNUMERIC_VALUE      = 0x2;
    public static final int STRUCTURED_APPEND_VALUE = 0x3;
    public static final int BYTE_VALUE              = 0x4;
    public static final int KANJI_VALUE             = 0x8;
}
