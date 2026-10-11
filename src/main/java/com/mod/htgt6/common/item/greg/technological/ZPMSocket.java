package com.mod.htgt6.common.item.greg.technological;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;
public class ZPMSocket extends AtlasItem {
    public  ZPMSocket () {
        setUnlocalizedName("ZPMSocket");
        setMaxStackSize(64);
        setCreativeTab(ModTab.INSTANCE);
        setTextureName(HTGT6.MOD_ID+ ":ZPMsocket");
    }
}
