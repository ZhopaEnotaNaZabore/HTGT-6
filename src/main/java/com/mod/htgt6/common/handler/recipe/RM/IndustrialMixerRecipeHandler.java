package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import gregapi.data.IL;
import gregapi.data.RM;
import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * HTGT6-owned recipe database for the Industrial Mixer. GT6 recipes are copied
 * as immutable input/output data; the machine never executes or retains a GT6
 * Recipe instance. Only component recipes with both item inputs and item
 * outputs are imported, keeping fluid-only chemistry in the Chemical Plant.
 */
public final class IndustrialMixerRecipeHandler extends RecipeMap<ChemicalPlantRecipe> {
    public static final IndustrialMixerRecipeHandler INSTANCE = new IndustrialMixerRecipeHandler();
    public static final int MAX_ITEM_INPUTS = 12;
    public static final int MAX_FLUID_INPUTS = 4;

    private final Set<Recipe> importedRecipes = Collections.newSetFromMap(
            new IdentityHashMap<Recipe, Boolean>());

    private IndustrialMixerRecipeHandler() { }

    /** Public API for addons and HTGT6 configuration code. */
    public static synchronized ChemicalPlantRecipe addRecipe(
            ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs,
            int duration, int euPerTick, int minTier) {
        return addRecipe(-1, itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                null, duration, euPerTick, minTier);
    }

    /** Public configurable recipe entry with an optional virtual Selector Tag. */
    public static synchronized ChemicalPlantRecipe addRecipe(
            int selectorTag, ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs, int[] outputChances,
            int duration, int euPerTick, int minTier) {
        return addRecipe("HTGT6 Industrial Mixer", selectorTag, itemInputs, fluidInputs,
                itemOutputs, fluidOutputs, outputChances, duration, euPerTick, minTier);
    }

    private static ChemicalPlantRecipe addRecipe(
            String source, int selectorTag, ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs, int[] outputChances,
            int duration, int euPerTick, int minTier) {
        ItemStack[] inputs = copyItems(itemInputs);
        ItemStack[] outputs = copyItems(itemOutputs);
        FluidStack[] fluidsIn = copyFluids(fluidInputs);
        if (inputs.length < 1 || inputs.length > MAX_ITEM_INPUTS || outputs.length < 1
                || fluidsIn.length > MAX_FLUID_INPUTS) return null;
        ChemicalPlantRecipe recipe = new ChemicalPlantRecipe(source, inputs, outputs,
                outputChances == null ? null : outputChances.clone(), fluidsIn,
                copyFluids(fluidOutputs), Math.max(1, duration), Math.max(1, euPerTick),
                Math.max(1, minTier), normalizeSelector(selectorTag));
        INSTANCE.addRecipe(recipe);
        return recipe;
    }

    /** Repeatable import: loadComplete can collect recipes registered late by scripts. */
    public synchronized void importGT6MixerRecipes() {
        int imported = 0;
        int skipped = 0;
        if (RM.Mixer.mRecipeList != null) for (Recipe recipe : RM.Mixer.mRecipeList) {
            if (recipe == null || importedRecipes.contains(recipe) || !recipe.mEnabled
                    || recipe.mFakeRecipe || recipe.mHidden) continue;
            ItemStack[] inputs = removeSelectorTags(recipe.mInputs);
            ItemStack[] outputs = copyItems(recipe.mOutputs);
            FluidStack[] fluidInputs = copyFluids(recipe.mFluidInputs);
            if (inputs.length < 1 || outputs.length < 1 || inputs.length > MAX_ITEM_INPUTS
                    || fluidInputs.length > MAX_FLUID_INPUTS) {
                skipped++;
                importedRecipes.add(recipe);
                continue;
            }
            ChemicalPlantRecipe added = addRecipe("GT6 Mixer / Components",
                    findSelectorTag(recipe.mInputs), inputs, fluidInputs, outputs,
                    recipe.mFluidOutputs, convertChances(recipe), clamp(recipe.mDuration),
                    clamp(recipe.mEUt), tierFor(recipe.mEUt));
            importedRecipes.add(recipe);
            if (added != null) imported++;
        }
        System.out.println("[HTGT6] Industrial Mixer imported " + imported
                + " new component recipes from GT6 RM.Mixer; skipped " + skipped
                + " fluid-only or unsupported recipes.");
    }

    private static int findSelectorTag(ItemStack[] inputs) {
        if (inputs != null) for (ItemStack input : inputs) {
            if (input != null && IL.Circuit_Selector.equal(input)) {
                return normalizeSelector(input.getItemDamage());
            }
        }
        return -1;
    }

    private static ItemStack[] removeSelectorTags(ItemStack[] stacks) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        if (stacks != null) for (ItemStack stack : stacks) {
            if (stack != null && stack.getItem() != null && !IL.Circuit_Selector.equal(stack)) {
                result.add(stack.copy());
            }
        }
        return result.toArray(new ItemStack[result.size()]);
    }

    private static int[] convertChances(Recipe recipe) {
        List<Integer> result = new ArrayList<Integer>();
        if (recipe.mOutputs != null) for (int i = 0; i < recipe.mOutputs.length; i++) {
            if (recipe.mOutputs[i] == null || recipe.mOutputs[i].getItem() == null) continue;
            long chance = recipe.mChances != null && i < recipe.mChances.length
                    ? recipe.mChances[i] : 10000L;
            long maximum = recipe.mMaxChances != null && i < recipe.mMaxChances.length
                    && recipe.mMaxChances[i] > 0 ? recipe.mMaxChances[i] : 10000L;
            result.add((int) Math.max(0L, Math.min(10000L, chance * 10000L / maximum)));
        }
        int[] chances = new int[result.size()];
        for (int i = 0; i < chances.length; i++) chances[i] = result.get(i);
        return chances;
    }

    private static int tierFor(long euPerTick) {
        long required = Math.max(1L, euPerTick);
        for (int i = 0; i < TileEntityBaseMachine.VOLTAGES.length; i++) {
            if (required <= TileEntityBaseMachine.VOLTAGES[i]) return i + 1;
        }
        return TileEntityBaseMachine.VOLTAGES.length;
    }

    private static int clamp(long value) {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, value));
    }

    private static int normalizeSelector(int selector) {
        return selector >= 0 && selector <= 24 ? selector : -1;
    }

    private static ItemStack[] copyItems(ItemStack[] stacks) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        if (stacks != null) for (ItemStack stack : stacks) {
            if (stack != null && stack.getItem() != null) result.add(stack.copy());
        }
        return result.toArray(new ItemStack[result.size()]);
    }

    private static FluidStack[] copyFluids(FluidStack[] stacks) {
        List<FluidStack> result = new ArrayList<FluidStack>();
        if (stacks != null) for (FluidStack stack : stacks) {
            if (stack != null && stack.getFluid() != null) result.add(stack.copy());
        }
        return result.toArray(new FluidStack[result.size()]);
    }
}
