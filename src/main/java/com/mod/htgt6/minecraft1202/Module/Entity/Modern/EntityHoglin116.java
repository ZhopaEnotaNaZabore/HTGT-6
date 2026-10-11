package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.HoglinCombat116;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.*;

public final class EntityHoglin116 extends EntityAnimal implements IMob,HoglinCombat116.Animated {
    private int attackTicks,conversion,pacified,retaliation,scanIndex=2601;
    private boolean immuneToZombification,cannotBeHunted,repellent;
    private int repelX,repelY,repelZ,scanX,scanY,scanZ;
    private EntityLivingBase fleeing;
    private int fleeTicks;
    public EntityHoglin116(World w){
        super(w);setSize(1.3964844F,1.4F);experienceValue=5;
        tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new AvoidRepellent());
        tasks.addTask(2,new EntityAIMate(this,.6));tasks.addTask(3,new HoglinCombat116.Melee(this));
        tasks.addTask(4,new EntityAITempt(this,.6,Item.getItemFromBlock(NetherVegetationContent116.crimsonFungus),false));
        tasks.addTask(5,new EntityAIFollowParent(this,.6));tasks.addTask(6,new EntityAIWander(this,.4));
        tasks.addTask(7,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(8,new EntityAILookIdle(this));
        targetTasks.addTask(1,new SelectTarget());
    }
    @Override public boolean isCreatureType(EnumCreatureType type,boolean forSpawnCount){return type==EnumCreatureType.monster;}
    @Override protected boolean isAIEnabled(){return true;}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(40);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3);getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(.6);getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);}
    @Override public void setGrowingAge(int age){super.setGrowingAge(age);if(getEntityAttribute(SharedMonsterAttributes.attackDamage)!=null)getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(age<0?.5:6);experienceValue=age<0?3:5;}
    @Override public boolean isBreedingItem(ItemStack stack){return stack!=null&&stack.getItem()==Item.getItemFromBlock(NetherVegetationContent116.crimsonFungus);}
    @Override public EntityAgeable createChild(EntityAgeable mate){EntityHoglin116 child=new EntityHoglin116(worldObj);child.func_110163_bv();return child;}
    @Override public boolean interact(EntityPlayer player){
        boolean food=isBreedingItem(player.getHeldItem());if(food&&pacified>0)return false;
        if(food&&isChild()){
            if(!worldObj.isRemote){setGrowingAge(Math.min(0,getGrowingAge()+(int)(-getGrowingAge()/20*.1F)*20));func_110163_bv();if(!player.capabilities.isCreativeMode&&--player.getHeldItem().stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}
            return true;
        }
        boolean used=super.interact(player);if(used&&food&&!worldObj.isRemote){func_110163_bv();setAttackTarget(null);retaliation=0;}return used;
    }
    @Override protected boolean canDespawn(){return !isNoDespawnRequired();}
    @Override public boolean allowLeashing(){return !getLeashed();}
    @Override protected int getExperiencePoints(EntityPlayer player){return isChild()?3:5;}
    public boolean canBeHunted(){return !isChild()&&!cannotBeHunted;}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){if(rand.nextFloat()<.2F)setGrowingAge(-24000);return data;}
    @Override public boolean getCanSpawnHere(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);return worldObj.provider.dimensionId==-1&&worldObj.difficultySetting!=EnumDifficulty.PEACEFUL&&worldObj.getBlock(x,y-1,z).isNormalCube()&&worldObj.getBlock(x,y-1,z)!=com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.netherWartBlock&&worldObj.checkNoEntityCollision(boundingBox)&&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()&&!worldObj.isAnyLiquid(boundingBox);}
    @Override public int getMaxSpawnedInChunk(){return 4;}
    @Override public void onLivingUpdate(){
        if(attackTicks>0)attackTicks--;super.onLivingUpdate();if(worldObj.isRemote)return;
        if(pacified>0)pacified--;if(fleeTicks>0&&--fleeTicks==0)fleeing=null;
        if(retaliation>0&&--retaliation==0)setAttackTarget(null);
        scanRepellents();tickConversion();
    }
    public void tickConversion(){
        if(worldObj.isRemote||isDead)return;if(immuneToZombification||worldObj.provider.dimensionId==-1){conversion=0;return;}
        if(++conversion<=300||conversion!=301&&conversion%20!=0)return;
        EntityZoglin116 result=new EntityZoglin116(worldObj);result.copyLocationAndAnglesFrom(this);result.setChild(isChild());if(hasCustomNameTag())result.setCustomNameTag(getCustomNameTag());if(isNoDespawnRequired())result.func_110163_bv();
        result.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.confusion.id,200));
        if(worldObj.spawnEntityInWorld(result)){setDead();playSound("htgt6:entity.hoglin.converted_to_zombified",1,1);}
    }
    @Override public int attackAnimationTicks(){return attackTicks;}
    @Override public boolean attackEntityAsMob(Entity target){if(worldObj.isRemote||!(target instanceof EntityLivingBase))return false;attackTicks=10;worldObj.setEntityState(this,(byte)4);playSound("htgt6:entity.hoglin.attack",1,1);boolean hit=HoglinCombat116.attack(this,target);
        if(hit&&!isChild()&&target instanceof EntityPiglin116&&com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherHerd116.piglinsOutnumber(this)){
            retreatFrom((EntityLivingBase)target);for(EntityHoglin116 ally:com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherHerd116.hoglins(this))ally.retreatFrom((EntityLivingBase)target);
        }return hit;}
    @Override public void handleHealthUpdate(byte state){if(state==4)attackTicks=10;else super.handleHealthUpdate(state);}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){
        boolean hit=super.attackEntityFrom(source,amount);if(!hit||worldObj.isRemote||!(source.getEntity() instanceof EntityLivingBase)||source.getEntity() instanceof EntityHoglin116)return hit;
        EntityLivingBase enemy=(EntityLivingBase)source.getEntity();if(enemy instanceof EntityPlayer&&((EntityPlayer)enemy).capabilities.isCreativeMode)return hit;
        resetInLove();if(isChild()){fleeing=enemy;fleeTicks=100+rand.nextInt(301);setAttackTarget(null);}else{
            retaliate(enemy);for(Object o:worldObj.getEntitiesWithinAABB(EntityHoglin116.class,boundingBox.expand(16,8,16)))if(o!=this)((EntityHoglin116)o).retaliate(enemy);
        }return hit;
    }
    public void retreatFrom(EntityLivingBase enemy){fleeing=enemy;fleeTicks=100+rand.nextInt(301);retaliation=0;setAttackTarget(null);getNavigator().clearPathEntity();}
    public void retaliate(EntityLivingBase target){if(isChild()||pacified>0||fleeTicks>0)return;setAttackTarget(target);retaliation=200;}
    public static boolean repellent(net.minecraft.block.Block b){return b==NetherVegetationContent116.warpedFungus||b==NetherVegetationContent116.POTS[1]||b==Blocks.portal||b==NetherUtilityContent116.anchor;}
    private void scanRepellents(){
        if(repellent&&(!worldObj.blockExists(repelX,repelY,repelZ)||!repellent(worldObj.getBlock(repelX,repelY,repelZ))||getDistanceSq(repelX+.5,repelY+.5,repelZ+.5)>64))repellent=false;
        if(repellent){pacified=200;setAttackTarget(null);return;}
        if(scanIndex>=2601){if((ticksExisted+getEntityId())%60!=0)return;scanIndex=0;scanX=MathHelper.floor_double(posX);scanY=MathHelper.floor_double(posY);scanZ=MathHelper.floor_double(posZ);}
        // At most 96 loaded-block reads per tick, staggered; never force-load chunks.
        for(int budget=0;budget<96&&scanIndex<2601;budget++,scanIndex++){
            int x=scanX+scanIndex%17-8,z=scanZ+scanIndex/17%17-8,y=scanY+scanIndex/289-4;
            if(worldObj.blockExists(x,y,z)&&getDistanceSq(x+.5,y+.5,z+.5)<=64&&repellent(worldObj.getBlock(x,y,z))){repellent=true;repelX=x;repelY=y;repelZ=z;pacified=200;setAttackTarget(null);scanIndex=2601;return;}
        }
    }
    private final class AvoidRepellent extends EntityAIBase {
        int pathDelay;AvoidRepellent(){setMutexBits(3);}
        public boolean shouldExecute(){return repellent||fleeing!=null&&fleeTicks>0;}
        public boolean continueExecuting(){return shouldExecute();}
        public void updateTask(){if(--pathDelay>0)return;pathDelay=20;Vec3 danger=fleeing!=null?Vec3.createVectorHelper(fleeing.posX,fleeing.posY,fleeing.posZ):Vec3.createVectorHelper(repelX+.5,repelY+.5,repelZ+.5);Vec3 destination=RandomPositionGenerator.findRandomTargetBlockAwayFrom(EntityHoglin116.this,8,4,danger);if(destination!=null)getNavigator().tryMoveToXYZ(destination.xCoord,destination.yCoord,destination.zCoord,fleeing!=null?1.3:1);}
        public void resetTask(){getNavigator().clearPathEntity();}
    }
    private final class SelectTarget extends EntityAIBase {
        EntityLivingBase next;SelectTarget(){setMutexBits(1);}
        public boolean shouldExecute(){
            if(pacified>0||fleeTicks>0||isInLove()||worldObj.difficultySetting==EnumDifficulty.PEACEFUL){setAttackTarget(null);return false;}
            if(ticksExisted%10!=0)return false;EntityLivingBase current=getAttackTarget();if(current!=null&&current.isEntityAlive()&&getDistanceSqToEntity(current)<256&&(!(current instanceof EntityPlayer)||!((EntityPlayer)current).capabilities.isCreativeMode))return false;
            next=null;double distance=256;for(Object o:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)o;double d=getDistanceSqToEntity(p);if(p.isEntityAlive()&&!p.capabilities.isCreativeMode&&d<distance&&getEntitySenses().canSee(p)){next=p;distance=d;}}
            if(next==null)setAttackTarget(null);return next!=null;
        }
        public void startExecuting(){setAttackTarget(next);}
        public boolean continueExecuting(){return false;}
    }
    @Override protected void dropFewItems(boolean recentlyHit,int looting){if(isChild())return;dropItem(isBurning()?Items.cooked_porkchop:Items.porkchop,2+rand.nextInt(3)+Math.round(rand.nextFloat()*looting));int leather=rand.nextInt(2)+Math.round(rand.nextFloat()*looting);if(leather>0)dropItem(Items.leather,leather);}
    @Override protected String getLivingSound(){return "htgt6:entity.hoglin."+(pacified>0||fleeTicks>0?"retreat":getAttackTarget()!=null?"angry":"ambient");}
    @Override protected String getHurtSound(){return "htgt6:entity.hoglin.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.hoglin.death";}
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block b){playSound("htgt6:entity.hoglin.step",.15F,1);}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setBoolean("IsImmuneToZombification",immuneToZombification);n.setBoolean("CannotBeHunted",cannotBeHunted);n.setInteger("TimeInOverworld",conversion);n.setInteger("Pacified",pacified);}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);immuneToZombification=n.getBoolean("IsImmuneToZombification");cannotBeHunted=n.getBoolean("CannotBeHunted");conversion=MathHelper.clamp_int(n.getInteger("TimeInOverworld"),0,300);pacified=MathHelper.clamp_int(n.getInteger("Pacified"),0,200);}
}
