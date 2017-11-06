package ys.qrcode.format;

/**
 * 構造的連接
 */
public class StructuredAppend {
    // パリティデータのビット数
    public static final int PARITY_DATA_LENGTH = 8;

    // 構造的連接ヘッダーのビット数
    public static final int HEADER_LENGTH = ModeIndicator.LENGTH +
                                            SymbolSequenceIndicator.POSITION_LENGTH +
                                            SymbolSequenceIndicator.TOTAL_NUMBER_LENGTH +
                                            PARITY_DATA_LENGTH;
}
