package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageComposter120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.world.World;
import net.minecraft.tileentity.*;
import net.minecraft.inventory.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.lang.reflect.*;
import java.util.ArrayList;
public final class Composter172Fixture {
 static int checks,failures;
 static void check(boolean ok,String name){checks++;if(!ok){failures++;System.out.println("FAIL "+name);}}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  Campfire149Fixture.TestWorld w=(Campfire149Fixture.TestWorld)u.allocateInstance(Campfire149Fixture.TestWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;
  w.block=VillagePillageContent.composter;w.meta=8;
  TileEntityVillageComposter120 c=new TileEntityVillageComposter120();c.setWorldObj(w);c.yCoord=10;w.tile=c;
  TileEntityHopper hopper=new TileEntityHopper();
  for(int i=0;i<5;i++)hopper.setInventorySlotContents(i,new ItemStack(Items.stick,63));
  Method pull=TileEntityHopper.class.getDeclaredMethod("func_145892_a",IHopper.class,IInventory.class,int.class,int.class);pull.setAccessible(true);
  check(!(Boolean)pull.invoke(null,hopper,c,0,0),"incompatible non-full hopper refuses output");
  check(w.meta==8&&c.getStackInSlot(0)!=null,"failed transfer preserves ready produce");
  check(!(Boolean)pull.invoke(null,hopper,c,0,0)&&w.meta==8,"repeated failed transfer preserves produce");
  hopper.setInventorySlotContents(0,null);
  check((Boolean)pull.invoke(null,hopper,c,0,0),"retry with free slot succeeds");
  check(w.meta==0&&hopper.getStackInSlot(0)!=null&&hopper.getStackInSlot(0).getItem()==Items.dye,"success commits exactly one bone meal");
  check(!(Boolean)pull.invoke(null,hopper,c,0,0)&&hopper.getStackInSlot(0)!=null&&hopper.getStackInSlot(0).stackSize==1,"no duplicate on next pull");
  w.meta=8;ItemStack taken=c.decrStackSize(0,1);
  check(taken!=null&&c.getStackInSlot(0)==null&&c.decrStackSize(0,1)==null,"pending extraction cannot be repeated");
  c.setInventorySlotContents(0,taken);check(w.meta==8&&c.getStackInSlot(0)!=null,"explicit rollback restores virtual slot");
  c.markDirty();check(w.meta==8,"markDirty after rollback does not empty");
  for(int side=0;side<6;side++)check(c.canExtractItem(0,new ItemStack(Items.dye,1,15),side)==(side==0),"output side "+side);
  w.meta=0;
  for(int side=0;side<6;side++)check(c.canInsertItem(0,new ItemStack(Items.wheat_seeds),side)==(side==1),"input side "+side);
  check(!c.isItemValidForSlot(0,new ItemStack(Items.wheat_seeds,0)),"empty input rejected");
  check(!c.isItemValidForSlot(0,new ItemStack(Items.wheat_seeds,-1)),"negative input rejected");
  ItemStack remainder=TileEntityHopper.func_145889_a(c,new ItemStack(Items.wheat_seeds,1),1);
  check(remainder==null&&w.meta==1,"real hopper insertion consumes and fills first layer");
  w.meta=0;remainder=TileEntityHopper.func_145889_a(c,new ItemStack(Items.wheat_seeds,4),1);
  check(remainder!=null&&remainder.stackSize==3&&w.meta==1,"hopper inserts only one item from larger stack");
  w.meta=7;remainder=TileEntityHopper.func_145889_a(c,new ItemStack(Items.wheat_seeds,2),1);
  check(remainder!=null&&remainder.stackSize==2&&w.meta==7,"maturing composter rejects hopper input");
  w.meta=8;check(c.decrStackSize(1,1)==null&&c.decrStackSize(0,0)==null&&w.meta==8,"invalid extraction leaves produce");
  c.decrStackSize(0,1);c.markDirty();c.markDirty();check(w.meta==0&&c.getStackInSlot(0)==null,"commit is idempotent");
  w.meta=8;w.isRemote=true;check(c.decrStackSize(0,1)==null&&w.meta==8,"client extraction does not mutate");
  w.meta=0;c.setInventorySlotContents(0,new ItemStack(Items.wheat_seeds));check(w.meta==0,"client insertion does not mutate");
  System.out.println("Composter172: checks="+checks+", failures="+failures);if(failures>0)throw new AssertionError(failures);
 }
}
