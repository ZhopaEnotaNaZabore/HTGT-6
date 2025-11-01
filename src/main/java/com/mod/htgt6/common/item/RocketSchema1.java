package com.mod.htgt6.common.item;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class RocketSchema1 extends Item {
    public RocketSchema1 (
    )
    {   setUnlocalizedName("RocketSchema1");
        setCreativeTab(ModTab.INSTANCE);
        setMaxStackSize(64);
        setTextureName(HTGT6.MOD_ID+ ":RocketSchema1");
    }
}
