package com.mod.htgt6.common.network;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.multiblock.gateway.TileEntityMultiblockGateway;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraft.network.play.server.S2FPacketSetSlot;

/** Server-authoritative container transfer for reusable multiblock fluid gateways. */
public final class PacketGatewayFluidTransfer implements IMessage {
    private int x, y, z, tank;

    public PacketGatewayFluidTransfer() { }
    public PacketGatewayFluidTransfer(TileEntityMultiblockGateway gateway, int tankIndex) {
        x = gateway.xCoord; y = gateway.yCoord; z = gateway.zCoord;
        tank = tankIndex;
    }

    @Override public void fromBytes(ByteBuf buffer) { x = buffer.readInt(); y = buffer.readInt(); z = buffer.readInt(); tank = buffer.readInt(); }
    @Override public void toBytes(ByteBuf buffer) { buffer.writeInt(x); buffer.writeInt(y); buffer.writeInt(z); buffer.writeInt(tank); }

    public static final class Handler implements IMessageHandler<PacketGatewayFluidTransfer, IMessage> {
        @Override public IMessage onMessage(PacketGatewayFluidTransfer message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().playerEntity;
            if (player == null || player.worldObj == null
                    || player.getDistanceSq(message.x + 0.5D, message.y + 0.5D, message.z + 0.5D) > 64D) return null;
            TileEntity tile = player.worldObj.getTileEntity(message.x, message.y, message.z);
            if (!(tile instanceof TileEntityMultiblockGateway)) return null;
            TileEntityMultiblockGateway gateway = (TileEntityMultiblockGateway) tile;
            GatewayType type = gateway.getGatewayType();
            if (type != GatewayType.FLUID_INPUT && type != GatewayType.FLUID_OUTPUT) return null;
            IFluidHandler tankHandler = gateway.getFluidTankHandler(message.tank);
            if (tankHandler == null) return null;

            ItemStack held = player.inventory.getItemStack();
            if (held == null) return null;
            if (held.getItem() instanceof IFluidContainerItem) {
                StackedFluidContainerTransfer.transfer(player, held, tankHandler,
                        type == GatewayType.FLUID_INPUT, type == GatewayType.FLUID_OUTPUT);
            } else {
                transferRegistryContainer(player, tankHandler, type, held);
            }
            gateway.markDirty();
            player.inventory.markDirty();
            // This custom packet bypasses Container.slotClick, including its
            // cursor acknowledgement. Explicitly reconcile the cursor and slots.
            player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, player.inventory.getItemStack()));
            player.openContainer.detectAndSendChanges();
            return null;
        }

        private static void transferRegistryContainer(EntityPlayerMP player,
                                                       IFluidHandler gateway,
                                                       GatewayType type, ItemStack held) {
            if (type == GatewayType.FLUID_INPUT) {
                FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(held);
                ItemStack emptied = FluidContainerRegistry.drainFluidContainer(held);
                if (contained == null || emptied == null
                        || gateway.fill(ForgeDirection.UNKNOWN, contained, false) != contained.amount) return;
                gateway.fill(ForgeDirection.UNKNOWN, contained, true);
                replaceOneHeldContainer(player, held, emptied);
                return;
            }
            FluidStack offered = gateway.drain(ForgeDirection.UNKNOWN, Integer.MAX_VALUE, false);
            if (offered == null || offered.amount <= 0) return;
            ItemStack filled = FluidContainerRegistry.fillFluidContainer(offered, held);
            if (filled == null) return;
            FluidStack transferred = FluidContainerRegistry.getFluidForFilledItem(filled);
            if (transferred == null || transferred.amount <= 0) return;
            gateway.drain(ForgeDirection.UNKNOWN, transferred.amount, true);
            replaceOneHeldContainer(player, held, filled);
        }

        private static void replaceOneHeldContainer(EntityPlayerMP player, ItemStack held, ItemStack result) {
            if (held.stackSize <= 1) {
                player.inventory.setItemStack(result);
                return;
            }
            held.stackSize--;
            if (!player.inventory.addItemStackToInventory(result.copy())) {
                player.dropPlayerItemWithRandomChoice(result.copy(), false);
            }
        }
    }
}
