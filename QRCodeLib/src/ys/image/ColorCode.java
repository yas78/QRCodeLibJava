package ys.image;

public class ColorCode {
    public static final String BLACK = "#000000";
    public static final String WHITE = "#FFFFFF";

    public static Boolean isWebColor(String arg) {
        Boolean ret = arg.matches("^#[0-9A-Fa-f]{6}$");
        return ret;
    }
}
