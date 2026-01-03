package com.mod.htgt6.common.block.greg.technological.hulls;

import com.mod.htgt6.HTGT6;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class hullMV extends Block {
    public hullMV () {
        super(Material.iron);
        setHarvestLevel("pickaxe", 3);
        setBlockTextureName(HTGT6.MOD_ID+ ":hullMV");
        setBlockName("hullMV");
        setHardness(20f);
    }
}
