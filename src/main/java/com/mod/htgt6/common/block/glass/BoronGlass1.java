package com.mod.htgt6.common.block.glass;


import com.mod.htgt6.HTGT6;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BoronGlass1 extends Block {
    public BoronGlass1 (){
        super(Material.glass);
        setHarvestLevel("pickaxe", 3);
        setBlockTextureName(HTGT6.MOD_ID+ ":BornoGlass1");
        setBlockName("BoronGlass1");
        setHardness(20f);
    }
}
