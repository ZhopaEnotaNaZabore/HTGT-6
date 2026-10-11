package com.mod.htgt6.common.centrifuge;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Blocks and controller registration for the 7x5x7 Industrial Centrifuge. */
public final class IndustrialCentrifugeContent {
    public static final Block controller = new BlockIndustrialCentrifugeController();
    public static final Block casing = new BlockIndustrialCentrifugeCasing(false);
    public static final Block rib = new BlockIndustrialCentrifugeCasing(true);
    public static final Block glass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;

    private IndustrialCentrifugeContent() { }

    public static void register() {
        GameRegistry.registerBlock(controller, ItemBlockIndustrialCentrifugeController.class,
                "industrial_centrifuge");
        GameRegistry.registerBlock(casing, "industrial_centrifuge_casing");
        GameRegistry.registerBlock(rib, "industrial_centrifuge_rib");
        GameRegistry.registerTileEntity(MultiTileIndustrialCentrifuge.class,
                "htgt6:multi_tile_industrial_centrifuge");
    }
}
