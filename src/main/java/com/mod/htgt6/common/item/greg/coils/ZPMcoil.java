package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class ZPMcoil extends Item {
    public ZPMcoil () {
        setUnlocalizedName("ZPMcoil");
        setTextureName(HTGT6.MOD_ID+ ":ZPMcoil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
