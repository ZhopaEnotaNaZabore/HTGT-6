package com.mod.htgt6.client;


import codechicken.nei.api.API;
import codechicken.nei.recipe.DefaultOverlayHandler;
import com.mod.htgt6.client.GUI.GuiAssembler;
import com.mod.htgt6.client.render.biomes.lukewarm.FogEventHandler;
import com.mod.htgt6.client.render.biomes.lukewarm.warmOceanWater;
import com.mod.htgt6.common.CommonProxy;


import com.mod.htgt6.common.handler.recipe.ASrecipeHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import net.minecraftforge.common.MinecraftForge;

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
    }


    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (event.getSide().isClient()) {
            registerNEIHandler();
            registerNEI();
        }
    }

    public void registerNEIHandler() {
        ASrecipeHandler handler = new ASrecipeHandler();
        codechicken.nei.api.API.registerRecipeHandler(handler);
        codechicken.nei.api.API.registerUsageHandler(handler);
        API.registerGuiOverlayHandler(GuiAssembler.class, new DefaultOverlayHandler(), "assembling_machine");
    }
    public void registerNEI() {
        ASrecipeHandler handler = new ASrecipeHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);

        // This links your TileEntity GUI to the recipe handler.
        // Replace 'GuiAssembler.class' with your actual GUI class name.
        // "assembling_machine" must match the 'getOverlayIdentifier' in the handler.
        API.registerGuiOverlay(GuiAssembler.class, "assembling_machine");
    }

}












