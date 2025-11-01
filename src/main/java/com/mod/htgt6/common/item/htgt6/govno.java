package com.mod.htgt6.common.item.htgt6;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.ItemFood;

public class govno extends ItemFood {
    public govno () {
        super(2,5F, false);
        setUnlocalizedName("Govno");
        setTextureName(HTGT6.MOD_ID+ ":Govno");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setAlwaysEdible();
    }
}
