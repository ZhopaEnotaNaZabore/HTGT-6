package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

/** Bastion guard; independent AI tasks, shared piglin conversion and group retaliation. */
public final class EntityPiglinBrute116 extends EntityPiglin116 {
    private boolean hasHome;
    private int homeX,homeY,homeZ;
    public EntityPiglinBrute116(World w){
        super(w);experienceValue=20;tasks.taskEntries.clear();targetTasks.taskEntries.clear();
        tasks.addTask(0,new EntityAISwimming(this));
        tasks.addTask(2,new EntityAIAttackOnCollide(this,1,true){
            @Override public boolean continueExecuting(){EntityLivingBase target=getAttackTarget();return target!=null&&target.isEntityAlive()&&getDistanceSqToEntity(target)<256&&(!(target instanceof EntityPlayer)||!((EntityPlayer)target).capabilities.isCreativeMode);}
        });
        tasks.addTask(4,new EntityAIMoveTowardsRestriction(this,.6));
        tasks.addTask(5,new EntityAIWander(this,.6));
        tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,8));
        tasks.addTask(7,new EntityAILookIdle(this));
        targetTasks.addTask(1,new SelectBruteTarget());
    }
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(7);}
    @Override public boolean acceptsGold(){return false;}
    @Override public void setChild(boolean child){super.setChild(false);}
    @Override protected boolean canDespawn(){return false;}
    @Override public boolean getCanSpawnHere(){return false;}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){setCurrentItemOrArmor(0,new ItemStack(Items.golden_axe));rememberHome(MathHelper.floor_double(posX),MathHelper.floor_double(posY),MathHelper.floor_double(posZ));return data;}
    public void rememberHome(int x,int y,int z){hasHome=true;homeX=x;homeY=y;homeZ=z;setHomeArea(x,y,z,16);}
    public EntityLivingBase nearestBruteTarget(){
        EntityLivingBase best=null;double distance=144;
        for(Object o:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)o;double d=getDistanceSqToEntity(p);if(p.isEntityAlive()&&!p.capabilities.isCreativeMode&&d<distance&&getEntitySenses().canSee(p)){best=p;distance=d;}}
        if(best!=null)return best;
        distance=256;
        for(Object o:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(16,16,16))){
            EntityLivingBase e=(EntityLivingBase)o;boolean hated=e instanceof EntityWither||e instanceof EntitySkeleton&&((EntitySkeleton)e).getSkeletonType()==1;
            double d=getDistanceSqToEntity(e);if(hated&&e.isEntityAlive()&&d<distance&&getEntitySenses().canSee(e)){best=e;distance=d;}
        }
        return best;
    }
    private final class SelectBruteTarget extends EntityAIBase {
        EntityLivingBase next;
        SelectBruteTarget(){setMutexBits(1);}
        public boolean shouldExecute(){if(ticksExisted%10!=0)return false;EntityLivingBase current=getAttackTarget();if(current!=null&&current.isEntityAlive()&&(!(current instanceof EntityPlayer)||!((EntityPlayer)current).capabilities.isCreativeMode)&&getDistanceSqToEntity(current)<256)return false;next=nearestBruteTarget();if(next==null)setAttackTarget(null);return next!=null;}
        public void startExecuting(){setAttackTarget(next);playSound("htgt6:entity.piglin_brute.angry",1,1);}
        public boolean continueExecuting(){return false;}
    }
    @Override public boolean attackEntityFrom(DamageSource source,float damage){
        EntityLivingBase previous=getAttackTarget();boolean hit=super.attackEntityFrom(source,damage);
        // Piglins do not retaliate against members of their own family.
        if(source.getEntity() instanceof EntityPiglin116)setAttackTarget(previous);
        return hit;
    }
    @Override protected String getLivingSound(){return "htgt6:entity.piglin_brute.ambient";}
    @Override protected String getHurtSound(){return "htgt6:entity.piglin_brute.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.piglin_brute.death";}
    @Override protected String conversionSound(){return "htgt6:entity.piglin_brute.converted_to_zombified";}
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block block){playSound("htgt6:entity.piglin_brute.step",.15F,1);}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setBoolean("BruteHasHome",hasHome);n.setInteger("BruteHomeX",homeX);n.setInteger("BruteHomeY",homeY);n.setInteger("BruteHomeZ",homeZ);}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);hasHome=n.getBoolean("BruteHasHome");if(hasHome)rememberHome(n.getInteger("BruteHomeX"),n.getInteger("BruteHomeY"),n.getInteger("BruteHomeZ"));else detachHome();}
}
