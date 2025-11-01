package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class HVcoil extends Item {
    public HVcoil () {
        setUnlocalizedName("HVcoil");
        setTextureName(HTGT6.MOD_ID+ ":HVcoil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
