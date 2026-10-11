package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.neptune;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

/** Persistent generation markers: removing a deck block must not rebuild the base. */
public final class NeptuneLandingData extends WorldSavedData {
    private final Set<Long> bases = new HashSet<Long>();

    public NeptuneLandingData(String name) { super(name); }

    public static NeptuneLandingData get(World world) {
        String name = "su_neptune_landings_" + world.provider.dimensionId;
        NeptuneLandingData data = (NeptuneLandingData) world.mapStorage.loadData(NeptuneLandingData.class, name);
        if (data == null) {
            data = new NeptuneLandingData(name);
            world.mapStorage.setData(name, data);
        }
        return data;
    }

    public boolean contains(int x, int z) { return bases.contains(key(x, z)); }
    public boolean containsDeckColumn(int x, int z) {
        for (Long position : bases) {
            int dx = x - (int) (position >> 32), dz = z - (int) (long) position;
            if (Math.abs(dx) <= 4 && Math.abs(dz) <= 4
                    || Math.abs(dx) <= 3 && dz >= -11 && dz <= -5) return true;
        }
        return false;
    }
    public void record(int x, int z) { if (bases.add(key(x, z))) markDirty(); }
    private static long key(int x, int z) { return ((long) x << 32) | (z & 0xffffffffL); }

    @Override public void readFromNBT(NBTTagCompound tag) {
        bases.clear();
        NBTTagList list = tag.getTagList("Bases", 10);
        for (int i = 0; i < list.tagCount(); i++) bases.add(list.getCompoundTagAt(i).getLong("Position"));
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Long position : bases) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setLong("Position", position);
            list.appendTag(entry);
        }
        tag.setTag("Bases", list);
    }
}
