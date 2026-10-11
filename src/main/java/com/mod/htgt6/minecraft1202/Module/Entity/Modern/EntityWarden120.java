package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.List;

/** Native 1.7.10 implementation of the 1.20.2 Warden state machine. */
public final class EntityWarden120 extends EntityMob {
    public static final double MAX_HEALTH=500D,MOVEMENT_SPEED=.3D,KNOCKBACK_RESISTANCE=1D;
    public static final double ATTACK_DAMAGE=30D,ATTACK_KNOCKBACK=1.5D;
    public static final int IDLE=0,EMERGING=1,ROARING=2,SNIFFING=3,MELEE=4,SONIC=5,DIGGING=6;
    public static final int EMERGE_TICKS=135,DIG_TICKS=100,SONIC_DURATION=60,SONIC_HIT_TICK=34;
    private int vibrationCooldown,recentProjectileTicks,attackCooldown;
    private int state,stateTicks,anger,quietTicks,sonicCooldown;
    private final com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.WardenAnger120 suspects=new com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.WardenAnger120();
    private double disturbanceX,disturbanceY,disturbanceZ;

    public EntityWarden120(World world){super(world);setSize(.9F,2.9F);stepHeight=1F;experienceValue=5;isImmuneToFire=true;}
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Byte.valueOf((byte)IDLE));dataWatcher.addObject(21,Integer.valueOf(0));dataWatcher.addObject(22,Integer.valueOf(0));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(MOVEMENT_SPEED);
        getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(KNOCKBACK_RESISTANCE);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32D);}
    @Override protected boolean isAIEnabled(){return true;}
    @Override protected boolean canDespawn(){return false;}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public int getTotalArmorValue(){return 0;}
    public int getWardenState(){return worldObj.isRemote?dataWatcher.getWatchableObjectByte(20):state;}
    public int getWardenStateTicks(){return worldObj.isRemote?dataWatcher.getWatchableObjectInt(22):stateTicks;}
    public int getAnger(){return worldObj.isRemote?dataWatcher.getWatchableObjectInt(21):anger;}
    public float getHeartIntensity(){return Math.min(1F,getAnger()/150F);}
    public void beginEmerging(){setState(EMERGING,EMERGE_TICKS);noClip=false;playSound("htgt6:entity.warden.emerge",5F,1F);}

    public void hearVibration(Entity source,double x,double y,double z,int frequency){
        if(worldObj.isRemote||getWardenState()==EMERGING||getWardenState()==DIGGING||vibrationCooldown>0)return;
        if(source instanceof EntityLivingBase&&!canTarget((EntityLivingBase)source))return;
        vibrationCooldown=40;
        boolean projectile=source instanceof net.minecraft.entity.projectile.EntityArrow||source instanceof net.minecraft.entity.projectile.EntityThrowable;
        if(source instanceof net.minecraft.entity.projectile.EntityArrow)source=((net.minecraft.entity.projectile.EntityArrow)source).shootingEntity;
        else if(source instanceof net.minecraft.entity.projectile.EntityThrowable)source=((net.minecraft.entity.projectile.EntityThrowable)source).getThrower();
        disturbanceX=x;disturbanceY=y;disturbanceZ=z;quietTicks=0;
        if(source instanceof EntityLivingBase&&source!=this&&canTarget((EntityLivingBase)source)){
            if(!projectile)increaseAnger((EntityLivingBase)source,35);
            else if(getDistanceSqToEntity(source)<=900){
                if(recentProjectileTicks>0){disturbanceX=source.posX;disturbanceY=source.posY;disturbanceZ=source.posZ;}
                increaseAnger((EntityLivingBase)source,recentProjectileTicks>0?35:10);
            }
        }
        if(projectile&&source!=null)recentProjectileTicks=100;
        // Anonymous vibrations provide a location, not anger towards an unrelated target.
        dataWatcher.updateObject(21,Integer.valueOf(anger));playSound("htgt6:entity.warden.listening",2F,.9F+rand.nextFloat()*.2F);
    }
    private boolean canTarget(EntityLivingBase entity){return entity.isEntityAlive()&&entity!=this&&!(entity instanceof EntityWarden120)&&!(entity instanceof EntityPlayer&&((EntityPlayer)entity).capabilities.disableDamage);}
    private void increaseAnger(EntityLivingBase target,int amount){
        if(!canTarget(target))return;
        suspects.increase(target.getUniqueID(),amount);refreshAnger();
    }
    private void refreshAnger(){
        anger=suspects.highest();dataWatcher.updateObject(21,Integer.valueOf(anger));
        EntityLivingBase selected=null;
        for(Object raw:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(80,80,80))){
            EntityLivingBase candidate=(EntityLivingBase)raw;
            if(!canTarget(candidate)||suspects.get(candidate.getUniqueID())<80)continue;
            if(selected==null||(candidate instanceof EntityPlayer&&!(selected instanceof EntityPlayer))
                    ||(candidate instanceof EntityPlayer)==(selected instanceof EntityPlayer)
                    &&suspects.get(candidate.getUniqueID())>suspects.get(selected.getUniqueID()))selected=candidate;
        }
        EntityLivingBase previous=getAttackTarget();
        setAttackTarget(selected);
        if(selected!=null&&selected!=previous&&state==IDLE)setState(ROARING,84);
    }
    private void sniff(){
        EntityLivingBase nearest=null;double distance=Double.MAX_VALUE;
        for(Object raw:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(6,20,6))){
            EntityLivingBase candidate=(EntityLivingBase)raw;
            double dx=candidate.posX-posX,dz=candidate.posZ-posZ;
            if(canTarget(candidate)&&dx*dx+dz*dz<=36&&Math.abs(candidate.posY-posY)<=20
                    &&getDistanceSqToEntity(candidate)<distance){nearest=candidate;distance=getDistanceSqToEntity(candidate);}
        }
        if(nearest!=null){disturbanceX=nearest.posX;disturbanceY=nearest.posY;disturbanceZ=nearest.posZ;
            increaseAnger(nearest,35);getNavigator().tryMoveToXYZ(disturbanceX,disturbanceY,disturbanceZ,.7D);}
    }

    @Override public boolean attackEntityFrom(DamageSource source,float amount){
        if((state==EMERGING||state==DIGGING)&&source!=DamageSource.outOfWorld)return false;
        boolean hit=super.attackEntityFrom(source,amount);
        if(!worldObj.isRemote&&hit&&source.getEntity() instanceof EntityLivingBase&&canTarget((EntityLivingBase)source.getEntity())){
            EntityLivingBase attacker=(EntityLivingBase)source.getEntity();increaseAnger(attacker,100);quietTicks=0;
            if(!source.isProjectile()||getDistanceSqToEntity(attacker)<=25){setAttackTarget(attacker);sonicCooldown=200;setState(IDLE,0);}
        }
        return hit;
    }
    @Override public void setAttackTarget(EntityLivingBase target){
        if(target!=null&&target!=getAttackTarget())sonicCooldown=200;
        super.setAttackTarget(target);
    }
    @Override public boolean attackEntityAsMob(Entity target){
        attackCooldown=18;sonicCooldown=40;setState(MELEE,18);
        playSound("htgt6:entity.warden.attack_impact",3F,1F);
        boolean hit=super.attackEntityAsMob(target);
        if(hit){float yaw=(float)(Math.atan2(-(target.posX-posX),target.posZ-posZ)*180/Math.PI);
            com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatAttack120.knockback(target,ATTACK_KNOCKBACK*.5,yaw);}
        return hit;
    }

    @Override public void onLivingUpdate(){super.onLivingUpdate();if(worldObj.isRemote)return;
        if(worldObj.difficultySetting==EnumDifficulty.PEACEFUL){setDead();return;}
        if(ticksExisted%20==0){suspects.decay();refreshAnger();}
        if(vibrationCooldown>0)--vibrationCooldown;if(recentProjectileTicks>0)--recentProjectileTicks;if(attackCooldown>0)--attackCooldown;if(sonicCooldown>0)--sonicCooldown;if(stateTicks>0){--stateTicks;dataWatcher.updateObject(22,Integer.valueOf(stateTicks));if(stateTicks==0)finishState();}
        if(state==EMERGING||state==DIGGING){motionX=motionZ=0;getNavigator().clearPathEntity();return;}
        if(ticksExisted%120==0)applyDarkness();EntityLivingBase target=getAttackTarget();
        if(target!=null&&!canTarget(target)){setAttackTarget(null);target=null;}
        if(target==null){++quietTicks;
            if(quietTicks>1200)setState(DIGGING,DIG_TICKS);
            else if(state==IDLE&&quietTicks%100==0){setState(SNIFFING,84);getNavigator().tryMoveToXYZ(disturbanceX,disturbanceY,disturbanceZ,.7D);}return;}
        quietTicks=0;getLookHelper().setLookPositionWithEntity(target,30F,30F);
        if(boundingBox.expand(Math.sqrt(2.04F)-.6D,0,Math.sqrt(2.04F)-.6D).intersectsWith(target.boundingBox)&&state==IDLE&&attackCooldown==0&&canEntityBeSeen(target))attackEntityAsMob(target);
        else if(withinSonicRange(target)&&sonicCooldown==0&&state==IDLE){setState(SONIC,SONIC_DURATION);}
        else if(state==IDLE)getNavigator().tryMoveToEntityLiving(target,1.2D);
        if(state==SONIC&&stateTicks==SONIC_DURATION-SONIC_HIT_TICK)sonicBoom(target);
    }
    private boolean withinSonicRange(EntityLivingBase target){double dx=target.posX-posX,dz=target.posZ-posZ;return dx*dx+dz*dz<=225&&Math.abs(target.posY-posY)<=20;}
    private void sonicBoom(EntityLivingBase target){
        if(target==null||!canTarget(target)||!withinSonicRange(target))return;
        DamageSource sonic=new net.minecraft.util.EntityDamageSource("sonic_boom",this).setDamageBypassesArmor().setMagicDamage().setDifficultyScaled();
        target.attackEntityFrom(sonic,10F);
        double dx=target.posX-posX,dy=target.posY+target.getEyeHeight()-(posY+1.6),dz=target.posZ-posZ;
        double length=Math.sqrt(dx*dx+dy*dy+dz*dz);
        double resistance=Math.max(0,1-target.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).getAttributeValue());
        if(length>1E-6&&resistance>0){target.addVelocity(dx/length*2.5*resistance,dy/length*.5*resistance,dz/length*2.5*resistance);target.velocityChanged=true;}
        worldObj.playSoundEffect(posX,posY,posZ,"htgt6:entity.warden.sonic_boom",3F,1F);
    }
    @SuppressWarnings("unchecked") private void applyDarkness(){List<EntityPlayer> players=worldObj.getEntitiesWithinAABB(EntityPlayer.class,
            AxisAlignedBB.getBoundingBox(posX-20,posY-20,posZ-20,posX+20,posY+20,posZ+20));
        for(EntityPlayer player:players)if(!player.capabilities.disableDamage&&getDistanceSqToEntity(player)<400)player.addPotionEffect(new PotionEffect(Backport120Content.darkness.id,260,0));}
    private void setState(int next,int ticks){
        if(state!=next){
            if(next==ROARING&&getAttackTarget()!=null)suspects.increase(getAttackTarget().getUniqueID(),20);
            if(next==ROARING)playSound("htgt6:entity.warden.roar",5F,.9F+rand.nextFloat()*.15F);
            else if(next==SNIFFING)playSound("htgt6:entity.warden.listening",2F,.8F);
        }
        if(next==ROARING||next==SONIC){getNavigator().clearPathEntity();motionX=motionZ=0;}
        state=next;stateTicks=ticks;dataWatcher.updateObject(20,Byte.valueOf((byte)next));dataWatcher.updateObject(22,Integer.valueOf(ticks));
    }
    private void finishState(){if(state==DIGGING){setDead();return;}if(state==SONIC)sonicCooldown=40;if(state==ROARING)sonicCooldown=200;boolean sniffed=state==SNIFFING;setState(IDLE,0);if(sniffed)sniff();}
    @Override protected String getLivingSound(){return "htgt6:entity.warden.ambient";}
    @Override protected String getHurtSound(){return "htgt6:entity.warden.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.warden.death";}
    @Override protected float getSoundVolume(){return 3F;}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);suspects.write(n);n.setByte("WardenState",(byte)state);n.setShort("StateTicks",(short)stateTicks);n.setShort("Anger",(short)anger);n.setInteger("QuietTicks",quietTicks);n.setShort("SonicCooldown",(short)sonicCooldown);n.setShort("AttackCooldown",(short)attackCooldown);n.setShort("VibrationCooldown",(short)vibrationCooldown);n.setShort("RecentProjectile",(short)recentProjectileTicks);n.setDouble("DisturbanceX",disturbanceX);n.setDouble("DisturbanceY",disturbanceY);n.setDouble("DisturbanceZ",disturbanceZ);}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);suspects.read(n);state=n.getByte("WardenState");stateTicks=n.getShort("StateTicks");anger=suspects.highest();quietTicks=Math.max(0,n.getInteger("QuietTicks"));sonicCooldown=Math.max(0,n.getShort("SonicCooldown"));attackCooldown=Math.max(0,n.getShort("AttackCooldown"));vibrationCooldown=Math.max(0,n.getShort("VibrationCooldown"));recentProjectileTicks=Math.max(0,n.getShort("RecentProjectile"));disturbanceX=n.getDouble("DisturbanceX");disturbanceY=n.getDouble("DisturbanceY");disturbanceZ=n.getDouble("DisturbanceZ");dataWatcher.updateObject(20,Byte.valueOf((byte)state));dataWatcher.updateObject(21,Integer.valueOf(anger));dataWatcher.updateObject(22,Integer.valueOf(stateTicks));}
}
