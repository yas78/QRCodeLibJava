package ys.qrcode.misc;

public class ArrayUtil {
    /**
     * 配列のディープコピーを作成します。
     */
    public static int[][] deepCopy(int[][] arg) {
        int[][] ret = new int[arg.length][];

        for (int i = 0; i < arg.length; i++) {
            ret[i] = new int[arg[i].length];
            System.arraycopy(arg[i], 0, ret[i], 0, arg[i].length);
        }

        return ret;
    }

    /**
     * 配列を左に90度に回転します。
     */
    public static int[][] rotate90(int[][] arg) {
        int[][] ret = new int[arg[0].length][];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = new int[arg.length];
        }

        int k = ret.length - 1;

        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[i].length; j++) {
                ret[i][j] = arg[j][k - i];
            }
        }

        return ret;
    }
}
