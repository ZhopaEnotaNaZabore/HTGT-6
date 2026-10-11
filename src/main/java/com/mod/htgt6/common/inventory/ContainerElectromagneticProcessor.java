package com.mod.htgt6.common.inventory;

import com.mod.htgt6.api.hitech6.BaseMachines.ContainerBaseMachine;
import com.mod.htgt6.common.TE.TEElectromagneticProcessor;
import com.mod.htgt6.api.hitech6.Slots.SlotDeepStorage;
import com.mod.htgt6.api.hitech6.Slots.SlotFluidContainer;
import com.mod.htgt6.api.hitech6.Slots.SlotOutputDeep;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ContainerElectromagneticProcessor extends ContainerBaseMachine {

    public ContainerElectromagneticProcessor(InventoryPlayer playerInv, TEElectromagneticProcessor tile) {
        super(playerInv, tile);

        // 1 Deep Input Slot (Slot 0)
        addSlotToContainer(new SlotDeepStorage(tile, TEElectromagneticProcessor.SLOT_INPUT, 16, 25));

        // 6 Deep Output Slots (Slots 1-6) using SlotOutputDeep to block manual input insertion
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIdx = TEElectromagneticProcessor.SLOT_OUTPUT_START + (row * 3 + col);
                addSlotToContainer(new SlotOutputDeep(tile, slotIdx, 107 + col * 18, 16 + row * 18));
            }
        }

        // Fluid Drain Container Slot (Slot 7)
        addSlotToContainer(new SlotFluidContainer(tile, TEElectromagneticProcessor.SLOT_FLUID_CONTAINER, 54, 63));

        // Upgrades: Module, Transformer, Battery
        addUpgradeSlots(
                TEElectromagneticProcessor.SLOT_MODULE, 84, 63,
                TEElectromagneticProcessor.SLOT_TRANSFORMER, 102, 63,
                TEElectromagneticProcessor.SLOT_BATTERY, 138, 63
        );

        // Standard Player Inventory & Hotbar
        addPlayerInventory(playerInv, 8, 84);
    }


    @Override public void detectAndSendChanges() {
        super.detectAndSendChanges();
        TEElectromagneticProcessor machine=(TEElectromagneticProcessor)tile;
        for(Object listener:crafters)for(int i=0;i<2;i++) {
            FluidStack fluid=machine.tanks[i].getFluid();
            net.minecraft.inventory.ICrafting client=(net.minecraft.inventory.ICrafting)listener;
            client.sendProgressBarUpdate(this,10+i*2,fluid==null?-1:fluid.getFluidID());
            client.sendProgressBarUpdate(this,11+i*2,fluid==null?0:fluid.amount);
        }
    }
    @Override @SideOnly(Side.CLIENT) public void updateProgressBar(int id,int value) {
        if(id<10||id>13){super.updateProgressBar(id,value);return;}
        TEElectromagneticProcessor machine=(TEElectromagneticProcessor)tile;
        int tank=(id-10)/2;
        if((id&1)==0)machine.tanks[tank].setFluid(value<0?null:new FluidStack(FluidRegistry.getFluid(value),0));
        else if(machine.tanks[tank].getFluid()!=null)machine.tanks[tank].getFluid().amount=value;
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
                    // If it's a deep slot, allow up to slot limit (4096)
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
