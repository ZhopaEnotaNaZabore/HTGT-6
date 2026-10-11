package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemLeatherHorseArmor151;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;

public final class LeatherHorseArmor151 {
    public static final ItemLeatherHorseArmor151 ITEM=new ItemLeatherHorseArmor151();
    private static boolean registered;
    private LeatherHorseArmor151(){}
    public static void register(){
        if(registered)return;
        GameRegistry.registerItem(ITEM,"village_pillage_leather_horse_armor");
        GameRegistry.addRecipe(new ItemStack(ITEM),"X X","XXX","X X",'X',Items.leather);
        GameRegistry.addRecipe(new RecipeHorseArmorDye151());
        VillagerTradeItems146.register("LEATHER_HORSE_ARMOR",new ItemStack(ITEM));
        registered=true;
    }
}
