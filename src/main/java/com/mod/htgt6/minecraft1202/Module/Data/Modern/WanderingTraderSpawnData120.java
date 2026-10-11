package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Persistent daily spawn cadence matching the modern wandering-trader spawner. */
public final class WanderingTraderSpawnData120 extends WorldSavedData {
    private static final String NAME="HTGT6WanderingTraderSpawner120";
    public int tickDelay=1200,spawnDelay=24000,spawnChance=25;
    public WanderingTraderSpawnData120(){super(NAME);}
    public WanderingTraderSpawnData120(String name){super(name);}
    public static WanderingTraderSpawnData120 get(World world){
        WanderingTraderSpawnData120 data=(WanderingTraderSpawnData120)world.perWorldStorage.loadData(
                WanderingTraderSpawnData120.class,NAME);
        if(data==null){data=new WanderingTraderSpawnData120();world.perWorldStorage.setData(NAME,data);}
        return data;
    }
    @Override public void readFromNBT(NBTTagCompound tag){tickDelay=positive(tag.getInteger("TickDelay"),1200);
        spawnDelay=positive(tag.getInteger("SpawnDelay"),24000);spawnChance=Math.max(25,Math.min(75,tag.getInteger("SpawnChance")));}
    @Override public void writeToNBT(NBTTagCompound tag){tag.setInteger("TickDelay",tickDelay);
        tag.setInteger("SpawnDelay",spawnDelay);tag.setInteger("SpawnChance",spawnChance);}
    private static int positive(int value,int fallback){return value>0?value:fallback;}
}
