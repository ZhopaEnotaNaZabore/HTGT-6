package com.mod.htgt6.common.item.greg.materials.CelTit;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class rotorCelTit extends Item {
    public rotorCelTit() {
        setUnlocalizedName("rotorCelTit");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+":rotorCelTit");
    }
}
