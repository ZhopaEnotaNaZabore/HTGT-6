package com.mod.htgt6.api.hitech6.RM;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;

import gregapi.block.multitileentity.MultiTileEntityRegistry;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictPrefix;
import gregapi.util.ST;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

public abstract class RecipeMap<R extends MachineRecipe> {

    private static final MultiTileEntityRegistry GT_REGISTRY =
            MultiTileEntityRegistry.getRegistry("gt.multitileentity");

    protected final List<R> recipeList = new ArrayList<R>();

    public List<R> getRecipeList() {
        return recipeList;
    }

    /** Safety validation & registration */
    public void addRecipe(R recipe) {
        if (recipe == null) return;

        // Check outputs
        for (ItemStack out : recipe.outputs) {
            if (out == null || out.getItem() == null) {
                System.out.println("[HTGT6] Skipped adding recipe: An output item is NULL!");
                return;
            }
        }

        // Check inputs
        for (ItemStack in : recipe.inputs) {
            if (in != null && in.getItem() == null) {
                System.out.println("[HTGT6] Skipped adding recipe for "
                        + recipe.outputs[0].getDisplayName() + ": An input item is NULL!");
                return;
            }
        }

        recipeList.add(recipe);
    }

    // ==========================================
    // COMMON RECIPE STACK FACTORIES
    // ==========================================

    public static ItemStack gt(OreDictPrefix prefix, OreDictMaterial material, long amount) {
        if (prefix == null || material == null || amount <= 0) return null;

        /*
         * GT6 pipes, wires and cables are MultiTileEntities. Their material is
         * stored in NBT, not in the Item damage value, so constructing a plain
         * ItemStack is not sufficient. The OreDictionary entry is the canonical
         * stack and already contains the registry id and all required MTE NBT.
         */
        String oreName = prefix.mNameInternal + material.mNameInternal;
        List<ItemStack> registeredStacks = OreDictionary.getOres(oreName);
        if (registeredStacks != null) {
            for (ItemStack registeredStack : registeredStacks) {
                if (ST.valid(registeredStack)) {
                    return ST.amount(amount, registeredStack);
                }
            }
        }

        // Keep support for regular GT prefix items which may not have been
        // exposed to Forge's OreDictionary yet. Unsupported combinations are
        // simply absent from the recipe map instead of crashing registration.
        try {
            ItemStack generatedStack = prefix.mat(material, amount);
            return ST.valid(generatedStack) ? generatedStack : null;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static ItemStack mte(int id, int amount) {
        return GT_REGISTRY == null ? null : GT_REGISTRY.getItem(id, amount);
    }

    public static FluidStack fluid(Fluid fluid, int amount) {
        return fluid == null || amount <= 0 ? null : new FluidStack(fluid, amount);
    }

    /** Copies the source stack, so changing a recipe amount never mutates GT6's shared stack. */
    public static FluidStack fluid(FluidStack source, int amount) {
        if (source == null || source.getFluid() == null || amount <= 0) return null;
        FluidStack result = source.copy();
        result.amount = amount;
        return result;
    }

    public static FluidStack molten(OreDictMaterial material, int amount) {
        return material == null ? null : fluid(material.mLiquid, amount);
    }

    public static FluidStack molten(FluidStack source, int amount) {
        return fluid(source, amount);
    }

    // ==========================================
    // MATCHING & CONSUMPTION LOGIC
    // ==========================================

    public R findRecipe(ItemStack[] inventory, int startInputSlot, int inputCount, int outputSlot) {
        for (R recipe : recipeList) {
            if (matches(recipe, inventory, startInputSlot, inputCount)) {
                if (canOutput(inventory[outputSlot], recipe.outputs[0])) {
                    return recipe;
                }
            }
        }
        return null;
    }

    public boolean matches(R recipe, ItemStack[] inventory, int startInputSlot, int inputCount) {
        boolean[] usedSlots = new boolean[inputCount];
        for (int i = 0; i < recipe.inputs.length; i++) {
            ItemStack recipeStack = recipe.inputs[i];
            if (recipeStack == null) continue;

            boolean foundMatch = false;
            for (int j = 0; j < inputCount; j++) {
                if (usedSlots[j]) continue;
                ItemStack invStack = inventory[startInputSlot + j];
                if (invStack != null && areItemStacksEqual(recipeStack, invStack) && invStack.stackSize >= recipeStack.stackSize) {
                    foundMatch = true;
                    usedSlots[j] = true;
                    break;
                }
            }
            if (!foundMatch) return false;
        }
        return true;
    }

    public boolean canOutput(ItemStack outputSlot, ItemStack recipeOutput) {
        if (outputSlot == null) return true;
        if (!outputSlot.isItemEqual(recipeOutput)) return false;
        if (!ItemStack.areItemStackTagsEqual(outputSlot, recipeOutput)) return false;
        return outputSlot.stackSize + recipeOutput.stackSize <= outputSlot.getMaxStackSize();
    }

    public void consumeInputs(R recipe, ItemStack[] inventory, int startInputSlot, int inputCount) {
        boolean[] usedSlots = new boolean[inputCount];
        for (ItemStack recipeStack : recipe.inputs) {
            if (recipeStack == null) continue;

            for (int j = 0; j < inputCount; j++) {
                if (usedSlots[j]) continue;
                ItemStack invStack = inventory[startInputSlot + j];
                if (invStack != null && areItemStacksEqual(recipeStack, invStack)
                        && invStack.stackSize >= recipeStack.stackSize) {
                    invStack.stackSize -= recipeStack.stackSize;
                    if (invStack.stackSize <= 0) {
                        inventory[startInputSlot + j] = null;
                    }
                    usedSlots[j] = true;
                    break;
                }
            }
        }
    }

    public void outputRecipe(R recipe, ItemStack[] inventory, int outputSlot) {
        if (recipe.outputs.length == 0 || recipe.outputs[0] == null) return;

        if (inventory[outputSlot] == null) {
            inventory[outputSlot] = recipe.outputs[0].copy();
        } else {
            inventory[outputSlot].stackSize += recipe.outputs[0].stackSize;
        }
    }


    public boolean areItemStacksEqual(ItemStack target, ItemStack input) {
        if (target == null || input == null) return false;
        if (target.getItem() != input.getItem()) return false;

        boolean damageMatches = (target.getItemDamage() == OreDictionary.WILDCARD_VALUE)
                || (target.getItemDamage() == input.getItemDamage());

        return damageMatches && ItemStack.areItemStackTagsEqual(target, input);
    }

 //   public boolean areItemStacksEqual(ItemStack target, ItemStack input) {
   //     if (target == null || input == null) return false;
   //     if (target.getItem() != input.getItem()) return false;
  //      return target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input.getItemDamage();
  //  }

    // ==========================================
    // OVERCLOCKING MATHEMATICS
    // ==========================================

    public static int getOverclockedDuration(MachineRecipe recipe, int machineTier) {
        if (machineTier <= recipe.minTier) return recipe.duration;
        int diff = machineTier - recipe.minTier;
        int result = recipe.duration >> diff; // Fast divide by 2^diff
        return Math.max(result, 1);
    }

    public static long getOverclockedEUt(MachineRecipe recipe, int machineTier) {
        return MachineTierSystem.overclockEUt(recipe.euPerTick, recipe.minTier, machineTier);
    }
}
