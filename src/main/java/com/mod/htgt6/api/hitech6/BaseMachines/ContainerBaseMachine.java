package com.mod.htgt6.api.hitech6.BaseMachines;

import com.mod.htgt6.api.hitech6.Slots.SlotBattery;
import com.mod.htgt6.api.hitech6.Slots.SlotTierModifier;
import com.mod.htgt6.api.hitech6.Slots.SlotTransformerModule;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerBaseMachine extends Container {

    protected TileEntityBaseMachine tile;
    private int lastProgress;
    private int lastMaxProgress;

    // FIX 1: Change lastEnergy to long to match TileEntityBaseMachine
    private long lastEnergy;

    public ContainerBaseMachine(InventoryPlayer playerInv, TileEntityBaseMachine tile) {
        this.tile = tile;
    }

    /** Helper to quickly add base upgrade & power slots */
    protected void addUpgradeSlots(int moduleSlot, int moduleX, int moduleY,
                                   int transformerSlot, int transX, int transY,
                                   int batterySlot, int batX, int batY) {
        addSlotToContainer(new SlotTierModifier(tile, tile, moduleSlot, moduleX, moduleY, transformerSlot));
        addSlotToContainer(new SlotTransformerModule(tile, tile, transformerSlot, transX, transY));
        addSlotToContainer(new SlotBattery(tile, batterySlot, batX, batY));
    }

    /** Standard Player Inventory (27 slots) & Hotbar (9 slots) */
    protected void addPlayerInventory(InventoryPlayer playerInv, int startX, int startY) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, startX + j * 18, startY + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(playerInv, i, startX + i * 18, startY + 58));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafter : crafters) {
            ICrafting ic = (ICrafting) crafter;
            if (lastProgress != tile.progress) ic.sendProgressBarUpdate(this, 0, tile.progress);
            if (lastMaxProgress != tile.maxProgress) ic.sendProgressBarUpdate(this, 1, tile.maxProgress);

            // FIX 2: Split 64-bit long energy into two 32-bit ints to send over network (IDs 2 and 3)
            if (lastEnergy != tile.energy) {
                int lowerBits = (int) (tile.energy & 0xFFFFFFFFL);
                int upperBits = (int) (tile.energy >> 32);
                ic.sendProgressBarUpdate(this, 2, lowerBits);
                ic.sendProgressBarUpdate(this, 3, upperBits);
            }
        }
        lastProgress = tile.progress;
        lastMaxProgress = tile.maxProgress;
        lastEnergy = tile.energy;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) tile.progress = value;
        if (id == 1) tile.maxProgress = value;

        // FIX 3: Reconstruct the long on the client side without truncation overflow
        if (id == 2) {
            tile.energy = (tile.energy & 0xFFFFFFFF00000000L) | (value & 0xFFFFFFFFL);
        }
        if (id == 3) {
            tile.energy = (tile.energy & 0x00000000FFFFFFFFL) | (((long) value) << 32);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            itemstack = stack.copy();

            int tileSlots = tile.getSizeInventory();

            if (slotIndex < tileSlots) {
                if (!mergeItemStack(stack, tileSlots, tileSlots + 36, true)) {
                    return null;
                }
                slot.onSlotChange(stack, itemstack);
            } else {
                if (!mergeItemStack(stack, 0, tileSlots, false)) {
                    return null;
                }
            }

            if (stack.stackSize == 0) slot.putStack(null);
            else slot.onSlotChanged();

            if (stack.stackSize == itemstack.stackSize) return null;
            slot.onPickupFromSlot(player, stack);
        }
        return itemstack;
    }
}
