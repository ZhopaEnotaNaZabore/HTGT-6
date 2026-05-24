package com.mod.htgt6.client;



import com.mod.htgt6.HTGT6;
import com.mod.htgt6.client.render.biomes.lukewarm.FogEventHandler;
import com.mod.htgt6.client.render.biomes.lukewarm.warmOceanWater;
import com.mod.htgt6.common.CommonProxy;



import com.mod.htgt6.common.handler.recipe.ASrecipeHandler;
import com.mod.htgt6.common.handler.recipe.RecipeConfigLoader;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;

import static com.mod.htgt6.common.handler.recipe.ASrecipeHandler.registerNEI;

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
           // registerNEI();
            File file =
                    new File(
                            "config/htgt6/assembler.recipes"
                    );

            RecipeConfigLoader
                    .loadAssemblerRecipes(file);

            if (event.getSide().isClient()) {

                registerNEI();
            }
        }
    }

  //  private void registerNEI() {

   //     ASrecipeHandler handler = new ASrecipeHandler();

   //     API.registerRecipeHandler(handler);

   //     API.registerUsageHandler(handler);

   //     API.registerGuiOverlay(
    //            GuiAssembler.class,
    //            "assembling_machine"
    ///    );

   //     API.registerGuiOverlayHandler(
   //             GuiAssembler.class,
   //             new DefaultOverlayHandler(),
    //            "assembling_machine"
   //     );
    }














