package com.mod.htgt6.common.handler.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import cpw.mods.fml.common.Loader;
import gregapi.data.RM;
import gregapi.oredict.OreDictItemData;
import gregapi.recipes.Recipe;
import gregapi.util.OM;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Replaces the IE Crusher table with the final, materialized GT6 maps. */
public final class ImmersiveEngineeringCrusherGT6Recipes {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-IE-Crusher");
    private static final long TIME_PENALTY = 8L;
    private static final int FALLBACK_RF_PER_TICK = 80;

    private ImmersiveEngineeringCrusherGT6Recipes() {}

    public static void register() {
        if (!Loader.isModLoaded("ImmersiveEngineering")) return;

        int removed = CrusherRecipe.recipeList.size();
        CrusherRecipe.recipeList.clear();
        // IE caches both successful and failed lookups. Without this clear an
        // input inspected before load-complete can keep its obsolete result.
        TileEntityCrusher.recipeCache.clear();

        int rfPerTick = Config.getInt("crusher_consumption");
        if (rfPerTick <= 0) rfPerTick = FALLBACK_RF_PER_TICK;

        Set<String> occupiedInputs = new HashSet<String>();
        ImportStats crusher = importMap(RM.Crusher, "Crusher", occupiedInputs, rfPerTick);
        ImportStats shredder = importMap(RM.Shredder, "Shredder", occupiedInputs, rfPerTick);

        LOGGER.info(
                "Replaced {} IE Crusher recipes with {} GT6 recipes "
                        + "(Crusher={}, Shredder={}, ore inputs={}, crushed inputs={}, "
                        + "map-materialized={}, input-generated={}, skipped={}, duplicates={}); time penalty={}x at {} RF/t",
                removed,
                crusher.added + shredder.added,
                crusher.added,
                shredder.added,
                crusher.oreInputs + shredder.oreInputs,
                crusher.crushedInputs + shredder.crushedInputs,
                crusher.materialized + shredder.materialized,
                crusher.dynamic + shredder.dynamic,
                crusher.skipped + shredder.skipped,
                crusher.duplicates + shredder.duplicates,
                TIME_PENALTY,
                rfPerTick);
    }

    private static ImportStats importMap(
            Recipe.RecipeMap source,
            String sourceName,
            Set<String> occupiedInputs,
            int rfPerTick) {
        ImportStats stats = new ImportStats();
        if (source == null || source.mRecipeList == null) return stats;

        GT6CrusherRecipeCollector.Snapshot snapshot = GT6CrusherRecipeCollector.collect(source);
        List<Recipe> recipes = snapshot.recipes;
        stats.materialized = snapshot.materialized;
        stats.dynamic = snapshot.dynamic;

        for (Recipe gtRecipe : recipes) {
            if (!isUsable(gtRecipe)) {
                stats.skipped++;
                continue;
            }

            ItemStack inputStack = gtRecipe.mInputs[0].copy();
            Object input = recipeInput(inputStack);
            String inputKey = input instanceof String
                    ? "ore:" + input
                    : "stack:" + stackKey(inputStack);
            if (!occupiedInputs.add(inputKey)) {
                stats.duplicates++;
                continue;
            }

            int primaryIndex = guaranteedOutputIndex(gtRecipe);
            if (primaryIndex < 0) {
                occupiedInputs.remove(inputKey);
                stats.skipped++;
                continue;
            }

            CrusherRecipe recipe = CrusherRecipe.addRecipe(
                    gtRecipe.mOutputs[primaryIndex].copy(),
                    input,
                    energyCost(gtRecipe.mDuration, rfPerTick));
            addSecondaryOutputs(recipe, gtRecipe, primaryIndex);
            stats.added++;

            String prefix = prefixName(inputStack);
            if (prefix.startsWith("ore")) stats.oreInputs++;
            if (prefix.startsWith("crushed")) stats.crushedInputs++;
        }

        if ("Crusher".equals(sourceName) && stats.oreInputs == 0) {
            LOGGER.warn("GT6 Crusher import produced no ore-input recipes");
        }
        LOGGER.debug(
                "Imported GT6 {} into IE: added={}, ore={}, crushed={}, materialized={}, dynamic={}, skipped={}, duplicates={}",
                sourceName, stats.added, stats.oreInputs, stats.crushedInputs,
                stats.materialized, stats.dynamic, stats.skipped, stats.duplicates);
        return stats;
    }

    private static boolean isUsable(Recipe recipe) {
        if (recipe == null || !recipe.mEnabled || recipe.mFakeRecipe) return false;
        if (recipe.mInputs == null || recipe.mInputs.length != 1 || recipe.mInputs[0] == null) return false;
        if (recipe.mFluidInputs != null && recipe.mFluidInputs.length > 0) return false;
        if (recipe.mFluidOutputs != null && recipe.mFluidOutputs.length > 0) return false;
        if (recipe.mOutputs == null || recipe.mOutputs.length == 0) return false;
        if (recipe.mInputs[0].getItem() == null || recipe.mInputs[0].stackSize <= 0) return false;
        return guaranteedOutputIndex(recipe) >= 0;
    }

    private static int guaranteedOutputIndex(Recipe recipe) {
        for (int i = 0; i < recipe.mOutputs.length; i++) {
            if (recipe.mOutputs[i] == null || recipe.mOutputs[i].getItem() == null
                    || recipe.mOutputs[i].stackSize <= 0) continue;
            int maximum = recipe.getMaxChance(i);
            if (maximum <= 0 || recipe.getOutputChance(i) >= maximum) return i;
        }
        return -1;
    }

    private static void addSecondaryOutputs(CrusherRecipe target, Recipe source, int primaryIndex) {
        List<ItemStack> outputs = new ArrayList<ItemStack>();
        List<Float> chances = new ArrayList<Float>();
        for (int i = 0; i < source.mOutputs.length; i++) {
            if (i == primaryIndex) continue;
            if (source.mOutputs[i] == null || source.mOutputs[i].getItem() == null
                    || source.mOutputs[i].stackSize <= 0) continue;
            int maximum = source.getMaxChance(i);
            float chance = maximum > 0
                    ? Math.min(1.0F, source.getOutputChance(i) / (float) maximum)
                    : 1.0F;
            if (chance <= 0.0F) continue;
            outputs.add(source.mOutputs[i].copy());
            chances.add(Float.valueOf(chance));
        }
        target.secondaryOutput = outputs.toArray(new ItemStack[outputs.size()]);
        target.secondaryChance = new float[chances.size()];
        for (int i = 0; i < chances.size(); i++) target.secondaryChance[i] = chances.get(i).floatValue();
    }

    private static Object recipeInput(ItemStack stack) {
        String oreName = oreName(stack);
        // A String input in IE consumes one item. Preserve larger GT6 inputs
        // as exact stacks so their required quantity is not accidentally lost.
        return oreName != null && stack.stackSize == 1 ? oreName : stack;
    }

    private static String oreName(ItemStack stack) {
        OreDictItemData association = OM.anyassociation(stack);
        if (association == null || !association.hasValidPrefixMaterialData()
                || association.mPrefix == null || association.mMaterial == null
                || association.mMaterial.mMaterial == null) return null;
        String name = association.mPrefix.mNameInternal
                + association.mMaterial.mMaterial.mNameInternal;
        int oreId = OreDictionary.getOreID(name);
        return OreDictionary.getOres(oreId).isEmpty() ? null : name;
    }

    private static String prefixName(ItemStack stack) {
        OreDictItemData association = OM.anyassociation(stack);
        return association != null && association.mPrefix != null
                && association.mPrefix.mNameInternal != null
                ? association.mPrefix.mNameInternal
                : "";
    }

    private static int energyCost(long gtDurationTicks, int rfPerTick) {
        long energy = Math.max(1L, gtDurationTicks) * TIME_PENALTY * Math.max(1, rfPerTick);
        return energy > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) energy;
    }

    private static String stackKey(ItemStack stack) {
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage() + ":" + stack.stackSize
                + ":" + (stack.hasTagCompound() ? stack.getTagCompound().toString() : "");
    }

    private static final class ImportStats {
        int added;
        int oreInputs;
        int crushedInputs;
        int materialized;
        int dynamic;
        int skipped;
        int duplicates;
    }
}
