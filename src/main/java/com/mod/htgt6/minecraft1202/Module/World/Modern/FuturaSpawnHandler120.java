package com.mod.htgt6.minecraft1202.Module.World.Modern;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.event.world.WorldEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

/** Deterministic, dry, two-block-clear spawn selection owned only by the Futura world type. */
public final class FuturaSpawnHandler120 {
    private static final Logger LOGGER=LogManager.getLogger("HTGT6-FuturaSpawn120");
    private static final int SEARCH_RADIUS=256;
    private static final int STEP=8;

    @SubscribeEvent
    public void createSpawn(WorldEvent.CreateSpawnPosition event){
        World world=event.world;
        if(!FuturaWorldType120.isFutura(world))return;
        int[] spawn=findSpawn(world);
        int x=spawn[0],y=spawn[1],z=spawn[2];
        world.getWorldInfo().setSpawnPosition(x,y,z);
        if(event.settings.isBonusChestEnabled())createBonusChest(world,x,y,z);
        event.setCanceled(true);
    }

    private static int[] findSpawn(World world){
        int y=safeY(world,0,0);
        if(y>0)return new int[]{0,y,0};
        for(int radius=STEP;radius<=SEARCH_RADIUS;radius+=STEP){
            for(int offset=-radius;offset<=radius;offset+=STEP){
                int[][] candidates={{offset,-radius},{offset,radius},{-radius,offset},{radius,offset}};
                for(int[] candidate:candidates){
                    // Biome lookup is the cheap source-of-truth pre-filter. The old
                    // estimated height came from a different density path and could
                    // reject every real land column before its chunk was inspected.
                    if(!isSpawnBiome(world.getBiomeGenForCoords(candidate[0],candidate[1])))continue;
                    y=safeY(world,candidate[0],candidate[1]);
                    if(y>0)return new int[]{candidate[0],y,candidate[1]};
                }
            }
        }
        // Vanilla world creation never aborts solely because its preferred-biome search
        // failed. Preserve that contract for unusually large oceans: spawn above the
        // generated origin column and let the player descend into the water safely.
        world.getChunkFromBlockCoords(0,0);
        int fallbackY=Math.max(2,Math.min(ChunkProviderFutura120.PHYSICAL_HEIGHT-2,
                world.getTopSolidOrLiquidBlock(0,0)+1));
        LOGGER.warn("Futura found no dry spawn column within {}; using generated origin at Y {}",
                SEARCH_RADIUS,fallbackY);
        return new int[]{0,fallbackY,0};
    }

    private static boolean isSpawnBiome(BiomeGenBase biome){
        return biome!=null&&!BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.OCEAN)
                &&!BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.RIVER)
                &&!BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.WATER);
    }

    private static boolean isSafe(World world,int x,int y,int z){
        Block floor=world.getBlock(x,y-1,z);
        return floor!=null&&floor!=Blocks.water&&floor!=Blocks.flowing_water
                &&floor!=Blocks.lava&&floor!=Blocks.flowing_lava&&floor.getMaterial().blocksMovement()
                &&world.isAirBlock(x,y,z)&&world.isAirBlock(x,y+1,z);
    }

    private static int safeY(World world,int x,int z){
        // Force the exact Futura chunk to materialize, then inspect its blocks. This must
        // not be replaced by estimateSurfaceHeight: density detail and biome-edge blending
        // intentionally make the final surface differ by several blocks.
        world.getChunkFromBlockCoords(x,z);
        for(int y=ChunkProviderFutura120.PHYSICAL_HEIGHT-2;y>ChunkProviderFutura120.SEA_LEVEL;y--)
            if(isSafe(world,x,y,z))return y;
        return -1;
    }

    private static void createBonusChest(World world,int x,int y,int z){
        Random random=new Random(world.getSeed()^0x465554555241L);
        WorldGeneratorBonusChest generator=new WorldGeneratorBonusChest(
                ChestGenHooks.getItems(ChestGenHooks.BONUS_CHEST,random),
                ChestGenHooks.getCount(ChestGenHooks.BONUS_CHEST,random));
        for(int i=0;i<10;i++){
            int chestX=x+random.nextInt(6)-random.nextInt(6);
            int chestZ=z+random.nextInt(6)-random.nextInt(6);
            int chestY=world.getTopSolidOrLiquidBlock(chestX,chestZ);
            if(generator.generate(world,random,chestX,chestY,chestZ))return;
        }
    }
}
