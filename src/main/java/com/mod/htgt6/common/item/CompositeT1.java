package com.mod.htgt6.common.item;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class CompositeT1 extends Item {
    public CompositeT1() {
        setUnlocalizedName("CompositeT1");
        setMaxStackSize(64);
        setTextureName(HTGT6.MOD_ID+ ":RocketAlloyT1");
        setCreativeTab(ModTab.INSTANCE);
    }
}
