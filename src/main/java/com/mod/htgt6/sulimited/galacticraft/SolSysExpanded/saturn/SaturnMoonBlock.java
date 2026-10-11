package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn;

import com.mod.htgt6.sulimited.block.IndexedTerrainBlock;
import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import net.minecraft.block.material.Material;

final class SaturnMoonBlock extends IndexedTerrainBlock {
    SaturnMoonBlock(TerrainTextureStyle style, int colour, float hardness, int harvestLevel) {
        super(Material.rock, style, colour);
        setHardness(hardness);
        setResistance(hardness * 2.5F);
        setHarvestLevel("pickaxe", harvestLevel);
    }
}
