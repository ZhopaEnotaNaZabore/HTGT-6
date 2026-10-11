package com.mod.htgt6.common.crusher;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** Blocks and controller registration for the 7x6x16 Industrial Crusher. */
public final class IndustrialCrusherContent {
    public static final Block controller = new BlockIndustrialCrusherController();
    public static final Block casing = new BlockIndustrialCrusherCasing(false);
    public static final Block rib = new BlockIndustrialCrusherCasing(true);
    public static final Block collision = new BlockCrusherCollision();

    private IndustrialCrusherContent() { }

    public static void register() {
        GameRegistry.registerBlock(controller, ItemBlockIndustrialCrusherController.class,
                "industrial_crusher");
        GameRegistry.registerBlock(casing, "industrial_crusher_casing");
        GameRegistry.registerBlock(rib, "industrial_crusher_rib");
        GameRegistry.registerBlock(collision, (Class<? extends net.minecraft.item.ItemBlock>) null,
                "industrial_crusher_collision");
        GameRegistry.registerTileEntity(TileCrusherCollision.class, "htgt6:crusher_collision");
        GameRegistry.registerTileEntity(MultiTileIndustrialCrusher.class,
                "htgt6:multi_tile_industrial_crusher");
    }
}
