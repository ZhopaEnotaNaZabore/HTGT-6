package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.UUID;

/** Minecraft 1.20.2 Allay adapted to legacy flying movement, equipment sync and task AI. */
public final class EntityAllay120 extends EntityCreature {
    public static final int FLAGS_WATCHER=20;
    public static final int FLAG_DANCING=1;
    public static final int FLAG_CAN_DUPLICATE=2;
    public static final int DUPLICATION_COOLDOWN=6000;
    public static final double MAX_HEALTH=20.0D;
    public static final double MOVEMENT_SPEED=0.10D;
    public static final String NBT_INVENTORY="Inventory";
    public static final String NBT_LIKED_PLAYER="LikedPlayer";
    public static final String NBT_DUPLICATION_COOLDOWN="DuplicationCooldown";
    public static final String NBT_CAN_DUPLICATE="CanDuplicate";
    private UUID likedPlayer;
    private int duplicationCooldown;

    public EntityAllay120(World world){
        super(world);setSize(0.35F,0.6F);experienceValue=0;
        tasks.addTask(1,new EntityAIAllayCollect120(this));
        tasks.addTask(2,new FollowLikedPlayer());
        tasks.addTask(3,new FlyingWander());
    }

    @Override protected void entityInit(){
        super.entityInit();dataWatcher.addObject(FLAGS_WATCHER,Byte.valueOf((byte)FLAG_CAN_DUPLICATE));
    }

    @Override protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(MOVEMENT_SPEED);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(48.0D);
    }

    @Override protected boolean isAIEnabled(){return true;}
    @Override protected boolean canDespawn(){return false;}
    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean isPushedByWater(){return false;}
    @Override protected void fall(float distance){}
    @Override public boolean attackEntityFrom(DamageSource source,float amount){
        return source==DamageSource.fall?false:super.attackEntityFrom(source,amount);
    }

    public ItemStack getCarriedStack(){return getEquipmentInSlot(0);}
    public void setCarriedStack(ItemStack stack){setCurrentItemOrArmor(0,stack);}
    public UUID getLikedPlayerId(){return likedPlayer;}
    public void setLikedPlayer(EntityPlayer player){likedPlayer=player==null?null:player.getUniqueID();}
    public boolean isDancing(){return flag(FLAG_DANCING);}
    public boolean canDuplicate(){return flag(FLAG_CAN_DUPLICATE);}
    private boolean flag(int bit){return (dataWatcher.getWatchableObjectByte(FLAGS_WATCHER)&bit)!=0;}
    private void setAllayFlag(int bit,boolean value){int flags=dataWatcher.getWatchableObjectByte(FLAGS_WATCHER)&255;
        dataWatcher.updateObject(FLAGS_WATCHER,Byte.valueOf((byte)(value?flags|bit:flags&~bit)));}

    public EntityPlayer likedPlayer(){
        if(likedPlayer==null)return null;
        for(Object raw:worldObj.playerEntities){EntityPlayer player=(EntityPlayer)raw;
            if(likedPlayer.equals(player.getUniqueID()))return player;}
        return null;
    }

    @Override public void onLivingUpdate(){
        super.onLivingUpdate();fallDistance=0.0F;
        if(!worldObj.isRemote){
            if(duplicationCooldown>0)--duplicationCooldown;
            setAllayFlag(FLAG_CAN_DUPLICATE,duplicationCooldown<=0);
            if(ticksExisted%20==0)setAllayFlag(FLAG_DANCING,hasNearbyPlayingJukebox());
        }
    }

    private boolean hasNearbyPlayingJukebox(){
        int ox=MathHelper.floor_double(posX),oy=MathHelper.floor_double(posY),oz=MathHelper.floor_double(posZ);
        for(int x=ox-6;x<=ox+6;x++)for(int y=oy-6;y<=oy+6;y++)for(int z=oz-6;z<=oz+6;z++)
            if(worldObj.getBlock(x,y,z)==Blocks.jukebox){TileEntity tile=worldObj.getTileEntity(x,y,z);
                if(tile!=null&&tile.getClass().getName().endsWith("TileEntityJukebox")
                        &&tile.getBlockMetadata()>0)return true;}
        return false;
    }

    @Override public boolean interact(EntityPlayer player){
        ItemStack hand=player.inventory.getCurrentItem();
        if(hand!=null&&hand.getItem()==Backport120Content.amethystShard&&isDancing()&&canDuplicate()){
            if(!worldObj.isRemote){EntityAllay120 copy=new EntityAllay120(worldObj);
                copy.setLocationAndAngles(posX,posY,posZ,rotationYaw,rotationPitch);copy.likedPlayer=likedPlayer;
                copy.duplicationCooldown=DUPLICATION_COOLDOWN;copy.setAllayFlag(FLAG_CAN_DUPLICATE,false);
                copy.func_110163_bv();worldObj.spawnEntityInWorld(copy);duplicationCooldown=DUPLICATION_COOLDOWN;
                setAllayFlag(FLAG_CAN_DUPLICATE,false);if(!player.capabilities.isCreativeMode&&--hand.stackSize<=0)
                    player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
                playSound("htgt6:entity.allay.duplicate",1.0F,1.0F);}return true;
        }
        ItemStack carried=getCarriedStack();
        if(carried==null&&hand!=null){
            if(!worldObj.isRemote){ItemStack filter=hand.copy();filter.stackSize=1;setCarriedStack(filter);setLikedPlayer(player);func_110163_bv();
                if(!player.capabilities.isCreativeMode&&--hand.stackSize<=0)
                    player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
                playSound("htgt6:entity.allay.item_given",1.0F,1.0F);}return true;
        }
        if(carried!=null&&hand==null&&likedPlayer!=null&&likedPlayer.equals(player.getUniqueID())){
            if(!worldObj.isRemote){setCarriedStack(null);likedPlayer=null;
                if(!player.inventory.addItemStackToInventory(carried))player.dropPlayerItemWithRandomChoice(carried,false);
                playSound("htgt6:entity.allay.item_taken",1.0F,1.0F);}return true;
        }
        return super.interact(player);
    }

    public static boolean matchesFilter(ItemStack filter,ItemStack candidate){
        return filter!=null&&candidate!=null&&filter.getItem()!=null&&filter.getItem()==candidate.getItem()
                &&filter.getItemDamage()==candidate.getItemDamage()
                &&ItemStack.areItemStackTagsEqual(filter,candidate);
    }

    public void flyToward(double x,double y,double z,double speed){
        double dx=x-posX,dy=y-posY,dz=z-posZ,length=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(length<0.001D)return;motionX+=dx/length*speed;motionY+=dy/length*speed;motionZ+=dz/length*speed;
        double horizontal=Math.sqrt(dx*dx+dz*dz);if(horizontal>0.001D)rotationYaw=(float)(Math.atan2(dz,dx)*180.0D/Math.PI)-90.0F;
    }

    @Override public void moveEntityWithHeading(float strafe,float forward){
        if(isInWater()){moveFlying(strafe,forward,0.02F);moveEntity(motionX,motionY,motionZ);motionX*=0.8D;motionY*=0.8D;motionZ*=0.8D;}
        else if(handleLavaMovement()){moveFlying(strafe,forward,0.02F);moveEntity(motionX,motionY,motionZ);motionX*=0.5D;motionY*=0.5D;motionZ*=0.5D;}
        else{moveFlying(strafe,forward,0.02F);moveEntity(motionX,motionY,motionZ);motionX*=0.91D;motionY*=0.91D;motionZ*=0.91D;}
    }

    @Override protected void dropFewItems(boolean hit,int looting){ItemStack carried=getCarriedStack();
        if(carried!=null){entityDropItem(carried.copy(),0.0F);setCarriedStack(null);}}
    @Override protected String getLivingSound(){return getCarriedStack()==null
            ?"htgt6:entity.allay.ambient_without_item":"htgt6:entity.allay.ambient_with_item";}
    @Override protected String getHurtSound(){return "htgt6:entity.allay.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.allay.death";}

    @Override public void writeEntityToNBT(NBTTagCompound tag){
        super.writeEntityToNBT(tag);ItemStack carried=getCarriedStack();if(carried!=null){NBTTagCompound item=new NBTTagCompound();
            carried.writeToNBT(item);item.setByte("Slot",(byte)0);NBTTagList inventory=new NBTTagList();inventory.appendTag(item);
            tag.setTag(NBT_INVENTORY,inventory);}if(likedPlayer!=null)tag.setString(NBT_LIKED_PLAYER,likedPlayer.toString());
        tag.setLong(NBT_DUPLICATION_COOLDOWN,duplicationCooldown);tag.setBoolean(NBT_CAN_DUPLICATE,canDuplicate());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){
        super.readEntityFromNBT(tag);ItemStack carried=null;if(tag.hasKey(NBT_INVENTORY,9)){
            NBTTagList inventory=tag.getTagList(NBT_INVENTORY,10);if(inventory.tagCount()>0)
                carried=ItemStack.loadItemStackFromNBT(inventory.getCompoundTagAt(0));}
        if(carried==null&&tag.hasKey("HeldItem",10))carried=ItemStack.loadItemStackFromNBT(tag.getCompoundTag("HeldItem"));
        setCarriedStack(carried);try{likedPlayer=tag.hasKey(NBT_LIKED_PLAYER,8)?UUID.fromString(tag.getString(NBT_LIKED_PLAYER)):null;
        }catch(IllegalArgumentException ignored){likedPlayer=null;}
        duplicationCooldown=(int)Math.min(Integer.MAX_VALUE,Math.max(0L,tag.getLong(NBT_DUPLICATION_COOLDOWN)));
        setAllayFlag(FLAG_CAN_DUPLICATE,duplicationCooldown<=0&&(!tag.hasKey(NBT_CAN_DUPLICATE)||tag.getBoolean(NBT_CAN_DUPLICATE)));}

    private final class FollowLikedPlayer extends EntityAIBase{
        private EntityPlayer owner;FollowLikedPlayer(){setMutexBits(1);}
        @Override public boolean shouldExecute(){owner=likedPlayer();return owner!=null&&getDistanceSqToEntity(owner)>9.0D;}
        @Override public boolean continueExecuting(){return owner!=null&&!owner.isDead&&getDistanceSqToEntity(owner)>4.0D;}
        @Override public void updateTask(){flyToward(owner.posX,owner.posY+1.2D,owner.posZ,0.06D);
            ItemStack carried=getCarriedStack();if(carried!=null&&carried.stackSize>1&&getDistanceSqToEntity(owner)<6.25D){
                ItemStack delivery=carried.copy();delivery.stackSize=carried.stackSize-1;carried.stackSize=1;setCarriedStack(carried);
                Entity item=new net.minecraft.entity.item.EntityItem(worldObj,posX,posY,posZ,delivery);worldObj.spawnEntityInWorld(item);
                playSound("htgt6:entity.allay.item_thrown",1.0F,1.0F);}}
        @Override public void resetTask(){owner=null;}
    }

    private final class FlyingWander extends EntityAIBase{
        private double x,y,z;private int ticks;FlyingWander(){setMutexBits(1);}
        @Override public boolean shouldExecute(){return rand.nextInt(20)==0;}
        @Override public void startExecuting(){x=posX+rand.nextInt(13)-6;y=posY+rand.nextInt(7)-3;z=posZ+rand.nextInt(13)-6;ticks=30;}
        @Override public boolean continueExecuting(){return ticks-->0;}
        @Override public void updateTask(){flyToward(x,y,z,0.025D);}
    }
}
