package com.mod.htgt6.minecraft1202.Module.Entity.Exploration;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Exact 1.12 Vex task/move-helper contract, including gravity-free phasing. */
public final class EntityVex112 extends EntityMob {
    private int boundX,boundY,boundZ,lifeTicks;private boolean hasBound,limitedLife;private EntityLiving owner;
    public EntityVex112(World world){super(world);isImmuneToFire=true;com.mod.htgt6.common.util.EntityMoveHelperAccess112.install(this,new VexMoveHelper112(this));setSize(.4F,.8F);experienceValue=3;
        tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(4,new ChargeAI());tasks.addTask(8,new RandomMoveAI());tasks.addTask(9,new EntityAIWatchClosest(this,EntityPlayer.class,3,1));tasks.addTask(10,new EntityAIWatchClosest(this,EntityLiving.class,8));
        targetTasks.addTask(1,new EntityAIHurtByTarget(this,true));targetTasks.addTask(2,new CopyOwnerTargetAI());targetTasks.addTask(3,new EntityAINearestAttackableTarget(this,EntityPlayer.class,0,true));
    }
    private static final int FLAGS_WATCHER = 20;
    @Override protected void entityInit() { super.entityInit(); dataWatcher.addObject(FLAGS_WATCHER, Byte.valueOf((byte)0)); }
    private void setCharging(boolean value) {
        byte flags = dataWatcher.getWatchableObjectByte(FLAGS_WATCHER);
        dataWatcher.updateObject(FLAGS_WATCHER, Byte.valueOf((byte)(value ? flags | 1 : flags & ~1)));
    }

    // Forge 1.7.10 requires this opt-in to execute tasks and targetTasks.
    @Override protected boolean isAIEnabled() { return true; }

    @Override public net.minecraft.entity.IEntityLivingData onSpawnWithEgg(net.minecraft.entity.IEntityLivingData data) {
        data=super.onSpawnWithEgg(data);setCurrentItemOrArmor(0,new ItemStack(Items.iron_sword));equipmentDropChances[0]=0;return data;
    }
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(14);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4);}
    public void setOwner(EntityLiving owner){this.owner=owner;}public void setBoundOrigin(int x,int y,int z){boundX=x;boundY=y;boundZ=z;hasBound=true;}public void setLimitedLife(int ticks){limitedLife=true;lifeTicks=ticks;}public boolean isCharging(){return (dataWatcher.getWatchableObjectByte(FLAGS_WATCHER) & 1) != 0;}
    @Override public void onUpdate(){noClip=true;super.onUpdate();noClip=false;fallDistance=0;if(!worldObj.isRemote&&limitedLife&&--lifeTicks<=0){lifeTicks=20;attackEntityFrom(DamageSource.starve,1);}}
    /** 1.12 sets noGravity=true; this is its 1.7 movement equivalent. */
    @Override public void moveEntityWithHeading(float strafe,float forward){moveFlying(strafe,forward,.02F);moveEntity(motionX,motionY,motionZ);motionX*=.91;motionY*=.91;motionZ*=.91;}
    @Override public boolean getCanSpawnHere(){return true;}@Override protected boolean canDespawn(){return !limitedLife;}@Override protected net.minecraft.item.Item getDropItem(){return null;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);if(hasBound){tag.setInteger("BoundX",boundX);tag.setInteger("BoundY",boundY);tag.setInteger("BoundZ",boundZ);}if(limitedLife)tag.setInteger("LifeTicks",lifeTicks);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);if(tag.hasKey("BoundX",99))setBoundOrigin(tag.getInteger("BoundX"),tag.getInteger("BoundY"),tag.getInteger("BoundZ"));if(tag.hasKey("LifeTicks",99))setLimitedLife(tag.getInteger("LifeTicks"));}

    private final class CopyOwnerTargetAI extends EntityAIBase{@Override public boolean shouldExecute(){return owner!=null&&owner.getAttackTarget()!=null&&owner.getAttackTarget().isEntityAlive();}@Override public void startExecuting(){setAttackTarget(owner.getAttackTarget());}}
    private final class RandomMoveAI extends EntityAIBase{RandomMoveAI(){setMutexBits(1);}@Override public boolean shouldExecute(){return !getMoveHelper().isUpdating()&&rand.nextInt(7)==0;}@Override public boolean continueExecuting(){return false;}@Override public void updateTask(){int cx=hasBound?boundX:MathHelper.floor_double(posX),cy=hasBound?boundY:MathHelper.floor_double(posY),cz=hasBound?boundZ:MathHelper.floor_double(posZ);for(int i=0;i<3;i++){int x=cx+rand.nextInt(15)-7,y=cy+rand.nextInt(11)-5,z=cz+rand.nextInt(15)-7;if(worldObj.isAirBlock(x,y,z)){getMoveHelper().setMoveTo(x+.5,y+.5,z+.5,.25);if(getAttackTarget()==null)getLookHelper().setLookPosition(x+.5,y+.5,z+.5,180,20);break;}}}}
    private final class ChargeAI extends EntityAIBase{ChargeAI(){setMutexBits(1);}@Override public boolean shouldExecute(){return getAttackTarget()!=null&&!getMoveHelper().isUpdating()&&rand.nextInt(7)==0&&getDistanceSqToEntity(getAttackTarget())>4;}@Override public boolean continueExecuting(){return getMoveHelper().isUpdating()&&isCharging()&&getAttackTarget()!=null&&getAttackTarget().isEntityAlive();}@Override public void startExecuting(){EntityLivingBase target=getAttackTarget();getMoveHelper().setMoveTo(target.posX,target.posY+target.getEyeHeight(),target.posZ,1);setCharging(true);worldObj.playSoundAtEntity(EntityVex112.this,"mob.endermen.scream",1,1);}@Override public void resetTask(){setCharging(false);}@Override public void updateTask(){EntityLivingBase target=getAttackTarget();if(boundingBox.intersectsWith(target.boundingBox)){attackEntityAsMob(target);setCharging(false);}else if(getDistanceSqToEntity(target)<9)getMoveHelper().setMoveTo(target.posX,target.posY+target.getEyeHeight(),target.posZ,1);}}
    private static final class VexMoveHelper112 extends EntityMoveHelper{private final EntityVex112 vex;private double x,y,z,speed;private boolean active;VexMoveHelper112(EntityVex112 v){super(v);vex=v;}@Override public void setMoveTo(double x,double y,double z,double speed){this.x=x;this.y=y;this.z=z;this.speed=speed;active=true;super.setMoveTo(x,y,z,speed);}@Override public boolean isUpdating(){return active;}@Override public void onUpdateMoveHelper(){if(!active)return;double dx=x-vex.posX,dy=y-vex.posY,dz=z-vex.posZ,d=Math.sqrt(dx*dx+dy*dy+dz*dz);if(d<vex.boundingBox.getAverageEdgeLength()){active=false;vex.motionX*=.5;vex.motionY*=.5;vex.motionZ*=.5;}else{vex.motionX+=dx/d*.05*speed;vex.motionY+=dy/d*.05*speed;vex.motionZ+=dz/d*.05*speed;double fx=vex.getAttackTarget()==null?vex.motionX:vex.getAttackTarget().posX-vex.posX,fz=vex.getAttackTarget()==null?vex.motionZ:vex.getAttackTarget().posZ-vex.posZ;vex.rotationYaw=vex.renderYawOffset=-(float)Math.atan2(fx,fz)*57.295776F;}}}
}
