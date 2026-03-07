package com.mod.htgt6;

import com.mod.htgt6.common.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;



@Mod(modid = HTGT6.MOD_ID, version = "1.0.4")
public class HTGT6 {
    public static final String MOD_ID = "HTGT6";
    public static final String ASSET_PREFIX = MOD_ID.toLowerCase();




     @SidedProxy(
            clientSide = "com.mod.htgt6.client.ClientProxy",
            serverSide = "com.mod.htgt6.common.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);

    }
   @Mod.EventHandler
           public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
   }
}
