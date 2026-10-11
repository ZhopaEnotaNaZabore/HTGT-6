package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.api.hitech6.Misc.MachineState;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.common.handler.recipe.RM.MetalFormerRecipeMap;
import net.minecraft.item.ItemStack;

public class MetalFormerTE extends TileEntityBaseMachine {

    // Slot Definitions
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_MOLD = 2; // Special catalyst slot
    public static final int SLOT_MODULE = 3;
    public static final int SLOT_TRANSFORMER = 4;
    public static final int SLOT_BATTERY = 5;

    public MachineState state = MachineState.IDLE;
    private MachineRecipe currentRecipe = null;

    public MetalFormerTE() {
        // 6 Slots: 1 Input, 1 Output, 1 Mold, +3 Upgrades
        super(6);
    }

    @Override
    public int getModuleSlot() {
        return SLOT_MODULE;
    }

    @Override
    public int getTransformerSlot() {
        return SLOT_TRANSFORMER;
    }

    @Override
    protected void processMachineLogic() {
        // If no recipe is actively running, check for a new valid recipe match
        if (currentRecipe == null || progress == 0) {
            // Ensure input slot and mold slot are both populated
            if (inventory[SLOT_INPUT] == null || inventory[SLOT_MOLD] == null) {
                this.state = MachineState.IDLE;
                this.progress = 0;
                this.currentRecipe = null;
                return;
            }

            // Find recipe matching current input and mold
            MachineRecipe foundRecipe = MetalFormerRecipeMap.INSTANCE.findRecipe(inventory[SLOT_INPUT], inventory[SLOT_MOLD]);

            if (foundRecipe != null && MetalFormerRecipeMap.INSTANCE.canOutput(inventory, SLOT_OUTPUT, foundRecipe)) {
                long euTick = MetalFormerRecipeMap.getOverclockedEUt(foundRecipe, machineTier);

                // Check power to start
                if (this.energy >= euTick) {
                    this.currentRecipe = foundRecipe;
                    this.maxProgress = MetalFormerRecipeMap.getOverclockedDuration(currentRecipe, machineTier);

                    // Consume items from Input Slot 0 directly inside this class
                    consumeInputDirectly();

                    this.energy -= euTick;
                    this.progress = 1;
                    this.state = MachineState.RUNNING;
                } else {
                    this.state = MachineState.NO_POWER;
                }
            } else if (foundRecipe != null) {
                this.state = MachineState.OUTPUT_FULL;
            } else {
                this.state = MachineState.IDLE;
            }
            return;
        }

        // Active processing loop (Items were already consumed upon start)
        if (currentRecipe != null && progress > 0) {
            if (MetalFormerRecipeMap.INSTANCE.canOutput(inventory, SLOT_OUTPUT, currentRecipe)) {
                long euTick = MetalFormerRecipeMap.getOverclockedEUt(currentRecipe, machineTier);

                if (this.energy >= euTick) {
                    this.energy -= euTick;
                    this.progress++;
                    this.state = MachineState.RUNNING;

                    // Recipe completed -> Produce output stack
                    if (this.progress >= this.maxProgress) {
                        outputRecipe();
                        this.progress = 0;
                        this.currentRecipe = null;
                    }
                } else {
                    this.state = MachineState.NO_POWER;
                }
            } else {
                this.state = MachineState.OUTPUT_FULL;
            }
        }
    }

    /**
     * Self-contained consumption logic defined entirely inside this TE.
     * Decrements the stack size in SLOT_INPUT based on recipe requirements and clears the slot if 0.
     * Ignores SLOT_MOLD completely so the mold is preserved.
     */
    private void consumeInputDirectly() {
        if (currentRecipe != null && currentRecipe.inputs.length > 0 && currentRecipe.inputs[0] != null) {
            int requiredAmount = currentRecipe.inputs[0].stackSize;
            ItemStack inputStack = inventory[SLOT_INPUT];

            if (inputStack != null) {
                inputStack.stackSize -= requiredAmount;

                if (inputStack.stackSize <= 0) {
                    inventory[SLOT_INPUT] = null;
                }
                this.markDirty();
            }
        }
    }

    /** Self-contained output generation logic */
    private void outputRecipe() {
        if (currentRecipe != null && currentRecipe.outputs.length > 0 && currentRecipe.outputs[0] != null) {
            ItemStack out = currentRecipe.outputs[0].copy();
            if (inventory[SLOT_OUTPUT] == null) {
                inventory[SLOT_OUTPUT] = out;
            } else {
                inventory[SLOT_OUTPUT].stackSize += out.stackSize;
            }
            this.markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return null;
    }
}
