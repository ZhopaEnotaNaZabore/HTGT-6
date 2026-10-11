package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/** Randomly grows one outward-facing crystal stage and intentionally never drops itself. */
public final class BlockBackport120BuddingAmethyst extends Block {
    public BlockBackport120BuddingAmethyst(){super(Material.rock);setBlockName("backport_1_20.budding_amethyst");
        setBlockTextureName("htgt6:1_20_backport/budding_amethyst");setHardness(1.5F);
        setResistance(Backport120Content.legacyResistance(1.5F));setStepSound(soundTypeGlass);setHarvestLevel("pickaxe",0);
        setCreativeTab(CreativeTabs.tabBlock);setTickRandomly(true);}
    @Override public int getRenderType(){return Backport120Content.getJsonRenderId();}
    @Override public Item getItemDropped(int m,Random r,int f){return null;}
    @Override protected boolean canSilkHarvest(){return false;}
    @Override public void updateTick(World w,int x,int y,int z,Random r){if(w.isRemote||r.nextInt(5)!=0)return;
        ForgeDirection d=ForgeDirection.VALID_DIRECTIONS[r.nextInt(6)];int nx=x+d.offsetX,ny=y+d.offsetY,nz=z+d.offsetZ;
        Block b=w.getBlock(nx,ny,nz);int facing=d.ordinal();
        if(b.isAir(w,nx,ny,nz))w.setBlock(nx,ny,nz,Backport120Content.smallAmethystBud,facing,3);
        else if((w.getBlockMetadata(nx,ny,nz)&7)==facing){
            if(b==Backport120Content.smallAmethystBud)w.setBlock(nx,ny,nz,Backport120Content.mediumAmethystBud,facing,3);
            else if(b==Backport120Content.mediumAmethystBud)w.setBlock(nx,ny,nz,Backport120Content.largeAmethystBud,facing,3);
            else if(b==Backport120Content.largeAmethystBud)w.setBlock(nx,ny,nz,Backport120Content.amethystCluster,facing,3);}
    }
}
