package com.mod.htgt6.asm;

import java.io.File;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.SaveFormatOld;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.DimensionManager;

/** Resolves Forge's pre-world save directory without opening a second save session. */
public final class WorldSaveDirectoryHooks {
    private WorldSaveDirectoryHooks() {}

    public static File current() {
        WorldServer world = DimensionManager.getWorld(0);
        if (world != null) return ((SaveHandler) world.getSaveHandler()).getWorldDirectory();
        MinecraftServer server = MinecraftServer.getServer();
        return server == null ? null : resolve(server.getActiveAnvilConverter(), server.getFolderName());
    }

    static File resolve(ISaveFormat format, String name) {
        // A custom converter may choose a different layout. Preserve its original lookup contract.
        if (format.getClass() != AnvilSaveConverter.class && format.getClass() != SaveFormatOld.class)
            return ((SaveHandler) format.getSaveLoader(name, false)).getWorldDirectory();
        File directory = new File(((SaveFormatOld) format).savesDirectory, name);
        // AE2 uses mkdir, not mkdirs, for its children during SERVER_ABOUT_TO_START.
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory())
            throw new IllegalStateException("Cannot create world save directory: " + directory);
        return directory;
    }
}
