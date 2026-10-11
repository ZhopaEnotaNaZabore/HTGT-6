package com.mod.htgt6.client.vanillatweaks.TAB;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public class PacketSyncTabStats implements IMessage {
    public double tps;
    public Map<String, Integer> playerPings;

    public PacketSyncTabStats() {
        this.playerPings = new HashMap<>();
    }

    public PacketSyncTabStats(double tps, Map<String, Integer> playerPings) {
        this.tps = tps;
        this.playerPings = playerPings;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.tps = buf.readDouble();
        int size = buf.readInt();
        this.playerPings = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String name = ByteBufUtils.readUTF8String(buf);
            int ping = buf.readInt();
            this.playerPings.put(name, ping);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(this.tps);
        buf.writeInt(this.playerPings.size());
        for (Map.Entry<String, Integer> entry : this.playerPings.entrySet()) {
            ByteBufUtils.writeUTF8String(buf, entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public static class Handler implements IMessageHandler<PacketSyncTabStats, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncTabStats message, MessageContext ctx) {
            final double tps = message.tps;
            final Map<String, Integer> pings = new HashMap<String, Integer>(message.playerPings);
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override
                public void run() {
                    TABDataHolder.update(tps, pings);
                }
            });
            return null;
        }
    }
}
