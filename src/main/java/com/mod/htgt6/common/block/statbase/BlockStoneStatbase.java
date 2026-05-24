package com.mod.htgt6.common.block.statbase;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockStoneStatbase extends Block {

    public BlockStoneStatbase () {
        super(Material.rock);
        this.setHarvestLevel("pickaxe", 3);
        this.setHardness(20f);;
    }
}
