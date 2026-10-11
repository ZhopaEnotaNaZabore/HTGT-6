package com.mod.htgt6.common.handler.recipes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import crazypants.enderio.machine.crusher.CrusherRecipeManager;
import crazypants.enderio.machine.recipe.OreDictionaryRecipeInput;
import crazypants.enderio.machine.recipe.RecipeBonusType;
import crazypants.enderio.machine.recipe.RecipeInput;
import crazypants.enderio.machine.recipe.RecipeOutput;
import gregapi.data.RM;
import gregapi.oredict.OreDictItemData;
import gregapi.recipes.Recipe;
import gregapi.util.OM;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Replaces EnderIO's legacy SAG Mill table with the final GT6 Crusher and
 * Shredder tables.  Inputs and outputs are copied at load-complete time, so
 * they cannot drift when another mod wins ore-dictionary unification.
 */
public final class EnderIOSagMillGT6Recipes {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-EnderIO-SAG");

    /** EnderIO's basic capacitor consumes 20 RF/t. */
    private static final long BASIC_RF_PER_TICK = 20L;
    private static final long TIME_PENALTY = 8L;
    private static final int SAG_OUTPUT_SLOTS = 4;

    private EnderIOSagMillGT6Recipes() {}

    public static void register() {
        if (!Loader.isModLoaded("EnderIO")) return;

        CrusherRecipeManager manager = CrusherRecipeManager.getInstance();
        int removedRecipes = manager.getRecipes().size();
        int removedBalls = manager.getBalls().size();
        manager.getRecipes().clear();
        // Grinding balls would alter GT6 output quantities and chances, so the
        // inherited recipe table deliberately does not accept any of them.
        manager.getBalls().clear();

        Set<String> occupiedInputs = new HashSet<String>();
        ImportStats crusher = importMap(manager, RM.Crusher, "Crusher", occupiedInputs);
        ImportStats shredder = importMap(manager, RM.Shredder, "Shredder", occupiedInputs);

        LOGGER.info(
                "Replaced {} EnderIO SAG recipes and {} grinding balls with {} GT6 recipes "
                        + "(Crusher={}, Shredder={}, ore inputs={}, map-materialized={}, input-generated={}, "
                        + "skipped={}, duplicate inputs={}); time penalty={}x",
                removedRecipes,
                removedBalls,
                crusher.added + shredder.added,
                crusher.added,
                shredder.added,
                crusher.oreInputs + shredder.oreInputs,
                crusher.materialized + shredder.materialized,
                crusher.dynamic + shredder.dynamic,
                crusher.skipped + shredder.skipped,
                crusher.duplicates + shredder.duplicates,
                TIME_PENALTY);
    }

    private static ImportStats importMap(
            CrusherRecipeManager manager,
            Recipe.RecipeMap source,
            String sourceName,
            Set<String> occupiedInputs) {
        ImportStats stats = new ImportStats();
        if (source == null || source.mRecipeList == null) return stats;

        // GT6 keeps the ordinary ore-processing chains in recipe-map handlers
        // and creates them lazily for NEI/findRecipe.  Reading mRecipeList
        // directly therefore misses ore -> crushed ore and the following
        // stages.  getNEIAllRecipes() asks those handlers to materialize their
        // complete recipe set before we take our stable snapshot.
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
            String inputKey = stackKey(inputStack);
            if (!occupiedInputs.add(inputKey) || manager.getRecipeForInput(inputStack) != null) {
                stats.duplicates++;
                continue;
            }

            RecipeOutput[] outputs = copyOutputs(gtRecipe);
            if (outputs.length == 0) {
                stats.skipped++;
                occupiedInputs.remove(inputKey);
                continue;
            }

            int energyCost = energyForEightTimesDuration(gtRecipe.mDuration);
            RecipeInput input = oreDictionaryInput(inputStack);
            manager.addRecipe(new crazypants.enderio.machine.recipe.Recipe(
                    input,
                    energyCost,
                    RecipeBonusType.NONE,
                    outputs));
            stats.added++;
            if (isOreInput(inputStack)) stats.oreInputs++;
        }

        if ("Crusher".equals(sourceName) && stats.oreInputs == 0) {
            LOGGER.warn("GT6 Crusher materialization produced no ore-input recipes");
        }
        LOGGER.debug(
                "Imported GT6 {} recipes: added={}, ore inputs={}, materialized={}, dynamic={}, skipped={}, duplicates={}",
                sourceName, stats.added, stats.oreInputs, stats.materialized, stats.dynamic,
                stats.skipped, stats.duplicates);
        return stats;
    }

    private static boolean isOreInput(ItemStack stack) {
        OreDictItemData association = OM.anyassociation(stack);
        return association != null && association.mPrefix != null
                && association.mPrefix.mNameInternal != null
                && association.mPrefix.mNameInternal.startsWith("ore");
    }

    private static boolean isUsable(Recipe recipe) {
        if (recipe == null || !recipe.mEnabled || recipe.mFakeRecipe) return false;
        if (recipe.mInputs == null || recipe.mInputs.length != 1 || recipe.mInputs[0] == null) return false;
        if (recipe.mFluidInputs != null && recipe.mFluidInputs.length > 0) return false;
        if (recipe.mFluidOutputs != null && recipe.mFluidOutputs.length > 0) return false;
        if (recipe.mOutputs == null || recipe.mOutputs.length == 0) return false;
        boolean hasOutput = false;
        for (ItemStack output : recipe.mOutputs) {
            if (output != null && output.getItem() != null && output.stackSize > 0) {
                hasOutput = true;
                break;
            }
        }
        if (!hasOutput) return false;
        return recipe.mInputs[0].getItem() != null && recipe.mInputs[0].stackSize > 0;
    }

    private static RecipeOutput[] copyOutputs(Recipe recipe) {
        List<RecipeOutput> outputs = new ArrayList<RecipeOutput>(recipe.mOutputs.length);
        for (int i = 0; i < recipe.mOutputs.length; i++) {
            ItemStack output = recipe.mOutputs[i];
            if (output == null || output.getItem() == null || output.stackSize <= 0) continue;
            int maximum = recipe.getMaxChance(i);
            int chanceValue = recipe.getOutputChance(i);
            float chance = maximum > 0 ? Math.min(1.0F, chanceValue / (float) maximum) : 1.0F;
            if (chance <= 0.0F) continue;
            outputs.add(new RecipeOutput(output.copy(), chance, 0.0F));
            if (outputs.size() == SAG_OUTPUT_SLOTS) break;
        }
        return outputs.toArray(new RecipeOutput[outputs.size()]);
    }

    private static RecipeInput oreDictionaryInput(ItemStack stack) {
        OreDictItemData association = OM.anyassociation(stack);
        if (association != null && association.hasValidPrefixMaterialData()
                && association.mPrefix != null && association.mMaterial != null
                && association.mMaterial.mMaterial != null) {
            String oreName = association.mPrefix.mNameInternal
                    + association.mMaterial.mMaterial.mNameInternal;
            int oreId = OreDictionary.getOreID(oreName);
            if (!OreDictionary.getOres(oreId).isEmpty()) {
                return new OreDictionaryRecipeInput(stack, oreId, 0);
            }
        }
        // Metadata must remain significant for GT6's shared prefix items.
        return new RecipeInput(stack, true);
    }

    private static int energyForEightTimesDuration(long gtDurationTicks) {
        long duration = Math.max(1L, gtDurationTicks);
        long energy = duration * TIME_PENALTY * BASIC_RF_PER_TICK;
        return energy > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) energy;
    }

    private static String stackKey(ItemStack stack) {
        String name = String.valueOf(Item.getIdFromItem(stack.getItem()));
        return name + ':' + stack.getItemDamage() + ':' + stack.stackSize
                + ':' + (stack.hasTagCompound() ? stack.getTagCompound().toString() : "");
    }

    private static final class ImportStats {
        int added;
        int oreInputs;
        int materialized;
        int dynamic;
        int skipped;
        int duplicates;
    }
}
