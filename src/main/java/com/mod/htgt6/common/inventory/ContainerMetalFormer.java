package com.mod.htgt6.common.inventory;

import com.mod.htgt6.api.hitech6.BaseMachines.ContainerBaseMachine;
import com.mod.htgt6.common.TE.MetalFormerTE;
import com.mod.htgt6.api.hitech6.Slots.SlotDeepStorage;
import com.mod.htgt6.api.hitech6.Slots.SlotOutputDeep;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMetalFormer extends ContainerBaseMachine {

    public ContainerMetalFormer(InventoryPlayer playerInv, MetalFormerTE tile) {
        super(playerInv, tile);

        // 1. Deep Input Slot (Slot 0) - Positioned same as EMP (16, 25)
        addSlotToContainer(new SlotDeepStorage(tile, MetalFormerTE.SLOT_INPUT, 16, 25));

        // 2. Deep Output Slot (Slot 1) - Positioned same as top-left output slot of EMP (107, 16)
        addSlotToContainer(new SlotOutputDeep(tile, MetalFormerTE.SLOT_OUTPUT, 107, 25));

        // 3. Special Mold Slot (Slot 2) - Positioned at the bottom row (54, 63)
        addSlotToContainer(new Slot(tile, MetalFormerTE.SLOT_MOLD, 54, 63));

        // 4. Upgrade Slots (Module, Transformer, Battery) - Same as EMP container
        addUpgradeSlots(
                MetalFormerTE.SLOT_MODULE, 84, 63,
                MetalFormerTE.SLOT_TRANSFORMER, 102, 63,
                MetalFormerTE.SLOT_BATTERY, 138, 63
        );

        // Standard Player Inventory & Hotbar (8, 84)
        addPlayerInventory(playerInv, 8, 84);
    }

    @Override
    protected boolean mergeItemStack(ItemStack stackToShift, int startIndex, int endIndex, boolean reverseDirection) {
        boolean success = false;
        int currentIndex = reverseDirection ? endIndex - 1 : startIndex;

        // Phase 1: Try stacking into existing non-empty slots
        if (stackToShift.isStackable()) {
            while (stackToShift.stackSize > 0 && (reverseDirection ? currentIndex >= startIndex : currentIndex < endIndex)) {
                Slot slot = (Slot) this.inventorySlots.get(currentIndex);
                ItemStack stackInSlot = slot.getStack();

                if (stackInSlot != null && stackInSlot.getItem() == stackToShift.getItem()
                        && (!stackToShift.getHasSubtypes() || stackToShift.getItemDamage() == stackInSlot.getItemDamage())
                        && ItemStack.areItemStackTagsEqual(stackToShift, stackInSlot)) {

                    int maxLimit = Math.min(slot.getSlotStackLimit(), stackToShift.getMaxStackSize());
                    if (slot instanceof SlotDeepStorage) {
                        maxLimit = slot.getSlotStackLimit();
                    }

                    int totalSize = stackInSlot.stackSize + stackToShift.stackSize;

                    if (totalSize <= maxLimit) {
                        stackToShift.stackSize = 0;
                        stackInSlot.stackSize = totalSize;
                        slot.onSlotChanged();
                        success = true;
                    } else if (stackInSlot.stackSize < maxLimit) {
                        stackToShift.stackSize -= maxLimit - stackInSlot.stackSize;
                        stackInSlot.stackSize = maxLimit;
                        slot.onSlotChanged();
                        success = true;
                    }
                }

                if (reverseDirection) currentIndex--;
                else currentIndex++;
            }
        }

        // Phase 2: Place into empty slots if stack is still remaining
        if (stackToShift.stackSize > 0) {
            currentIndex = reverseDirection ? endIndex - 1 : startIndex;

            while (reverseDirection ? currentIndex >= startIndex : currentIndex < endIndex) {
                Slot slot = (Slot) this.inventorySlots.get(currentIndex);
                ItemStack stackInSlot = slot.getStack();

                if (stackInSlot == null && slot.isItemValid(stackToShift)) {
                    int maxLimit = Math.min(slot.getSlotStackLimit(), stackToShift.getMaxStackSize());
                    if (slot instanceof SlotDeepStorage) {
                        maxLimit = slot.getSlotStackLimit();
                    }

                    if (stackToShift.stackSize > maxLimit) {
                        slot.putStack(stackToShift.splitStack(maxLimit));
                    } else {
                        slot.putStack(stackToShift.copy());
                        stackToShift.stackSize = 0;
                    }

                    slot.onSlotChanged();
                    success = true;
                    break;
                }

                if (reverseDirection) currentIndex--;
                else currentIndex++;
            }
        }

        return success;
    }
}
