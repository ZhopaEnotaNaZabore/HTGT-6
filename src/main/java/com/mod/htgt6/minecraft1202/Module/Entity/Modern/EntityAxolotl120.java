package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** 1.20 axolotl adapted to the legacy aquatic movement and data-watcher protocol. */
public final class EntityAxolotl120 extends EntityWaterMob {
    private static final int VARIANT = 20;
    private static final int PLAYING_DEAD = 21;
    private int dryTicks;
    private int playDeadTicks;
    private int swimChange;

    public EntityAxolotl120(World world) {
        super(world);
        setSize(0.75F, 0.42F);
    }

    @Override protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(VARIANT, Byte.valueOf((byte)0));
        dataWatcher.addObject(PLAYING_DEAD, Byte.valueOf((byte)0));
    }

    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(14.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
    }

    public int getVariant() { return dataWatcher.getWatchableObjectByte(VARIANT) & 255; }
    public void setVariant(int value) { dataWatcher.updateObject(VARIANT, Byte.valueOf((byte)MathHelper.clamp_int(value,0,4))); }
    public boolean isPlayingDead() { return dataWatcher.getWatchableObjectByte(PLAYING_DEAD) != 0; }

    private void setPlayingDead(boolean value) {
        dataWatcher.updateObject(PLAYING_DEAD, Byte.valueOf((byte)(value ? 1 : 0)));
    }

    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data=super.onSpawnWithEgg(data);
        // Blue is the modern rare mutation; the four common skins are equiprobable.
        setVariant(rand.nextInt(1200)==0 ? 4 : rand.nextInt(4));
        return data;
    }

    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        if (isInWater()) {
            dryTicks=0;
            setAir(300);
            if (!isPlayingDead()) {
                if (--swimChange<=0) { rotationYaw+=rand.nextFloat()*120.0F-60.0F; swimChange=30+rand.nextInt(70); }
                float yaw=rotationYaw*(float)Math.PI/180.0F;
                motionX+=-MathHelper.sin(yaw)*0.018D;
                motionZ+=MathHelper.cos(yaw)*0.018D;
                motionY+=(rand.nextFloat()-0.48F)*0.012D;
                if (!worldObj.isRemote && ticksExisted%20==0) attackNearbyAquatic();
            }
            motionX*=0.90D; motionY*=0.90D; motionZ*=0.90D;
        } else if (!worldObj.isRemote && ++dryTicks>6000 && dryTicks%20==0) {
            attackEntityFrom(DamageSource.drown,1.0F);
        }
        if (!worldObj.isRemote && playDeadTicks>0) {
            setPlayingDead(true);
            motionX*=0.5D; motionY*=0.5D; motionZ*=0.5D;
            if (--playDeadTicks==0) setPlayingDead(false);
        }
    }

    private void attackNearbyAquatic() {
        java.util.List list=worldObj.getEntitiesWithinAABB(EntitySquid.class,boundingBox.expand(6.0D,3.0D,6.0D));
        if (list.isEmpty()) return;
        Entity target=(Entity)list.get(0);
        double dx=target.posX-posX, dy=target.posY-posY, dz=target.posZ-posZ;
        double distance=Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(distance>0.001D){motionX+=dx/distance*0.025D;motionY+=dy/distance*0.025D;motionZ+=dz/distance*0.025D;}
        if(distance<1.2D)attackEntityAsMob(target);
    }

    @Override public boolean attackEntityAsMob(Entity target){
        return target.attackEntityFrom(DamageSource.causeMobDamage(this),2.0F);
    }

    @Override public boolean attackEntityFrom(DamageSource source,float amount) {
        boolean hurt=super.attackEntityFrom(source,amount);
        if(hurt&&!worldObj.isRemote&&getHealth()>0.0F&&getHealth()<getMaxHealth()/2.0F&&rand.nextInt(3)==0){
            playDeadTicks=200; setPlayingDead(true);
        }
        return hurt;
    }

    @Override public boolean getCanSpawnHere() {
        int x=MathHelper.floor_double(posX), y=MathHelper.floor_double(boundingBox.minY), z=MathHelper.floor_double(posZ);
        return worldObj.getBlock(x,y,z).getMaterial()==net.minecraft.block.material.Material.water
                && (worldObj.getBlock(x,y-1,z)==Blocks.clay || nearbyClay(x,y,z))
                && super.getCanSpawnHere();
    }

    private boolean nearbyClay(int x,int y,int z) {
        for(int dx=-2;dx<=2;dx++)for(int dy=-2;dy<=1;dy++)for(int dz=-2;dz<=2;dz++)
            if(worldObj.getBlock(x+dx,y+dy,z+dz)==Blocks.clay)return true;
        return false;
    }

    @Override public boolean canBreatheUnderwater(){return true;}
    @Override public boolean isPushedByWater(){return false;}
    @Override protected String getLivingSound(){return null;}
    @Override protected String getHurtSound(){return "mob.silverfish.hit";}
    @Override protected String getDeathSound(){return "mob.silverfish.kill";}
    @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Variant",getVariant());tag.setInteger("DryTicks",dryTicks);tag.setInteger("PlayingDeadTicks",playDeadTicks);}
    @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);setVariant(tag.getInteger("Variant"));dryTicks=tag.getInteger("DryTicks");playDeadTicks=tag.getInteger("PlayingDeadTicks");setPlayingDead(playDeadTicks>0);}
}
