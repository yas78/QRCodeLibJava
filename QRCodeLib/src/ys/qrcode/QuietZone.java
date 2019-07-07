package ys.qrcode;

/**
 * クワイエットゾーン
 */
class QuietZone {
    static final int WIDTH = 4;

    /**
     * クワイエットゾーンを追加します。
     */
    public static int[][] place(int[][] moduleMatrix) {
        int size = moduleMatrix.length + WIDTH * 2;
        int[][] ret = new int[size][];

        for (int i = 0; i < size; i++) {
            ret[i] = new int[size];
        }

        for (int i = 0; i < moduleMatrix.length; i++) {
            System.arraycopy(moduleMatrix[i],
                             0,
                             ret[i + WIDTH],
                             WIDTH,
                             moduleMatrix[i].length);
        }

        return ret;
    }
}
