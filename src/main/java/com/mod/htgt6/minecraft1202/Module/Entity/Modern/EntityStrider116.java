package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.StriderNavigation116;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.*;
import net.minecraft.world.World;
import java.util.UUID;

public final class EntityStrider116 extends EntityAnimal {
    private static final UUID COLD=UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174");
    private StriderNavigation116 lavaNavigator;
    private EntityPlayer lastRider;
    private int pendingJockey;
    private static final class SpawnGroup implements IEntityLivingData {int members;}
    public EntityStrider116(World w){
        super(w);setSize(.9F,1.7F);isImmuneToFire=true;stepHeight=1;lavaNavigator=new StriderNavigation116(this,w);
        tasks.addTask(0,new EntityAIPanic(this,1.65));tasks.addTask(2,new EntityAIMate(this,1));
        tasks.addTask(3,new EntityAITempt(this,1.4,Item.getItemFromBlock(NetherVegetationContent116.warpedFungus),false));
        tasks.addTask(3,new EntityAITempt(this,1.4,StriderContent116.fungusStick,false));
        tasks.addTask(4,new SeekLava());tasks.addTask(5,new EntityAIFollowParent(this,1));tasks.addTask(7,new EntityAIWander(this,1));tasks.addTask(8,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(9,new EntityAILookIdle(this));
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(20,Byte.valueOf((byte)0));dataWatcher.addObject(21,Integer.valueOf(0));dataWatcher.addObject(22,Integer.valueOf(0));}
    @Override protected boolean isAIEnabled(){return true;}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.175);getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(16);}
    @Override public PathNavigate getNavigator(){return lavaNavigator==null?super.getNavigator():lavaNavigator;}
    @Override protected void updateAITasks(){if(lavaNavigator!=null)lavaNavigator.onUpdateNavigation();super.updateAITasks();}
    public boolean isSaddled(){return (dataWatcher.getWatchableObjectByte(20)&1)!=0;}
    public boolean isCold(){return (dataWatcher.getWatchableObjectByte(20)&2)!=0;}
    private void flag(int mask,boolean value){int flags=dataWatcher.getWatchableObjectByte(20);dataWatcher.updateObject(20,Byte.valueOf((byte)(value?flags|mask:flags&~mask)));}
    public void setSaddled(boolean saddle){flag(1,saddle);}
    public void setCold(boolean cold){
        flag(2,cold);IAttributeInstance speed=getEntityAttribute(SharedMonsterAttributes.movementSpeed);AttributeModifier old=speed.getModifier(COLD);
        if(cold&&old==null)speed.applyModifier(new AttributeModifier(COLD,"Strider suffocating modifier",-.34,1));else if(!cold&&old!=null)speed.removeModifier(old);
    }
    public int boostAge(){return (int)worldObj.getTotalWorldTime()-dataWatcher.getWatchableObjectInt(22);}
    public boolean boost(){int duration=dataWatcher.getWatchableObjectInt(21);if(worldObj.isRemote||duration>0&&boostAge()<=duration)return false;dataWatcher.updateObject(22,Integer.valueOf((int)worldObj.getTotalWorldTime()));dataWatcher.updateObject(21,Integer.valueOf(140+rand.nextInt(841)));return true;}
    public float boostMultiplier(){int age=boostAge(),duration=dataWatcher.getWatchableObjectInt(21);return duration>0&&age>=0&&age<duration?1+1.15F*MathHelper.sin((float)age/duration*(float)Math.PI):1;}
    public float riddenSpeed(){return (float)getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue()*(isCold()?.35F:.55F)*boostMultiplier();}
    public static boolean controls(EntityPlayer player){ItemStack main=player.getHeldItem(),off=OffhandHooks.getOffhand(player);return main!=null&&main.getItem()==StriderContent116.fungusStick||off!=null&&off.getItem()==StriderContent116.fungusStick;}
    @Override public boolean canBeSteered(){return isSaddled()&&riddenByEntity instanceof EntityPlayer&&controls((EntityPlayer)riddenByEntity);}
    @Override public boolean isBreedingItem(ItemStack stack){return stack!=null&&stack.getItem()==Item.getItemFromBlock(NetherVegetationContent116.warpedFungus);}
    @Override public EntityAgeable createChild(EntityAgeable mate){return new EntityStrider116(worldObj);}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){
        SpawnGroup group=data instanceof SpawnGroup?(SpawnGroup)data:new SpawnGroup();
        if(group.members++>0&&rand.nextFloat()<.5F)setGrowingAge(-24000);
        if(!isChild()){if(rand.nextInt(30)==0)pendingJockey=1;else if(rand.nextInt(10)==0)pendingJockey=2;}
        return group;
    }
    @Override public boolean getCanSpawnHere(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);return worldObj.provider.dimensionId==-1&&(worldObj.getBlock(x,y,z).getMaterial()==Material.lava||worldObj.getBlock(x,y-1,z).getMaterial()==Material.lava)&&worldObj.getBlock(x,y+1,z).getMaterial()!=Material.lava&&worldObj.checkNoEntityCollision(boundingBox)&&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty();}
    private void spawnJockey(){
        if(pendingJockey==0||ticksExisted%20!=0||!addedToChunk||riddenByEntity!=null)return;
        EntityLiving rider;if(pendingJockey==1){EntityZombifiedPiglin116 zombie=new EntityZombifiedPiglin116(worldObj);zombie.setCurrentItemOrArmor(0,new ItemStack(StriderContent116.fungusStick));rider=zombie;}
        else{EntityStrider116 child=new EntityStrider116(worldObj);child.setGrowingAge(-24000);rider=child;}
        rider.setLocationAndAngles(posX,posY+getMountedYOffset(),posZ,rotationYaw,0);
        if(worldObj.spawnEntityInWorld(rider)){if(pendingJockey==1)setSaddled(true);rider.mountEntity(this);pendingJockey=0;}
    }
    @Override public boolean interact(EntityPlayer player){
        ItemStack held=player.getHeldItem();boolean food=isBreedingItem(held);
        if(!food&&isSaddled()&&riddenByEntity==null&&!player.isSneaking()){if(!worldObj.isRemote)player.mountEntity(this);return true;}
        if(!isChild()&&!isSaddled()&&held!=null&&held.getItem()==Items.saddle){if(!worldObj.isRemote){setSaddled(true);if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);playSound("htgt6:entity.strider.saddle",.5F,1);}return true;}
        if(food&&isChild()){if(!worldObj.isRemote){setGrowingAge(Math.min(0,getGrowingAge()+(int)(-getGrowingAge()/20*.1F)*20));if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);}return true;}
        boolean used=super.interact(player);if(used&&food&&!worldObj.isRemote)playSound("htgt6:entity.strider.eat",1,1+(rand.nextFloat()-rand.nextFloat())*.2F);return used;
    }
    public boolean warm(){int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);return worldObj.getBlock(x,y,z).getMaterial()==Material.lava||worldObj.getBlock(x,y-1,z).getMaterial()==Material.lava||handleLavaMovement();}
    /** Half-block collision surface is LiquidBlock.STABLE_SHAPE in the original game. */
    public double lavaSurface(){
        int x=MathHelper.floor_double(posX),y=MathHelper.floor_double(boundingBox.minY),z=MathHelper.floor_double(posZ);
        for(int dy=0;dy>=-1;dy--){int ly=y+dy;if(worldObj.blockExists(x,ly,z)&&worldObj.getBlock(x,ly,z).getMaterial()==Material.lava&&worldObj.getBlockMetadata(x,ly,z)==0&&worldObj.getBlock(x,ly+1,z).getMaterial()!=Material.lava)return ly+.5;}
        return Double.NaN;
    }
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();if(worldObj.isRemote)return;
        setCold(!warm()||ridingEntity instanceof EntityStrider116&&((EntityStrider116)ridingEntity).isCold());
        if(isWet())attackEntityFrom(DamageSource.drown,1);
        spawnJockey();
        if(riddenByEntity instanceof EntityPlayer)lastRider=(EntityPlayer)riddenByEntity;
        else if(lastRider!=null){if(lastRider.worldObj==worldObj&&lastRider.ridingEntity==null&&getDistanceSqToEntity(lastRider)<16)safeDismount(lastRider);lastRider=null;}
    }
    @Override public void moveEntityWithHeading(float strafe,float forward){
        boolean riding=canBeSteered();if(riding){EntityPlayer rider=(EntityPlayer)riddenByEntity;rotationYaw=rider.rotationYaw;rotationPitch=rider.rotationPitch*.5F;renderYawOffset=rotationYaw;rotationYawHead=rotationYaw;setRotation(rotationYaw,rotationPitch);strafe=0;forward=1;setAIMoveSpeed(riddenSpeed());getNavigator().clearPathEntity();}
        else if(riddenByEntity instanceof EntityPlayer){strafe=forward=0;getNavigator().clearPathEntity();}
        double surface=lavaSurface();boolean lava=handleLavaMovement()||!Double.isNaN(surface)&&boundingBox.minY<=surface+.6;
        if(!lava){super.moveEntityWithHeading(strafe,forward);return;}
        double before=boundingBox.minY;float acceleration=getAIMoveSpeed();moveFlying(strafe,forward,acceleration);moveEntity(motionX,motionY,motionZ);motionX*=.546;motionZ*=.546;
        surface=lavaSurface();if(!Double.isNaN(surface)&&before>=surface-.05&&boundingBox.minY<=surface+.05&&worldObj.getCollidingBoundingBoxes(this,boundingBox.getOffsetBoundingBox(0,surface-boundingBox.minY,0)).isEmpty()){
            setPosition(posX,posY+surface-boundingBox.minY,posZ);motionY=0;onGround=true;
        }else{motionY=!Double.isNaN(surface)&&boundingBox.minY>=surface?(motionY-.08)*.98:motionY*.5+.05;onGround=false;}
        fallDistance=0;
        double dx=posX-prevPosX,dz=posZ-prevPosZ;float movement=Math.min(1,MathHelper.sqrt_double(dx*dx+dz*dz)*4);prevLimbSwingAmount=limbSwingAmount;limbSwingAmount+=(movement-limbSwingAmount)*.4F;limbSwing+=limbSwingAmount;
    }
    @Override protected void fall(float distance){if(!warm())super.fall(distance);}
    @Override public double getMountedYOffset(){return height-.19+Math.cos(limbSwing*1.5)*.24*Math.min(.25,limbSwingAmount);}
    private void safeDismount(EntityPlayer p){
        int bx=MathHelper.floor_double(posX),by=MathHelper.floor_double(boundingBox.maxY),bz=MathHelper.floor_double(posZ);
        for(int dy=0;dy>=-2;dy--)for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            if(dx==0&&dz==0)continue;int x=bx+dx,y=by+dy,z=bz+dz;if(!worldObj.blockExists(x,y,z))continue;
            net.minecraft.block.Block below=worldObj.getBlock(x,y-1,z);if(below.getMaterial().isLiquid()||below.getCollisionBoundingBoxFromPool(worldObj,x,y-1,z)==null||worldObj.getBlock(x,y,z).getMaterial().isLiquid())continue;
            AxisAlignedBB box=AxisAlignedBB.getBoundingBox(x+.5-p.width/2,y,z+.5-p.width/2,x+.5+p.width/2,y+p.height,z+.5+p.width/2);if(worldObj.getCollidingBoundingBoxes(p,box).isEmpty()){p.setPositionAndUpdate(x+.5,y,z+.5);return;}
        }
        p.setPositionAndUpdate(posX,boundingBox.maxY,posZ);
    }
    private final class SeekLava extends EntityAIBase {
        double x,y,z;SeekLava(){setMutexBits(1);}
        public boolean shouldExecute(){if(riddenByEntity!=null||!isCold()||ticksExisted%40!=getEntityId()%40)return false;
            int bx=MathHelper.floor_double(posX),by=MathHelper.floor_double(boundingBox.minY),bz=MathHelper.floor_double(posZ);
            for(int i=0;i<32;i++){int px=bx+rand.nextInt(17)-8,pz=bz+rand.nextInt(17)-8;for(int py=by+2;py>=by-2;py--)if(worldObj.blockExists(px,py-1,pz)&&worldObj.getBlock(px,py-1,pz).getMaterial()==Material.lava&&lavaNavigator.walkable(px,py,pz)){x=px+.5;y=py;z=pz+.5;return true;}}return false;}
        public void startExecuting(){getNavigator().tryMoveToXYZ(x,y,z,1);}
        public boolean continueExecuting(){return riddenByEntity==null&&isCold()&&!getNavigator().noPath();}
    }
    @Override protected void dropFewItems(boolean hit,int looting){if(!isChild())dropItem(Items.string,2+rand.nextInt(4)+Math.round(rand.nextFloat()*looting));if(isSaddled())dropItem(Items.saddle,1);}
    @Override protected String getLivingSound(){return "htgt6:entity.strider.ambient";}
    @Override protected String getHurtSound(){return "htgt6:entity.strider.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.strider.death";}
    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block b){playSound("htgt6:entity.strider."+(warm()?"step_lava":"step"),1,1);}
    @Override public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);n.setBoolean("Saddle",isSaddled());n.setInteger("PendingJockey",pendingJockey);}
    @Override public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);setSaddled(n.getBoolean("Saddle"));pendingJockey=MathHelper.clamp_int(n.getInteger("PendingJockey"),0,2);}
}
