package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/** Minecraft 1.20.2 Panda expressed only through the 1.7.10 animal, AI and DataWatcher API. */
public final class EntityPanda120 extends EntityAnimal {
    public static final int MAIN_GENE_WATCHER=20,HIDDEN_GENE_WATCHER=21,FLAGS_WATCHER=22;
    public static final int UNHAPPY_WATCHER=23,SNEEZE_WATCHER=24,EAT_WATCHER=25;
    public static final int ROLL_COUNTER_WATCHER=26;
    public static final int FLAG_SNEEZE=2,FLAG_ROLL=4,FLAG_SIT=8,FLAG_ON_BACK=16;
    public static final double MAX_HEALTH=20.0D,WEAK_MAX_HEALTH=10.0D;
    public static final double MOVEMENT_SPEED=.15D,LAZY_MOVEMENT_SPEED=.07D,ATTACK_DAMAGE=6.0D;
    public static final int TOTAL_ROLL_STEPS=32,TOTAL_UNHAPPY_TIME=32,EAT_TICK_INTERVAL=5;

    private float sitAmount,sitAmountOld,onBackAmount,onBackAmountOld,rollAmount,rollAmountOld;
    private int rollCounter;

    public EntityPanda120(World world){
        super(world);setSize(1.3F,1.25F);experienceValue=1;
        tasks.addTask(0,new EntityAISwimming(this));
        tasks.addTask(2,new PandaPanicGoal());
        tasks.addTask(2,new PandaMateGoal());
        tasks.addTask(3,new PandaAttackGoal());
        tasks.addTask(4,new EntityAITempt(this,1.0D,Item.getItemFromBlock(Backport120Content.bamboo),false));
        tasks.addTask(5,new EntityAIFollowParent(this,1.25D));
        tasks.addTask(6,new WorriedAvoidGoal(EntityPlayer.class,8.0F));
        tasks.addTask(6,new WorriedAvoidGoal(EntityMob.class,4.0F));
        tasks.addTask(6,new PandaSitGoal());
        tasks.addTask(7,new PandaLieOnBackGoal());
        tasks.addTask(8,new PandaSneezeGoal());
        tasks.addTask(8,new PandaRollGoal());
        tasks.addTask(9,new EntityAIWander(this,1.0D));
        tasks.addTask(10,new EntityAIWatchClosest(this,EntityPlayer.class,6.0F));
        tasks.addTask(11,new EntityAILookIdle(this));
    }

    @Override protected void entityInit(){
        super.entityInit();
        dataWatcher.addObject(MAIN_GENE_WATCHER,Byte.valueOf((byte)Gene.NORMAL.id));
        dataWatcher.addObject(HIDDEN_GENE_WATCHER,Byte.valueOf((byte)Gene.NORMAL.id));
        dataWatcher.addObject(FLAGS_WATCHER,Byte.valueOf((byte)0));
        dataWatcher.addObject(UNHAPPY_WATCHER,Integer.valueOf(0));
        dataWatcher.addObject(SNEEZE_WATCHER,Integer.valueOf(0));
        dataWatcher.addObject(EAT_WATCHER,Integer.valueOf(0));
        dataWatcher.addObject(ROLL_COUNTER_WATCHER,Integer.valueOf(0));
    }

    @Override protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(MOVEMENT_SPEED);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(ATTACK_DAMAGE);
    }
    @Override protected boolean isAIEnabled(){return true;}

    public Gene getMainGene(){return Gene.byId(dataWatcher.getWatchableObjectByte(MAIN_GENE_WATCHER));}
    public Gene getHiddenGene(){return Gene.byId(dataWatcher.getWatchableObjectByte(HIDDEN_GENE_WATCHER));}
    public Gene getVariant(){return Gene.product(getMainGene(),getHiddenGene());}
    public void setMainGene(Gene gene){dataWatcher.updateObject(MAIN_GENE_WATCHER,Byte.valueOf((byte)gene.id));refreshGeneAttributes();}
    public void setHiddenGene(Gene gene){dataWatcher.updateObject(HIDDEN_GENE_WATCHER,Byte.valueOf((byte)gene.id));refreshGeneAttributes();}
    public boolean isLazy(){return getVariant()==Gene.LAZY;}
    public boolean isWorried(){return getVariant()==Gene.WORRIED;}
    public boolean isScared(){return isWorried()&&worldObj!=null&&worldObj.isThundering();}
    public boolean isPlayful(){return getVariant()==Gene.PLAYFUL;}
    public boolean isBrown(){return getVariant()==Gene.BROWN;}
    public boolean isWeak(){return getVariant()==Gene.WEAK;}
    public boolean isAggressive(){return getVariant()==Gene.AGGRESSIVE;}

    public boolean isSneezing(){return flag(FLAG_SNEEZE);}
    public boolean isRolling(){return flag(FLAG_ROLL);}
    public boolean isSitting(){return flag(FLAG_SIT);}
    public boolean isOnBack(){return flag(FLAG_ON_BACK);}
    public void setSneezing(boolean value){setPandaFlag(FLAG_SNEEZE,value);if(!value)setSneezeCounter(0);}
    public void setRolling(boolean value){setPandaFlag(FLAG_ROLL,value);if(!value){rollCounter=0;dataWatcher.updateObject(ROLL_COUNTER_WATCHER,Integer.valueOf(0));}}
    public int getRollCounter(){return dataWatcher.getWatchableObjectInt(ROLL_COUNTER_WATCHER);}
    public void setSitting(boolean value){setPandaFlag(FLAG_SIT,value);if(value)getNavigator().clearPathEntity();}
    public void setOnBack(boolean value){setPandaFlag(FLAG_ON_BACK,value);if(value)getNavigator().clearPathEntity();}
    private boolean flag(int bit){return (dataWatcher.getWatchableObjectByte(FLAGS_WATCHER)&bit)!=0;}
    private void setPandaFlag(int bit,boolean value){int flags=dataWatcher.getWatchableObjectByte(FLAGS_WATCHER)&255;
        dataWatcher.updateObject(FLAGS_WATCHER,Byte.valueOf((byte)(value?flags|bit:flags&~bit)));}
    public int getUnhappyCounter(){return dataWatcher.getWatchableObjectInt(UNHAPPY_WATCHER);}
    public void setUnhappyCounter(int value){dataWatcher.updateObject(UNHAPPY_WATCHER,Integer.valueOf(Math.max(0,value)));}
    public int getSneezeCounter(){return dataWatcher.getWatchableObjectInt(SNEEZE_WATCHER);}
    private void setSneezeCounter(int value){dataWatcher.updateObject(SNEEZE_WATCHER,Integer.valueOf(value));}
    public int getEatCounter(){return dataWatcher.getWatchableObjectInt(EAT_WATCHER);}
    private void setEatCounter(int value){dataWatcher.updateObject(EAT_WATCHER,Integer.valueOf(value));}

    private void refreshGeneAttributes(){
        if(getEntityAttribute(SharedMonsterAttributes.maxHealth)==null)return;
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(isWeak()?WEAK_MAX_HEALTH:MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(isLazy()?LAZY_MOVEMENT_SPEED:MOVEMENT_SPEED);
        if(getHealth()>getMaxHealth())setHealth(getMaxHealth());
    }

    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){
        data=super.onSpawnWithEgg(data);setMainGene(Gene.random(rand));setHiddenGene(Gene.random(rand));
        refreshGeneAttributes();setHealth(getMaxHealth());return data;
    }

    @Override public void onLivingUpdate(){
        super.onLivingUpdate();
        sitAmountOld=sitAmount;onBackAmountOld=onBackAmount;rollAmountOld=rollAmount;
        sitAmount=approach(sitAmount,isSitting()?1F:0F,.15F);
        onBackAmount=approach(onBackAmount,isOnBack()?1F:0F,.15F);
        rollAmount=approach(rollAmount,isRolling()?1F:0F,.15F);
        if(worldObj.isRemote)return;
        pickupNearbyFood();
        if(isWorried()&&worldObj.isThundering()){setSitting(true);setOnBack(false);setRolling(false);}
        int unhappy=getUnhappyCounter();if(unhappy>0){setUnhappyCounter(unhappy-1);if(unhappy==29||unhappy==14)playSound("htgt6:entity.panda.cant_breed",1F,1F);}
        tickSneeze();tickRoll();tickEating();
    }

    private void tickSneeze(){if(!isSneezing())return;int counter=getSneezeCounter()+1;setSneezeCounter(counter);
        if(counter==1)playSound("htgt6:entity.panda.pre_sneeze",1F,1F);if(counter>20){setSneezing(false);playSound("htgt6:entity.panda.sneeze",1F,1F);afterSneeze();}}
    private void afterSneeze(){
        List<?> pandas=worldObj.getEntitiesWithinAABB(EntityPanda120.class,boundingBox.expand(10,4,10));
        for(Object raw:pandas){EntityPanda120 panda=(EntityPanda120)raw;if(!panda.isChild()&&panda.onGround&&!panda.isInWater()){
            panda.motionY=.4D;panda.velocityChanged=true;}}
        if(rand.nextInt(700)==0)dropItem(Items.slime_ball,1);
    }
    private void tickRoll(){if(!isRolling())return;if(++rollCounter>TOTAL_ROLL_STEPS){setRolling(false);return;}
        if(rollCounter==1){float yaw=rotationYaw*.017453292F;double speed=isChild()?.1D:.2D;
            motionX-=MathHelper.sin(yaw)*speed;motionZ+=MathHelper.cos(yaw)*speed;motionY=.27D;}
        if(rollCounter==7||rollCounter==15||rollCounter==23)motionY=onGround?.27D:motionY;
        dataWatcher.updateObject(ROLL_COUNTER_WATCHER,Integer.valueOf(rollCounter));velocityChanged=true;
    }
    private void tickEating(){ItemStack held=getEquipmentInSlot(0);if(!isSitting()||!isPandaFood(held)){setEatCounter(0);return;}
        int ticks=getEatCounter()+1;setEatCounter(ticks);if(ticks%EAT_TICK_INTERVAL==0)playSound("htgt6:entity.panda.eat",.5F,.9F+rand.nextFloat()*.2F);
        if(ticks>100){if(held!=null&&--held.stackSize<=0)setCurrentItemOrArmor(0,null);setEatCounter(0);setSitting(false);}}

    @Override public boolean interact(EntityPlayer player){
        ItemStack held=player.inventory.getCurrentItem();
        if(isBreedingItem(held)&&!isChild()&&!isInLove()){
            if(hasNearbyBamboo())return super.interact(player);
            if(!worldObj.isRemote){setSitting(true);setCurrentItemOrArmor(0,new ItemStack(held.getItem(),1,held.getItemDamage()));
                if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}
            return true;
        }
        return super.interact(player);
    }
    @Override public boolean isBreedingItem(ItemStack stack){return stack!=null&&stack.getItem()==Item.getItemFromBlock(Backport120Content.bamboo);}
    public static boolean isPandaFood(ItemStack stack){return stack!=null&&stack.stackSize>0&&(stack.getItem()==Item.getItemFromBlock(Backport120Content.bamboo)||stack.getItem()==Items.cake);}
    private boolean canPerformAction(){return !isOnBack()&&!isScared()&&!isRolling()&&!isSitting()&&getEatCounter()==0;}
    private boolean edibleDrop(EntityItem item){return item!=null&&!item.isDead&&item.delayBeforeCanPickup<=0&&isPandaFood(item.getEntityItem());}
    private void pickupNearbyFood(){
        if(worldObj.isRemote||!isEntityAlive()||getEquipmentInSlot(0)!=null||!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return;
        for(Object raw:worldObj.getEntitiesWithinAABB(EntityItem.class,boundingBox.expand(1,0,1))){
            EntityItem item=(EntityItem)raw;if(!edibleDrop(item))continue;
            ItemStack copy=item.getEntityItem().copy();setCurrentItemOrArmor(0,copy);setEquipmentDropChance(0,2F);
            onItemPickup(item,copy.stackSize);item.setDead();return;
        }
    }

    @Override public EntityPanda120 createChild(EntityAgeable mate){
        EntityPanda120 child=new EntityPanda120(worldObj);EntityPanda120 other=mate instanceof EntityPanda120?(EntityPanda120)mate:null;
        child.setGeneFromParents(this,other,rand);return child;
    }
    public void setGeneFromParents(EntityPanda120 first,EntityPanda120 second,Random random){
        Gene main=parentGene(first,random),hidden=second==null?Gene.random(random):parentGene(second,random);
        if(random.nextBoolean()){Gene swap=main;main=hidden;hidden=swap;}
        if(random.nextInt(32)==0)main=Gene.random(random);if(random.nextInt(32)==0)hidden=Gene.random(random);
        setMainGene(main);setHiddenGene(hidden);
    }
    private static Gene parentGene(EntityPanda120 parent,Random random){return random.nextBoolean()?parent.getMainGene():parent.getHiddenGene();}
    @Override public boolean canMateWith(EntityAnimal mate){return mate instanceof EntityPanda120&&super.canMateWith(mate);}

    @Override public boolean attackEntityAsMob(Entity target){
        boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(this),(float)getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue());
        if(hit){playSound("htgt6:entity.panda.bite",1F,1F);if(!isAggressive())setAttackTarget(null);}return hit;
    }
    @Override public boolean attackEntityFrom(DamageSource source,float amount){
        boolean hurt=super.attackEntityFrom(source,amount);Entity attacker=source.getEntity();
        if(hurt&&attacker instanceof EntityLivingBase)setAttackTarget((EntityLivingBase)attacker);
        if(hurt){setSitting(false);setOnBack(false);setRolling(false);}return hurt;
    }
    @Override protected boolean isMovementBlocked(){return isSitting()||isOnBack()||isRolling()||super.isMovementBlocked();}
    @Override protected void dropFewItems(boolean hit,int looting){dropItem(Item.getItemFromBlock(Backport120Content.bamboo),1);}
    @Override public boolean getCanSpawnHere(){int x=MathHelper.floor_double(posX),z=MathHelper.floor_double(posZ);
        return worldObj.getBiomeGenForCoords(x,z).biomeID==FuturaBiomeIds120.BAMBOO_JUNGLE&&super.getCanSpawnHere();}

    public float getSitAmount(float partial){return sitAmountOld+(sitAmount-sitAmountOld)*partial;}
    public float getOnBackAmount(float partial){return onBackAmountOld+(onBackAmount-onBackAmountOld)*partial;}
    public float getRollAmount(float partial){return rollAmountOld+(rollAmount-rollAmountOld)*partial;}
    private static float approach(float value,float target,float step){return value<target?Math.min(target,value+step):Math.max(target,value-step);}

    /** 1.20 breeding gate: pandas need bamboo within an 8x3x8 neighbourhood. */
    public boolean hasNearbyBamboo(){
        int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(posY),z=MathHelper.floor_double(posZ);
        for(int dx=-8;dx<=8;dx++)for(int dz=-8;dz<=8;dz++)for(int dy=-3;dy<=3;dy++)
            if(worldObj.getBlock(x+dx,y+dy,z+dz)==Backport120Content.bamboo)return true;
        return false;
    }

    @Override protected String getLivingSound(){return "htgt6:entity.panda."+(isAggressive()?"aggressive_ambient":isWorried()?"worried_ambient":"ambient");}
    @Override protected String getHurtSound(){return "htgt6:entity.panda.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.panda.death";}
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block block){playSound("htgt6:entity.panda.step",.15F,1F);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setString("MainGene",getMainGene().name);
        tag.setString("HiddenGene",getHiddenGene().name);tag.setBoolean("Sitting",isSitting());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setMainGene(Gene.byName(tag.getString("MainGene")));
        setHiddenGene(Gene.byName(tag.getString("HiddenGene")));setSitting(tag.getBoolean("Sitting"));refreshGeneAttributes();}

    public enum Gene {
        NORMAL(0,"normal",false),LAZY(1,"lazy",false),WORRIED(2,"worried",false),PLAYFUL(3,"playful",false),
        BROWN(4,"brown",true),WEAK(5,"weak",true),AGGRESSIVE(6,"aggressive",false);
        public final int id;public final String name;public final boolean recessive;
        Gene(int id,String name,boolean recessive){this.id=id;this.name=name;this.recessive=recessive;}
        public static Gene product(Gene main,Gene hidden){return main.recessive?(main==hidden?main:NORMAL):main;}
        public static Gene byId(int id){for(Gene gene:values())if(gene.id==id)return gene;return NORMAL;}
        public static Gene byName(String name){for(Gene gene:values())if(gene.name.equals(name))return gene;return NORMAL;}
        /** Exact 1.20.2 16-way gene distribution. */
        public static Gene random(Random random){return fromRoll(random.nextInt(16));}
        public static Gene fromRoll(int value){if(value==0)return LAZY;if(value==1)return WORRIED;
            if(value==2)return PLAYFUL;if(value==4)return AGGRESSIVE;if(value<9)return WEAK;if(value<11)return BROWN;return NORMAL;}
    }

    private final class PandaMateGoal extends EntityAIMate {
        PandaMateGoal(){super(EntityPanda120.this,1.0D);}
        @Override public boolean shouldExecute(){
            if(!isInLove())return false;
            if(hasNearbyBamboo())return super.shouldExecute();
            setUnhappyCounter(TOTAL_UNHAPPY_TIME);return false;
        }
    }
    private final class WorriedAvoidGoal extends EntityAIAvoidEntity {
        WorriedAvoidGoal(Class type,float distance){super(EntityPanda120.this,type,distance,2.0D,2.0D);}
        @Override public boolean shouldExecute(){return isWorried()&&!isSitting()&&!isOnBack()&&super.shouldExecute();}
    }

    private final class PandaPanicGoal extends EntityAIBase {
        private int ticks;PandaPanicGoal(){setMutexBits(1);}
        @Override public boolean shouldExecute(){return (isBurning()||isWorried()&&worldObj.isThundering())&&!isSitting();}
        @Override public boolean continueExecuting(){return ticks++<60&&!getNavigator().noPath();}
        @Override public void startExecuting(){ticks=0;getNavigator().tryMoveToXYZ(posX+rand.nextInt(17)-8,posY,posZ+rand.nextInt(17)-8,2D);}
    }
    private final class PandaAttackGoal extends EntityAIBase {
        private int cooldown;
        PandaAttackGoal(){setMutexBits(3);}
        @Override public boolean shouldExecute(){return canPerformAction()&&getAttackTarget()!=null&&getAttackTarget().isEntityAlive();}
        @Override public boolean continueExecuting(){return shouldExecute();}
        @Override public void updateTask(){
            if(cooldown>0)--cooldown;EntityLivingBase target=getAttackTarget();if(target==null)return;
            getNavigator().tryMoveToEntityLiving(target,1.2D);getLookHelper().setLookPositionWithEntity(target,30,30);
            double reach=width*2F*width*2F+target.width;
            if(cooldown==0&&getDistanceSq(target.posX,target.boundingBox.minY,target.posZ)<=reach){cooldown=20;attackEntityAsMob(target);}
        }
    }
    private final class PandaSitGoal extends EntityAIBase {
        private EntityItem target;
        PandaSitGoal(){setMutexBits(3);}
        @Override public boolean shouldExecute(){target=null;return !isChild()&&!isInWater()&&canPerformAction()&&getUnhappyCounter()==0&&(isPandaFood(getEquipmentInSlot(0))||findFood());}
        private boolean findFood(){
            if(!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return false;
            double nearest=Double.MAX_VALUE;for(Object raw:worldObj.getEntitiesWithinAABB(EntityItem.class,boundingBox.expand(6,6,6))){
                EntityItem item=(EntityItem)raw;if(edibleDrop(item)&&getDistanceSqToEntity(item)<nearest){nearest=getDistanceSqToEntity(item);target=item;}
            }return target!=null;
        }
        @Override public boolean continueExecuting(){return !isInWater()&&(isPandaFood(getEquipmentInSlot(0))&&getEatCounter()<120
                ||target!=null&&edibleDrop(target)&&worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")&&!getNavigator().noPath());}
        @Override public void startExecuting(){if(isPandaFood(getEquipmentInSlot(0))){getNavigator().clearPathEntity();setSitting(true);}else if(target!=null)getNavigator().tryMoveToEntityLiving(target,1.2D);}
        @Override public void updateTask(){if(isPandaFood(getEquipmentInSlot(0))){getNavigator().clearPathEntity();setSitting(true);}}
        @Override public void resetTask(){target=null;if(!isPandaFood(getEquipmentInSlot(0)))setSitting(false);}
    }
    private final class PandaSneezeGoal extends EntityAIBase {
        @Override public boolean shouldExecute(){return isChild()&&!isSneezing()&&rand.nextInt(isWeak()?500:6000)==1;}
        @Override public void startExecuting(){setSneezing(true);}
    }
    private final class PandaRollGoal extends EntityAIBase {
        @Override public boolean shouldExecute(){return !isRolling()&&!isSitting()&&onGround&&(isPlayful()?rand.nextInt(60):isChild()?rand.nextInt(6000):1)==0;}
        @Override public boolean continueExecuting(){return isRolling();}
        @Override public void startExecuting(){setRolling(true);}
    }
    private final class PandaLieOnBackGoal extends EntityAIBase {
        private int ticks;@Override public boolean shouldExecute(){return isLazy()&&!isChild()&&!isSitting()&&onGround&&rand.nextInt(400)==1;}
        @Override public boolean continueExecuting(){return isOnBack()&&ticks++<200;}
        @Override public void startExecuting(){ticks=0;setOnBack(true);}
        @Override public void resetTask(){setOnBack(false);}
    }
}
