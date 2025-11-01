package com.mod.htgt6.common.item;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;
public class SMDdiode extends Item {
    public SMDdiode () {
        setUnlocalizedName("SMDdiode");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+ ":SMDdiode");
    }
}
