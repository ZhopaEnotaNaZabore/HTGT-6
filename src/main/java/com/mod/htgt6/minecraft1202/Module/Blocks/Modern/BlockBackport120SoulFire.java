package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;

import java.util.Random;

/** Non-spreading Minecraft 1.20 soul fire implemented directly on the 1.7 fire API. */
public final class BlockBackport120SoulFire extends BlockFire {
    private final IIcon[] icons=new IIcon[2];
    public BlockBackport120SoulFire(){setBlockName("htgt6.backport120.soul_fire");setLightLevel(10F/15F);setTickRandomly(true);}
    @Override public boolean canPlaceBlockAt(World world,int x,int y,int z){
        Block support=world.getBlock(x,y-1,z);
        return support==Blocks.soul_sand||support==EtFVanillaGapContent120.soulSoil;
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block neighbor){
        if(!canPlaceBlockAt(world,x,y,z))world.setBlockToAir(x,y,z);
    }
    @Override public void updateTick(World world,int x,int y,int z,Random random){
        if(!canPlaceBlockAt(world,x,y,z))world.setBlockToAir(x,y,z);
    }
    @Override public void onEntityCollidedWithBlock(World world,int x,int y,int z,Entity entity){
        if(!entity.isImmuneToFire()){entity.attackEntityFrom(DamageSource.inFire,2F);entity.setFire(8);}
    }
    @Override public int quantityDropped(Random random){return 0;}
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register){
        icons[0]=register.registerIcon("htgt6:1_20_backport/soul_fire_0");
        icons[1]=register.registerIcon("htgt6:1_20_backport/soul_fire_1");blockIcon=icons[0];
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getFireIcon(int index){return icons[index&1];}
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int side,int metadata){return icons[0];}
}
