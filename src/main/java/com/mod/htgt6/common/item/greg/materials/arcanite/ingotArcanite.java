package com.mod.htgt6.common.item.greg.materials.arcanite;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

public class ingotArcanite extends AtlasItem {
    public ingotArcanite () {
        setUnlocalizedName("ingotArcanite");
        setMaxStackSize(64);
        setTextureName(HTGT6.MOD_ID+":ingotArcanite");
        setCreativeTab(ModTab.INSTANCE);
    }
}
