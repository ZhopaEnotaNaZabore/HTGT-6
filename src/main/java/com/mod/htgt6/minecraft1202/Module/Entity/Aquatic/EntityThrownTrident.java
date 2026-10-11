package com.mod.htgt6.minecraft1202.Module.Entity.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.entity.effect.EntityLightningBolt;

public final class EntityThrownTrident extends EntityThrowable implements cpw.mods.fml.common.registry.IEntityAdditionalSpawnData {
    public static final float BASE_DAMAGE = 8.0F;
    public static final float IMPALING_DAMAGE_PER_LEVEL = 2.5F;
    public static final float GRAVITY = 0.05F;
    public static final double WATER_INERTIA = 0.99D;
    private ItemStack tridentStack = new ItemStack(OceanContent.trident);
    private boolean collectible;
    private boolean returning;
    private boolean embedded;
    private boolean dealtDamage;
    private int embeddedTicks;
    private boolean hasAnchor;
    private int anchorX, anchorY, anchorZ;

    private static final int STATE_DATA = 20;
    private static final byte STATE_FLYING = 0;
    private static final byte STATE_EMBEDDED = 1;
    private static final byte STATE_RETURNING = 2;

    public EntityThrownTrident(World world) { super(world); }
    public EntityThrownTrident(World world, EntityLivingBase owner) { this(world, owner, 0, false); }
    public EntityThrownTrident(World world, EntityLivingBase owner, int damage, boolean collectible) {
        this(world, owner, new ItemStack(OceanContent.trident, 1, damage), collectible);
    }
    public EntityThrownTrident(World world, EntityLivingBase owner, ItemStack stack, boolean collectible) {
        super(world, owner);
        tridentStack = stack == null ? new ItemStack(OceanContent.trident) : stack.copy();
        tridentStack.stackSize = 1;
        this.collectible = collectible;
    }
    public EntityThrownTrident(World world, double x, double y, double z) { super(world, x, y, z); }

    @Override protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(STATE_DATA, Byte.valueOf(STATE_FLYING));
    }

    @Override
    protected float getGravityVelocity() { return returning ? 0.0F : GRAVITY; }

    @Override
    public void onUpdate() {
        if (worldObj.isRemote) {
            byte state = dataWatcher.getWatchableObjectByte(STATE_DATA);
            returning = state == STATE_RETURNING;
            embedded = state == STATE_EMBEDDED;
        }
        if (embedded) {
            if (!worldObj.isRemote && hasAnchor && !hasEmbeddingSupport()) {
                embedded=false;inGround=false;embeddedTicks=0;hasAnchor=false;
                setState(STATE_FLYING);
                // Gravity resumes next tick; do not keep a projectile suspended in air.
                return;
            }
            lastTickPosX = posX;
            lastTickPosY = posY;
            lastTickPosZ = posZ;
            onEntityUpdate();
            motionX = motionY = motionZ = 0.0D;
            if (!worldObj.isRemote && ++embeddedTicks >= 4
                    && EnchantmentHelper.getEnchantmentLevel(
                    OceanContent.loyalty.effectId, tridentStack) > 0) {
                embedded = false;
                returning = true;
                inGround = false;
                setState(STATE_RETURNING);
            } else if (!worldObj.isRemote && embeddedTicks >= 1200) {
                setDead();
            }
            return;
        }
        if (!worldObj.isRemote && returning) {
            EntityLivingBase owner = getThrower();
            if (owner == null || owner.isDead) {
                dropOrDiscard();
                return;
            }
            double dx = owner.posX - posX;
            double dy = owner.posY + owner.getEyeHeight() - posY;
            double dz = owner.posZ - posZ;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < 1.5D) {
                returnToOwner(owner);
                return;
            }
            int loyalty = EnchantmentHelper.getEnchantmentLevel(OceanContent.loyalty.effectId, tridentStack);
            motionX = motionX * 0.95D + dx / distance * 0.05D * loyalty;
            motionY = motionY * 0.95D + dy / distance * 0.05D * loyalty;
            motionZ = motionZ * 0.95D + dz / distance * 0.05D * loyalty;
            noClip = true;
        }
        float incomingYaw=rotationYaw,incomingPitch=rotationPitch;
        super.onUpdate();
        if(embedded) {rotationYaw=incomingYaw;rotationPitch=incomingPitch;}
        // EntityThrowable hardcodes 0.8 water drag. AbstractArrow and
        // ThrownTrident use getWaterInertia() == 0.99 in Java 1.20.2.
        if (isInWater() && !embedded && !isDead) {
            final double correction = WATER_INERTIA / 0.8D;
            motionX *= correction;
            motionY = (motionY + getGravityVelocity()) * correction - getGravityVelocity();
            motionZ *= correction;
        }
    }

    /** Returning/embedded tridents ignore impacts, including target blocks. */
    public boolean acceptsTargetImpact116(){return !returning&&!embedded&&!isDead;}

    @Override
    protected void onImpact(MovingObjectPosition hit) {
        if (returning) return;
        if (worldObj.isRemote) {
            if (hit.entityHit == null) embedAt(hit);
            return;
        }
        if (hit.entityHit != null) {
            if (dealtDamage) return;
            dealtDamage = true;
            float damage = BASE_DAMAGE;
            int impaling = EnchantmentHelper.getEnchantmentLevel(OceanContent.impaling.effectId, tridentStack);
            if (impaling > 0 && isAquatic(hit.entityHit)) {
                damage += impaling * IMPALING_DAMAGE_PER_LEVEL;
            }
            boolean damaged = hit.entityHit.attackEntityFrom(
                    DamageSource.causeThrownDamage(this, getThrower()), damage);
            if(damaged&&hit.entityHit instanceof net.minecraft.entity.EntityLivingBase&&getThrower() instanceof net.minecraft.entity.player.EntityPlayer)
                com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120.complete((net.minecraft.entity.player.EntityPlayer)getThrower(),"throw_trident");
            int channeling = EnchantmentHelper.getEnchantmentLevel(
                    OceanContent.channeling.effectId, tridentStack);
            if (damaged && channeling > 0 && worldObj.isThundering()) {
                int x = MathHelper.floor_double(hit.entityHit.posX);
                int y = MathHelper.floor_double(hit.entityHit.posY);
                int z = MathHelper.floor_double(hit.entityHit.posZ);
                if (worldObj.canBlockSeeTheSky(x, y, z)) {
                    worldObj.addWeatherEffect(new EntityLightningBolt(worldObj,
                            hit.entityHit.posX, hit.entityHit.posY, hit.entityHit.posZ));
                }
            }
            if (EnchantmentHelper.getEnchantmentLevel(
                    OceanContent.loyalty.effectId, tridentStack) > 0) {
                returning = true;
                setState(STATE_RETURNING);
            } else {
                // AbstractArrow bounces very slightly after an entity hit.
                motionX *= -0.01D;
                motionY *= -0.10D;
                motionZ *= -0.01D;
            }
            worldObj.playSoundAtEntity(this, "htgt6:item.trident.hit_ground", 0.5F, 1.5F);
            return;
        }
        worldObj.playSoundAtEntity(this, "htgt6:item.trident.hit_ground", 0.5F, 1.5F);
        embedAt(hit);
    }

    private void embedAt(MovingObjectPosition hit) {
        hasAnchor=hit!=null && hit.entityHit==null;
        if(hasAnchor){anchorX=hit.blockX;anchorY=hit.blockY;anchorZ=hit.blockZ;}
        if (hit != null && hit.hitVec != null) setPosition(hit.hitVec.xCoord, hit.hitVec.yCoord, hit.hitVec.zCoord);
        motionX = motionY = motionZ = 0.0D;
        embedded = true;
        returning = false;
        inGround = false;
        embeddedTicks = 0;
        setState(STATE_EMBEDDED);
    }

    private boolean hasEmbeddingSupport() {
        // Unloaded chunks are not evidence that a supporting block was removed.
        if(!worldObj.blockExists(anchorX,anchorY,anchorZ))return true;
        net.minecraft.block.Block block=worldObj.getBlock(anchorX,anchorY,anchorZ);
        net.minecraft.util.AxisAlignedBB collision=block.getCollisionBoundingBoxFromPool(worldObj,anchorX,anchorY,anchorZ);
        return collision!=null && boundingBox!=null && collision.intersectsWith(boundingBox.expand(.01D,.01D,.01D));
    }

    private void setState(byte state) {
        dataWatcher.updateObject(STATE_DATA, Byte.valueOf(state));
    }

    private boolean isAquatic(net.minecraft.entity.Entity entity) {
        if (entity instanceof EntitySeaTurtle || entity instanceof net.minecraft.entity.passive.EntitySquid) return true;
        String name = entity.getClass().getSimpleName().toLowerCase();
        return name.contains("guardian") || name.contains("dolphin") || name.contains("axolotl")
                || name.contains("tadpole") || name.contains("fish");
    }

    private void returnToOwner(EntityLivingBase owner) {
        if (collectible && owner instanceof EntityPlayer
                && ((EntityPlayer)owner).inventory.addItemStackToInventory(tridentStack.copy())) {
            worldObj.playSoundAtEntity(this, "htgt6:item.trident.return", 0.2F, 1.5F);
            ((EntityPlayer)owner).onItemPickup(this, 1);
            setDead();
            return;
        }
        // A living owner's full inventory must not turn Loyalty into a loose dropped item.
        if (collectible && owner instanceof EntityPlayer) return;
        dropOrDiscard();
    }

    @Override
    public void onCollideWithPlayer(EntityPlayer player) {
        if (worldObj.isRemote || !embedded || !collectible || player == null) return;
        EntityLivingBase owner = getThrower();
        if (owner != null && owner != player) return;
        if (player.inventory.addItemStackToInventory(tridentStack.copy())) {
            worldObj.playSoundAtEntity(this, "htgt6:item.trident.return", 0.2F, 1.5F);
            player.onItemPickup(this, 1);
            setDead();
        }
    }

    @Override public boolean canBeCollidedWith() { return true; }

    public boolean isEnchanted() { return tridentStack != null && tridentStack.hasEffect(); }

    @Override public void writeSpawnData(io.netty.buffer.ByteBuf buffer) {
        cpw.mods.fml.common.network.ByteBufUtils.writeItemStack(buffer,tridentStack);
        buffer.writeBoolean(collectible);buffer.writeBoolean(returning);buffer.writeBoolean(embedded);
        buffer.writeBoolean(dealtDamage);buffer.writeInt(embeddedTicks);
    }
    @Override public void readSpawnData(io.netty.buffer.ByteBuf buffer) {
        ItemStack stack=cpw.mods.fml.common.network.ByteBufUtils.readItemStack(buffer);
        if(stack!=null && stack.getItem()==OceanContent.trident){tridentStack=stack;tridentStack.stackSize=1;}
        collectible=buffer.readBoolean();returning=buffer.readBoolean();embedded=buffer.readBoolean();
        dealtDamage=buffer.readBoolean();embeddedTicks=Math.max(0,buffer.readInt());
        setState(returning?STATE_RETURNING:embedded?STATE_EMBEDDED:STATE_FLYING);
    }

    private void dropOrDiscard() {
        if (collectible && tridentStack.getItemDamage() < OceanContent.trident.getMaxDamage()) {
            EntityItem dropped = new EntityItem(worldObj, posX, posY, posZ, tridentStack.copy());
            dropped.delayBeforeCanPickup = 10;
            worldObj.spawnEntityInWorld(dropped);
        }
        setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        NBTTagCompound itemTag = new NBTTagCompound();
        tridentStack.writeToNBT(itemTag);
        tag.setTag("TridentItem", itemTag);
        // Retained for worlds saved by the earlier damage-only implementation.
        tag.setInteger("TridentDamage", tridentStack.getItemDamage());
        tag.setBoolean("Collectible", collectible);
        tag.setBoolean("Returning", returning);
        tag.setBoolean("Embedded", embedded);
        tag.setBoolean("DealtDamage", dealtDamage);
        tag.setInteger("EmbeddedTicks", embeddedTicks);
        if(hasAnchor) {
            tag.setInteger("AnchorX",anchorX);tag.setInteger("AnchorY",anchorY);tag.setInteger("AnchorZ",anchorZ);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        ItemStack saved = tag.hasKey("TridentItem", 10)
                ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("TridentItem")) : null;
        tridentStack = saved != null && saved.getItem() == OceanContent.trident
                ? saved : new ItemStack(OceanContent.trident, 1, tag.getInteger("TridentDamage"));
        tridentStack.stackSize = 1;
        collectible = tag.getBoolean("Collectible");
        returning = tag.getBoolean("Returning");
        embedded = tag.getBoolean("Embedded");
        dealtDamage = tag.getBoolean("DealtDamage");
        embeddedTicks = tag.getInteger("EmbeddedTicks");
        hasAnchor=tag.hasKey("AnchorX",99)&&tag.hasKey("AnchorY",99)&&tag.hasKey("AnchorZ",99);
        anchorX=tag.getInteger("AnchorX");anchorY=tag.getInteger("AnchorY");anchorZ=tag.getInteger("AnchorZ");
        setState(returning ? STATE_RETURNING : embedded ? STATE_EMBEDDED : STATE_FLYING);
    }
}
