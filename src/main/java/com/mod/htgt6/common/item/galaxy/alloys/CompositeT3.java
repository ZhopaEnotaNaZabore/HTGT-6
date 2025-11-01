package com.mod.htgt6.common.item.galaxy.alloys;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class CompositeT3 extends Item {
    public CompositeT3 () {
        setUnlocalizedName("CompositeT3");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+ ":RocketAlloyT3");
    }
}
