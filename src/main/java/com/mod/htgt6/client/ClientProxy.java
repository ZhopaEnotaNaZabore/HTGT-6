package com.mod.htgt6.client;


import com.mod.htgt6.client.render.biomes.lukewarm.FogEventHandler;
import com.mod.htgt6.client.render.biomes.lukewarm.warmOceanWater;
import com.mod.htgt6.common.CommonProxy;


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


        }
            }












