package com.mod.htgt6.common.item.galaxy.alloys;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class CompositeT2 extends AtlasItem {
    public CompositeT2 () {
        setUnlocalizedName("CompositeT2");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+ ":RocketAlloyT2");
    }
}
