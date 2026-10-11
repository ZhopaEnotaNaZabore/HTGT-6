package com.mod.htgt6.common.handler.recipe.RM;

import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.api.hitech6.RM.RecipeMap;
import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import com.mod.htgt6.common.handler.recipe.RM.ChemicalPlantRecipeHandler.ChemicalPlantRecipe;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.RM;
import gregapi.recipes.Recipe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Collections;
import java.util.IdentityHashMap;

/** Independent HTGT6 database for normal and implosion compression recipes. */
public final class CompressorRecipeHandler extends RecipeMap<CompressorRecipeHandler.CompressionRecipe> {
    public enum CompressionType { COMPRESSOR, IMPLOSION }
    public static final CompressorRecipeHandler INSTANCE = new CompressorRecipeHandler();
    private final Set<Recipe> importedGT6Recipes = Collections.newSetFromMap(
            new IdentityHashMap<Recipe, Boolean>());
    private CompressorRecipeHandler() { }

    /** Backwards-compatible HTGT6/CraftTweaker entry. */
    public static CompressionRecipe addRecipe(ItemStack[] inputs, ItemStack output,
                                               int duration, int euPerTick, int tier) {
        return addRecipe(CompressionType.COMPRESSOR, "HTGT6", inputs, null,
                new ItemStack[] { output }, null, null, duration, euPerTick, tier);
    }

    public static CompressionRecipe addImplosionRecipe(ItemStack[] inputs, ItemStack output,
                                                        int duration, int euPerTick, int tier) {
        return addRecipe(CompressionType.IMPLOSION, "HTGT6 Implosion", inputs, null,
                new ItemStack[] { output }, null, null, duration, euPerTick, tier);
    }

    /** Public addon entry, including fluid and chance outputs. */
    public static synchronized CompressionRecipe addRecipe(
            CompressionType type, String source, ItemStack[] itemInputs, FluidStack[] fluidInputs,
            ItemStack[] itemOutputs, FluidStack[] fluidOutputs, int[] outputChances,
            int duration, int euPerTick, int minTier) {
        CompressionRecipe recipe = new CompressionRecipe(type, source, copyItems(itemInputs),
                copyItems(itemOutputs), outputChances == null ? null : outputChances.clone(),
                copyFluids(fluidInputs), copyFluids(fluidOutputs), Math.max(1, duration),
                Math.max(1, euPerTick), Math.max(1, minTier));
        if (recipe.inputs.length + recipe.fluidInputs.length == 0
                || recipe.outputs.length + recipe.fluidOutputs.length == 0) return null;
        INSTANCE.addRecipe(recipe);
        return recipe;
    }

    /** Copy GT6 recipes after its registries are complete; no GT machine integration is retained. */
    public synchronized void importGT6Recipes() {
        int normal = importMap(CompressionType.COMPRESSOR, "GT6 Compressor", RM.Compressor.mRecipeList);
        int implosion = importMap(CompressionType.IMPLOSION, "GT6 Implosion Compressor",
                RM.ImplosionCompressor.mRecipeList);
        if (normal + implosion > 0) System.out.println("[HTGT6] Super-Massive Compressor imported "
                + normal + " compressor and " + implosion + " implosion recipes from GT6.");
    }

    private static int importMap(CompressionType type, String source, Collection<Recipe> recipes) {
        if (recipes == null) return 0;
        int imported = 0;
        for (Recipe recipe : recipes) {
            if (recipe == null || !recipe.mEnabled || recipe.mFakeRecipe || recipe.mHidden) continue;
            if (!INSTANCE.importedGT6Recipes.add(recipe)) continue;
            CompressionRecipe added = addRecipe(type, source, recipe.mInputs, recipe.mFluidInputs,
                    recipe.mOutputs, recipe.mFluidOutputs, convertChances(recipe),
                    clamp(recipe.mDuration), clamp(recipe.mEUt), tierFor(recipe.mEUt));
            if (added != null) imported++;
            else INSTANCE.importedGT6Recipes.remove(recipe);
        }
        return imported;
    }

    private static int[] convertChances(Recipe recipe) {
        if (recipe.mOutputs == null) return new int[0];
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < recipe.mOutputs.length; i++) {
            if (recipe.mOutputs[i] == null) continue;
            long chance = recipe.mChances != null && i < recipe.mChances.length ? recipe.mChances[i] : 10000L;
            long maximum = recipe.mMaxChances != null && i < recipe.mMaxChances.length
                    && recipe.mMaxChances[i] > 0 ? recipe.mMaxChances[i] : 10000L;
            result.add((int) Math.max(0L, Math.min(10000L, chance * 10000L / maximum)));
        }
        int[] chances = new int[result.size()];
        for (int i = 0; i < chances.length; i++) chances[i] = result.get(i);
        return chances;
    }

    private static int tierFor(long eut) {
        long required = Math.max(1L, eut);
        for (int i = 0; i < TileEntityBaseMachine.VOLTAGES.length; i++) {
            if (required <= TileEntityBaseMachine.VOLTAGES[i]) return i + 1;
        }
        return TileEntityBaseMachine.VOLTAGES.length;
    }

    private static int clamp(long value) {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, value));
    }

    private static ItemStack[] copyItems(ItemStack[] values) {
        if (values == null) return new ItemStack[0];
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (ItemStack value : values) if (value != null && value.getItem() != null) result.add(value.copy());
        return result.toArray(new ItemStack[result.size()]);
    }

    private static FluidStack[] copyFluids(FluidStack[] values) {
        if (values == null) return new FluidStack[0];
        List<FluidStack> result = new ArrayList<FluidStack>();
        for (FluidStack value : values) if (value != null && value.getFluid() != null) result.add(value.copy());
        return result.toArray(new FluidStack[result.size()]);
    }

    public static void registerSMCrecipes() {
        addDense(MT.Lead, 32, 1); addDense(MT.Iron, 32, 1); addDense(MT.Copper, 32, 1);
        addDense(MT.AnnealedCopper, 32, 1); addDense(MT.Gold, 32, 1); addDense(MT.Bronze, 32, 1);
        addDense(MT.Aluminium, 32, 1); addDense(MT.Brass, 32, 1); addDense(MT.Steel, 32, 1);
        addDense(MT.TinAlloy, 32, 1); addDense(MT.Sn, 32, 1); addDense(MT.Arsenic, 32, 1);
        addDense(MT.SolderingAlloy, 32, 1); addDense(MT.Sb, 32, 1); addDense(MT.Silver, 32, 1);
        addDense(MT.BatteryAlloy, 32, 1); addDense(MT.Chromium, 32, 1); addDense(MT.Nickel, 32, 1);
        addDense(GT6MaterialAutoGenerator.POLY_ETHYLENE, 32, 1); addDense(MT.Constantan, 32, 1);
        addDense(MT.AluminiumBrass, 128, 2); addDense(MT.Cobalt, 128, 2);
        addDense(MT.VanadiumSteel, 128, 2); addDense(MT.V, 128, 2); addDense(MT.Electrum, 128, 2);
        addDense(MT.BlueAlloy, 128, 2); addDense(MT.Electrotine, 128, 2); addDense(MT.Kanthal, 128, 2);
        addDense(MT.StainlessSteel, 512, 3); addDense(MT.Nichrome, 512, 3);
        addDense(MT.Titanium, 512, 3); addDense(MT.Ultimet, 512, 3); addDense(MT.Trinium, 512, 3);
        addDense(MT.Platinum, 512, 3); addDense(MT.Palladium, 512, 3); addDense(MT.Ta, 512, 3);
        addDense(MT.Tungsten, 2048, 4); addDense(MT.TungstenSteel, 2048, 4);
        addDense(MT.TungstenCarbide, 2048, 4); addDense(MT.Osmium, 2048, 4);
        addDense(MT.Iridium, 2048, 4); addDense(MT.Iritanium, 2048, 4);
    }

    private static void addDense(gregapi.oredict.OreDictMaterial material, int eut, int tier) {
        addRecipe(new ItemStack[] { OP.plate.mat(material, 9) }, OP.plateDense.mat(material, 1),
                1600, eut, tier);
    }

    public static final class CompressionRecipe extends ChemicalPlantRecipe {
        public final CompressionType compressionType;
        private CompressionRecipe(CompressionType type, String source, ItemStack[] inputs,
                                  ItemStack[] outputs, int[] chances, FluidStack[] fluidInputs,
                                  FluidStack[] fluidOutputs, int duration, int euPerTick, int minTier) {
            super(source, inputs, outputs, chances, fluidInputs, fluidOutputs,
                    duration, euPerTick, minTier, -1);
            compressionType = type == null ? CompressionType.COMPRESSOR : type;
        }
    }
}
