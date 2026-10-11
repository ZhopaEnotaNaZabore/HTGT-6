package com.mod.htgt6.common.TE;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Append-only structural content for the legacy-ID solidifier controller. */
public final class FluidSolidifierContent {
    public static final Block rib = new com.mod.htgt6.common.multiblock.BlockMultiblockTrim("fluid_solidifier_rib", "multiblock_v2_foundry_rib");
    public static final Block casing = new BlockFluidSolidifierCasing();
    public static final Block glass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;
    private FluidSolidifierContent() { }
    public static void register() {
        GameRegistry.registerBlock(casing, "fluid_solidifier_casing");
        GameRegistry.registerBlock(rib, "fluid_solidifier_rib");
    }
}
