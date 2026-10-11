package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.*;
import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.nbt.*;import net.minecraft.inventory.Slot;import net.minecraft.world.World;import net.minecraft.world.storage.*;
import java.lang.reflect.*;import java.util.*;
public final class Cartography144Fixture {
 static int checks;static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static ItemStack firstMap(Swimming115Fixture.Player p){for(ItemStack s:p.inventory.mainInventory)if(s!=null&&s.getItem()==Items.filled_map)return s;return null;}
 static ContainerVillageWorkstation120 container(Swimming115Fixture.Player p,World w,ItemStack map,ItemStack reagent){ContainerVillageWorkstation120 c=new ContainerVillageWorkstation120(p.inventory,VillageWorkstationType120.CARTOGRAPHY,w,0,0,0);((Slot)c.inventorySlots.get(0)).putStack(map);((Slot)c.inventorySlots.get(1)).putStack(reagent);return c;}
 public static void main(String[]args)throws Exception{
  World w=BountifulMechanicsFixture120.initialize();field(World.class,"worldInfo").set(w,new WorldInfo(new NBTTagCompound()));field(World.class,"mapStorage").set(w,new MapStorage(null));w.provider.worldObj=w;
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);
  Method center=ContainerVillageWorkstation120.class.getDeclaredMethod("scaledMapCenter",int.class,int.class);center.setAccessible(true);
  int[] coords={0,63,64,191,192,-64,-65,-192,-193,30000000,-30000000};for(int scale=1;scale<=4;scale++)for(int x:coords){int size=128<<scale;int expected=Math.floorDiv(x+64,size)*size+size/2-64;check((Integer)center.invoke(null,x,scale)==expected,"modern map grid "+x+" scale "+scale);}
  MapData old=new MapData("map_42");old.scale=0;old.xCenter=old.zCenter=0;old.colors[10]=7;w.setItemData("map_42",old);ItemStack source=new ItemStack(Items.filled_map,1,42);
  ContainerVillageWorkstation120 c=container(p,w,source.copy(),new ItemStack(Items.paper));check(c.transferStackInSlot(p,2)!=null,"shift scale succeeds");ItemStack scaled=firstMap(p);check(scaled!=null&&scaled.getItemDamage()!=42,"inventory receives final new map ID");MapData data=Items.filled_map.getMapData(scaled,w);check(data.scale==1&&data.xCenter==64&&data.zCenter==64,"scaled saved data aligned");check(!scaled.getTagCompound().hasKey("map_is_scaling")&&data.colors[10]==0,"scaling marker removed and survey reset");check(old.scale==0&&old.colors[10]==7,"source survey unchanged");
  Arrays.fill(p.inventory.mainInventory,null);c=container(p,w,source.copy(),new ItemStack(Blocks.glass_pane));check(c.transferStackInSlot(p,2)!=null,"shift lock succeeds");ItemStack locked=firstMap(p);int lockedId=locked.getItemDamage();check(lockedId!=42&&locked.getTagCompound().getBoolean("map_is_locked"),"locked output final identity and flag");check(Items.filled_map.getMapData(locked,w).colors[10]==7,"locked copy retains survey");
  ItemStack toClone=locked.copy();Arrays.fill(p.inventory.mainInventory,null);c=container(p,w,toClone,new ItemStack(Items.map));check(c.transferStackInSlot(p,2)!=null,"locked map can be cloned");ItemStack cloned=firstMap(p);check(cloned.stackSize==2&&cloned.getItemDamage()==lockedId,"cloning locked maps preserves common ID");
  Arrays.fill(p.inventory.mainInventory,new ItemStack(Items.stick,64));c=container(p,w,source.copy(),new ItemStack(Items.paper));check(c.transferStackInSlot(p,2)==null,"full inventory rejects shift output");check(((Slot)c.inventorySlots.get(0)).getStack().getItemDamage()==42&&((Slot)c.inventorySlots.get(1)).getStack().stackSize==1,"failed transfer consumes nothing");
  Method recipes=VillageWorkstationLogic120.class.getDeclaredMethod("stoneRecipes",ItemStack.class);recipes.setAccessible(true);List<ItemStack> stone=(List<ItemStack>)recipes.invoke(null,new ItemStack(Blocks.nether_brick));check(stone.size()==2,"only nether brick stairs and slabs");for(ItemStack s:stone)check(s.getItem()!=Item.getItemFromBlock(Blocks.nether_brick_fence),"no fabricated fence recipe");
  System.out.println("PASS cartography144: "+checks);
 }
}
