package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;

import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;

/**
 * Distinct registry blocks backed by the existing SU texture atlas.  Reusing
 * authored rock/ice textures avoids adding fifteen near-duplicate PNG files.
 */
public final class MinorWorldBlocks {
    public static Block arielSurface, arielCrust, arielCore;
    public static Block haumeaSurface, haumeaCrust, haumeaCore;
    public static Block makemakeSurface, makemakeCrust, makemakeCore;
    public static Block erisSurface, erisCrust, erisCore;
    public static Block deeDeeSurface, deeDeeCrust, deeDeeCore;

    private MinorWorldBlocks() { }

    public static void register() {
        if (arielSurface != null) return;
        arielSurface = block("SUArielSurface", TerrainTextureStyle.SMOOTH_SNOWY, 0x988C86, 1.5F, 1);
        arielCrust = block("SUArielCrust", TerrainTextureStyle.BROKEN_ICE, 0x9A8D87, 2.7F, 2);
        arielCore = block("SUArielCore", TerrainTextureStyle.DENSE_CORE, 0x958883, 4.4F, 2);
        haumeaSurface = block("SUHaumeaSurface", TerrainTextureStyle.SMOOTH_SNOWY, 0xD6CBBA, 1.8F, 2);
        haumeaCrust = block("SUHaumeaCrust", TerrainTextureStyle.BROKEN_ICE, 0xB3B1AC, 3.2F, 2);
        haumeaCore = block("SUHaumeaCore", TerrainTextureStyle.DENSE_CORE, 0x252526, 5.0F, 3);
        makemakeSurface = block("SUMakemakeSurface", TerrainTextureStyle.SMOOTH_REGOLITH, 0x874E31, 1.7F, 2);
        makemakeCrust = block("SUMakemakeCrust", TerrainTextureStyle.CHIPPED_STONE, 0x8F532E, 3.4F, 2);
        makemakeCore = block("SUMakemakeCore", TerrainTextureStyle.DENSE_CORE, 0x2C201D, 5.2F, 3);
        erisSurface = block("SUErisSurface", TerrainTextureStyle.SMOOTH_SNOWY, 0xCAC3C0, 1.9F, 2);
        erisCrust = block("SUErisCrust", TerrainTextureStyle.BROKEN_ICE, 0xABBCCC, 3.6F, 2);
        erisCore = block("SUErisCore", TerrainTextureStyle.DENSE_CORE, 0x42413F, 5.4F, 3);
        deeDeeSurface = block("SUDeeDeeSurface", TerrainTextureStyle.ROUGH_REGOLITH, 0x2B2C31, 1.9F, 2);
        deeDeeCrust = block("SUDeeDeeCrust", TerrainTextureStyle.CHIPPED_STONE, 0x424448, 3.8F, 3);
        deeDeeCore = block("SUDeeDeeCore", TerrainTextureStyle.DENSE_CORE, 0x292D33, 5.8F, 3);
    }

    private static Block block(String name, TerrainTextureStyle style, int colour, float hardness, int harvestLevel) {
        Block block = new MinorWorldBlock(style, colour, hardness, harvestLevel).setBlockName(name)
                .setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(block, name);
        return block;
    }
}
