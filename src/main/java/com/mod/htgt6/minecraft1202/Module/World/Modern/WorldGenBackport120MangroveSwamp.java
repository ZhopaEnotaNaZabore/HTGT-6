package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/** trees_mangrove placed feature: count 25 and 85% tall-mangrove selector. */
public final class WorldGenBackport120MangroveSwamp extends WorldGenerator {
    public static final int TREE_COUNT=25;
    public static final float TALL_CHANCE=.85F;
    private final int radius;
    public WorldGenBackport120MangroveSwamp(int radius){this.radius=Math.max(4,radius);}
    @Override public boolean generate(World world,Random random,int x,int y,int z){boolean placed=false;
        for(int i=0;i<TREE_COUNT;i++){int px=x-8+random.nextInt(16),pz=z-8+random.nextInt(16);
            if(world.getBiomeGenForCoords(px,pz).biomeID!=FuturaBiomeIds120.MANGROVE_SWAMP)continue;
            int py=floorY(world,px,pz);Block floor=world.getBlock(px,py-1,pz);
            if(floor==Blocks.dirt||floor==Blocks.grass||floor==Blocks.clay||floor==Backport120Content.mud)
                placed|=new WorldGenBackport120MangroveTree(random.nextFloat()<TALL_CHANCE).generate(world,random,px,py,pz);}
        return placed;}

    /** Returns the first block position above soil, preserving shallow water above it. */
    private static int floorY(World world,int x,int z){
        int y=world.getTopSolidOrLiquidBlock(x,z);
        while(y>1){Block below=world.getBlock(x,y-1,z);
            if(below==Blocks.water||below==Blocks.flowing_water)y--;else break;}
        return y;
    }
}
