package com.mod.htgt6.common.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

/** Splits NBT fluid-container stacks into single items before mutating their fluid state. */
final class StackedFluidContainerTransfer {
    private StackedFluidContainerTransfer() { }

    static boolean transfer(EntityPlayer player, ItemStack cursorStack, IFluidHandler handler,
                            boolean allowContainerToTank, boolean allowTankToContainer) {
        if (player == null || cursorStack == null || handler == null
                || !(cursorStack.getItem() instanceof IFluidContainerItem)) return false;
        IFluidContainerItem container = (IFluidContainerItem) cursorStack.getItem();
        ItemStack singleTemplate = cursorStack.copy();
        singleTemplate.stackSize = 1;
        boolean changed = false;

        // At most the original stack size is processed. Returned containers are
        // never fed back into this loop, even if they merge elsewhere.
        int originalCount = cursorStack.stackSize;
        for (int index = 0; index < originalCount && cursorStack.stackSize > 0; index++) {
            ItemStack single = singleTemplate.copy();
            FluidStack contained = container.getFluid(single);
            boolean transferred;
            if (contained != null && contained.amount > 0 && allowContainerToTank) {
                transferred = emptyOneIntoTank(container, single, handler, contained);
            } else if (allowTankToContainer) {
                transferred = fillOneFromTank(container, single, handler);
            } else {
                break;
            }
            if (!transferred) break;
            returnProcessedContainer(player, cursorStack, single);
            changed = true;
        }
        return changed;
    }

    private static boolean emptyOneIntoTank(IFluidContainerItem container, ItemStack single,
                                            IFluidHandler handler, FluidStack contained) {
        int accepted = handler.fill(ForgeDirection.UNKNOWN, contained, false);
        if (accepted <= 0) return false;
        FluidStack drained = container.drain(single, accepted, true);
        if (drained == null || drained.amount <= 0) return false;
        int inserted = handler.fill(ForgeDirection.UNKNOWN, drained, true);
        return inserted > 0;
    }

    private static boolean fillOneFromTank(IFluidContainerItem container, ItemStack single,
                                           IFluidHandler handler) {
        FluidStack present = container.getFluid(single);
        int free = Math.max(0, container.getCapacity(single) - (present == null ? 0 : present.amount));
        if (free <= 0) return false;
        FluidStack offered = handler.drain(ForgeDirection.UNKNOWN, free, false);
        if (offered == null || offered.amount <= 0) return false;
        int accepted = container.fill(single, offered, true);
        if (accepted <= 0) return false;
        FluidStack drained = handler.drain(ForgeDirection.UNKNOWN, accepted, true);
        return drained != null && drained.amount > 0;
    }

    private static void returnProcessedContainer(EntityPlayer player, ItemStack original, ItemStack processed) {
        original.stackSize--;
        ItemStack remainder = processed.copy();
        player.inventory.addItemStackToInventory(remainder);
        if (original.stackSize <= 0) {
            player.inventory.setItemStack(remainder.stackSize > 0 ? remainder : null);
        } else if (remainder.stackSize > 0) {
            player.dropPlayerItemWithRandomChoice(remainder, false);
        }
    }
}
