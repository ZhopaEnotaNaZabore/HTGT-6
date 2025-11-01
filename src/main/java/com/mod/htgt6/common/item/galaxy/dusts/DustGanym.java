package com.mod.htgt6.common.item.galaxy.dusts;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;
public class DustGanym extends Item {
    public DustGanym () {
        setUnlocalizedName("DustGanym");
        setTextureName(HTGT6.MOD_ID+ ":DustGanym");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }

}
