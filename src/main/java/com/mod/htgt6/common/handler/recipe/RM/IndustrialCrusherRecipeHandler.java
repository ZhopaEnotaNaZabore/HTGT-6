package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import gregapi.data.IL;
import gregapi.data.RM;
import gregapi.recipes.Recipe;
import gregapi.oredict.OreDictItemData;
import gregapi.util.OM;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * HTGT6-owned recipe database for the Industrial Crusher. GT6 recipes are copied
 * as immutable input/output data; the machine never executes or retains a GT6
 * Recipe instance during processing. Both Crusher and Shredder maps are preserved;
 * native entries support items, fluids, hybrids and chance outputs.
 */
public final class IndustrialCrusherRecipeHandler extends RecipeMap<ChemicalPlantRecipe> {
    public static final IndustrialCrusherRecipeHandler INSTANCE = new IndustrialCrusherRecipeHandler();
    public static final int MAX_ITEM_INPUTS = 12;
    public static final int MAX_FLUID_INPUTS = 4;

    private final Set<Recipe> importedRecipes = Collections.newSetFromMap(
            new IdentityHashMap<Recipe, Boolean>());

    private IndustrialCrusherRecipeHandler() { }

    public static boolean acceptsMode(int mode, ChemicalPlantRecipe recipe) {
        return recipe != null && (mode == 0 || !recipe.source.startsWith("GT6")
                || mode == 1 && "GT6 Crusher".equals(recipe.source)
                || mode == 2 && "GT6 Shredder".equals(recipe.source));
    }

    private int nativeRecipeCount;
    private final Set<String> importedKeys = new java.util.HashSet<String>();
    private final Set<String> inspectedInputs = new java.util.HashSet<String>();

    /** Stable across recipe enumeration order and runtime numeric item IDs. */
    public static String recipeKey(ChemicalPlantRecipe recipe) {
        StringBuilder key=new StringBuilder(recipe.source).append('|').append(recipe.selectorTag)
                .append('|').append(recipe.duration).append('|').append(recipe.euPerTick).append('|').append(recipe.minTier);
        for(ItemStack[] stacks:new ItemStack[][]{recipe.inputs,recipe.outputs}){
            key.append('|');
            for(ItemStack stack:stacks)key.append(stack==null?"null":
                    net.minecraft.item.Item.itemRegistry.getNameForObject(stack.getItem())+":"+stack.getItemDamage()
                    +":"+stack.stackSize+":"+stack.getTagCompound()).append(';');
        }
        for(FluidStack[] stacks:new FluidStack[][]{recipe.fluidInputs,recipe.fluidOutputs}){
            key.append('|');
            for(FluidStack stack:stacks)key.append(stack==null?"null":stack.getFluid().getName()+":"+stack.amount+":"+stack.tag).append(';');
        }
        return key.append(java.util.Arrays.toString(recipe.outputChances)).toString();
    }

    /** Server-side lazy import, retaining the exact item, metadata and NBT of each host rock. */
    public synchronized void importGT6RecipesForInput(ItemStack stack) {
        if (stack == null || stack.getItem() == null || stack.stackSize <= 0) return;
        ItemStack input = stack.copy();
        input.stackSize = 1;
        String key = input.writeToNBT(new net.minecraft.nbt.NBTTagCompound()).toString();
        if (!inspectedInputs.add(key)) return;
        com.mod.htgt6.common.handler.recipes.GT6CrusherRecipeMaterializer.materializeInput(input);
        // Read the newly materialized exact recipe. getNEIUsages on the ordinary
        // Crusher uses OM.data (without item overrides), losing unregistered ore variants.
        List<Recipe> crusher = new ArrayList<Recipe>();
        for (Recipe recipe : RM.Crusher.mRecipeList) {
            if (recipe != null && recipe.mInputs != null) for (ItemStack required : recipe.mInputs) {
                if (required != null && required.isItemEqual(input)
                        && ItemStack.areItemStackTagsEqual(required, input)) {
                    crusher.add(recipe);
                    break;
                }
            }
        }
        importRecipes(crusher, "GT6 Crusher", false);
        importRecipes(RM.Shredder.getNEIUsages(input), "GT6 Shredder", false);
    }

    /** Native recipes always precede imports, even when registered after loadComplete. */
    @Override public synchronized void addRecipe(ChemicalPlantRecipe recipe) {
        if (recipe == null) return;
        int size = recipeList.size();
        super.addRecipe(recipe);
        if (recipeList.size() > size) {
            recipeList.remove(size);
            recipeList.add(nativeRecipeCount++, recipe);
        }
    }

    private void addImportedRecipe(ChemicalPlantRecipe recipe) { super.addRecipe(recipe); }

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
        return addRecipe("HTGT6 Industrial Crusher", selectorTag, itemInputs, fluidInputs,
                itemOutputs, fluidOutputs, outputChances, duration, euPerTick, minTier);
    }

    private static ChemicalPlantRecipe addRecipe(
            String source, int selectorTag, ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs, int[] outputChances,
            int duration, int euPerTick, int minTier) {
        ItemStack[] inputs = copyItems(itemInputs);
        ItemStack[] outputs = copyItems(itemOutputs);
        FluidStack[] fluidsIn = copyFluids(fluidInputs);
        if (inputs.length + fluidsIn.length == 0 || inputs.length > MAX_ITEM_INPUTS
                || outputs.length > 12 || fluidsIn.length > MAX_FLUID_INPUTS
                || copyFluids(fluidOutputs).length > 4
                || outputs.length + copyFluids(fluidOutputs).length == 0) return null;
        ChemicalPlantRecipe recipe = new ChemicalPlantRecipe(source, inputs, outputs,
                outputChances == null ? null : outputChances.clone(), fluidsIn,
                copyFluids(fluidOutputs), Math.max(1, duration), Math.max(1, euPerTick),
                Math.max(1, minTier), normalizeSelector(selectorTag));
        if (source.startsWith("GT6")) INSTANCE.addImportedRecipe(recipe);
        else INSTANCE.addRecipe(recipe);
        return recipe;
    }

    /** Repeatable import: loadComplete can collect recipes registered late by scripts. */
    public synchronized void importGT6CrusherRecipes() {
        inspectedInputs.clear();
        importMap(RM.Crusher, "GT6 Crusher");
        importMap(RM.Shredder, "GT6 Shredder");
    }

    private void importMap(Recipe.RecipeMap map, String source) {
        importRecipes(com.mod.htgt6.common.handler.recipes.GT6CrusherRecipeCollector.recipesFor(map), source, true);
    }

    private void importRecipes(List<Recipe> recipes, String source, boolean log) {
        int imported = 0;
        int skipped = 0;
        for (Recipe recipe : recipes) {
            if (recipe == null || importedRecipes.contains(recipe) || !recipe.mEnabled
                    || recipe.mFakeRecipe || recipe.mHidden) continue;
            String key = importKey(source, recipe);
            if (!importedKeys.add(key)) continue;
            ItemStack[] inputs = removeSelectorTags(recipe.mInputs);
            ItemStack[] outputs = copyItems(recipe.mOutputs);
            FluidStack[] fluidInputs = copyFluids(recipe.mFluidInputs);
            if (inputs.length == 0 || inputs.length > MAX_ITEM_INPUTS
                    || outputs.length > 12 || fluidInputs.length > MAX_FLUID_INPUTS) {
                skipped++;
                importedRecipes.add(recipe);
                continue;
            }
            ChemicalPlantRecipe added = addRecipe(source,
                    findSelectorTag(recipe.mInputs), inputs, fluidInputs, outputs,
                    recipe.mFluidOutputs, convertChances(recipe), clamp(recipe.mDuration),
                    clamp(recipe.mEUt), tierFor(recipe.mEUt));
            importedRecipes.add(recipe);
            if (added != null) imported++;
        }
        if (log) System.out.println("[HTGT6] Industrial Crusher imported " + imported
                + " recipes from " + source + "; skipped " + skipped
                + " unsupported recipes.");
    }

    private static String importKey(String source, Recipe recipe) {
        StringBuilder key = new StringBuilder(source).append('|').append(recipe.mDuration).append('|').append(recipe.mEUt);
        for (ItemStack[] stacks : new ItemStack[][] {recipe.mInputs, recipe.mOutputs}) {
            key.append('|');
            if (stacks != null) for (ItemStack stack : stacks) {
                key.append(stack == null ? "null" : stack.writeToNBT(new net.minecraft.nbt.NBTTagCompound()).toString()).append(';');
            }
        }
        for (FluidStack[] stacks : new FluidStack[][] {recipe.mFluidInputs, recipe.mFluidOutputs}) {
            key.append('|');
            if (stacks != null) for (FluidStack stack : stacks) {
                key.append(stack == null ? "null" : stack.writeToNBT(new net.minecraft.nbt.NBTTagCompound()).toString()).append(';');
            }
        }
        return key.append(java.util.Arrays.toString(recipe.mChances))
                .append(java.util.Arrays.toString(recipe.mMaxChances)).toString();
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
