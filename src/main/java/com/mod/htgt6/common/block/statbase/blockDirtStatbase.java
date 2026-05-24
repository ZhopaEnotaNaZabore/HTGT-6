package com.mod.htgt6.common.block.statbase;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;

public class blockDirtStatbase extends Block {
    public blockDirtStatbase () {
        super(Material.clay);
        this.setHarvestLevel("shovel", 1);
        this.setHardness(20f);;
    }
}
