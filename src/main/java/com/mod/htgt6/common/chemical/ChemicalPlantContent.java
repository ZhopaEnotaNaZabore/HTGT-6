package com.mod.htgt6.common.chemical;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Chemical Plant content; generic gateways are reused from the shared multiblock module. */
public final class ChemicalPlantContent {
    public static final Block rib = new com.mod.htgt6.common.multiblock.BlockMultiblockTrim("chemical_plant_rib", "multiblock_v2_chemical_rib");
    public static final Block controller = new BlockChemicalPlantController();
    public static final Block casing = new BlockChemicalPlantCasing();
    public static final Block glass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;

    private ChemicalPlantContent() { }

    public static void register() {
        GameRegistry.registerBlock(controller, ItemBlockChemicalPlantController.class, "multi_tile_chemical_plant");
        GameRegistry.registerBlock(casing, ItemBlockChemicalPlant.class, "chemical_plant_casing");
        GameRegistry.registerTileEntity(MultiTileChemicalPlant.class, "htgt6:multi_tile_chemical_plant");
        GameRegistry.registerBlock(rib, "chemical_plant_rib");
    }
}
