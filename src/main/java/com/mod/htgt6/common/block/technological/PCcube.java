package com.mod.htgt6.common.block.technological;

import com.mod.htgt6.HTGT6;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class PCcube extends Block {
    public PCcube () {
        super(Material.iron);
        setHarvestLevel("pickaxe", 3);
        setBlockTextureName(HTGT6.MOD_ID + ":PCcube");
        setBlockName("PCcube");
        setHardness(20f);
    }
}
