package com.mod.htgt6.common.network;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityFluidGenerator;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

/** Server-authoritative direct canister transfer for ModularUI fluid generators. */
public class PacketEngineFluidTransfer implements IMessage {
    private int x, y, z;
    public PacketEngineFluidTransfer() {}
    public PacketEngineFluidTransfer(TileEntityFluidGenerator generator) {
        x = generator.xCoord; y = generator.yCoord; z = generator.zCoord;
    }
    @Override public void fromBytes(ByteBuf buffer) { x = buffer.readInt(); y = buffer.readInt(); z = buffer.readInt(); }
    @Override public void toBytes(ByteBuf buffer) { buffer.writeInt(x); buffer.writeInt(y); buffer.writeInt(z); }

    public static class Handler implements IMessageHandler<PacketEngineFluidTransfer, IMessage> {
        @Override public IMessage onMessage(PacketEngineFluidTransfer message, MessageContext context) {
            MachineUiTransfers.enqueue(context.getServerHandler().playerEntity, message.x, message.y, message.z);
            return null;
        }
    }
}
