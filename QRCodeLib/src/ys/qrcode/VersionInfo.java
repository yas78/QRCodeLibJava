package ys.qrcode;

/**
 * 型番情報
 */
class VersionInfo {
    // 型番情報
    private static int[] _versionInfoValues = {
        -1, -1, -1, -1, -1, -1, -1,
        0x007C94, 0x0085BC, 0x009A99, 0x00A4D3, 0x00BBF6, 0x00C762, 0x00D847, 0x00E60D,
        0x00F928, 0x010B78, 0x01145D, 0x012A17, 0x013532, 0x0149A6, 0x015683, 0x0168C9,
        0x0177EC, 0x018EC4, 0x0191E1, 0x01AFAB, 0x01B08E, 0x01CC1A, 0x01D33F, 0x01ED75,
        0x01F250, 0x0209D5, 0x0216F0, 0x0228BA, 0x02379F, 0x024B0B, 0x02542E, 0x026A64,
        0x027541, 0x028C69
    };

    /**
     * 型番情報を配置します。
     */
    public static void place(int[][] moduleMatrix, int version) {
        int numModulesPerSide = moduleMatrix.length;

        int versionInfoValue = _versionInfoValues[version];

        int p1 = 0;
        int p2 = numModulesPerSide - 11;

        for (int i = 0; i < 18; i++) {
            int v = (versionInfoValue & (1 << i)) > 0 ? 3 : -3;

            moduleMatrix[p1][p2] = v;
            moduleMatrix[p2][p1] = v;

            p2++;

            if (i % 3 == 2) {
                p1++;
                p2 = numModulesPerSide - 11;
            }
        }
    }

    /**
     * 型番情報の予約領域を配置します。
     */
    public static void placeTempBlank(int[][] moduleMatrix) {
        int numModulesPerSide = moduleMatrix.length;

        for (int i = 0; i <= 5; i++) {
            for (int j = numModulesPerSide - 11; j <= numModulesPerSide - 9; j++) {
                moduleMatrix[i][j] = -3;
                moduleMatrix[j][i] = -3;
            }
        }
    }
}
