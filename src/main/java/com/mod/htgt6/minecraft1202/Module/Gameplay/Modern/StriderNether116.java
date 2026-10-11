package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
/** Lava spawning cannot use 1.7's land/water-only candidate predicates. */
public final class StriderNether116 {
    public static final int INTERVAL=400,ATTEMPTS=16,MAX_LOADED=10;
    private static boolean counts(Object value){return value instanceof net.minecraft.entity.passive.EntityAnimal
        &&!(value instanceof net.minecraft.entity.monster.IMob)&&!((EntityLiving)value).isDead&&!((EntityLiving)value).isNoDespawnRequired();}
    /** A shared passive budget, scaled by the union of player spawning chunks. */
    public static int remainingCapacity(World w){
        java.util.HashSet<Long> chunks=new java.util.HashSet<Long>();
        for(Object value:w.playerEntities){EntityPlayer p=(EntityPlayer)value;int cx=MathHelper.floor_double(p.posX)>>4,cz=MathHelper.floor_double(p.posZ)>>4;
            for(int dx=-8;dx<=8;dx++)for(int dz=-8;dz<=8;dz++)if(w.blockExists((cx+dx)<<4,64,(cz+dz)<<4))chunks.add(((long)(cx+dx)<<32)^((cz+dz)&0xffffffffL));}
        int count=0;for(Object value:w.loadedEntityList)if(counts(value))count++;
        return Math.max(0,MAX_LOADED*chunks.size()/289-count);
    }
    public static boolean localCapacity(World w,int x,int z){
        int cx=x>>4,cz=z>>4;
        for(Object value:w.playerEntities){EntityPlayer p=(EntityPlayer)value;int pcx=MathHelper.floor_double(p.posX)>>4,pcz=MathHelper.floor_double(p.posZ)>>4;
            if(Math.abs(pcx-cx)>8||Math.abs(pcz-cz)>8)continue;
            int count=0;for(Object entity:w.loadedEntityList)if(counts(entity)){Entity e=(Entity)entity;
                if(Math.abs((MathHelper.floor_double(e.posX)>>4)-pcx)<=8&&Math.abs((MathHelper.floor_double(e.posZ)>>4)-pcz)<=8)count++;}
            if(count<MAX_LOADED)return true;
        }return false;
    }
    @SubscribeEvent public void tick(TickEvent.WorldTickEvent event){if(event.phase==TickEvent.Phase.END)spawn(event.world);}
    public void spawn(World w){
        if(w.isRemote||w.provider.dimensionId!=-1||w.getTotalWorldTime()%INTERVAL!=0||!w.getGameRules().getGameRuleBooleanValue("doMobSpawning")||w.playerEntities.isEmpty())return;
        int available=remainingCapacity(w);if(available<=0)return;
        EntityPlayer player=(EntityPlayer)w.playerEntities.get((int)(w.getTotalWorldTime()/INTERVAL%w.playerEntities.size()));
        int bx=MathHelper.floor_double(player.posX),bz=MathHelper.floor_double(player.posZ);
        for(int attempt=0;attempt<ATTEMPTS;attempt++){
            int x=bx+w.rand.nextInt(161)-80,z=bz+w.rand.nextInt(161)-80;if(!w.blockExists(x,64,z))continue;
            for(int y=120;y>=16;y--){
                if(w.getBlock(x,y,z).getMaterial()!=Material.lava||w.getBlock(x,y+1,z).getMaterial()!=Material.air)continue;
                if(w.getClosestPlayer(x+.5,y+1,z+.5,24)!=null||!localCapacity(w,x,z))break;
                IEntityLivingData group=null;int size=Math.min(2+w.rand.nextInt(3),available);
                for(int i=0;i<size;i++){
                    int px=x+(i==0?0:w.rand.nextInt(5)-2),pz=z+(i==0?0:w.rand.nextInt(5)-2);if(!w.blockExists(px,y,pz)||w.getBlock(px,y,pz).getMaterial()!=Material.lava)continue;
                    if(w.getClosestPlayer(px+.5,y+1,pz+.5,24)!=null||!localCapacity(w,px,pz))continue;
                    EntityStrider116 strider=new EntityStrider116(w);strider.setLocationAndAngles(px+.5,y+1,pz+.5,w.rand.nextFloat()*360,0);
                    Event.Result permission=net.minecraftforge.event.ForgeEventFactory.canEntitySpawn(strider,w,(float)strider.posX,(float)strider.posY,(float)strider.posZ);
                    if(permission==Event.Result.DENY||permission==Event.Result.DEFAULT&&!strider.getCanSpawnHere())continue;
                    if(!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(strider,w,(float)strider.posX,(float)strider.posY,(float)strider.posZ))group=strider.onSpawnWithEgg(group);
                    if(w.spawnEntityInWorld(strider))available--;
                }
                return;
            }
        }
    }
}
