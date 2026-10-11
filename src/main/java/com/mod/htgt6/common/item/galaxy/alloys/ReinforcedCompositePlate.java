package com.mod.htgt6.common.item.galaxy.alloys;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;

/** Reinforced plate counterpart of the existing T1-T6 rocket composite ingots. */
public final class ReinforcedCompositePlate extends AtlasItem {
    public ReinforcedCompositePlate(int tier) {
        if (tier < 1 || tier > 6) throw new IllegalArgumentException("Composite tier: " + tier);
        String name = "ReinforcedCompositePlateT" + tier;
        setUnlocalizedName(name);
        setTextureName(HTGT6.MOD_ID + ":" + name);
        setCreativeTab(ModTab.INSTANCE);
        setMaxStackSize(64);
    }
}
