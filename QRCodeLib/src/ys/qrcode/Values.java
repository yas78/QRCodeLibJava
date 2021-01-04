package ys.qrcode;

class Values
{
    public static final int BLANK      = 0;
    public static final int WORD       = 1;
    public static final int ALIGNMENT  = 2;
    public static final int FINDER     = 3;
    public static final int FORMAT     = 4;
    public static final int SEPARATOR  = 6;
    public static final int TIMING     = 7;
    public static final int VERSION    = 8;

    public static Boolean isDark(int value) {
        return value > BLANK;
    }
}
