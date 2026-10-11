package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;

/** Preserve trade identity without restoring a dead villager's health, position or POI claims. */
public final class VillagerConversion114 {
    private static final String[] KEYS = {"Profession", "ProfessionName", "Career", "CareerLevel",
            "Profession120", "VillagerType120", "Offers", "HTGT6Economy145", "Riches"};
    public static NBTTagCompound capture(EntityVillager villager) {
        NBTTagCompound full = new NBTTagCompound();
        villager.writeEntityToNBT(full);
        VillagerCareers112.write(villager, full);
        NBTTagCompound snapshot = new NBTTagCompound();
        for (String key : KEYS) if (full.hasKey(key)) snapshot.setTag(key, full.getTag(key).copy());
        return snapshot;
    }
    public static void restore(EntityVillager villager, NBTTagCompound snapshot) {
        NBTTagCompound merged = new NBTTagCompound();
        villager.writeEntityToNBT(merged);
        for (String key : KEYS) {
            merged.removeTag(key);
            if (snapshot.hasKey(key)) merged.setTag(key, snapshot.getTag(key).copy());
        }
        villager.readEntityFromNBT(merged);
        VillagerCareers112.read(villager, merged);
    }
    private VillagerConversion114() {}
}
