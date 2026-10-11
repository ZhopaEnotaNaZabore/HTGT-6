package com.mod.htgt6.minecraft1202.Module.Recipes.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

/** Four-side recipe: top/back, left, right, and bottom/front are retained in output NBT. */
public final class RecipeBackport120DecoratedPot implements net.minecraft.item.crafting.IRecipe {
    private static final int[][] SLOTS={{1,0},{0,1},{2,1},{1,2}};
    @Override public boolean matches(InventoryCrafting inv,World world){if(inv.getSizeInventory()!=9)return false;
        for(int row=0;row<3;row++)for(int col=0;col<3;col++){ItemStack stack=inv.getStackInRowAndColumn(col,row);boolean required=(row==0&&col==1)||(row==1&&(col==0||col==2))||(row==2&&col==1);
            if(required?!ingredient(stack):stack!=null)return false;}return true;}
    @Override public ItemStack getCraftingResult(InventoryCrafting inv){String[] values=new String[4];for(int i=0;i<4;i++)values[i]=name(inv.getStackInRowAndColumn(SLOTS[i][0],SLOTS[i][1]));
        ItemStack result=new ItemStack(Backport120Content.decoratedPot);NBTTagCompound root=new NBTTagCompound(),block=new NBTTagCompound();NBTTagList list=new NBTTagList();
        for(String value:values)list.appendTag(new NBTTagString(value));block.setTag("Sherds",list);root.setTag("BlockEntityTag",block);result.setTagCompound(root);return result;}
    private static boolean ingredient(ItemStack stack){return stack!=null&&name(stack)!=null;}
    private static String name(ItemStack stack){if(stack==null)return null;if(stack.getItem()==net.minecraft.init.Items.brick)return "brick";
        Item item=stack.getItem();for(int i=0;i<Backport120Content.potterySherds.length;i++)if(item==Backport120Content.potterySherds[i])return Backport120Content.POTTERY_SHERD_NAMES[i];return null;}
    @Override public int getRecipeSize(){return 4;}@Override public ItemStack getRecipeOutput(){return new ItemStack(Backport120Content.decoratedPot);}
}
