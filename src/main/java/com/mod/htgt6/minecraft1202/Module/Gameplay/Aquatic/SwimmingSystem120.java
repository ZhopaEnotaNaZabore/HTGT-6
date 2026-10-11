package com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic;

import java.util.Map;
import java.util.WeakHashMap;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import com.mod.htgt6.minecraft1202.Module.Network.Aquatic.PacketSwimming120;

public final class SwimmingSystem120 {
    private static SimpleNetworkWrapper network;
    private final Map<EntityPlayer,Integer> sent=new WeakHashMap<EntityPlayer,Integer>();
    public static void init() {
        if(network!=null)return;
        network=NetworkRegistry.INSTANCE.newSimpleChannel("HTGT6_Swimming");
        network.registerMessage(PacketSwimming120.Handler.class,PacketSwimming120.class,0,Side.CLIENT);
        SwimmingSystem120 system=new SwimmingSystem120();
        FMLCommonHandler.instance().bus().register(system);MinecraftForge.EVENT_BUS.register(system);
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        SwimmingPose120.animate(event.player);
        if(event.player.worldObj.isRemote||!(event.player instanceof EntityPlayerMP))return;
        EntityPlayerMP player=(EntityPlayerMP)event.player;
        PlayerSwimming120.updateState(player);
        boolean swimming=PlayerSwimming120.isSwimming(player);int state=(swimming?1:0)|(SwimmingPose120.kind(player)<<1);Integer previous=sent.put(player,state);
        // A periodic snapshot also repairs a state packet arriving before the spawn packet.
        if(previous==null||previous.intValue()!=state||player.ticksExisted%20==0) {
            PacketSwimming120 packet=new PacketSwimming120(player,swimming);
            network.sendTo(packet,player);
            for(EntityPlayer watcher:((WorldServer)player.worldObj).getEntityTracker().getTrackingPlayers(player))
                if(watcher instanceof EntityPlayerMP&&watcher!=player)network.sendTo(packet,(EntityPlayerMP)watcher);
        }
    }
    @SubscribeEvent public void tracking(PlayerEvent.StartTracking event) {
        if(event.entityPlayer instanceof EntityPlayerMP&&event.target instanceof EntityPlayer)
            network.sendTo(new PacketSwimming120((EntityPlayer)event.target,PlayerSwimming120.isSwimming((EntityPlayer)event.target)),(EntityPlayerMP)event.entityPlayer);
    }
    @SubscribeEvent public void logout(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
        sent.remove(event.player);PlayerSwimming120.clear(event.player);
    }
}
