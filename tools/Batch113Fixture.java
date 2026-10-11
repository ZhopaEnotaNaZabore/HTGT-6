package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import net.minecraft.world.World;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraftforge.oredict.*;
import java.lang.reflect.*;
import java.util.*;
public final class Batch113Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  field(Class.forName("cpw.mods.fml.relauncher.FMLRelaunchLog"),"side").set(null,cpw.mods.fml.relauncher.Side.CLIENT);
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  field(World.class,"worldInfo").set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  field(net.minecraft.world.WorldProvider.class,"worldObj").set(world.provider,world);
  field(World.class,"worldAccesses").set(world,new ArrayList());field(World.class,"theProfiler").set(world,new net.minecraft.profiler.Profiler());
  EntityDolphin120 dolphin=new EntityDolphin120(world);dolphin.setPosition(.5,10.5,.5);
  for(int i=0;i<2399;i++)dolphin.onEntityUpdate();
  check(dolphin.getMoistness()==1&&dolphin.getHealth()==10,"two-minute dry reserve");
  dolphin.onEntityUpdate();check(dolphin.getMoistness()==0&&dolphin.getHealth()==9,"damage at dry threshold");
  dolphin.onEntityUpdate();check(dolphin.getHealth()==9,"hurt resistance prevents every-tick damage");
  world.setBlock(0,10,0,Blocks.water,0,2);world.setBlock(0,11,0,Blocks.water,0,2);dolphin.onEntityUpdate();
  check(dolphin.getMoistness()==2400&&dolphin.getHealth()==9,"water restores moisture without healing");
  dolphin.setMoistness(731);NBTTagCompound tag=new NBTTagCompound();dolphin.writeEntityToNBT(tag);
  EntityDolphin120 copy=new EntityDolphin120(world);copy.readEntityFromNBT(tag);check(copy.getMoistness()==731,"moisture NBT round trip");
  tag.removeTag("Moistness");copy.readEntityFromNBT(tag);check(copy.getMoistness()==2400,"old save default");
  tag.setInteger("Moistness",-99);copy.readEntityFromNBT(tag);check(copy.getMoistness()==0,"negative moisture bounded");
  tag.setInteger("Moistness",9999);copy.readEntityFromNBT(tag);check(copy.getMoistness()==2400,"excess moisture bounded");
  dolphin.setMoistness(913);copy.getDataWatcher().updateWatchedObjectsFromList(dolphin.getDataWatcher().getAllWatched());check(copy.getMoistness()==913,"watcher replication");
  world.cells.clear();dolphin.onGround=true;dolphin.motionX=dolphin.motionY=dolphin.motionZ=0;dolphin.onEntityUpdate();
  check(dolphin.motionY==.5&&Math.abs(dolphin.motionX)<=.2&&Math.abs(dolphin.motionZ)<=.2&&!dolphin.onGround&&dolphin.isAirBorne,"dry ground flop");
  double y=dolphin.motionY;dolphin.onEntityUpdate();check(dolphin.motionY==y,"no repeat flop while airborne");
  world.isRemote=true;dolphin.setMoistness(51);dolphin.onGround=true;dolphin.onEntityUpdate();check(dolphin.getMoistness()==51&&dolphin.onGround,"client does not simulate moisture or flop");world.isRemote=false;

  Constructor<RecipeBookSystem112> constructor=RecipeBookSystem112.class.getDeclaredConstructor();constructor.setAccessible(true);RecipeBookSystem112 handler=constructor.newInstance();
  check(RecipeBookSystem112.class.getMethod("onOreRegistered",OreDictionary.OreRegisterEvent.class).isAnnotationPresent(cpw.mods.fml.common.eventhandler.SubscribeEvent.class),"Forge subscription declared");
  {
   String ore="fixture113LateOre";IRecipe recipe=new ShapedOreRecipe(new ItemStack(Items.apple),"A",'A',ore);CraftingManager.getInstance().getRecipeList().add(recipe);
   check(RecipeBookRegistry112.byRecipe(recipe).hasUnresolvedIngredients,"initial ore missing");int revision=RecipeBookRegistry112.revision();
   OreDictionary.registerOre(ore,new ItemStack(Items.diamond));
   handler.onOreRegistered(new OreDictionary.OreRegisterEvent(ore,new ItemStack(Items.diamond)));
   check(RecipeBookRegistry112.revision()>revision,"OreRegisterEvent callback invalidates cache");
   RecipeBookRegistry112.Entry entry=RecipeBookRegistry112.byRecipe(recipe);
   check(!entry.hasUnresolvedIngredients&&RecipePlacement112.chooseSources(new ItemStack[]{new ItemStack(Items.diamond)},entry,1)!=null,"new ingredient available without manual invalidation");
   revision=RecipeBookRegistry112.revision();OreDictionary.registerOre(ore,new ItemStack(Items.coal));
   handler.onOreRegistered(new OreDictionary.OreRegisterEvent(ore,new ItemStack(Items.coal)));
   check(RecipeBookRegistry112.revision()>revision&&RecipePlacement112.chooseSources(new ItemStack[]{new ItemStack(Items.coal)},RecipeBookRegistry112.byRecipe(recipe),1)!=null,"additional ore alternative updates cache");
  }
  System.out.println("PASS batch113: "+checks+" moisture, dry damage, NBT, watcher, flop and OreDictionary callback checks");
 }
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void check(boolean value,String name){if(!value)throw new AssertionError(name);checks++;}
}
