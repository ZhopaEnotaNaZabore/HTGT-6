package com.mod.htgt6.client.inventory112;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;
public final class RecipeClient89Fixture {
 public static void main(String[] args)throws Exception {
  World world=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Constructor<?> pc=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);pc.setAccessible(true);EntityPlayer original=(EntityPlayer)pc.newInstance(world);
  Class<?> uc=Class.forName("sun.misc.Unsafe");Field uf=uc.getDeclaredField("theUnsafe");uf.setAccessible(true);Object u=uf.get(null);Method alloc=uc.getMethod("allocateInstance",Class.class);
  Minecraft mc=(Minecraft)alloc.invoke(u,Minecraft.class);EntityClientPlayerMP player=(EntityClientPlayerMP)alloc.invoke(u,EntityClientPlayerMP.class);
  for(Class<?> c=EntityPlayer.class;c!=Object.class;c=c.getSuperclass())for(Field f:c.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(player,f.get(original));}
  player.inventory=new InventoryPlayer(player);player.openContainer=player.inventoryContainer=new ContainerPlayer(player.inventory,false,player);
  mc.thePlayer=player;Field instance=Minecraft.class.getDeclaredField("theMinecraft");instance.setAccessible(true);instance.set(null,mc);
  List list=CraftingManager.getInstance().getRecipeList();list.clear();
  IRecipe first=new ShapelessRecipes(new ItemStack(Items.apple),Arrays.asList(new ItemStack(Items.stick)));list.add(first);
  String key=RecipeBookRegistry112.byRecipe(first).key;
  RecipeBookClientState112.apply(true,-1,Collections.singletonList(key),null,false);
  RecipeBook112 book=new RecipeBook112(2);book.initialize(mc,null);
  Field filter=RecipeBook112.class.getDeclaredField("craftableOnly");filter.setAccessible(true);filter.setBoolean(book,true);
  Field filtered=RecipeBook112.class.getDeclaredField("filtered");filtered.setAccessible(true);
  player.ticksExisted++;book.refreshContents();check(((List)filtered.get(book)).isEmpty(),"no ingredient hidden");
  player.inventory.mainInventory[0]=new ItemStack(Items.stick);player.ticksExisted++;book.refreshContents();
  check(((List)filtered.get(book)).contains(first),"inventory arrival updates filter");
  player.inventory.mainInventory[0]=null;player.ticksExisted++;book.refreshContents();check(((List)filtered.get(book)).isEmpty(),"inventory removal updates filter");
  ((ContainerPlayer)player.openContainer).craftMatrix.setInventorySlotContents(0,new ItemStack(Items.stick));player.ticksExisted++;book.refreshContents();check(((List)filtered.get(book)).contains(first),"grid ingredient updates filter");
  IRecipe second=new ShapelessRecipes(new ItemStack(Items.bread),Arrays.asList(new ItemStack(Items.stick)));list.set(0,second);
  key=RecipeBookRegistry112.byRecipe(second).key;RecipeBookClientState112.apply(false,-1,Collections.singletonList(key),null,false);
  player.ticksExisted++;book.refreshContents();check(((List)filtered.get(book)).contains(second)&&!((List)filtered.get(book)).contains(first),"open book updates recipe objects");
  player.inventory.mainInventory[0]=new ItemStack(Items.diamond,7);SavedHotbars112.load(0);SavedHotbars112.save(0);
  check(player.inventory.mainInventory[0].stackSize==7,"survival cannot load or save creative preset");
  Field bars=SavedHotbars112.class.getDeclaredField("HOTBARS");bars.setAccessible(true);ItemStack[][] values=(ItemStack[][])bars.get(null);
  values[8][8]=new ItemStack(Items.diamond,23);values[8][8].setStackDisplayName("NBT preset");
  Field storage=SavedHotbars112.class.getDeclaredField("storage");storage.setAccessible(true);storage.set(null,new java.io.File("build/client89/saved-hotbars-test.dat"));
  Method persist=SavedHotbars112.class.getDeclaredMethod("persist");persist.setAccessible(true);persist.invoke(null);
  values[8][8]=null;Method read=SavedHotbars112.class.getDeclaredMethod("load");read.setAccessible(true);read.invoke(null);
  check(values[8][8].stackSize==23&&"NBT preset".equals(values[8][8].getDisplayName()),"last preset slot persisted with NBT");
  System.out.println("PASS client: live inventory/grid filtering, same-size recipe replacement, survival preset guard, preset NBT round trip");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
