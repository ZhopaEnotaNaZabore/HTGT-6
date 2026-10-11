package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** Lightweight 1.20.2 parrot backport with five variants, taming and gliding. */
public final class EntityParrot120 extends EntityTameable {
    public static final int VARIANT_WATCHER=20;
    private boolean creativeDamage;
    public float flap,flapSpeed,previousFlap,previousFlapSpeed;
    private float flapping=1.0F;
    public void updateFlapping(){
        previousFlap=flap;previousFlapSpeed=flapSpeed;
        flapSpeed=net.minecraft.util.MathHelper.clamp_float(flapSpeed+(onGround||ridingEntity!=null?-.3F:1.2F),0,1);
        if(!onGround&&flapping<1)flapping=1;
        flapping*=.9F;
        if(!onGround&&motionY<0){motionY*=.6D;fallDistance=0;}
        flap+=flapping*2;
    }
    public float flapAmount(float partial){
        return (net.minecraft.util.MathHelper.sin(previousFlap+(flap-previousFlap)*partial)+1)
                *(previousFlapSpeed+(flapSpeed-previousFlapSpeed)*partial);
    }
    private boolean party;
    private int jukeboxX,jukeboxY,jukeboxZ;
    public void setRecordPlayingNearby(int x,int y,int z,boolean playing){
        if(!playing&&party&&(x!=jukeboxX||y!=jukeboxY||z!=jukeboxZ))return;
        jukeboxX=x;jukeboxY=y;jukeboxZ=z;party=playing;
    }
    public boolean isPartyParrot(){return party;}
    public void updatePartyState(){
        if(party&&(getDistanceSq(jukeboxX+.5,jukeboxY+.5,jukeboxZ+.5)>=3.46D*3.46D
                ||worldObj.getBlock(jukeboxX,jukeboxY,jukeboxZ)!=net.minecraft.init.Blocks.jukebox))party=false;
    }
    public EntityParrot120(World world){
        super(world);setSize(.5F,.9F);
        tasks.addTask(0,new EntityAISwimming(this));
        tasks.addTask(1,aiSit);
        tasks.addTask(2,new EntityAITempt(this,1.0D,Items.wheat_seeds,false));
        tasks.addTask(3,new EntityAIFollowOwner(this,1.0D,3.0F,1.0F));
        tasks.addTask(4,new EntityAIWander(this,1.0D));
        tasks.addTask(5,new EntityAIWatchClosest(this,EntityPlayer.class,8.0F));
        tasks.addTask(6,new EntityAILookIdle(this));
    }
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(VARIANT_WATCHER,Byte.valueOf((byte)0));}
    @Override protected void applyEntityAttributes(){super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(6.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.2D);}
    @Override protected boolean isAIEnabled(){return true;}
    public int getVariant(){return dataWatcher.getWatchableObjectByte(VARIANT_WATCHER)&7;}
    public void setVariant(int value){dataWatcher.updateObject(VARIANT_WATCHER,Byte.valueOf((byte)Math.max(0,Math.min(4,value))));}
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data){data=super.onSpawnWithEgg(data);setVariant(rand.nextInt(5));return data;}

    @Override public void onLivingUpdate(){
        updatePartyState();
        super.onLivingUpdate();
        updateFlapping();
    }
    @Override protected void fall(float distance){ }

    @Override public boolean interact(EntityPlayer player){
        ItemStack held=player.inventory.getCurrentItem();
        if(held!=null&&held.getItem()==Items.cookie){
            if(worldObj.isRemote)return true;
            if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)
                player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
            addPotionEffect(new PotionEffect(Potion.poison.id,900,0));
            if(player.capabilities.isCreativeMode||!isEntityInvulnerable())
                attackEntityFrom(net.minecraft.util.DamageSource.causePlayerDamage(player),Float.MAX_VALUE);
            return true;
        }
        if(!isTamed()&&isTamingSeed(held)){
            if(worldObj.isRemote)return true;
            if(!player.capabilities.isCreativeMode&&--held.stackSize<=0)
                player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
            if(!worldObj.isRemote){
                if(rand.nextInt(10)==0){setTamed(true);func_152115_b(player.getUniqueID().toString());
                    getNavigator().clearPathEntity();playTameEffect(true);worldObj.setEntityState(this,(byte)7);}
                else{playTameEffect(false);worldObj.setEntityState(this,(byte)6);}
            }
            return true;
        }
        if(onGround&&isTamed()&&func_152114_e(player)){
            if(!worldObj.isRemote)aiSit.setSitting(!isSitting());
            return true;
        }
        return super.interact(player);
    }
    private static boolean isTamingSeed(ItemStack stack){if(stack==null)return false;Item item=stack.getItem();
        return item==Items.wheat_seeds||item==Items.melon_seeds||item==Items.pumpkin_seeds
                ||item==Item.itemRegistry.getObject("Minecraft_1.20.2:beetroot_seeds");}
    @Override public boolean isChild(){return false;}
    @Override public boolean canMateWith(net.minecraft.entity.passive.EntityAnimal other){return false;}
    @Override public boolean isEntityInvulnerable(){return !creativeDamage&&super.isEntityInvulnerable();}
    @Override public boolean attackEntityFrom(net.minecraft.util.DamageSource source,float amount){
        boolean previous=creativeDamage;
        // Modern source-aware invulnerability permits attacks from creative players.
        // Legacy EntityLivingBase calls a parameterless predicate, so scope the bypass to this call.
        if(source.getEntity() instanceof EntityPlayer
                &&((EntityPlayer)source.getEntity()).capabilities.isCreativeMode)creativeDamage=true;
        try {
            if(isEntityInvulnerable())return false;
            if(!worldObj.isRemote)aiSit.setSitting(false);
            return super.attackEntityFrom(source,amount);
        } finally {creativeDamage=previous;}
    }
    @Override public boolean isBreedingItem(ItemStack stack){return false;}
    @Override public EntityParrot120 createChild(EntityAgeable mate){return null;}
    @Override protected String getLivingSound(){return "mob.chicken.say";}
    @Override protected String getHurtSound(){return "mob.chicken.hurt";}
    @Override protected String getDeathSound(){return "mob.chicken.hurt";}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Variant",getVariant());}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setVariant(tag.getInteger("Variant"));}
}
