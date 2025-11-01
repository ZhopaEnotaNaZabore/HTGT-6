package com.mod.htgt6.common.item;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class NovaCrystall extends Item {
    public NovaCrystall () {
        setUnlocalizedName("NovaCrystall");
        setTextureName(HTGT6.MOD_ID + ":NovaCrystall");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
