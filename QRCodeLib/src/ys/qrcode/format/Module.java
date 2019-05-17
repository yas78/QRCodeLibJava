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
        assert Constants.MIN_VERSION <= version && version <= Constants.MAX_VERSION;

        return 17 + version * 4;
    }
}
