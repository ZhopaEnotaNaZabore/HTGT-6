package com.mod.htgt6.common.crusher;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Explicit structural roles: ribs never change texture with their neighbours. */
public final class BlockIndustrialCrusherCasing extends Block {
    public BlockIndustrialCrusherCasing(boolean rib) {
        super(Material.iron);
        String name = rib ? "industrial_crusher_rib" : "industrial_crusher_casing";
        setBlockName(name);
        setBlockTextureName("htgt6:industrial_crusher_" + (rib ? "rib" : "casing"));
        setHardness(7F);
        setResistance(35F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }
}
