package com.mod.htgt6.common.centrifuge;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Explicit structural roles: ribs never change texture with their neighbours. */
public final class BlockIndustrialCentrifugeCasing extends Block {
    public BlockIndustrialCentrifugeCasing(boolean rib) {
        super(Material.iron);
        String name = rib ? "industrial_centrifuge_rib" : "industrial_centrifuge_casing";
        setBlockName(name);
        setBlockTextureName("htgt6:multiblock_v2_centrifuge_" + (rib ? "rib" : "casing"));
        setHardness(7F);
        setResistance(35F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }
}
