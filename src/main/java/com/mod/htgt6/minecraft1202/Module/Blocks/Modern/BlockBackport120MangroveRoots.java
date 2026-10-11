package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Porous mangrove roots; the JSON model supplies the holes rather than a cube substitute. */
public final class BlockBackport120MangroveRoots extends Block {
    public BlockBackport120MangroveRoots(boolean muddy) {
        super(muddy ? Material.ground : Material.wood);
        String name = muddy ? "muddy_mangrove_roots" : "mangrove_roots";
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + name + "_side");
        setHardness(0.7F);
        setResistance(Backport120Content.legacyResistance(0.7F));
        setStepSound(muddy ? soundTypeGravel : soundTypeWood);
        setHarvestLevel(muddy ? "shovel" : "axe", 0);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
}
