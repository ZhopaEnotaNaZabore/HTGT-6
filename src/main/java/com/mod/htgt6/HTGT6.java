package com.mod.htgt6;

import com.mod.htgt6.common.CommonProxy;
import com.mod.htgt6.common.GalaxyServer.Glise792.sysGliese792;
import com.mod.htgt6.common.GalaxyServer.Sirius.GC;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.handler.GuiHandler;


import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import minetweaker.MineTweakerAPI;


@Mod(modid = HTGT6.MOD_ID, version = "1.0.4",
dependencies = "after:gregtech")
public class HTGT6 {
    public static final String MOD_ID = "HTGT6";
    @Mod.Instance(MOD_ID) //
    public static HTGT6 instance;
  //  public static final String ASSET_PREFIX = MOD_ID.toLowerCase();
  public static SimpleNetworkWrapper network;





     @SidedProxy(
            clientSide = "com.mod.htgt6.client.ClientProxy",
            serverSide = "com.mod.htgt6.common.CommonProxy"
    )
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);
    }



    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        GameRegistry.registerTileEntity(TileEntityAssembler.class, "TileEntityAssembler");
        GC.init(event);


      //  DimensionManager.registerProviderType(-80, WorldProviderGliese.class, false);
      //  DimensionManager.registerDimension(-80, -80);
     //   DimensionManager.registerProviderType(-888, WorldProviderGlieseC.class, false);
     //   DimensionManager.registerDimension(-888, -888
     //   );
        sysGliese792.init(event);
        proxy.init(event);

    }
   @Mod.EventHandler
           public void postInit(FMLPostInitializationEvent event) {
       MineTweakerAPI.registerClass( com.mod.htgt6.common.MT.CTAssembler.class);

        proxy.postInit(event);
   }
}
