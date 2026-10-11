package com.mod.htgt6.common.multiblock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Explicit solid rib; never replaces the existing open lattice frame. */
public final class BlockMultiblockTrim extends Block {
    public BlockMultiblockTrim(String name, String texture) {
        super(Material.iron);
        setBlockName(name);
        setBlockTextureName("htgt6:" + texture);
        setHardness(7F);
        setResistance(35F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }
}
