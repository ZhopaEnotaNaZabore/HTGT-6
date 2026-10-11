package com.mod.htgt6.minecraft1202.Module.Recipes.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldBannerData120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/** Shapeless banner + undecorated shield recipe from Java Edition. */
public final class RecipeShieldDecoration120 implements IRecipe {
    @Override public boolean matches(InventoryCrafting inventory, World world) {
        return ingredients(inventory) != null;
    }

    @Override public ItemStack getCraftingResult(InventoryCrafting inventory) {
        ItemStack[] ingredients = ingredients(inventory);
        if (ingredients == null) return null;
        ItemStack result = ingredients[0].copy();
        result.stackSize = 1;
        ShieldBannerData120.copyBannerDecoration(result, ingredients[1]);
        return result;
    }

    private static ItemStack[] ingredients(InventoryCrafting inventory) {
        ItemStack shield = null, banner = null;
        for (int slot = 0; slot < inventory.getSizeInventory(); ++slot) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null) continue;
            if (stack.getItem() == CombatContent.shield && shield == null
                    && !ShieldBannerData120.hasDecoration(stack)) shield = stack;
            else if (stack.getItem() == BountifulContent.banner && banner == null) banner = stack;
            else return null;
        }
        return shield != null && banner != null ? new ItemStack[]{shield, banner} : null;
    }

    @Override public int getRecipeSize() { return 2; }
    @Override public ItemStack getRecipeOutput() { return new ItemStack(CombatContent.shield); }
}
