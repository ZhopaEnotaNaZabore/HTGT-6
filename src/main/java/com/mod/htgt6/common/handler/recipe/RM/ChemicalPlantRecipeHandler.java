package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import gregapi.data.RM;
import gregapi.data.IL;
import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

/** HTGT6-owned Chemical Plant recipe database, including copied GT6 mixer recipes. */
public final class ChemicalPlantRecipeHandler extends RecipeMap<ChemicalPlantRecipeHandler.ChemicalPlantRecipe> {
    public static final ChemicalPlantRecipeHandler INSTANCE = new ChemicalPlantRecipeHandler();
    private boolean gt6Imported;

    private ChemicalPlantRecipeHandler() { }

    /** Public entry point for HTGT6 and addon recipes. Arrays are defensively copied. */
    public static ChemicalPlantRecipe addRecipe(ItemStack[] itemInputs, FluidStack[] fluidInputs,
                                                 ItemStack[] itemOutputs, FluidStack[] fluidOutputs,
                                                 int duration, int euPerTick, int minTier) {
        return addRecipe("HTGT6", itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                null, duration, euPerTick, minTier, -1);
    }

    /** Custom recipe entry with a required virtual GT6 Selector Tag (0-24), or -1 for none. */
    public static ChemicalPlantRecipe addRecipe(int selectorTag, ItemStack[] itemInputs, FluidStack[] fluidInputs,
                                                 ItemStack[] itemOutputs, FluidStack[] fluidOutputs,
                                                 int duration, int euPerTick, int minTier) {
        return addRecipe("HTGT6", itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                null, duration, euPerTick, minTier, selectorTag);
    }

    /** Entry point with source label and optional item-output chances in basis points. */
    public static synchronized ChemicalPlantRecipe addRecipe(String source, ItemStack[] itemInputs,
                                                              FluidStack[] fluidInputs, ItemStack[] itemOutputs,
                                                              FluidStack[] fluidOutputs, int[] outputChances,
                                                              int duration, int euPerTick, int minTier, int selectorTag) {
        ChemicalPlantRecipe recipe = new ChemicalPlantRecipe(source, copyItems(itemInputs), copyItems(itemOutputs),
                outputChances == null ? null : outputChances.clone(), copyFluids(fluidInputs), copyFluids(fluidOutputs),
                Math.max(1, duration), Math.max(1, euPerTick), Math.max(1, minTier), normalizeSelector(selectorTag));
        if (recipe.inputs.length + recipe.fluidInputs.length == 0
                || recipe.outputs.length + recipe.fluidOutputs.length == 0) return null;
        INSTANCE.addRecipe(recipe);
        return recipe;
    }

    /** Copies recipes; the Chemical Plant never holds or executes GT6 Recipe objects. */
    public synchronized void importGT6MixerRecipes() {
        if (gt6Imported) return;
        gt6Imported = true;
        int mixer = importMap("GT6 Mixer", RM.Mixer.mRecipeList);
        int heat = importMap("GT6 Thermal Mixer", RM.HeatMixer.mRecipeList);
        int burn = importMap("GT6 Burner Mixer", RM.BurnMixer.mRecipeList);
        System.out.println("[HTGT6] Chemical Plant imported " + mixer + " Mixer, " + heat
                + " Thermal Mixer and " + burn + " Burner Mixer recipes from GT6.");
    }

    private static int importMap(String source, Collection<Recipe> recipes) {
        if (recipes == null) return 0;
        int imported = 0;
        for (Recipe recipe : recipes) {
            if (recipe == null || !recipe.mEnabled || recipe.mFakeRecipe || recipe.mHidden) continue;
            int[] chances = convertChances(recipe);
            int selectorTag = findSelectorTag(recipe.mInputs);
            ChemicalPlantRecipe added = addRecipe(source, removeSelectorTags(recipe.mInputs), recipe.mFluidInputs,
                    recipe.mOutputs, recipe.mFluidOutputs, chances,
                    clampPositive(recipe.mDuration), clampPositive(recipe.mEUt), tierFor(recipe.mEUt), selectorTag);
            if (added != null) imported++;
        }
        return imported;
    }

    private static int[] convertChances(Recipe recipe) {
        if (recipe.mOutputs == null) return new int[0];
        List<Integer> chances = new ArrayList<Integer>();
        for (int i = 0; i < recipe.mOutputs.length; i++) {
            if (recipe.mOutputs[i] == null || recipe.mOutputs[i].getItem() == null) continue;
            long chance = recipe.mChances != null && i < recipe.mChances.length ? recipe.mChances[i] : 10000L;
            long maximum = recipe.mMaxChances != null && i < recipe.mMaxChances.length
                    && recipe.mMaxChances[i] > 0 ? recipe.mMaxChances[i] : 10000L;
            chances.add((int) Math.max(0L, Math.min(10000L, chance * 10000L / maximum)));
        }
        int[] result = new int[chances.size()];
        for (int i = 0; i < result.length; i++) result[i] = chances.get(i);
        return result;
    }

    private static int tierFor(long euPerTick) {
        long required = Math.max(1L, euPerTick);
        for (int i = 0; i < TileEntityBaseMachine.VOLTAGES.length; i++) {
            if (required <= TileEntityBaseMachine.VOLTAGES[i]) return i + 1;
        }
        return TileEntityBaseMachine.VOLTAGES.length;
    }

    private static int clampPositive(long value) {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, value));
    }

    private static int findSelectorTag(ItemStack[] inputs) {
        if (inputs == null) return -1;
        for (ItemStack input : inputs) {
            if (input != null && IL.Circuit_Selector.equal(input)) return normalizeSelector(input.getItemDamage());
        }
        return -1;
    }

    private static ItemStack[] removeSelectorTags(ItemStack[] inputs) {
        if (inputs == null) return new ItemStack[0];
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (ItemStack input : inputs) {
            if (input != null && !IL.Circuit_Selector.equal(input)) result.add(input.copy());
        }
        return result.toArray(new ItemStack[result.size()]);
    }

    private static int normalizeSelector(int selectorTag) {
        return selectorTag >= 0 && selectorTag <= 24 ? selectorTag : -1;
    }

    private static ItemStack[] copyItems(ItemStack[] stacks) {
        if (stacks == null) return new ItemStack[0];
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (ItemStack stack : stacks) if (stack != null && stack.getItem() != null) result.add(stack.copy());
        return result.toArray(new ItemStack[result.size()]);
    }

    private static FluidStack[] copyFluids(FluidStack[] stacks) {
        if (stacks == null) return new FluidStack[0];
        List<FluidStack> result = new ArrayList<FluidStack>();
        for (FluidStack stack : stacks) if (stack != null && stack.getFluid() != null) result.add(stack.copy());
        return result.toArray(new FluidStack[result.size()]);
    }

    public static class ChemicalPlantRecipe extends MachineRecipe {
        public final String source;
        public final int selectorTag;

        public ChemicalPlantRecipe(String source, ItemStack[] inputs, ItemStack[] outputs, int[] chances,
                                    FluidStack[] fluidInputs, FluidStack[] fluidOutputs,
                                    int duration, int euPerTick, int minTier, int selectorTag) {
            super(inputs, outputs, chances, fluidInputs, fluidOutputs, duration, euPerTick, minTier);
            this.source = source == null ? "HTGT6" : source;
            this.selectorTag = selectorTag;
        }

        public ItemStack getSelectorDisplayStack() {
            return selectorTag < 0 ? null : IL.Circuit_Selector.getWithMeta(1, selectorTag);
        }
    }
}
