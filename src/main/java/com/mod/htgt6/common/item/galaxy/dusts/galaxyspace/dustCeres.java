package com.mod.htgt6.common.item.galaxy.dusts.galaxyspace;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class dustCeres extends Item {
    public dustCeres() {
        setUnlocalizedName("dustCeres");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+ ":dustCeres");
    }
}
