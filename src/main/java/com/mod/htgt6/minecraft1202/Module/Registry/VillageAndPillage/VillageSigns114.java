package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Sign;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120Sign;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;

/** Oak keeps its existing registry identity; the other five woods get their own pairs. */
public final class VillageSigns114 {
    public static final String[] WOODS={"spruce","birch","jungle","acacia","dark_oak"};
    public static final BlockBackport120Sign[] STANDING=new BlockBackport120Sign[5],WALL=new BlockBackport120Sign[5];
    public static final Item[] ITEMS=new Item[5];
    static{for(int i=0;i<5;i++){
        STANDING[i]=new BlockBackport120Sign(WOODS[i]+"_sign",true);
        WALL[i]=new BlockBackport120Sign(WOODS[i]+"_wall_sign",false);
        ITEMS[i]=new ItemBackport120Sign(WOODS[i]+"_sign",STANDING[i],WALL[i]);
        STANDING[i].setSignItem(ITEMS[i]);WALL[i].setSignItem(ITEMS[i]);
    }}
    public static int index(Block block){for(int i=0;i<5;i++)if(block==STANDING[i]||block==WALL[i])return i;return -1;}
    public static boolean standing(Block block){for(Block b:STANDING)if(b==block)return true;return false;}
    public static Item item(String name){for(int i=0;i<5;i++)if(name.equals(WOODS[i]+"_sign"))return ITEMS[i];return null;}
    public static void register(){for(int i=0;i<5;i++){
        GameRegistry.registerBlock(STANDING[i],ItemBlockBackport120Technical.class,"backport_1_14_"+WOODS[i]+"_standing_sign");
        GameRegistry.registerBlock(WALL[i],ItemBlockBackport120Technical.class,"backport_1_14_"+WOODS[i]+"_wall_sign");
        GameRegistry.registerItem(ITEMS[i],"backport_1_14_"+WOODS[i]+"_sign");
        GameRegistry.addRecipe(new ItemStack(ITEMS[i],3),"PPP","PPP"," S ",'P',new ItemStack(Blocks.planks,1,i+1),'S',net.minecraft.init.Items.stick);
        // The 1.7 oak recipe accepts every plank metadata. Specific woods must win.
        java.util.List recipes=net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
        recipes.add(0,recipes.remove(recipes.size()-1));
        net.minecraftforge.oredict.OreDictionary.registerOre("signWood",ITEMS[i]);
    }}
    private VillageSigns114(){}
}
