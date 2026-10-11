package com.mod.htgt6.minecraft1202.Module.Data.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanAquaticHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.BuriedTreasureHandler;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanStructureHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

/** Persistent index shared by ocean ruins, shipwrecks and their exploration maps. */
public final class AquaticStructureData extends WorldSavedData {
    public static final String RUIN = "ruin";
    public static final String SHIPWRECK = "shipwreck";
    public static final String BURIED_TREASURE = "buried_treasure";
    private static final String DATA_NAME = "HTGT6AquaticStructures";
    // Keep records as vanilla NBT compounds instead of a lazily loaded private
    // inner class. LaunchClassLoader 1.7 can lose access to a not-yet-loaded
    // $Entry class when a development JAR is replaced while Minecraft runs.
    private final List<NBTTagCompound> entries = new ArrayList<NBTTagCompound>();

    public AquaticStructureData(String name) {
        super(name);
    }

    public static void record(World world, String kind, int x, int z) {
        if (world == null || world.isRemote) return;
        AquaticStructureData data = get(world);
        for (NBTTagCompound entry : data.entries) {
            if (entry.getInteger("X") == x && entry.getInteger("Z") == z
                    && entry.getString("Kind").equals(kind)) return;
        }
        data.entries.add(entry(kind, x, z));
        data.markDirty();
    }

    /** Returns one immutable deterministic target for an exploration map. */
    public static int[] findTarget(World world, String kind, int sourceX, int sourceZ) {
        if (RUIN.equals(kind)) {
            return OceanAquaticHandler.findNearestRuinCandidate(world, sourceX, sourceZ);
        }
        if (SHIPWRECK.equals(kind)) {
            return OceanStructureHandler.findNearestShipwreckCandidate(world, sourceX, sourceZ);
        }
        if (BURIED_TREASURE.equals(kind)) {
            return BuriedTreasureHandler.findNearestBuriedTreasureCandidate(world, sourceX, sourceZ);
        }
        return null;
    }

    public static int[] findGeneratedTarget(World world, String kind, int sourceX, int sourceZ) {
        return get(world).findNearest(kind, sourceX, sourceZ);
    }

    /** True only when generation recorded this exact target, not merely a candidate nearby. */
    public static boolean isGeneratedTarget(World world, String kind, int x, int z) {
        if (world == null || world.isRemote) return false;
        for (NBTTagCompound entry : get(world).entries) {
            if (entry.getInteger("X") == x && entry.getInteger("Z") == z
                    && entry.getString("Kind").equals(kind)) return true;
        }
        return false;
    }

    private int[] findNearest(String kind, int x, int z) {
        int[] nearest = null;
        long nearestDistance = Long.MAX_VALUE;
        for (NBTTagCompound entry : entries) {
            if (!entry.getString("Kind").equals(kind)) continue;
            int entryX = entry.getInteger("X"), entryZ = entry.getInteger("Z");
            long dx = (long)entryX - x;
            long dz = (long)entryZ - z;
            long distance = dx * dx + dz * dz;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = new int[] {entryX, entryZ};
            }
        }
        return nearest;
    }

    private static AquaticStructureData get(World world) {
        AquaticStructureData data = (AquaticStructureData)world.loadItemData(
                AquaticStructureData.class, DATA_NAME);
        if (data == null) {
            data = new AquaticStructureData(DATA_NAME);
            world.setItemData(DATA_NAME, data);
        }
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        entries.clear();
        NBTTagList list = tag.getTagList("Structures", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            String kind = entry.getString("Kind");
            if (RUIN.equals(kind) || SHIPWRECK.equals(kind) || BURIED_TREASURE.equals(kind)) {
                entries.add(entry(kind, entry.getInteger("X"), entry.getInteger("Z")));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (NBTTagCompound entry : entries) {
            list.appendTag(entry(entry.getString("Kind"), entry.getInteger("X"), entry.getInteger("Z")));
        }
        tag.setTag("Structures", list);
    }

    private static NBTTagCompound entry(String kind, int x, int z) {
        NBTTagCompound result = new NBTTagCompound();
        result.setString("Kind", kind);
        result.setInteger("X", x);
        result.setInteger("Z", z);
        return result;
    }
}
