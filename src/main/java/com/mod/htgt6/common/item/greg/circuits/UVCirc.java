package com.mod.htgt6.common.item.greg.circuits;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class UVCirc extends Item {
    public UVCirc () {
        setUnlocalizedName("UVCirc");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+ ":UVCorc");
    }
}
