package ys.qrcode;

/**
 * 分離パターン
 */
class Separator {
    /**
     * 分離パターンを配置します。
     */
    public static void place(int[][] moduleMatrix) {
        int offset = moduleMatrix.length - 8;

        for (int i = 0; i <= 7; i++) {
            moduleMatrix[i][7] = -2;
            moduleMatrix[7][i] = -2;

            moduleMatrix[offset + i][7] = -2;
            moduleMatrix[offset + 0][i] = -2;

            moduleMatrix[i][offset + 0] = -2;
            moduleMatrix[7][offset + i] = -2;
        }
    }
}
