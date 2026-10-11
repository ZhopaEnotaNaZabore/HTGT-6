package com.mod.htgt6.minecraft1202.Module.Recipes.Exploration;

import com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemBlockShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/** 1.12 recolouring recipe; preserves the complete BlockEntityTag payload. */
public final class RecipeShulkerColor112 implements IRecipe {
    @Override public boolean matches(InventoryCrafting inventory, World world) {
        return result(inventory) != null;
    }

    @Override public ItemStack getCraftingResult(InventoryCrafting inventory) { return result(inventory); }
    @Override public int getRecipeSize() { return 2; }
    @Override public ItemStack getRecipeOutput() { return null; }

    private static ItemStack result(InventoryCrafting inventory) {
        ItemStack box = null, dye = null;
        for (int slot = 0; slot < inventory.getSizeInventory(); ++slot) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack == null) continue;
            if (stack.getItem() instanceof ItemBlockShulkerBox112) {
                if (box != null) return null; box = stack;
            } else if (stack.getItem() == Items.dye) {
                if (dye != null) return null; dye = stack;
            } else return null;
        }
        if (box == null || dye == null) return null;
        int color = 15 - (dye.getItemDamage() & 15);
        Item target = Item.getItemFromBlock(ExplorationContent112.shulkerBoxes[color]);
        ItemStack output = new ItemStack(target);
        if (box.hasTagCompound()) output.setTagCompound((net.minecraft.nbt.NBTTagCompound)box.getTagCompound().copy());
        if (box.hasDisplayName()) output.setStackDisplayName(box.getDisplayName());
        return output;
    }
}
