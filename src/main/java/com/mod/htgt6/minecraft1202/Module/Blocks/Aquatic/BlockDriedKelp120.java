package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class BlockDriedKelp120 extends Block {
    private IIcon top,bottom;
    public BlockDriedKelp120(){super(Material.grass);setBlockName("dried_kelp_block");setBlockTextureName("htgt6:dried_kelp_side");setHardness(.5F);setStepSound(soundTypeGrass);setCreativeTab(CreativeTabs.tabBlock);}
    @Override @SideOnly(Side.CLIENT) public void registerBlockIcons(IIconRegister register){super.registerBlockIcons(register);top=register.registerIcon("htgt6:dried_kelp_top");bottom=register.registerIcon("htgt6:dried_kelp_bottom");}
    @Override @SideOnly(Side.CLIENT) public IIcon getIcon(int side,int meta){return side==1?top:side==0?bottom:blockIcon;}
}
