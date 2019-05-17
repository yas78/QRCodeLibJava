package ys.qrcode.format;

import ys.qrcode.Constants;

/**
 * コード語
 */
public class Codeword {
    /**
     * コード語総数を返します。
     *
     * @param version
     *            型番
     */
    public static int getTotalNumber(int version) {
        assert Constants.MIN_VERSION <= version && version <= Constants.MAX_VERSION;

        return _totalNumbers[version];
    }

    // コード語総数
    private static final int[] _totalNumbers = {
          -1,
          26,   44,   70,  100,  134,  172,  196,  242,  292,  346,
         404,  466,  532,  581,  655,  733,  815,  901,  991, 1085,
        1156, 1258, 1364, 1474, 1588, 1706, 1828, 1921, 2051, 2185,
        2323, 2465, 2611, 2761, 2876, 3034, 3196, 3362, 3532, 3706
    };
}
