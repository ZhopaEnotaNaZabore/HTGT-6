package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** Dolphin with air management, swimming and Dolphin's Grace. */
public final class EntityDolphin120 extends EntityCreature implements IAnimals {
    private static final int MOISTNESS=20;
    private static final net.minecraft.util.DamageSource DRY_OUT=new net.minecraft.util.DamageSource("htgt6.dry_out");
    private int swimChange;
    private EntityPlayer graceTarget;
    private final DolphinAirNavigation120 airNavigation=new DolphinAirNavigation120(this);
    public EntityDolphin120(World world){super(world);setSize(.9F,.6F);setHealth(10);setAir(4800);experienceValue=1;}
    @Override protected void entityInit(){super.entityInit();dataWatcher.addObject(MOISTNESS,Integer.valueOf(2400));}
    public int getMoistness(){return dataWatcher.getWatchableObjectInt(MOISTNESS);}
    public void setMoistness(int value){dataWatcher.updateObject(MOISTNESS,Integer.valueOf(Math.max(0,Math.min(2400,value))));}
    @Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setInteger("Moistness",getMoistness());}
    @Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag){
        super.readEntityFromNBT(tag);setMoistness(tag.hasKey("Moistness",99)?tag.getInteger("Moistness"):2400);
    }
    @Override protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.maxHealth).setBaseValue(10D);
    }
    @Override public boolean isCreatureType(EnumCreatureType type,boolean forSpawnCount){return type==EnumCreatureType.waterCreature;}
    @Override public boolean getCanSpawnHere(){return worldObj.checkNoEntityCollision(boundingBox);}
    @Override public int getTalkInterval(){return 120;}
    @Override public float getEyeHeight(){return .3F;}
    @Override public boolean handleWaterMovement(){
        inWater=worldObj.handleMaterialAcceleration(boundingBox.contract(.001,.001,.001),net.minecraft.block.material.Material.water,this);
        if(inWater){fallDistance=0;extinguish();}
        return inWater;
    }
    @Override public void onEntityUpdate(){
        // EntityWaterMob resets air to 300 under water and suffocates on land.
        // Use the ordinary air-breathing lifecycle, with the modern surface reserve.
        super.onEntityUpdate();
        if(isEntityAlive()&&!isInsideOfMaterial(net.minecraft.block.material.Material.water))setAir(4800);
        updateMoistness();
    }
    private void updateMoistness(){
        if(worldObj.isRemote||!isEntityAlive())return;
        if(isWet())setMoistness(2400);
        else{
            setMoistness(getMoistness()-1);
            if(getMoistness()==0)attackEntityFrom(DRY_OUT,1F);
            if(onGround){
                motionX+=(rand.nextFloat()*2F-1F)*.2F;motionY+=.5D;motionZ+=(rand.nextFloat()*2F-1F)*.2F;
                rotationYaw=rand.nextFloat()*360F;onGround=false;isAirBorne=true;
            }
        }
    }
    @Override public void onLivingUpdate(){
        if(!worldObj.isRemote&&isInWater()){
            updateSwimmingIntent();
        }
        else {graceTarget=null;airNavigation.clear();}
        super.onLivingUpdate();
    }
    private void updateSwimmingIntent(){
        if(worldObj.isRemote||!isEntityAlive()){airNavigation.clear();graceTarget=null;return;}
        // Breathing takes precedence over wandering and helping a swimmer.
        if(getAir()<140){airNavigation.tick();graceTarget=null;return;}
        airNavigation.clear();
        if(--swimChange<=0){swimChange=15+rand.nextInt(30);motionX+=(rand.nextDouble()-.5D)*.16D;motionY+=(rand.nextDouble()-.35D)*.08D;motionZ+=(rand.nextDouble()-.5D)*.16D;}
        updateGrace();
    }
    @Override protected void updateEntityActionState(){
        if(isInWater()&&getAir()<140){
            // Keep the legacy land navigator and random jumping out of the air route.
            ++entityAge;despawnEntity();moveForward=moveStrafing=0F;isJumping=false;
        }else super.updateEntityActionState();
    }
    @Override public void moveEntityWithHeading(float strafe,float forward){
        if(!worldObj.isRemote&&isInWater()){
            moveFlying(strafe,forward,getAIMoveSpeed());
            moveEntity(motionX,motionY,motionZ);
            motionX*=.9D;motionY*=.9D;motionZ*=.9D;
            if(getAttackTarget()==null)motionY-=.005D;
        }else super.moveEntityWithHeading(strafe,forward);
    }
    private boolean canAid(EntityPlayer player){return player!=null&&player.worldObj==worldObj&&player.isEntityAlive()
            &&player!=getAttackTarget()&&com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerSwimming120.isSwimming(player);}
    private void updateGrace(){
        if(worldObj.isRemote||!isEntityAlive()){graceTarget=null;return;}
        if(!canAid(graceTarget)||(graceTarget!=null&&getDistanceSqToEntity(graceTarget)>=256D))graceTarget=null;
        if(graceTarget==null){double distance=100D;
            for(Object object:worldObj.getEntitiesWithinAABB(EntityPlayer.class,boundingBox.expand(10,10,10))){EntityPlayer player=(EntityPlayer)object;double d=getDistanceSqToEntity(player);if(canAid(player)&&d<=distance){distance=d;graceTarget=player;}}
            if(graceTarget!=null)com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.AquaticEffects120.grant(graceTarget);
        }else if(worldObj.rand.nextInt(6)==0)com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.AquaticEffects120.grant(graceTarget);
    }
    @Override public boolean canBreatheUnderwater(){return false;}
    @Override protected void dropFewItems(boolean hit,int looting){
        // 1.20.2 dolphin loot: uniform 0..1 cod, plus rounded uniform Looting bonus.
        int count=rand.nextInt(2);
        if(looting>0)count+=Math.round(rand.nextFloat()*looting);
        if(count>0)entityDropItem(new net.minecraft.item.ItemStack(isBurning()?net.minecraft.init.Items.cooked_fished:net.minecraft.init.Items.fish,count,0),0F);
    }
    @Override protected String getLivingSound(){return "htgt6:entity.dolphin.ambient_water";}
    @Override protected String getHurtSound(){return "htgt6:entity.dolphin.hurt";}
    @Override protected String getDeathSound(){return "htgt6:entity.dolphin.death";}
}
