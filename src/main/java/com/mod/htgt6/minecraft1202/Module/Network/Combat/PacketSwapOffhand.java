package com.mod.htgt6.minecraft1202.Module.Network.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

public final class PacketSwapOffhand implements IMessage {
    private int sequence;
    private int selectedSlot;

    public PacketSwapOffhand() { }
    public PacketSwapOffhand(int sequence, int selectedSlot) {
        this.sequence = sequence;
        this.selectedSlot = selectedSlot;
    }
    @Override public void fromBytes(ByteBuf buffer) {
        sequence = buffer.readInt();
        selectedSlot = buffer.readInt();
    }
    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeInt(sequence);
        buffer.writeInt(selectedSlot);
    }
    public static final class Handler implements IMessageHandler<PacketSwapOffhand, IMessage> {
        @Override public IMessage onMessage(PacketSwapOffhand message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            OffhandSystem.requestSwap(player, message.sequence, message.selectedSlot);
            return null;
        }
    }
}
