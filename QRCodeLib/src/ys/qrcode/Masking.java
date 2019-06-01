package ys.qrcode;

import java.util.function.BiPredicate;

import ys.qrcode.misc.IntegerUtil;

class Masking {
    /**
     * マスクを適用します。
     *
     * @param moduleMatrix
     *            シンボルの明暗パターン
     * @param version
     *            型番
     * @param ecLevel
     *            誤り訂正レベル
     * @return
     *            適用されたマスクパターン参照子
     */
    public static int apply(int[][] moduleMatrix,
                            int version,
                            ErrorCorrectionLevel ecLevel) {
        int maskPatternReference = selectMaskPattern(moduleMatrix, version, ecLevel);
        mask(moduleMatrix, maskPatternReference);

        return maskPatternReference;
    }

    /**
     * マスクパターンを決定します。
     *
     * @param moduleMatrix
     *            シンボルの明暗パターン
     * @param version
     *            型番
     * @param ecLevel
     *            誤り訂正レベル
     * @return
     *            マスクパターン参照子
     */
    private static int selectMaskPattern(int[][] moduleMatrix,
                                         int version,
                                         ErrorCorrectionLevel ecLevel) {
        int minPenalty = Integer.MAX_VALUE;
        int ret = 0;

        for (int maskPatternReference = 0; maskPatternReference <= 7; maskPatternReference++) {
            int[][] moduleMatrixClone = IntegerUtil.cloneDeep(moduleMatrix);

            mask(moduleMatrixClone, maskPatternReference);

            FormatInfo.place(moduleMatrixClone, ecLevel, maskPatternReference);

            if (version >= 7) {
                VersionInfo.place(moduleMatrixClone, version);
            }

            int penalty = MaskingPenaltyScore.calcTotal(moduleMatrixClone);

            if (penalty < minPenalty) {
                minPenalty = penalty;
                ret = maskPatternReference;
            }
        }

        return ret;
    }

    /**
     * マスクパターンを適用したシンボルデータを返します。
     *
     * @param moduleMatrix
     *            シンボルの明暗パターン
     * @param maskPatternReference
     *            マスクパターン参照子
     */
    private static void mask(int[][] moduleMatrix, int maskPatternReference) {
        BiPredicate<Integer, Integer> condition = getCondition(maskPatternReference);

        for (int r = 0; r < moduleMatrix.length; r++) {
            for (int c = 0; c < moduleMatrix[r].length; c++) {
                if (Math.abs(moduleMatrix[r][c]) == 1) {
                    if (condition.test(new Integer(r), new Integer(c))) {
                        moduleMatrix[r][c] *= -1;
                    }
                }
            }
        }
    }

    /**
     * マスク条件を返します。
     *
     * @param maskPatternReference
     *            マスクパターン参照子
     */
    private static BiPredicate<Integer, Integer> getCondition(int maskPatternReference) {
        switch (maskPatternReference) {
        case 0:
            return (r, c) -> (r + c) % 2 == 0;
        case 1:
            return (r, c) -> r % 2 == 0;
        case 2:
            return (r, c) -> c % 3 == 0;
        case 3:
            return (r, c) -> (r + c) % 3 == 0;
        case 4:
            return (r, c) -> ((r / 2) + (c / 3)) % 2 == 0;
        case 5:
            return (r, c) -> (r * c) % 2 + (r * c) % 3 == 0;
        case 6:
            return (r, c) -> ((r * c) % 2 + (r * c) % 3) % 2 == 0;
        case 7:
            return (r, c) -> ((r + c) % 2 + (r * c) % 3) % 2 == 0;
        default:
            throw new IllegalArgumentException("maskPatternReference");
        }
    }
}
