package com.mod.htgt6.common.item.galaxy.dusts.extendedplanets;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class dustHelema extends Item {
    public dustHelema () {
        setUnlocalizedName("dustHelema");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID +":dustHelema");
    }
}
