package ys.qrcode;

/**
 * クワイエットゾーン
 */
class QuietZone {
    private static final int QUIET_ZONE_WIDTH = 4;

    /**
     * クワイエットゾーンを追加します。
     */
    public static int[][] place(int[][] moduleMatrix) {
        int[][] ret = new int[moduleMatrix.length + QUIET_ZONE_WIDTH * 2][];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = new int[ret.length];
        }

        for (int i = 0; i < moduleMatrix.length; i++) {
            System.arraycopy(moduleMatrix[i],
                             0,
                             ret[i + QUIET_ZONE_WIDTH],
                             QUIET_ZONE_WIDTH,
                             moduleMatrix[i].length);
        }

        return ret;
    }
}
