package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.VillagePoiData120;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.world.World;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/** 1.20 REST activity adapted to the fixed 1.7 height range and entity protocol. */
public final class VillagerSleep120 {
    public static final int WATCHER = 31;
    private static final int SLEEPING = 4;
    private static final String NBT_SLEEPING = "HTGT6VillagerSleeping";
    private static final String NBT_BED_X = "HTGT6VillagerBedX";
    private static final String NBT_BED_Y = "HTGT6VillagerBedY";
    private static final String NBT_BED_Z = "HTGT6VillagerBedZ";
    private static final int INIT_WATCHER = 1;
    private static final int INIT_AI = 2;
    private static final Map<EntityVillager,Boolean> AI_INSTALLED = Collections.synchronizedMap(
            new WeakHashMap<EntityVillager,Boolean>());

    /** Safe while Entity's constructor is still executing; never touches EntityLiving.tasks. */
    public static void initializeWatcher(EntityVillager villager) {
        try { villager.getDataWatcher().addObject(WATCHER, Byte.valueOf((byte)0)); }
        catch (IllegalArgumentException alreadyPresent) { }
    }

    /** Must only run after EntityVillager's constructor has initialized its AI task lists. */
    public static void initializeAfterConstruction(EntityVillager villager) {
        initializeWatcher(villager);
        if (villager.worldObj != null && !villager.worldObj.isRemote && !AI_INSTALLED.containsKey(villager)) {
            villager.tasks.addTask(0, new SleepAI(villager));
            HeroGiftGoal120.attach(villager);
            AI_INSTALLED.put(villager, Boolean.TRUE);
        }
    }

    public static boolean isSleeping(EntityVillager villager) {
        try { return (villager.getDataWatcher().getWatchableObjectByte(WATCHER) & SLEEPING) != 0; }
        catch (RuntimeException missingWatcher) { return false; }
    }

    public static int bedDirection(EntityVillager villager) {
        try { return villager.getDataWatcher().getWatchableObjectByte(WATCHER) & 3; }
        catch (RuntimeException missingWatcher) { return 0; }
    }

    public static boolean isRestTime(long worldTime) {
        long dayTime = worldTime % 24000L;
        return dayTime >= 12000L || dayTime < 10L;
    }

    public static boolean maySleep(EntityVillager villager) {
        return isRestTime(villager.worldObj.getWorldTime()) && villager.hurtTime == 0
                && !villager.isBurning() && !villager.isInWater() && villager.getAttackTarget() == null;
    }

    public static boolean bedAvailable(World world, VillagePoiData120.Entry home, EntityVillager villager) {
        if (home == null || !VillagePoiType120.isHomeHead(world, home.x, home.y, home.z)) return false;
        if ((world.getBlockMetadata(home.x, home.y, home.z) & 4) == 0) return true;
        return villager.getEntityData().getBoolean(NBT_SLEEPING)
                && villager.getEntityData().getInteger(NBT_BED_X) == home.x
                && villager.getEntityData().getInteger(NBT_BED_Y) == home.y
                && villager.getEntityData().getInteger(NBT_BED_Z) == home.z;
    }

    public static void sleep(EntityVillager villager, VillagePoiData120.Entry home) {
        int direction = villager.worldObj.getBlockMetadata(home.x, home.y, home.z) & 3;
        villager.getNavigator().clearPathEntity();
        setSleepingPosition(villager, home.x, home.y, home.z, direction);
        villager.rotationYaw = direction * 90.0F;
        int metadata = villager.worldObj.getBlockMetadata(home.x, home.y, home.z);
        if ((metadata & 4) == 0)
            villager.worldObj.setBlockMetadataWithNotify(home.x, home.y, home.z, metadata | 4, 3);
        villager.getEntityData().setBoolean(NBT_SLEEPING, true);
        villager.getEntityData().setInteger(NBT_BED_X, home.x);
        villager.getEntityData().setInteger(NBT_BED_Y, home.y);
        villager.getEntityData().setInteger(NBT_BED_Z, home.z);
        villager.getDataWatcher().updateObject(WATCHER, Byte.valueOf((byte)(SLEEPING | direction)));
    }

    public static void wake(EntityVillager villager) {
        if (!isSleeping(villager) && !villager.getEntityData().getBoolean(NBT_SLEEPING)) return;
        int direction = bedDirection(villager);
        World world = villager.worldObj;
        int bedX = villager.getEntityData().getInteger(NBT_BED_X);
        int bedY = villager.getEntityData().getInteger(NBT_BED_Y);
        int bedZ = villager.getEntityData().getInteger(NBT_BED_Z);
        if (world.getChunkProvider().chunkExists(bedX >> 4, bedZ >> 4)
                && VillagePoiType120.isHomeHead(world, bedX, bedY, bedZ)) {
            int metadata = world.getBlockMetadata(bedX, bedY, bedZ);
            direction = metadata & 3;
            if ((metadata & 4) != 0) world.setBlockMetadataWithNotify(bedX, bedY, bedZ, metadata & ~4, 3);
        }
        villager.getDataWatcher().updateObject(WATCHER, Byte.valueOf((byte)direction));
        villager.getEntityData().setBoolean(NBT_SLEEPING, false);
        boolean moved = false;
        for (int radius = 1; radius <= 2 && !moved; ++radius)
            for (int offX = -radius; offX <= radius && !moved; ++offX)
                for (int offZ = -radius; offZ <= radius; ++offZ) {
                    if (Math.max(Math.abs(offX), Math.abs(offZ)) != radius) continue;
                    int outX = bedX + offX, outZ = bedZ + offZ;
                    if (world.isAirBlock(outX, bedY, outZ) && world.isAirBlock(outX, bedY + 1, outZ)
                            && World.doesBlockHaveSolidTopSurface(world, outX, bedY - 1, outZ)) {
                        villager.setPosition(outX + .5D, bedY, outZ + .5D);
                        moved = true;
                        break;
                    }
                }
        if (!moved && world.isAirBlock(bedX, bedY + 1, bedZ))
            villager.setPosition(bedX + .5D, bedY + 1, bedZ + .5D);
    }

    static void maintain(EntityVillager villager){
        if(!isSleeping(villager))return;
        villager.getNavigator().clearPathEntity();
        villager.motionX=villager.motionY=villager.motionZ=0.0D;villager.fallDistance=0.0F;
        int x=villager.getEntityData().getInteger(NBT_BED_X);
        int y=villager.getEntityData().getInteger(NBT_BED_Y);
        int z=villager.getEntityData().getInteger(NBT_BED_Z);
        setSleepingPosition(villager,x,y,z,bedDirection(villager));
    }

    /** Matches EntityPlayer's 1.7 sleeping anchor on the head half of a bed. */
    private static void setSleepingPosition(EntityVillager villager,int x,int y,int z,int direction){
        villager.setPosition(x+sleepOffsetX(direction),y+.9375D,z+sleepOffsetZ(direction));
    }

    private static double sleepOffsetX(int direction){return direction==1?.1D:direction==3?.9D:.5D;}
    private static double sleepOffsetZ(int direction){return direction==0?.9D:direction==2?.1D:.5D;}

    /** Same direction-to-yaw mapping used by EntityPlayer#getBedOrientationInDegrees. */
    public static float renderYaw(EntityVillager villager) { return renderYaw(bedDirection(villager)); }
    private static float renderYaw(int direction){
        switch(direction){case 0:return 90.0F;case 1:return 0.0F;case 2:return 270.0F;default:return 180.0F;}
    }

    public static void assertFixture() {
        if (!isRestTime(12000L) || !isRestTime(18000L) || !isRestTime(0L)
                || isRestTime(10L) || isRestTime(11999L))
            throw new IllegalStateException("villager REST schedule differs from 1.20");
        for (int direction = 0; direction < 4; ++direction)
            if ((byte)(SLEEPING | direction) < SLEEPING)
                throw new IllegalStateException("villager sleep watcher lost bed direction");
        double[] expectedX={.5D,.1D,.5D,.9D},expectedZ={.9D,.5D,.1D,.5D};
        float[] expectedYaw={90.0F,0.0F,270.0F,180.0F};
        for(int direction=0;direction<4;++direction)
            if(sleepOffsetX(direction)!=expectedX[direction]||sleepOffsetZ(direction)!=expectedZ[direction]
                    ||renderYaw(direction)!=expectedYaw[direction])
                throw new IllegalStateException("villager sleeping transform differs from the 1.7 bed contract");
        if (initializationPlan(true, true, false) != INIT_WATCHER
                || initializationPlan(false, true, false) != (INIT_WATCHER | INIT_AI)
                || initializationPlan(false, false, false) != INIT_WATCHER
                || initializationPlan(false, true, true) != INIT_WATCHER)
            throw new IllegalStateException("villager initialization order/idempotence regression");
        EntityVillager constructed = new EntityVillager(null);
        initializeWatcher(constructed);
        initializeWatcher(constructed);
        if (constructed.getDataWatcher().getWatchableObjectByte(WATCHER) != 0)
            throw new IllegalStateException("villager watcher initialization is not idempotent");
    }

    /** Pure constructor-order contract used by the headless regression fixture. */
    private static int initializationPlan(boolean constructing, boolean server, boolean aiInstalled) {
        return INIT_WATCHER | (!constructing && server && !aiInstalled ? INIT_AI : 0);
    }

    private VillagerSleep120() { }

    private static final class SleepAI extends EntityAIBase {
        private final EntityVillager villager;
        private SleepAI(EntityVillager value){villager=value;setMutexBits(7);}
        @Override public boolean shouldExecute(){return isSleeping(villager);}
        @Override public boolean continueExecuting(){return isSleeping(villager)&&maySleep(villager);}
        @Override public void startExecuting(){maintain(villager);}
        @Override public void updateTask(){maintain(villager);}
        @Override public void resetTask(){if(isSleeping(villager)&&!maySleep(villager))wake(villager);}
    }
}
