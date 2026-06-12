package com.mod.htgt6.common.inventory;


import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import com.mod.htgt6.common.inventory.slot.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;


public class ContainerSuperMasicCompressor extends Container {

    private TESuperMasicCompressor te;
    private int lastProgress;
    private int lastMaxProgress;
    private int lastEnergy;

    public ContainerSuperMasicCompressor(InventoryPlayer playerInv, TESuperMasicCompressor teSMC) {
        this.te = teSMC;

        // 3 Input Slots in a Single Horizontal Row (Slots 0, 1, 2)
        for (int j = 0; j < 3; ++j) {
            addSlotToContainer(new Slot(teSMC, j, 16 + j * 18, 25));
        }

        // 1 Output Slot (Slot 3)
        addSlotToContainer(new SlotFurnace(playerInv.player, teSMC, 3, 110, 24));

        // Specialized Upgrade & Power Utility Slots
        addSlotToContainer(new SlotFluidContainer(teSMC, 4, 54, 63));
        addSlotToContainer(new SlotTierModifier(teSMC, teSMC, 5, 84, 63, 6));
        addSlotToContainer(new SlotTransformerModule(teSMC, teSMC, 6, 102, 63));
        addSlotToContainer(new SlotBattery(teSMC, 7, 138, 63));

        // Player Inventory standard matrix loop
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return te.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafter : crafters) {
            ICrafting ic = (ICrafting) crafter;
            if (lastProgress != te.progress) ic.sendProgressBarUpdate(this, 0, te.progress);
            if (lastMaxProgress != te.maxProgress) ic.sendProgressBarUpdate(this, 1, te.maxProgress);
            if (lastEnergy != te.energy) ic.sendProgressBarUpdate(this, 2, te.energy);
        }
        lastProgress = te.progress;
        lastMaxProgress = te.maxProgress;
        lastEnergy = te.energy;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) te.progress = value;
        if (id == 1) te.maxProgress = value;
        if (id == 2) te.energy = value;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            // Machine Slots -> Player Inventory
            if (slotIndex < 8) {
                if (!mergeItemStack(stack, 8, 44, true)) {
                    return null;
                }
                slot.onSlotChange(stack, itemstack);
            } else {
                // Player Inventory -> Machine Slots
                if (stack.getItem() instanceof ic2.api.item.IElectricItem) {
                    if (!mergeItemStack(stack, 7, 8, false)) return null;
                } else {
                    // Quick-stack into the 3 horizontal processing input channels
                    if (!mergeItemStack(stack, 0, 3, false)) return null;
                }
            }

            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (stack.stackSize == itemstack.stackSize) return null;
            slot.onPickupFromSlot(player, stack);
        }
        return itemstack;
    }
}