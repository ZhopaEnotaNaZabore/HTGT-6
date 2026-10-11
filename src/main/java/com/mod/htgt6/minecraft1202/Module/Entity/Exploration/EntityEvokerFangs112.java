package com.mod.htgt6.minecraft1202.Module.Entity.Exploration;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Delayed evoker-fangs strike with the 1.12 warmup/lifetime NBT contract. */
public final class EntityEvokerFangs112 extends Entity implements cpw.mods.fml.common.registry.IEntityAdditionalSpawnData {
    private int warmupDelayTicks;
    private int lifeTicks = 22;
    private EntityLivingBase owner;
    private java.util.UUID ownerUUID;
    private boolean attackStarted;

    public EntityEvokerFangs112(World world) { super(world); setSize(0.5F, 0.8F); }
    public EntityEvokerFangs112(World world, double x, double y, double z, float yaw,
            int warmup, EntityLivingBase owner) {
        this(world);
        setLocationAndAngles(x, y, z, (float)Math.toDegrees(yaw), 0.0F);
        warmupDelayTicks = warmup;
        this.owner = owner;
        this.ownerUUID = owner == null ? null : owner.getUniqueID();
    }

    @Override protected void entityInit() { }

    @Override public void onUpdate() {
        super.onUpdate();
        if (worldObj.isRemote) {
            if (attackStarted) --lifeTicks;
            return;
        }
        if (--warmupDelayTicks < 0) {
            if (warmupDelayTicks == -8) damageNearby();
            if (!attackStarted) {
                attackStarted = true;
                worldObj.setEntityState(this, (byte)4);
            }
            if (--lifeTicks < 0) setDead();
        }
    }

    @SuppressWarnings("unchecked")
    private void damageNearby() {
        if (owner == null && ownerUUID != null) for (Object value : worldObj.loadedEntityList) {
            if (value instanceof EntityLivingBase && ownerUUID.equals(((EntityLivingBase)value).getUniqueID())) { owner=(EntityLivingBase)value; break; }
        }
        AxisAlignedBB box = boundingBox.expand(0.2D, 0.0D, 0.2D);
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
        for (EntityLivingBase entity : entities) {
            if (!entity.isEntityAlive() || entity.isEntityInvulnerable() || entity == owner
                    || owner != null && (owner.isOnSameTeam(entity)
                    || owner instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112
                    && entity instanceof com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112)) continue;
            entity.attackEntityFrom(owner == null ? DamageSource.magic
                    : DamageSource.causeIndirectMagicDamage(this, owner), 6.0F);
        }
    }

    public float animationProgress(float partialTicks) {
        return !attackStarted ? 0 : Math.max(0, Math.min(1, 1-(lifeTicks-2-partialTicks)/20F));
    }

    @Override protected void readEntityFromNBT(NBTTagCompound tag) {
        warmupDelayTicks = tag.getInteger("Warmup");
        attackStarted = tag.getBoolean("AttackStarted");
        owner = null; ownerUUID = null;
        if(tag.hasKey("OwnerUUID"))try{ownerUUID=java.util.UUID.fromString(tag.getString("OwnerUUID"));}catch(IllegalArgumentException ignored){}
        lifeTicks = tag.hasKey("LifeTicks", 99) ? tag.getInteger("LifeTicks") : 22;
    }

    @Override protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("Warmup", warmupDelayTicks);
        tag.setBoolean("AttackStarted",attackStarted);
        if(ownerUUID!=null)tag.setString("OwnerUUID",ownerUUID.toString());
        tag.setInteger("LifeTicks", lifeTicks);
    }
    @Override public void handleHealthUpdate(byte state) {
        if(state==4)attackStarted=true;else super.handleHealthUpdate(state);
    }
    @Override public void writeSpawnData(io.netty.buffer.ByteBuf buffer) {
        buffer.writeInt(warmupDelayTicks);buffer.writeInt(lifeTicks);buffer.writeBoolean(attackStarted);
    }
    @Override public void readSpawnData(io.netty.buffer.ByteBuf buffer) {
        warmupDelayTicks=buffer.readInt();lifeTicks=buffer.readInt();attackStarted=buffer.readBoolean();
    }
}
