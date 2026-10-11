package com.mod.htgt6.minecraft1202.Module.Blocks.Combat;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;

public final class BlockPurpurPillar extends Block {
    private IIcon side;
    private IIcon top;

    public BlockPurpurPillar() {
        super(Material.rock);
        setBlockName("purpur_pillar");
        setHardness(1.5F);
        setResistance(10.0F);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override public void registerBlockIcons(IIconRegister register) {
        side = register.registerIcon("htgt6:combat_update/purpur_pillar");
        top = register.registerIcon("htgt6:combat_update/purpur_pillar_top");
        blockIcon = side;
    }

    @Override public IIcon getIcon(int sideNumber, int metadata) { return sideNumber < 2 ? top : side; }
}
