package com.mod.htgt6.common.block.greg.technological.hulls;

/** Client-independent palette and face rules shared by rendering and energy connections. */
public final class HullAppearance {
    private HullAppearance() { }
    public static int front(int meta) { return meta >= 2 && meta <= 5 ? meta : 2; }
    public static boolean accepts(int meta, int side) { return side >= 0 && side < 6 && side != front(meta); }
    public static boolean emits(int meta, int side) { return side == front(meta); }
    /** 0: smooth; 1: ribbed side; 2: reinforced top/bottom. */
    public static int texture(int tier, int meta, int side) {
        return tier < 3 || side == front(meta) ? 0 : side < 2 ? 2 : 1;
    }
    public static int color(int tier) {
        switch (tier) {
            case 0: return 0xC2C6C9;
            case 1: return 0xE7E4DC;
            case 2: return 0xCCDDE5;
            case 3: return 0xECE2C3;
            case 4: return 0xD4C9ED;
            case 5: return 0xC5CDE6;
            case 6: return 0xE9CADC;
            case 7: return 0xDFC1C0;
            case 8: return 0xC0E3DB;
            case 9: return 0xCFDDBB;
            case 16: return 0xEEE3BA;
            default: return 0xDDDDE0;
        }
    }
}
