package com.mod.htgt6.common.item;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;
public class InfiSing extends Item {
    public  InfiSing () {
        setUnlocalizedName("InfiSing");
        setTextureName(HTGT6.MOD_ID+ ":InfinitySingularity");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
