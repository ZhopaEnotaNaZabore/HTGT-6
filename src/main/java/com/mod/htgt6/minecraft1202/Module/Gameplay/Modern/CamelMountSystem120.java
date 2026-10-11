package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;

import com.mod.htgt6.minecraft1202.Module.Network.Modern.PacketCamelDash120;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/** Dedicated 1.7 transport contract for Camel controls, independent of EntityHorse packets. */
public final class CamelMountSystem120 {
    public static final String CHANNEL="MC1202_Camel";
    public static final SimpleNetworkWrapper NETWORK=NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
    private static boolean initialized;
    public static synchronized void init(){
        if(initialized)return;
        NETWORK.registerMessage(PacketCamelDash120.Handler.class,PacketCamelDash120.class,0,Side.SERVER);
        initialized=true;
    }
    private CamelMountSystem120(){}
}
