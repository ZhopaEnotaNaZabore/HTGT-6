package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Native 1.7 animal implementation of the 1.20.2 Camel; no horse state or inventory is inherited. */
public final class EntityCamel120 extends EntityAnimal {
    public static final int STATE_WATCHER=16,DASH_COOLDOWN_WATCHER=17,DASH_COOLDOWN_TICKS=55;
    public static final double MAX_HEALTH=32.0D,MOVEMENT_SPEED=0.09D;
    public static final double DASH_HORIZONTAL_BASE=1.5D,DASH_VERTICAL_BASE=0.55D;
    public static final String NBT_SITTING="IsSitting",NBT_SADDLED="Saddle",
            NBT_LAST_POSE_TICK="LastPoseTick",NBT_DASH_COOLDOWN="DashCooldown";
    private static final int SITTING_FLAG=1,SADDLED_FLAG=2;
    private int poseTicks;
    private boolean previousSitting;
    private float dashRequest;
    private long lastPoseTick;

    public EntityCamel120(World world){
        super(world);setSize(1.7F,2.375F);stepHeight=1.5F;getNavigator().setAvoidsWater(true);
        tasks.addTask(0,new EntityAISwimming(this));
        tasks.addTask(1,new EntityAIPanic(this,1.2D));
        tasks.addTask(2,new EntityAIMate(this,1.0D));
        tasks.addTask(3,new EntityAITempt(this,1.1D,Item.getItemFromBlock(Blocks.cactus),false));
        tasks.addTask(4,new EntityAIFollowParent(this,1.0D));
        tasks.addTask(5,new EntityAIWander(this,.8D));
        tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,8.0F));
        tasks.addTask(7,new EntityAILookIdle(this));
    }
    @Override protected void entityInit(){
        super.entityInit();dataWatcher.addObject(STATE_WATCHER,Byte.valueOf((byte)0));
        dataWatcher.addObject(DASH_COOLDOWN_WATCHER,Integer.valueOf(0));
    }
    @Override protected void applyEntityAttributes(){
        super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(MOVEMENT_SPEED);
    }
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){
        data=super.onSpawnWithEgg(data);setHealth(getMaxHealth());return data;
    }
    private boolean state(int flag){return(dataWatcher.getWatchableObjectByte(STATE_WATCHER)&flag)!=0;}
    private void state(int flag,boolean enabled){byte value=dataWatcher.getWatchableObjectByte(STATE_WATCHER);
        dataWatcher.updateObject(STATE_WATCHER,Byte.valueOf((byte)(enabled?value|flag:value&~flag)));}
    public boolean isCamelSitting(){return state(SITTING_FLAG);}
    public boolean isSaddled(){return state(SADDLED_FLAG);}
    public void setSaddled(boolean value){state(SADDLED_FLAG,value);}
    public void setCamelSitting(boolean sitting){
        if(isCamelSitting()==sitting)return;state(SITTING_FLAG,sitting);poseTicks=0;
        lastPoseTick=worldObj==null?0L:worldObj.getTotalWorldTime();
        if(worldObj!=null&&!worldObj.isRemote)playSound(sitting?"htgt6:entity.camel.sit":"htgt6:entity.camel.stand",1,1);
    }
    public int getDashCooldown(){return dataWatcher.getWatchableObjectInt(DASH_COOLDOWN_WATCHER);}
    private void setDashCooldown(int ticks){dataWatcher.updateObject(DASH_COOLDOWN_WATCHER,Integer.valueOf(Math.max(0,ticks)));}
    public float getPoseProgress(float partial){float v=Math.min(40F,poseTicks+partial)/40F;return isCamelSitting()?v:1F-v;}
    public static double dashHorizontalStrength(int power){return DASH_HORIZONTAL_BASE+.4D+.4D*MathHelper.clamp_int(power,0,90)/90D;}
    public static double dashVerticalStrength(int power){return DASH_VERTICAL_BASE+(.4D+.4D*MathHelper.clamp_int(power,0,90)/90D)*.25D;}

    @Override public void onLivingUpdate(){
        super.onLivingUpdate();boolean sitting=isCamelSitting();
        if(sitting!=previousSitting){poseTicks=0;previousSitting=sitting;}if(poseTicks<Integer.MAX_VALUE)poseTicks++;
        if(!worldObj.isRemote){int cooldown=getDashCooldown();
            if(cooldown>0){setDashCooldown(cooldown-1);if(cooldown==1)playSound("htgt6:entity.camel.dash_ready",1,1);}
            if(riddenByEntity!=null)getNavigator().clearPathEntity();
            if(riddenByEntity!=null&&sitting)setCamelSitting(false);
            else if(riddenByEntity==null&&onGround){
                if(sitting){if(poseTicks>100+rand.nextInt(100))setCamelSitting(false);}
                else if(ticksExisted%40==0&&rand.nextInt(60)==0)setCamelSitting(true);
            }
        }
    }
    @Override public boolean interact(EntityPlayer player){
        ItemStack hand=player.inventory.getCurrentItem();
        if(isCamelFood(hand)){
            if(!worldObj.isRemote){boolean used=false;if(getHealth()<getMaxHealth()){heal(2);used=true;}
                if(isChild()){addGrowth(60);used=true;}else if(getGrowingAge()==0&&!isInLove()){func_146082_f(player);used=true;}
                if(used){playSound("htgt6:entity.camel.eat",1,1);consume(player,hand);}}
            return true;
        }
        if(hand!=null&&hand.getItem()==Items.saddle&&!isSaddled()){
            if(!worldObj.isRemote){setSaddled(true);consume(player,hand);}
            return true;
        }
        if(isChild())return super.interact(player);
        if(!worldObj.isRemote){if(isCamelSitting())setCamelSitting(false);
            if(riddenByEntity==null||riddenByEntity==player)player.mountEntity(this);else mountSecondPassenger(player);}
        return true;
    }
    private static void consume(EntityPlayer player,ItemStack stack){
        if(!player.capabilities.isCreativeMode&&--stack.stackSize<=0)
            player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
    }
    private void mountSecondPassenger(EntityPlayer player){
        EntityCamelSeat120 seat=findSecondSeat();
        if(seat==null){seat=new EntityCamelSeat120(worldObj,this);worldObj.spawnEntityInWorld(seat);}
        if(seat.riddenByEntity==null||seat.riddenByEntity==player)player.mountEntity(seat);
    }
    private EntityCamelSeat120 findSecondSeat(){
        java.util.List nearby=worldObj.getEntitiesWithinAABB(EntityCamelSeat120.class,boundingBox.expand(2.5D,2.5D,2.5D));
        for(Object value:nearby){EntityCamelSeat120 seat=(EntityCamelSeat120)value;if(seat.getCamel()==this)return seat;}return null;
    }
    public static boolean isCamelFood(ItemStack stack){return stack!=null&&stack.getItem()==Item.getItemFromBlock(Blocks.cactus);}
    @Override public boolean isBreedingItem(ItemStack stack){return isCamelFood(stack);}
    @Override public boolean getCanSpawnHere(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY)-1,z=MathHelper.floor_double(posZ);
        return isCamelSpawnGround(worldObj.getBlock(x,y,z))&&worldObj.getBlockLightValue(x,y+1,z)>8
                &&worldObj.checkNoEntityCollision(boundingBox)&&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()
                &&!worldObj.isAnyLiquid(boundingBox);}
    public static boolean isCamelSpawnGround(net.minecraft.block.Block block){Object key=block==null?null:net.minecraft.block.Block.blockRegistry.getNameForObject(block);return isCamelSpawnGroundKey(key==null?null:String.valueOf(key));}
    public static boolean isCamelSpawnGroundKey(String key){return"minecraft:sand".equals(key)||"minecraft:sandstone".equals(key);}
    @Override public EntityCamel120 createChild(EntityAgeable mate){return new EntityCamel120(worldObj);}
    @Override public boolean canMateWith(EntityAnimal mate){return mate instanceof EntityCamel120&&super.canMateWith(mate);}
    @Override public double getMountedYOffset(){return isCamelSitting()?1.1D:2.05D;}
    @Override protected boolean isMovementBlocked(){return isCamelSitting()||super.isMovementBlocked();}
    @Override public boolean canBeSteered(){return riddenByEntity instanceof EntityLivingBase&&isSaddled();}

    /** Server-authoritative endpoint used by the module's native 1.7 input packet. */
    public void requestDash(int power){power=MathHelper.clamp_int(power,0,90);
        if(getDashCooldown()==0&&!isCamelSitting()&&riddenByEntity!=null&&isSaddled()&&onGround)
            dashRequest=.4F+.4F*power/90F;}
    @Override public void moveEntityWithHeading(float strafe,float forward){
        if(riddenByEntity instanceof EntityLivingBase&&isSaddled()){
            EntityLivingBase rider=(EntityLivingBase)riddenByEntity;rotationYaw=rider.rotationYaw;prevRotationYaw=rotationYaw;
            rotationPitch=rider.rotationPitch*.5F;setRotation(rotationYaw,rotationPitch);renderYawOffset=rotationYaw;rotationYawHead=rotationYaw;
            strafe=rider.moveStrafing*.5F;forward=rider.moveForward;if(forward<=0)forward*=.25F;
            setAIMoveSpeed((float)getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
        }
        if(dashRequest>0&&riddenByEntity!=null&&isSaddled()&&onGround&&!worldObj.isRemote){
            float yaw=rotationYaw*(float)Math.PI/180F;double strength=DASH_HORIZONTAL_BASE+dashRequest;
            motionX+=-MathHelper.sin(yaw)*strength;motionZ+=MathHelper.cos(yaw)*strength;
            motionY=DASH_VERTICAL_BASE+dashRequest*.25D;velocityChanged=true;setDashCooldown(DASH_COOLDOWN_TICKS);
            playSound("htgt6:entity.camel.dash",1,1);dashRequest=0;
        }
        super.moveEntityWithHeading(strafe,forward);
    }
    @Override protected void dropFewItems(boolean player,int looting){super.dropFewItems(player,looting);if(isSaddled())dropItem(Items.saddle,1);}
    @Override protected String getLivingSound(){return"htgt6:entity.camel.ambient";}
    @Override protected String getHurtSound(){return"htgt6:entity.camel.hurt";}
    @Override protected String getDeathSound(){return"htgt6:entity.camel.death";}
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block block){playSound(block==Blocks.sand?"htgt6:entity.camel.step_sand":"htgt6:entity.camel.step",.9F,1);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);
        tag.setBoolean(NBT_SITTING,isCamelSitting());tag.setBoolean(NBT_SADDLED,isSaddled());tag.setLong(NBT_LAST_POSE_TICK,lastPoseTick);tag.setInteger(NBT_DASH_COOLDOWN,getDashCooldown());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);
        state(SITTING_FLAG,tag.getBoolean(NBT_SITTING));
        // One-way save compatibility for camels created by the removed EntityHorse implementation.
        setSaddled(tag.hasKey(NBT_SADDLED)?tag.getBoolean(NBT_SADDLED):tag.hasKey("SaddleItem"));
        lastPoseTick=tag.getLong(NBT_LAST_POSE_TICK);
        setDashCooldown(tag.getInteger(NBT_DASH_COOLDOWN));previousSitting=isCamelSitting();poseTicks=40;}
}
