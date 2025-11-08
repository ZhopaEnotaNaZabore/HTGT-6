package com.mod.htgt6.common.item.greg.materials.CelTit;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class rodCelTit extends Item {
    public rodCelTit () {
        setUnlocalizedName("rodCelTit");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+":rodCelTit");
    }
}
