package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars;

import com.mod.htgt6.sulimited.block.IndexedTerrainBlock;
import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/** SU-owned blocks with local 64x64 geological masks. */
public final class MarsMoonBlocks {
    public static Block phobosRegolith, phobosSubsoil, phobosStone, phobosBreccia;
    public static Block deimosRegolith, deimosFracturedRock, deimosBreccia, deimosCoreRock;

    private MarsMoonBlocks() { }

    public static void register() {
        if (phobosRegolith != null) return;
        phobosRegolith = indexedBlock("SUPhobosRegolith", TerrainTextureStyle.ROUGH_REGOLITH, 0x87705E, 1.4F, 1);
        phobosSubsoil = indexedBlock("SUPhobosSubsoil", TerrainTextureStyle.SMOOTH_REGOLITH, 0x685346, 2.0F, 1);
        phobosStone = indexedBlock("SUPhobosStone", TerrainTextureStyle.DENSE_CORE, 0x51463F, 4.4F, 2);
        phobosBreccia = indexedBlock("SUPhobosBreccia", TerrainTextureStyle.CHIPPED_STONE, 0x947661, 3.1F, 2);
        deimosRegolith = indexedBlock("SUDeimosRegolith", TerrainTextureStyle.SMOOTH_REGOLITH, 0x874E31, 1.2F, 1);
        deimosFracturedRock = indexedBlock("SUDeimosFracturedRock", TerrainTextureStyle.CHIPPED_STONE, 0x57392A, 2.8F, 2);
        deimosBreccia = indexedBlock("SUDeimosBreccia", TerrainTextureStyle.ROUGH_ROCKY, 0x8F532E, 3.5F, 2);
        deimosCoreRock = indexedBlock("SUDeimosCoreRock", TerrainTextureStyle.DENSE_CORE, 0x2C201D, 4.8F, 3);
    }

    private static Block indexedBlock(String name, TerrainTextureStyle style, int colour,
                                      float hardness, int harvestLevel) {
        Block block = new IndexedTerrainBlock(Material.rock, style, colour).setHardness(hardness)
                .setResistance(hardness * 2.5F).setBlockName(name).setCreativeTab(CreativeTabs.tabBlock);
        block.setHarvestLevel("pickaxe", harvestLevel);
        GameRegistry.registerBlock(block, name);
        return block;
    }
}
