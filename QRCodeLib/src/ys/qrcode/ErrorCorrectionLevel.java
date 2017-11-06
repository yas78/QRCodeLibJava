package ys.qrcode;

/**
 * 誤り訂正レベル
 */
public enum ErrorCorrectionLevel {
    L(0),
    M(1),
    Q(2),
    H(3);

    private int _value;

    private ErrorCorrectionLevel(int value) {
        _value = value;
    }

    public int toInt() {
        return _value;
    }
}
