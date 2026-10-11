package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageLectern120;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import java.lang.reflect.Method;
public final class Lectern176Fixture{
 static int checks,failures;
 static void check(boolean ok,String name){checks++;if(!ok){failures++;System.out.println("FAIL "+name);}}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.initialize();
  Method write=TileEntityVillageLectern120.class.getDeclaredMethod("writeState",NBTTagCompound.class);write.setAccessible(true);
  Method read=TileEntityVillageLectern120.class.getDeclaredMethod("readState",NBTTagCompound.class);read.setAccessible(true);
  TileEntityVillageLectern120 t=new TileEntityVillageLectern120();
  ItemStack book=new ItemStack(Items.written_book);NBTTagCompound data=new NBTTagCompound();NBTTagList pages=new NBTTagList();for(int i=0;i<4;i++)pages.appendTag(new NBTTagString("page "+i));data.setTag("pages",pages);data.setString("author","fixture");book.setTagCompound(data);
  t.setInventorySlotContents(0,book.copy());t.setPage(2);
  check(t.decrStackSize(0,0)==null&&t.getStackInSlot(0)!=null&&t.getPage()==2,"zero extraction preserves book/page");
  t.setInventorySlotContents(0,book.copy());t.setPage(2);
  check(t.decrStackSize(0,-1)==null&&t.getStackInSlot(0)!=null,"negative extraction preserves book");
  t.setInventorySlotContents(0,book.copy());t.setPage(2);
  NBTTagCompound saved=new NBTTagCompound();write.invoke(t,saved);TileEntityVillageLectern120 loaded=new TileEntityVillageLectern120();read.invoke(loaded,saved);
  check(loaded.getPage()==2&&loaded.pageCount()==4&&loaded.getStackInSlot(0).getTagCompound().getString("author").equals("fixture"),"pages and custom NBT round trip");
  check(t.decrStackSize(0,1)!=null&&t.getPage()==0,"normal removal resets page");write.invoke(t,saved);read.invoke(loaded,saved);check(!saved.hasKey("Book")&&loaded.getStackInSlot(0)==null,"reused NBT cannot resurrect removed book");
  check(!t.isItemValidForSlot(1,book)&&!t.isItemValidForSlot(-1,book),"invalid slots reject books");
  for(int count:new int[]{0,-1,64}){
   ItemStack input=book.copy();input.stackSize=count;t.setInventorySlotContents(0,input);check(count>0?t.getStackInSlot(0).stackSize==1:t.getStackInSlot(0)==null,"set count "+count);
   NBTTagCompound raw=new NBTTagCompound();book.writeToNBT(raw);raw.setByte("Count",(byte)count);saved.setTag("Book",raw);saved.setInteger("Page",999);read.invoke(loaded,saved);
   check(count>0?loaded.getStackInSlot(0).stackSize==1&&loaded.getPage()==3:loaded.getStackInSlot(0)==null&&loaded.getPage()==0,"saved count/page "+count);
  }
  check(!t.isItemValidForSlot(0,new ItemStack(Items.written_book,0))&&!t.isItemValidForSlot(0,new ItemStack(Items.stick)),"invalid contents rejected");
  t.setInventorySlotContents(0,book.copy());t.setPage(0);check(t.comparatorLevel()==1,"first page signal");t.setPage(99);check(t.comparatorLevel()==15,"last page signal");
  System.out.println("Lectern176: checks="+checks+", failures="+failures);if(failures>0)throw new AssertionError(failures);
 }
}
