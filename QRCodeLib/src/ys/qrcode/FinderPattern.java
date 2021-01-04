package ys.qrcode;

/**
 * 位置検出パターン
 */
class FinderPattern {
    private static final int VAL = Values.FINDER;

    // 位置検出パターン
    private static int[][] _finderPattern = {
        new int[] {VAL,  VAL,  VAL,  VAL,  VAL,  VAL,  VAL},
        new int[] {VAL, -VAL, -VAL, -VAL, -VAL, -VAL,  VAL},
        new int[] {VAL, -VAL,  VAL,  VAL,  VAL, -VAL,  VAL},
        new int[] {VAL, -VAL,  VAL,  VAL,  VAL, -VAL,  VAL},
        new int[] {VAL, -VAL,  VAL,  VAL,  VAL, -VAL,  VAL},
        new int[] {VAL, -VAL, -VAL, -VAL, -VAL, -VAL,  VAL},
        new int[] {VAL,  VAL,  VAL,  VAL,  VAL,  VAL,  VAL}
    };

    /**
     * 位置検出パターンを配置します。
     */
    public static void place(int[][] moduleMatrix) {
        int offset = moduleMatrix.length - _finderPattern.length;

        for (int i = 0; i < _finderPattern.length; i++) {
            for (int j = 0; j < _finderPattern[i].length; j++) {
                int v = _finderPattern[i][j];

                moduleMatrix[i][j] = v;
                moduleMatrix[i][j + offset] = v;
                moduleMatrix[i + offset][j] = v;
            }
        }
    }
}
