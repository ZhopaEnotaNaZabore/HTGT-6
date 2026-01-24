package com.mod.htgt6.common.item.htgt6.circuits;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class CircuitsAndParts extends Item {

    public static Item itemInductiveCoil;
    public static Item itemDiode;
    public static Item itemTransistor;

    public static Item itemSMDResistor;
    public static Item itemSMDdiode;
    public static Item itemSMDTransistor;
    public static Item itemSMDCapacitor;
    public static Item itemSMDInductiveCoil;

    public static Item itemADVSMDResistor;
    public static Item  itemADVSMDdiode;
    public static Item itemADVSMDTransistor;
    public static Item itemADVSMDCapacitor;
    public static Item itemADVSMDInductiveCoil;
    public static Item itemOPSMDResistor;
    public static Item  itemOPSMDdiode;
    public static Item itemOPSMDTransistor;
    public static Item itemOPSMDCapacitor;
    public static Item itemOPSMDInductiveCoil;
    public static void CP() {
        //register
        itemDiode = register(new Item(), "itemDiode");
        itemTransistor = register(new Item(), "itemTransistor");
        itemInductiveCoil = register(new Item(), "itemInductiveCoil");

        itemSMDResistor = register(new Item(), "itemSMDResistor");
        itemSMDdiode = register(new Item(), "itemSMDdiode");
        itemSMDTransistor = register(new Item(), "itemSMDTransistor");
        itemSMDCapacitor = register(new Item(), "itemSMDCapacitor");
        itemSMDInductiveCoil = register(new Item(), "itemSMDInductiveCoil");

        itemADVSMDResistor = register(new Item(), "itemADVSMDResistor");
        itemADVSMDdiode = register( new Item(), "itemADVSMDdiode");
        itemADVSMDTransistor = register(new Item(), "itemADVSMDTransistor");
        itemADVSMDCapacitor = register(new Item(), "itemADVSMDCapacitor");
        itemADVSMDInductiveCoil = register(new Item(), "itemADVSMDInductiveCoil");

        itemOPSMDResistor = register(new Item (), "itemOPSMDResistor");
        itemOPSMDdiode = register(new Item(), "itemOPSMDdiode");
        itemOPSMDCapacitor = register(new Item(), "itemOPSMDCapacitor");
        itemOPSMDTransistor = register(new Item(), "itemOPSMDTransistor");
        itemOPSMDInductiveCoil = register(new Item(), "itemOPSMDInductiveCoil");

        //name
        itemDiode.setUnlocalizedName("itemDiode");
        itemTransistor.setUnlocalizedName("itemTransistor");
        itemInductiveCoil.setUnlocalizedName("itemInductiveCoil");

        itemSMDResistor.setUnlocalizedName("itemSMDResistor");
        itemSMDdiode.setUnlocalizedName("itemSMDdiode");
        itemSMDTransistor.setUnlocalizedName("itemSMDTransistor");
        itemSMDCapacitor.setUnlocalizedName("itemSMDCapacitor");
        itemSMDInductiveCoil.setUnlocalizedName("itemSMDInductiveCoil");
        itemADVSMDResistor.setUnlocalizedName("itemADVSMDResistor");
        itemADVSMDdiode.setUnlocalizedName("itemADVSMDdiode");
        itemADVSMDTransistor.setUnlocalizedName("itemADVSMDTransistor");
        itemADVSMDCapacitor.setUnlocalizedName("itemADVSMDCapacitor");
        itemADVSMDInductiveCoil.setUnlocalizedName("itemADVSMDInductiveCoil");
        itemOPSMDResistor.setUnlocalizedName("itemOPSMDResistor");
        itemOPSMDdiode.setUnlocalizedName("itemOPSMDdiode");
        itemOPSMDCapacitor.setUnlocalizedName("itemOPSMDCapacitor");
        itemOPSMDTransistor.setUnlocalizedName("itemOPSMDTransistor");
        itemOPSMDInductiveCoil.setUnlocalizedName("itemOPSMDInductiveCoil");
        //textures
        itemDiode.setTextureName(HTGT6.MOD_ID+":itemDiode");
        itemTransistor.setTextureName(HTGT6.MOD_ID+":itemTransistor");
        itemInductiveCoil.setTextureName(HTGT6.MOD_ID+":itemInductiveCoil");

        itemSMDResistor.setTextureName(HTGT6.MOD_ID+":itemSMDResistor");
        itemSMDdiode.setTextureName(HTGT6.MOD_ID+":itemSMDdiode");
        itemSMDTransistor.setTextureName(HTGT6.MOD_ID+":itemSMDTransistor");
        itemSMDCapacitor.setTextureName(HTGT6.MOD_ID+":itemSMDCapacitor");
        itemSMDInductiveCoil.setTextureName(HTGT6.MOD_ID+":itemSMDInductiveCoil");
        itemADVSMDResistor.setTextureName(HTGT6.MOD_ID+":itemADVSMDResistor");
        itemADVSMDdiode.setTextureName(HTGT6.MOD_ID+":itemADVSMDdiode");
        itemADVSMDTransistor.setTextureName(HTGT6.MOD_ID+":itemADVSMDTransistor");
        itemADVSMDCapacitor.setTextureName(HTGT6.MOD_ID+":itemADVSMDCapacitor");
        itemADVSMDInductiveCoil.setTextureName(HTGT6.MOD_ID+":itemADVSMDInductiveCoil");
        itemOPSMDResistor.setTextureName(HTGT6.MOD_ID+":itemOPSMDResistor");
        itemOPSMDdiode.setTextureName(HTGT6.MOD_ID+":itemOPSMDdiode");
        itemOPSMDCapacitor.setTextureName(HTGT6.MOD_ID+":itemOPSMDCapacitor");
        itemOPSMDInductiveCoil.setTextureName(HTGT6.MOD_ID+":itemOPSMDInductiveCoil");
        itemOPSMDTransistor.setTextureName(HTGT6.MOD_ID+":itemOPSMDTransistor");

    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;

    }

}
