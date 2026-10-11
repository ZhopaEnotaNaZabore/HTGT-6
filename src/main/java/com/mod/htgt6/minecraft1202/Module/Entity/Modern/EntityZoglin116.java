package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoglinCombat116;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Undead monster, not an animal adapter: no breeding, aging or Nether conversion. */
public final class EntityZoglin116 extends EntityMob implements HoglinCombat116.Animated {
    private int attackTicks,retaliation;
    public EntityZoglin116(World w){
        super(w);setSize(1.3964844F,1.4F);isImmuneToFire=true;experienceValue=5;
        tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(2,new HoglinCombat116.Melee(this));tasks.addTask(5,new EntityAIWander(this,.4));tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(7,new EntityAILookIdle(this));targetTasks.addTask(1,new SelectTarget());
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Byte.valueOf((byte)0));}
    @Override protected boolean isAIEnabled(){return true;}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3);getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(.6);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);}
    public void setChild(boolean child){dataWatcher.updateObject(20,Byte.valueOf((byte)(child?1:0)));setSize(child?.6982422F:1.3964844F,child?.7F:1.4F);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(child?.5:6);}
    @Override public boolean isChild(){return dataWatcher.getWatchableObjectByte(20)!=0;}
    @Override public EnumCreatureAttribute getCreatureAttribute(){return EnumCreatureAttribute.UNDEAD;}
    @Override public boolean allowLeashing(){return !getLeashed();}
    @Override public boolean getCanSpawnHere(){return false;}
    @Override public int attackAnimationTicks(){return attackTicks;}
    @Override public void onLivingUpdate(){if(attackTicks>0)attackTicks--;super.onLivingUpdate();if(worldObj.isRemote){setSize(isChild()?.6982422F:1.3964844F,isChild()?.7F:1.4F);return;}if(retaliation>0&&--retaliation==0)setAttackTarget(null);}
    @Override public boolean attackEntityAsMob(Entity target){if(worldObj.isRemote||!(target instanceof EntityLivingBase))return false;attackTicks=10;worldObj.setEntityState(this,(byte)4);playSound("htgt6:entity.zoglin.attack",1,1);return HoglinCombat116.attack(this,target);}
    @Override public void handleHealthUpdate(byte state){if(state==4)attackTicks=10;else super.handleHealthUpdate(state);}
    public boolean canTarget(EntityLivingBase target){return target!=this&&target.isEntityAlive()&&!(target instanceof EntityZoglin116)&&!(target instanceof EntityCreeper)&&(!(target instanceof EntityPlayer)||!((EntityPlayer)target).capabilities.isCreativeMode);}
    @Override public boolean attackEntityFrom(DamageSource source,float damage){boolean hit=super.attackEntityFrom(source,damage);if(hit&&!worldObj.isRemote&&source.getEntity() instanceof EntityLivingBase&&canTarget((EntityLivingBase)source.getEntity())){setAttackTarget((EntityLivingBase)source.getEntity());retaliation=200;}return hit;}
    private final class SelectTarget extends EntityAIBase {
        EntityLivingBase next;SelectTarget(){setMutexBits(1);}
        public boolean shouldExecute(){if(ticksExisted%10!=0)return false;EntityLivingBase current=getAttackTarget();if(current!=null&&canTarget(current)&&getDistanceSqToEntity(current)<256)return false;
            next=null;double distance=256;for(Object o:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(16,16,16))){EntityLivingBase candidate=(EntityLivingBase)o;double d=getDistanceSqToEntity(candidate);if(canTarget(candidate)&&d<distance&&getEntitySenses().canSee(candidate)){next=candidate;distance=d;}}if(next==null)setAttackTarget(null);return next!=null;}
        public void startExecuting(){setAttackTarget(next);playSound("htgt6:entity.zoglin.angry",1,1);}
        public boolean continueExecuting(){return false;}
    }
    @Override protected void dropFewItems(boolean recentlyHit,int looting){dropItem(Items.rotten_flesh,1+rand.nextInt(3)+Math.round(rand.nextFloat()*looting));}
    @Override protected String getLivingSound(){return "htgt6:entity.zoglin.ambient";}
    @Override protected String getHurtSound(){return "htgt6:entity.zoglin.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.zoglin.death";}
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block b){playSound("htgt6:entity.zoglin.step",.15F,1);}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setBoolean("IsBaby",isChild());}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);setChild(n.getBoolean("IsBaby"));}
}
