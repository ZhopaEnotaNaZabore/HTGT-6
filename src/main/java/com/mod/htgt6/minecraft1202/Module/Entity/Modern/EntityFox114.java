package com.mod.htgt6.minecraft1202.Module.Entity.Modern;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
/** Fox state is server-owned and synchronized through the legacy DataWatcher. */
public final class EntityFox114 extends EntityAnimal {
    private final Set<UUID> trusted=new LinkedHashSet<UUID>();private int eatTicks,sleepDelay=200;
    private float crouch,previousCrouch,interest,previousInterest;
    public EntityFox114(World w){super(w);setSize(.6F,.7F);setCanPickUpLoot(false);
        tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIPanic(this,2.2){public boolean shouldExecute(){return !defending()&&super.shouldExecute();}public boolean continueExecuting(){return !defending()&&super.continueExecuting();}});tasks.addTask(2,new EntityAIMate(this,1));
        tasks.addTask(3,new AvoidThreats());
        tasks.addTask(3,new Faceplant());tasks.addTask(4,new Pounce());tasks.addTask(4,new Stalk());tasks.addTask(9,new Perch());tasks.addTask(5,new EntityAIAttackOnCollide(this,1.2,true));tasks.addTask(6,new Sleep());tasks.addTask(7,new EntityAIFollowParent(this,1.1));tasks.addTask(8,new Forage());tasks.addTask(9,new EntityAIWander(this,1));tasks.addTask(10,new EntityAIWatchClosest(this,EntityPlayer.class,24));
        targetTasks.addTask(1,new DefendTrusted());targetTasks.addTask(3,new EntityAINearestAttackableTarget(this,EntityChicken.class,10,true));
        targetTasks.addTask(3,new EntityAINearestAttackableTarget(this,com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulRabbit.class,10,true));
        targetTasks.addTask(4,new EntityAINearestAttackableTarget(this,com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.class,20,true));
    }
    @Override protected boolean isAIEnabled(){return true;}
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Byte.valueOf((byte)0));dataWatcher.addObject(21,Byte.valueOf((byte)0));}
    public boolean snow(){return dataWatcher.getWatchableObjectByte(20)!=0;}public void setSnow(boolean b){dataWatcher.updateObject(20,Byte.valueOf((byte)(b?1:0)));}
    @Override public String getCommandSenderName(){return hasCustomNameTag()?getCustomNameTag():StatCollector.translateToLocal("entity.HTGT6.Fox.name");}
    public boolean sleeping(){return (dataWatcher.getWatchableObjectByte(21)&1)!=0;}public boolean pouncing(){return (dataWatcher.getWatchableObjectByte(21)&2)!=0;}
    public boolean defending(){return (dataWatcher.getWatchableObjectByte(21)&4)!=0;}
    public boolean sitting(){return (dataWatcher.getWatchableObjectByte(21)&8)!=0;}
    public boolean crouching(){return (dataWatcher.getWatchableObjectByte(21)&16)!=0;}
    public boolean interested(){return (dataWatcher.getWatchableObjectByte(21)&32)!=0;}
    public boolean faceplanted(){return (dataWatcher.getWatchableObjectByte(21)&64)!=0;}
    public float crouchAmount(float partial){return previousCrouch+(crouch-previousCrouch)*partial;}
    public float headRoll(float partial){return (previousInterest+(interest-previousInterest)*partial)*.11F*(float)Math.PI;}
    private void animatePose(){previousCrouch=crouch;previousInterest=interest;crouch=crouching()?Math.min(3,crouch+.2F):0;interest+=(interested()?1-interest:-interest)*.4F;}
    private boolean prey(EntityLivingBase target){return target instanceof EntityChicken||target instanceof com.mod.htgt6.minecraft1202.Module.Entity.Bountiful.EntityBountifulRabbit;}
    private void flag(int mask,boolean on){int b=dataWatcher.getWatchableObjectByte(21);dataWatcher.updateObject(21,Byte.valueOf((byte)(on?b|mask:b&~mask)));}
    public void trust(UUID id){if(id!=null&&trusted.size()<2)trusted.add(id);}public boolean trusts(UUID id){return trusted.contains(id);}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(10);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3);getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2);}
    @Override public boolean attackEntityAsMob(Entity target){boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(this),2);if(hit)playSound("htgt6:entity.fox.bite",1,1);return hit;}
    @Override public boolean isBreedingItem(ItemStack s){return s!=null&&(s.getItem()==Backport120Content.sweetBerries||s.getItem()==Backport120Content.glowBerries);}
    @Override public boolean interact(EntityPlayer player){
        ItemStack held=player.getHeldItem();
        if(isChild()&&held!=null&&held.stackSize>0&&isBreedingItem(held)){
            if(!worldObj.isRemote){setGrowingAge(Math.min(0,getGrowingAge()+Math.max(1,-getGrowingAge()/10)));if(!player.capabilities.isCreativeMode&&--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}
            return true;
        }
        return super.interact(player);
    }
    @Override public boolean getCanSpawnHere(){
        int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);
        if(!worldObj.blockExists(x,y-1,z))return false;
        net.minecraft.block.Block floor=worldObj.getBlock(x,y-1,z);int meta=worldObj.getBlockMetadata(x,y-1,z);
        boolean ground=floor==Blocks.grass||floor==Blocks.snow||floor==Blocks.snow_layer||floor==Blocks.dirt&&(meta==1||meta==2);
        return ground&&worldObj.getFullBlockLightValue(x,y,z)>8&&worldObj.checkNoEntityCollision(boundingBox)&&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()&&!worldObj.isAnyLiquid(boundingBox);
    }
    @Override public EntityFox114 createChild(EntityAgeable mate){EntityFox114 child=new EntityFox114(worldObj);child.setSnow(rand.nextBoolean()?snow():((EntityFox114)mate).snow());EntityPlayer first=func_146083_cb(),second=((EntityFox114)mate).func_146083_cb();if(first!=null)child.trust(first.getUniqueID());if(second!=null)child.trust(second.getUniqueID());return child;}
    private static final class FoxGroup implements IEntityLivingData {final boolean snow;int count;FoxGroup(boolean snow){this.snow=snow;}}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData group){
        super.onSpawnWithEgg(group);
        FoxGroup family=group instanceof FoxGroup?(FoxGroup)group:new FoxGroup(worldObj.getBiomeGenForCoords(MathHelper.floor_double(posX),MathHelper.floor_double(posZ)).getEnableSnow());
        setSnow(family.snow);if(family.count++>=2)setGrowingAge(-24000);
        if(rand.nextFloat()<.2F){setCurrentItemOrArmor(0,new ItemStack(spawnHeldItem(rand)));setEquipmentDropChance(0,2);}
        return family;
    }
    public static Item spawnHeldItem(Random random){
        float roll=random.nextFloat();
        if(roll<.05F)return Items.emerald;
        if(roll<.2F)return Items.egg;
        if(roll<.4F)return random.nextBoolean()?com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.rabbitFoot:com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.rabbitHide;
        if(roll<.6F)return Items.wheat;
        return roll<.8F?Items.leather:Items.feather;
    }
    @Override public void onLivingUpdate(){super.onLivingUpdate();animatePose();if(worldObj.isRemote)return;if(sleeping()&&!canSleepHere())flag(1,false);if(sitting()&&(getAttackTarget()!=null||isInLove()||isInWater()||!onGround||!getNavigator().noPath()||hasAlertPlayer()))flag(8,false);if(getAttackTarget()==null){flag(16,false);flag(32,false);}if(sleepDelay>0)--sleepDelay;
        if(!sleeping()&&getAttackTarget()==null&&worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))for(Object raw:worldObj.getEntitiesWithinAABB(EntityItem.class,boundingBox.expand(1,0,1))){EntityItem e=(EntityItem)raw;ItemStack s=e.getEntityItem(),old=getHeldItem();if(e.isDead||e.delayBeforeCanPickup>0||s==null||s.stackSize<1||old!=null&&(!(s.getItem() instanceof ItemFood)||old.getItem() instanceof ItemFood))continue;
            if(old!=null)entityDropItem(old.copy(),0);ItemStack one=s.splitStack(1);setCurrentItemOrArmor(0,one);setEquipmentDropChance(0,2);if(s.stackSize<=0)e.setDead();eatTicks=0;break;}
        ItemStack held=getHeldItem();if(held!=null&&held.getItem() instanceof ItemFood&&!sleeping()&&getAttackTarget()==null){if(++eatTicks>=600){setCurrentItemOrArmor(0,com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.FoxFood114.finish(this,held));eatTicks=0;playSound("htgt6:entity.fox.eat",1,1);}}else eatTicks=0;
    }
    @Override public boolean attackEntityFrom(DamageSource s,float damage){flag(1,false);flag(8,false);flag(16,false);flag(32,false);flag(64,false);sleepDelay=200;return super.attackEntityFrom(s,damage);}
    @Override protected String getLivingSound(){return "htgt6:entity.fox."+(sleeping()?"sleep":"ambient");}@Override protected String getHurtSound(){return "htgt6:entity.fox.hurt";}@Override protected String getDeathSound(){return "htgt6:entity.fox.death";}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setString("Type",snow()?"snow":"red");n.setBoolean("Sleeping",sleeping());n.setBoolean("Sitting",sitting());NBTTagList list=new NBTTagList();for(UUID id:trusted){NBTTagCompound t=new NBTTagCompound();t.setLong("Most",id.getMostSignificantBits());t.setLong("Least",id.getLeastSignificantBits());list.appendTag(t);}n.setTag("Trusted114",list);}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);setSnow("snow".equals(n.getString("Type")));flag(1,n.getBoolean("Sleeping"));flag(8,n.getBoolean("Sitting"));sleepDelay=sleeping()?0:200;trusted.clear();NBTTagList list=n.getTagList("Trusted114",10);for(int i=0;i<Math.min(2,list.tagCount());i++){NBTTagCompound t=list.getCompoundTagAt(i);trust(new UUID(t.getLong("Most"),t.getLong("Least")));}}
    private boolean canSleepHere(){return worldObj.isDaytime()&&onGround&&!isInWater()&&!isInLove()&&getAttackTarget()==null&&!worldObj.canBlockSeeTheSky(MathHelper.floor_double(posX),MathHelper.floor_double(posY+height),MathHelper.floor_double(posZ))&&!hasAlertPlayer();}
    private final class Sleep extends EntityAIBase {Sleep(){setMutexBits(7);}public boolean shouldExecute(){return (sleeping()||sleepDelay<=0)&&canSleep();}private boolean canSleep(){return canSleepHere();}public boolean continueExecuting(){return canSleep();}public void startExecuting(){getNavigator().clearPathEntity();flag(1,true);}public void resetTask(){flag(1,false);sleepDelay=140+rand.nextInt(140);}}
    private final class Stalk extends EntityAIBase {
        Stalk(){setMutexBits(3);}
        public boolean shouldExecute(){EntityLivingBase t=getAttackTarget();return prey(t)&&t.isEntityAlive()&&!sleeping()&&!faceplanted()&&!pouncing()&&onGround&&getDistanceSqToEntity(t)>4&&(crouch<3||getDistanceSqToEntity(t)>36);}
        public boolean continueExecuting(){return shouldExecute()&&crouch<3;}
        public void startExecuting(){flag(8,false);flag(32,true);}
        public void updateTask(){EntityLivingBase t=getAttackTarget();if(t==null)return;getLookHelper().setLookPositionWithEntity(t,30,30);
            if(getDistanceSqToEntity(t)>36){getNavigator().tryMoveToEntityLiving(t,1.5);flag(16,false);}
            else {getNavigator().clearPathEntity();flag(16,true);}}
        public void resetTask(){if(!prey(getAttackTarget())||!getAttackTarget().isEntityAlive()||getDistanceSqToEntity(getAttackTarget())<=4||!canEntityBeSeen(getAttackTarget())){flag(16,false);flag(32,false);}}
    }
    private final class Pounce extends EntityAIBase {
        int ticks;boolean descending,hit;
        Pounce(){setMutexBits(3);}
        public boolean shouldExecute(){EntityLivingBase t=getAttackTarget();return prey(t)&&t.isEntityAlive()&&onGround&&crouch>=3&&getDistanceSqToEntity(t)>1&&getDistanceSqToEntity(t)<100&&canEntityBeSeen(t);}
        public void startExecuting(){EntityLivingBase t=getAttackTarget();double dx=t.posX-posX,dz=t.posZ-posZ,len=Math.sqrt(dx*dx+dz*dz);
            getNavigator().clearPathEntity();motionX=len<.001?0:dx/len*.8;motionZ=len<.001?0:dz/len*.8;motionY=.9;
            flag(2,true);flag(16,false);flag(32,false);ticks=0;descending=hit=false;}
        public boolean continueExecuting(){return ++ticks<40&&(!onGround||ticks<3);}
        public void updateTask(){descending|=motionY<-.1;rotationPitch=(float)(-Math.atan2(motionY,Math.sqrt(motionX*motionX+motionZ*motionZ))*180/Math.PI);
            EntityLivingBase t=getAttackTarget();if(!hit&&t!=null&&boundingBox.expand(.2,.2,.2).intersectsWith(t.boundingBox)&&canEntityBeSeen(t)){hit=attackEntityAsMob(t);}}
        public void resetTask(){flag(2,false);flag(16,false);flag(32,false);
            int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);
            net.minecraft.block.Block feet=worldObj.getBlock(x,y,z),floor=worldObj.getBlock(x,y-1,z);
            if(!hit&&descending&&onGround&&(feet==Blocks.snow_layer||floor==Blocks.snow||floor==Blocks.snow_layer)){flag(64,true);rotationPitch=60;}
            else rotationPitch=0;}
    }
    private final class Faceplant extends EntityAIBase {
        int remaining;Faceplant(){setMutexBits(7);}
        public boolean shouldExecute(){return faceplanted();}public boolean continueExecuting(){return faceplanted()&&remaining>0;}
        public void startExecuting(){remaining=40;getNavigator().clearPathEntity();}
        public void updateTask(){--remaining;motionX=motionZ=0;}
        public void resetTask(){flag(64,false);rotationPitch=0;}
    }
    private final class Perch extends EntityAIBase {
        int remaining;Perch(){setMutexBits(3);}
        public boolean shouldExecute(){return !sleeping()&&!faceplanted()&&onGround&&!isInWater()&&!isInLove()&&getAttackTarget()==null&&!hasAlertPlayer()&&(sitting()||rand.nextInt(200)==0);}
        public boolean continueExecuting(){return sitting()&&remaining>0&&getAttackTarget()==null&&!hasAlertPlayer()&&!isInWater()&&!isInLove();}
        public void startExecuting(){remaining=80+rand.nextInt(21);flag(8,true);getNavigator().clearPathEntity();}
        public void updateTask(){--remaining;if(remaining%20==0)getLookHelper().setLookPosition(posX+rand.nextDouble()*8-4,posY+getEyeHeight(),posZ+rand.nextDouble()*8-4,30,30);}
        public void resetTask(){flag(8,false);}
    }
    private final class DefendTrusted extends EntityAIBase {
        EntityLivingBase attacker;final Map<UUID,Integer> stamps=new HashMap<UUID,Integer>();
        DefendTrusted(){setMutexBits(1);}
        public boolean shouldExecute(){
            for(Object o:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(16,8,16))){
                EntityLivingBase friend=(EntityLivingBase)o;if(!trusts(friend.getUniqueID()))continue;
                EntityLivingBase a=friend.getAITarget();Integer previous=stamps.get(friend.getUniqueID());
                if(a!=null&&a!=EntityFox114.this&&a.isEntityAlive()&&!trusts(a.getUniqueID())&&
                        (previous==null||friend.func_142015_aE()!=previous)){
                    attacker=a;stamps.put(friend.getUniqueID(),friend.func_142015_aE());return true;
                }
            }return false;
        }
        public void startExecuting(){setAttackTarget(attacker);flag(4,true);flag(1,false);getNavigator().clearPathEntity();}
        public boolean continueExecuting(){return attacker!=null&&attacker.isEntityAlive()&&getAttackTarget()==attacker&&getDistanceSqToEntity(attacker)<1024;}
        public void resetTask(){flag(4,false);if(getAttackTarget()==attacker)setAttackTarget(null);attacker=null;}
    }
    /** Filter before choosing a target: a nearby tame wolf must not hide a further wild wolf. */
    public boolean avoids(EntityLivingBase entity){
        if(defending())return false;
        if(entity instanceof EntityPlayer){EntityPlayer p=(EntityPlayer)entity;return !p.capabilities.disableDamage&&!p.isSneaking()&&!trusts(p.getUniqueID());}
        if(entity instanceof EntityWolf)return !((EntityWolf)entity).isTamed();
        return entity instanceof com.mod.htgt6.minecraft1202.Module.Entity.Frostburn.EntityFrostburnPolarBear;
    }
    private boolean hasAlertPlayer(){for(Object o:worldObj.playerEntities){EntityPlayer p=(EntityPlayer)o;if(p.isEntityAlive()&&getDistanceSqToEntity(p)<144&&avoids(p))return true;}return false;}
    private final class AvoidThreats extends EntityAIBase {
        EntityLivingBase threat;net.minecraft.pathfinding.PathEntity path;
        AvoidThreats(){setMutexBits(1);}
        public boolean shouldExecute(){
            threat=null;double nearest=256;
            for(Object raw:worldObj.getEntitiesWithinAABB(EntityLivingBase.class,boundingBox.expand(16,3,16))){
                EntityLivingBase candidate=(EntityLivingBase)raw;double distance=getDistanceSqToEntity(candidate);
                if(candidate.isEntityAlive()&&avoids(candidate)&&distance<nearest&&
                        (candidate instanceof EntityPlayer||distance<64)&&getEntitySenses().canSee(candidate)){threat=candidate;nearest=distance;}
            }
            if(threat==null)return false;
            Vec3 away=RandomPositionGenerator.findRandomTargetBlockAwayFrom(EntityFox114.this,16,7,Vec3.createVectorHelper(threat.posX,threat.posY,threat.posZ));
            if(away==null||threat.getDistanceSq(away.xCoord,away.yCoord,away.zCoord)<nearest)return false;
            path=getNavigator().getPathToXYZ(away.xCoord,away.yCoord,away.zCoord);
            return path!=null&&path.isDestinationSame(away);
        }
        public void startExecuting(){getNavigator().setPath(path,1.6);}
        public boolean continueExecuting(){return threat!=null&&threat.isEntityAlive()&&avoids(threat)&&!getNavigator().noPath();}
        public void updateTask(){getNavigator().setSpeed(getDistanceSqToEntity(threat)<49?1.4:1.6);}
        public void resetTask(){threat=null;path=null;}
    }
    private final class Forage extends EntityAIBase {int x,y,z,wait;EntityItem item;Forage(){setMutexBits(1);}public boolean shouldExecute(){if(sleeping()||getAttackTarget()!=null||!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")||rand.nextInt(20)!=0)return false;item=null;for(Object o:worldObj.getEntitiesWithinAABB(EntityItem.class,boundingBox.expand(8,4,8))){EntityItem e=(EntityItem)o;if(!e.isDead&&e.delayBeforeCanPickup==0&&(getHeldItem()==null||e.getEntityItem().getItem() instanceof ItemFood&&!(getHeldItem().getItem() instanceof ItemFood))){item=e;return true;}}
        int bx=MathHelper.floor_double(posX),by=MathHelper.floor_double(posY),bz=MathHelper.floor_double(posZ);for(int i=0;i<40;i++){x=bx+rand.nextInt(17)-8;y=by+rand.nextInt(5)-2;z=bz+rand.nextInt(17)-8;if(worldObj.blockExists(x,y,z)&&worldObj.getBlock(x,y,z)==Backport120Content.sweetBerryBush&&worldObj.getBlockMetadata(x,y,z)>=2)return true;}return false;}
        public void startExecuting(){wait=0;if(item!=null)getNavigator().tryMoveToEntityLiving(item,1.2);else getNavigator().tryMoveToXYZ(x+.5,y,z+.5,1.2);}public boolean continueExecuting(){return ++wait<200&&!sleeping()&&getAttackTarget()==null&&worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")&&(item!=null?!item.isDead:worldObj.blockExists(x,y,z)&&worldObj.getBlock(x,y,z)==Backport120Content.sweetBerryBush&&worldObj.getBlockMetadata(x,y,z)>=2);}
        public void updateTask(){if(item==null&&getDistanceSq(x+.5,y,z+.5)<4){int age=worldObj.getBlockMetadata(x,y,z);int count=1+rand.nextInt(2)+(age==3?1:0);if(worldObj.setBlockMetadataWithNotify(x,y,z,1,3)){if(getHeldItem()==null){setCurrentItemOrArmor(0,new ItemStack(Backport120Content.sweetBerries));count--;}if(count>0)entityDropItem(new ItemStack(Backport120Content.sweetBerries,count),0);playSound("htgt6:entity.fox.eat",1,1);}wait=200;}}
    }
}
