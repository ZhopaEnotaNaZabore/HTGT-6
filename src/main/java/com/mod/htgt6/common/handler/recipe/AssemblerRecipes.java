package com.mod.htgt6.common.handler.recipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class AssemblerRecipes {
    private static final AssemblerRecipes instance = new AssemblerRecipes();
    private final List<AssemblerRecipe> recipes = new ArrayList<AssemblerRecipe>();

    public static AssemblerRecipes getInstance() {
        return instance;
    }

    private AssemblerRecipes() {}
    public List<AssemblerRecipe> getRecipes() {
        return recipes;
    }

    public void registerRecipes() {
        // Example: 16 Torches Recipe
        ItemStack[] torchInput = new ItemStack[9];
        torchInput[0] = new ItemStack(Items.coal, 4);
        torchInput[1] = new ItemStack(Items.stick, 4);

        addRecipe(new AssemblerRecipe(
                torchInput,
                new ItemStack(Blocks.torch, 16),
                100,
                8,
                1
        ));
        ItemStack[] IOdispenser = new ItemStack[9];
        IOdispenser[0] = new ItemStack(Blocks.cobblestone, 6);
        IOdispenser[1] = new ItemStack(Items.redstone, 4);
        IOdispenser[3] = new ItemStack(Items.iron_ingot, 2);

        addRecipe(new AssemblerRecipe(
                IOdispenser,
                new ItemStack(Blocks.dispenser, 1),
                400,
                16,
                1
        ));
    }

    public void addRecipe(AssemblerRecipe recipe) {
        this.recipes.add(recipe);
    }

    public AssemblerRecipe getRecipe(ItemStack[] inv) {
        for (AssemblerRecipe recipe : recipes) {
            if (recipe.matches(inv)) {
                return recipe;
            }
        }
        return null;
    }
}