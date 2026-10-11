package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public final class EndFightData extends WorldSavedData {
    private static final String NAME = "HTGT6EndFight19";
    private int gateways;
    private boolean modernGatewayOrder=true;
    private int respawnState;
    private int respawnTicks;
    private boolean initialDragonSpawned;
    public EndFightData() { super(NAME); }
    public EndFightData(String name) { super(name); }
    public static EndFightData get(World world) {
        EndFightData data = (EndFightData) world.perWorldStorage.loadData(EndFightData.class, NAME);
        if (data == null) { data = new EndFightData(); world.perWorldStorage.setData(NAME, data); }
        return data;
    }
    /** Returns the next entry of the 1.12 shuffled ring without changing old saves. */
    public int nextGateway(long seed) {
        if(gateways>=20)return -1;
        java.util.List<Integer> order = new java.util.ArrayList<Integer>();
        for (int i = 0; i < 20; i++) order.add(i);
        java.util.Collections.shuffle(order, new java.util.Random(seed));
        int value = order.get(modernGatewayOrder?19-gateways:gateways);
        if (gateways < 20) gateways++;
        markDirty(); return value;
    }
    public boolean isRespawning() { return respawnState != 0; }
    public void beginRespawn() { respawnState = 1; respawnTicks = 0; markDirty(); }
    public int tickRespawn() { respawnTicks++; markDirty(); return respawnTicks; }
    public int getRespawnState() { return respawnState; }
    public void setRespawnState(int state) { respawnState = state; respawnTicks = 0; markDirty(); }
    public boolean claimInitialDragon() { if(initialDragonSpawned)return false;initialDragonSpawned=true;markDirty();return true; }
    @Override public void readFromNBT(NBTTagCompound tag) { gateways = Math.max(0,Math.min(20,tag.getInteger("Gateways")));modernGatewayOrder=tag.hasKey("ModernGatewayOrder")?tag.getBoolean("ModernGatewayOrder"):gateways==0; respawnState=tag.getInteger("RespawnState");respawnTicks=tag.getInteger("RespawnTicks");initialDragonSpawned=tag.getBoolean("InitialDragonSpawned"); }
    @Override public void writeToNBT(NBTTagCompound tag) { tag.setInteger("Gateways", gateways);tag.setBoolean("ModernGatewayOrder",modernGatewayOrder);tag.setInteger("RespawnState",respawnState);tag.setInteger("RespawnTicks",respawnTicks);tag.setBoolean("InitialDragonSpawned",initialDragonSpawned); }
}
