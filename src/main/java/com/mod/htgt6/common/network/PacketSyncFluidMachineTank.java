package com.mod.htgt6.common.network;

import com.mod.htgt6.common.TE.TileEntityAssembler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;

/** Pushes assembler tank display changes while its ModularUI remains open. */
public class PacketSyncFluidMachineTank implements IMessage {
    private int x;
    private int y;
    private int z;
    private String fluidName = "";
    private int amount;

    public PacketSyncFluidMachineTank() {}

    public PacketSyncFluidMachineTank(TileEntityAssembler assembler, String fluidName, int amount) {
        x = assembler.xCoord;
        y = assembler.yCoord;
        z = assembler.zCoord;
        this.fluidName = fluidName == null ? "" : fluidName;
        this.amount = amount;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        x = buffer.readInt();
        y = buffer.readInt();
        z = buffer.readInt();
        amount = buffer.readInt();
        try {
            fluidName = new PacketBuffer(buffer).readStringFromBuffer(128);
        } catch (IOException ignored) {
            fluidName = "";
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        buffer.writeInt(amount);
        try {
            new PacketBuffer(buffer).writeStringToBuffer(fluidName);
        } catch (IOException ignored) { }
    }

    public static class Handler implements IMessageHandler<PacketSyncFluidMachineTank, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncFluidMachineTank message, MessageContext context) {
            // Reserved legacy packet ID. Tank display now belongs solely to ModularUI.
            return null;
        }
    }
}
