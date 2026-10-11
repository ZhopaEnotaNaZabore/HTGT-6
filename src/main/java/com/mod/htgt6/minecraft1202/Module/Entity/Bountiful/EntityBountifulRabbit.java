package com.mod.htgt6.minecraft1202.Module.Entity.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;

/** Minecraft 1.8 rabbit variants, breeding and drops. */
public final class EntityBountifulRabbit extends EntityAnimal {
    private static final int TYPE = 18;
    private static final int JUMP = 19;
    private int jumpTicks,jumpDelay;
    private boolean previouslyGrounded;
    private int carrotCooldown;
    public EntityBountifulRabbit(World world) {
        super(world); setSize(0.45F,0.55F);
        tasks.addTask(0,new EntityAISwimming(this)); tasks.addTask(1,new EntityAIPanic(this,1.5D));
        tasks.addTask(2,new EntityAIMate(this,0.8D)); tasks.addTask(3,new EntityAITempt(this,1.0D,Items.carrot,false));
        tasks.addTask(3,new EntityAITempt(this,1.0D,Items.golden_carrot,false));
        tasks.addTask(3,new EntityAITempt(this,1.0D,Item.getItemFromBlock(Blocks.yellow_flower),false));
        tasks.addTask(4,new EntityAIFollowParent(this,0.8D)); tasks.addTask(6,new EntityAIWander(this,0.6D));
        tasks.addTask(4,new EntityAIAvoidEntity(this,EntityWolf.class,16.0F,1.33D,1.33D));
        tasks.addTask(4,new KillerAttackAI());
        tasks.addTask(5,new RaidCarrotsAI());
        tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,10.0F));tasks.addTask(7,new EntityAILookIdle(this));
        targetTasks.addTask(1,new KillerTargetAI());
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(TYPE,Byte.valueOf((byte)0));
        dataWatcher.addObject(JUMP,Byte.valueOf((byte)0));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(3.0D);getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30D);}
    public int getRabbitType(){return dataWatcher.getWatchableObjectByte(TYPE)&255;}
    public void setRabbitType(int type){dataWatcher.updateObject(TYPE,Byte.valueOf((byte)type));
        if(type==99&&!hasCustomNameTag())setCustomNameTag("The Killer Bunny");}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){data=super.onSpawnWithEgg(data);
        net.minecraft.world.biome.BiomeGenBase biome=worldObj.getBiomeGenForCoords(
                MathHelper.floor_double(posX),MathHelper.floor_double(posZ));
        if(biome==net.minecraft.world.biome.BiomeGenBase.desert
                ||biome==net.minecraft.world.biome.BiomeGenBase.desertHills)setRabbitType(4);
        else if(biome==net.minecraft.world.biome.BiomeGenBase.icePlains
                ||biome==net.minecraft.world.biome.BiomeGenBase.coldTaiga
                ||biome==net.minecraft.world.biome.BiomeGenBase.coldTaigaHills)
            setRabbitType(rand.nextInt(5)==0?3:1);
        else {int roll=rand.nextInt(10);setRabbitType(roll<5?0:roll<9?5:2);}return data;}
    @Override public EntityBountifulRabbit createChild(EntityAgeable mate){EntityBountifulRabbit child=new EntityBountifulRabbit(worldObj);child.setRabbitType(rand.nextBoolean()?getRabbitType():((EntityBountifulRabbit)mate).getRabbitType());return child;}
    @Override public boolean isBreedingItem(net.minecraft.item.ItemStack stack){return stack!=null
            &&(stack.getItem()==Items.carrot||stack.getItem()==Items.golden_carrot
            ||Block.getBlockFromItem(stack.getItem())==Blocks.yellow_flower);}
    @Override protected Item getDropItem(){return BountifulContent.rawRabbit;}
    @Override protected void dropFewItems(boolean hit,int looting){looting=Math.max(0,looting);
        int meat=1+EntityBountifulGuardian.lootingBonus(rand,looting);
        int hide=rand.nextInt(2)+EntityBountifulGuardian.lootingBonus(rand,looting);if(meat>0)entityDropItem(new net.minecraft.item.ItemStack(
                isBurning()?BountifulContent.cookedRabbit:BountifulContent.rawRabbit,meat),0);
        if(hide>0)entityDropItem(new net.minecraft.item.ItemStack(BountifulContent.rabbitHide,hide),0);
        if(hit&&rand.nextFloat()<0.10F+0.03F*looting)dropItem(BountifulContent.rabbitFoot,1);}
    @Override public boolean getCanSpawnHere(){int x=MathHelper.floor_double(posX),
            y=MathHelper.floor_double(boundingBox.minY)-1,z=MathHelper.floor_double(posZ);
        Block below=worldObj.getBlock(x,y,z);return (below==Blocks.grass||below==Blocks.sand
                ||below==Blocks.snow||below==Blocks.snow_layer)&&worldObj.getBlockLightValue(x,y+1,z)>8
                &&worldObj.checkNoEntityCollision(boundingBox)
                &&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()
                &&!worldObj.isAnyLiquid(boundingBox);}
    @Override public void onLivingUpdate(){
        if(worldObj.isRemote)jumpTicks=dataWatcher.getWatchableObjectByte(JUMP)&255;
        else {if(jumpDelay>0)jumpDelay--;if(carrotCooldown>0)carrotCooldown=Math.max(0,carrotCooldown-rand.nextInt(3));
            if(onGround&&!previouslyGrounded)jumpDelay=getMoveHelper().getSpeed()<2.2?10:1;
            boolean moving=getNavigator().getPath()!=null||getAttackTarget()!=null;
            if(onGround&&moving&&jumpDelay==0){
                net.minecraft.pathfinding.PathEntity path=getNavigator().getPath();
                boolean elevated=path!=null&&!path.isFinished()&&path.getPosition(this).yCoord>posY+.5;
                motionY=jumpImpulse(getMoveHelper().getSpeed(),isCollidedHorizontally||elevated);
                if(isPotionActive(net.minecraft.potion.Potion.jump))motionY+=(getActivePotionEffect(net.minecraft.potion.Potion.jump).getAmplifier()+1)*.1;
                isAirBorne=true;jumpTicks=10;
            }
            if(jumpTicks>0)jumpTicks--;dataWatcher.updateObject(JUMP,Byte.valueOf((byte)jumpTicks));}
        previouslyGrounded=onGround;
        super.onLivingUpdate();
    }
    public static double jumpImpulse(double speed,boolean obstacle){return speed<=.6?.2:obstacle?.5:.3;}
    private final class RaidCarrotsAI extends net.minecraft.entity.ai.EntityAIBase {
        private int x,y,z,searchDelay,timeout;
        RaidCarrotsAI(){setMutexBits(3);}
        public boolean shouldExecute(){
            if(searchDelay-->0||carrotCooldown>0||!worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return false;
            searchDelay=200+rand.nextInt(200);int bx=MathHelper.floor_double(posX),by=MathHelper.floor_double(posY),bz=MathHelper.floor_double(posZ);
            for(int r=0;r<=16;r++)for(int dy=-1;dy<=1;dy++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
                if(Math.abs(dx)!=r&&Math.abs(dz)!=r)continue;
                if(worldObj.getBlock(bx+dx,by+dy,bz+dz)==Blocks.carrots&&worldObj.getBlockMetadata(bx+dx,by+dy,bz+dz)==7){x=bx+dx;y=by+dy;z=bz+dz;return true;}
            }return false;
        }
        public void startExecuting(){timeout=1200;getNavigator().tryMoveToXYZ(x+.5,y,z+.5,.7);}
        public boolean continueExecuting(){return timeout>0&&carrotCooldown==0&&worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")&&worldObj.getBlock(x,y,z)==Blocks.carrots;}
        public void resetTask(){getNavigator().clearPathEntity();}
        public void updateTask(){
            --timeout;getLookHelper().setLookPosition(x+.5,y+.5,z+.5,10,40);
            if(getDistanceSq(x+.5,y,z+.5)>1){if(timeout%40==0)getNavigator().tryMoveToXYZ(x+.5,y,z+.5,.7);return;}
            int age=worldObj.getBlockMetadata(x,y,z);
            if(age==0)worldObj.setBlockToAir(x,y,z);else worldObj.setBlockMetadataWithNotify(x,y,z,age-1,2);
            worldObj.playAuxSFX(2001,x,y,z,Block.getIdFromBlock(Blocks.carrots)|(age<<12));
            carrotCooldown=40;timeout=0;searchDelay=10;
        }
    }
    @Override public boolean attackEntityAsMob(Entity target){return getRabbitType()==99&&target.attackEntityFrom(
            net.minecraft.util.DamageSource.causeMobDamage(this),8.0F);}
    @Override public int getTotalArmorValue(){return getRabbitType()==99?8:super.getTotalArmorValue();}
    private final class KillerAttackAI extends EntityAIAttackOnCollide{
        KillerAttackAI(){super(EntityBountifulRabbit.this,1.4D,true);}
        @Override public boolean shouldExecute(){return getRabbitType()==99&&super.shouldExecute();}
        @Override public boolean continueExecuting(){return getRabbitType()==99&&super.continueExecuting();}}
    private final class KillerTargetAI extends EntityAINearestAttackableTarget{
        KillerTargetAI(){super(EntityBountifulRabbit.this,EntityPlayer.class,0,true);}
        @Override public boolean shouldExecute(){return getRabbitType()==99&&super.shouldExecute();}}
    public float getJumpCompletion(float partial){return jumpTicks<=0?0.0F:
            MathHelper.sin(((10-jumpTicks+partial)/10.0F)*(float)Math.PI);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("RabbitType",getRabbitType());tag.setInteger("MoreCarrotTicks",carrotCooldown);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setRabbitType(tag.getInteger("RabbitType"));carrotCooldown=Math.max(0,tag.getInteger("MoreCarrotTicks"));}
}
