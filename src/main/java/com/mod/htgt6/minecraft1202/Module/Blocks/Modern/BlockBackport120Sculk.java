package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

/** Sculk substrate: silk-touch drops the block, ordinary mining releases experience. */
public class BlockBackport120Sculk extends Block {
    private final int experience;

    public BlockBackport120Sculk(String name, float hardness, int experience) {
        super(Material.rock);
        this.experience = experience;
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + ("sculk".equals(name) ? name : name + "_side"));
        setHardness(hardness);
        setResistance(Backport120Content.legacyResistance(hardness));
        setStepSound(soundTypeGrass);
        setHarvestLevel("hoe", 0);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override protected boolean canSilkHarvest() { return true; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return null; }
    @Override public int getExpDrop(IBlockAccess world, int metadata, int fortune) { return experience; }
}
