package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.Map;
import java.util.WeakHashMap;
import micdoodle8.mods.galacticraft.core.blocks.GCBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Long-lived high-wind cells with frequent lightning, inspired by Neptune's supersonic weather. */
public final class NeptuneStormHandler {
    private final Map<World,Integer> stormTime=new WeakHashMap<World,Integer>();
    private final Map<World,Integer> gustTime=new WeakHashMap<World,Integer>();
    private final Map<World,Double> gustDirection=new WeakHashMap<World,Double>();
    @SubscribeEvent public void tick(TickEvent.WorldTickEvent event){
        World world=event.world;
        if(event.phase!=TickEvent.Phase.END||world.isRemote||!(world.provider instanceof WorldProviderNeptune))return;
        Integer left=stormTime.get(world); int ticks=left==null?0:left;
        if(ticks<=0&&world.rand.nextInt(600)==0){
            ticks=600+world.rand.nextInt(1000);
            world.getWorldInfo().setRaining(true);world.getWorldInfo().setThundering(true);
            world.getWorldInfo().setRainTime(ticks);world.getWorldInfo().setThunderTime(ticks);
        }
        if(ticks>0){
            ticks--;
            world.getWorldInfo().setRaining(true);world.getWorldInfo().setThundering(true);
            if(world.rand.nextInt(9)==0&&!world.playerEntities.isEmpty()){
                EntityPlayer target=stormTarget(world);
                if(target!=null){double x=target.posX+world.rand.nextInt(61)-30,z=target.posZ+world.rand.nextInt(61)-30;
                    world.addWeatherEffect(new EntityLightningBolt(world,x,165+world.rand.nextInt(61),z));}
            }
        }else if(world.getWorldInfo().isThundering()||world.getWorldInfo().isRaining()){
            world.getWorldInfo().setThundering(false);world.getWorldInfo().setRaining(false);
        }
        stormTime.put(world,ticks);
        Integer gust=gustTime.get(world);int gustTicks=gust==null?0:gust;
        int chance=ticks>0?75:700;
        if(gustTicks<=0&&world.rand.nextInt(chance)==0){gustTicks=(ticks>0?30:12)+world.rand.nextInt(ticks>0?55:18);gustDirection.put(world,world.rand.nextDouble()*Math.PI*2.0D);}
        if(gustTicks>0){gustTicks--;double angle=gustDirection.get(world),speed=ticks>0?0.042D:0.020D;
            for(Object value:world.playerEntities){EntityPlayer player=(EntityPlayer)value;
                if(player.posY<140||player.capabilities.isCreativeMode||player.ridingEntity!=null||isInSafeZone(world,player))continue;
                player.motionX=MathHelper.clamp_double(player.motionX+Math.cos(angle)*speed,-1.8D,1.8D);
                player.motionZ=MathHelper.clamp_double(player.motionZ+Math.sin(angle)*speed,-1.8D,1.8D);
                player.motionY=MathHelper.clamp_double(player.motionY+Math.sin(gustTicks*.31D)*0.008D,-0.8D,0.8D);
                player.velocityChanged=true;
            }
        }
        gustTime.put(world,gustTicks);
    }
    private EntityPlayer stormTarget(World world){
        for(int attempt=0;attempt<world.playerEntities.size();attempt++){
            EntityPlayer player=(EntityPlayer)world.playerEntities.get(world.rand.nextInt(world.playerEntities.size()));
            if(!isInSafeZone(world,player))return player;
        }return null;
    }
    public static boolean isInSafeZone(World world,EntityPlayer player){
        int px=MathHelper.floor_double(player.posX),py=MathHelper.floor_double(player.posY),pz=MathHelper.floor_double(player.posZ);
        if (!world.canBlockSeeTheSky(px, py + 1, pz)) return true;
        if(player.posY<198||player.posY>209
                || !NeptuneLandingData.get(world).containsDeckColumn(px, pz))return false;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)for(int dy=1;dy<=6;dy++){
            Block block=world.getBlock(px+dx,py-dy,pz+dz);
            if(block==GCBlocks.basicBlock||block==Blocks.glass)return true;
        }return false;
    }
}
