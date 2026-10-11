package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemLeatherHorseArmor151;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/** Modern brightness-preserving RGB mix, with modern dye colors in legacy metadata order. */
public final class RecipeHorseArmorDye151 implements IRecipe {
    private static final int[] RGB={0x1D1D21,0xB02E26,0x5E7C16,0x835432,0x3C44AA,0x8932B8,0x169C9C,0x9D9D97,0x474F52,0xF38BAA,0x80C71F,0xFED83D,0x3AB3DA,0xC74EBD,0xF9801D,0xF9FFFE};
    @Override public boolean matches(InventoryCrafting inv,World world){return getCraftingResult(inv)!=null;}
    @Override public ItemStack getCraftingResult(InventoryCrafting inv){
        ItemStack armor=null;int[] sums=new int[3];int brightness=0,count=0,dyes=0;
        for(int i=0;i<inv.getSizeInventory();i++){
            ItemStack stack=inv.getStackInSlot(i);if(stack==null)continue;
            int color;
            if(stack.getItem() instanceof ItemLeatherHorseArmor151){
                if(armor!=null)return null;armor=stack.copy();armor.stackSize=1;
                if(!ItemLeatherHorseArmor151.hasColor(stack))continue;
                color=ItemLeatherHorseArmor151.color(stack);
            }else{int dye=VillageWorkstationLogic120.dyeColor(stack);if(dye<0||dye>15)return null;color=RGB[dye];dyes++;}
            int r=color>>16&255,g=color>>8&255,b=color&255;sums[0]+=r;sums[1]+=g;sums[2]+=b;brightness+=Math.max(r,Math.max(g,b));count++;
        }
        if(armor==null||dyes==0)return null;
        int r=sums[0]/count,g=sums[1]/count,b=sums[2]/count;float mean=(float)brightness/count,max=Math.max(r,Math.max(g,b));
        if(max>0){r=(int)(r*mean/max);g=(int)(g*mean/max);b=(int)(b*mean/max);}
        ItemLeatherHorseArmor151.setColor(armor,r<<16|g<<8|b);return armor;
    }
    @Override public int getRecipeSize(){return 10;}
    @Override public ItemStack getRecipeOutput(){return null;}
}
