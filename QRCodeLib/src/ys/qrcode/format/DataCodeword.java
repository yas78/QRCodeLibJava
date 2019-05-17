package ys.qrcode.format;

import ys.qrcode.Constants;
import ys.qrcode.ErrorCorrectionLevel;

/**
 * データコード語
 */
public class DataCodeword {
    /**
     * データコード語数を返します。
     *
     * @param ecLevel
     *            誤り訂正レベル
     * @param version
     *            型番
     */
    public static int getTotalNumber(ErrorCorrectionLevel ecLevel, int version) {
        assert Constants.MIN_VERSION <= version && version <= Constants.MAX_VERSION;

        return _totalNumbers[ecLevel.toInt()][version];
    }

    // データコード語数
    private static final int[][] _totalNumbers = {
        /* Error Correction Level L */
        new int[]{
              -1,
              19,   34,   55,   80,  108,  136,  156,  194,  232,  274,
             324,  370,  428,  461,  523,  589,  647,  721,  795,  861,
             932, 1006, 1094, 1174, 1276, 1370, 1468, 1531, 1631, 1735,
            1843, 1955, 2071, 2191, 2306, 2434, 2566, 2702, 2812, 2956
        },
        /* Error Correction Level M */
        new int[]{
              -1,
              16,   28,   44,   64,   86,  108,  124,  154,  182,  216,
             254,  290,  334,  365,  415,  453,  507,  563,  627,  669,
             714,  782,  860,  914, 1000, 1062, 1128, 1193, 1267, 1373,
            1455, 1541, 1631, 1725, 1812, 1914, 1992, 2102, 2216, 2334
        },
        /* Error Correction Level Q */
        new int[]{
              -1,
              13,   22,   34,   48,   62,   76,   88,  110,  132,  154,
             180,  206,  244,  261,  295,  325,  367,  397,  445,  485,
             512,  568,  614,  664,  718,  754,  808,  871,  911,  985,
            1033, 1115, 1171, 1231, 1286, 1354, 1426, 1502, 1582, 1666
        },
        /* Error Correction Level H */
        new int[]{
              -1,
               9,   16,   26,   36,   46,   60,   66,   86,  100,  122,
             140,  158,  180,  197,  223,  253,  283,  313,  341,  385,
             406,  442,  464,  514,  538,  596,  628,  661,  701,  745,
             793,  845,  901,  961,  986, 1054, 1096, 1142, 1222, 1276
        }
    };
}
