package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class InfiCoil extends Item {
    public InfiCoil () {
        setUnlocalizedName("InfiCoil");
        setTextureName(HTGT6.MOD_ID+ ":InfiCoil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
