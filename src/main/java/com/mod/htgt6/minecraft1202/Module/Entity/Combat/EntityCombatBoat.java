package com.mod.htgt6.minecraft1202.Module.Entity.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** Network-safe wood variant for the five boats absent from Minecraft 1.7.10. */
public final class EntityCombatBoat extends EntityBoat {
    private static final int VARIANT_WATCHER = 20;
    private static final int PADDLES_WATCHER = 21;
    private final float[] paddlePositions = new float[2];
    private static final float PADDLE_STEP = 0.3926991F;

    public EntityCombatBoat(World world) { super(world); }
    public EntityCombatBoat(World world, double x, double y, double z, int variant) {
        super(world, x, y, z);
        setVariant(variant);
    }

    @Override protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(VARIANT_WATCHER, Byte.valueOf((byte) 1));
        dataWatcher.addObject(PADDLES_WATCHER, Byte.valueOf((byte) 0));
    }

    @Override public void onUpdate() {
        super.onUpdate();
        if (!worldObj.isRemote) {
            int paddles = 0;
            // C0CPacketInput already synchronizes the controlling player's input in 1.7.10.
            if (riddenByEntity instanceof EntityPlayer) {
                EntityPlayer rider = (EntityPlayer) riddenByEntity;
                paddles = paddleMask(rider.moveStrafing > 0, rider.moveStrafing < 0, rider.moveForward > 0);
            }
            dataWatcher.updateObject(PADDLES_WATCHER, Byte.valueOf((byte) paddles));
        }
        tickPaddles();
    }

    public static int paddleMask(boolean left, boolean right, boolean forward) {
        return ((right && !left || forward) ? 1 : 0) | ((left && !right || forward) ? 2 : 0);
    }

    public void tickPaddles() {
        for (int i = 0; i < 2; i++) paddlePositions[i] = paddleActive(i) ? paddlePositions[i] + PADDLE_STEP : 0;
    }

    private boolean paddleActive(int side) {
        return (dataWatcher.getWatchableObjectByte(PADDLES_WATCHER) & (1 << side)) != 0;
    }

    public float getRowingTime(int side, float partialTick) {
        return paddleActive(side) ? paddlePositions[side] - PADDLE_STEP + PADDLE_STEP * partialTick : 0;
    }

    public int getVariant() { return clamp(dataWatcher.getWatchableObjectByte(VARIANT_WATCHER)); }
    public void setVariant(int variant) {
        dataWatcher.updateObject(VARIANT_WATCHER, Byte.valueOf((byte) clamp(variant)));
    }

    @Override public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isEntityInvulnerable()) return false;
        if (worldObj.isRemote || isDead) return true;
        setForwardDirection(-getForwardDirection());
        setTimeSinceHit(10);
        setDamageTaken(getDamageTaken() + amount * 10.0F);
        setBeenAttacked();
        boolean creative = source.getEntity() instanceof EntityPlayer
                && ((EntityPlayer) source.getEntity()).capabilities.isCreativeMode;
        if (creative || getDamageTaken() > 40.0F) {
            if (riddenByEntity != null) riddenByEntity.mountEntity(this);
            if (shouldDropBoat(creative,entityDropsEnabled(worldObj.getGameRules())))
                func_145778_a(CombatContent.boatItem(getVariant()), 1, 0.0F);
            setDead();
        }
        return true;
    }
    public static boolean shouldDropBoat(boolean creative,boolean entityDrops){return !creative&&entityDrops;}
    public static boolean entityDropsEnabled(net.minecraft.world.GameRules rules) {
        // 1.7.10 does not define the modern rule by default. Absence means vanilla default true.
        return !rules.hasRule("doEntityDrops")||rules.getGameRuleBooleanValue("doEntityDrops");
    }

    @Override protected void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setString("Type", CombatContent.BOAT_TYPES[getVariant()]);
    }
    @Override protected void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        String type = tag.getString("Type");
        for (int i = 0; i < CombatContent.BOAT_TYPES.length; ++i)
            if (CombatContent.BOAT_TYPES[i].equals(type)) { setVariant(i); return; }
        setVariant(1);
    }

    @Override public boolean interactFirst(EntityPlayer player) {
        if (isDead || player.isSneaking() || isPassengerOfThisBoat(player)) return false;
        if (worldObj.isRemote) return true;
        return boardPassenger(player);
    }

    /** Legacy mountEntity toggles an existing mount, unlike modern startRiding. */
    public boolean boardPassenger(Entity passenger) {
        if (worldObj.isRemote || isDead || passenger == this || passenger.isDead || isPassengerOfThisBoat(passenger)) return false;
        if (riddenByEntity == null) {
            passenger.mountEntity(this);
            return passenger.ridingEntity == this;
        }
        EntityCombatBoatSeat seat = findSecondSeat();
        if (seat != null && seat.riddenByEntity != null) return false;
        if (seat == null) {
            seat = new EntityCombatBoatSeat(worldObj, this);
            if (!worldObj.spawnEntityInWorld(seat)) {
                seat.setDead();
                return false;
            }
        }
        passenger.mountEntity(seat);
        return passenger.ridingEntity == seat;
    }

    private boolean isPassengerOfThisBoat(Entity entity) {
        return entity.ridingEntity == this || entity.ridingEntity instanceof EntityCombatBoatSeat
                && ((EntityCombatBoatSeat) entity.ridingEntity).getBoat() == this;
    }

    private boolean sharesBoat(Entity entity) {
        return entity == this || isPassengerOfThisBoat(entity)
                || entity instanceof EntityCombatBoatSeat && ((EntityCombatBoatSeat) entity).getBoat() == this;
    }

    @Override public net.minecraft.util.AxisAlignedBB getCollisionBox(Entity entity) {
        return sharesBoat(entity) ? null : super.getCollisionBox(entity);
    }

    @Override public void applyEntityCollision(Entity entity) {
        if (!sharesBoat(entity)) super.applyEntityCollision(entity);
    }

    @Override public net.minecraft.item.ItemStack getPickedResult(net.minecraft.util.MovingObjectPosition target) {
        return new net.minecraft.item.ItemStack(CombatContent.boatItem(getVariant()));
    }

    /** The inherited fall break path emits oak planks through this hook; horizontal impact is disabled by ASM. */
    @Override public net.minecraft.entity.item.EntityItem func_145778_a(Item item, int count, float offset) {
        if (!entityDropsEnabled(worldObj.getGameRules())) return null;
        if (item == Item.getItemFromBlock(net.minecraft.init.Blocks.planks))
            return entityDropItem(new net.minecraft.item.ItemStack(item, count, getVariant()), offset);
        return super.func_145778_a(item, count, offset);
    }

    private EntityCombatBoatSeat findSecondSeat() {
        java.util.List nearby = worldObj.getEntitiesWithinAABB(EntityCombatBoatSeat.class,
                boundingBox.expand(2.0D, 2.0D, 2.0D));
        for (Object value : nearby) {
            EntityCombatBoatSeat seat = (EntityCombatBoatSeat) value;
            if (!seat.isDead && seat.getBoat() == this) return seat;
        }
        return null;
    }

    public static double passengerOffset(int index,boolean twoPassengers) {
        return twoPassengers?(index==0?.2D:-.6D):0D;
    }
    public void positionPassenger(Entity passenger,int index,boolean twoPassengers) {
        double offset=passengerOffset(index,twoPassengers),yaw=Math.toRadians(rotationYaw);
        passenger.setPosition(posX-Math.sin(yaw)*offset,posY+getMountedYOffset()+passenger.getYOffset(),
                posZ+Math.cos(yaw)*offset);
    }
    @Override public void updateRiderPosition() {
        if(riddenByEntity!=null) {
            EntityCombatBoatSeat seat=findSecondSeat();
            positionPassenger(riddenByEntity,0,seat!=null&&seat.riddenByEntity!=null);
        }
    }
    @Override public void setDead() {
        if(worldObj!=null&&!worldObj.isRemote) {
            EntityCombatBoatSeat seat=findSecondSeat();
            if(seat!=null)seat.setDead();
            if(riddenByEntity!=null)riddenByEntity.mountEntity(null);
        }
        super.setDead();
    }

    private static int clamp(int variant) {
        return variant < 0 || variant >= CombatContent.BOAT_TYPES.length ? 1 : variant;
    }
}
