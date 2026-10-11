package com.mod.htgt6.client.vanillatweaks.TAB;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class TABServerHandler {
    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        this.tickCounter++;
        if (this.tickCounter >= 20) { // Fresh TPS and ping snapshot once per second
            this.tickCounter = 0;

            MinecraftServer server = MinecraftServer.getServer();
            if (server == null || server.getConfigurationManager() == null) return;

            // 1. Calculate TPS
            long[] tickTimes = server.tickTimeArray;
            double meanTickTime = 0;
            if (tickTimes != null) {
                long total = 0;
                for (long t : tickTimes) total += t;
                meanTickTime = (double) total / tickTimes.length * 1.0E-6D;
            }
            double tps = Math.min(20.0, 1000.0 / (meanTickTime > 0 ? meanTickTime : 1));

            // 2. Gather pings for ALL online players
            Map<String, Integer> pings = new HashMap<>();
            for (Object obj : server.getConfigurationManager().playerEntityList) {
                if (obj instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) obj;
                    pings.put(player.getCommandSenderName(), player.ping);
                }
            }

            // 3. Send global data to all players
            HTGT6.network.sendToAll(new PacketSyncTabStats(tps, pings));
        }
    }
}
