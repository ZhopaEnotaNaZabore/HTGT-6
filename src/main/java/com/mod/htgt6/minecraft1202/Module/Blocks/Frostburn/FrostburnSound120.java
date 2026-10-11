package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import net.minecraft.block.Block;

/** Modern sound events through the legacy block sound API. */
public final class FrostburnSound120 extends Block.SoundType {
    private final String family;
    public FrostburnSound120(String family) {
        super(family, 1.0F, 1.0F);
        this.family = family;
    }
    @Override public String getBreakSound() { return "htgt6:block." + family + ".break"; }
    @Override public String getStepResourcePath() { return "htgt6:block." + family + ".step"; }
    @Override public String func_150496_b() { return "htgt6:block." + family + ".place"; }
}
