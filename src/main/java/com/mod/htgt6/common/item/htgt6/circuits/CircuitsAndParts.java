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

    public static Item itemPlasticCircBoard;
    public static Item itemEpoxidCircBoard;
    public static Item itemCrystallCirctBoard;
    public static Item itemOpticalCirctBoard;
    public static Item itemLivingCirctBoard;
    public static Item itemMAXcircBoard;

    public static void CP() {
        //register
        itemPlasticCircBoard = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemPlasticCircBoard");
        itemDiode = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemDiode");
        itemTransistor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemTransistor");
        itemInductiveCoil = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemInductiveCoil");

        itemSMDResistor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemSMDResistor");
        itemSMDdiode = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemSMDdiode");
        itemSMDTransistor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemSMDTransistor");
        itemSMDCapacitor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemSMDCapacitor");
        itemSMDInductiveCoil = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemSMDInductiveCoil");

        itemADVSMDResistor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemADVSMDResistor");
        itemADVSMDdiode = register( new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemADVSMDdiode");
        itemADVSMDTransistor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemADVSMDTransistor");
        itemADVSMDCapacitor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemADVSMDCapacitor");
        itemADVSMDInductiveCoil = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemADVSMDInductiveCoil");

        itemOPSMDResistor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemOPSMDResistor");
        itemOPSMDdiode = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemOPSMDdiode");
        itemOPSMDCapacitor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemOPSMDCapacitor");
        itemOPSMDTransistor = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemOPSMDTransistor");
        itemOPSMDInductiveCoil = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemOPSMDInductiveCoil");
        itemEpoxidCircBoard= register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemEpoxidCircBoard");
        itemCrystallCirctBoard= register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemCrystallCirctBoard");
        itemOpticalCirctBoard= register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemOpticalCirctBoard");
        itemLivingCirctBoard= register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemLivingCirctBoard");
        itemMAXcircBoard= register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemMAXcircBoard");


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
        itemPlasticCircBoard.setUnlocalizedName("itemPlasticCircBoard");
        itemEpoxidCircBoard.setUnlocalizedName("itemEpoxidCircBoard");
        itemCrystallCirctBoard.setUnlocalizedName("itemCrystallCirctBoard");
        itemOpticalCirctBoard.setUnlocalizedName("itemOpticalCirctBoard");
        itemLivingCirctBoard.setUnlocalizedName("itemLivingCirctBoard");
        itemMAXcircBoard.setUnlocalizedName("itemMAXcircBoard");


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
        itemPlasticCircBoard.setTextureName(HTGT6.MOD_ID+":itemPlasticCircBoard");
        itemEpoxidCircBoard.setTextureName(HTGT6.MOD_ID+":itemEpoxidCircBoard");
        itemCrystallCirctBoard.setTextureName(HTGT6.MOD_ID+":itemCrystallCirctBoard");
        itemOpticalCirctBoard.setTextureName(HTGT6.MOD_ID+":itemOpticalCirctBoard");
        itemLivingCirctBoard.setTextureName(HTGT6.MOD_ID+":itemLivingCirctBoard");
        itemMAXcircBoard.setTextureName(HTGT6.MOD_ID+":itemMAXcircBoard");


    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;

    }

}
