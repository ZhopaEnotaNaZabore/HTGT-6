package com.mod.htgt6.common.Migaration;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Central entry point for migrations owned by the HTGT6 container. */
public final class MigrationDispatcher {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Migration");

    private MigrationDispatcher() {}

    public static void interceptMissingMappings(FMLMissingMappingsEvent event) {
        int backports = LegacyBackportRegistryInterceptor.intercept(event);
        int amunRa = AmunRaRegistryInterceptor.intercept(event);
        if (backports + amunRa > 0) {
            LOGGER.info("Remapped {} legacy registry entries (Minecraft_1.20.2={}, Amun-Ra={})",
                    backports + amunRa, backports, amunRa);
        }
    }
}
