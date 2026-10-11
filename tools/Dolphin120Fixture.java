package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.PlayerSwimming120;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import java.lang.reflect.*;
import java.util.Random;
public final class Dolphin120Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  Dolphin112Fixture.field(net.minecraft.world.World.class,"worldInfo").set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));world.provider.worldObj=world;
  Method drop=EntityDolphin120.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);drop.setAccessible(true);
  Field random=Entity.class.getDeclaredField("rand");random.setAccessible(true);
  // Explicit loot-table boundary cases: base, Looting level, random bonus, expected count.
  float[][] cases={{0,0,0,0},{1,0,0,1},{0,3,0,0},{0,3,.16F,0},{0,3,.17F,1},{1,3,.49F,2},{1,3,.5F,3},{1,3,.999F,4}};
  for(boolean hit:new boolean[]{false,true})for(boolean burning:new boolean[]{false,true})for(final float[] c:cases){
   EntityDolphin120 d=new EntityDolphin120(world);d.captureDrops=true;if(burning)d.setFire(10);
   random.set(d,new Random(){public int nextInt(int bound){return (int)c[0];}public float nextFloat(){return c[2];}});
   drop.invoke(d,hit,(int)c[1]);
   check(d.capturedDrops.size()==(c[3]==0?0:1),"zero loot does not spawn empty item");
   if(c[3]>0){ItemStack stack=d.capturedDrops.get(0).getEntityItem();
    check(stack.stackSize==(int)c[3],"Looting count and rounding");
    check(stack.getItemDamage()==0&&stack.getItem()==(burning?Items.cooked_fished:Items.fish),"cod mapping and smelting");
   }
  }
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(world);p.wet=p.eyes=true;p.setSprinting(true);PlayerSwimming120.updateState(p);
  EntityDolphin120 d=new EntityDolphin120(world);Method aid=EntityDolphin120.class.getDeclaredMethod("canAid",net.minecraft.entity.player.EntityPlayer.class);aid.setAccessible(true);
  d.setRevengeTarget(p);check((Boolean)aid.invoke(d,p),"revenge memory alone does not replace current attack target");
  d.setRevengeTarget(null);d.setAttackTarget(p);check(!(Boolean)aid.invoke(d,p),"attack target excluded without revenge memory");
  System.out.println("PASS dolphin120: "+checks+" checks");
 }
 static void check(boolean v,String n){if(!v)throw new AssertionError(n);checks++;}
}
