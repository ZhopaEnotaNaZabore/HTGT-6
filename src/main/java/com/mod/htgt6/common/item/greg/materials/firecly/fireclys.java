package com.mod.htgt6.common.item.greg.materials.firecly;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class fireclys extends Item {

    public static Item UnfiredFireclyBrick;
    public static Item FireclayBrick;

    public static void Fireclys(
            ) {
        UnfiredFireclyBrick = register(new Item(), "UnfFireclybrick");
        UnfiredFireclyBrick.setUnlocalizedName("UnfFireClyBrick");
        UnfiredFireclyBrick.setTextureName(HTGT6.MOD_ID+":UnfFIreClyBrick");
        FireclayBrick = register(new Item(), "FireclayBrick");
        FireclayBrick.setUnlocalizedName("FireClayBrick");
        FireclayBrick.setTextureName(HTGT6.MOD_ID+":FireClay");

    }


    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}
