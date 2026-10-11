package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus;

import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;

/** Independent four-block geological palettes for Uranus's outer moons. */
public final class UranusMoonBlocks {
    public static Block umbrielRegolith, umbrielFracturedIce, umbrielBreccia, umbrielCoreRock;
    public static Block titaniaRegolith, titaniaFaultIce, titaniaRockyIce, titaniaCoreRock;
    public static Block oberonRegolith, oberonCraterIce, oberonDarkRock, oberonBreccia;
    private UranusMoonBlocks() { }
    public static void register() {
        if (umbrielRegolith != null) return;
        umbrielRegolith=block("UmbrielRegolith",TerrainTextureStyle.SMOOTH_REGOLITH,0x1F1D1D,1.6F,1);
        umbrielFracturedIce=block("UmbrielFracturedIce",TerrainTextureStyle.BROKEN_ICE,0x252527,2.8F,2);
        umbrielBreccia=block("UmbrielImpactBreccia",TerrainTextureStyle.CHIPPED_STONE,0x282625,3.7F,2);
        umbrielCoreRock=block("UmbrielCoreRock",TerrainTextureStyle.DENSE_CORE,0x1E1C1D,5.0F,3);
        titaniaRegolith=block("TitaniaRegolith",TerrainTextureStyle.SMOOTH_REGOLITH,0xC8C1B9,1.3F,1);
        titaniaFaultIce=block("TitaniaFaultIce",TerrainTextureStyle.BROKEN_ICE,0xC5C6C5,2.7F,2);
        titaniaRockyIce=block("TitaniaRockyIce",TerrainTextureStyle.ROUGH_ROCKY,0xB9B6B1,3.8F,2);
        titaniaCoreRock=block("TitaniaCoreRock",TerrainTextureStyle.DENSE_CORE,0x9C9388,5.1F,3);
        oberonRegolith=block("OberonRegolith",TerrainTextureStyle.ROUGH_REGOLITH,0x484038,1.7F,1);
        oberonCraterIce=block("OberonCraterIce",TerrainTextureStyle.BROKEN_ICE,0xB3B1AC,2.6F,2);
        oberonDarkRock=block("OberonDarkRock",TerrainTextureStyle.SUEDE_STONE,0x252526,5.2F,3);
        oberonBreccia=block("OberonImpactBreccia",TerrainTextureStyle.CHIPPED_STONE,0x676664,3.9F,2);
    }
    private static Block block(String name,TerrainTextureStyle style,int colour,float hardness,int harvest){Block block=new UranusMoonBlock(style,colour,hardness,harvest).setBlockName(name).setCreativeTab(CreativeTabs.tabBlock);GameRegistry.registerBlock(block,name);return block;}
}
