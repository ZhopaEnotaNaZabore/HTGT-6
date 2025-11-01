package com.mod.htgt6.common.block;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class EternalBlock extends Block {
    public EternalBlock() {
        super(Material.rock);
        setBlockName("EternalBlock");
        setBlockTextureName(HTGT6.MOD_ID+ ":EternalBlock");
        setCreativeTab(ModTab.INSTANCE);
        setHardness(3);
    }
}
