package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.charon.blocks;

import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

/** New blocks are owned and registered by SU-Limited, not GalaxySpace. */
public final class CharonBlocks {
    public static Block regolith;
    public static Block crust;
    public static Block canyonIce;

    private CharonBlocks() { }

    public static void register() {
        if (regolith != null) return;
        regolith = block("CharonRegolith", TerrainTextureStyle.ROUGH_REGOLITH, 0xCAC3C0, 1.8F, 2);
        crust = block("CharonCrust", TerrainTextureStyle.ROUGH_ROCKY, 0x42413F, 4.5F, 3);
        canyonIce = block("CharonCanyonIce", TerrainTextureStyle.BROKEN_ICE, 0xABBCCC, 3.2F, 2);
    }

    private static Block block(String registryName, TerrainTextureStyle style, int colour,
                               float hardness, int harvestLevel) {
        Block block = new BlockCharonRock(style, colour, hardness, harvestLevel)
                .setBlockName(registryName)
                .setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabBlock);
        GameRegistry.registerBlock(block, registryName);
        return block;
    }
}
