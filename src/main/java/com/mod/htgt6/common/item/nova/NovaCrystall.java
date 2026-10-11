package com.mod.htgt6.common.item.nova;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class NovaCrystall extends AtlasItem {
    public NovaCrystall () {
        setUnlocalizedName("NovaCrystall");
        setTextureName(HTGT6.MOD_ID + ":NovaCrystall");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
