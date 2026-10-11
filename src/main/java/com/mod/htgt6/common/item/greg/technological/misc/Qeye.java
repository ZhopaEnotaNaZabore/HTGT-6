package com.mod.htgt6.common.item.greg.technological.misc;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class Qeye extends AtlasItem {
    public Qeye () {
        setUnlocalizedName("Qeye");
        setTextureName(HTGT6.MOD_ID+":Qeye");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
