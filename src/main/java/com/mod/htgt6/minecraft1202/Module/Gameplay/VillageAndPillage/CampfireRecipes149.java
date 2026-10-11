package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import net.minecraft.init.Items;import net.minecraft.item.Item;import net.minecraft.item.ItemStack;
/** The nine official 1.20.2 campfire recipes, not arbitrary furnace recipes. */
public final class CampfireRecipes149 {
 public static ItemStack result(ItemStack in){
  if(in==null||in.stackSize<1)return null;Item i=in.getItem();if(i==null)return null;
  if(i==Items.potato)return new ItemStack(Items.baked_potato);
  if(i==Items.beef)return new ItemStack(Items.cooked_beef);
  if(i==Items.chicken)return new ItemStack(Items.cooked_chicken);
  if(i==Items.porkchop)return new ItemStack(Items.cooked_porkchop);
  if(i==Items.fish&&(in.getItemDamage()==0||in.getItemDamage()==1))return new ItemStack(Items.cooked_fished,1,in.getItemDamage());
  if(i==BountifulContent.rawMutton)return new ItemStack(BountifulContent.cookedMutton);
  if(i==BountifulContent.rawRabbit)return new ItemStack(BountifulContent.cookedRabbit);
  if(i==Item.getItemFromBlock(OceanContent.kelp))return new ItemStack(OceanContent.driedKelp);
  return null;
 }
 private CampfireRecipes149(){}
}
