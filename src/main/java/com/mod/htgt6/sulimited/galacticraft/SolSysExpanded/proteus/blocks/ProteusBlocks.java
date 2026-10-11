package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.proteus.blocks;

import com.mod.htgt6.sulimited.block.IndexedTerrainBlock;
import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public final class ProteusBlocks {
    public static Block regolith;
    public static Block bedrock;
    public static Block impactBreccia;
    private ProteusBlocks() { }

    public static void register() {
        if (regolith != null) return;
        regolith = block("ProteusRegolith", TerrainTextureStyle.ROUGH_REGOLITH, 0x2B2C31, 1.6F, 2);
        bedrock = block("ProteusBedrock", TerrainTextureStyle.DENSE_CORE, 0x292D33, 5.2F, 3);
        impactBreccia = block("ProteusImpactBreccia", TerrainTextureStyle.CHIPPED_STONE, 0x424448, 3.8F, 3);
    }
    private static Block block(String name, TerrainTextureStyle style, int colour, float hardness, int harvest) {
        Block result = new ProteusRock(style, colour, hardness, harvest).setBlockName(name)
                .setCreativeTab(CreativeTabs.tabBlock);
        GameRegistry.registerBlock(result, name);
        return result;
    }
    private static final class ProteusRock extends IndexedTerrainBlock {
        private ProteusRock(TerrainTextureStyle style, int colour, float hardness, int harvest) {
            super(Material.rock, style, colour);
            setHardness(hardness); setResistance(hardness * 2.8F); setHarvestLevel("pickaxe", harvest);
        }
    }
}
