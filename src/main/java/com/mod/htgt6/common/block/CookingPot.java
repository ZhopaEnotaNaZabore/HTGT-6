package com.mod.htgt6.common.block;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class CookingPot extends Block {
    public CookingPot () {
        super(Material.rock);
        setBlockName("CookingPot");
        setBlockTextureName(HTGT6.MOD_ID+ ":cookingpot");
        setCreativeTab(ModTab.INSTANCE);
    }
}
