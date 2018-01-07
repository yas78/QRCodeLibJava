package ys.qrcode;

import java.util.ArrayList;
import java.util.List;

import ys.qrcode.misc.IntegerUtil;

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
        penalty += calcAdjacentModulesInRowInSameColor(IntegerUtil.rotate90(moduleMatrix));

        return penalty;
    }

    /**
     * 行の同色隣接モジュールパターンの失点を計算します。
     */
    private static int calcAdjacentModulesInRowInSameColor(int[][] moduleMatrix) {
        int penalty = 0;

        for (int r = 0; r < moduleMatrix.length; r++) {
            int[] columns = moduleMatrix[r];
            int cnt = 1;

            for (int c = 0; c < columns.length - 1; c++) {
                if ((columns[c] > 0) == (columns[c + 1] > 0)) {
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
                boolean tmp = moduleMatrix[r][c] > 0;
                boolean sameColor = true;

                sameColor &= moduleMatrix[r + 0][c + 1] > 0 == tmp;
                sameColor &= moduleMatrix[r + 1][c + 0] > 0 == tmp;
                sameColor &= moduleMatrix[r + 1][c + 1] > 0 == tmp;

                if (sameColor) {
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
        penalty += calcModuleRatioInRow(IntegerUtil.rotate90(moduleMatrixTemp));

        return penalty;
    }

    /**
     * 行の1 : 1 : 3 : 1 : 1 比率パターンの失点を計算します。
     */
    private static int calcModuleRatioInRow(int[][] moduleMatrix) {
        int penalty = 0;

        for (int r = 0; r < moduleMatrix.length - 4; r++) {
            int[] columns = moduleMatrix[r];
            List<Integer> startIndexes = new ArrayList<Integer>();

            startIndexes.add(0);

            for (int c = 4; c < columns.length - 2; c++) {
                if (columns[c] > 0 && columns[c + 1] <= 0) {
                    startIndexes.add(c + 1);
                }
            }

            for (int i = 0; i < startIndexes.size(); i++) {
                int index = startIndexes.get(i);
                ModuleRatio moduleRatio = new ModuleRatio();

                while (index < columns.length && columns[index] <= 0) {
                    moduleRatio.PreLightRatio4++;
                    index++;
                }

                while (index < columns.length && columns[index] > 0) {
                    moduleRatio.PreDarkRatio1++;
                    index++;
                }

                while (index < columns.length && columns[index] <= 0) {
                    moduleRatio.PreLightRatio1++;
                    index++;
                }

                while (index < columns.length && columns[index] > 0) {
                    moduleRatio.CenterDarkRatio3++;
                    index++;
                }

                while (index < columns.length && columns[index] <= 0) {
                    moduleRatio.FolLightRatio1++;
                    index++;
                }

                while (index < columns.length && columns[index] > 0) {
                    moduleRatio.FolDarkRatio1++;
                    index++;
                }

                while (index < columns.length && columns[index] <= 0) {
                    moduleRatio.FolLightRatio4++;
                    index++;
                }

                if (moduleRatio.penaltyImposed()) {
                    penalty += 40;
                }
            }
        }

        return penalty;
    }

    /**
     * 全体に対する暗モジュールの占める割合について失点を計算します。
     */
    private static int calcProportionOfDarkModules(int[][] moduleMatrix) {

        int darkCount = 0;

        for (int r = 0; r < moduleMatrix.length; r++) {
            for (int c = 0; c < moduleMatrix[r].length; c++) {
                if (moduleMatrix[r][c] > 0) {
                    darkCount++;
                }
            }
        }

        int tmp;
        tmp = (int) Math.ceil((darkCount / Math.pow(moduleMatrix.length, 2) * 100));
        tmp = Math.abs(tmp - 50);
        tmp = (tmp + 4) / 5;

        return tmp * 10;
    }

}
