package com.mod.htgt6.common.handler.recipe.RM;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraftforge.oredict.*;
import org.apache.logging.log4j.LogManager;

/** Additive copies of the final component crafting grids, without crafting tools. */
public final class ComponentAssemblerRecipes {
    private static final Set<IRecipe> imported = Collections.newSetFromMap(new IdentityHashMap<IRecipe, Boolean>());
    private ComponentAssemblerRecipes() {}

    public static int componentTier(ItemStack output) {
        Item item = GameRegistry.findItem("gregtech", "gt.multiitem.technological");
        if (output == null || item == null || output.getItem() != item) return -1;
        for (int base : new int[] {12000,12020,12040,12060,12080,12100,12120,12140}) {
            int tier = output.getItemDamage() - base;
            if (tier >= 0 && tier <= 5) return tier;
        }
        return -1;
    }

    public static ItemStack selector() {
        ItemStack tag = GameRegistry.findItemStack("gregapi", "gt.integrated_circuit", 1);
        if (tag != null) { tag.setItemDamage(23); tag.stackSize = 0; }
        return tag;
    }

    public static void register() {
        ItemStack tag = selector();
        if (tag == null) {
            LogManager.getLogger("HTGT6-Components").warn("Selector tag unavailable; component assembler recipes skipped");
            return;
        }
        int added = 0;
        for (Object value : CraftingManager.getInstance().getRecipeList()) {
            IRecipe recipe = (IRecipe)value;
            int tier = componentTier(recipe.getRecipeOutput());
            if (tier < 0 || tier > 3 || imported.contains(recipe)) continue;
            Object[] ingredients;
            if (recipe instanceof ShapedOreRecipe) ingredients = ((ShapedOreRecipe)recipe).getInput();
            else if (recipe instanceof ShapedRecipes) ingredients = ((ShapedRecipes)recipe).recipeItems;
            else continue;
            List<ItemStack> parts = new ArrayList<ItemStack>();
            boolean valid = true;
            for (Object ingredient : ingredients) {
                if (ingredient == null) continue;
                ItemStack stack = null;
                if (ingredient instanceof ItemStack) stack = (ItemStack)ingredient;
                else if (ingredient instanceof List && !((List<?>)ingredient).isEmpty())
                    stack = (ItemStack)((List<?>)ingredient).get(0);
                if (stack == null) { valid = false; break; }
                if (isTool(stack)) continue;
                boolean merged = false;
                for (ItemStack part : parts) {
                    if (part.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(part, stack)) {
                        part.stackSize += stack.stackSize; merged = true; break;
                    }
                }
                if (!merged) parts.add(stack.copy());
            }
            if (!valid || parts.isEmpty() || parts.size() > 8) {
                LogManager.getLogger("HTGT6-Components").warn("Cannot import component crafting recipe {}", recipe.getRecipeOutput());
                continue;
            }
            ItemStack[] inputs = new ItemStack[9];
            for (int i = 0; i < parts.size(); i++) inputs[i] = parts.get(i);
            inputs[8] = tag.copy();
            // The unupgraded HiTech6 assembler is internally tier 1. Using 0
            // would overclock 8 EU/t to 32 EU/t on that base machine.
            AssemblerRecipeHandler.addRecipe(inputs, recipe.getRecipeOutput().copy(), 20, 8, 1);
            imported.add(recipe); added++;
        }
        LogManager.getLogger("HTGT6-Components").info("Added {} ULV-HV component assembler recipes with selector 23", added);
    }

    private static boolean isTool(ItemStack stack) {
        for (int id : OreDictionary.getOreIDs(stack))
            if (OreDictionary.getOreName(id).startsWith("craftingTool")) return true;
        return false;
    }
}
