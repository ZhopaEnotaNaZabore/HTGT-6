package com.mod.htgt6.common.mixer;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Blocks and controller registration for the 5x10x5 Industrial Mixer. */
public final class IndustrialMixerContent {
    public static final Block rib = new com.mod.htgt6.common.multiblock.BlockMultiblockTrim("industrial_mixer_rib", "multiblock_v2_mixer_rib");
    public static final Block controller = new BlockIndustrialMixerController();
    public static final Block casing = new BlockIndustrialMixerCasing();
    public static final Block glass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;

    private IndustrialMixerContent() { }

    public static void register() {
        GameRegistry.registerBlock(controller, ItemBlockIndustrialMixerController.class,
                "industrial_mixer");
        GameRegistry.registerBlock(casing, "industrial_mixer_casing");
        GameRegistry.registerTileEntity(MultiTileIndustrialMixer.class,
                "htgt6:multi_tile_industrial_mixer");
        GameRegistry.registerBlock(rib, "industrial_mixer_rib");
    }
}
