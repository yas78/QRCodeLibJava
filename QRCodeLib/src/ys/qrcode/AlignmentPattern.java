package ys.qrcode;

class AlignmentPattern {
    // 位置合せパターンの中心座標
    private static final int[][] _centerPosArrays = {
        null,
        null,
        new int[]{6, 18},
        new int[]{6, 22},
        new int[]{6, 26},
        new int[]{6, 30},
        new int[]{6, 34},
        new int[]{6, 22, 38},
        new int[]{6, 24, 42},
        new int[]{6, 26, 46},
        new int[]{6, 28, 50},
        new int[]{6, 30, 54},
        new int[]{6, 32, 58},
        new int[]{6, 34, 62},
        new int[]{6, 26, 46, 66},
        new int[]{6, 26, 48, 70},
        new int[]{6 ,26, 50, 74},
        new int[]{6 ,30, 54, 78},
        new int[]{6, 30, 56, 82},
        new int[]{6, 30, 58, 86},
        new int[]{6, 34, 62, 90},
        new int[]{6, 28, 50, 72, 94},
        new int[]{6, 26, 50, 74, 98},
        new int[]{6, 30, 54, 78, 102},
        new int[]{6, 28, 54, 80, 106},
        new int[]{6, 32, 58, 84, 110},
        new int[]{6, 30, 58, 86, 114},
        new int[]{6, 34, 62, 90, 118},
        new int[]{6, 26, 50, 74, 98, 122},
        new int[]{6, 30, 54, 78, 102, 126},
        new int[]{6, 26, 52, 78, 104, 130},
        new int[]{6, 30, 56, 82, 108, 134},
        new int[]{6, 34, 60, 86, 112, 138},
        new int[]{6, 30, 58, 86, 114, 142},
        new int[]{6, 34, 62, 90, 118, 146},
        new int[]{6, 30, 54, 78, 102, 126, 150},
        new int[]{6, 24, 50, 76, 102, 128, 154},
        new int[]{6, 28, 54, 80, 106, 132, 158},
        new int[]{6, 32, 58, 84, 110, 136, 162},
        new int[]{6, 26, 54, 82, 110, 138, 166},
        new int[]{6, 30, 58, 86, 114, 142, 170}
    };

    /**
     * 位置合わせパターンを配置します。
     */
    public static void place(int[][] moduleMatrix, int version) {
        int[] centerPosArray = _centerPosArrays[version];

        int maxIndex = centerPosArray.length - 1;

        for (int i = 0; i <= maxIndex; i++) {
            int r = centerPosArray[i];

            for (int j = 0; j <= maxIndex; j++) {
                int c = centerPosArray[j];

                // 位置検出パターンと重なる場合
                if (i == 0        && j == 0        ||
                    i == 0        && j == maxIndex ||
                    i == maxIndex && j == 0) {

                    continue;
                }

                System.arraycopy(new int[] { 2,  2,  2,  2,  2 }, 0, moduleMatrix[r - 2], c - 2, 5);
                System.arraycopy(new int[] { 2, -2, -2, -2,  2 }, 0, moduleMatrix[r - 1], c - 2, 5);
                System.arraycopy(new int[] { 2, -2,  2, -2,  2 }, 0, moduleMatrix[r + 0], c - 2, 5);
                System.arraycopy(new int[] { 2, -2, -2, -2,  2 }, 0, moduleMatrix[r + 1], c - 2, 5);
                System.arraycopy(new int[] { 2,  2,  2,  2,  2 }, 0, moduleMatrix[r + 2], c - 2, 5);
            }
        }
    }
}
