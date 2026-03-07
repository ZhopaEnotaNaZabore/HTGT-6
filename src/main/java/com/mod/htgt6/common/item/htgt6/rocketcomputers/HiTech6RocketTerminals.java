package com.mod.htgt6.common.item.htgt6.rocketcomputers;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class HiTech6RocketTerminals extends Item {


    public static Item itemMonitor;
    public static Item itemDisplay;
    public static Item ComputerT1;


    public static void RocketComputers () {

        //REG
        ComputerT1 = register(new Item(), "RocketTerminalT1");
        itemMonitor = register(new Item(), "itemMonitor");
        itemDisplay = register(new Item(), "itemDisplay");

        //NAME
        ComputerT1.setUnlocalizedName("RocketTerminalT1");
        itemMonitor.setUnlocalizedName("itemMonitor");
        itemDisplay.setUnlocalizedName("itemDisplay");

        //TEXTURE
        ComputerT1.setTextureName(HTGT6.MOD_ID+":RocketTerminalT1");
        itemMonitor.setTextureName(HTGT6.MOD_ID+":itemMonitor");
        itemDisplay.setTextureName(HTGT6.MOD_ID+":itemDisplay");
    }


    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
}}
