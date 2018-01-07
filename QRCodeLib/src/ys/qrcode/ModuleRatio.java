package ys.qrcode;

class ModuleRatio {
    public int PreLightRatio4   = 0;
    public int PreDarkRatio1    = 0;
    public int PreLightRatio1   = 0;
    public int CenterDarkRatio3 = 0;
    public int FolLightRatio1   = 0;
    public int FolDarkRatio1    = 0;
    public int FolLightRatio4   = 0;

    public boolean penaltyImposed() {
        if (PreDarkRatio1 == 0) {
            return false;
        }

        if (PreDarkRatio1     == PreLightRatio1 &&
            PreDarkRatio1     == FolLightRatio1 &&
            PreDarkRatio1     == FolDarkRatio1  &&
            PreDarkRatio1 * 3 == CenterDarkRatio3) {

            return PreLightRatio4 >= PreDarkRatio1 * 4 ||
                   FolLightRatio4 >= PreDarkRatio1 * 4;
        } else {
            return false;
        }
    }
}
