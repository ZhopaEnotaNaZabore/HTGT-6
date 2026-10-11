package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class ULVcoil extends AtlasItem {
    public ULVcoil () {
        setUnlocalizedName("ULVcoil");
        setTextureName(HTGT6.MOD_ID+ ":ULVcoil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
