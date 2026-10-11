package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageFurnaceRecipes143;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.*;
import net.minecraft.block.Block;import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.item.crafting.FurnaceRecipes;import java.lang.reflect.*;
public final class Blast146Fixture {
 static int n;static void check(boolean b,String s){if(!b)throw new AssertionError(s);n++;}
 public static void main(String[] args)throws Exception{
  Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.CLIENT);
  BountifulMechanicsFixture120.initialize();
  Block[] modern={Backport120Content.copperOre,Backport120Content.deepslateCopperOre,Backport120Content.deepslateCoalOre,Backport120Content.deepslateIronOre,Backport120Content.deepslateGoldOre,Backport120Content.deepslateRedstoneOre,Backport120Content.deepslateEmeraldOre,Backport120Content.deepslateLapisOre,Backport120Content.deepslateDiamondOre,EtFVanillaGapContent120.netherGoldOre,EtFVanillaGapContent120.ancientDebris};
  int old=FurnaceRecipes.smelting().getSmeltingList().size();VillageFurnaceRecipes143.registerOreSmelting();check(old==FurnaceRecipes.smelting().getSmeltingList().size(),"unregistered blocks do not invent item recipes");
  for(int i=0;i<modern.length;i++)cpw.mods.fml.common.registry.GameRegistry.registerBlock(modern[i],"blast146_"+i);
  VillageFurnaceRecipes143.registerOreSmelting();int size=FurnaceRecipes.smelting().getSmeltingList().size();VillageFurnaceRecipes143.registerOreSmelting();check(size==FurnaceRecipes.smelting().getSmeltingList().size(),"idempotent");
  Block[] missing={Backport120Content.deepslateCoalOre,Backport120Content.deepslateDiamondOre,Backport120Content.deepslateEmeraldOre,Backport120Content.deepslateLapisOre,Backport120Content.deepslateRedstoneOre,EtFVanillaGapContent120.netherGoldOre};
  Item[] result={Items.coal,Items.diamond,Items.emerald,Items.dye,Items.redstone,Items.gold_ingot};
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
  VillageFurnace143Fixture.FurnaceWorld world=(VillageFurnace143Fixture.FurnaceWorld)u.allocateInstance(VillageFurnace143Fixture.FurnaceWorld.class);
  for(int i=0;i<missing.length;i++){
   ItemStack out=FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(missing[i]));check(out!=null&&out.stackSize==1&&out.getItem()==result[i]&&out.getItemDamage()==(i==3?4:0),"exact modern result");
   TileEntityVillageFurnace120 f=(TileEntityVillageFurnace120)new BlockVillageFurnace120("fixture","fixture",false).createNewTileEntity(world,0);f.setWorldObj(world);f.setInventorySlotContents(0,new ItemStack(missing[i]));f.setInventorySlotContents(1,new ItemStack(Items.coal));for(int t=0;t<100;t++)f.updateEntity();check(f.getStackInSlot(2)!=null&&f.getStackInSlot(2).getItem()==result[i],"real 100tick blast");
  }
  for(Block b:new Block[]{Blocks.coal_ore,Blocks.diamond_ore,Blocks.emerald_ore,Blocks.redstone_ore,Blocks.lapis_ore,Blocks.quartz_ore,Blocks.iron_ore,Blocks.gold_ore})check(VillageFurnaceRecipes143.isBlastable(new ItemStack(b)),"vanilla ore accepted");
  for(Block b:modern)check(VillageFurnaceRecipes143.isBlastable(new ItemStack(b)),"ported ore accepted");
  check(!VillageFurnaceRecipes143.isBlastable(new ItemStack(Blocks.cobblestone)),"stone excluded");check(!VillageFurnaceRecipes143.isBlastable(new ItemStack(Items.diamond_sword)),"diamond equipment excluded");
  ItemStack key=new ItemStack(Backport120Content.deepslateCoalOre);FurnaceRecipes.smelting().getSmeltingList().clear();FurnaceRecipes.smelting().func_151394_a(key,new ItemStack(Items.apple),.3F);VillageFurnaceRecipes143.registerOreSmelting();check(FurnaceRecipes.smelting().getSmeltingResult(key).getItem()==Items.apple,"pack recipe preserved");
  System.out.println("PASS blast146: "+n);
 }
}
