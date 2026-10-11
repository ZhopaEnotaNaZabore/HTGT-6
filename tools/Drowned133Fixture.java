package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import java.lang.reflect.*;
import java.util.*;
public class Drowned133Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(w,new net.minecraft.profiler.Profiler());w.provider.worldObj=w;
  Field rng=Entity.class.getDeclaredField("rand");rng.setAccessible(true);
  Method loot=EntityDrowned.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);loot.setAccessible(true);
  Method rare=EntityDrowned.class.getDeclaredMethod("dropRareDrop",int.class);rare.setAccessible(true);
  Method equipment=EntityDrowned.class.getDeclaredMethod("dropEquipment",boolean.class,int.class);equipment.setAccessible(true);
  for(boolean hit:new boolean[]{false,true})for(final int l:new int[]{0,1,3})for(final float roll:new float[]{0F,.10999F,.11F,.16999F,.17F,.99F}) {
   EntityDrowned d=new EntityDrowned(w);d.captureDrops=true;
   rng.set(d,new Random(){public int nextInt(int bound){return 2%bound;}public float nextFloat(){return roll;}});
   loot.invoke(d,hit,l);int flesh=0,copper=0;
   for(Object obj:d.capturedDrops){ItemStack s=((EntityItem)obj).getEntityItem();if(s.getItem()==Items.rotten_flesh)flesh+=s.stackSize;else if(s.getItem()==Backport120Content.copperIngot)copper+=s.stackSize;else throw new AssertionError("unexpected table item");}
   check(flesh==2+(l>0?Math.round(roll*l):0),"flesh looting distribution");check(copper==(hit&&roll<.11F+l*.02F?1:0),"copper player gate and threshold");
   int before=d.capturedDrops.size();rare.invoke(d,1);check(d.capturedDrops.size()==before,"no zombie rare loot");
  }
  EntityDrowned d=new EntityDrowned(w);d.setCarriesNautilusShell(true);NBTTagCompound tag=new NBTTagCompound();d.writeEntityToNBT(tag);
  EntityDrowned restored=new EntityDrowned(w);restored.readEntityFromNBT(tag);check(restored.carriesNautilusShell(),"shell survives NBT");restored.captureDrops=true;
  equipment.invoke(restored,false,0);int shells=0;for(Object o:restored.capturedDrops)if(((EntityItem)o).getEntityItem().getItem()==OceanContent.nautilusShell)shells++;
  check(shells==1&&!restored.carriesNautilusShell(),"equipped shell guaranteed without player kill");
  restored.capturedDrops.clear();equipment.invoke(restored,true,3);check(restored.capturedDrops.isEmpty(),"shell not duplicated");
  w.isRemote=true;d.captureDrops=true;loot.invoke(d,true,3);check(d.capturedDrops.isEmpty(),"client table does not drop");
  System.out.println("PASS drowned133: "+checks);
 }
}
