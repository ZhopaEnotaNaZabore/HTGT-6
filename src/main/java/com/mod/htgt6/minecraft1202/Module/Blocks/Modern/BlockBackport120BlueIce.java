package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** Dense 1.13+ blue ice used by the Ancient City ice box. */
public final class BlockBackport120BlueIce extends Block {
    public BlockBackport120BlueIce() {
        super(Material.packedIce);
        setBlockName("backport_1_20.blue_ice");
        setBlockTextureName("htgt6:1_20_backport/blue_ice");
        setHardness(2.8F);
        setResistance(Backport120Content.legacyResistance(2.8F));
        setStepSound(soundTypeGlass);
        setHarvestLevel("pickaxe", 0);
        slipperiness = 0.989F;
    }
}
