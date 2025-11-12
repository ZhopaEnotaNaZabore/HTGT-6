package com.mod.htgt6.common.item.galaxy.dusts.extendedplanets;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class dustRukan extends Item {
    public dustRukan () {
        setUnlocalizedName("dustRukan");
        setMaxStackSize(64);
        setTextureName(HTGT6.MOD_ID+":dustRukan");
        setCreativeTab(ModTab.INSTANCE);
    }
}
