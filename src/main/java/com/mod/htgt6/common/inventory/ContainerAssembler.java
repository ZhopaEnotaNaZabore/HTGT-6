// ==========================================
// ContainerAssembler.java
// ==========================================

package com.mod.htgt6.common.inventory;

import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.inventory.slot.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;


public class ContainerAssembler extends Container {

    private TileEntityAssembler te;

    private int lastProgress;
    private int lastMaxProgress;
    private int lastEnergy;

    public ContainerAssembler(InventoryPlayer playerInv, TileEntityAssembler te) {

        this.te = te;

        // Craft grid
        for (int i = 0; i < 3; ++i) {

            for (int j = 0; j < 3; ++j) {

                addSlotToContainer(
                        new Slot(te, j + i * 3, 16 + j * 18, 7 + i * 18)
                );
            }
        }

        // Output
        addSlotToContainer(
                new SlotFurnace(playerInv.player, te, 9, 106, 26)
        );

        // Fluid
        addSlotToContainer(
                new SlotFluidContainer(te, 10, 54, 63)
        );

        // Tier Modifier
        addSlotToContainer(
             //   new SlotTierModifier(te, 11, 65, 62)
                new SlotTierModifier(te, te, 11, 84, 63)
        );

        // Transformer Module
        addSlotToContainer(
               // new SlotTransformerModule(te, 12, 80, 62)
                new SlotTransformerModule(te, te, 12, 102, 63)
        );

        // Battery
        addSlotToContainer(
                new SlotBattery(te, 13, 138, 63)
        );

        // Player Inventory
        for (int i = 0; i < 3; ++i) {

            for (int j = 0; j < 9; ++j) {

                addSlotToContainer(
                        new Slot(
                                playerInv,
                                j + i * 9 + 9,
                                8 + j * 18,
                                84 + i * 18
                        )
                );
            }
        }

        // Hotbar
        for (int i = 0; i < 9; ++i) {

            addSlotToContainer(
                    new Slot(playerInv, i, 8 + i * 18, 142)
            );
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

            ICrafting ic = (ICrafting)crafter;

            if (lastProgress != te.progress) {
                ic.sendProgressBarUpdate(this, 0, te.progress);
            }

            if (lastMaxProgress != te.maxProgress) {
                ic.sendProgressBarUpdate(this, 1, te.maxProgress);
            }

            if (lastEnergy != (int)te.energy) {
                ic.sendProgressBarUpdate(this, 2, (int)te.energy);
            }
        }

        lastProgress = te.progress;
        lastMaxProgress = te.maxProgress;
        lastEnergy = (int)te.energy;
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

        Slot slot = (Slot)this.inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {

            ItemStack stack = slot.getStack();

            itemstack = stack.copy();

            // Machine -> Player
            if (slotIndex < 14) {

                if (!mergeItemStack(stack, 14, 50, true)) {
                    return null;
                }

                slot.onSlotChange(stack, itemstack);

            } else {

                // Battery
                if (stack.getItem() instanceof ic2.api.item.IElectricItem) {

                    if (!mergeItemStack(stack, 13, 14, false)) {
                        return null;
                    }

                } else {

                    // Crafting slots
                    if (!mergeItemStack(stack, 0, 9, false)) {

                        return null;
                    }
                }
            }

            if (stack.stackSize == 0) {

                slot.putStack(null);

            } else {

                slot.onSlotChanged();
            }

            if (stack.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, stack);
        }

        return itemstack;
    }
}