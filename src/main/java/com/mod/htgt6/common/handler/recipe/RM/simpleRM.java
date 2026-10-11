package com.mod.htgt6.common.handler.recipe.RM;



import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.CommonProxy;
import com.mod.htgt6.common.prospector.ItemGT6BulkScanner;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class simpleRM {

    /**
     * Call this method in your Main Mod Class during the Init or PostInit phase.
     */
    public static void init() {
        registerCraftingRecipes();
        registerSmeltingRecipes();
    }

    private static void registerCraftingRecipes() {
        // Example Shaped Recipe: Diamond Sword pattern using your custom items/blocks
        // (Replace 'Items.diamond' with your own ModItems.yourItem if needed)
        // GameRegistry.addRecipe(new ItemStack(ModItems.customSword), " D ", " D ", " S ", 'D', Items.diamond, 'S', Items.stick);

        // Example Shapeless Recipe: Turning a block back into 9 items
        // GameRegistry.addShapelessRecipe(new ItemStack(ModItems.customIngot, 9), new ItemStack(ModBlocks.customBlock));
    }

    private static void registerSmeltingRecipes() {
        // Example Smelting Recipe (Input, Output, XP granted)
        // GameRegistry.addSmelting(ModBlocks.customOre, new ItemStack(ModItems.customIngot), 0.7F);
    }
}