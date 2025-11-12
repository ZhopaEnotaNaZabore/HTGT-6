package com.mod.htgt6.common.item.galaxy.dusts.galaxyspace;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class dustVenus extends Item {
    public dustVenus() {
        setUnlocalizedName("dustVenus");
        setTextureName(HTGT6.MOD_ID+ ":dustVenus");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
