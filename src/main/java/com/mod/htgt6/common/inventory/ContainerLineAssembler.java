package com.mod.htgt6.common.inventory;

import com.mod.htgt6.api.hitech6.BaseMachines.ContainerBaseMachine;
import com.mod.htgt6.common.TE.LineAssemblerTE;
import com.mod.htgt6.api.hitech6.Slots.SlotFluidDisplay;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ContainerLineAssembler extends ContainerBaseMachine {

    private final LineAssemblerTE te;

    // Track 4 tanks independently using arrays
    private int[] lastFluidIDs = new int[]{-1, -1, -1, -1};
    private int[] lastFluidAmounts = new int[]{0, 0, 0, 0};
    private final int[] syncedFluidIDs = new int[]{-1, -1, -1, -1};
    private final int[] syncedFluidAmounts = new int[]{0, 0, 0, 0};

    public ContainerLineAssembler(InventoryPlayer playerInv, LineAssemblerTE te) {
        super(playerInv, te); // Handles base machine inventory & power slot registrations
        this.te = te;

        // 1. 16 Input Recipe Slots (4x4 Grid, Slots 0-15)
        for (int row = 0; row < 4; ++row) {
            for (int col = 0; col < 4; ++col) {
                addSlotToContainer(new Slot(te, col + row * 4, 8 + col * 18, 18 + row * 18));
            }
        }

        // 2. Output Slot (Slot 16)
        addSlotToContainer(new SlotFurnace(playerInv.player, te, 16, 142, 70));

        // 3. 4 Fluid Input Slots (Slots 17-20)
        for (int i = 0; i < 4; i++) {
            addSlotToContainer(new SlotFluidDisplay(te, 17 + i, 88, 18 + i * 18));
        }

        // 4. Upgrade & Power Slots (21: Tier Module, 22: Transformer, 23: Battery)
        addUpgradeSlots(21, 142, 18, 22, 142, 36, 23, 142, 54);

        // 5. Player Inventory (shifted down to fit 4x4 grid)
        addPlayerInventory(playerInv, 8, 104);
    }

    @Override
    public ItemStack slotClick(int slotId, int mouseButton, int modifier, EntityPlayer player) {
        if (slotId >= LineAssemblerTE.FLUID_SLOT_START
                && slotId < LineAssemblerTE.FLUID_SLOT_START + LineAssemblerTE.FLUID_SLOT_COUNT) {
            if (!player.worldObj.isRemote && mouseButton == 0) {
                transferCursorCanister(slotId - LineAssemblerTE.FLUID_SLOT_START, player.inventory.getItemStack());
            }
            return player.inventory.getItemStack();
        }
        return super.slotClick(slotId, mouseButton, modifier, player);
    }

    /** Transfers fluid without ever placing the canister into the machine inventory. */
    private void transferCursorCanister(int tankIndex, ItemStack cursorStack) {
        if (cursorStack == null || !(cursorStack.getItem() instanceof IFluidContainerItem)) return;

        IFluidContainerItem canister = (IFluidContainerItem) cursorStack.getItem();
        FluidTank tank = te.tanks[tankIndex];
        FluidStack tankFluid = tank.getFluid();

        // An empty canister takes fluid back out; a filled canister inserts fluid.
        if ((canister.getFluid(cursorStack) == null || canister.getFluid(cursorStack).amount <= 0)
                && tankFluid != null && tank.getFluidAmount() > 0) {
            FluidStack offered = tank.drain(Math.min(canister.getCapacity(cursorStack), tank.getFluidAmount()), false);
            if (offered != null) {
                int accepted = canister.fill(cursorStack, offered, true);
                if (accepted > 0) tank.drain(accepted, true);
            }
        } else {
            FluidStack offered = canister.getFluid(cursorStack);
            if (offered != null && offered.amount > 0) {
                int accepted = tank.fill(offered, false);
                if (accepted > 0) {
                    FluidStack drained = canister.drain(cursorStack, accepted, true);
                    if (drained != null) tank.fill(drained, true);
                }
            }
        }
        te.markDirty();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object crafter : crafters) {
            ICrafting ic = (ICrafting) crafter;

            // Loop through all 4 tanks and send updates when fluid ID or amount changes
            for (int i = 0; i < te.tanks.length; i++) {
                int fluidID = te.tanks[i].getFluid() != null ? te.tanks[i].getFluid().getFluidID() : -1;
                int fluidAmount = te.tanks[i].getFluid() != null ? te.tanks[i].getFluid().amount : 0;

                int idPacketKey = 4 + (i * 3);      // 0-3 are reserved by ContainerBaseMachine
                int amountLowPacketKey = idPacketKey + 1;
                int amountHighPacketKey = idPacketKey + 2;

                if (lastFluidIDs[i] != fluidID) {
                    ic.sendProgressBarUpdate(this, idPacketKey, fluidID);
                }
                if (lastFluidAmounts[i] != fluidAmount) {
                    ic.sendProgressBarUpdate(this, amountLowPacketKey, fluidAmount & 0xFFFF);
                    ic.sendProgressBarUpdate(this, amountHighPacketKey, fluidAmount >>> 16);
                }
            }
        }

        // Update cached values
        for (int i = 0; i < te.tanks.length; i++) {
            lastFluidIDs[i] = te.tanks[i].getFluid() != null ? te.tanks[i].getFluid().getFluidID() : -1;
            lastFluidAmounts[i] = te.tanks[i].getFluid() != null ? te.tanks[i].getFluid().amount : 0;
        }
    }

    @Override
    public void updateProgressBar(int id, int value) {
        super.updateProgressBar(id, value); // Standard base checks for progress and energy

        if (id >= 4 && id < 4 + te.tanks.length * 3) {
            int tankIndex = (id - 4) / 3;
            int part = (id - 4) % 3;
            if (part == 0) syncedFluidIDs[tankIndex] = value;
            else if (part == 1) syncedFluidAmounts[tankIndex] = (syncedFluidAmounts[tankIndex] & 0xFFFF0000) | (value & 0xFFFF);
            else syncedFluidAmounts[tankIndex] = (syncedFluidAmounts[tankIndex] & 0xFFFF) | ((value & 0xFFFF) << 16);

            if (syncedFluidIDs[tankIndex] == -1) {
                te.tanks[tankIndex].setFluid(null);
            } else if (FluidRegistry.getFluid(syncedFluidIDs[tankIndex]) != null) {
                te.tanks[tankIndex].setFluid(new FluidStack(FluidRegistry.getFluid(syncedFluidIDs[tankIndex]), syncedFluidAmounts[tankIndex]));
            }
        }
    }
}
