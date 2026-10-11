package com.mod.htgt6.minecraft1202.Module.Data.Exploration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Persistent resolution/completion ledger for chunk-order independent mansion placement. */
public final class WoodlandMansionData112 extends WorldSavedData {
    private static final String NAME = "HTGT6WoodlandMansions112";
    private final Map<Long, Start> starts = new HashMap<Long, Start>();

    public WoodlandMansionData112() { super(NAME); }
    public WoodlandMansionData112(String name) { super(name); }

    public static WoodlandMansionData112 get(World world) {
        WoodlandMansionData112 data = (WoodlandMansionData112) world.perWorldStorage.loadData(
                WoodlandMansionData112.class, NAME);
        if (data == null) {
            data = new WoodlandMansionData112();
            world.perWorldStorage.setData(NAME, data);
        }
        return data;
    }

    public Start getStart(int chunkX, int chunkZ) { return starts.get(key(chunkX, chunkZ)); }

    public Start resolve(int chunkX, int chunkZ, int baseY, boolean valid) {
        long key = key(chunkX, chunkZ);
        Start value = starts.get(key);
        if (value == null) { value = new Start(chunkX, chunkZ); starts.put(key, value); }
        value.resolved = true; value.valid = valid; value.baseY = baseY;
        if (!valid) value.pending.clear();
        markDirty();
        return value;
    }

    public void addPending(int startX, int startZ, int targetX, int targetZ) {
        long key = key(startX, startZ);
        Start value = starts.get(key);
        if (value == null) { value = new Start(startX, startZ); starts.put(key, value); }
        if (!value.completed.contains(key(targetX, targetZ)) && value.pending.add(key(targetX, targetZ))) markDirty();
    }

    public boolean isComplete(Start start, int chunkX, int chunkZ) {
        return start.completed.contains(key(chunkX, chunkZ));
    }

    public void complete(Start start, int chunkX, int chunkZ) {
        long target = key(chunkX, chunkZ);
        if (start.completed.add(target) | start.pending.remove(target)) markDirty();
    }

    public List<Long> pending(Start start) { return new ArrayList<Long>(start.pending); }

    @Override public void readFromNBT(NBTTagCompound root) {
        starts.clear();
        NBTTagList list = root.getTagList("Starts", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            Start start = new Start(tag.getInteger("X"), tag.getInteger("Z"));
            start.resolved = tag.getBoolean("Resolved"); start.valid = tag.getBoolean("Valid");
            start.baseY = tag.getInteger("BaseY");
            readLongSet(tag.getTagList("Pending", 10), start.pending);
            readLongSet(tag.getTagList("Completed", 10), start.completed);
            starts.put(key(start.chunkX, start.chunkZ), start);
        }
    }

    @Override public void writeToNBT(NBTTagCompound root) {
        NBTTagList list = new NBTTagList();
        for (Start start : starts.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("X", start.chunkX); tag.setInteger("Z", start.chunkZ);
            tag.setBoolean("Resolved", start.resolved); tag.setBoolean("Valid", start.valid);
            tag.setInteger("BaseY", start.baseY);
            tag.setTag("Pending", writeLongSet(start.pending));
            tag.setTag("Completed", writeLongSet(start.completed));
            list.appendTag(tag);
        }
        root.setTag("Starts", list);
    }

    private static NBTTagList writeLongSet(Set<Long> values) {
        NBTTagList list = new NBTTagList();
        for (Long value : values) { NBTTagCompound tag = new NBTTagCompound(); tag.setLong("K", value); list.appendTag(tag); }
        return list;
    }
    private static void readLongSet(NBTTagList list, Set<Long> values) {
        for (int i = 0; i < list.tagCount(); ++i) values.add(list.getCompoundTagAt(i).getLong("K"));
    }
    public static long key(int x, int z) { return (x & 0xffffffffL) | ((long) z << 32); }
    public static int keyX(long key) { return (int) key; }
    public static int keyZ(long key) { return (int) (key >> 32); }

    public static final class Start {
        public final int chunkX, chunkZ;
        public boolean resolved, valid;
        public int baseY;
        private final Set<Long> pending = new HashSet<Long>();
        private final Set<Long> completed = new HashSet<Long>();
        private Start(int x, int z) { chunkX = x; chunkZ = z; }
        public int pendingCount() { return pending.size(); }
        public int completedCount() { return completed.size(); }
    }
}
