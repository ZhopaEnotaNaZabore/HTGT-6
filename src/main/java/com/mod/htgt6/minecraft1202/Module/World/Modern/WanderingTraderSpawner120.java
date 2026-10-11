package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Data.Modern.WanderingTraderSpawnData120;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityTraderLlama120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWanderingTrader120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/** Native 1.7 world-tick implementation of the 1.20 daily wandering-trader spawner. */
public final class WanderingTraderSpawner120 {
    public static final int TICK_INTERVAL=1200,SPAWN_INTERVAL=24000,MIN_CHANCE=25,MAX_CHANCE=75;
    @SubscribeEvent public void onWorldTick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||event.world.isRemote||event.world.provider.dimensionId!=0)return;
        World world=event.world;if(!world.getGameRules().getGameRuleBooleanValue("doMobSpawning"))return;
        WanderingTraderSpawnData120 data=WanderingTraderSpawnData120.get(world);
        if(--data.tickDelay>0)return;data.tickDelay=TICK_INTERVAL;data.spawnDelay-=TICK_INTERVAL;
        if(data.spawnDelay>0){data.markDirty();return;}data.spawnDelay=SPAWN_INTERVAL;
        int chance=data.spawnChance;data.spawnChance=Math.min(MAX_CHANCE,data.spawnChance+25);data.markDirty();
        // Modern values are stored as 25/50/75 but evaluated behind a 1-in-10 gate: 2.5/5/7.5% per day.
        if(world.rand.nextInt(10)!=0||world.rand.nextInt(100)>=chance)return;
        if(trySpawn(world)){data.spawnChance=MIN_CHANCE;data.markDirty();}
    }

    @SuppressWarnings("unchecked") private static boolean trySpawn(World world){
        for(Object raw:world.loadedEntityList)if(raw instanceof EntityWanderingTrader120&&((Entity)raw).isEntityAlive())return false;
        List<EntityPlayer> players=world.playerEntities;if(players.isEmpty())return false;
        EntityPlayer player=players.get(world.rand.nextInt(players.size()));int[] point=findGround(world,
                (int)Math.floor(player.posX),(int)Math.floor(player.posZ),world.rand.nextInt(2)==0?1:-1,
                world.rand.nextInt(2)==0?1:-1);if(point==null)return false;
        EntityWanderingTrader120 trader=new EntityWanderingTrader120(world);trader.setLocationAndAngles(point[0]+.5D,point[1],point[2]+.5D,
                world.rand.nextFloat()*360F,0);trader.setWanderTarget((int)player.posX,(int)player.posY,(int)player.posZ);
        trader.setDespawnDelay(EntityWanderingTrader120.DEFAULT_DESPAWN_DELAY);if(!world.spawnEntityInWorld(trader))return false;
        spawnLlama(world,trader,point[0]+2,point[2]);spawnLlama(world,trader,point[0]-2,point[2]);return true;
    }
    private static int[] findGround(World world,int centerX,int centerZ,int signX,int signZ){
        for(int attempt=0;attempt<10;attempt++){int x=centerX+signX*(24+world.rand.nextInt(25))+world.rand.nextInt(17)-8;
            int z=centerZ+signZ*(24+world.rand.nextInt(25))+world.rand.nextInt(17)-8;int y=world.getTopSolidOrLiquidBlock(x,z);
            if(valid(world,x,y,z))return new int[]{x,y,z};}return null;
    }
    private static boolean valid(World world,int x,int y,int z){if(y<1||y+2>=world.getActualHeight())return false;
        Block ground=world.getBlock(x,y-1,z);if(ground.getMaterial().isLiquid()||!ground.isSideSolid(world,x,y-1,z,
                net.minecraftforge.common.util.ForgeDirection.UP))return false;
        if(!world.isAirBlock(x,y,z)||!world.isAirBlock(x,y+1,z))return false;
        return world.getCollidingBoundingBoxes(null,AxisAlignedBB.getBoundingBox(x+.2D,y,z+.2D,x+.8D,y+1.95D,z+.8D)).isEmpty();}
    private static void spawnLlama(World world,EntityWanderingTrader120 trader,int x,int z){int y=world.getTopSolidOrLiquidBlock(x,z);
        if(!valid(world,x,y,z))return;EntityTraderLlama120 llama=new EntityTraderLlama120(world);
        llama.setLocationAndAngles(x+.5D,y,z+.5D,world.rand.nextFloat()*360F,0);llama.onSpawnWithEgg(null);
        llama.setDespawnDelay(trader.getDespawnDelay());if(world.spawnEntityInWorld(llama))llama.setLeashedToEntity(trader,true);}
}
