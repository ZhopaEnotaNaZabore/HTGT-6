package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class IVcoil extends Item {
    public IVcoil () {
        setUnlocalizedName("IVcoil");
        setTextureName(HTGT6.MOD_ID+ ":IVcoil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
