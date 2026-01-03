package com.mod.htgt6.common.item.htgt6.rocketcomputers;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class HiTech6RocketTerminals extends Item {

    public static Item ComputerT1;

    public static void RocketComputers () {
        ComputerT1 = register(new Item(), "RocketTerminalT1");
        ComputerT1.setUnlocalizedName("RocketTerminalT1");
        ComputerT1.setTextureName(HTGT6.MOD_ID+":RocketTerminalT1");
    }


    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
}}
