package com.mod.htgt6.common.block.greg.technological.hulls;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class blockHullStatbase extends Block {
    public blockHullStatbase () {
        super(Material.iron);
        this.setHarvestLevel("pickaxe", 3);
        this.setHardness(20f);
    }
}
