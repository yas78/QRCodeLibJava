package ys.qrcode;

/**
 * 分離パターン
 */
class Separator {
    private static final int VAL = Values.SEPARATOR;

    /**
     * 分離パターンを配置します。
     */
    public static void place(int[][] moduleMatrix) {
        int offset = moduleMatrix.length - 8;

        for (int i = 0; i <= 7; i++) {
            moduleMatrix[i][7] = -VAL;
            moduleMatrix[7][i] = -VAL;

            moduleMatrix[offset + i][7] = -VAL;
            moduleMatrix[offset + 0][i] = -VAL;

            moduleMatrix[i][offset + 0] = -VAL;
            moduleMatrix[7][offset + i] = -VAL;
        }
    }
}
