package com.mod.htgt6.sulimited.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;

/** A neutral indexed terrain texture coloured with one per-material RGB value. */
public class IndexedTerrainBlock extends Block {
    private final int tint;

    public IndexedTerrainBlock(Material material, TerrainTextureStyle style, int targetAverageColour) {
        super(material);
        this.tint = style.tintForAverage(targetAverageColour);
        setBlockTextureName(style.textureName());
    }

    @Override
    public int getBlockColor() {
        return tint;
    }

    @Override
    public int getRenderColor(int metadata) {
        return tint;
    }

    @Override
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return tint;
    }
}
