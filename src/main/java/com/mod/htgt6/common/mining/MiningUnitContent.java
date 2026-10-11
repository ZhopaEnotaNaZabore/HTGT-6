package com.mod.htgt6.common.mining;

import com.mod.htgt6.common.multiblock.gateway.TileEntityMultiblockGateway;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Registration kept entirely inside HTGT6; GT5 is only a design reference. */
public final class MiningUnitContent {
    public static final Block rib = new com.mod.htgt6.common.multiblock.BlockMultiblockTrim("mining_unit_rib", "multiblock_v2_mining_rib");
    public static final Block controller = new BlockMiningUnitController();
    public static final Block casing = new BlockMiningUnitCasing();
    public static final Block frame = new BlockMiningUnitFrame();
    public static final Block port = new BlockMiningUnitPort();
    public static final Block formedGlass = com.mod.htgt6.common.multiblock.glass.ObservationGlassContent.glass;

    private MiningUnitContent() { }

    public static void register() {
        GameRegistry.registerBlock(controller, ItemBlockMiningUnitController.class, "multi_te_mining_unit");
        GameRegistry.registerBlock(casing, ItemBlockMiningUnit.class, "mining_unit_casing");
        GameRegistry.registerBlock(frame, ItemBlockMiningUnit.class, "mining_unit_frame");
        GameRegistry.registerBlock(port, ItemBlockMiningUnit.class, "mining_unit_port");
        GameRegistry.registerTileEntity(MultiTEMiningUnit.class, "htgt6:multi_te_mining_unit");
        GameRegistry.registerTileEntity(TileEntityMultiblockGateway.class, "htgt6:multiblock_gateway");
        // Historical id remains loadable; all newly placed gateways use the generic TE.
        GameRegistry.registerTileEntity(TileEntityMiningUnitPort.class, "htgt6:mining_unit_port");
        GameRegistry.registerBlock(rib, "mining_unit_rib");
    }
}
