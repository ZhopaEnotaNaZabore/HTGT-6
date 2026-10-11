package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillagePoi120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import net.minecraft.world.World;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.item.EntityItem;
import java.lang.reflect.*;
import java.util.ArrayList;
public final class Composter171Fixture {
 static int checks, failures;
 static void check(boolean value,String name){checks++;if(!value){failures++;System.out.println("FAIL "+name);}}
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe u=(sun.misc.Unsafe)Dolphin112Fixture.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  Campfire149Fixture.TestWorld w=(Campfire149Fixture.TestWorld)u.allocateInstance(Campfire149Fixture.TestWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  Dolphin112Fixture.field(World.class,"worldAccesses").set(w,new ArrayList());
  w.provider.worldObj=w;w.spawns=new ArrayList();
  BlockVillagePoi120 b=(BlockVillagePoi120)VillagePillageContent.composter;w.block=b;
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);
  p.inventory.mainInventory[0]=new ItemStack(Items.wheat_seeds,2);w.meta=7;
  check(b.onBlockActivated(w,0,10,0,p,1,0,0,0),"maturing accepts compostable interaction");
  check(w.meta==7&&p.getHeldItem().stackSize==2,"maturing consumes nothing");
  p.inventory.mainInventory[0]=new ItemStack(Items.stick);check(!b.onBlockActivated(w,0,10,0,p,1,0,0,0),"maturing unrelated item passes");
  for(boolean creative:new boolean[]{false,true}){
   p.capabilities.isCreativeMode=creative;p.inventory.clearInventory(null,-1);w.meta=8;w.spawns.clear();
   check(b.onBlockActivated(w,0,10,0,p,1,0,0,0)&&w.meta==0,"ready empty "+creative);
   check(w.spawns.size()==1,"produce drops in world "+creative);
   check(!p.inventory.hasItem(Items.dye),"no direct inventory reward "+creative);
   if(w.spawns.size()==1){EntityItem e=(EntityItem)w.spawns.get(0);
    check(e.getEntityItem().getItem()==Items.dye&&e.getEntityItem().getItemDamage()==15&&e.getEntityItem().stackSize==1,"one bone meal");
    check(e.posX>=.15&&e.posX<=.85&&e.posY>=10.66&&e.posY<=11.36&&e.posZ>=.15&&e.posZ<=.85&&e.delayBeforeCanPickup==10,"block-local randomized drop and pickup delay");
   }
  }
  w.isRemote=true;w.meta=8;w.spawns.clear();check(b.onBlockActivated(w,0,10,0,p,1,0,0,0)&&w.meta==8&&w.spawns.isEmpty(),"client does not extract");
  w.isRemote=false;w.meta=0;p.capabilities.isCreativeMode=false;p.inventory.mainInventory[0]=new ItemStack(Items.wheat_seeds,1);
  b.onBlockActivated(w,0,10,0,p,1,0,0,0);check(w.meta==1&&p.getHeldItem()==null,"last seed fills first level");
  w.meta=0;p.capabilities.isCreativeMode=true;p.inventory.mainInventory[0]=new ItemStack(Items.wheat_seeds,1);
  b.onBlockActivated(w,0,10,0,p,1,0,0,0);check(w.meta==1&&p.getHeldItem().stackSize==1,"creative input preserved");
  System.out.println("Composter171: checks="+checks+", failures="+failures);if(failures>0)throw new AssertionError(failures);
 }
}
