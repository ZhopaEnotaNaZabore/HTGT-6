package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Persistent per-suspect anger; unknown sources cannot transfer anger between players. */
public final class WardenAnger120 {
    private final Map<UUID, Integer> suspects = new HashMap<UUID, Integer>();

    public int get(UUID id) {
        Integer value = suspects.get(id);
        return value == null ? 0 : value;
    }

    public void increase(UUID id, int amount) {
        if (id != null) suspects.put(id, (int) Math.min(150L, Math.max(0L, (long) get(id) + amount)));
    }

    public int highest() {
        int result = 0;
        for (int value : suspects.values()) result = Math.max(result, value);
        return result;
    }

    public void decay() {
        for (Iterator<Map.Entry<UUID, Integer>> iterator = suspects.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            if (entry.getValue() <= 1) iterator.remove();
            else entry.setValue(entry.getValue() - 1);
        }
    }

    public void write(NBTTagCompound root) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, Integer> entry : suspects.entrySet()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("UUID", entry.getKey().toString());
            tag.setInteger("Anger", entry.getValue());
            list.appendTag(tag);
        }
        root.setTag("Suspects", list);
    }

    public void read(NBTTagCompound root) {
        suspects.clear();
        NBTTagList list = root.getTagList("Suspects", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            try {
                increase(UUID.fromString(tag.getString("UUID")), tag.getInteger("Anger"));
            } catch (IllegalArgumentException ignored) {
                // A malformed saved UUID cannot identify an entity to be angry at.
            }
        }
    }
}
