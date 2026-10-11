package com.mod.htgt6.common.network;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityFluidMachine;
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

/** Server-authoritative canister transfer for FluidMachine ModularUI tanks. */
public class PacketFluidMachineTransfer implements IMessage {
    private int x;
    private int y;
    private int z;

    public PacketFluidMachineTransfer() {}

    public PacketFluidMachineTransfer(TileEntityFluidMachine machine) {
        x = machine.xCoord;
        y = machine.yCoord;
        z = machine.zCoord;
    }

    @Override public void fromBytes(ByteBuf buffer) {
        x = buffer.readInt(); y = buffer.readInt(); z = buffer.readInt();
    }
    @Override public void toBytes(ByteBuf buffer) {
        buffer.writeInt(x); buffer.writeInt(y); buffer.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketFluidMachineTransfer, IMessage> {
        @Override public IMessage onMessage(PacketFluidMachineTransfer message, MessageContext context) {
            MachineUiTransfers.enqueue(context.getServerHandler().playerEntity, message.x, message.y, message.z);
            return null;
        }
    }
}
