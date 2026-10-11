package com.mod.htgt6.minecraft1202.Module.Entity.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** Minecraft 1.8 armor-stand behavior adapted to the 1.7 equipment-slot API. */
public final class EntityBountifulArmorStand extends EntityLiving {
    private static final int FLAGS_WATCHER = 20;
    private static final int FLAG_SMALL = 1;
    private static final int FLAG_SHOW_ARMS = 4;
    private static final int FLAG_NO_BASE = 8;
    private static final int FLAG_MARKER = 16;
    private static final int FLAG_NO_GRAVITY = 32;
    private static final String[] POSE_NAMES = {"Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg"};
    private static final float[][] DEFAULT_POSE = {
        {0,0,0},{0,0,0},{-10,0,-10},{-15,0,10},{-1,0,-1},{1,0,1}
    };
    private int disabledSlots;
    private long lastPlayerHit;

    public EntityBountifulArmorStand(World world) {
        super(world);
        setSize(0.5F, 1.975F);
        setShowArms(false);
        experienceValue = 0;
        for (int slot = 0; slot < equipmentDropChances.length; ++slot) equipmentDropChances[slot] = 1.0F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(FLAGS_WATCHER, Byte.valueOf((byte) 0));
        for (int i = 0; i < POSE_NAMES.length; ++i) dataWatcher.addObject(21 + i, encodePose(DEFAULT_POSE[i]));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(5.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.0D);
    }

    @Override protected boolean isAIEnabled() { return false; }
    /** Prevent EntityLiving's legacy AI from tracking and turning toward nearby players. */
    @Override protected void updateEntityActionState() { }
    @Override protected boolean canDespawn() { return false; }
    @Override public boolean canBePushed() { return false; }
    @Override public boolean canBeCollidedWith() { return !isMarker(); }
    @Override protected void collideWithEntity(Entity entity) { }
    @Override public boolean isEntityInsideOpaqueBlock() { return false; }
    @Override public float getEyeHeight() { return isSmall() ? 0.88875F : 1.7775F; }

    @Override
    public void onLivingUpdate() {
        if (hasNoGravity()) motionY = 0.0D;
        super.onLivingUpdate();
        if (hasNoGravity()) motionY = 0.0D;
        motionX = 0.0D;
        motionZ = 0.0D;
        renderYawOffset = rotationYaw;
        prevRenderYawOffset = rotationYaw;
        rotationYawHead = rotationYaw;
        prevRotationYawHead = rotationYaw;
    }

    public boolean hasArms() { return (dataWatcher.getWatchableObjectByte(FLAGS_WATCHER) & FLAG_SHOW_ARMS) != 0; }
    public boolean hasBasePlate() { return (dataWatcher.getWatchableObjectByte(FLAGS_WATCHER) & FLAG_NO_BASE) == 0; }
    public boolean isSmall() { return hasFlag(FLAG_SMALL); }
    public boolean isMarker() { return hasFlag(FLAG_MARKER); }
    public boolean hasNoGravity() { return hasFlag(FLAG_NO_GRAVITY); }
    public void setShowArms(boolean value) { updateArmorStandFlag(FLAG_SHOW_ARMS, value); }
    public void setNoBasePlate(boolean value) { updateArmorStandFlag(FLAG_NO_BASE, value); }
    public void setSmall(boolean value) { updateArmorStandFlag(FLAG_SMALL, value); refreshSize(); }
    public void setMarker(boolean value) { updateArmorStandFlag(FLAG_MARKER, value); refreshSize(); }
    public void setNoGravity(boolean value) { updateArmorStandFlag(FLAG_NO_GRAVITY, value); }
    private boolean hasFlag(int flag) { return (dataWatcher.getWatchableObjectByte(FLAGS_WATCHER) & flag) != 0; }

    @Override public void func_145781_i(int id) {
        super.func_145781_i(id);
        if (id == FLAGS_WATCHER) refreshSize();
    }

    private void refreshSize() {
        if (isMarker()) setSize(0.0F, 0.0F);
        else if (isSmall()) setSize(0.25F, 0.9875F);
        else setSize(0.5F, 1.975F);
    }

    public float[] getPose(int part) { return decodePose(dataWatcher.getWatchableObjectString(21 + part), DEFAULT_POSE[part]); }
    public void setPose(int part, float[] pose) {
        if (part >= 0 && part < POSE_NAMES.length && pose != null && pose.length >= 3)
            dataWatcher.updateObject(21 + part, encodePose(pose));
    }

    private void updateArmorStandFlag(int flag, boolean value) {
        byte flags = dataWatcher.getWatchableObjectByte(FLAGS_WATCHER);
        dataWatcher.updateObject(FLAGS_WATCHER, Byte.valueOf((byte) (value ? flags | flag : flags & ~flag)));
    }

    @Override
    protected boolean interact(EntityPlayer player) {
        if (player == null || isMarker()) return false;
        ItemStack held = player.getHeldItem();
        int slot = held == null ? clickedEquipmentSlot(player) : equipmentSlot(held);
        if (slot < 0 || held != null && slot == 0 && !hasArms()) return true;
        if (!worldObj.isRemote) swapEquipment(player, slot, held);
        return true;
    }

    private void swapEquipment(EntityPlayer player, int slot, ItemStack held) {
        ItemStack equipped = getEquipmentInSlot(slot);
        if (!canSwapSlot(slot, equipped, held)) return;
        if (player.capabilities.isCreativeMode && equipped == null && held != null) {
            ItemStack placed = held.copy();
            placed.stackSize = 1;
            setCurrentItemOrArmor(slot, placed);
            return;
        }

        if (held != null && held.stackSize > 1) {
            if (equipped != null) return;
            ItemStack placed = held.copy();
            placed.stackSize = 1;
            setCurrentItemOrArmor(slot, placed);
            --held.stackSize;
        } else {
            setCurrentItemOrArmor(slot, held);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, equipped);
        }
        player.inventory.markDirty();
    }

    private boolean canSwapSlot(int slot, ItemStack equipped, ItemStack held) {
        if ((disabledSlots & (1 << slot)) != 0) return false;
        if (equipped != null && (disabledSlots & (1 << (slot + 8))) != 0) return false;
        return held == null || equipped != null || (disabledSlots & (1 << (slot + 16))) == 0;
    }

    /** Reconstructs the missing 1.8 interaction hit vector from the player's eye ray. */
    private int clickedEquipmentSlot(EntityPlayer player) {
        Vec3 start = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLookVec();
        Vec3 end = start.addVector(look.xCoord * 5.0D, look.yCoord * 5.0D, look.zCoord * 5.0D);
        MovingObjectPosition hit = boundingBox.expand(0.1D, 0.1D, 0.1D).calculateIntercept(start, end);
        double y = hit == null ? player.posY - posY + player.getEyeHeight() : hit.hitVec.yCoord - posY;
        return clickedEquipmentSlot(y);
    }

    public int clickedEquipmentSlot(double hitY) {
        boolean small=isSmall(); double y=small?hitY*2:hitY;
        if (y >= .1D && y < .1D+(small?.8D:.45D) && getEquipmentInSlot(1)!=null) return 1;
        if (y >= .9D+(small?.3D:0) && y < .9D+(small?1D:.7D) && getEquipmentInSlot(3)!=null) return 3;
        if (y >= .4D && y < .4D+(small?1D:.8D) && getEquipmentInSlot(2)!=null) return 2;
        if (y >= 1.6D && getEquipmentInSlot(4)!=null) return 4;
        return getEquipmentInSlot(0)!=null?0:-1;
    }

    public static int equipmentSlot(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemArmor) return 4 - ((ItemArmor) stack.getItem()).armorType;
        if (stack != null && (stack.getItem()==net.minecraft.init.Items.skull
                || stack.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.pumpkin))) return 4;
        return 0;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isDead || worldObj.isRemote) return false;
        if (source == DamageSource.outOfWorld) { setDead(); return false; }
        if (isEntityInvulnerable() || isMarker() || isInvisible()) return false;
        if (source.isExplosion()) { dropContentsAndStand(false); setDead(); return false; }
        if(source==DamageSource.inFire){if(!isBurning())setFire(5);else damageStand(.15F);return false;}
        if(source==DamageSource.onFire){damageStand(4.0F);return false;}
        Entity attacker=source.getEntity();Entity direct=source.getSourceOfDamage();
        boolean arrow=direct instanceof EntityArrow;
        boolean player=attacker instanceof EntityPlayer;
        if(!source.isExplosion()&&!arrow&&!player)return false;
        if(player&&!((EntityPlayer)attacker).capabilities.allowEdit)return false;
        if(arrow)direct.setDead();
        if(player&&((EntityPlayer)attacker).capabilities.isCreativeMode){setDead();return true;}
        long now=worldObj.getTotalWorldTime();
        if(arrow||now-lastPlayerHit<=5L){dropContentsAndStand(true);setDead();}
        else {lastPlayerHit=now;velocityChanged=true;worldObj.setEntityState(this,(byte)32);}
        return true;
    }

    private void damageStand(float amount){float health=getHealth()-amount;if(health<=.5F){dropContentsAndStand(false);setDead();}else setHealth(health);}

    private void dropContentsAndStand(boolean dropStand) {
        boolean drops=worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops");
        for (int slot = 0; slot <= 4; ++slot) {
            ItemStack stack = getEquipmentInSlot(slot);
            if (stack != null) {
                if(drops)entityDropItem(stack.copy(), 0.5F);
                setCurrentItemOrArmor(slot, null);
            }
        }
        if (dropStand && drops) entityDropItem(createStandItem(), 0.0F);
    }

    @Override public ItemStack getPickedResult(MovingObjectPosition target) {
        return new ItemStack(BountifulContent.armorStand);
    }

    private ItemStack createStandItem() {
        ItemStack item = new ItemStack(BountifulContent.armorStand);
        if(hasCustomNameTag())item.setStackDisplayName(getCustomNameTag());
        return item;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("ShowArms", hasArms());
        tag.setBoolean("NoBasePlate", !hasBasePlate());
        tag.setBoolean("Small", isSmall());
        tag.setBoolean("Marker", isMarker());
        tag.setBoolean("NoGravity", hasNoGravity());
        tag.setInteger("DisabledSlots", disabledSlots);
        NBTTagCompound pose = new NBTTagCompound();
        for (int i = 0; i < POSE_NAMES.length; ++i) pose.setTag(POSE_NAMES[i], writePose(getPose(i)));
        tag.setTag("Pose", pose);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        setShowArms(tag.getBoolean("ShowArms"));
        setNoBasePlate(tag.getBoolean("NoBasePlate"));
        setSmall(tag.getBoolean("Small"));
        setMarker(tag.getBoolean("Marker"));
        setNoGravity(tag.getBoolean("NoGravity"));
        setInvisible(tag.getBoolean("Invisible"));
        disabledSlots = tag.getInteger("DisabledSlots");
        if (tag.hasKey("Pose", 10)) {
            NBTTagCompound pose = tag.getCompoundTag("Pose");
            for (int i = 0; i < POSE_NAMES.length; ++i)
                if (pose.hasKey(POSE_NAMES[i], 9)) setPose(i, readPose(pose.getTagList(POSE_NAMES[i], 5), DEFAULT_POSE[i]));
        }
    }

    /** Applies the safe armor-stand subset of an item's modern EntityTag without replacing position/equipment. */
    public void applyItemEntityTag(NBTTagCompound tag) {
        if (tag.hasKey("ShowArms")) setShowArms(tag.getBoolean("ShowArms"));
        if (tag.hasKey("NoBasePlate")) setNoBasePlate(tag.getBoolean("NoBasePlate"));
        if (tag.hasKey("Small")) setSmall(tag.getBoolean("Small"));
        if (tag.hasKey("Marker")) setMarker(tag.getBoolean("Marker"));
        if (tag.hasKey("NoGravity")) setNoGravity(tag.getBoolean("NoGravity"));
        if (tag.hasKey("Invisible")) setInvisible(tag.getBoolean("Invisible"));
        if (tag.hasKey("DisabledSlots")) disabledSlots = tag.getInteger("DisabledSlots");
        if (tag.hasKey("Pose", 10)) {
            NBTTagCompound pose = tag.getCompoundTag("Pose");
            for (int i = 0; i < POSE_NAMES.length; ++i)
                if (pose.hasKey(POSE_NAMES[i], 9)) setPose(i, readPose(pose.getTagList(POSE_NAMES[i], 5), DEFAULT_POSE[i]));
        }
    }

    private static NBTTagList writePose(float[] values) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < 3; ++i) list.appendTag(new NBTTagFloat(values[i]));
        return list;
    }

    private static float[] readPose(NBTTagList list, float[] fallback) {
        if (list == null || list.tagCount() != 3) return fallback.clone();
        return new float[]{list.func_150308_e(0), list.func_150308_e(1), list.func_150308_e(2)};
    }

    private static float normalizedAngle(float angle) {
        return Float.isNaN(angle)||Float.isInfinite(angle)?0F:angle%360F;
    }
    private static String encodePose(float[] pose) { return normalizedAngle(pose[0]) + "," + normalizedAngle(pose[1]) + "," + normalizedAngle(pose[2]); }
    private static float[] decodePose(String encoded, float[] fallback) {
        try {
            String[] values = encoded.split(",", -1);
            if (values.length == 3) return new float[]{normalizedAngle(Float.parseFloat(values[0])), normalizedAngle(Float.parseFloat(values[1])), normalizedAngle(Float.parseFloat(values[2]))};
        } catch (RuntimeException ignored) { }
        return fallback.clone();
    }
}
