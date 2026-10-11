package com.mod.htgt6.minecraft1202.Module.Entity.Exploration;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import java.util.UUID;

public final class EntityLlamaSpit112 extends EntityThrowable {
    private EntityLivingBase owner;
    private UUID ownerId;
    private boolean leftOwner;
    public EntityLlamaSpit112(World world) { super(world); }
    public EntityLlamaSpit112(World world, EntityLivingBase owner) {
        super(world, owner);
        this.owner = owner;
        this.ownerId = owner.getUniqueID();
        // Modern LlamaSpit starts in front of the muzzle, using body yaw.
        float angle = owner.renderYawOffset * (float)Math.PI / 180.0F;
        double reach = (owner.width + 1.0F) * .5D;
        setPosition(owner.posX - reach * net.minecraft.util.MathHelper.sin(angle),
                owner.posY + owner.getEyeHeight() - .1D,
                owner.posZ + reach * net.minecraft.util.MathHelper.cos(angle));
    }

    @Override public void onUpdate() {
        if (isDead) return;
        if (!leftOwner) leftOwner = hasLeftOwner();
        // EntityThrowable's tick embeds in blocks and excludes the owner for only five ticks.
        super.onEntityUpdate();
        double dx = motionX, dy = motionY, dz = motionZ;
        MovingObjectPosition hit = findHit();
        if (hit != null) onImpact(hit);
        if (isDead) return;
        rotationYaw += MathHelper.wrapAngleTo180_float((float)(Math.atan2(dx, dz) * 180 / Math.PI) - rotationYaw) * .2F;
        rotationPitch += MathHelper.wrapAngleTo180_float((float)(Math.atan2(dy, Math.sqrt(dx*dx+dz*dz)) * 180 / Math.PI) - rotationPitch) * .2F;
        if (!touchesAir() || worldObj.isMaterialInBB(boundingBox, net.minecraft.block.material.Material.water)) {
            setDead(); return;
        }
        motionX = dx * .99F;
        motionY = dy * .99F - getGravityVelocity();
        motionZ = dz * .99F;
        setPosition(posX + dx, posY + dy, posZ + dz);
    }

    @Override public EntityLivingBase getThrower() {
        if (owner != null && !owner.isDead && owner.worldObj == worldObj) return owner;
        owner = null;
        if (ownerId != null) {
            for (Object value : worldObj.loadedEntityList) {
                Entity entity = (Entity)value;
                if (entity instanceof EntityLivingBase && !entity.isDead && ownerId.equals(entity.getUniqueID())) {
                    owner = (EntityLivingBase)entity; break;
                }
            }
            return owner;
        }
        // Compatibility with old EntityThrowable saves that only recorded an owner name.
        return super.getThrower();
    }

    private static Entity rootVehicle(Entity entity) {
        for (int depth=0; entity.ridingEntity!=null && depth<64; depth++) entity=entity.ridingEntity;
        return entity;
    }

    private boolean hasLeftOwner() {
        EntityLivingBase shooter = getThrower();
        if (shooter == null) return true;
        Entity root = rootVehicle(shooter);
        for (Object value : worldObj.getEntitiesWithinAABBExcludingEntity(this,
                boundingBox.addCoord(motionX,motionY,motionZ).expand(1,1,1))) {
            Entity entity=(Entity)value;
            if (!entity.isDead && entity.canBeCollidedWith() && rootVehicle(entity)==root) return false;
        }
        return true;
    }

    private MovingObjectPosition findHit() {
        Vec3 start=Vec3.createVectorHelper(posX,posY,posZ);
        Vec3 end=Vec3.createVectorHelper(posX+motionX,posY+motionY,posZ+motionZ);
        MovingObjectPosition hit=worldObj.rayTraceBlocks(start,end);
        if (hit!=null) end=hit.hitVec;
        double nearest=start.squareDistanceTo(end);
        EntityLivingBase shooter=getThrower();
        for (Object value : worldObj.getEntitiesWithinAABBExcludingEntity(this,
                boundingBox.addCoord(motionX,motionY,motionZ).expand(1,1,1))) {
            Entity entity=(Entity)value;
            if (entity.isDead || !entity.canBeCollidedWith() || entity.noClip
                    || !leftOwner && shooter!=null && rootVehicle(entity)==rootVehicle(shooter)) continue;
            MovingObjectPosition candidate=entity.boundingBox.expand(.3F,.3F,.3F).calculateIntercept(start,end);
            if (candidate!=null && start.squareDistanceTo(candidate.hitVec)<nearest) {
                nearest=start.squareDistanceTo(candidate.hitVec); hit=new MovingObjectPosition(entity);
            }
        }
        return hit;
    }

    private boolean touchesAir() {
        for (int x=MathHelper.floor_double(boundingBox.minX); x<=MathHelper.floor_double(boundingBox.maxX); x++)
            for (int y=MathHelper.floor_double(boundingBox.minY); y<=MathHelper.floor_double(boundingBox.maxY); y++)
                for (int z=MathHelper.floor_double(boundingBox.minZ); z<=MathHelper.floor_double(boundingBox.maxZ); z++)
                    if (worldObj.isAirBlock(x,y,z)) return true;
        return false;
    }

    @Override public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (ownerId!=null) { tag.setLong("OwnerMost",ownerId.getMostSignificantBits());tag.setLong("OwnerLeast",ownerId.getLeastSignificantBits()); }
        tag.setBoolean("LeftOwner",leftOwner);
    }

    @Override public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        owner=null;
        ownerId=tag.hasKey("OwnerMost",99)&&tag.hasKey("OwnerLeast",99)
                ? new UUID(tag.getLong("OwnerMost"),tag.getLong("OwnerLeast")) : null;
        leftOwner=tag.getBoolean("LeftOwner");
    }

    @Override protected float getGravityVelocity() { return 0.06F; }

    @Override protected void onImpact(MovingObjectPosition hit) {
        if (worldObj.isRemote) return;
        if (hit.entityHit != null) {
            EntityLivingBase owner = getThrower();
            if (owner != null) hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, owner), 1.0F);
        } else {
            // The reference discards on a block hit, not on an entity hit.
            setDead();
        }
    }
}
