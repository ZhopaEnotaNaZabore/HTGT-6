package com.mod.htgt6.common.item.greg.materials.arcanite;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;

public class ingotArcanite extends Item {
    public ingotArcanite () {
        setUnlocalizedName("ingotArcanite");
        setMaxStackSize(64);
        setTextureName(HTGT6.MOD_ID+":ingotArcanite");
        setCreativeTab(ModTab.INSTANCE);
    }
}
