package com.mod.htgt6.minecraft1202.Module.Entity.Modern;
import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import java.util.UUID;
/** Modern neutral behavior; deliberately does not inherit 1.7's nearest-player anger AI. */
public final class EntityZombifiedPiglin116 extends net.minecraft.entity.monster.EntityZombie {
    private static final UUID SPEED=UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718");
    private int angerTime,alertDelay,soundDelay=-1;private UUID angryAt;
    public EntityZombifiedPiglin116(World world){super(world);isImmuneToFire=true;tasks.taskEntries.clear();targetTasks.taskEntries.clear();
        tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(2,new EntityAIAttackOnCollide(this,1,false));tasks.addTask(7,new EntityAIWander(this,1));tasks.addTask(8,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(8,new EntityAILookIdle(this));targetTasks.addTask(2,new SelectAngryPlayer());
    }
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(field_110186_bp).setBaseValue(0);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.23);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(5);}
    @Override public void setFire(int seconds){} // No sunlight ignition, including its visual fire timer.
    @Override public boolean interact(EntityPlayer p){return false;}
    public int getAngerTime(){return angerTime;}
    public UUID getAngryAt(){return angryAt;}
    public boolean isAngryAt(EntityLivingBase target){return angerTime>0&&target!=null&&target.isEntityAlive()&&(!(target instanceof EntityPlayer)||!((EntityPlayer)target).capabilities.isCreativeMode)&&(target.getUniqueID().equals(angryAt)||target instanceof EntityPlayer&&worldObj.getGameRules().getGameRuleBooleanValue("universalAnger"));}
    public void angerAt(EntityLivingBase enemy,boolean broadcast){
        if(worldObj.isRemote||enemy==null||enemy==this||enemy instanceof EntityZombifiedPiglin116||enemy instanceof EntityPlayer&&((EntityPlayer)enemy).capabilities.isCreativeMode)return;
        angerTime=400+rand.nextInt(381);angryAt=enemy instanceof EntityPlayer&&worldObj.getGameRules().getGameRuleBooleanValue("universalAnger")?null:enemy.getUniqueID();setAttackTarget(enemy);alertDelay=80+rand.nextInt(41);soundDelay=rand.nextInt(21);
        if(broadcast)alert(enemy);
    }
    private void alert(EntityLivingBase enemy){double range=getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue();for(Object o:worldObj.getEntitiesWithinAABB(EntityZombifiedPiglin116.class,boundingBox.expand(range,10,range))){EntityZombifiedPiglin116 ally=(EntityZombifiedPiglin116)o;if(ally!=this&&ally.getAttackTarget()==null&&!ally.isOnSameTeam(enemy))ally.angerAt(enemy,false);}}
    public void clearAnger(){angerTime=0;angryAt=null;setAttackTarget(null);setRevengeTarget(null);getNavigator().clearPathEntity();soundDelay=-1;}
    public void tickAnger(){
        if(worldObj.isRemote)return;EntityLivingBase target=getAttackTarget(),remembered=null;
        if(angryAt!=null)for(Object o:worldObj.loadedEntityList)if(o instanceof EntityLivingBase&&angryAt.equals(((Entity)o).getUniqueID())){remembered=(EntityLivingBase)o;break;}
        boolean forgive=!worldObj.getGameRules().hasRule("forgiveDeadPlayers")||worldObj.getGameRules().getGameRuleBooleanValue("forgiveDeadPlayers");
        if(remembered!=null&&!remembered.isEntityAlive()&&(!(remembered instanceof EntityPlayer)||forgive)){clearAnger();target=null;}
        if(target!=null&&(!target.isEntityAlive()||target instanceof EntityPlayer&&((EntityPlayer)target).capabilities.isCreativeMode)){if(!(target instanceof EntityPlayer)||forgive)clearAnger();else setAttackTarget(null);target=null;}
        if(angerTime>0&&!(target instanceof EntityPlayer)&&--angerTime==0)clearAnger();
        IAttributeInstance speed=getEntityAttribute(SharedMonsterAttributes.movementSpeed);AttributeModifier old=speed.getModifier(SPEED);if(angerTime>0&&!isChild()){if(old==null)speed.applyModifier(new AttributeModifier(SPEED,"Attacking speed boost",.05,0).setSaved(false));}else if(old!=null)speed.removeModifier(old);
        if(angerTime>0){recentlyHit=100;if(soundDelay>=0&&soundDelay--==0)playSound("htgt6:entity.zombified_piglin.angry",2,1.8F);}
        if(target!=null&&--alertDelay<=0){if(getEntitySenses().canSee(target))alert(target);alertDelay=80+rand.nextInt(41);}
    }
    public EntityPlayer selectAngryPlayer(){EntityPlayer next=null;double distance=Math.pow(getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue(),2);for(Object o:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)o;double d=getDistanceSqToEntity(p);if(d<distance&&isAngryAt(p)&&getEntitySenses().canSee(p)){next=p;distance=d;}}return next;}
    private final class SelectAngryPlayer extends EntityAIBase {
        EntityPlayer next;public boolean shouldExecute(){if(ticksExisted%10!=0||getAttackTarget()!=null&&getAttackTarget().isEntityAlive())return false;next=selectAngryPlayer();return next!=null;}public void startExecuting(){setAttackTarget(next);}public boolean continueExecuting(){return false;}
    }
    @Override public void onLivingUpdate(){tickAnger();super.onLivingUpdate();}
    @Override public boolean attackEntityAsMob(Entity target){
        ItemStack held=getHeldItem();float modern=com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.AttackCooldown112.getVanillaAttackDamage(held);
        if(Float.isNaN(modern)||held.hasTagCompound()&&held.getTagCompound().hasKey("AttributeModifiers"))return super.attackEntityAsMob(target);
        java.util.UUID weapon=java.util.UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
        net.minecraft.entity.ai.attributes.IAttributeInstance attribute=getEntityAttribute(SharedMonsterAttributes.attackDamage);
        net.minecraft.entity.ai.attributes.AttributeModifier before=attribute.getModifier(weapon);
        if(before!=null&&before.getOperation()!=0)return super.attackEntityAsMob(target);
        if(before!=null)attribute.removeModifier(before);
        net.minecraft.entity.ai.attributes.AttributeModifier adjusted=new net.minecraft.entity.ai.attributes.AttributeModifier(weapon,"Modern piglin weapon",modern-1,0);
        attribute.applyModifier(adjusted);
        try{return super.attackEntityAsMob(target);}finally{attribute.removeModifier(adjusted);if(before!=null)attribute.applyModifier(before);}
    }
    @Override public boolean attackEntityFrom(DamageSource source,float damage){boolean hit=super.attackEntityFrom(source,damage);if(hit&&source.getEntity() instanceof EntityLivingBase)angerAt((EntityLivingBase)source.getEntity(),true);return hit;}
    @Override public boolean getCanSpawnHere(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);return worldObj.difficultySetting!=EnumDifficulty.PEACEFUL&&worldObj.getBlock(x,y-1,z)!=com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.netherWartBlock&&worldObj.checkNoEntityCollision(boundingBox)&&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()&&!worldObj.isAnyLiquid(boundingBox);}
    @Override protected void addRandomArmor(){setCurrentItemOrArmor(0,new ItemStack(Items.golden_sword));}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){IEntityLivingData result=super.onSpawnWithEgg(data);setVillager(false);getEntityAttribute(field_110186_bp).setBaseValue(0);return result;}
    @Override protected void dropFewItems(boolean hit,int looting){int flesh=rand.nextInt(2)+Math.round(rand.nextFloat()*looting),nuggets=rand.nextInt(2)+Math.round(rand.nextFloat()*looting);if(flesh>0)dropItem(Items.rotten_flesh,flesh);if(nuggets>0)dropItem(Items.gold_nugget,nuggets);}
    @Override protected void dropRareDrop(int looting){dropItem(Items.gold_ingot,1);}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setInteger("AngerTime",angerTime);n.removeTag("AngryAt");if(angryAt!=null)n.setString("AngryAt",angryAt.toString());}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);setVillager(false);angerTime=MathHelper.clamp_int(n.hasKey("AngerTime")?n.getInteger("AngerTime"):n.getShort("Anger"),0,780);try{angryAt=UUID.fromString(n.getString("AngryAt"));}catch(IllegalArgumentException e){angryAt=null;}}
    @Override protected String getLivingSound(){return "htgt6:entity.zombified_piglin."+(angerTime>0?"angry":"ambient");}
    @Override protected String getHurtSound(){return "htgt6:entity.zombified_piglin.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.zombified_piglin.death";}
}
