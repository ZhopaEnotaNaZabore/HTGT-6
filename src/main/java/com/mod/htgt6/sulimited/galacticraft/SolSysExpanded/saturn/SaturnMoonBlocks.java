package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;

/** Five independent geological palettes, each containing four terrain blocks. */
public final class SaturnMoonBlocks {
    public static Block mimasRegolith, mimasFracturedIce, mimasCoreRock, mimasEjecta;
    public static Block tethysFrost, tethysPorousIce, tethysAncientIce, tethysChasmIce;
    public static Block dioneRegolith, dioneStripedIce, dioneRockyIce, dioneWispyCliff;
    public static Block rheaRegolith, rheaDenseIce, rheaRockyIce, rheaRayEjecta;
    public static Block iapetusRegolith, iapetusBrightIce, iapetusDarkRock, iapetusRidgeRock;

    private SaturnMoonBlocks() { }

    public static void register() {
        if (mimasRegolith != null) return;
        mimasRegolith = block("MimasRegolith", TerrainTextureStyle.ROUGH_REGOLITH, 0x695E5A, 1.4F, 1);
        mimasFracturedIce = block("MimasFracturedIce", TerrainTextureStyle.BROKEN_ICE, 0x695E5A, 2.5F, 2);
        mimasCoreRock = block("MimasCoreRock", TerrainTextureStyle.DENSE_CORE, 0x584D49, 4.8F, 3);
        mimasEjecta = block("MimasEjecta", TerrainTextureStyle.SMOOTH_REGOLITH, 0x6A5F5B, 2.0F, 2);
        tethysFrost = block("TethysFrost", TerrainTextureStyle.SMOOTH_SNOWY, 0x988C86, 1.0F, 1);
        tethysPorousIce = block("TethysPorousIce", TerrainTextureStyle.BROKEN_ICE, 0x9A8D87, 2.0F, 2);
        tethysAncientIce = block("TethysAncientIce", TerrainTextureStyle.SMOOTH_SNOWY, 0x958883, 4.0F, 3);
        tethysChasmIce = block("TethysChasmIce", TerrainTextureStyle.BROKEN_ICE, 0x998D87, 3.0F, 2);
        dioneRegolith = block("DioneRegolith", TerrainTextureStyle.SMOOTH_REGOLITH, 0x7D7069, 1.5F, 1);
        dioneStripedIce = block("DioneStripedIce", TerrainTextureStyle.SMOOTH_SNOWY, 0x7C6F67, 2.7F, 2);
        dioneRockyIce = block("DioneRockyIce", TerrainTextureStyle.ROUGH_ROCKY, 0x786B64, 4.6F, 3);
        dioneWispyCliff = block("DioneWispyCliff", TerrainTextureStyle.SUEDE_STONE, 0x7B6E66, 3.4F, 2);
        rheaRegolith = block("RheaRegolith", TerrainTextureStyle.ROUGH_REGOLITH, 0x988982, 1.7F, 1);
        rheaDenseIce = block("RheaDenseIce", TerrainTextureStyle.BROKEN_ICE, 0x968880, 2.9F, 2);
        rheaRockyIce = block("RheaRockyIce", TerrainTextureStyle.ROUGH_ROCKY, 0x94857E, 4.9F, 3);
        rheaRayEjecta = block("RheaRayEjecta", TerrainTextureStyle.SMOOTH_SANDY, 0x988982, 2.2F, 2);
        iapetusRegolith = block("IapetusRegolith", TerrainTextureStyle.ROUGH_REGOLITH, 0xBCB4AE, 1.8F, 1);
        iapetusBrightIce = block("IapetusBrightIce", TerrainTextureStyle.BROKEN_ICE, 0xD6CBBA, 2.8F, 2);
        iapetusDarkRock = block("IapetusDarkRock", TerrainTextureStyle.SUEDE_STONE, 0x302C2B, 5.0F, 3);
        iapetusRidgeRock = block("IapetusRidgeRock", TerrainTextureStyle.CHIPPED_STONE, 0x6E5D51, 5.8F, 3);
    }

    private static Block block(String name, TerrainTextureStyle style, int colour, float hardness, int harvestLevel) {
        Block block = new SaturnMoonBlock(style, colour, hardness, harvestLevel).setBlockName(name)
                .setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(block, name);
        return block;
    }
}
