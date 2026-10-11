package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.BannerPatterns145;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.item.crafting.*;import net.minecraft.nbt.*;
import java.lang.reflect.*;
public final class Loom145Fixture {
 static int checks;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 public static void main(String[]args)throws Exception{
  net.minecraft.world.World w=BountifulMechanicsFixture120.initialize();BannerPatterns145.register();BannerPatterns145.register();
  Method result=VillageWorkstationLogic120.class.getDeclaredMethod("result",VillageWorkstationType120.class,ItemStack[].class,int.class);result.setAccessible(true);
  Method consumed=VillageWorkstationLogic120.class.getDeclaredMethod("consumed",VillageWorkstationType120.class,ItemStack[].class);consumed.setAccessible(true);
  Item[] patterns={BannerPatterns145.flower,BannerPatterns145.creeper,BannerPatterns145.skull,BannerPatterns145.mojang,BannerPatterns145.globe};String[] codes={"flo","cre","sku","moj","glb"};
  for(int i=0;i<patterns.length;i++){
   ItemStack[] inputs={new ItemStack(BountifulContent.banner),new ItemStack(Items.dye,1,1),new ItemStack(patterns[i])};ItemStack out=(ItemStack)result.invoke(null,VillageWorkstationType120.LOOM,inputs,0);
   check(out!=null,"pattern produces banner");NBTTagList p=out.getTagCompound().getCompoundTag("BlockEntityTag").getTagList("Patterns",10);check(p.tagCount()==1&&codes[i].equals(p.getCompoundTagAt(0).getString("Pattern")),"correct NBT pattern");check(p.getCompoundTagAt(0).getInteger("Color")==1,"dye metadata color preserved");
   int[] usage=(int[])consumed.invoke(null,VillageWorkstationType120.LOOM,inputs);check(usage[0]==1&&usage[1]==1&&usage[2]==0,"pattern reusable");check(!inputs[0].hasTagCompound(),"input banner not mutated");
  }
  ItemStack[] catalysts={new ItemStack(Blocks.red_flower,1,8),new ItemStack(Items.skull,1,4),new ItemStack(Items.skull,1,1),new ItemStack(Items.golden_apple,1,1)};
  for(int i=0;i<catalysts.length;i++){
   check(result.invoke(null,VillageWorkstationType120.LOOM,new ItemStack[]{new ItemStack(BountifulContent.banner),new ItemStack(Items.dye),catalysts[i]},0)==null,"raw catalyst rejected by loom");
   IRecipe recipe=VillageRecipes132Fixture.find(patterns[i]);ItemStack[] grid=new ItemStack[9];grid[3]=new ItemStack(Items.paper);grid[8]=catalysts[i];check(recipe!=null&&recipe.matches(VillageRecipes132Fixture.grid(grid),w),"official shapeless pattern craft");grid[3]=null;check(!recipe.matches(VillageRecipes132Fixture.grid(grid),w),"paper required");
  }
  check(VillageRecipes132Fixture.find(BannerPatterns145.globe)==null,"globe has no invented crafting recipe");
  System.out.println("PASS loom145: "+checks);
 }
}
