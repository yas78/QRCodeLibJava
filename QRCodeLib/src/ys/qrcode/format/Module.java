package ys.qrcode.format;

import ys.qrcode.Constants;

/**
 * モジュール
 */
public class Module {
    /**
     * １辺のモジュール数を返します。
     *
     * @param version
     *            型番
     */
    public static int getNumModulesPerSide(int version) {
        assert version >= Constants.MIN_VERSION &&
               version <= Constants.MAX_VERSION;

        return 17 + version * 4;
    }
}
