package com.mod.htgt6.common;

import com.mod.htgt6.common.achievements.Hitech6AchieveDatabase;
import com.mod.htgt6.common.achievements.Hitech6Achievments;
import com.mod.htgt6.common.handler.ModBlocks;
import com.mod.htgt6.common.handler.ModItems;
import com.mod.htgt6.common.handler.recipes;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        ModItems.register();
        ModBlocks.register();
    }
    public void init(FMLInitializationEvent event) {
        Hitech6AchieveDatabase.initialization();

    }
    public void postInit(FMLPostInitializationEvent event) {
        recipes.registerRecipes();

    }
}
