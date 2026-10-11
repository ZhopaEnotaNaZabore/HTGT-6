package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

/** Pre-1.17 grass path, called dirt path by modern versions. */
public final class BlockBackport120DirtPath extends Block {
    @SideOnly(Side.CLIENT) private IIcon top,side,bottom;
    public BlockBackport120DirtPath(){
        super(Material.ground);setBlockName("backport_1_20.dirt_path");setHardness(0.65F);
        setResistance(Backport120Content.legacyResistance(0.65F));setStepSound(soundTypeGrass);
        setHarvestLevel("shovel",0);setCreativeTab(CreativeTabs.tabBlock);
        setBlockBounds(0,0,0,1,0.9375F,1);setLightOpacity(255);
    }
    @Override public boolean isOpaqueCube(){return false;}
    @Override public boolean renderAsNormalBlock(){return false;}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister r){
        top=r.registerIcon("htgt6:1_20_backport/dirt_path_top");
        side=r.registerIcon("htgt6:1_20_backport/dirt_path_side");
        bottom=r.registerIcon("minecraft:dirt");blockIcon=top;
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int face,int meta){return face==1?top:face==0?bottom:side;}
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        if(world.getBlock(x,y+1,z).getMaterial().isSolid())world.setBlock(x,y,z,net.minecraft.init.Blocks.dirt,0,3);
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor){
        world.scheduleBlockUpdate(x,y,z,this,1);
    }
}
