package com.mod.htgt6.minecraft1202.Module.Network.Inventory;

import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.RecipeBookSystem112;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Persists a completed vanilla 1.12 tutorial step on the authoritative server. */
public final class PacketTutorialStep112 implements IMessage {
    private int step;

    public PacketTutorialStep112() { }
    public PacketTutorialStep112(int step) { this.step = step; }

    @Override public void fromBytes(ByteBuf buffer) { step = buffer.readUnsignedByte(); }
    @Override public void toBytes(ByteBuf buffer) { buffer.writeByte(step); }

    public static final class Handler implements IMessageHandler<PacketTutorialStep112, IMessage> {
        @Override public IMessage onMessage(PacketTutorialStep112 message, MessageContext context) {
            RecipeBookSystem112.requestTutorialStep(context.getServerHandler().playerEntity, message.step);
            return null;
        }
    }
}
