package com.mod.htgt6.common.inventory;



import com.mod.htgt6.common.TE.FluidSolidifierTE;
import com.mod.htgt6.api.hitech6.Slots.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class ContainerFluidSolidifier extends Container {

    private final FluidSolidifierTE te;

    private int lastProgress;
    private int lastMaxProgress;
    private long lastEnergy;

    public ContainerFluidSolidifier(InventoryPlayer playerInv, FluidSolidifierTE te) {
        this.te = te;

        // Slot 0: Mold Slot (Does not consume mold)
        addSlotToContainer(new Slot(te, 0, 44, 26));

        // Slot 1: Fluid Container Input Slot (For draining fluid)
        addSlotToContainer(new SlotFluidContainer(te, 1, 24, 26)); // Uses SlotFluidContainer[cite: 2]

        // Slot 2: Fluid Container Output Slot (Empty bucket/canister output)
        addSlotToContainer(new SlotFurnace(playerInv.player, te, 2, 14, 53));

        // Slot 3: Finished Item Output Slot[cite: 2]
        addSlotToContainer(new SlotFurnace(playerInv.player, te, 3, 106, 26));

        // Slot 4: Tier Modifier Slot[cite: 2]
        addSlotToContainer(new SlotTierModifier(te, te, 4, 76, 63, 5)); // Links to transformer slot 5[cite: 2]

        // Slot 5: Transformer Module Slot[cite: 2]
        addSlotToContainer(new SlotTransformerModule(te, te, 5, 94, 63)); // Uses SlotTransformerModule[cite: 2]


        // Player Inventory layout[cite: 2]
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Hotbar layout[cite: 2]
        for (int i = 0; i < 9; ++i) {
            addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return te.isUseableByPlayer(player); //[cite: 2]
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        for (Object crafter : crafters) {
            ICrafting ic = (ICrafting) crafter;

            if (lastProgress != te.progress) {
                ic.sendProgressBarUpdate(this, 0, te.progress); //[cite: 2]
            }
            if (lastMaxProgress != te.maxProgress) {
                ic.sendProgressBarUpdate(this, 1, te.maxProgress); //[cite: 2]
            }
            if (lastEnergy != te.energy) {
                ic.sendProgressBarUpdate(this, 2, (int) (te.energy & 0xFFFFL));
                ic.sendProgressBarUpdate(this, 5, (int) ((te.energy >>> 16) & 0xFFFFL));
                ic.sendProgressBarUpdate(this, 6, (int) ((te.energy >>> 32) & 0xFFFFL));
                ic.sendProgressBarUpdate(this, 7, (int) ((te.energy >>> 48) & 0xFFFFL));
            }

        }

        lastProgress = te.progress;
        lastMaxProgress = te.maxProgress;
        lastEnergy = te.energy;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        if (id == 0) te.progress = value;
        if (id == 1) te.maxProgress = value;
        if (id == 2) te.energy = (te.energy & ~0xFFFFL) | (value & 0xFFFFL);
        if (id == 5) te.energy = (te.energy & ~(0xFFFFL << 16)) | ((long) (value & 0xFFFF) << 16);
        if (id == 6) te.energy = (te.energy & ~(0xFFFFL << 32)) | ((long) (value & 0xFFFF) << 32);
        if (id == 7) te.energy = (te.energy & ~(0xFFFFL << 48)) | ((long) (value & 0xFFFF) << 48);

    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex); //[cite: 2]

        if (slot != null && slot.getHasStack()) { //[cite: 2]
            ItemStack stack = slot.getStack(); //[cite: 2]
            itemstack = stack.copy(); //[cite: 2]

            // Machine slots shift-clicking logic (Slots 0 to 5)
            if (slotIndex < 6) {
                if (!mergeItemStack(stack, 6, 42, true)) {
                    return null;
                }
                slot.onSlotChange(stack, itemstack); //[cite: 2]
            } else {
                // Player -> Machine shifts[cite: 2]
                if (FluidContainerRegistry.isContainer(stack) || stack.getItem() instanceof net.minecraftforge.fluids.IFluidContainerItem) {
                    if (!mergeItemStack(stack, 1, 2, false)) { // Prioritize Fluid Input slot
                        return null;
                    }
                } else if (stack.getUnlocalizedName().toLowerCase().contains("mold")) {
                    if (!mergeItemStack(stack, 0, 1, false)) { // Prioritize Mold slot
                        return null;
                    }
                } else {
                    // Fallback to checking upgrade slot groups if none of the above match
                    if (!mergeItemStack(stack, 4, 6, false)) {
                        return null;
                    }
                }
            }

            if (stack.stackSize == 0) {
                slot.putStack(null); //[cite: 2]
            } else {
                slot.onSlotChanged(); //[cite: 2]
            }

            if (stack.stackSize == itemstack.stackSize) {
                return null; //[cite: 2]
            }

            slot.onPickupFromSlot(player, stack); //[cite: 2]
        }

        return itemstack; //[cite: 2]
    }
}
