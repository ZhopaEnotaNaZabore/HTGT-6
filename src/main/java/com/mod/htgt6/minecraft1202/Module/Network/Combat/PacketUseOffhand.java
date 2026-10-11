package com.mod.htgt6.minecraft1202.Module.Network.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** Client intent only; the server validates that a usable shield or crossbow is still equipped. */
public final class PacketUseOffhand implements IMessage {
    private boolean using;

    public PacketUseOffhand() { }
    public PacketUseOffhand(boolean using) { this.using = using; }
    @Override public void fromBytes(ByteBuf buffer) { using = buffer.readBoolean(); }
    @Override public void toBytes(ByteBuf buffer) { buffer.writeBoolean(using); }

    public static final class Handler implements IMessageHandler<PacketUseOffhand, IMessage> {
        @Override public IMessage onMessage(PacketUseOffhand message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            OffhandSystem.requestUse(player, message.using);
            return null;
        }
    }
}
