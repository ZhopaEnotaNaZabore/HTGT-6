package com.mod.htgt6.common.inventory;

import com.mod.htgt6.common.TE.TileEntityAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

public class ContainerAssembler extends Container {
    private TileEntityAssembler te;
    private int lastProgress, lastMaxProgress, lastEnergy;

    public ContainerAssembler(InventoryPlayer playerInv, TileEntityAssembler te) {
        this.te = te;

        // 1. 3x3 Input Grid (Slots 0-8)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlotToContainer(new Slot(te, j + i * 3, 17 + j * 18, 7 + i * 18));
            }
        }

        // 2. Output Slot (Slot 9)
        this.addSlotToContainer(new SlotFurnace(playerInv.player, te, 9, 107, 25));

        // 3. Upgrade/Warning Slot (Slot 10)
        this.addSlotToContainer(new Slot(te, 10, 53, 62));

        // 4. Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // 5. Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafter : crafters) {
            ICrafting ic = (ICrafting) crafter;
            if (this.lastProgress != te.progress) ic.sendProgressBarUpdate(this, 0, te.progress);
            if (this.lastMaxProgress != te.maxProgress) ic.sendProgressBarUpdate(this, 1, te.maxProgress);
            if (this.lastEnergy != (int)te.energy) ic.sendProgressBarUpdate(this, 2, (int)te.energy);
        }
        this.lastProgress = te.progress;
        this.lastMaxProgress = te.maxProgress;
        this.lastEnergy = (int)te.energy;
    }

    @Override
    public void updateProgressBar(int id, int val) {
        if (id == 0) te.progress = val;
        if (id == 1) te.maxProgress = val;
        if (id == 2) te.energy = val;
    }

    @Override
    public boolean canInteractWith(EntityPlayer p) { return te.isUseableByPlayer(p); }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index < 11) { // From Machine to Player
                if (!this.mergeItemStack(itemstack1, 11, 47, true)) return null;
            } else { // From Player to Machine (Inputs only)
                if (!this.mergeItemStack(itemstack1, 0, 9, false)) return null;
            }

            if (itemstack1.stackSize == 0) slot.putStack(null);
            else slot.onSlotChanged();

            if (itemstack1.stackSize == itemstack.stackSize) return null;
            slot.onPickupFromSlot(player, itemstack1);
        }
        return itemstack;
    }
}