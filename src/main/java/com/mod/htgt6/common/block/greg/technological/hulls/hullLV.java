package com.mod.htgt6.common.block.greg.technological.hulls;

import com.mod.htgt6.HTGT6;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class hullLV extends Block {
    public hullLV() {
        super(Material.iron);
        setHarvestLevel("pickaxe", 3);
        setBlockTextureName(HTGT6.MOD_ID+ ":hullLV");
        setBlockName("hullLV");
        setHardness(20f);
    }
}
