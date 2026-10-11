package com.mod.htgt6.minecraft1202.Module.Events.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SculkCatalyst;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SculkSensor;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SculkShrieker;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityWarden120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaDeepDarkRuntime120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;

import java.lang.reflect.Field;
import java.util.*;

/** 1.20 game-event bridge, vibration routing, wool occlusion and catalyst death listener. */
public final class Backport120SculkHandler {
    private static final Field EXPERIENCE=experienceField();
    private final Map<Entity,Position> previous=new WeakHashMap<Entity,Position>();

    @SubscribeEvent public void worldTick(TickEvent.WorldTickEvent event){
        if(event.phase!=TickEvent.Phase.END||event.world.isRemote)return;
        List<TileEntity> listeners=listeners(event.world);
        @SuppressWarnings("unchecked") List<Entity> entities=new ArrayList<Entity>((List<Entity>)event.world.loadedEntityList);
        for(Entity entity:entities){
            if(entity.isDead||!(entity instanceof EntityLivingBase)||entity instanceof EntityWarden120)continue;
            Position now=new Position(entity.posX,entity.posY,entity.posZ);Position old=previous.put(entity,now);
            if(old==null||entity.isSneaking()||old.distanceSq(now)<.04D)continue;
            emitTo(event.world,listeners,entity.posX,entity.posY,entity.posZ,1,entity);
        }
    }

    @SubscribeEvent public void blockPlaced(BlockEvent.PlaceEvent event){emit(event.world,event.x+.5D,event.y+.5D,event.z+.5D,13,event.player,false);}
    @SubscribeEvent public void blockBroken(BlockEvent.BreakEvent event){emit(event.world,event.x+.5D,event.y+.5D,event.z+.5D,12,event.getPlayer(),false);}
    @SubscribeEvent public void interact(PlayerInteractEvent event){
        if(event.world==null||event.world.isRemote||event.action==PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)return;
        if(event.action==PlayerInteractEvent.Action.RIGHT_CLICK_AIR)
            emit(event.world,event.entityPlayer.posX,event.entityPlayer.posY,event.entityPlayer.posZ,10,event.entityPlayer,false);
        else emit(event.world,event.x+.5D,event.y+.5D,event.z+.5D,10,event.entityPlayer,false);
    }
    @SubscribeEvent public void hurt(LivingHurtEvent event){
        Entity source=event.source==null?null:event.source.getEntity();
        emit(event.entityLiving.worldObj,event.entityLiving.posX,event.entityLiving.posY,event.entityLiving.posZ,7,source,false);
    }
    @SubscribeEvent public void fall(LivingFallEvent event){if(event.distance>1F)emit(event.entityLiving.worldObj,event.entityLiving.posX,event.entityLiving.posY,event.entityLiving.posZ,2,event.entityLiving,false);}
    @SubscribeEvent public void shoot(ArrowLooseEvent event){emit(event.entityPlayer.worldObj,event.entityPlayer.posX,event.entityPlayer.posY,event.entityPlayer.posZ,3,event.entityPlayer,false);}
    @SubscribeEvent public void entityInteract(EntityInteractEvent event){emit(event.entityPlayer.worldObj,event.target.posX,event.target.posY,event.target.posZ,6,event.entityPlayer,false);}
    @SubscribeEvent public void finishUse(PlayerUseItemEvent.Finish event){emit(event.entityPlayer.worldObj,event.entityPlayer.posX,event.entityPlayer.posY,event.entityPlayer.posZ,frequencyForUseAction(event.item==null?EnumAction.none:event.item.getItemUseAction()),event.entityPlayer,false);}
    @SubscribeEvent public void exploded(ExplosionEvent.Detonate event){emit(event.world,event.explosion.explosionX,event.explosion.explosionY,event.explosion.explosionZ,15,event.explosion.getExplosivePlacedBy(),false);}

    @SubscribeEvent public void onLivingDeath(LivingDeathEvent event) {
        World world=event.entityLiving.worldObj;if(world.isRemote)return;
        Entity source=event.source==null?null:event.source.getEntity();
        emit(world,event.entityLiving.posX,event.entityLiving.posY,event.entityLiving.posZ,15,source,false);
        int ox=MathHelper.floor_double(event.entityLiving.posX),oy=MathHelper.floor_double(event.entityLiving.posY),oz=MathHelper.floor_double(event.entityLiving.posZ);
        int[] nearest=null;double best=Double.MAX_VALUE;
        for(int y=oy-4;y<=oy+4;y++)for(int x=ox-8;x<=ox+8;x++)for(int z=oz-8;z<=oz+8;z++)
            if(world.getBlock(x,y,z)==Backport120Content.sculkCatalyst){double d=(x-ox)*(x-ox)+(y-oy)*(y-oy)+(z-oz)*(z-oz);if(d<best){best=d;nearest=new int[]{x,y,z};}}
        if(nearest==null)return;
        ((BlockBackport120SculkCatalyst)Backport120Content.sculkCatalyst).bloom(world,nearest[0],nearest[1],nearest[2]);
        int charge=experience(event.entityLiving);
        if(charge>0)FuturaDeepDarkRuntime120.spreadCharge(world,world.rand,ox,oy,oz,charge);
    }

    /** Routes a modern game-event frequency to loaded listeners with travel time and wool occlusion. */
    public static void emit(World world,double x,double y,double z,int frequency,Entity source,boolean resonance){
        if(world==null||world.isRemote||frequency<1||frequency>15)return;
        emitTo(world,listeners(world),x,y,z,frequency,source);
    }
    private static void emitTo(World world,List<TileEntity> tiles,double x,double y,double z,int frequency,Entity source){
        Entity responsible=responsible(source);
        for(TileEntity tile:tiles){
            Block block=tile.getBlockType();
            int range=block instanceof BlockBackport120SculkSensor&&((BlockBackport120SculkSensor)block).isCalibrated()?16:8;
            double dx=tile.xCoord+.5D-x,dy=tile.yCoord+.5D-y,dz=tile.zCoord+.5D-z;
            double distance=Math.sqrt(dx*dx+dy*dy+dz*dz);if(distance>range||occluded(world,x,y,z,tile.xCoord+.5D,tile.yCoord+.5D,tile.zCoord+.5D))continue;
            int delay=Math.max(1,MathHelper.ceiling_double_int(distance));
            if(block instanceof BlockBackport120SculkSensor)
                ((BlockBackport120SculkSensor)block).receiveVibration(world,tile.xCoord,tile.yCoord,tile.zCoord,frequency,delay);
            else ((BlockBackport120SculkShrieker)block).receiveVibration(world,tile.xCoord,tile.yCoord,tile.zCoord,delay,responsible);
        }
        notifyWardens(world,x,y,z,frequency,source);
    }
    @SuppressWarnings("unchecked") private static void notifyWardens(World world,double x,double y,double z,int frequency,Entity source){
        if(source instanceof EntityWarden120)return;
        List<EntityWarden120> wardens=world.getEntitiesWithinAABB(EntityWarden120.class,
                net.minecraft.util.AxisAlignedBB.getBoundingBox(x-16,y-16,z-16,x+16,y+16,z+16));
        for(EntityWarden120 warden:wardens)if(warden.getDistanceSq(x,y,z)<=256D
                &&!occluded(world,x,y,z,warden.posX,warden.posY+warden.getEyeHeight(),warden.posZ))
            warden.hearVibration(source,x,y,z,frequency);
    }
    private static Entity responsible(Entity source){
        if(source instanceof EntityArrow&&((EntityArrow)source).shootingEntity!=null)return ((EntityArrow)source).shootingEntity;
        if(source instanceof EntityThrowable&&((EntityThrowable)source).getThrower()!=null)return ((EntityThrowable)source).getThrower();
        return source;
    }
    private static List<TileEntity> listeners(World world){
        @SuppressWarnings("unchecked") List<TileEntity> loaded=new ArrayList<TileEntity>((List<TileEntity>)world.loadedTileEntityList);
        List<TileEntity> result=new ArrayList<TileEntity>();for(TileEntity tile:loaded){Block block=tile.getBlockType();
            if(block instanceof BlockBackport120SculkSensor||block instanceof BlockBackport120SculkShrieker)result.add(tile);}return result;
    }

    static boolean occluded(World world,double ax,double ay,double az,double bx,double by,double bz){
        double dx=bx-ax,dy=by-ay,dz=bz-az;int steps=Math.max(1,MathHelper.ceiling_double_int(Math.sqrt(dx*dx+dy*dy+dz*dz)*4D));
        for(int i=1;i<steps;i++){double t=(double)i/steps;int x=MathHelper.floor_double(ax+dx*t),y=MathHelper.floor_double(ay+dy*t),z=MathHelper.floor_double(az+dz*t);if(world.getBlock(x,y,z)==net.minecraft.init.Blocks.wool)return true;}
        return false;
    }

    private static int experience(EntityLivingBase living){
        if(living instanceof EntityPlayer)return Math.max(0,Math.min(100,((EntityPlayer)living).experienceTotal));
        if(!living.worldObj.getGameRules().getGameRuleBooleanValue("doMobLoot"))return 0;
        if(living instanceof EntityLiving&&EXPERIENCE!=null)try{return Math.max(0,EXPERIENCE.getInt(living));}catch(Exception ignored){ }
        return 0;
    }
    private static Field experienceField(){try{return ReflectionHelper.findField(EntityLiving.class,"experienceValue","field_70728_aV");}catch(Exception ignored){return null;}}
    public static int frequencyForUseAction(EnumAction action){return action==EnumAction.eat||action==EnumAction.drink?8:3;}
    public static int frequencyForFixture(String event){String[] groups={"step swim flap","projectile_land hit_ground splash","item_interact_finish projectile_shoot instrument_play","roar shake glide","dismount equip","interact shear mount","damage","drink eat","close deactivate detach","open activate attach prime","block_change","destroy fluid_pickup","place fluid_place","entity_place lightning teleport","die explode"};for(int i=0;i<groups.length;i++)for(String name:groups[i].split(" "))if(name.equals(event))return i+1;return 0;}
    private static final class Position{final double x,y,z;Position(double x,double y,double z){this.x=x;this.y=y;this.z=z;}double distanceSq(Position p){double a=x-p.x,b=y-p.y,c=z-p.z;return a*a+b*b+c*c;}}
}
