package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.Pufferfish;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;

public final class Puffer107Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  field(World.class,"worldInfo").set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  field(net.minecraft.world.WorldProvider.class,"worldObj").set(world.provider,world);
  field(World.class,"worldAccesses").set(world,new ArrayList());
  field(World.class,"theProfiler").set(world,new net.minecraft.profiler.Profiler());
  field(World.class,"worldScoreboard").set(world,new net.minecraft.scoreboard.Scoreboard());
  Pufferfish fish=new Pufferfish(world);fish.setPosition(0,10,0);
  EntityCow cow=new EntityCow(world);cow.setPosition(1.5,10,0);world.loadedEntityList.add(cow);
  Method tick=Pufferfish.class.getDeclaredMethod("tickPuff");tick.setAccessible(true);
  check(fish.getPuffState()==0&&near(fish.width,.35F),"initial small dimensions");
  tick.invoke(fish);check(fish.getPuffState()==1&&near(fish.width,.49F),"land mob triggers medium state");
  for(int i=1;i<40;i++)tick.invoke(fish);
  check(fish.getPuffState()==1,"not full during first forty danger ticks");tick.invoke(fish);
  check(fish.getPuffState()==2&&near(fish.width,.7F),"full after counter exceeds forty");
  check(cow.getHealth()==cow.getMaxHealth(),"nearby threat outside contact is not hurt");
  world.loadedEntityList.clear();for(int i=0;i<61;i++)tick.invoke(fish);
  check(fish.getPuffState()==2,"full retained while deflate timer <=60");tick.invoke(fish);
  check(fish.getPuffState()==1,"medium after timer exceeds sixty");
  for(int i=62;i<101;i++)tick.invoke(fish);check(fish.getPuffState()==1,"medium retained until timer exceeds100");tick.invoke(fish);
  check(fish.getPuffState()==0,"small after prolonged calm");
  EntityAquaticFish120.Cod cod=new EntityAquaticFish120.Cod(world);cod.setPosition(0,10,0);world.loadedEntityList.add(cod);
  tick.invoke(fish);check(fish.getPuffState()==0,"aquatic fish not scary");world.loadedEntityList.clear();
  Constructor<?> pc=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);pc.setAccessible(true);EntityPlayer player=(EntityPlayer)pc.newInstance(world);player.setPosition(1,10,0);player.capabilities.isCreativeMode=true;world.loadedEntityList.add(player);
  tick.invoke(fish);check(fish.getPuffState()==0,"creative player not scary");player.capabilities.isCreativeMode=false;
  tick.invoke(fish);check(fish.getPuffState()==1,"survival player scary");
  fish.onCollideWithPlayer(player);check(player.getHealth()==18&&player.getActivePotionEffect(Potion.poison).getDuration()==60,"medium contact damage and poison");
  fish.onCollideWithPlayer(player);check(player.getHealth()==18,"hurt resistance prevents repeated damage");
  player.clearActivePotions();player.hurtResistantTime=0;fish.setPuffState(2);fish.onCollideWithPlayer(player);
  check(player.getHealth()==15&&player.getActivePotionEffect(Potion.poison).getDuration()==120,"full contact damage and poison");
  player.clearActivePotions();player.hurtResistantTime=0;world.isRemote=true;fish.onCollideWithPlayer(player);tick.invoke(fish);
  check(player.getHealth()==15&&!player.isPotionActive(Potion.poison)&&fish.getPuffState()==2,"client does not sting or simulate puff");world.isRemote=false;
  player.capabilities.isCreativeMode=true;fish.onCollideWithPlayer(player);check(player.getHealth()==15&&!player.isPotionActive(Potion.poison),"creative contact safe");
  world.loadedEntityList.clear();cow.setPosition(0,10,0);world.loadedEntityList.add(cow);tick.invoke(fish);
  check(cow.getHealth()==7&&cow.getActivePotionEffect(Potion.poison).getDuration()==120,"mob within point3 gets damage and poison");
  Pufferfish mirror=new Pufferfish(world);
  mirror.getDataWatcher().updateWatchedObjectsFromList(fish.getDataWatcher().getAllWatched());
  check(mirror.getPuffState()==2&&near(mirror.width,.7F),"watcher replication refreshes client dimensions");
  fish.setPuffState(0);mirror.getDataWatcher().updateWatchedObjectsFromList(fish.getDataWatcher().getAllWatched());
  check(mirror.getPuffState()==0&&near(mirror.width,.35F),"watcher shrink");
  for(int state:new int[]{-8,0,1,2,99}) {
   NBTTagCompound tag=new NBTTagCompound();fish.writeEntityToNBT(tag);tag.setByte("PuffState",(byte)state);mirror.readEntityFromNBT(tag);
   check(mirror.getPuffState()==Math.max(0,Math.min(2,state)),"legacy byte NBT and bounds");
  }
  fish.setPuffState(2);fish.setDead();cow.hurtResistantTime=0;cow.clearActivePotions();tick.invoke(fish);
  check(cow.getHealth()==7&&!cow.isPotionActive(Potion.poison),"dead fish cannot sting");
  System.out.println("PASS puffer107: "+checks+" checks, timers, threats, contact poison, authority, watcher dimensions and legacy NBT");
 }
 static Field field(Class<?> type,String name)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
 static boolean near(float a,float b){return Math.abs(a-b)<.0001F;}
 static void check(boolean b,String name){if(!b)throw new AssertionError(name);checks++;}
}
