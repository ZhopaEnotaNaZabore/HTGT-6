package com.mod.htgt6.minecraft1202.Module.Client.Inventory;

import com.mod.htgt6.client.inventory112.OffhandRenderer112;
import com.mod.htgt6.client.inventory112.Inventory112ClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;

/** Client input and rendering owned by the native off-hand module. */
@SideOnly(Side.CLIENT)
public final class OffhandModule112Client {
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;
        Inventory112ClientHandler input = new Inventory112ClientHandler();
        MinecraftForge.EVENT_BUS.register(input);
        FMLCommonHandler.instance().bus().register(input);
        MinecraftForge.EVENT_BUS.register(new OffhandRenderer112());
    }

    private OffhandModule112Client() { }
}
