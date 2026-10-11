package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import java.util.*;
public final class VillageRecipes132Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  GameRegistry.registerItem(ExplorationContent112.ironNugget,"fixture_iron_nugget");
  VillagePillageContent.registerModernIngredientRecipes();
  VillagePillageContent.register();
  VillagePillageContent.registerModernIngredientRecipes();
  check(find(Item.getItemFromBlock(VillagePillageContent.blastFurnace))==null,"blast furnace deferred until smooth stone registered");
  GameRegistry.registerBlock(EtFVanillaGapContent120.smoothStone,"fixture_smooth_stone");
  VillagePillageContent.registerModernIngredientRecipes();
  VillagePillageContent.registerModernIngredientRecipes();
  ItemStack[] cross={s(Items.stick),s(Items.iron_ingot),s(Items.stick),s(Items.string),new ItemStack(Blocks.tripwire_hook),s(Items.string),null,s(Items.stick),null};
  ItemStack[] lantern=new ItemStack[9];Arrays.fill(lantern,s(ExplorationContent112.ironNugget));lantern[4]=new ItemStack(Blocks.torch);
  ItemStack[] furnace={s(Items.iron_ingot),s(Items.iron_ingot),s(Items.iron_ingot),s(Items.iron_ingot),new ItemStack(Blocks.furnace),s(Items.iron_ingot),new ItemStack(EtFVanillaGapContent120.smoothStone),new ItemStack(EtFVanillaGapContent120.smoothStone),new ItemStack(EtFVanillaGapContent120.smoothStone)};
  verify(find(VillagePillageContent.crossbow),cross,world);
  verify(find(Item.getItemFromBlock(VillagePillageContent.lantern)),lantern,world);
  verify(find(Item.getItemFromBlock(VillagePillageContent.blastFurnace)),furnace,world);
  ItemStack[] oldCross=cross.clone();for(int i=0;i<9;i++)if(oldCross[i]!=null){if(oldCross[i].getItem()==Items.string)oldCross[i]=s(Items.stick);else if(oldCross[i].getItem()==Items.stick)oldCross[i]=s(Items.string);}
  check(!find(VillagePillageContent.crossbow).matches(grid(oldCross),world),"reject swapped string/sticks");
  ItemStack[] oldLantern=lantern.clone();for(int i=0;i<9;i++)if(i!=4)oldLantern[i]=s(Items.iron_ingot);
  check(!find(Item.getItemFromBlock(VillagePillageContent.lantern)).matches(grid(oldLantern),world),"reject eight ingots");
  ItemStack[] oldFurnace=furnace.clone();for(int i=6;i<9;i++)oldFurnace[i]=new ItemStack(Blocks.stone);
  check(!find(Item.getItemFromBlock(VillagePillageContent.blastFurnace)).matches(grid(oldFurnace),world),"reject unprocessed stone");
  System.out.println("PASS village recipes132: "+checks+" checks");
 }
 static void verify(IRecipe recipe,ItemStack[] expected,net.minecraft.world.World world) {
  check(recipe!=null,"recipe present");InventoryCrafting inventory=grid(expected);
  check(recipe.matches(inventory,world),"official grid matches");
  check(recipe.getCraftingResult(inventory).stackSize==1,"one output");
  for(int i=0;i<9;i++)if(expected[i]!=null){ItemStack[] missing=expected.clone();missing[i]=null;check(!recipe.matches(grid(missing),world),"required occupied slot "+i);}
 }
 static IRecipe find(Item item){IRecipe found=null;for(Object o:CraftingManager.getInstance().getRecipeList()){IRecipe r=(IRecipe)o;if(r.getRecipeOutput()!=null&&r.getRecipeOutput().getItem()==item){check(found==null,"unique recipe");found=r;}}return found;}
 static InventoryCrafting grid(ItemStack[] values){InventoryCrafting g=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);for(int i=0;i<9;i++)g.setInventorySlotContents(i,values[i]==null?null:values[i].copy());return g;}
 static ItemStack s(Item i){return new ItemStack(i);}
 static void check(boolean v,String message){checks++;if(!v)throw new AssertionError(message);}
}
