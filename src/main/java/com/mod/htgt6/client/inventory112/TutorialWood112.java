package com.mod.htgt6.client.inventory112;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Modpack equivalent of modern logs/planks tags, preserving metadata and wildcard entries. */
public final class TutorialWood112 {
    public static boolean matches(ItemStack stack, boolean planks) {
        if(stack==null||stack.stackSize<=0)return false;
        Item item=stack.getItem();
        if(planks ? item==Item.getItemFromBlock(Blocks.planks)
                : item==Item.getItemFromBlock(Blocks.log)||item==Item.getItemFromBlock(Blocks.log2))return true;
        for(int id:OreDictionary.getOreIDs(stack)) {
            String name=OreDictionary.getOreName(id);
            if(planks ? name.equals("plankWood")||name.equals("plankAnyWood")
                    : name.equals("logWood")||name.equals("woodLog")||name.equals("logAnyWood")
                    ||name.equals("logAny")||name.equals("LogAny"))return true;
        }
        return false;
    }
    private TutorialWood112() { }
}
