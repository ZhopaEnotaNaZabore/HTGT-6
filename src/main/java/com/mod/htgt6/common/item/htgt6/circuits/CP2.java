package com.mod.htgt6.common.item.htgt6.circuits;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class CP2 extends Item {

    public static Item itemWafer1;
    public static Item itemWafer2;
    public static Item itemWafer3;
    public static Item itemWafer4;
public static Item itemWaferRAM;
    public static Item itemWaferCPU;
    public static Item itemWaferIC;
    public static Item itemRAM;
    public static Item itemCPU;
    public static Item itemIC;
    //to do NAND, NANOCENTRAL, QUANTUMBIT

    public static void logisticsCP2 () {

        //REGISTER
        itemWafer1 = register( new Item(), "itemWafer1");
        itemWafer2 = register( new Item(), "itemWafer2");
        itemWafer3 = register( new Item(), "itemWafer3");
        itemWafer4 = register( new Item(), "itemWafer4");
        itemWaferRAM = register( new Item(), "itemWaferRAM");
        itemRAM = register( new Item(), "itemRAM");
        itemWaferCPU = register( new Item(), "itemWaferCPU");
        itemCPU = register( new Item(), "itemCPU");
        itemWaferIC = register( new Item(), "itemWaferIC");
        itemIC = register( new Item(), "itemIC");



        //NAME
        itemWafer1.setUnlocalizedName("itemWafer1");
        itemWafer2.setUnlocalizedName("itemWafer2");
        itemWafer3.setUnlocalizedName("itemWafer3");
        itemWafer4.setUnlocalizedName("itemWafer4");
        itemWaferRAM.setUnlocalizedName("itemWaferRAM");
        itemRAM.setUnlocalizedName("itemRAM");
        itemWaferCPU.setUnlocalizedName("itemWaferCPU");
        itemCPU.setUnlocalizedName("itemCPU");
        itemWaferIC.setUnlocalizedName("itemWaferIC");
        itemIC.setUnlocalizedName("itemIC");

        //TEXTURE
        itemWafer1.setTextureName(HTGT6.MOD_ID+":itemWafer1");
        itemWafer2.setTextureName(HTGT6.MOD_ID+":itemWafer2");
        itemWafer3.setTextureName(HTGT6.MOD_ID+":itemWafer3");
        itemWafer4.setTextureName(HTGT6.MOD_ID+":itemWafer4");
        itemWaferRAM.setTextureName(HTGT6.MOD_ID+":itemWaferRAM");
        itemRAM.setTextureName(HTGT6.MOD_ID+":itemRAM");
        itemWaferCPU.setTextureName(HTGT6.MOD_ID+":itemWaferCPU");
        itemCPU.setTextureName(HTGT6.MOD_ID+":itemCPU");
        itemWaferIC.setTextureName(HTGT6.MOD_ID+":itemWaferIC");
        itemIC.setTextureName(HTGT6.MOD_ID+":itemIC");
    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }
}
