package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.block.Block;

/** Names resolve through the external sound pack; no audio is bundled. */
public final class BuildingSounds116 {
    public static final Block.SoundType BASALT = sound("basalt");
    public static final Block.SoundType STONE = sound("stone");
    public static final Block.SoundType NETHER_BRICKS = sound("nether_bricks");
    public static final Block.SoundType NETHER_WOOD = sound("nether_wood");
    private static Block.SoundType sound(final String family) {
        return new Block.SoundType(family, 1, 1) {
            @Override public String getBreakSound() { return "htgt6:block." + family + ".break"; }
            @Override public String getStepResourcePath() { return "htgt6:block." + family + ".step"; }
            @Override public String func_150496_b() { return "htgt6:block." + family + ".place"; }
        };
    }
    private BuildingSounds116() {}
}
