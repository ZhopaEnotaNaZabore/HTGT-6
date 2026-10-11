package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import net.minecraft.item.*;import net.minecraft.init.Items;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;import java.lang.reflect.*;import java.util.*;
public class OffhandCrossbow141Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();w.provider.worldObj=w;
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(w,new ArrayList());
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);Field registry=net.minecraft.potion.Potion.class.getDeclaredField("potionTypes");u.putObject(u.staticFieldBase(registry),u.staticFieldOffset(registry),Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536));
  ItemCrossbow item=new ItemCrossbow();cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"offhand141_crossbow");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);p.setPosition(.5,10,.5);
  ItemStack stack=new ItemStack(item);OffhandHooks.setOffhand(p,stack);p.inventory.mainInventory[2]=new ItemStack(Items.arrow,20);
  check(OffhandCrossbowUse120.start(p),"server start");check(PlayerItemUse120.item(p)==null,"no vanilla mainhand timer or fake slot");
  for(int tick=0;tick<25;tick++){check(!OffhandCrossbowUse120.start(p),"duplicate press cannot restart");OffhandCrossbowUse120.tick(p);}
  check(OffhandCrossbowUse120.useTicks(p,stack)==25,"25 authoritative ticks");check(OffhandCrossbowUse120.release(p)&&ItemCrossbow.isCharged(stack),"release completes charge");check(p.inventory.mainInventory[2].stackSize==19,"single ammo consumed");
  check(!OffhandCrossbowUse120.release(p),"duplicate release inactive");
  check(OffhandCrossbowUse120.start(p),"loaded shot on press");
  check(!OffhandCrossbowUse120.start(p),"repeated press cannot fire/restart without release");OffhandCrossbowUse120.release(p);
  for(int mode=0;mode<4;mode++){
   stack=new ItemStack(item);OffhandHooks.setOffhand(p,stack);check(OffhandCrossbowUse120.start(p),"start cancellation case");for(int t=0;t<25;t++)OffhandCrossbowUse120.tick(p);
   if(mode==0)OffhandHooks.setOffhand(p,new ItemStack(item));
   if(mode==1)OffhandCrossbowUse120.cancel(p);
   if(mode==2)p.inventory.mainInventory[0]=new ItemStack(Items.bow);
   if(mode==3)p.isDead=true;
   check(!OffhandCrossbowUse120.release(p)&&!ItemCrossbow.isCharged(stack),"changed stack/cancel/main priority/death cannot charge");
   p.isDead=false;p.inventory.mainInventory[0]=null;
  }
  stack=new ItemStack(item);OffhandHooks.setOffhand(p,stack);OffhandCrossbowUse120.start(p);for(int t=0;t<24;t++)OffhandCrossbowUse120.tick(p);OffhandCrossbowUse120.release(p);check(!ItemCrossbow.isCharged(stack),"early release does not load");
  for(int i=0;i<100;i++){OffhandCrossbowUse120.start(p);OffhandCrossbowUse120.cancel(p);OffhandCrossbowUse120.tick(p);check(OffhandCrossbowUse120.useTicks(p,stack)==-1,"swap cancellation has no residual session");}
  OffhandCrossbowUse120.start(p);for(int t=0;t<25;t++)OffhandCrossbowUse120.tick(p);
  Method clear=OffhandSystem.class.getDeclaredMethod("clearState",net.minecraft.entity.player.EntityPlayer.class);clear.setAccessible(true);clear.invoke(null,p);
  check(!OffhandCrossbowUse120.release(p)&&!ItemCrossbow.isCharged(stack),"logout cleanup cancels without loading");
  net.minecraft.entity.player.EntityPlayerMP networkPlayer=(net.minecraft.entity.player.EntityPlayerMP)u.allocateInstance(net.minecraft.entity.player.EntityPlayerMP.class);
  Field uid=net.minecraft.entity.Entity.class.getDeclaredField("entityUniqueID");uid.setAccessible(true);uid.set(networkPlayer,UUID.randomUUID());
  Field pending=OffhandSystem.class.getDeclaredField("PENDING_USE");pending.setAccessible(true);Map queues=(Map)pending.get(null);
  OffhandSystem.requestUse(networkPlayer,true);OffhandSystem.requestUse(networkPlayer,false);
  java.util.Queue queue=(java.util.Queue)queues.get(networkPlayer.getUniqueID());check(queue.size()==2&&Boolean.TRUE.equals(queue.poll())&&Boolean.FALSE.equals(queue.poll()),"same tick press/release retained in order");
  for(int i=0;i<128;i++)OffhandSystem.requestUse(networkPlayer,true);OffhandSystem.requestUse(networkPlayer,false);
  check(queue.size()==1&&Boolean.FALSE.equals(queue.peek()),"overflow bounded and releases hold");
  clear.invoke(null,networkPlayer);check(!queues.containsKey(networkPlayer.getUniqueID()),"logout clears packet queue");
  w.isRemote=true;check(!OffhandCrossbowUse120.start(p)&&!OffhandCrossbowUse120.release(p),"client cannot mutate server lifecycle");
  for(int t=0;t<26;t++)OffhandCrossbowUse120.clientTick(p,true);check(OffhandCrossbowUse120.useTicks(p,stack.copy())==25,"renderer copied stack sees predicted ticks");check(!ItemCrossbow.isCharged(stack)&&p.inventory.mainInventory[2].stackSize==19,"prediction cannot load or consume");
  OffhandHooks.setOffhand(p,stack.copy());OffhandCrossbowUse120.clientTick(p,true);check(OffhandCrossbowUse120.useTicks(p,stack.copy())==26,"equal sync copy preserves visual timer");
  ItemStack different=stack.copy();different.setItemDamage(1);OffhandHooks.setOffhand(p,different);OffhandCrossbowUse120.clientTick(p,true);check(OffhandCrossbowUse120.useTicks(p,different)==0,"different stack resets visual timer");
  OffhandCrossbowUse120.clientTick(p,false);check(OffhandCrossbowUse120.useTicks(p,stack)==-1,"release clears visual state");
  System.out.println("PASS offhand crossbow141: "+checks+" checks");
 }
}
