package com.mod.htgt6.minecraft1202.Module.World.Combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/** Exact 1.12 End-spike positions, dimensions, cages and crystals. */
public final class EndSpikeGenerator {
    public static void populate(World world, int chunkX, int chunkZ) {
        int[][] spikes = getSpikes(world.getSeed());
        for (int[] spike : spikes) {
            int startChunkX = (spike[0] - spike[2]) >> 4;
            int startChunkZ = (spike[1] - spike[2]) >> 4;
            if (startChunkX == chunkX && startChunkZ == chunkZ) generate(world, spike, false);
        }
    }

    public static void regenerate(World world, int index) {
        int[][] spikes = getSpikes(world.getSeed());
        if (index >= 0 && index < spikes.length) generate(world, spikes[index], true);
    }

    private static int[][] getSpikes(long seed) {
        Random worldRandom = new Random(seed);
        long spikeSeed = worldRandom.nextLong() & 65535L;
        List<Integer> order = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) order.add(i);
        Collections.shuffle(order, new Random(spikeSeed));
        int[][] spikes = new int[10][5];
        for (int i = 0; i < 10; i++) {
            int value = order.get(i);
            spikes[i][0] = (int)(42.0D * Math.cos(2.0D * (-Math.PI + Math.PI / 10.0D * i)));
            spikes[i][1] = (int)(42.0D * Math.sin(2.0D * (-Math.PI + Math.PI / 10.0D * i)));
            spikes[i][2] = 2 + value / 3;
            spikes[i][3] = 76 + value * 3;
            spikes[i][4] = value == 1 || value == 2 ? 1 : 0;
        }
        return spikes;
    }

    private static void generate(World world, int[] spike, boolean replace) {
        int cx=spike[0],cz=spike[1],radius=spike[2],height=spike[3];
        if(replace){
            List old=world.getEntitiesWithinAABB(EntityEnderCrystal.class,AxisAlignedBB.getBoundingBox(cx-10,height-10,cz-10,cx+11,height+11,cz+11));
            for(Object entity:old)((EntityEnderCrystal)entity).setDead();
        }
        for(int x=cx-radius;x<=cx+radius;x++)for(int z=cz-radius;z<=cz+radius;z++)for(int y=0;y<=height+10;y++){
            int dx=x-cx,dz=z-cz;
            if(dx*dx+dz*dz<=radius*radius+1&&y<height)world.setBlock(x,y,z,Blocks.obsidian,0,2);
            else if(y>65)world.setBlockToAir(x,y,z);
        }
        if(spike[4]!=0)for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){
            if(Math.abs(x)==2||Math.abs(z)==2)for(int y=0;y<=2;y++)world.setBlock(cx+x,height+y,cz+z,Blocks.iron_bars,0,2);
            world.setBlock(cx+x,height+3,cz+z,Blocks.iron_bars,0,2);
        }
        EntityEnderCrystal crystal=new EntityEnderCrystal(world);
        crystal.setLocationAndAngles(cx+.5D,height+1,cz+.5D,world.rand.nextFloat()*360F,0);
        world.spawnEntityInWorld(crystal);
        world.setBlock(cx,height,cz,Blocks.bedrock,0,2);
    }
    private EndSpikeGenerator(){}
}
