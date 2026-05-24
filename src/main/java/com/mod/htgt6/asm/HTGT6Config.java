package com.mod.htgt6.asm;


import net.minecraftforge.common.config.Configuration;

import java.io.File;

public final class HTGT6Config {

    public static boolean ENABLE_BIOME_PATCHES = true;

    public static void load(File minecraftDir) {

        File cfgFile = new File(
                minecraftDir,
                "config/htgt6asm.cfg"
        );

        Configuration cfg =
                new Configuration(cfgFile);

        cfg.load();

        ENABLE_BIOME_PATCHES =
                cfg.get(
                        "general",
                        "enableBiomePatches",
                        true
                ).getBoolean(true);

        cfg.save();

        System.out.println(
                "[HTGT6 ASM] enableBiomePatches="
                        + ENABLE_BIOME_PATCHES
        );
    }

    private HTGT6Config() {}
}
