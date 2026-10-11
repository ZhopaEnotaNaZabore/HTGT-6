package com.mod.htgt6.common.flotation;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Stable block registrations reused by the reference-based Flotation Chamber. */
public final class FlotationChamberContent {
    public static final Block rib = new com.mod.htgt6.common.multiblock.BlockMultiblockTrim("flotation_chamber_rib", "multiblock_v2_mixer_rib");
    public static final Block controller = new BlockFlotationChamberController();
    public static final Block casing = new BlockFlotationChamberCasing();
    public static final Block glass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;

    private FlotationChamberContent() { }

    public static void register() {
        GameRegistry.registerBlock(controller, ItemBlockFlotationChamberController.class,
                "flotation_chamber");
        GameRegistry.registerBlock(casing, "flotation_chamber_casing");
        GameRegistry.registerTileEntity(MultiTileFlotationChamber.class,
                "htgt6:multi_tile_flotation_chamber");
        GameRegistry.registerBlock(rib, "flotation_chamber_rib");
    }
}
