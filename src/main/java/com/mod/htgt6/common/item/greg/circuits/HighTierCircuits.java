package com.mod.htgt6.common.item.greg.circuits;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.handler.ModTab;
import com.mod.htgt6.common.item.htgt6.AtlasItem;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Circuit item identities after UV; every icon lives in the electronics atlas. */
public final class HighTierCircuits {
    public enum Tier { PUV1, UX, OLV, OMV, OHV, OEV, OIV, MAX, INFI }
    private static final Item[] ITEMS = new Item[Tier.values().length];
    private static boolean registered;

    private HighTierCircuits() { }

    public static synchronized void register() {
        if (registered) return;
        for (Tier tier : Tier.values()) {
            String name = tier.name() + "Circ";
            Item item = new AtlasItem().setUnlocalizedName(name)
                    .setTextureName(HTGT6.MOD_ID + ":" + name)
                    .setCreativeTab(ModTab.INSTANCE).setMaxStackSize(64);
            GameRegistry.registerItem(item, name);
            ITEMS[tier.ordinal()] = item;
            OreDictionary.registerOre("circuit" + tier.name(), new ItemStack(item));
        }
        registered = true;
    }

    public static ItemStack stack(Tier tier, int amount) {
        if (!registered) throw new IllegalStateException("High-tier circuits are not registered yet");
        return new ItemStack(ITEMS[tier.ordinal()], amount);
    }
}
