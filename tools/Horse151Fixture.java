package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.LeatherHorseArmor151;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemLeatherHorseArmor151;
import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.inventory.*;import net.minecraft.entity.passive.EntityHorse;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;
import java.util.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;
public final class Horse151Fixture {
 static int n;static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);n++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;
  LeatherHorseArmor151.register();ItemStack leather=new ItemStack(LeatherHorseArmor151.ITEM);EntityHorse horse=new EntityHorse(w);
  check(LeatherHorseHooks151.color(horse)==-1,"transformed entity initializes no armor");check(horse.getTotalArmorValue()==0,"unarmored protection preserved");check(EntityHorse.func_146085_a(leather.getItem()),"real armor slot validator accepts leather");check(!EntityHorse.func_146085_a(Items.leather),"raw leather not armor");
  horse.func_146086_d(leather);check(horse.getTotalArmorValue()==3,"real transformed horse has three armor");check(horse.func_110241_cb()==0,"vanilla texture index remains safe");check(LeatherHorseHooks151.color(horse)==0xA06540,"default color synchronized");ItemLeatherHorseArmor151.setColor(leather,0x123456);horse.func_146086_d(leather);check(LeatherHorseHooks151.color(horse)==0x123456,"custom color synchronized");
  for(Item item:new Item[]{Items.iron_horse_armor,Items.golden_horse_armor,Items.diamond_horse_armor}){horse.func_146086_d(new ItemStack(item));check(LeatherHorseHooks151.color(horse)==-1,"replacing leather clears color");check(horse.getTotalArmorValue()==(item==Items.iron_horse_armor?5:item==Items.golden_horse_armor?7:11),"vanilla armor unchanged");}
  horse.func_146086_d(null);check(horse.getTotalArmorValue()==0,"unequip resets protection");
  AnimalChest chest=(AnimalChest)Dolphin112Fixture.field(EntityHorse.class,"horseChest").get(horse);chest.setInventorySlotContents(1,leather);NBTTagCompound saved=new NBTTagCompound();horse.writeEntityToNBT(saved);EntityHorse restored=new EntityHorse(w);restored.readEntityFromNBT(saved);check(restored.getTotalArmorValue()==3&&LeatherHorseHooks151.color(restored)==0x123456,"actual horse inventory NBT restores leather and color");
  RecipeHorseArmorDye151 recipe=new RecipeHorseArmorDye151();InventoryCrafting inv=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);inv.setInventorySlotContents(0,new ItemStack(LeatherHorseArmor151.ITEM));check(recipe.getCraftingResult(inv)==null,"dye required");
  int[] expected={0x1D1D21,0xB02E26,0x5E7C16,0x835432,0x3C44AA,0x8932B8,0x169C9C,0x9D9D97,0x474F52,0xF38BAA,0x80C71F,0xFED83D,0x3AB3DA,0xC74EBD,0xF9801D,0xF9FFFE};
  for(int i=0;i<16;i++){inv.setInventorySlotContents(1,new ItemStack(Items.dye,1,i));check(ItemLeatherHorseArmor151.color(recipe.getCraftingResult(inv))==expected[i],"official dye RGB "+i);}check(!ItemLeatherHorseArmor151.hasColor(inv.getStackInSlot(0)),"recipe does not mutate input");inv.setInventorySlotContents(2,new ItemStack(LeatherHorseArmor151.ITEM));check(recipe.getCraftingResult(inv)==null,"two armor inputs rejected");inv.setInventorySlotContents(2,new ItemStack(Items.diamond));check(recipe.getCraftingResult(inv)==null,"foreign ingredient rejected");
  Swimming115Fixture.Player player=new Swimming115Fixture.Player(w);w.setBlock(0,10,0,Blocks.cauldron,3,2);w.rejectWrites=true;LeatherHorseArmor151.ITEM.onItemUse(leather,player,w,0,10,0,1,0,0,0);check(ItemLeatherHorseArmor151.hasColor(leather),"denied cauldron update keeps color");w.rejectWrites=false;check(LeatherHorseArmor151.ITEM.onItemUse(leather,player,w,0,10,0,1,0,0,0)&&!ItemLeatherHorseArmor151.hasColor(leather)&&w.getBlockMetadata(0,10,0)==2,"water wash consumes one level");
  check(VillagerTradeItems146.stack("LEATHER_HORSE_ARMOR").getItem()==LeatherHorseArmor151.ITEM,"official trader maps real armor");
  System.out.println("PASS horse151: "+n);
 }
}
