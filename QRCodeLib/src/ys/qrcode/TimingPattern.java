package ys.qrcode;

/**
 * タイミングパターン
 */
class TimingPattern {
    /**
     * タイミングパターンを配置します。
     */
    public static void place(int[][] moduleMatrix) {
        for (int i = 8; i <= moduleMatrix.length - 9; i++) {
            int v = ((i % 2 == 0) ? 2 : -2);

            moduleMatrix[6][i] = v;
            moduleMatrix[i][6] = v;
        }
    }
}
