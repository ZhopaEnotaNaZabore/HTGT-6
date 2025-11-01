package com.mod.htgt6.common.item.greg.materials.quantinum;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class QuantinumGear extends Item {
    public QuantinumGear () {
        setUnlocalizedName("QuantinumGear");
        setTextureName(HTGT6.MOD_ID+ ":QuantGear");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
