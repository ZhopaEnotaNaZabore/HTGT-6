package com.mod.htgt6.client;




import com.mod.htgt6.client.render.biomes.lukewarm.FogEventHandler;
import com.mod.htgt6.client.render.biomes.lukewarm.warmOceanWater;
import com.mod.htgt6.client.vanillatweaks.VanillaTweaksHandler;
import com.mod.htgt6.client.vanillatweaks.VanillaTweaksMultiplayerHandler;
import com.mod.htgt6.common.CommonProxy;


import com.mod.htgt6.common.handler.recipe.RecipeConfigLoader;

import com.mod.htgt6.common.handler.recipe.compressor.CompressorRecipeHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import net.minecraftforge.common.MinecraftForge;

import java.io.File;

import static codechicken.nei.NEIClientConfig.configDir;
import static com.mod.htgt6.common.handler.recipe.assembler.ASrecipeHandler.registerNEI;
import static com.mod.htgt6.common.handler.recipe.compressor.CSrecipeHandler.registerSMCNEI;


public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new FogEventHandler());
        System.out.println("[ClientProxy] Registered Cyan Fog Handler");
        MinecraftForge.EVENT_BUS.register(new warmOceanWater());
        //gui test
        VanillaTweaksHandler handler =
                new VanillaTweaksHandler();
        MinecraftForge.EVENT_BUS.register(handler);

        FMLCommonHandler.instance()
                .bus()
                .register(handler);
        MinecraftForge.EVENT_BUS.register(new VanillaTweaksMultiplayerHandler());

    }


    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (event.getSide().isClient()) {
            File file = new File("config/htgt6/assembler.recipes");
            RecipeConfigLoader.loadAssemblerRecipes(file);
            if (event.getSide().isClient()) {
                registerNEI();
            }
        }
        if (event.getSide().isClient()) {File file = new File("config/htgt6/compressor.recipes"); RecipeConfigLoader.loadCompressorRecipes(file); if (event.getSide().isClient()) {
            registerSMCNEI();

        }}
            }
        }


















