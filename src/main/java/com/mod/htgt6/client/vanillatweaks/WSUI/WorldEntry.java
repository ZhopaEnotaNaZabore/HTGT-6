package com.mod.htgt6.client.vanillatweaks.WSUI;

import java.io.File;
import java.io.FileInputStream;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.SaveFormatComparator;

/** 1.12 WorldSummary equivalent backed by the fields available in the 1.7 save format. */
public final class WorldEntry {
    public final String folderName;
    public final String displayName;
    public final long lastPlayed;
    public final boolean conversionRequired;
    public final boolean hardcore;
    public final boolean cheats;
    public final String gameMode;
    public double posX, posY, posZ;
    public String versionName = "1.7.10";
    public int dataVersion;
    public File iconFile;
    public ResourceLocation iconTexture;

    public WorldEntry(SaveFormatComparator summary, File worldDir) {
        folderName = summary.getFileName();
        displayName = summary.getDisplayName();
        lastPlayed = summary.getLastTimePlayed();
        conversionRequired = summary.requiresConversion();
        hardcore = summary.isHardcoreModeEnabled();
        cheats = summary.getCheatsEnabled();
        gameMode = summary.getEnumGameType().getName();
        iconFile = new File(worldDir, "icon.png");
        readExtendedMetadata(new File(worldDir, "level.dat"));
    }

    public boolean isFromNewerVersion() { return dataVersion > 0; }

    private void readExtendedMetadata(File levelDat) {
        if (!levelDat.isFile()) return;
        try {
            FileInputStream input = new FileInputStream(levelDat);
            NBTTagCompound root;
            try { root = CompressedStreamTools.readCompressed(input); } finally { input.close(); }
            NBTTagCompound data = root.getCompoundTag("Data");
            dataVersion = data.getInteger("DataVersion");
            if (data.hasKey("Version")) {
                String name = data.getCompoundTag("Version").getString("Name");
                if (name != null && !name.isEmpty()) versionName = name;
            }
            if (data.hasKey("Player")) {
                net.minecraft.nbt.NBTTagList pos = data.getCompoundTag("Player").getTagList("Pos", 6);
                if (pos.tagCount() >= 3) {
                    posX = pos.func_150309_d(0); posY = pos.func_150309_d(1); posZ = pos.func_150309_d(2);
                }
            }
        } catch (Exception ignored) { }
    }
}
