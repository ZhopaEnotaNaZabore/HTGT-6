package com.mod.htgt6.common.item;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class PlutoDust extends Item {
    public PlutoDust() {
        setUnlocalizedName("PlutoDust");
        setTextureName(HTGT6.MOD_ID+ ":PlutoDust");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
