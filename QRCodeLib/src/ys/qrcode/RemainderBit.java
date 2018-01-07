package ys.qrcode;

/**
 * 残余ビット
 */
class RemainderBit {
    /**
     * 残余ビットを配置します。
     */
    public static void place(int[][] moduleMatrix) {
        for (int r = 0; r < moduleMatrix.length; r++) {
            for (int c = 0; c < moduleMatrix[r].length; c++) {
                if (moduleMatrix[r][c] == 0) {
                    moduleMatrix[r][c] = -1;
                }
            }
        }
    }
}
