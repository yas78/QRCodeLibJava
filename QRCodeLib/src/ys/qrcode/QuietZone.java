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
        int[][] ret = new int[moduleMatrix.length + WIDTH * 2][];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = new int[ret.length];
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
