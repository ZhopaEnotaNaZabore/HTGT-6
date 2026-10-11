package com.mod.htgt6.orehelper;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/** Bundled child mod; no dependency on the standalone GT6OreHelper. */
@Mod(modid = OreHelperMod.ID, name = "HiTech6 Ore Helper", version = "1.0.0",
        dependencies = "required-after:HTGT6;required-after:gregapi_post;after:gregtech;after:NotEnoughItems")
public final class OreHelperMod {
    public static final String ID = "HiTech6_Ore_Helper";
    @SidedProxy(clientSide = "com.mod.htgt6.orehelper.client.OreHelperClient",
            serverSide = "com.mod.htgt6.orehelper.OreHelperProxy")
    public static OreHelperProxy proxy;

    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        OreHelperNetwork.init();
        proxy.init();
    }
}
