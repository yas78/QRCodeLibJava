package ys.qrcode.format;

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
        return 17 + version * 4;
    }
}
