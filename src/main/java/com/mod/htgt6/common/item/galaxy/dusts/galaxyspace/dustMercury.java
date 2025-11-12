package com.mod.htgt6.common.item.galaxy.dusts.galaxyspace;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class dustMercury extends Item {
    public dustMercury() {
        setUnlocalizedName("dustMercury");
        setTextureName(HTGT6.MOD_ID+ ":dustMercury");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
