package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Shared fish behavior; schooling navigation remains a separate backport task. */
public abstract class EntityAquaticFish120 extends EntityWaterMob {
    private int swimChange;
    private boolean fromBucket;
    protected EntityAquaticFish120(World world,float width,float height,int health){super(world);setSize(width,height);setHealth(health);experienceValue=1;}
    public abstract int species();
    /** Fish-item metadata and bucket metadata use different orders for the last two species. */
    public final int bucketMetadata(){return species()==2?3:species()==3?2:species();}
    @Override protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.maxHealth).setBaseValue(3.0D);
    }
    @Override protected boolean canDespawn(){return !fromBucket&&super.canDespawn();}

    @Override public boolean handleWaterMovement(){
        // Entity 1.7.10 shrinks the box by 0.4 on EACH vertical side. For a fish
        // shorter than 0.8 this inverts the test box and intermittently reports dry land.
        inWater=worldObj.handleMaterialAcceleration(boundingBox.contract(.001D,.001D,.001D),
                net.minecraft.block.material.Material.water,this);
        if(inWater){fallDistance=0;extinguish();}
        return inWater;
    }

    /** Capturing is an interaction, never damage/death, so no raw-fish drop is produced. */
    @Override protected boolean interact(EntityPlayer player){
        ItemStack held=player.getHeldItem();
        if(isDead||getHealth()<=0||held==null||held.stackSize<=0||held.getItem()!=Items.water_bucket)return super.interact(player);
        if(worldObj.isRemote)return true;
        ItemStack bucket=new ItemStack(OceanContent.fishBucket,1,bucketMetadata());
        writeToBucket(bucket);
        if(player.capabilities.isCreativeMode){
            if(!player.inventory.addItemStackToInventory(bucket))player.dropPlayerItemWithRandomChoice(bucket,false);
        }else if(--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,bucket);
        else if(!player.inventory.addItemStackToInventory(bucket))player.dropPlayerItemWithRandomChoice(bucket,false);
        player.inventory.markDirty();
        worldObj.playSoundAtEntity(this,"random.pop",1.0F,1.0F);
        setDead();
        com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete(player,"tactical_fishing");
        return true;
    }

    public void writeToBucket(ItemStack bucket){
        NBTTagCompound root=bucket.hasTagCompound()?(NBTTagCompound)bucket.getTagCompound().copy():new NBTTagCompound();
        NBTTagCompound entity=new NBTTagCompound();entity.setBoolean("FromBucket",true);
        entity.setFloat("Health",getHealth());
        if(hasCustomNameTag())entity.setString("CustomName",getCustomNameTag());
        writeBucketVariant(entity);root.setTag("BucketEntityTag",entity);bucket.setTagCompound(root);
        if(hasCustomNameTag())bucket.setStackDisplayName(getCustomNameTag());
    }

    public void readFromBucket(ItemStack bucket){
        fromBucket=true;
        if(bucket==null||!bucket.hasTagCompound())return;
        NBTTagCompound entity=bucket.getTagCompound().getCompoundTag("BucketEntityTag");
        if(entity.hasKey("Health",99)){
            float health=entity.getFloat("Health");
            if(!Float.isNaN(health)&&!Float.isInfinite(health))setHealth(Math.min(getMaxHealth(),Math.max(.1F,health)));
        }
        if(entity.hasKey("CustomName",8))setCustomNameTag(entity.getString("CustomName"));
        if(bucket.hasDisplayName())setCustomNameTag(bucket.getDisplayName());
        if(bucket.getTagCompound().hasKey("BucketEntityTag",10))readBucketVariant(entity);
    }

    protected void writeBucketVariant(NBTTagCompound tag){}
    protected void readBucketVariant(NBTTagCompound tag){}
    @Override public void onLivingUpdate(){
        updateFishMotion();
        super.onLivingUpdate();
    }
    private void updateFishMotion(){
        if(worldObj.isRemote||!isEntityAlive())return;
        if(isInWater()){
            if(--swimChange<=0){swimChange=20+rand.nextInt(40);motionX+=(rand.nextDouble()-.5D)*.12D;motionY+=(rand.nextDouble()-.5D)*.06D;motionZ+=(rand.nextDouble()-.5D)*.12D;}
            rotationYaw=(float)(Math.atan2(motionZ,motionX)*180D/Math.PI)-90F;
        }else if(onGround&&isCollidedVertically){
            motionX+=(rand.nextFloat()*2F-1F)*.05F;
            motionY+=.4F;
            motionZ+=(rand.nextFloat()*2F-1F)*.05F;
            onGround=false;isAirBorne=true;
            worldObj.playSoundAtEntity(this,"htgt6:"+soundPrefix()+"flop",1F,1F);
        }
    }
    @Override public void moveEntityWithHeading(float strafe,float forward){
        if(!worldObj.isRemote&&isInWater()){
            moveFlying(strafe,forward,.01F);
            moveEntity(motionX,motionY,motionZ);
            motionX*=.9D;motionY*=.9D;motionZ*=.9D;
            if(getAttackTarget()==null)motionY-=.005D;
        }else super.moveEntityWithHeading(strafe,forward);
    }
    private String soundPrefix(){return species()==0?"entity.cod.":species()==1?"entity.salmon.":species()==2?"entity.tropical_fish.":"entity.puffer_fish.";}
    @Override protected String getLivingSound(){return "htgt6:"+soundPrefix()+"ambient";}
    @Override protected String getHurtSound(){return "htgt6:"+soundPrefix()+"hurt";}
    @Override protected String getDeathSound(){return "htgt6:"+soundPrefix()+"death";}
    @Override protected Item getDropItem(){return Items.fish;}
    @Override protected void dropFewItems(boolean hit,int looting){
        // 1.20.2 fish loot tables: one fish (no Looting multiplier), plus 5% bone meal.
        entityDropItem(new ItemStack(isBurning()&&species()<2?Items.cooked_fished:Items.fish,1,species()),0F);
        if(rand.nextFloat()<.05F)entityDropItem(new ItemStack(Items.dye,1,15),0F);
    }
    @Override public boolean attackEntityFrom(DamageSource source,float amount){return super.attackEntityFrom(source,amount);}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setByte("FishSpecies",(byte)species());tag.setBoolean("FromBucket",fromBucket);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);fromBucket=tag.getBoolean("FromBucket");}

    public static final class Cod extends EntityAquaticFish120 { public Cod(World w){super(w,.5F,.3F,3);}public int species(){return 0;} }
    public static final class Salmon extends EntityAquaticFish120 { public Salmon(World w){super(w,.7F,.4F,3);}public int species(){return 1;} }
    public static final class Pufferfish extends EntityAquaticFish120 {
        private static final int PUFF_STATE=20;
        private int inflateCounter,deflateTimer;
        public Pufferfish(World w){super(w,.7F,.7F,3);refreshPuffSize();}public int species(){return 3;}
        @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(PUFF_STATE,Byte.valueOf((byte)0));}
        public int getPuffState(){return Math.max(0,Math.min(2,dataWatcher.getWatchableObjectByte(PUFF_STATE)));}
        public void setPuffState(int state){dataWatcher.updateObject(PUFF_STATE,Byte.valueOf((byte)Math.max(0,Math.min(2,state))));refreshPuffSize();}
        public static float puffScale(int state){return state==0?.5F:state==1?.7F:1F;}
        private void refreshPuffSize(){float size=.7F*puffScale(getPuffState());setSize(size,size);}
        @Override public void func_145781_i(int id){super.func_145781_i(id);if(id==PUFF_STATE)refreshPuffSize();}
        @Override public void onLivingUpdate(){super.onLivingUpdate();tickPuff();}
        private boolean scary(net.minecraft.entity.EntityLivingBase entity){
            return entity!=this&&entity.isEntityAlive()
                    &&!(entity instanceof EntityPlayer&&((EntityPlayer)entity).capabilities.isCreativeMode)
                    &&!(entity instanceof EntityWaterMob)
                    &&!entity.isCreatureType(net.minecraft.entity.EnumCreatureType.waterCreature,false);
        }
        private void tickPuff(){
            if(worldObj.isRemote||!isEntityAlive())return;
            boolean danger=false;
            for(Object value:worldObj.getEntitiesWithinAABB(net.minecraft.entity.EntityLivingBase.class,boundingBox.expand(2,2,2)))
                if(scary((net.minecraft.entity.EntityLivingBase)value)){danger=true;break;}
            if(danger){
                if(inflateCounter==0){inflateCounter=1;deflateTimer=0;}
                if(getPuffState()==0)changePuff(1,true);
                else if(inflateCounter>40&&getPuffState()==1)changePuff(2,true);
                ++inflateCounter;
            }else{
                inflateCounter=0;
                if(getPuffState()>0){
                    if(deflateTimer>60&&getPuffState()==2)changePuff(1,false);
                    else if(deflateTimer>100&&getPuffState()==1)changePuff(0,false);
                    ++deflateTimer;
                }
            }
            if(getPuffState()>0)for(Object value:worldObj.getEntitiesWithinAABB(net.minecraft.entity.EntityLiving.class,boundingBox.expand(.3,.3,.3)))
                if(scary((net.minecraft.entity.EntityLivingBase)value))sting((net.minecraft.entity.EntityLivingBase)value);
        }
        private void changePuff(int state,boolean inflate){
            setPuffState(state);
            worldObj.playSoundAtEntity(this,"htgt6:entity.puffer_fish."+(inflate?"blow_up":"blow_out"),1F,1F);
        }
        private void sting(net.minecraft.entity.EntityLivingBase target){
            int state=getPuffState();
            if(worldObj.isRemote||!isEntityAlive()||state==0||!target.isEntityAlive())return;
            if(target.attackEntityFrom(DamageSource.causeMobDamage(this),1+state)){
                target.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.poison.id,60*state,0));
                worldObj.playSoundAtEntity(this,"htgt6:entity.puffer_fish.sting",1F,1F);
            }
        }
        @Override public void onCollideWithPlayer(EntityPlayer player){
            if(!player.capabilities.isCreativeMode)sting(player);
        }
        @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("PuffState",getPuffState());}
        @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setPuffState(tag.getInteger("PuffState"));inflateCounter=deflateTimer=0;}
        @Override protected void writeBucketVariant(NBTTagCompound tag){tag.setInteger("PuffState",getPuffState());}
        @Override protected void readBucketVariant(NBTTagCompound tag){setPuffState(tag.getInteger("PuffState"));inflateCounter=deflateTimer=0;}
    }
    public static final class Tropical extends EntityAquaticFish120 {
        private static final int VARIANT=20;
        public Tropical(World w){super(w,.5F,.4F,3);if(!w.isRemote)setVariant(rand.nextInt(12));}public int species(){return 2;}
        @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(VARIANT,Byte.valueOf((byte)0));}
        public int getVariant(){return Math.abs(dataWatcher.getWatchableObjectByte(VARIANT)%12);}
        public void setVariant(int variant){dataWatcher.updateObject(VARIANT,Byte.valueOf((byte)Math.abs(variant%12)));}
        @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setByte("Variant",(byte)getVariant());}
        @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);readBucketVariant(tag);}
        @Override protected void writeBucketVariant(NBTTagCompound tag){tag.setByte("Variant",(byte)getVariant());}
        @Override protected void readBucketVariant(NBTTagCompound tag){if(tag.hasKey("Variant",99))setVariant(tag.getInteger("Variant"));}
    }
}
