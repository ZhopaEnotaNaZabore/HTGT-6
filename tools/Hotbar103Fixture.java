import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.client.inventory112.SavedHotbars112;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import java.lang.reflect.*;
import java.io.File;

public final class Hotbar103Fixture {
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.initialize();
  Field bars=SavedHotbars112.class.getDeclaredField("HOTBARS");bars.setAccessible(true);ItemStack[][] values=(ItemStack[][])bars.get(null);
  Field storage=SavedHotbars112.class.getDeclaredField("storage");storage.setAccessible(true);
  File file=new File("build/hotbars103/test.dat");storage.set(null,file);
  Method save=SavedHotbars112.class.getDeclaredMethod("persist");save.setAccessible(true);
  Method load=SavedHotbars112.class.getDeclaredMethod("load");load.setAccessible(true);
  values[8][8]=new ItemStack(Items.diamond_pickaxe,1,137);values[8][8].setStackDisplayName("Modded NBT");
  values[8][8].getTagCompound().setLong("CustomEnergy",987654321L);
  save.invoke(null);
  NBTTagCompound root=CompressedStreamTools.read(file);NBTTagList list=root.getTagList("Hotbars",10);
  NBTTagCompound entry=list.getCompoundTagAt(0);
  check(entry.getString("RegistryName").equals("minecraft:diamond_pickaxe"),"named serialization");
  entry.setShort("id",(short)Item.getIdFromItem(Items.stick));CompressedStreamTools.safeWrite(root,file);
  values[0][0]=new ItemStack(Items.apple);load.invoke(null);
  ItemStack restored=values[8][8];
  check(restored.getItem()==Items.diamond_pickaxe&&restored.getItemDamage()==137,"name overrides conflicting numeric ID");
  check(restored.getTagCompound().getLong("CustomEnergy")==987654321L&&restored.getDisplayName().equals("Modded NBT"),"custom NBT preserved");
  check(values[0][0]==null,"loading clears stale slots");
  entry.setString("RegistryName","missing_mod:missing_tool");CompressedStreamTools.safeWrite(root,file);load.invoke(null);
  check(values[8][8]==null,"missing name never falls back to wrong numeric item");
  values[1][1]=new ItemStack(Items.coal,12);save.invoke(null);
  list=CompressedStreamTools.read(file).getTagList("Hotbars",10);boolean retained=false;
  for(int i=0;i<list.tagCount();i++){NBTTagCompound e=list.getCompoundTagAt(i);if(e.getByte("Preset")==8){retained=e.getString("RegistryName").equals("missing_mod:missing_tool")&&e.getCompoundTag("tag").getLong("CustomEnergy")==987654321L;}}
  check(retained,"unavailable mod item retained across other saves");
  Item returnedItem=new Item();Method raw=Item.itemRegistry.getClass().getDeclaredMethod("addObjectRaw",int.class,String.class,Object.class);raw.setAccessible(true);raw.invoke(Item.itemRegistry,30001,"missing_mod:missing_tool",returnedItem);
  load.invoke(null);check(values[8][8].getItem()==returnedItem&&values[8][8].getTagCompound().getLong("CustomEnergy")==987654321L,"returning mod restores retained item");
  root=new NBTTagCompound();list=new NBTTagList();entry=new NBTTagCompound();new ItemStack(Items.iron_pickaxe,1,92).writeToNBT(entry);entry.setByte("Preset",(byte)2);entry.setByte("Slot",(byte)4);list.appendTag(entry);root.setTag("Hotbars",list);
  CompressedStreamTools.safeWrite(root,file);load.invoke(null);check(values[2][4].getItem()==Items.iron_pickaxe&&values[2][4].getItemDamage()==92,"legacy format readable");
  save.invoke(null);check(CompressedStreamTools.read(file).getTagList("Hotbars",10).getCompoundTagAt(0).hasKey("RegistryName",8),"legacy migrates on save");
  System.out.println("PASS hotbars: stable names, conflicting IDs, damage/custom NBT, stale slots, missing mods retained, legacy read/migration");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
