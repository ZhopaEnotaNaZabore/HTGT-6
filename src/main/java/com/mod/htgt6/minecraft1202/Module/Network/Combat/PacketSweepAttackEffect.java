package com.mod.htgt6.minecraft1202.Module.Network.Combat;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Server-authoritative position of the Minecraft 1.12 sweeping-attack particle. */
public final class PacketSweepAttackEffect implements IMessage {
    private double x;
    private double y;
    private double z;

    public PacketSweepAttackEffect() { }

    public PacketSweepAttackEffect(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override public void fromBytes(ByteBuf buffer) {
        x = buffer.readDouble();
        y = buffer.readDouble();
        z = buffer.readDouble();
    }

    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    public static final class Handler implements IMessageHandler<PacketSweepAttackEffect, IMessage> {
        @Override public IMessage onMessage(final PacketSweepAttackEffect message, MessageContext context) {
            net.minecraft.client.Minecraft.getMinecraft().func_152344_a(new Runnable() {
                @Override public void run() {
                    com.mod.htgt6.minecraft1202.Module.Client.Combat.SweepAttackParticle112.spawn(message.x, message.y, message.z);
                }
            });
            return null;
        }
    }
}
