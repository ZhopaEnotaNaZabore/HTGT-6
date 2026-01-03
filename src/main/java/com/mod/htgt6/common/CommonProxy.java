package com.mod.htgt6.common;

import com.mod.htgt6.common.achievements.Hitech6AchieveDatabase;
import com.mod.htgt6.common.handler.FluidDatabase;
import com.mod.htgt6.common.handler.ModBlocks;
import com.mod.htgt6.common.handler.ModItems;
import com.mod.htgt6.common.handler.recipes;
import com.mod.htgt6.common.item.greg.circuits.CiruitParts;
import com.mod.htgt6.common.item.greg.materials.firecly.fireclys;
import com.mod.htgt6.common.item.greg.technological.sieves.sieves;
import com.mod.htgt6.common.item.htgt6.coins.HiTech6Coins;
import com.mod.htgt6.common.item.htgt6.rocketcomputers.HiTech6RocketTerminals;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        ModItems.register();
        ModBlocks.register();
        FluidDatabase.register();
        HiTech6Coins.CoinItemStack();
        HiTech6RocketTerminals.RocketComputers();
        fireclys.Fireclys();
        sieves.Sieves();
        CiruitParts.CircuitPartsDatabase();

    }
    public void init(FMLInitializationEvent event) {
        Hitech6AchieveDatabase.initialization();

    }
    public void postInit(FMLPostInitializationEvent event) {
        recipes.registerRecipes();

    }
}
