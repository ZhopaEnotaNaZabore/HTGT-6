package com.mod.htgt6.minecraft1202.Module.Recipes.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

/** Copies a patterned banner onto an otherwise blank banner of the same base colour. */
public final class RecipeBountifulBannerDuplicate implements IRecipe {
    @Override public boolean matches(InventoryCrafting inventory,World world){return patterned(inventory)!=null;}
    @Override public ItemStack getCraftingResult(InventoryCrafting inventory){
        ItemStack source=patterned(inventory);if(source==null)return null;
        ItemStack result=source.copy();result.stackSize=2;return result;
    }
    private static ItemStack patterned(InventoryCrafting inventory){
        ItemStack patterned=null,blank=null;
        for(int i=0;i<inventory.getSizeInventory();i++){
            ItemStack stack=inventory.getStackInSlot(i);if(stack==null)continue;
            if(stack.getItem()!=BountifulContent.banner)return null;
            if(patternCount(stack)>0){if(patterned!=null)return null;patterned=stack;}
            else {if(blank!=null)return null;blank=stack;}
        }
        return patterned!=null&&blank!=null&&(patterned.getItemDamage()&15)==(blank.getItemDamage()&15)
                ?patterned:null;
    }
    private static int patternCount(ItemStack stack){
        if(!stack.hasTagCompound())return 0;NBTTagCompound root=stack.getTagCompound();
        if(!root.hasKey("BlockEntityTag",10))return 0;
        NBTTagCompound block=root.getCompoundTag("BlockEntityTag");
        NBTTagList list=block.getTagList("Patterns",10);return list.tagCount();
    }
    @Override public int getRecipeSize(){return 2;}
    @Override public ItemStack getRecipeOutput(){return new ItemStack(BountifulContent.banner,2,0);}
}
