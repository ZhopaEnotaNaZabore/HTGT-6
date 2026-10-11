package com.mod.htgt6.minecraft1202.Module.Entity.Frostburn;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

/** Neutral Minecraft 1.10 Polar Bear with cub-protection behaviour. */
public final class EntityFrostburnPolarBear extends EntityAnimal {
    private static final int STANDING = 19;
    private float previousStandingScale;
    private float standingScale;
    private int warningSoundCooldown;
    private int angerTime;
    private java.util.UUID angryAt;
    private int universalAngerTimestamp;

    public EntityFrostburnPolarBear(World world) {
        super(world);
        setSize(1.3F, 1.4F);
        experienceValue = 1;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new CubPanic(this));
        tasks.addTask(1, new AdultAttack(this));
        tasks.addTask(4, new EntityAIFollowParent(this, 1.25D));
        tasks.addTask(5, new EntityAIWander(this, 1.0D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new PolarBearHurtByTarget(this));
        targetTasks.addTask(2, new ProtectCubsTarget(this));
        targetTasks.addTask(3,new EntityAINearestAttackableTarget(this,EntityPlayer.class,10,true,false,
                new net.minecraft.command.IEntitySelector() {
                    public boolean isEntityApplicable(Entity entity) { return isAngryAt(entity); }
                }));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(STANDING, Byte.valueOf((byte) 0));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
        getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
        getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
    }

    /** EntityAnimal still defaults to the pre-1.7 legacy AI path. */
    @Override
    protected boolean isAIEnabled() {
        return true;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (worldObj.isRemote) {
            previousStandingScale = standingScale;
            if (isStanding()) standingScale = Math.min(6.0F, standingScale + 1.0F);
            else standingScale = Math.max(0.0F, standingScale - 1.0F);
            // The reference expands dimensions on the client alongside the
            // standing animation. EntityAgeable caches unscaled dimensions.
            setSize(1.3F, 1.4F * (1F + standingScale / 6F));
            setScale(isChild() ? .5F : 1F);
        }
        if (warningSoundCooldown > 0) --warningSoundCooldown;
        if(!worldObj.isRemote)tickAnger();
    }

    public int getAngerTime() { return angerTime; }
    public java.util.UUID getAngryAt() { return angryAt; }
    public boolean isAngryAt(Entity entity) {
        return !isChild() && angerTime>0 && entity!=null && entity.isEntityAlive()
                && (entity.getUniqueID().equals(angryAt)
                    || (entity instanceof EntityPlayer && angryAt == null && universalAnger()));
    }
    private boolean universalAnger() {
        return worldObj.getGameRules().getGameRuleBooleanValue("universalAnger");
    }
    private EntityLivingBase rememberedEntity() {
        if (angryAt == null) return null;
        for (Object value : worldObj.loadedEntityList) {
            Entity entity = (Entity) value;
            if (entity instanceof EntityLivingBase && angryAt.equals(entity.getUniqueID()))
                return (EntityLivingBase) entity;
        }
        return null;
    }
    /** The timer pauses while fighting the remembered player, as NeutralMob does. */
    public void tickAnger() {
        if(worldObj.isRemote)return;
        // ResetUniversalAngerTargetGoal(false): this bear forgets the attacking
        // player's identity, without spreading anger to unrelated nearby bears.
        if (universalAnger() && getAITarget() instanceof EntityPlayer
                && func_142015_aE() > universalAngerTimestamp) {
            universalAngerTimestamp = func_142015_aE();
            clearAnger();
            angerTime = (20 + rand.nextInt(20)) * 20;
        }
        EntityLivingBase target=getAttackTarget();
        EntityLivingBase remembered = rememberedEntity();
        if ((target == null || !target.isEntityAlive())
                && remembered instanceof net.minecraft.entity.EntityLiving) {
            clearAnger();return;
        }
        if (target == null && remembered instanceof EntityPlayer && !remembered.isEntityAlive()
                && (!worldObj.getGameRules().hasRule("forgiveDeadPlayers")
                    || worldObj.getGameRules().getGameRuleBooleanValue("forgiveDeadPlayers"))) {
            clearAnger();return;
        }
        if(target!=null && !target.isEntityAlive()) {
            boolean forgive=!worldObj.getGameRules().hasRule("forgiveDeadPlayers")
                    || worldObj.getGameRules().getGameRuleBooleanValue("forgiveDeadPlayers");
            if(!(target instanceof EntityPlayer) || forgive) { clearAnger();return; }
            setAttackTarget(null);target=null;
        }
        if(target!=null && !target.getUniqueID().equals(angryAt)) {
            angryAt=target.getUniqueID();angerTime=(20+rand.nextInt(20))*20;
        }
        if(angerTime>0 && !(target instanceof EntityPlayer)) {
            if(--angerTime==0)clearAnger();
        }
    }
    private void clearAnger() {
        angerTime=0;angryAt=null;setAttackTarget(null);setRevengeTarget(null);
    }
    @Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.writeEntityToNBT(tag);tag.setInteger("AngerTime",angerTime);
        if(angryAt!=null) {
            long most=angryAt.getMostSignificantBits(),least=angryAt.getLeastSignificantBits();
            tag.setIntArray("AngryAt",new int[]{(int)(most>>>32),(int)most,(int)(least>>>32),(int)least});
        } else tag.removeTag("AngryAt");
    }
    @Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.readEntityFromNBT(tag);angerTime=Math.max(0,tag.getInteger("AngerTime"));angryAt=null;
        int[] id=tag.getIntArray("AngryAt");
        if(id.length==4)angryAt=new java.util.UUID(((long)id[0]<<32)|(id[1]&0xffffffffL),((long)id[2]<<32)|(id[3]&0xffffffffL));
        if (!worldObj.isRemote) {
            EntityLivingBase remembered = rememberedEntity();
            if (remembered != null) setRevengeTarget(remembered);
        }
    }

    public boolean isStanding() {
        return dataWatcher.getWatchableObjectByte(STANDING) != 0;
    }

    private void setStanding(boolean standing) {
        dataWatcher.updateObject(STANDING, Byte.valueOf((byte) (standing ? 1 : 0)));
    }

    private void playWarningSound() {
        if (warningSoundCooldown > 0) return;
        playSound("htgt6:entity.polar_bear.warning", 1.0F, 1.0F);
        warningSoundCooldown = 40;
    }

    public float getStandingAnimationScale(float partialTick) {
        return (previousStandingScale + (standingScale - previousStandingScale) * partialTick) / 6.0F;
    }

    /** Mob.getAttackBoundingBox in 1.20.2: horizontal reach plus vertical overlap. */
    public boolean canPerformMeleeAttack120(EntityLivingBase target) {
        if(target==null||!target.isEntityAlive())return false;
        net.minecraft.util.AxisAlignedBB box=boundingBox;
        if(ridingEntity!=null) {
            net.minecraft.util.AxisAlignedBB mount=ridingEntity.boundingBox;
            box=net.minecraft.util.AxisAlignedBB.getBoundingBox(Math.min(box.minX,mount.minX),box.minY,
                    Math.min(box.minZ,mount.minZ),Math.max(box.maxX,mount.maxX),box.maxY,Math.max(box.maxZ,mount.maxZ));
        }
        net.minecraft.util.AxisAlignedBB targetBox=target.boundingBox;
        if(target.ridingEntity!=null) {
            double seat=target.ridingEntity.posY+target.ridingEntity.getMountedYOffset()+target.getYOffset();
            targetBox=net.minecraft.util.AxisAlignedBB.getBoundingBox(targetBox.minX,Math.max(targetBox.minY,seat),targetBox.minZ,
                    targetBox.maxX,targetBox.maxY,targetBox.maxZ);
        }
        double reach=Math.sqrt(2.04F)-(double).6F;
        return box.expand(reach,0,reach).intersectsWith(targetBox)&&getEntitySenses().canSee(target);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        float damage = (float)(int)getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(this),damage);
        if(hit) {
            if(target instanceof EntityLivingBase)
                net.minecraft.enchantment.EnchantmentHelper.func_151384_a((EntityLivingBase)target,this);
            net.minecraft.enchantment.EnchantmentHelper.func_151385_b(this,target);
        }
        return hit;
    }

    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        GroupData group = data instanceof GroupData ? (GroupData) data : new GroupData();
        if (group.hasAdult) setGrowingAge(-24000);
        else group.hasAdult = true;
        return group;
    }

    @Override
    public EntityFrostburnPolarBear createChild(EntityAgeable mate) {
        return new EntityFrostburnPolarBear(worldObj);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    protected void dropFewItems(boolean recentlyHit, int looting) {
        // Table entry is selected before its count and Looting functions execute.
        int fishMeta = rand.nextInt(4) < 3 ? 0 : 1;
        int count = FrostburnLoot120.count(rand,2,looting);
        for (int i = 0; i < count; ++i) entityDropItem(new ItemStack(isBurning()?Items.cooked_fished:Items.fish, 1, fishMeta), 0.0F);
    }

    @Override protected String getLivingSound() { return isChild() ? "htgt6:entity.polar_bear.ambient_baby" : "htgt6:entity.polar_bear.ambient"; }
    @Override protected String getHurtSound() { return "htgt6:entity.polar_bear.hurt"; }
    @Override protected String getDeathSound() { return "htgt6:entity.polar_bear.death"; }

    @Override protected void func_145780_a(int x,int y,int z,net.minecraft.block.Block block) {
        playSound("htgt6:entity.polar_bear.step",.15F,1F);
    }
    @Override public boolean getCanSpawnHere() {
        int x=net.minecraft.util.MathHelper.floor_double(posX),y=net.minecraft.util.MathHelper.floor_double(boundingBox.minY),z=net.minecraft.util.MathHelper.floor_double(posZ);
        net.minecraft.world.biome.BiomeGenBase biome=worldObj.getBiomeGenForCoords(x,z);
        if(biome!=null && (biome==net.minecraft.world.biome.BiomeGenBase.frozenOcean
                || biome==com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanBiomeRegistry.deepFrozenOcean)) {
            return worldObj.getBlock(x,y-1,z)==net.minecraft.init.Blocks.ice
                &&worldObj.getFullBlockLightValue(x,y,z)>8
                &&worldObj.checkNoEntityCollision(boundingBox)
                &&worldObj.getCollidingBoundingBoxes(this,boundingBox).isEmpty()
                &&!worldObj.isAnyLiquid(boundingBox);
        }
        return super.getCanSpawnHere();
    }

    private boolean hasNearbyCub() {
        List<?> bears = worldObj.getEntitiesWithinAABB(EntityFrostburnPolarBear.class,
                boundingBox.expand(8.0D, 4.0D, 8.0D));
        for (Object bear : bears) if (((EntityFrostburnPolarBear) bear).isChild()) return true;
        return false;
    }

    private final class ProtectCubsTarget extends EntityAINearestAttackableTarget {
        ProtectCubsTarget(EntityFrostburnPolarBear bear) {
            super(bear, EntityPlayer.class, 20, true);
        }

        @Override
        public boolean shouldExecute() {
            if (EntityFrostburnPolarBear.this.isChild()) return false;
            if (super.shouldExecute() && EntityFrostburnPolarBear.this.hasNearbyCub()) return true;
            return false;
        }

        @Override
        protected double getTargetDistance() {
            return super.getTargetDistance() * 0.5D;
        }
    }

    private final class CubPanic extends EntityAIPanic {
        CubPanic(EntityFrostburnPolarBear bear) {
            super(bear, 2.0D);
        }

        @Override
        public boolean shouldExecute() {
            return (EntityFrostburnPolarBear.this.isChild()
                    || EntityFrostburnPolarBear.this.isBurning()) && super.shouldExecute();
        }
    }

    /** Cubs call nearby adults for help, then flee instead of keeping their own combat target. */
    private final class PolarBearHurtByTarget extends EntityAIHurtByTarget {
        PolarBearHurtByTarget(EntityFrostburnPolarBear bear) {
            super(bear, false);
        }

        @Override
        public void startExecuting() {
            super.startExecuting();
            if (!EntityFrostburnPolarBear.this.isChild()) return;
            EntityLivingBase attacker = EntityFrostburnPolarBear.this.getAttackTarget();
            if (attacker != null) {
                double range = getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue();
                List<?> bears = worldObj.getEntitiesWithinAABB(EntityFrostburnPolarBear.class,
                        boundingBox.expand(range, 10.0D, range));
                for (Object value : bears) {
                    EntityFrostburnPolarBear bear = (EntityFrostburnPolarBear) value;
                    if (bear != EntityFrostburnPolarBear.this && !bear.isChild()
                            && bear.getAttackTarget() == null && !bear.isOnSameTeam(attacker)) {
                        bear.setAttackTarget(attacker);
                    }
                }
            }
            EntityFrostburnPolarBear.this.setAttackTarget(null);
        }
    }

    private final class AdultAttack extends EntityAIAttackOnCollide {
        private int attackCooldown;
        private int repathCooldown;

        AdultAttack(EntityFrostburnPolarBear bear) {
            super(bear, 1.25D, true);
        }

        @Override
        public boolean shouldExecute() {
            return !EntityFrostburnPolarBear.this.isChild() && super.shouldExecute();
        }

        @Override
        public boolean continueExecuting() {
            return !EntityFrostburnPolarBear.this.isChild() && super.continueExecuting();
        }

        @Override
        public void updateTask() {
            EntityLivingBase target = EntityFrostburnPolarBear.this.getAttackTarget();
            if (target == null) return;
            getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            double distanceSq = getDistanceSqToEntity(target);
            double warningReachSq = (target.width+3F)*(target.width+3F);
            if (--repathCooldown <= 0) {
                repathCooldown = 10;
                getNavigator().tryMoveToEntityLiving(target, 1.25D);
            }
            if (attackCooldown > 0) --attackCooldown;

            if (attackCooldown <= 0 && canPerformMeleeAttack120(target)) {
                attackCooldown = 20;
                setStanding(false);
                attackEntityAsMob(target);
            } else if (distanceSq < warningReachSq) {
                if (attackCooldown <= 0) {
                    setStanding(false);
                    attackCooldown = 20;
                } else if (attackCooldown <= 10) {
                    setStanding(true);
                    playWarningSound();
                }
            } else {
                attackCooldown = 20;
                setStanding(false);
            }
        }

        @Override
        public void resetTask() {
            setStanding(false);
            super.resetTask();
        }
    }

    private static final class GroupData implements IEntityLivingData {
        boolean hasAdult;
    }
}
