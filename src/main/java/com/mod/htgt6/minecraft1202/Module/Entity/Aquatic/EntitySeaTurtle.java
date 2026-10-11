package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockTurtleEgg120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Lightweight sea turtle with aquatic wandering and vanilla animal breeding. */
public final class EntitySeaTurtle extends EntityAnimal {
    public static final double MOVEMENT_SPEED = 0.25D;
    private static final int HAS_EGG_DATA = 20;
    private int swimChange;
    private int homeX;
    private int homeY;
    private int homeZ;
    private boolean hasHome;
    private boolean hasEgg;
    private int layEggCounter;
    private boolean wasChild;

    public EntitySeaTurtle(World world) {
        super(world);
        setSize(1.2F, 0.45F);
        getNavigator().setAvoidsWater(false);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.2D));
        tasks.addTask(2, new EntityAITurtleMate120(this, 1.0D));
        tasks.addTask(3, new EntityAITempt(this, 1.1D,
                Item.getItemFromBlock(OceanContent.marinePlant), false));
        tasks.addTask(4, new EntityAIWander(this, 0.75D));
        tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(6, new EntityAILookIdle(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(HAS_EGG_DATA, Byte.valueOf((byte)0));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(MOVEMENT_SPEED);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!worldObj.isRemote) {
            updateEggLaying();
            boolean child = isChild();
            if (wasChild && !child) entityDropItem(new ItemStack(OceanContent.scute), 1.0F);
            wasChild = child;
        }
        if (isInWater()) {
            setAir(300);
            if (--swimChange <= 0) {
                rotationYaw += rand.nextFloat() * 120.0F - 60.0F;
                swimChange = 40 + rand.nextInt(80);
            }
            float yaw = rotationYaw * (float)Math.PI / 180.0F;
            motionX += -MathHelper.sin(yaw) * 0.012D;
            motionZ += MathHelper.cos(yaw) * 0.012D;
            motionY += (rand.nextFloat() - 0.48F) * 0.008D;
            motionX *= 0.94D; motionY *= 0.92D; motionZ *= 0.94D;
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(boundingBox.minY),
                z = MathHelper.floor_double(posZ);
        int upperY = FuturaWorldType120.isFutura(worldObj)
                ? FuturaMountainDensityRouter120.SEA_LEVEL + 5 : 68;
        return worldObj.getBlock(x, y - 1, z) == Blocks.sand && y < upperY
                && worldObj.getBlockLightValue(x, y, z) > 8
                && worldObj.checkNoEntityCollision(boundingBox)
                && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
    }

    @Override public boolean isBreedingItem(ItemStack stack) {
        return stack != null && stack.getItem() == Item.getItemFromBlock(OceanContent.marinePlant)
                && stack.getItemDamage() == 0;
    }
    @Override public EntitySeaTurtle createChild(EntityAgeable mate) {
        EntitySeaTurtle child = new EntitySeaTurtle(worldObj);
        child.homeX = homeX;
        child.homeY = homeY;
        child.homeZ = homeZ;
        child.hasHome = hasHome;
        return child;
    }
    public boolean hasEgg() {
        return worldObj != null && worldObj.isRemote
                ? dataWatcher.getWatchableObjectByte(HAS_EGG_DATA) != 0 : hasEgg;
    }
    void setHasEgg(boolean value) {
        hasEgg = value;
        layEggCounter = 0;
        dataWatcher.updateObject(HAS_EGG_DATA, Byte.valueOf((byte)(value ? 1 : 0)));
    }
    public void setHome(int x, int y, int z) {
        homeX = x; homeY = y; homeZ = z; hasHome = true;
    }

    private void updateEggLaying() {
        if (worldObj.isRemote || isDead || !hasEgg || !hasHome) return;
        double distance = getDistanceSq(homeX + 0.5D, homeY, homeZ + 0.5D);
        if (distance > 100.0D) {
            if (ticksExisted % 20 == 0) getNavigator().tryMoveToXYZ(homeX + 0.5D, homeY, homeZ + 0.5D, 1.0D);
            layEggCounter = 0;
            return;
        }
        int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(boundingBox.minY),
                z = MathHelper.floor_double(posZ);
        if (worldObj.getBlock(x, y - 1, z) != Blocks.sand || !worldObj.isAirBlock(x, y, z)) {
            if (ticksExisted % 20 == 0) getNavigator().tryMoveToXYZ(homeX + 0.5D, homeY, homeZ + 0.5D, 0.8D);
            layEggCounter = 0;
            return;
        }
        getNavigator().clearPathEntity();
        if (++layEggCounter < 200) return;
        int eggs = 1 + rand.nextInt(4);
        if (worldObj.setBlock(x, y, z, OceanContent.turtleEgg, BlockTurtleEgg120.metadata(eggs, 0), 3)) {
            setHasEgg(false);
        } else {
            // Preserve the pregnancy and retry after another laying cycle rather
            // than issuing a denied placement every tick.
            layEggCounter = 0;
        }
    }
    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        homeX = MathHelper.floor_double(posX);
        homeY = MathHelper.floor_double(posY);
        homeZ = MathHelper.floor_double(posZ);
        hasHome = true;
        return data;
    }
    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("HasHome", hasHome);
        tag.setInteger("HomeX", homeX);
        tag.setInteger("HomeY", homeY);
        tag.setInteger("HomeZ", homeZ);
        tag.setBoolean("HasEgg", hasEgg);
        tag.setInteger("LayEggCounter", layEggCounter);
    }
    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        hasHome = tag.getBoolean("HasHome");
        homeX = tag.getInteger("HomeX");
        homeY = tag.getInteger("HomeY");
        homeZ = tag.getInteger("HomeZ");
        hasEgg = tag.getBoolean("HasEgg");
        dataWatcher.updateObject(HAS_EGG_DATA, Byte.valueOf((byte)(hasEgg ? 1 : 0)));
        layEggCounter = tag.getInteger("LayEggCounter");
        wasChild = isChild();
    }
    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public boolean isPushedByWater() { return false; }
    @Override public boolean allowLeashing() { return false; }
    @Override public float getBlockPathWeight(int x, int y, int z) {
        return BlockKelp.isWater(worldObj.getBlock(x, y, z)) ? 10.0F : super.getBlockPathWeight(x, y, z);
    }
    @Override protected String getLivingSound() { return "htgt6:entity.turtle.ambient_land"; }
    @Override protected String getHurtSound() { return "htgt6:entity.turtle.hurt"; }
    @Override protected String getDeathSound() { return "htgt6:entity.turtle.death"; }
}
