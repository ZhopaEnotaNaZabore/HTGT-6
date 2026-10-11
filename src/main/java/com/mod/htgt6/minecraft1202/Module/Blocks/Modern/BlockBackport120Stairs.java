package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.creativetab.CreativeTabs;

/** Forge 1.7.10 stair physics with the shared modern JSON render type. */
public final class BlockBackport120Stairs extends BlockStairs {
    public BlockBackport120Stairs(Block source, String name, String texture) {
        this(source, name, texture, "pickaxe", 0);
    }

    public BlockBackport120Stairs(Block source, String name, String texture,
                           String harvestTool, int harvestLevel) {
        super(source, 0);
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + texture);
        setCreativeTab(CreativeTabs.tabBlock);
        if (harvestTool != null) setHarvestLevel(harvestTool, harvestLevel);
        useNeighborBrightness = true;
    }

    @Override public int getRenderType() {
        return Backport120Content.getJsonRenderId();
    }
}
