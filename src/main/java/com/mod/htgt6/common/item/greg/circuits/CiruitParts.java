package com.mod.htgt6.common.item.greg.circuits;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;


public class CiruitParts extends Item {



    public static Item GlassTube;
    public static Item VacuumTube;
    public static Item SimpleResistor;
    public static Item WoodPlate;
    public static Item SimpleCircPlate;

    public static void CircuitPartsDatabase() {
        GlassTube = register(new Item(), "GlassTube");
        GlassTube.setUnlocalizedName("GlassTube");
        GlassTube.setTextureName(HTGT6.MOD_ID+":GlassTube");
        VacuumTube = register(new Item(), "vacuumtube");
        VacuumTube.setUnlocalizedName("vacuumtube");
        VacuumTube.setTextureName(HTGT6.MOD_ID+":vacuumtube");
        SimpleResistor = register(new Item(), "Resistor");
        SimpleResistor.setUnlocalizedName("simpleresistor");
        SimpleResistor.setTextureName(HTGT6.MOD_ID+":Resistor");
        SimpleCircPlate = register(new Item(), "CircuitPlate");
        SimpleCircPlate.setUnlocalizedName("WoodCircuitPlate");
        SimpleCircPlate.setTextureName(HTGT6.MOD_ID+":Board");
        WoodPlate = register(new Item(), "WoodPlate");
        WoodPlate.setUnlocalizedName("WoodPlate");

    }

    public static Item register(Item item, String name) {
        GameRegistry.registerItem(item, name);
        return item;
    }

}
