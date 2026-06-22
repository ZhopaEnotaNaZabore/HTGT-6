package com.mod.htgt6.client;




import com.mod.htgt6.client.nei.NEIHTGT6Config;
import com.mod.htgt6.client.render.biomes.lukewarm.FogEventHandler;
import com.mod.htgt6.client.render.biomes.lukewarm.warmOceanWater;
import com.mod.htgt6.client.vanillatweaks.CHAT.*;
//


import com.mod.htgt6.client.vanillatweaks.TAB.TAB;
import com.mod.htgt6.client.vanillatweaks.VanillaTweaksHandler;
import com.mod.htgt6.client.vanillatweaks.VanillaTweaksMultiplayerHandler;
import com.mod.htgt6.client.vanillatweaks.memory.CommandMemory;
import com.mod.htgt6.client.vanillatweaks.memory.MemoryCleaner;
import com.mod.htgt6.client.vanillatweaks.network.NetworkOptimizer;
import com.mod.htgt6.common.CommonProxy;


import com.mod.htgt6.common.handler.recipe.RecipeConfigLoader;

import com.mod.htgt6.common.handler.recipe.compressor.CompressorRecipeHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.lang.reflect.Field;

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
        System.out.println("[HTGT6] Loading Cyan Fog for Ocean completed");
        MinecraftForge.EVENT_BUS.register(new warmOceanWater());
        VanillaTweaksHandler handler = new VanillaTweaksHandler();
        MinecraftForge.EVENT_BUS.register(handler);

        FMLCommonHandler.instance()
                .bus()
                .register(handler);
        MinecraftForge.EVENT_BUS.register(new VanillaTweaksMultiplayerHandler());
        new TAB();
        registerVanillaTweaks();


    }

    public void registerVanillaTweaks() {
        //Chat BAR
        MinecraftForge.EVENT_BUS.register(new ChatGuiInterceptor());
        //HiTech6 Stickers
        ChatEmojiRenderer emojiRenderer = new ChatEmojiRenderer();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(emojiRenderer);
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(emojiRenderer);
        //ChatNEW
        MinecraftForge.EVENT_BUS.register(new ChatRender());
        //LangTweaker

        //NetworkTweaker ALPHA
        new NetworkOptimizer();
        //memory
        new MemoryCleaner();
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new CommandMemory());

        }


    public void registerNEI() {
        NEIHTGT6Config.registerManual();
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


















