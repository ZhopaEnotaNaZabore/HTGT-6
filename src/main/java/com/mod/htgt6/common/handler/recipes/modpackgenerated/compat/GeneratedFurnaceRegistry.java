package com.mod.htgt6.common.handler.recipes.modpackgenerated.compat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedRecipeSettings;
import com.mod.htgt6.common.handler.recipes.modpackgenerated.GeneratedValueResolver;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

/** Native furnace.addRecipe/furnace.remove migration. */
public final class GeneratedFurnaceRegistry {

    private GeneratedFurnaceRegistry() {}

    public static void add(String source, String outputToken, String inputToken,
            GeneratedRecipeSettings settings, Logger logger) {
        ItemStack output = GeneratedValueResolver.item(outputToken, source + " output", settings, logger);
        ItemStack input = GeneratedValueResolver.item(inputToken, source + " input", settings, logger);
        if (output != null && input != null) GameRegistry.addSmelting(input, output, 0.0F);
    }

    @SuppressWarnings("unchecked")
    public static int remove(String source, String outputToken,
            GeneratedRecipeSettings settings, Logger logger) {
        List<ItemStack> targets = GeneratedValueResolver.matchingItems(outputToken, source, settings, logger);
        Map<ItemStack, ItemStack> recipes = FurnaceRecipes.smelting().getSmeltingList();
        List<ItemStack> keys = new ArrayList<ItemStack>();
        for (Map.Entry<ItemStack, ItemStack> entry : recipes.entrySet()) {
            for (ItemStack target : targets) {
                if (OreDictionary.itemMatches(target, entry.getValue(), false)) {
                    keys.add(entry.getKey());
                    break;
                }
            }
        }
        for (ItemStack key : keys) recipes.remove(key);
        return keys.size();
    }
}
