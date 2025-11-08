package com.mod.htgt6.common.item.greg.materials.quantinum;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class rotorQuantinum extends Item {
    public rotorQuantinum(){
        setUnlocalizedName("rotorQuantinum");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+":rotorQuantinum");
    }
}
