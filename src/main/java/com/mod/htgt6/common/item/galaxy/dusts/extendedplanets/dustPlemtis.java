package com.mod.htgt6.common.item.galaxy.dusts.extendedplanets;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class dustPlemtis extends Item {
    public dustPlemtis () {
        setUnlocalizedName("dustPlemtis");
        setMaxStackSize(64);
        setTextureName(HTGT6.MOD_ID+":dustPlemtis");
        setCreativeTab(ModTab.INSTANCE);
    }
}
