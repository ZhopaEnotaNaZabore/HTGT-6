package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Directional glazed terracotta using the four horizontal facing states from 1.20.2. */
public final class BlockBackport120GlazedTerracotta extends Block {
    private final String color;
    private IIcon icon;

    public BlockBackport120GlazedTerracotta(String color){
        super(Material.rock);this.color=color;
        setBlockName("backport_1_20."+color+"_glazed_terracotta");
        setHardness(1.4F);setResistance(Backport120Content.legacyResistance(4.2F));
        setStepSound(soundTypePiston);setHarvestLevel("pickaxe",0);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabBlock);
    }

    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        world.setBlockMetadataWithNotify(x,y,z,
                MathHelper.floor_double(placer.rotationYaw*4.0F/360.0F+0.5D)&3,2);
    }
    @Override public void registerBlockIcons(IIconRegister register){
        icon=register.registerIcon("htgt6:1_20_backport/"+color+"_glazed_terracotta");
    }
    @Override public IIcon getIcon(int side,int metadata){return icon;}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
}
