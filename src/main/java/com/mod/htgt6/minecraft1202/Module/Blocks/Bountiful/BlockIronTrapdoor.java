package com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** Redstone-operated iron trapdoor introduced in Java 1.8. */
public final class BlockIronTrapdoor extends BlockTrapDoor {
    public BlockIronTrapdoor() {
        super(Material.iron);
        setBlockName("iron_trapdoor"); setBlockTextureName("htgt6:bountiful/iron_trapdoor");
        setHardness(5.0F); setResistance(Backport120Content.legacyResistance(5.0F)); setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }
}
