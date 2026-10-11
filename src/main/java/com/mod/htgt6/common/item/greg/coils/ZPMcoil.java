package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class ZPMcoil extends AtlasItem {
    public ZPMcoil () {
        setUnlocalizedName("ZPMcoil");
        setTextureName(HTGT6.MOD_ID+ ":ZPMcoil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
