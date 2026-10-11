package com.mod.htgt6.common.item.galaxy.alloys;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class CompositeT1 extends AtlasItem {
    public CompositeT1() {
        setUnlocalizedName("CompositeT1");
        setMaxStackSize(64);
        setTextureName(HTGT6.MOD_ID+ ":RocketAlloyT1");
        setCreativeTab(ModTab.INSTANCE);
    }
}
