package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;

import com.mod.htgt6.sulimited.block.IndexedTerrainBlock;
import com.mod.htgt6.sulimited.block.TerrainTextureStyle;
import net.minecraft.block.material.Material;

final class MinorWorldBlock extends IndexedTerrainBlock {
    MinorWorldBlock(TerrainTextureStyle style, int colour, float hardness, int harvestLevel) {
        super(Material.rock, style, colour);
        setHardness(hardness);
        setResistance(hardness * 5.0F);
        setStepSound(soundTypeStone);
        setHarvestLevel("pickaxe", harvestLevel);
    }
}
