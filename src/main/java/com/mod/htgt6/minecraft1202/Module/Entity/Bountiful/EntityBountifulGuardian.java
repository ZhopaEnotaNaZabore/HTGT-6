package com.mod.htgt6.minecraft1202.Module.Entity.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.OceanMonumentData120;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityAxolotl120;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import java.util.List;

/** Guardian/Elder Guardian AI and animation contract ported from Minecraft 1.12. */
public final class EntityBountifulGuardian extends EntityMob {
    private static final int ELDER=20,BEAM_TARGET=21,MOVING=22;
    private float tailPrevious,tailCurrent,tailSpeed,spikesPrevious,spikesCurrent;
    private int clientBeamTicks;
    private int lastClientBeamTargetId;
    private EntityLivingBase clientBeamTarget;
    private boolean clientBeamSoundPending;
    private final GuardianRandomSwimAI randomSwimAI;

    public EntityBountifulGuardian(World world){super(world);setSize(.85F,.85F);experienceValue=10;com.mod.htgt6.common.util.EntityMoveHelperAccess112.install(this,new GuardianMoveHelper112(this));randomSwimAI=new GuardianRandomSwimAI(this);
        tasks.addTask(4,new GuardianBeamAI(this));tasks.addTask(7,randomSwimAI);tasks.addTask(8,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(8,new EntityAIWatchClosest(this,EntityBountifulGuardian.class,12,.01F));tasks.addTask(9,new EntityAILookIdle(this));
        targetTasks.addTask(1,new EntityAINearestAttackableTarget(this,EntityLivingBase.class,10,true,false,new IEntitySelector(){public boolean isEntityApplicable(Entity e){return(e instanceof EntityPlayer||e instanceof EntitySquid||e instanceof EntityAxolotl120)&&e.getDistanceSqToEntity(EntityBountifulGuardian.this)>9;}}));
        tailPrevious=tailCurrent=rand.nextFloat();
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(ELDER,Byte.valueOf((byte)0));dataWatcher.addObject(BEAM_TARGET,Integer.valueOf(0));dataWatcher.addObject(MOVING,Byte.valueOf((byte)0));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.5);getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6);getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16);}
    public boolean isElder(){return dataWatcher.getWatchableObjectByte(ELDER)!=0;}
    public void setElder(boolean elder){boolean changed=elder!=isElder();applyElderState(elder);if(elder&&changed)setHealth(getMaxHealth());}
    private void applyElderState(boolean elder){
        dataWatcher.updateObject(ELDER,Byte.valueOf((byte)(elder?1:0)));
        setSize(elder?1.9975F:.85F,elder?1.9975F:.85F);
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(elder?80:30);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(elder?8:6);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(elder?.3:.5);
        // Elder guardians inherit the guardian XP value in Java Edition.
        experienceValue=10;
    }
    private void setMoving(boolean moving){dataWatcher.updateObject(MOVING,Byte.valueOf((byte)(moving?1:0)));}
    public boolean isMoving(){return dataWatcher.getWatchableObjectByte(MOVING)!=0;}
    private void setBeamTarget(EntityLivingBase target){dataWatcher.updateObject(BEAM_TARGET,Integer.valueOf(target==null?0:target.getEntityId()));}
    public boolean hasBeamTarget(){return dataWatcher.getWatchableObjectInt(BEAM_TARGET)!=0;}
    public EntityLivingBase getBeamTarget(){
        if(!worldObj.isRemote)return getAttackTarget();
        int id=dataWatcher.getWatchableObjectInt(BEAM_TARGET);
        if(id==0){clientBeamTarget=null;return null;}
        if(clientBeamTarget==null||clientBeamTarget.getEntityId()!=id){Entity e=worldObj.getEntityByID(id);clientBeamTarget=e instanceof EntityLivingBase?(EntityLivingBase)e:null;}
        return clientBeamTarget;
    }
    public float getBeamProgress(float partial){return MathHelper.clamp_float((clientBeamTicks+partial)/(float)getAttackDuration(),0,1);}
    public boolean consumeBeamSoundRequest(){boolean pending=clientBeamSoundPending;clientBeamSoundPending=false;return pending;}
    public int getAttackDuration(){return isElder()?60:80;}
    public float getTailAnimation(float partial){return tailPrevious+(tailCurrent-tailPrevious)*partial;}
    public float getSpikesAnimation(float partial){return spikesPrevious+(spikesCurrent-spikesPrevious)*partial;}

    @Override public void onLivingUpdate(){
        if(worldObj.isRemote)updateClientAnimations();
        else if(isElder()&&(ticksExisted+getEntityId())%1200==0){
            List players=worldObj.getEntitiesWithinAABB(EntityPlayer.class,boundingBox.expand(50,50,50));
            for(Object value:players){EntityPlayer player=(EntityPlayer)value;
                PotionEffect current=player.getActivePotionEffect(Potion.digSlowdown);
                if(!player.capabilities.isCreativeMode&&getDistanceSqToEntity(player)<2500
                        &&(current==null||current.getAmplifier()<2||current.getDuration()<1200))
                    player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id,6000,2));}
        }
        if(isInWater())setAir(300);else if(onGround){motionY+=.5;motionX+=(rand.nextFloat()*2-1)*.4;motionZ+=(rand.nextFloat()*2-1)*.4;rotationYaw=rand.nextFloat()*360;onGround=false;isAirBorne=true;}
        if(getAttackTarget()!=null)rotationYaw=rotationYawHead;
        super.onLivingUpdate();
    }
    private void updateClientAnimations(){
        tailPrevious=tailCurrent;
        tailSpeed=isInWater()?(isMoving()?(tailSpeed<.5F?4F:tailSpeed+(.5F-tailSpeed)*.1F):tailSpeed+(.125F-tailSpeed)*.2F):2F;
        tailCurrent+=tailSpeed;
        spikesPrevious=spikesCurrent;
        spikesCurrent=!isInWater()?rand.nextFloat():(isMoving()?spikesCurrent+(0-spikesCurrent)*.25F:spikesCurrent+(1-spikesCurrent)*.06F);
        int targetId=dataWatcher.getWatchableObjectInt(BEAM_TARGET);
        if(targetId!=lastClientBeamTargetId){clientBeamTicks=0;clientBeamTarget=null;lastClientBeamTargetId=targetId;clientBeamSoundPending=targetId!=0;}
        if(targetId==0){clientBeamTicks=0;clientBeamTarget=null;return;}
        if(clientBeamTicks<getAttackDuration())++clientBeamTicks;
        EntityLivingBase target=getBeamTarget();
        if(target==null)return;
        getLookHelper().setLookPositionWithEntity(target,90,90);
        double progress=getBeamProgress(0),dx=target.posX-posX,dy=target.posY+target.height*.5-(posY+getEyeHeight()),dz=target.posZ-posZ;
        double distance=Math.sqrt(dx*dx+dy*dy+dz*dz);if(distance<.001)return;dx/=distance;dy/=distance;dz/=distance;
        double step=rand.nextDouble();
        while(step<distance){step+=1.8-progress+rand.nextDouble()*(1.7-progress);worldObj.spawnParticle("bubble",posX+dx*step,posY+getEyeHeight()+dy*step,posZ+dz*step,0,0,0);}
    }
    @Override public void moveEntityWithHeading(float strafe,float forward){if(isAIEnabled()&&isInWater()){moveFlying(strafe,forward,.1F);moveEntity(motionX,motionY,motionZ);motionX*=.9;motionY*=.9;motionZ*=.9;if(!isMoving()&&getAttackTarget()==null)motionY-=.005;}else super.moveEntityWithHeading(strafe,forward);}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){if(!isMoving()&&!source.isUnblockable()&&source.getSourceOfDamage() instanceof EntityLivingBase&&source.getSourceOfDamage()==source.getEntity())source.getSourceOfDamage().attackEntityFrom(DamageSource.causeThornsDamage(this),2);return super.attackEntityFrom(source,amount);}
    @Override public boolean attackEntityAsMob(Entity target){return false;}
    @Override protected boolean canDespawn(){return !isElder()&&super.canDespawn();}
    @Override public boolean getCanSpawnHere(){
        int x=MathHelper.floor_double(posX),z=MathHelper.floor_double(posZ);
        return worldObj.difficultySetting!=EnumDifficulty.PEACEFUL&&isInWater()
                &&OceanMonumentData120.get(worldObj).isInsideMonument(x,z)
                &&worldObj.checkNoEntityCollision(boundingBox)
                &&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty();
    }
    @Override protected void dropFewItems(boolean playerKill,int looting){
        looting=Math.max(0,looting);
        int shards=rand.nextInt(3)+lootingBonus(rand,looting);
        if(shards>0)dropItem(BountifulContent.prismarineShard,shards);
        int secondary=rand.nextInt(5);
        if(secondary<2){int count=1+lootingBonus(rand,looting);entityDropItem(new net.minecraft.item.ItemStack(
                isBurning()?net.minecraft.init.Items.cooked_fished:net.minecraft.init.Items.fish,count,0),0.0F);}
        else if(secondary<4){int count=1+lootingBonus(rand,looting);dropItem(BountifulContent.prismarineCrystals,count);}
        if(playerKill&&rand.nextFloat()<0.025F+0.01F*looting){
            int roll=rand.nextInt(100),fish=roll<60?0:roll<85?1:roll<87?2:3;
            entityDropItem(new net.minecraft.item.ItemStack(isBurning()&&fish<2?net.minecraft.init.Items.cooked_fished:net.minecraft.init.Items.fish,1,fish),0);
        }
        if(isElder()&&playerKill)entityDropItem(new net.minecraft.item.ItemStack(BountifulContent.sponge,1,1),0.0F);
    }
    public static int lootingBonus(java.util.Random random,int level){return level>0?Math.round(random.nextFloat()*level):0;}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("Elder",isElder());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){float saved=tag.hasKey("Health",99)?tag.getFloat("Health"):getHealth();super.readEntityFromNBT(tag);applyElderState(tag.getBoolean("Elder"));setHealth(Math.min(saved,getMaxHealth()));}

    private static final class GuardianBeamAI extends EntityAIBase{
        private final EntityBountifulGuardian guardian;private int ticks;
        GuardianBeamAI(EntityBountifulGuardian g){guardian=g;setMutexBits(3);}
        @Override public boolean shouldExecute(){return guardian.getAttackTarget()!=null&&guardian.getAttackTarget().isEntityAlive();}
        @Override public boolean continueExecuting(){return shouldExecute()&&(guardian.isElder()||guardian.getDistanceSqToEntity(guardian.getAttackTarget())>9);}
        @Override public void startExecuting(){ticks=-10;guardian.getNavigator().clearPathEntity();guardian.getLookHelper().setLookPositionWithEntity(guardian.getAttackTarget(),90,90);guardian.isAirBorne=true;}
        @Override public void resetTask(){guardian.setBeamTarget(null);guardian.setAttackTarget(null);guardian.randomSwimAI.requestUpdate();}
        @Override public void updateTask(){EntityLivingBase target=guardian.getAttackTarget();guardian.getNavigator().clearPathEntity();guardian.getLookHelper().setLookPositionWithEntity(target,90,90);if(!guardian.canEntityBeSeen(target)){guardian.setAttackTarget(null);return;}if(++ticks==0){guardian.setBeamTarget(target);guardian.worldObj.setEntityState(guardian,(byte)21);}else if(ticks>=guardian.getAttackDuration()){float magic=1;if(guardian.worldObj.difficultySetting==EnumDifficulty.HARD)magic+=2;if(guardian.isElder())magic+=2;target.attackEntityFrom(DamageSource.causeIndirectMagicDamage(guardian,guardian),magic);target.attackEntityFrom(DamageSource.causeMobDamage(guardian),(float)guardian.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());guardian.setAttackTarget(null);}}
    }
    private static final class GuardianRandomSwimAI extends EntityAIBase{
        private final EntityBountifulGuardian guardian;private boolean forced;GuardianRandomSwimAI(EntityBountifulGuardian g){guardian=g;setMutexBits(3);}
        void requestUpdate(){forced=true;}
        @Override public boolean shouldExecute(){return guardian.getAttackTarget()==null&&!guardian.getMoveHelper().isUpdating()&&(forced||guardian.rand.nextInt(80)==0);}
        @Override public void startExecuting(){forced=false;for(int i=0;i<10;i++){int x=MathHelper.floor_double(guardian.posX)+guardian.rand.nextInt(17)-8,y=MathHelper.floor_double(guardian.posY)+guardian.rand.nextInt(9)-4,z=MathHelper.floor_double(guardian.posZ)+guardian.rand.nextInt(17)-8;if(guardian.worldObj.getBlock(x,y,z).getMaterial()==net.minecraft.block.material.Material.water){guardian.getMoveHelper().setMoveTo(x+.5,y+.5,z+.5,1);break;}}}
    }
    private static final class GuardianMoveHelper112 extends EntityMoveHelper{
        private final EntityBountifulGuardian g;private double x,y,z,speed;private boolean active;
        GuardianMoveHelper112(EntityBountifulGuardian g){super(g);this.g=g;}
        @Override public void setMoveTo(double x,double y,double z,double speed){this.x=x;this.y=y;this.z=z;this.speed=speed;active=true;super.setMoveTo(x,y,z,speed);}
        @Override public boolean isUpdating(){return active;}
        @Override public void onUpdateMoveHelper(){if(!active){g.setAIMoveSpeed(0);g.setMoving(false);return;}double dx=x-g.posX,dy=y-g.posY,dz=z-g.posZ,d=Math.sqrt(dx*dx+dy*dy+dz*dz);if(d<.5){active=false;g.setMoving(false);return;}dy/=d;float wanted=(float)(Math.atan2(dz,dx)*180/Math.PI)-90;g.rotationYaw=g.rotationYawHead=approach(g.rotationYaw,wanted,90);float target=(float)(speed*g.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());g.setAIMoveSpeed(g.getAIMoveSpeed()+(target-g.getAIMoveSpeed())*.125F);double wobble=Math.sin((g.ticksExisted+g.getEntityId())*.5)*.05;g.motionX+=wobble*Math.cos(g.rotationYaw*Math.PI/180);g.motionZ+=wobble*Math.sin(g.rotationYaw*Math.PI/180);g.motionY+=Math.sin((g.ticksExisted+g.getEntityId())*.75)*.05*(Math.sin(g.rotationYaw*Math.PI/180)+Math.cos(g.rotationYaw*Math.PI/180))*.25+g.getAIMoveSpeed()*dy*.1;g.getLookHelper().setLookPosition(g.posX+dx/d*2,g.posY+g.getEyeHeight()+dy/d,g.posZ+dz/d*2,10,40);g.setMoving(true);}
        private static float approach(float current,float wanted,float limit){float delta=MathHelper.wrapAngleTo180_float(wanted-current);return current+MathHelper.clamp_float(delta,-limit,limit);}
    }
}
