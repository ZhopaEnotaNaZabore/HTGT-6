package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemAquaticFishBucket120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;

public final class Aquatic106Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(world,new ArrayList());
  Constructor<?> ctor=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);ctor.setAccessible(true);EntityPlayer player=(EntityPlayer)ctor.newInstance(world);
  Method interact=EntityAquaticFish120.class.getDeclaredMethod("interact",EntityPlayer.class);interact.setAccessible(true);
  Method despawn=EntityAquaticFish120.class.getDeclaredMethod("canDespawn");despawn.setAccessible(true);
  ItemAquaticFishBucket120 bucket=(ItemAquaticFishBucket120)OceanContent.fishBucket;
  for(int species=0;species<4;species++) {
   Arrays.fill(player.inventory.mainInventory,null);player.capabilities.isCreativeMode=false;
   EntityAquaticFish120 fish=ItemAquaticFishBucket120.create(world,species);
   check(fish.getMaxHealth()==3&&fish.getHealth()==3,"three health for each fish");fish.heal(100);check(fish.getHealth()==3,"heal capped at three");
   fish.setHealth(1.5F);fish.setCustomNameTag("Fish "+species);
   player.inventory.mainInventory[0]=new ItemStack(Items.water_bucket);
   check((Boolean)interact.invoke(fish,player)&&fish.isDead,"capture succeeds");
   ItemStack captured=player.getHeldItem();
   check(captured.getItem()==bucket&&captured.getItemDamage()==species,"capture retains species metadata");
   EntityAquaticFish120 restored=ItemAquaticFishBucket120.create(world,captured.getItemDamage());restored.readFromBucket(captured);
   check(restored.getClass()==fish.getClass()&&restored.getHealth()==1.5F&&restored.getCustomNameTag().equals("Fish "+species),"species health and name round trip");
   check(!(Boolean)despawn.invoke(restored),"bucket fish cannot naturally despawn");
   captured.getTagCompound().getCompoundTag("BucketEntityTag").setBoolean("FromBucket",false);restored.readFromBucket(captured);
   check(!(Boolean)despawn.invoke(restored),"legacy false flag cannot undo bucket persistence");
   player.inventory.mainInventory[0]=new ItemStack(Items.water_bucket);
   check(!(Boolean)interact.invoke(fish,player)&&player.getHeldItem().getItem()==Items.water_bucket,"dead fish cannot be captured twice");
   EntityAquaticFish120 stacked=ItemAquaticFishBucket120.create(world,species);player.inventory.mainInventory[0]=new ItemStack(Items.water_bucket,3);
   interact.invoke(stacked,player);check(player.getHeldItem().stackSize==2&&player.inventory.mainInventory[1].getItemDamage()==species,"modded stack of buckets consumes one");
   Arrays.fill(player.inventory.mainInventory,null);player.capabilities.isCreativeMode=true;player.inventory.mainInventory[0]=new ItemStack(Items.water_bucket);
   interact.invoke(ItemAquaticFishBucket120.create(world,species),player);
   check(player.getHeldItem().getItem()==Items.water_bucket&&player.inventory.mainInventory[1].getItemDamage()==species,"creative keeps water bucket");
   EntityAquaticFish120 clientFish=ItemAquaticFishBucket120.create(world,species);world.isRemote=true;
   interact.invoke(clientFish,player);check(!clientFish.isDead&&player.getHeldItem().getItem()==Items.water_bucket,"client capture does not mutate");world.isRemote=false;
  }
  player.capabilities.isCreativeMode=false;
  world.rayHit=new net.minecraft.util.MovingObjectPosition(0,9,0,1,net.minecraft.util.Vec3.createVectorHelper(.5,10,.5));
  for(net.minecraft.block.Block old:new net.minecraft.block.Block[]{Blocks.air,Blocks.flowing_water,Blocks.water,Blocks.tallgrass}) {
   world.cells.clear();world.setBlock(0,10,0,old,old==Blocks.air?0:2,3);world.rejectSpawns=true;
   ItemStack filled=new ItemStack(bucket,1,2);
   check(bucket.onItemRightClick(filled,world,player)==filled,"failed spawn retains filled bucket");
   check(world.getBlock(0,10,0)==old&&world.getBlockMetadata(0,10,0)==(old==Blocks.air?0:2),"failed spawn restores original block and metadata");
  }
  world.rejectSpawns=false;world.cells.clear();
  ItemStack filled=new ItemStack(bucket,1,3);ItemStack empty=bucket.onItemRightClick(filled,world,player);
  check(empty.getItem()==Items.bucket&&world.lastSpawn instanceof EntityAquaticFish120.Tropical&&world.getBlock(0,10,0)==Blocks.water,"successful release water fish and empty bucket");
  NBTTagCompound saved=new NBTTagCompound();world.lastSpawn.writeToNBT(saved);
  check(saved.getBoolean("FromBucket"),"released persistence saved to entity NBT");
  System.out.println("PASS aquatic106: "+checks+" checks for all four fish, capture/release identity, health, persistence, stacked buckets, creative/client and spawn rollback");
 }
 static void check(boolean ok,String text){if(!ok)throw new AssertionError(text);checks++;}
}
