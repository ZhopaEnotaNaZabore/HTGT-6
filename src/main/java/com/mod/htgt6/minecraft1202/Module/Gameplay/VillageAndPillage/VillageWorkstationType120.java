package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

/** The six 1.20.2 workstations which expose a menu (fletching deliberately has none). */
public enum VillageWorkstationType120 {
    SMITHING(30, "smithing_table", 3), GRINDSTONE(31, "grindstone", 2),
    STONECUTTER(32, "stonecutter", 1), LOOM(33, "loom", 3),
    CARTOGRAPHY(34, "cartography_table", 2), LECTERN(35, "lectern", 1);

    public final int guiId;
    public final String blockName;
    public final int inputSlots;
    private VillageWorkstationType120(int guiId, String blockName, int inputSlots) {
        this.guiId=guiId; this.blockName=blockName; this.inputSlots=inputSlots;
    }
    public static VillageWorkstationType120 byGuiId(int id) {
        for (VillageWorkstationType120 type:values()) if(type.guiId==id)return type;
        return null;
    }
    public static VillageWorkstationType120 byBlockName(String name) {
        for (VillageWorkstationType120 type:values()) if(type.blockName.equals(name))return type;
        return null;
    }
}
