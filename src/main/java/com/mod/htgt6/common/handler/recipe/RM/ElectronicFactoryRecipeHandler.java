package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import cpw.mods.fml.common.registry.GameRegistry;
import gregapi.data.IL;
import gregapi.data.RM;
import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Independent HTGT6 recipe database for circuit, board and processor production. */
public final class ElectronicFactoryRecipeHandler extends RecipeMap<ChemicalPlantRecipe> {
    public static final ElectronicFactoryRecipeHandler INSTANCE = new ElectronicFactoryRecipeHandler();
    public static final int MAX_ITEM_INPUTS = 12;
    public static final int MAX_FLUID_INPUTS = 4;
    /**
     * GT6 and the migrated HTGT6 script recipes do not finish populating their
     * maps at the same lifecycle point. Track source recipe instances so the
     * map can be imported again during loadComplete without duplicating the
     * recipes copied during postInit.
     */
    private final Set<Recipe> importedGT6Recipes = Collections.newSetFromMap(
            new IdentityHashMap<Recipe, Boolean>());

    private ElectronicFactoryRecipeHandler() { }

    public static synchronized ChemicalPlantRecipe addRecipe(
            ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs,
            int duration, int euPerTick, int minTier) {
        return addRecipe(-1, itemInputs, fluidInputs, itemOutputs, fluidOutputs,
                null, duration, euPerTick, minTier);
    }

    public static synchronized ChemicalPlantRecipe addRecipe(
            int selectorTag, ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs, int[] outputChances,
            int duration, int euPerTick, int minTier) {
        return addRecipe("HTGT6 Electronic Factory", selectorTag, itemInputs, fluidInputs,
                itemOutputs, fluidOutputs, outputChances, duration, euPerTick, minTier);
    }

    private static ChemicalPlantRecipe addRecipe(
            String source, int selectorTag, ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs, int[] outputChances,
            int duration, int euPerTick, int minTier) {
        ItemStack[] copiedInputs = copyItems(itemInputs);
        FluidStack[] copiedFluidInputs = copyFluids(fluidInputs);
        if (copiedInputs.length < 1 || copiedInputs.length > MAX_ITEM_INPUTS
                || copiedFluidInputs.length > MAX_FLUID_INPUTS) return null;
        ChemicalPlantRecipe recipe = new ChemicalPlantRecipe(source,
                copiedInputs, copyItems(itemOutputs),
                outputChances == null ? null : outputChances.clone(),
                copiedFluidInputs, copyFluids(fluidOutputs),
                Math.max(1, duration), Math.max(1, euPerTick), Math.max(1, minTier),
                selectorTag >= 0 && selectorTag <= 24 ? selectorTag : -1);
        if (recipe.inputs.length + recipe.fluidInputs.length == 0
                || recipe.outputs.length + recipe.fluidOutputs.length == 0) return null;
        INSTANCE.addRecipe(recipe);
        return recipe;
    }

    /**
     * Incrementally copies the recipe map used by GT6 Circuit Assemblers into
     * the HTGT6-owned database. This method is intentionally repeatable: the
     * second invocation picks up recipes inserted by ModpackGeneratedRecipes
     * during loadComplete.
     */
    public synchronized void importGT6CircuitAssemblerRecipes() {
        ImportResult result = importMap("GT6 Circuit Assembler", RM.Assembler.mRecipeList, false);
        System.out.println("[HTGT6] Electronics Factory imported " + result.imported
                + " new GT6 Circuit Assembler recipes; skipped " + result.skippedByShape
                + " recipes outside the 1-12 item / 0-4 fluid input limits.");
    }

    /**
     * Copies HTGT6 SMD component recipes from the GT6 Press while leaving the
     * originals in that machine. The factory therefore acts as the production
     * line alternative requested by the pack, not as a destructive migration.
     */
    public synchronized void importGT6PressSMDRecipes() {
        ImportResult result = importMap("GT6 Press / SMD line", RM.Press.mRecipeList, true);
        System.out.println("[HTGT6] Electronics Factory imported " + result.imported
                + " new SMD Press recipes; skipped " + result.skippedByShape
                + " recipes outside the factory input limits.");
    }

    private ImportResult importMap(String source, Collection<Recipe> recipes, boolean smdOnly) {
        ImportResult result = new ImportResult();
        if (recipes == null) return result;
        for (Recipe recipe : recipes) {
            if (recipe == null || importedGT6Recipes.contains(recipe)
                    || !recipe.mEnabled || recipe.mFakeRecipe || recipe.mHidden
                    || smdOnly && !hasHTGT6SMDOutput(recipe.mOutputs)) continue;
            ItemStack[] inputs = removeSelectorTags(recipe.mInputs);
            FluidStack[] fluidInputs = copyFluids(recipe.mFluidInputs);
            if (inputs.length < 1 || inputs.length > MAX_ITEM_INPUTS
                    || fluidInputs.length > MAX_FLUID_INPUTS) {
                result.skippedByShape++;
                importedGT6Recipes.add(recipe);
                continue;
            }
            ChemicalPlantRecipe added = addRecipe(source,
                    findSelectorTag(recipe.mInputs), inputs, fluidInputs,
                    recipe.mOutputs, recipe.mFluidOutputs, convertChances(recipe),
                    clampPositive(recipe.mDuration), clampPositive(recipe.mEUt), tierFor(recipe.mEUt));
            importedGT6Recipes.add(recipe);
            if (added != null) result.imported++;
        }
        return result;
    }

    private static boolean hasHTGT6SMDOutput(ItemStack[] outputs) {
        if (outputs == null) return false;
        for (ItemStack output : outputs) {
            if (output == null || output.getItem() == null) continue;
            GameRegistry.UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(output.getItem());
            if (id != null && "HTGT6".equalsIgnoreCase(id.modId)
                    && id.name != null && id.name.toLowerCase(java.util.Locale.ROOT).contains("smd")) return true;
        }
        return false;
    }

    private static final class ImportResult {
        int imported;
        int skippedByShape;
    }

    private static int findSelectorTag(ItemStack[] inputs) {
        if (inputs == null) return -1;
        for (ItemStack input : inputs) {
            if (input != null && IL.Circuit_Selector.equal(input)) {
                int metadata = input.getItemDamage();
                return metadata >= 0 && metadata <= 24 ? metadata : -1;
            }
        }
        return -1;
    }

    private static ItemStack[] removeSelectorTags(ItemStack[] inputs) {
        if (inputs == null) return new ItemStack[0];
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (ItemStack input : inputs) {
            if (input != null && input.getItem() != null && !IL.Circuit_Selector.equal(input)) {
                result.add(input.copy());
            }
        }
        return result.toArray(new ItemStack[result.size()]);
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

    private static ItemStack[] copyItems(ItemStack[] stacks) {
        if (stacks == null) return new ItemStack[0];
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (ItemStack stack : stacks) {
            if (stack != null && stack.getItem() != null) result.add(stack.copy());
        }
        return result.toArray(new ItemStack[result.size()]);
    }

    private static FluidStack[] copyFluids(FluidStack[] stacks) {
        if (stacks == null) return new FluidStack[0];
        List<FluidStack> result = new ArrayList<FluidStack>();
        for (FluidStack stack : stacks) {
            if (stack != null && stack.getFluid() != null) result.add(stack.copy());
        }
        return result.toArray(new FluidStack[result.size()]);
    }
}
