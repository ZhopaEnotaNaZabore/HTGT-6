package com.mod.htgt6.minecraft1202.Module.Network.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ElytraFlight;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public final class PacketElytraStart implements IMessage {
    @Override public void fromBytes(ByteBuf buffer) { }
    @Override public void toBytes(ByteBuf buffer) { }
    public static final class Handler implements IMessageHandler<PacketElytraStart, IMessage> {
        @Override public IMessage onMessage(PacketElytraStart message, MessageContext context) {
            ElytraFlight.requestStart(context.getServerHandler().playerEntity);
            return null;
        }
    }
}
