package com.mod.htgt6.common.Migaration;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Compatibility bridge for unofficial Amun-Ra builds which used the shortened
 * namespaces "AmunRa" or "amunra".  The official 0.4.8 namespace remains
 * GalacticraftAmunRa inside the combined HTGT6 JAR and therefore needs no
 * conversion for normal worlds.
 */
public final class AmunRaRegistryInterceptor {
    static final String TARGET_MOD_ID = "GalacticraftAmunRa";
    private static final String[] LEGACY_MOD_IDS = {"AmunRa", "amunra"};

    private AmunRaRegistryInterceptor() {}

    static int intercept(FMLMissingMappingsEvent event) {
        int remapped = 0;
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            String path = null;
            for (String legacyModId : LEGACY_MOD_IDS) {
                path = MigrationRegistryName.pathFrom(mapping.name, legacyModId);
                if (path != null) break;
            }
            if (path == null) continue;

            if (mapping.type == GameRegistry.Type.BLOCK) {
                Block target = GameRegistry.findBlock(TARGET_MOD_ID, path);
                if (target != null) {
                    mapping.remap(target);
                    remapped++;
                }
            } else if (mapping.type == GameRegistry.Type.ITEM) {
                Item target = GameRegistry.findItem(TARGET_MOD_ID, path);
                if (target != null) {
                    mapping.remap(target);
                    remapped++;
                }
            }
        }
        return remapped;
    }
}
