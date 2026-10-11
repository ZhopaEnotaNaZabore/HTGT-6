package com.mod.htgt6.minecraft1202.Module.Network.Modern;

import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120RecoveryCompass;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;

/** Server-to-client copy of the persistent last-death GlobalPos used by the recovery compass. */
public final class PacketBackport120DeathPosition implements IMessage {
    private boolean valid;
    private int x;
    private int z;
    private int dimension;

    public PacketBackport120DeathPosition() { }

    public PacketBackport120DeathPosition(boolean valid, int x, int z, int dimension) {
        this.valid = valid;
        this.x = x;
        this.z = z;
        this.dimension = dimension;
    }

    @Override public void fromBytes(ByteBuf buffer) {
        valid = buffer.readBoolean();
        x = buffer.readInt();
        z = buffer.readInt();
        dimension = buffer.readInt();
    }

    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeBoolean(valid);
        buffer.writeInt(x);
        buffer.writeInt(z);
        buffer.writeInt(dimension);
    }

    public static final class Handler implements IMessageHandler<PacketBackport120DeathPosition, IMessage> {
        @Override public IMessage onMessage(final PacketBackport120DeathPosition message, MessageContext context) {
            Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override public void run() {
                    ItemBackport120RecoveryCompass.setClientTarget(message.valid, message.x,
                            message.z, message.dimension);
                }
            });
            return null;
        }
    }
}
