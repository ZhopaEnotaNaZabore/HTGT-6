package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** Invisible second passenger anchor; the first passenger remains the controlling rider. */
public final class EntityCombatBoatSeat extends Entity {
    private static final int BOAT_ID_WATCHER = 20;
    private int missingBoatTicks;

    public EntityCombatBoatSeat(World world) {
        super(world); noClip = true; setSize(0.01F, 0.01F);
    }
    public EntityCombatBoatSeat(World world, EntityCombatBoat boat) {
        this(world); setBoat(boat); follow(boat);
    }
    @Override protected void entityInit() { dataWatcher.addObject(BOAT_ID_WATCHER, Integer.valueOf(-1)); }
    public void setBoat(EntityCombatBoat boat) {
        dataWatcher.updateObject(BOAT_ID_WATCHER, Integer.valueOf(boat == null ? -1 : boat.getEntityId()));
    }
    public EntityCombatBoat getBoat() {
        Entity value = worldObj.getEntityByID(dataWatcher.getWatchableObjectInt(BOAT_ID_WATCHER));
        return value instanceof EntityCombatBoat ? (EntityCombatBoat) value : null;
    }
    @Override public void onUpdate() {
        super.onUpdate();
        updateSeat();
    }
    public void updateSeat() {
        EntityCombatBoat boat = getBoat();
        if (boat == null || boat.isDead) {
            if (++missingBoatTicks > 40 || !worldObj.isRemote) setDead();
            return;
        }
        missingBoatTicks = 0; follow(boat);
        if(!worldObj.isRemote&&riddenByEntity!=null&&riddenByEntity.isDead)riddenByEntity.mountEntity(null);
        if(!worldObj.isRemote&&boat.riddenByEntity==null&&riddenByEntity!=null) {
            riddenByEntity.mountEntity(boat);
            setDead();
            return;
        }
        if (!worldObj.isRemote && riddenByEntity == null && ticksExisted > 20) setDead();
    }
    private void follow(EntityCombatBoat boat) {
        double yaw = Math.toRadians(boat.rotationYaw),offset=EntityCombatBoat.passengerOffset(1,true);
        setPosition(boat.posX-Math.sin(yaw)*offset,boat.posY+boat.getMountedYOffset(),boat.posZ+Math.cos(yaw)*offset);
        rotationYaw = boat.rotationYaw;
    }
    @Override public void updateRiderPosition() {
        EntityCombatBoat boat=getBoat();
        if (riddenByEntity != null&&boat!=null)boat.positionPassenger(riddenByEntity,1,true);
    }
    @Override public void setDead() {
        if(worldObj!=null&&!worldObj.isRemote&&riddenByEntity!=null)riddenByEntity.mountEntity(null);
        super.setDead();
    }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean writeToNBTOptional(NBTTagCompound tag) { return false; }
    @Override protected void writeEntityToNBT(NBTTagCompound tag) { }
    @Override protected void readEntityFromNBT(NBTTagCompound tag) { }
}
