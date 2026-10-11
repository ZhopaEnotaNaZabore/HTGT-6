package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class MVcoil extends AtlasItem {
    public MVcoil () {
        setUnlocalizedName("MVcoil");
        setTextureName(HTGT6.MOD_ID+ ":MVcoil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
