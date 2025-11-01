package com.mod.htgt6.common.item;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import net.minecraft.item.Item;
public class NovaCrystallAlloy extends Item {
    public NovaCrystallAlloy() {
        setUnlocalizedName("NovaCrystallAlloyIngot");
        setTextureName(HTGT6.MOD_ID + ":NovaCrystalAlloyIngot");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
    }
}
