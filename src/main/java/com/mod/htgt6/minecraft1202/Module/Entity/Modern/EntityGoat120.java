package com.mod.htgt6.minecraft1202.Module.Entity.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/** Minecraft 1.20.2 goat adapted to the 1.7 animal, watcher and path-navigation APIs. */
public final class EntityGoat120 extends EntityAnimal {
    public static final int FLAGS_WATCHER = 20;
    public static final int RAMMING_WATCHER = 21;
    public static final int FLAG_SCREAMING = 1;
    public static final int FLAG_LEFT_HORN = 2;
    public static final int FLAG_RIGHT_HORN = 4;
    public static final int MIN_RAM_COOLDOWN = 600;
    public static final int MAX_RAM_COOLDOWN = 6000;
    public static final int SCREAMING_MIN_RAM_COOLDOWN = 100;
    public static final int SCREAMING_MAX_RAM_COOLDOWN = 300;
    public static final int SCREAMING_CHANCE = 50;
    public static final double MAX_HEALTH = 10.0D;
    public static final double MOVEMENT_SPEED = 0.20D;
    public static final String NBT_SCREAMING = "IsScreamingGoat";
    public static final String NBT_LEFT_HORN = "HasLeftHorn";
    public static final String NBT_RIGHT_HORN = "HasRightHorn";
    public static final String NBT_RAM_COOLDOWN = "RamCooldown";

    private int ramCooldown;

    public EntityGoat120(World world) {
        super(world);
        setSize(0.9F, 1.3F);
        stepHeight = 1.25F;
        experienceValue = 1;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.25D));
        tasks.addTask(2, new EntityAILongJump120(this));
        tasks.addTask(3, new RamGoal());
        tasks.addTask(4, new EntityAIMate(this, 1.0D));
        tasks.addTask(5, new EntityAITempt(this, 1.1D, Items.wheat, false));
        tasks.addTask(6, new EntityAIFollowParent(this, 1.1D));
        tasks.addTask(7, new EntityAIWander(this, 0.8D));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(9, new EntityAILookIdle(this));
    }

    @Override protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(FLAGS_WATCHER, Byte.valueOf((byte)(FLAG_LEFT_HORN | FLAG_RIGHT_HORN)));
        dataWatcher.addObject(RAMMING_WATCHER, Byte.valueOf((byte)0));
    }

    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(MAX_HEALTH);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(MOVEMENT_SPEED);
    }

    @Override protected boolean isAIEnabled() { return true; }

    public boolean isScreaming() { return flag(FLAG_SCREAMING); }
    public boolean hasLeftHorn() { return flag(FLAG_LEFT_HORN); }
    public boolean hasRightHorn() { return flag(FLAG_RIGHT_HORN); }
    public boolean isRamming() { return dataWatcher.getWatchableObjectByte(RAMMING_WATCHER) != 0; }
    public int getRamCooldown() { return ramCooldown; }

    public void setScreaming(boolean value) { setGoatFlag(FLAG_SCREAMING, value); }
    public void setLeftHorn(boolean value) { setGoatFlag(FLAG_LEFT_HORN, value); }
    public void setRightHorn(boolean value) { setGoatFlag(FLAG_RIGHT_HORN, value); }
    private void setRamming(boolean value) {
        dataWatcher.updateObject(RAMMING_WATCHER, Byte.valueOf((byte)(value ? 1 : 0)));
    }
    private boolean flag(int bit) { return (dataWatcher.getWatchableObjectByte(FLAGS_WATCHER) & bit) != 0; }
    private void setGoatFlag(int bit, boolean value) {
        int flags = dataWatcher.getWatchableObjectByte(FLAGS_WATCHER) & 255;
        dataWatcher.updateObject(FLAGS_WATCHER, Byte.valueOf((byte)(value ? flags | bit : flags & ~bit)));
    }

    @Override public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        setScreaming(rand.nextInt(SCREAMING_CHANCE) == 0);
        ramCooldown = randomRamCooldown();
        return data;
    }

    public static int nextRamCooldown(int boundedRandom) {
        return MIN_RAM_COOLDOWN + MathHelper.clamp_int(boundedRandom, 0, MAX_RAM_COOLDOWN - MIN_RAM_COOLDOWN);
    }

    public static int nextRamCooldown(boolean screaming, int boundedRandom) {
        if (!screaming) return nextRamCooldown(boundedRandom);
        return SCREAMING_MIN_RAM_COOLDOWN + MathHelper.clamp_int(boundedRandom, 0,
                SCREAMING_MAX_RAM_COOLDOWN - SCREAMING_MIN_RAM_COOLDOWN);
    }

    private int randomRamCooldown() {
        int range = isScreaming() ? SCREAMING_MAX_RAM_COOLDOWN - SCREAMING_MIN_RAM_COOLDOWN
                : MAX_RAM_COOLDOWN - MIN_RAM_COOLDOWN;
        return nextRamCooldown(isScreaming(), rand.nextInt(range + 1));
    }

    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        if (!worldObj.isRemote && ramCooldown > 0) --ramCooldown;
    }

    @Override public boolean interact(EntityPlayer player) {
        ItemStack held = player.inventory.getCurrentItem();
        if (held != null && held.getItem() == Items.bucket && !isChild()) {
            if (--held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem,
                    new ItemStack(Items.milk_bucket));
            else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.milk_bucket)))
                player.dropPlayerItemWithRandomChoice(new ItemStack(Items.milk_bucket), false);
            playSound(isScreaming() ? "htgt6:entity.goat.screaming_milk" : "mob.cow.say", 1.0F, 1.0F);
            return true;
        }
        return super.interact(player);
    }

    @Override public EntityGoat120 createChild(EntityAgeable mate) {
        EntityGoat120 child = new EntityGoat120(worldObj);
        child.setScreaming(isScreaming() || mate instanceof EntityGoat120 && ((EntityGoat120)mate).isScreaming()
                ? rand.nextInt(2) == 0 : rand.nextInt(SCREAMING_CHANCE) == 0);
        return child;
    }

    @Override public boolean isBreedingItem(ItemStack stack) { return stack != null && stack.getItem() == Items.wheat; }

    @Override public boolean getCanSpawnHere() {
        int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(boundingBox.minY) - 1,
                z = MathHelper.floor_double(posZ);
        Block ground = worldObj.getBlock(x, y, z);
        return (ground == Blocks.grass || ground == Blocks.stone || ground == Blocks.snow
                || ground == Blocks.snow_layer) && worldObj.getBlockLightValue(x, y + 1, z) > 8
                && super.getCanSpawnHere();
    }

    private void shedHorn() {
        if (!hasLeftHorn() && !hasRightHorn()) return;
        if (hasLeftHorn() && hasRightHorn()) {
            if (rand.nextBoolean()) setLeftHorn(false); else setRightHorn(false);
        } else if (hasLeftHorn()) setLeftHorn(false); else setRightHorn(false);
        entityDropItem(new ItemStack(Backport120Content.goatHorn, 1,
                instrumentMetadata(isScreaming(), rand.nextInt(4))), 0.35F);
        playSound("htgt6:entity.goat.horn_break",1.0F,1.0F);
    }

    /** Exact 1.20.2 snaps_goat_horn tag mapped to the available 1.7/backport block identities. */
    public static boolean canBreakHornOn(Block block) {
        return block!=null&&(block==Blocks.log||block==Blocks.log2||block==Blocks.stone||block==Blocks.packed_ice
                ||block==Blocks.iron_ore||block==Blocks.coal_ore||block==Blocks.emerald_ore
                ||block==Backport120Content.copperOre||block==Backport120Content.cherryLog
                ||block==Backport120Content.mangroveLog);
    }

    /** Bootstrap-free mirror of the legacy vanilla identities used by the contract fixture. */
    public static boolean isVanillaHornBreakKey(String key) {
        return "log".equals(key)||"log2".equals(key)||"stone".equals(key)||"packed_ice".equals(key)
                ||"iron_ore".equals(key)||"coal_ore".equals(key)||"emerald_ore".equals(key);
    }

    /** 1.20 normal horns use instruments 0..3; screaming goats use 4..7. */
    public static int instrumentMetadata(boolean screaming, int commonIndex) {
        return (screaming ? 4 : 0) + MathHelper.clamp_int(commonIndex, 0, 3);
    }

    @Override protected String getLivingSound() { return isScreaming()
            ? "htgt6:entity.goat.scream" : "htgt6:entity.goat.ambient"; }
    @Override protected String getHurtSound() { return isScreaming()
            ? "htgt6:entity.goat.screaming_hurt" : "htgt6:entity.goat.hurt"; }
    @Override protected String getDeathSound() { return isScreaming()
            ? "htgt6:entity.goat.screaming_death" : "htgt6:entity.goat.death"; }

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean(NBT_SCREAMING, isScreaming());
        tag.setBoolean(NBT_LEFT_HORN, hasLeftHorn());
        tag.setBoolean(NBT_RIGHT_HORN, hasRightHorn());
        tag.setInteger(NBT_RAM_COOLDOWN, ramCooldown);
    }

    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setScreaming(tag.getBoolean(NBT_SCREAMING));
        setLeftHorn(!tag.hasKey(NBT_LEFT_HORN) || tag.getBoolean(NBT_LEFT_HORN));
        setRightHorn(!tag.hasKey(NBT_RIGHT_HORN) || tag.getBoolean(NBT_RIGHT_HORN));
        ramCooldown = Math.max(0, tag.getInteger(NBT_RAM_COOLDOWN));
    }

    private final class RamGoal extends EntityAIBase {
        private EntityLivingBase target;
        private int chargeTicks;

        RamGoal() { setMutexBits(3); }

        @Override public boolean shouldExecute() {
            if (isChild() || ramCooldown > 0 || !onGround || rand.nextInt(isScreaming() ? 5 : 20) != 0) return false;
            List<?> candidates = worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
                    boundingBox.expand(10.0D, 4.0D, 10.0D));
            double best = Double.MAX_VALUE;
            for (Object raw : candidates) {
                EntityLivingBase candidate = (EntityLivingBase)raw;
                if (candidate == EntityGoat120.this || candidate instanceof EntityGoat120 || !candidate.isEntityAlive()) continue;
                double distance = getDistanceSqToEntity(candidate);
                if (distance < best) { best = distance; target = candidate; }
            }
            return target != null;
        }

        @Override public void startExecuting() {
            chargeTicks = 0;
            setRamming(true);
            playSound(isScreaming()?"htgt6:entity.goat.screaming_pre_ram":"htgt6:entity.goat.pre_ram",1.0F,1.0F);
            getNavigator().tryMoveToEntityLiving(target, 1.65D);
        }

        @Override public boolean continueExecuting() {
            return target != null && target.isEntityAlive() && chargeTicks < 80 && !getNavigator().noPath();
        }

        @Override public void updateTask() {
            ++chargeTicks;
            getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            getNavigator().tryMoveToEntityLiving(target, 1.65D);
            if (getDistanceSqToEntity(target) <= 2.0D) {
                target.attackEntityFrom(DamageSource.causeMobDamage(EntityGoat120.this), isScreaming() ? 3.0F : 2.0F);
                double dx = target.posX - posX, dz = target.posZ - posZ;
                double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
                target.addVelocity(dx / length * 1.1D, 0.35D, dz / length * 1.1D);
                target.velocityChanged = true;
                playSound("htgt6:entity.goat.ram_impact",1.0F,1.0F);
                chargeTicks = 80;
            } else if (isSolidAhead()) {
                shedHorn();
                chargeTicks = 80;
            }
        }

        private boolean isSolidAhead() {
            float yaw = rotationYaw * (float)Math.PI / 180.0F;
            int x = MathHelper.floor_double(posX - MathHelper.sin(yaw) * 0.8D);
            int y = MathHelper.floor_double(posY + 0.6D);
            int z = MathHelper.floor_double(posZ + MathHelper.cos(yaw) * 0.8D);
            Block block = worldObj.getBlock(x, y, z);
            return canBreakHornOn(block);
        }

        @Override public void resetTask() {
            getNavigator().clearPathEntity();
            setRamming(false);
            ramCooldown = randomRamCooldown();
            target = null;
        }
    }
}
