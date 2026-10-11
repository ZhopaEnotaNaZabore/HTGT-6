package com.mod.htgt6.common.Migaration;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.minecraft1202.Minecraft1202;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

/**
 * Moves only registry entries which used to belong to HTGT6 and now actually
 * exist in the Minecraft_1.20.2 container.  Looking the target up before the
 * remap is important: ordinary HTGT6 machines and items must stay in HTGT6.
 */
public final class LegacyBackportRegistryInterceptor {
    private LegacyBackportRegistryInterceptor() {}

    static int intercept(FMLMissingMappingsEvent event) {
        int remapped = 0;
        for (FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
            String path = MigrationRegistryName.pathFrom(mapping.name, HTGT6.MOD_ID);
            if (path == null) continue;

            if (mapping.type == GameRegistry.Type.BLOCK) {
                Block target = GameRegistry.findBlock(Minecraft1202.MOD_ID, path);
                if (target != null) {
                    mapping.remap(target);
                    remapped++;
                }
            } else if (mapping.type == GameRegistry.Type.ITEM) {
                Item target = GameRegistry.findItem(Minecraft1202.MOD_ID, path);
                if (target != null) {
                    mapping.remap(target);
                    remapped++;
                }
            }
        }
        return remapped;
    }
}
