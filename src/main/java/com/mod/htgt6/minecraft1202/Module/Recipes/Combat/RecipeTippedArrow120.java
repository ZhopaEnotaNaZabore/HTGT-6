package com.mod.htgt6.minecraft1202.Module.Recipes.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** The 3x3 tipped-arrow recipe carries the potion and its custom effects, as in 1.20.2. */
public final class RecipeTippedArrow120 implements IRecipe {
    @Override public boolean matches(InventoryCrafting grid,World world){
        if(grid.getSizeInventory()!=9)return false;
        for(int y=0;y<3;y++)for(int x=0;x<3;x++){
            ItemStack stack=grid.getStackInRowAndColumn(x,y);
            if(stack==null||stack.stackSize<=0||stack.getItem()!=(x==1&&y==1?CombatContent.lingeringPotion:Items.arrow))return false;
        }
        return true;
    }
    @Override public ItemStack getCraftingResult(InventoryCrafting grid){
        return matches(grid,null)?result(grid.getStackInRowAndColumn(1,1)):null;
    }
    public static ItemStack result(ItemStack potion){
        if(potion==null||potion.getItem()!=CombatContent.lingeringPotion)return null;
        ItemStack result=new ItemStack(CombatContent.tippedArrow,8,potion.getItemDamage());
        if(potion.hasTagCompound()&&potion.getTagCompound().hasKey("CustomPotionEffects",9)){
            NBTTagCompound tag=new NBTTagCompound();
            tag.setTag("CustomPotionEffects",potion.getTagCompound().getTagList("CustomPotionEffects",10).copy());
            result.setTagCompound(tag);
        }
        return result;
    }
    @Override public int getRecipeSize(){return 9;}
    @Override public ItemStack getRecipeOutput(){return null;}
}
