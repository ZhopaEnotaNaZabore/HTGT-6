package com.mod.htgt6.common.item;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class CompositeT2 extends Item {
    public CompositeT2 () {
        setUnlocalizedName("CompositeT2");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+ ":RocketAlloyT2");
    }
}
