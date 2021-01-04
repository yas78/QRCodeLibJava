package ys.qrcode;

import java.util.ArrayList;
import java.util.List;

import ys.qrcode.misc.ArrayUtil;

/**
 * マスクされたシンボルの失点評価
 */
class MaskingPenaltyScore {
    /**
     * マスクパターン失点の合計を返します。
     */
    public static int calcTotal(int[][] moduleMatrix) {
        int total = 0;
        int penalty;

        penalty = calcAdjacentModulesInSameColor(moduleMatrix);
        total += penalty;

        penalty = calcBlockOfModulesInSameColor(moduleMatrix);
        total += penalty;

        penalty = calcModuleRatio(moduleMatrix);
        total += penalty;

        penalty = calcProportionOfDarkModules(moduleMatrix);
        total += penalty;

        return total;
    }

    /**
     * 行／列の同色隣接モジュールパターンの失点を計算します。
     */
    private static int calcAdjacentModulesInSameColor(int[][] moduleMatrix) {
        int penalty = 0;

        penalty += calcAdjacentModulesInRowInSameColor(moduleMatrix);
        penalty += calcAdjacentModulesInRowInSameColor(ArrayUtil.rotate90(moduleMatrix));

        return penalty;
    }

    /**
     * 行の同色隣接モジュールパターンの失点を計算します。
     */
    private static int calcAdjacentModulesInRowInSameColor(int[][] moduleMatrix) {
        int penalty = 0;

        for (int[] row : moduleMatrix) {
            int cnt = 1;

            for (int i = 0; i < row.length - 1; i++) {
                if (Values.isDark(row[i]) == Values.isDark(row[i + 1])) {
                    cnt++;
                } else {
                    if (cnt >= 5) {
                        penalty += 3 + (cnt - 5);
                    }

                    cnt = 1;
                }
            }

            if (cnt >= 5) {
                penalty += 3 + (cnt - 5);
            }
        }

        return penalty;
    }

    /**
     * 2x2の同色モジュールパターンの失点を計算します。
     */
    private static int calcBlockOfModulesInSameColor(int[][] moduleMatrix) {
        int penalty = 0;

        for (int r = 0; r < moduleMatrix.length - 1; r++) {
            for (int c = 0; c < moduleMatrix[r].length - 1; c++) {
                boolean temp = Values.isDark(moduleMatrix[r][c]);

                if ((Values.isDark(moduleMatrix[r + 0][c + 1]) == temp) &&
                    (Values.isDark(moduleMatrix[r + 1][c + 0]) == temp) &&
                    (Values.isDark(moduleMatrix[r + 1][c + 1]) == temp)) {
                        penalty += 3;
                }
            }
        }

        return penalty;
    }

    /**
     * 行／列における1 : 1 : 3 : 1 : 1 比率パターンの失点を計算します。
     */
    private static int calcModuleRatio(int[][] moduleMatrix) {

        int[][] moduleMatrixTemp = QuietZone.place(moduleMatrix);

        int penalty = 0;

        penalty += calcModuleRatioInRow(moduleMatrixTemp);
        penalty += calcModuleRatioInRow(ArrayUtil.rotate90(moduleMatrixTemp));

        return penalty;
    }

    /**
     * 行の1 : 1 : 3 : 1 : 1 比率パターンの失点を計算します。
     */
    private static int calcModuleRatioInRow(int[][] moduleMatrix) {
        int penalty = 0;

        for (int[] row : moduleMatrix) {
            int[][] ratio3Ranges = getRatio3Ranges(row);

            for (int[] rng : ratio3Ranges) {
                int ratio3 = rng[1] + 1 - rng[0];
                int ratio1 = ratio3 / 3;
                int ratio4 = ratio1 * 4;
                boolean impose = false;
                int cnt;

                int i = rng[0] - 1;

                // light ratio 1
                for (cnt = 0; i >= 0 && !Values.isDark(row[i]); cnt++, i--);

                if (cnt != ratio1) {
                    continue;
                }

                // dark ratio 1
                for (cnt = 0; i >= 0 && Values.isDark(row[i]); cnt++, i--);

                if (cnt != ratio1) {
                    continue;
                }

                // light ratio 4
                for (cnt = 0; i >= 0 && !Values.isDark(row[i]); cnt++, i--);

                if (cnt >= ratio4) {
                    impose = true;
                }

                i = rng[1] + 1;

                // light ratio 1
                for (cnt = 0; i <= row.length - 1 && !Values.isDark(row[i]); cnt++, i++);

                if (cnt != ratio1) {
                    continue;
                }

                // dark ratio 1
                for (cnt = 0; i <= row.length - 1 && Values.isDark(row[i]); cnt++, i++);

                if (cnt != ratio1) {
                    continue;
                }

                // light ratio 4
                for (cnt = 0; i <= row.length - 1 && !Values.isDark(row[i]); cnt++, i++);

                if (cnt >= ratio4) {
                    impose = true;
                }

                if (impose) {
                    penalty += 40;
                }
            }
        }

        return penalty;
    }


    private static int[][] getRatio3Ranges(int[] arg) {
        List<int[]> ret = new ArrayList<int[]>();
        int s = 0;

        for (int i = 1; i < arg.length - 1; i++) {
            if (Values.isDark(arg[i])) {
                if (!Values.isDark(arg[i - 1])) {
                    s = i;
                }

                if (!Values.isDark(arg[i + 1])) {
                    if ((i + 1 - s) % 3 == 0) {
                        ret.add(new int[] { s, i });
                    }
                }
            }
        }

        return ret.toArray(new int[ret.size()][2]);
    }
    /**
     * 全体に対する暗モジュールの占める割合について失点を計算します。
     */
    private static int calcProportionOfDarkModules(int[][] moduleMatrix) {
        int darkCount = 0;

        for (int[] row : moduleMatrix) {
            for (int value : row) {
                if (Values.isDark(value)) {
                    darkCount++;
                }
            }
        }

        double numModules = Math.pow(moduleMatrix.length, 2);
        double k;
        k = darkCount / numModules * 100;
        k = Math.abs(k - 50);
        k = Math.floor(k / 5);
        int penalty = (int)k * 10;

        return penalty;
    }

}
