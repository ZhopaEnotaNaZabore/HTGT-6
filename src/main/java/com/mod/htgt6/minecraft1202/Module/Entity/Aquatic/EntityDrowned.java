package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;

/** 1.13 drowned adapted to the 1.7.10 zombie AI and navigation API. */
public final class EntityDrowned extends EntityZombie {
    public static final float TRIDENT_EQUIPMENT_CHANCE = 0.0625F;
    public static final float TRIDENT_DROP_CHANCE = 0.085F;
    public static final int OCEAN_SPAWN_ROLL = 40;
    public static final int RIVER_SPAWN_ROLL = 15;
    public static final int MINIMUM_DEPTH_BELOW_SEA = 5;
    public static final int TRIDENT_CHARGE_TICKS = 10;
    private static final int TRIDENT_CHARGE_DATA = 20;
    private static final int NAUTILUS_SHELL_DATA = 21;
    private int tridentCooldown;
    private int tridentCharge;

    public EntityDrowned(World world) {
        super(world);
        setSize(0.6F, 1.8F);
        experienceValue = 5;
        // EntityLiving.dropEquipment owns the single vanilla 8.5% roll.
        equipmentDropChances[0] = TRIDENT_DROP_CHANCE;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(TRIDENT_CHARGE_DATA, Byte.valueOf((byte)0));
        dataWatcher.addObject(NAUTILUS_SHELL_DATA, Byte.valueOf((byte)0));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.23D);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(3.0D);
    }

    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        if (rand.nextFloat() < TRIDENT_EQUIPMENT_CHANCE) {
            setCurrentItemOrArmor(0, new ItemStack(OceanContent.trident));
        }
        if (!worldObj.isRemote) setCarriesNautilusShell(rand.nextFloat() < .03F);
        return data;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (isInWater()) {
            setAir(300);
            EntityLivingBase target = getAttackTarget();
            if (target == null) {
                int seaLevel = FuturaWorldType120.isFutura(worldObj)
                        ? FuturaMountainDensityRouter120.SEA_LEVEL : 63;
                if (!worldObj.isDaytime() && posY < seaLevel - 1) motionY += 0.02D;
                else motionY -= 0.01D;
            } else if (target.posY < posY + 0.5D) motionY -= 0.025D;
            else motionY += 0.02D;
            if (motionY < -0.18D) motionY = -0.18D;
            if (motionY > 0.16D) motionY = 0.16D;
        }

        if (!worldObj.isRemote && worldObj.isDaytime() && getAttackTarget() != null
                && !getAttackTarget().isInWater()) {
            setAttackTarget(null);
        }

        if (!worldObj.isRemote) updateTridentAttack();
    }

    private void updateTridentAttack() {
        EntityLivingBase target = getAttackTarget();
        boolean valid = target != null && getHeldItem() != null
                && getHeldItem().getItem() == OceanContent.trident && canThrowAt(target);
        if (tridentCharge > 0) {
            if (!valid) {
                tridentCharge = 0;
                dataWatcher.updateObject(TRIDENT_CHARGE_DATA, Byte.valueOf((byte)0));
                return;
            }
            tridentCharge--;
            dataWatcher.updateObject(TRIDENT_CHARGE_DATA, Byte.valueOf((byte)tridentCharge));
            if (tridentCharge == 0) throwTrident(target);
            return;
        }
        if (tridentCooldown > 0) {
            tridentCooldown--;
            return;
        }
        if (valid) {
            tridentCharge = TRIDENT_CHARGE_TICKS;
            dataWatcher.updateObject(TRIDENT_CHARGE_DATA, Byte.valueOf((byte)tridentCharge));
        }
    }

    private boolean canThrowAt(EntityLivingBase target) {
        if (target == null) return false;
        double dx = target.posX - posX;
        double dy = target.posY + target.getEyeHeight() - (posY + getEyeHeight());
        double dz = target.posZ - posZ;
        double distanceSq = dx * dx + dy * dy + dz * dz;
        return distanceSq >= 16.0D && distanceSq <= 256.0D && getEntitySenses().canSee(target);
    }

    private void throwTrident(EntityLivingBase target) {
        if (target != null) {
            double dx = target.posX - posX;
            double dy = target.posY + target.getEyeHeight() - (posY + getEyeHeight());
            double dz = target.posZ - posZ;
            EntityThrownTrident trident = new EntityThrownTrident(worldObj, this, 0, false);
            trident.setThrowableHeading(dx, dy, dz, 1.6F, 5.0F);
            worldObj.spawnEntityInWorld(trident);
            worldObj.playSoundAtEntity(this, "htgt6:entity.drowned.shoot", 1.0F, 0.8F);
            tridentCooldown = 40 + rand.nextInt(20);
        }
    }

    public boolean isChargingTrident() {
        return dataWatcher.getWatchableObjectByte(TRIDENT_CHARGE_DATA) > 0;
    }

    @Override
    public boolean getCanSpawnHere() {
        int x = MathHelper.floor_double(posX), y = MathHelper.floor_double(boundingBox.minY),
                z = MathHelper.floor_double(posZ);
        BiomeGenBase biome = worldObj.getBiomeGenForCoords(x, z);
        boolean frequent = biome == BiomeGenBase.river || biome == BiomeGenBase.frozenRiver;
        int seaLevel = FuturaWorldType120.isFutura(worldObj)
                ? FuturaMountainDensityRouter120.SEA_LEVEL : 63;
        return worldObj.difficultySetting != EnumDifficulty.PEACEFUL
                && BlockKelp.isWater(worldObj.getBlock(x, y, z))
                && (frequent ? rand.nextInt(RIVER_SPAWN_ROLL) == 0
                        : y < seaLevel - MINIMUM_DEPTH_BELOW_SEA && rand.nextInt(OCEAN_SPAWN_ROLL) == 0)
                && isValidLightLevel()
                && worldObj.checkNoEntityCollision(boundingBox)
                && worldObj.getCollidingBoundingBoxes(this, boundingBox).isEmpty();
    }

    @Override protected String getLivingSound(){return isInWater()?"htgt6:entity.drowned.ambient_water":"htgt6:entity.drowned.ambient";}
    @Override protected String getHurtSound(){return isInWater()?"htgt6:entity.drowned.hurt_water":"htgt6:entity.drowned.hurt";}
    @Override protected String getDeathSound(){return isInWater()?"htgt6:entity.drowned.death_water":"htgt6:entity.drowned.death";}
    @Override protected void dropFewItems(boolean hit, int looting) {
        if (worldObj.isRemote) return;
        int flesh = rand.nextInt(3);
        if (looting > 0) flesh += Math.round(rand.nextFloat() * looting);
        if (flesh > 0) entityDropItem(new ItemStack(net.minecraft.init.Items.rotten_flesh, flesh), 0F);
        if (hit && rand.nextFloat() < .11F + Math.max(0, looting) * .02F) {
            entityDropItem(new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.copperIngot), 0F);
        }
    }

    // Drowned has its own loot table, not the zombie's iron/carrot/potato roll.
    @Override protected void dropRareDrop(int bonus) { }

    public boolean carriesNautilusShell() {
        return dataWatcher.getWatchableObjectByte(NAUTILUS_SHELL_DATA) != 0;
    }

    public void setCarriesNautilusShell(boolean value) {
        dataWatcher.updateObject(NAUTILUS_SHELL_DATA, Byte.valueOf((byte)(value ? 1 : 0)));
    }

    @Override protected void dropEquipment(boolean hit, int looting) {
        super.dropEquipment(hit, looting);
        if (!worldObj.isRemote && carriesNautilusShell()) {
            entityDropItem(new ItemStack(OceanContent.nautilusShell), 0F);
            setCarriesNautilusShell(false);
        }
    }

    @Override public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("HTGT6NautilusShell", carriesNautilusShell());
    }

    @Override public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setCarriesNautilusShell(tag.getBoolean("HTGT6NautilusShell"));
    }
}
