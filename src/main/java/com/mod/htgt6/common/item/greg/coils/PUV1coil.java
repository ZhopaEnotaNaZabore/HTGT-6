package com.mod.htgt6.common.item.greg.coils;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class PUV1coil extends Item {
    public PUV1coil () {
        setUnlocalizedName("PUV1coil");
        setTextureName(HTGT6.MOD_ID+ ":PUV1coil");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
