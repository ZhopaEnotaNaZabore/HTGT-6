package com.mod.htgt6.minecraft1202.Module.Data.Combat;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Persistent 1.7 structure-start index. It gives each igloo candidate exactly one start. */
public final class IglooStructureData120 extends WorldSavedData {
    private static final String NAME="HTGT6IglooStarts120";
    private final Set<Long> starts=new HashSet<Long>();
    public IglooStructureData120(){super(NAME);}
    public IglooStructureData120(String name){super(name);}

    public static IglooStructureData120 get(World world){
        IglooStructureData120 data=(IglooStructureData120)world.perWorldStorage.loadData(IglooStructureData120.class,NAME);
        if(data==null){data=new IglooStructureData120();world.perWorldStorage.setData(NAME,data);}
        return data;
    }

    /** Atomically reserves a structure start before its pieces touch the world. */
    public synchronized boolean claim(int chunkX,int chunkZ){
        if(!starts.add(key(chunkX,chunkZ)))return false;
        markDirty();return true;
    }
    public synchronized boolean contains(int chunkX,int chunkZ){return starts.contains(key(chunkX,chunkZ));}
    public synchronized int size(){return starts.size();}

    @Override public synchronized void readFromNBT(NBTTagCompound root){
        starts.clear();NBTTagList list=root.getTagList("Starts",10);
        for(int i=0;i<list.tagCount();i++)starts.add(list.getCompoundTagAt(i).getLong("Key"));
    }
    @Override public synchronized void writeToNBT(NBTTagCompound root){
        NBTTagList list=new NBTTagList();for(Long key:starts){NBTTagCompound tag=new NBTTagCompound();
            tag.setLong("Key",key.longValue());list.appendTag(tag);}root.setTag("Starts",list);
    }
    public static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
    public static void assertRoundTripFixture(){
        IglooStructureData120 source=new IglooStructureData120();
        if(!source.claim(-12,31)||source.claim(-12,31)||!source.claim(4,-9))
            throw new IllegalStateException("igloo start claim is not unique");
        NBTTagCompound tag=new NBTTagCompound();source.writeToNBT(tag);
        IglooStructureData120 copy=new IglooStructureData120();copy.readFromNBT(tag);
        if(copy.size()!=2||!copy.contains(-12,31)||!copy.contains(4,-9)||copy.claim(-12,31))
            throw new IllegalStateException("igloo start index did not survive NBT round-trip");
    }
}
