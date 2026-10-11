package com.mod.htgt6.common.block.hitech6;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;

/** 64x64 structural casing for the Super-Massive Compressor shell. */
public final class BlockSuperMassiveCompressorCasing extends Block {
    public BlockSuperMassiveCompressorCasing() {
        super(Material.iron);
        setBlockName("super_massive_compressor_casing");
        setHardness(8.0F);
        setResistance(40.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(CreativeTabs.tabRedstone);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:multiblock_v2_compressor_casing");
    }
}
