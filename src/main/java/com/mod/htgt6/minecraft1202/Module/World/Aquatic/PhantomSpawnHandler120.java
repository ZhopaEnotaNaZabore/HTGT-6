package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityPhantom120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/** 1.20.2 spawn gates plus a local population ceiling to prevent accumulating flocks. */
public final class PhantomSpawnHandler120 {
    public static final String REST_KEY="HTGT6TimeSinceRest";
    private final Map<World,Integer> delays=new WeakHashMap<World,Integer>();

    @SubscribeEvent public void playerTick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END||event.player.worldObj.isRemote)return;
        EntityPlayer player=event.player;
        int rest=player.getEntityData().getInteger(REST_KEY);
        player.getEntityData().setInteger(REST_KEY,nextRest(rest,player.isPlayerSleeping()||!player.isEntityAlive()));
    }
    public static int nextRest(int rest,boolean reset) {
        return reset?0:rest<0?1:rest==Integer.MAX_VALUE?rest:rest+1;
    }
    public static int nextDelay(Random random) { return (60+random.nextInt(60))*20; }
    public static boolean insomniaRoll(Random random,int rest) {
        return random.nextInt(Math.max(1,rest))>=72000;
    }
    public static int groupSize(Random random,int difficulty) { return 1+random.nextInt(difficulty+1); }
    /** DifficultyInstance.calculateDifficulty from 1.20.2, not the older 1.7 regional factor. */
    public static float localDifficulty(int difficulty,long dayTime,long inhabited,float moon) {
        if(difficulty==0)return 0;
        float age=MathHelper.clamp_float((dayTime-72000L)/1440000F,0,1)*.25F;
        float regional=MathHelper.clamp_float(inhabited/3600000F,0,1)*(difficulty==3?1:.75F);
        regional+=MathHelper.clamp_float(moon*.25F,0,age);
        if(difficulty==1)regional*=.5F;
        return difficulty*(.75F+age+regional);
    }
    @SubscribeEvent public void worldTick(TickEvent.WorldTickEvent event) {
        World world=event.world;
        if(event.phase!=TickEvent.Phase.END||world.isRemote||world.provider.dimensionId!=0
                ||world.difficultySetting==EnumDifficulty.PEACEFUL
                ||!world.getGameRules().getGameRuleBooleanValue("doMobSpawning")
                ||world.getGameRules().hasRule("doInsomnia")&&!world.getGameRules().getGameRuleBooleanValue("doInsomnia"))return;
        Integer previous=delays.get(world);
        int remaining=previous==null?0:previous-1;
        if(remaining>0) { delays.put(world,remaining); return; }
        delays.put(world,nextDelay(world.rand));
        if(world.skylightSubtracted<5)return;
        int seaLevel=FuturaWorldType120.isFutura(world)?127:63;
        int difficulty=world.difficultySetting.getDifficultyId();
        for(Object raw:world.playerEntities) {
            EntityPlayer player=(EntityPlayer)raw;
            if(!player.isEntityAlive()||player.capabilities.disableDamage||player.isPlayerSleeping())continue;
            int px=MathHelper.floor_double(player.posX),py=MathHelper.floor_double(player.posY),pz=MathHelper.floor_double(player.posZ);
            if(py<seaLevel||!world.canBlockSeeTheSky(px,py,pz))continue;
            float local=localDifficulty(difficulty,world.getWorldTime(),
                    world.getChunkFromBlockCoords(px,pz).inhabitedTime,world.getCurrentMoonPhaseFactor());
            if(local<=world.rand.nextFloat()*3||!insomniaRoll(world.rand,player.getEntityData().getInteger(REST_KEY)))continue;
            int y=py+20+world.rand.nextInt(15),x=px-10+world.rand.nextInt(21),z=pz-10+world.rand.nextInt(21);
            // Never clamp a too-high spawn down into a roof or force-load a neighbour.
            if(y>=world.getHeight()-1||!world.blockExists(x,y,z)||!world.isAirBlock(x,y,z))continue;
            // Backport population guard: vanilla's custom spawner is uncapped.
            // Share one nearby flock across players rather than adding a flock per
            // sleepless player indefinitely. Dead entities do not occupy the budget.
            int nearby=0;
            for(Object entity:world.getEntitiesWithinAABB(EntityPhantom120.class,
                    AxisAlignedBB.getBoundingBox(px-64,0,pz-64,px+65,world.getHeight(),pz+65)))
                if(((EntityPhantom120)entity).isEntityAlive())++nearby;
            int count=Math.min(groupSize(world.rand,difficulty),Math.max(0,difficulty+1-nearby));
            for(int i=0;i<count;i++) {
                EntityPhantom120 phantom=new EntityPhantom120(world);
                phantom.setLocationAndAngles(x+.5,y,z+.5,0,0);
                if(world.getCollidingBoundingBoxes(phantom,phantom.boundingBox).isEmpty()
                        &&!world.isAnyLiquid(phantom.boundingBox))world.spawnEntityInWorld(phantom);
            }
        }
    }
}
