package com.mod.htgt6.common.item.htgt6.monocrystalls;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

public class crystalls extends Item

 {

     public static Item itemMonocrystall1;
     public static Item itemMonocrystall2;
     public static Item itemMonocrystall3;
     public static Item itemMonocrystall4;

     public static void Monocrystals () {

         //register
         itemMonocrystall1 = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemMonocrystall1");
         itemMonocrystall2 = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemMonocrystall2");
         itemMonocrystall3 = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemMonocrystall3");
         itemMonocrystall4 = register(new com.mod.htgt6.common.item.htgt6.AtlasItem(), "itemMonocrystall4");

         //name
         itemMonocrystall1.setUnlocalizedName("itemMonocrystall1");
         itemMonocrystall2.setUnlocalizedName("itemMonocrystall2");
         itemMonocrystall3.setUnlocalizedName("itemMonocrystall3");
         itemMonocrystall4.setUnlocalizedName("itemMonocrystall4");
         //texture
         itemMonocrystall1.setTextureName(HTGT6.MOD_ID + ":itemMonocrystall1");
         itemMonocrystall2.setTextureName(HTGT6.MOD_ID + ":itemMonocrystall2");
         itemMonocrystall3.setTextureName(HTGT6.MOD_ID + ":itemMonocrystall3");
         itemMonocrystall4.setTextureName(HTGT6.MOD_ID + ":itemMonocrystall4");

     }
     public static Item register(Item item, String name) {
         GameRegistry.registerItem(item, name);
         return item;
 }
}
