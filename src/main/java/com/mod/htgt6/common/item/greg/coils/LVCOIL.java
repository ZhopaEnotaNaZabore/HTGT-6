package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class LVCOIL extends Item {
    public LVCOIL () {
        setUnlocalizedName("LVCOIL");
        setTextureName(HTGT6.MOD_ID+ ":LVCOIL");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
