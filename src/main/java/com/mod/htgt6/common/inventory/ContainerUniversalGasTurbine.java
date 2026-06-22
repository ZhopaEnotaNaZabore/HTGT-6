package com.mod.htgt6.common.inventory;

import com.mod.htgt6.common.TE.UniversalGasTurbineTE; //[cite: 14]
import com.mod.htgt6.common.inventory.slot.SlotFluidContainer; //[cite: 14]
import com.mod.htgt6.common.inventory.slot.SlotTierModifier; //[cite: 14]
import com.mod.htgt6.common.inventory.slot.SlotTransformerModule; //[cite: 14]

import net.minecraft.entity.player.EntityPlayer; //[cite: 14]
import net.minecraft.entity.player.InventoryPlayer; //[cite: 14]
import net.minecraft.inventory.Container; //[cite: 14]
import net.minecraft.inventory.ICrafting; //[cite: 14]
import net.minecraft.inventory.Slot; //[cite: 14]
import net.minecraft.item.ItemStack; //[cite: 14]
import net.minecraftforge.fluids.FluidContainerRegistry; //[cite: 14]
import net.minecraftforge.fluids.FluidRegistry; //[cite: 14]
import net.minecraftforge.fluids.FluidStack; //[cite: 14]

public class ContainerUniversalGasTurbine extends Container { //[cite: 14]

    private final UniversalGasTurbineTE te; //[cite: 14]

    private int lastEnergy; //[cite: 14]
    private int lastFluidAmount; //[cite: 14]
    private int lastFluidID; //[cite: 14]
    private int lastMachineTier; //[cite: 14]

    public ContainerUniversalGasTurbine(InventoryPlayer playerInv, UniversalGasTurbineTE te) { //[cite: 14]
        this.te = te; //[cite: 14]

        // Slot 0: Fluid Input Cell (Positioned under Fluid Info Text)
        addSlotToContainer(new SlotFluidContainer(te, 0, 23, 25));

        // Slot 1: Empty Cell Output (Positioned cleanly under Input Cell)
        addSlotToContainer(new Slot(te, 1, 23, 62) {
            @Override //[cite: 14]
            public boolean isItemValid(ItemStack stack) { //[cite: 14]
                return false; //[cite: 14]
            } //[cite: 14]
        }); //[cite: 14]

        // Slot 2: Tier Modifier (Placed under Energy Stats on the right)
        addSlotToContainer(new SlotTierModifier(te, te, 2, 135, 62, 3));

        // Slot 3: Transformer Module (Placed cleanly next to Tier Modifier)
        addSlotToContainer(new SlotTransformerModule(te, te, 3, 151, 62));

        // Player Inventory Slots (Perfectly centered on the 200px width grid)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                // Standard offset for 166 height: 8 + j * 18, 84 + i * 18
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            // Standard hotbar offset: 8 + i * 18, 142
            addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    } //[cite: 14]

    @Override //[cite: 14]
    public boolean canInteractWith(EntityPlayer player) { //[cite: 14]
        return te.isUseableByPlayer(player); //[cite: 14]
    } //[cite: 14]

    @Override //[cite: 14]
    public void detectAndSendChanges() { //[cite: 14]
        super.detectAndSendChanges(); //[cite: 14]

        int currentEnergy = (int) te.energy; //[cite: 14]
        int currentFluidAmount = te.fluidTank.getFluidAmount(); //[cite: 14]
        int currentFluidID = te.fluidTank.getFluid() != null ? te.fluidTank.getFluid().getFluidID() : -1; //[cite: 14]
        int currentMachineTier = te.machineTier; //[cite: 14]

        for (Object crafter : crafters) { //[cite: 14]
            ICrafting ic = (ICrafting) crafter; //[cite: 14]

            if (lastEnergy != currentEnergy) { //[cite: 14]
                ic.sendProgressBarUpdate(this, 0, currentEnergy & 0xFFFF); //[cite: 14]
                ic.sendProgressBarUpdate(this, 1, (currentEnergy >>> 16) & 0xFFFF); //[cite: 14]
            } //[cite: 14]

            if (lastFluidAmount != currentFluidAmount) { //[cite: 14]
                ic.sendProgressBarUpdate(this, 2, currentFluidAmount & 0xFFFF); //[cite: 14]
                ic.sendProgressBarUpdate(this, 3, (currentFluidAmount >>> 16) & 0xFFFF); //[cite: 14]
            } //[cite: 14]

            if (lastFluidID != currentFluidID) { //[cite: 14]
                ic.sendProgressBarUpdate(this, 4, currentFluidID); //[cite: 14]
            } //[cite: 14]

            if (lastMachineTier != currentMachineTier) { //[cite: 14]
                ic.sendProgressBarUpdate(this, 5, currentMachineTier); //[cite: 14]
            } //[cite: 14]
        } //[cite: 14]

        lastEnergy = currentEnergy; //[cite: 14]
        lastFluidAmount = currentFluidAmount; //[cite: 14]
        lastFluidID = currentFluidID; //[cite: 14]
        lastMachineTier = currentMachineTier; //[cite: 14]
    } //[cite: 14]

    @Override //[cite: 14]
    public void updateProgressBar(int id, int value) { //[cite: 14]
        if (id == 0) { //[cite: 14]
            int energyInt = (int) te.energy; //[cite: 14]
            energyInt = (energyInt & 0xFFFF0000) | (value & 0xFFFF); //[cite: 14]
            te.energy = energyInt; //[cite: 14]
        } else if (id == 1) { //[cite: 14]
            int energyInt = (int) te.energy; //[cite: 14]
            energyInt = (energyInt & 0x0000FFFF) | ((value & 0xFFFF) << 16); //[cite: 14]
            te.energy = energyInt; //[cite: 14]
        } else if (id == 2) { //[cite: 14]
            updateClientFluidAmount(value, true); //[cite: 14]
        } else if (id == 3) { //[cite: 14]
            updateClientFluidAmount(value, false); //[cite: 14]
        } else if (id == 4) { //[cite: 14]
            if (value == -1) { //[cite: 14]
                te.fluidTank.setFluid(null); //[cite: 14]
            } else { //[cite: 14]
                FluidStack current = te.fluidTank.getFluid(); //[cite: 14]
                int amount = current != null ? current.amount : 0; //[cite: 14]
                te.fluidTank.setFluid(new FluidStack(FluidRegistry.getFluid(value), amount)); //[cite: 14]
            } //[cite: 14]
        } else if (id == 5) { //[cite: 14]
            te.machineTier = value; //[cite: 14]
        } //[cite: 14]
    } //[cite: 14]

    private void updateClientFluidAmount(int value, boolean lowerBits) { //[cite: 14]
        FluidStack current = te.fluidTank.getFluid(); //[cite: 14]
        if (current == null) { //[cite: 14]
            current = new FluidStack(FluidRegistry.WATER, 0); //[cite: 14]
            te.fluidTank.setFluid(current); //[cite: 14]
        } //[cite: 14]

        if (lowerBits) { //[cite: 14]
            current.amount = (current.amount & 0xFFFF0000) | (value & 0xFFFF); //[cite: 14]
        } else { //[cite: 14]
            current.amount = (current.amount & 0xFFFF) | ((value & 0xFFFF) << 16); //[cite: 14]
        } //[cite: 14]
    } //[cite: 14]

    @Override //[cite: 14]
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) { //[cite: 14]
        ItemStack itemstack = null; //[cite: 14]
        Slot slot = (Slot) this.inventorySlots.get(slotIndex); //[cite: 14]

        if (slot != null && slot.getHasStack()) { //[cite: 14]
            ItemStack stack = slot.getStack(); //[cite: 14]
            itemstack = stack.copy(); //[cite: 14]

            if (slotIndex < 4) { //[cite: 14]
                if (!mergeItemStack(stack, 4, 40, true)) { //[cite: 14]
                    return null; //[cite: 14]
                } //[cite: 14]
                slot.onSlotChange(stack, itemstack); //[cite: 14]
            } else { //[cite: 14]
                if (FluidContainerRegistry.isFilledContainer(stack)) { //[cite: 14]
                    if (!mergeItemStack(stack, 0, 1, false)) { //[cite: 14]
                        return null; //[cite: 14]
                    } //[cite: 14]
                } else if (this.getSlot(2).isItemValid(stack) && !this.getSlot(2).getHasStack()) { //[cite: 14]
                    ItemStack singleItem = stack.copy(); //[cite: 14]
                    singleItem.stackSize = 1; //[cite: 14]
                    this.getSlot(2).putStack(singleItem); //[cite: 14]
                    stack.stackSize--; //[cite: 14]
                    this.getSlot(2).onSlotChanged(); //[cite: 14]
                } else if (this.getSlot(3).isItemValid(stack)) { //[cite: 14]
                    if (!mergeItemStack(stack, 3, 4, false)) { //[cite: 14]
                        return null; //[cite: 14]
                    } //[cite: 14]
                } else if (slotIndex >= 4 && slotIndex < 31) { //[cite: 14]
                    if (!mergeItemStack(stack, 31, 40, false)) { //[cite: 14]
                        return null; //[cite: 14]
                    } //[cite: 14]
                } else if (slotIndex >= 31 && slotIndex < 40 && !mergeItemStack(stack, 4, 31, false)) { //[cite: 14]
                    return null; //[cite: 14]
                } //[cite: 14]
            } //[cite: 14]

            if (stack.stackSize == 0) { //[cite: 14]
                slot.putStack(null); //[cite: 14]
            } else { //[cite: 14]
                slot.onSlotChanged(); //[cite: 14]
            } //[cite: 14]

            if (stack.stackSize == itemstack.stackSize) { //[cite: 14]
                return null; //[cite: 14]
            } //[cite: 14]

            slot.onPickupFromSlot(player, stack); //[cite: 14]
        } //[cite: 14]

        return itemstack; //[cite: 14]
    }
}
