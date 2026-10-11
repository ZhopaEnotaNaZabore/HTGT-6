package com.mod.htgt6.client.vanillatweaks.CHAT;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldSavedData;

import java.util.Map;

public class TitleSaveData extends WorldSavedData {
    private static final String DATA_NAME = "HTGT6_PlayerTitles";

    public TitleSaveData(String name) {
        super(name);
    }

    public static TitleSaveData get(World world) {
        // ALWAYS fallback to Overworld (Dimension 0) so titles are server-wide
        World overworld = world;
        if (world != null && !world.isRemote) {
            WorldServer mainWorld = MinecraftServer.getServer().worldServers[0];
            if (mainWorld != null) {
                overworld = mainWorld;
            }
        }

        TitleSaveData instance = (TitleSaveData) overworld.loadItemData(TitleSaveData.class, DATA_NAME);
        if (instance == null) {
            instance = new TitleSaveData(DATA_NAME);
            overworld.setItemData(DATA_NAME, instance);
        }
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        TitleManager.activePlayerTitles.clear();
        for (Object key : nbt.func_150296_c()) {
            String username = (String) key;
            if ("PurchasedByUuid".equals(username)) continue;
            String prefix = nbt.getString(username);
            TitleManager.activePlayerTitles.put(username, prefix);
        }
        TitleManager.purchasedTitles.clear();
        NBTTagCompound purchased = nbt.getCompoundTag("PurchasedByUuid");
        for (Object key : purchased.func_150296_c()) {
            String uuid = (String) key;
            NBTTagList ids = purchased.getTagList(uuid, 8);
            for (int i = 0; i < ids.tagCount(); i++) TitleManager.markPurchased(uuid, ids.getStringTagAt(i));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        for (Map.Entry<String, String> entry : TitleManager.activePlayerTitles.entrySet()) {
            nbt.setString(entry.getKey(), entry.getValue());
        }
        NBTTagCompound purchased = new NBTTagCompound();
        for (Map.Entry<String, java.util.Set<String>> entry : TitleManager.purchasedTitles.entrySet()) {
            NBTTagList ids = new NBTTagList();
            for (String id : entry.getValue()) ids.appendTag(new net.minecraft.nbt.NBTTagString(id));
            purchased.setTag(entry.getKey(), ids);
        }
        nbt.setTag("PurchasedByUuid", purchased);
    }
}
