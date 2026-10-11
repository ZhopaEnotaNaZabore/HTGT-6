package com.mod.htgt6.common.electronics;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Blocks and controller registration for the HTGT6 Electronics Factory. */
public final class ElectronicFactoryContent {
    public static final Block controller = new BlockElectronicFactoryController();
    public static final Block casing = new BlockElectronicFactoryCasing();
    public static final Block rib = new BlockElectronicFactoryCasing(true);
    public static final Block glass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;

    private ElectronicFactoryContent() { }

    public static void register() {
        GameRegistry.registerBlock(controller, ItemBlockElectronicFactoryController.class,
                "te_electronic_factory");
        GameRegistry.registerBlock(casing, "electronic_factory_casing");
        GameRegistry.registerBlock(rib, "electronic_factory_rib");
        GameRegistry.registerTileEntity(TEElectronicFactory.class, "htgt6:te_electronic_factory");
    }
}
