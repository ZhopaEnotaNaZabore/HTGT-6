package com.mod.htgt6.common.TE;

import com.mod.htgt6.common.block.hitech6.BlockSuperMassiveCompressorCasing;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** New append-only content; the legacy controller remains registered as SMCompressor. */
public final class SuperMassiveCompressorContent {
    public static final Block rib = new com.mod.htgt6.common.multiblock.BlockMultiblockTrim("super_massive_compressor_rib", "multiblock_v2_compressor_rib");
    public static final Block casing = new BlockSuperMassiveCompressorCasing();
    public static final Block glass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;
    private SuperMassiveCompressorContent() { }
    public static void register() {
        GameRegistry.registerBlock(casing, "super_massive_compressor_casing");
        // Appended after the existing casing to preserve its numeric block ID.
        GameRegistry.registerBlock(rib, "super_massive_compressor_rib");
    }
}
