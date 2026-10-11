package com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Heavy raid beast with strong knockback, adapted to 1.7.10 navigation. */
public final class EntityRavager extends EntityMob implements com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112 {
    private static final int ATTACK = 20, STUN = 21, ROAR = 22;
    private int clientAttack, clientStun, clientRoar;

    @Override protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(ATTACK, Integer.valueOf(0));
        dataWatcher.addObject(STUN, Integer.valueOf(0));
        dataWatcher.addObject(ROAR, Integer.valueOf(0));
    }

    public int getAttackTick() { return worldObj.isRemote ? clientAttack : dataWatcher.getWatchableObjectInt(ATTACK); }
    public int getStunnedTick() { return worldObj.isRemote ? clientStun : dataWatcher.getWatchableObjectInt(STUN); }
    public int getRoarTick() { return worldObj.isRemote ? clientRoar : dataWatcher.getWatchableObjectInt(ROAR); }

    @Override public void func_145781_i(int slot) {
        super.func_145781_i(slot);
        if (!worldObj.isRemote) return;
        if (slot == ATTACK) clientAttack = MathHelper.clamp_int(dataWatcher.getWatchableObjectInt(slot), 0, 10);
        else if (slot == STUN) clientStun = MathHelper.clamp_int(dataWatcher.getWatchableObjectInt(slot), 0, 40);
        else if (slot == ROAR) clientRoar = MathHelper.clamp_int(dataWatcher.getWatchableObjectInt(slot), 0, 20);
    }

    public EntityRavager(World world) {
        super(world);
        setSize(1.95F, 2.2F);
        experienceValue = 20;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(2, new EntityAIAttackOnCollide(this, 1.0D, true));
        tasks.addTask(5, new EntityAIWander(this, 0.65D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 10.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, 0, false));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, 0, false));
    }

    // EntityLiving defaults to the obsolete pre-task AI in 1.7.10.
    @Override protected boolean isAIEnabled(){return true;}

    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(100.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.30D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(12.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(32.0D);
        getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.75D);
    }

    @Override public boolean attackEntityAsMob(Entity target) {
        if (worldObj.isRemote || !isEntityAlive() || target == null
                || getAttackTick() > 0 || getStunnedTick() > 0 || getRoarTick() > 0) return false;
        dataWatcher.updateObject(ATTACK, Integer.valueOf(10));
        worldObj.playSoundAtEntity(this, "htgt6:entity.ravager.attack", 1, 1);
        // A blocked/rejected hit still animates; modern Ravager adds no unconditional shove here.
        return super.attackEntityAsMob(target);
    }

    /** Called by the common shield handler after a directional, effective shield block. */
    public void onBlockedByShield(EntityLivingBase defender) {
        if (worldObj.isRemote || !isEntityAlive() || defender == null || getRoarTick() > 0) return;
        if (rand.nextDouble() < 0.5D) {
            dataWatcher.updateObject(STUN, Integer.valueOf(40));
            worldObj.playSoundAtEntity(this, "htgt6:entity.ravager.stunned", 1, 1);
            defender.applyEntityCollision(this);
        } else {
            strongKnockback(defender);
        }
        defender.velocityChanged = true;
    }

    @Override protected boolean isMovementBlocked() {
        return super.isMovementBlocked() || getAttackTick() > 0 || getStunnedTick() > 0 || getRoarTick() > 0;
    }

    @Override public boolean canEntityBeSeen(Entity target) {
        return getStunnedTick() == 0 && getRoarTick() == 0 && super.canEntityBeSeen(target);
    }

    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!isEntityAlive()) return;
        if (worldObj.isRemote) tickClientVisuals();
        else tickCombatState();
    }

    /** Predict visual countdowns between tracker metadata packets; never writes authoritative state. */
    private void tickClientVisuals() {
        if (!worldObj.isRemote || !isEntityAlive()) return;
        if (clientRoar > 0 && --clientRoar == 10) {
            for (int i = 0; i < 40; i++) worldObj.spawnParticle("explode", posX, posY + height * .5D, posZ,
                    rand.nextGaussian() * .2D, rand.nextGaussian() * .2D, rand.nextGaussian() * .2D);
        }
        if (clientAttack > 0) --clientAttack;
        if (clientStun > 0) {
            --clientStun;
            if (rand.nextInt(6) == 0) {
                double yaw = rotationYawHead * Math.PI / 180.0D;
                worldObj.spawnParticle("mobSpell", posX - width * Math.sin(yaw) + rand.nextDouble() * .6D - .3D,
                        posY + height - .3D, posZ + width * Math.cos(yaw) + rand.nextDouble() * .6D - .3D,
                        .4980392156862745D, .5137254901960784D, .5725490196078431D);
            }
            if (clientStun == 0 && clientRoar == 0) clientRoar = 20;
        }
    }

    private void tickCombatState() {
        if (worldObj.isRemote || !isEntityAlive()) return;
        double speed = getEntityAttribute(SharedMonsterAttributes.movementSpeed).getBaseValue();
        double wanted = getAttackTarget() == null ? .3D : .35D;
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(
                isMovementBlocked() ? 0.0D : speed + (wanted - speed) * .1D);
        clearLeavesWhenBlocked();
        int roar = getRoarTick();
        if (roar > 0) {
            dataWatcher.updateObject(ROAR, Integer.valueOf(--roar));
            if (roar == 10) roar();
        }
        if (getAttackTick() > 0) dataWatcher.updateObject(ATTACK, Integer.valueOf(getAttackTick() - 1));
        int stun = getStunnedTick();
        if (stun > 0) {
            dataWatcher.updateObject(STUN, Integer.valueOf(--stun));
            if (stun == 0) {
                dataWatcher.updateObject(ROAR, Integer.valueOf(20));
                worldObj.playSoundAtEntity(this, "htgt6:entity.ravager.roar", 1, 1);
            }
        }
    }

    /** The modern obstruction fallback destroys only leaves, and jumps when no leaf was removed. */
    private void clearLeavesWhenBlocked() {
        if(worldObj.isRemote || !isEntityAlive() || !isCollidedHorizontally
                || !worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing"))return;
        net.minecraft.util.AxisAlignedBB bounds=boundingBox.expand(.2D,.2D,.2D);
        int minX=MathHelper.floor_double(bounds.minX),maxX=MathHelper.floor_double(bounds.maxX);
        int minY=Math.max(0,MathHelper.floor_double(bounds.minY)),maxY=Math.min(worldObj.getHeight()-1,MathHelper.floor_double(bounds.maxY));
        int minZ=MathHelper.floor_double(bounds.minZ),maxZ=MathHelper.floor_double(bounds.maxZ);
        if(!worldObj.checkChunksExist(minX,minY,minZ,maxX,maxY,maxZ))return;
        boolean removed=false;
        for(int x=minX;x<=maxX;x++)for(int y=minY;y<=maxY;y++)for(int z=minZ;z<=maxZ;z++) {
            net.minecraft.block.Block block=worldObj.getBlock(x,y,z);
            if(!block.isLeaves(worldObj,x,y,z)||!block.canEntityDestroy(worldObj,x,y,z,this))continue;
            int metadata=worldObj.getBlockMetadata(x,y,z);
            // Unlike legacy World.destroyBlock, do not drop first if a protection rejects removal.
            if(worldObj.setBlockToAir(x,y,z)) {
                removed=true;
                worldObj.playAuxSFX(2001,x,y,z,net.minecraft.block.Block.getIdFromBlock(block)+(metadata<<12));
                block.dropBlockAsItem(worldObj,x,y,z,metadata,0);
            }
        }
        if(!removed&&onGround){jump();velocityChanged=true;}
    }

    @Override protected void dropFewItems(boolean recentlyHit,int looting) {
        if(!worldObj.isRemote)dropItem(net.minecraft.init.Items.saddle,1);
    }

    private void roar() {
        if (worldObj.isRemote || !isEntityAlive()) return;
        for (Object value : worldObj.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox.expand(4, 4, 4))) {
            EntityLivingBase target = (EntityLivingBase)value;
            if (target instanceof EntityRavager || !target.isEntityAlive()) continue;
            // Current illager family: pillager, evoker and vindicator share this marker.
            if (!(target instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112))
                target.attackEntityFrom(DamageSource.causeMobDamage(this), 6.0F);
            strongKnockback(target);
        }
    }

    private void strongKnockback(Entity target) {
        double dx = target.posX - posX, dz = target.posZ - posZ;
        double squared = Math.max(dx * dx + dz * dz, .001D);
        target.addVelocity(dx / squared * 4, .2D, dz / squared * 4);
        target.velocityChanged = true;
    }

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("AttackTick", getAttackTick());
        tag.setInteger("StunTick", getStunnedTick());
        tag.setInteger("RoarTick", getRoarTick());
    }

    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        dataWatcher.updateObject(ATTACK, Integer.valueOf(MathHelper.clamp_int(tag.getInteger("AttackTick"), 0, 10)));
        dataWatcher.updateObject(STUN, Integer.valueOf(MathHelper.clamp_int(tag.getInteger("StunTick"), 0, 40)));
        dataWatcher.updateObject(ROAR, Integer.valueOf(MathHelper.clamp_int(tag.getInteger("RoarTick"), 0, 20)));
    }

    @Override public double getMountedYOffset(){return height+.0625D;}
    @Override public void updateRiderPosition(){
        if(riddenByEntity==null)return;
        double yaw=rotationYaw*Math.PI/180D;
        riddenByEntity.setPosition(posX+Math.sin(yaw)*.0625D,posY+getMountedYOffset()+riddenByEntity.getYOffset(),posZ-Math.cos(yaw)*.0625D);
    }

    @Override public boolean getCanSpawnHere() {
        return worldObj.provider.dimensionId == 0 && super.getCanSpawnHere();
    }
}
