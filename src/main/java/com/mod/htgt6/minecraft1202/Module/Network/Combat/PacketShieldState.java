package com.mod.htgt6.minecraft1202.Module.Network.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Server-authoritative offhand shield pose for third-person rendering. */
public final class PacketShieldState implements IMessage {
    private int entityId;
    private boolean blocking;

    public PacketShieldState() { }
    public PacketShieldState(int entityId, boolean blocking) {
        this.entityId = entityId;
        this.blocking = blocking;
    }
    @Override public void fromBytes(ByteBuf buffer) {
        entityId = buffer.readInt();
        blocking = buffer.readBoolean();
    }
    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeInt(entityId);
        buffer.writeBoolean(blocking);
    }

    public static final class Handler implements IMessageHandler<PacketShieldState, IMessage> {
        @Override public IMessage onMessage(final PacketShieldState message, MessageContext context) {
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override public void run() {
                    OffhandSystem.setClientShieldBlocking(message.entityId, message.blocking);
                }
            });
            return null;
        }
    }
}
