package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public final class BlockCombatSimple extends Block {
    public BlockCombatSimple(Material material, String name, float hardness, float resistance) {
        super(material);
        setBlockName(name);
        setBlockTextureName("htgt6:combat_update/" + name);
        setHardness(hardness);
        setResistance(Backport120Content.legacyResistance(resistance));
        setCreativeTab(CreativeTabs.tabBlock);
    }
}
