package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120.Tropical;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
public final class Tropical111Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  for(int variant=0;variant<12;variant++) {
   world.isRemote=false;Tropical server=new Tropical(world);server.setVariant(variant);
   world.isRemote=true;Tropical client=new Tropical(world);check(client.getVariant()==0,"client awaits authoritative spawn variant");
   client.getDataWatcher().updateWatchedObjectsFromList(server.getDataWatcher().getAllWatched());
   check(client.getVariant()==variant,"spawn watcher preserves variant");
   world.isRemote=false;
   NBTTagCompound saved=new NBTTagCompound();server.writeEntityToNBT(saved);Tropical loaded=new Tropical(world);loaded.readEntityFromNBT(saved);
   check(loaded.getVariant()==variant&&saved.hasKey("Variant",1),"legacy byte round trip");
   ItemStack bucket=new ItemStack(Items.water_bucket);server.writeToBucket(bucket);Tropical released=new Tropical(world);released.readFromBucket(bucket);
   check(released.getVariant()==variant,"bucket variant round trip");
   server.setVariant((variant+1)%12);client.getDataWatcher().updateWatchedObjectsFromList(server.getDataWatcher().getChanged());
   check(client.getVariant()==(variant+1)%12,"later watcher change updates client");
  }
  Tropical fish=new Tropical(world);fish.setVariant(7);ItemStack named=new ItemStack(Items.water_bucket);named.setStackDisplayName("Unspecified variant");fish.readFromBucket(named);
  check(fish.getVariant()==7,"missing payload does not randomize or reset variant");
  NBTTagCompound empty=new NBTTagCompound();named.getTagCompound().setTag("BucketEntityTag",empty);fish.readFromBucket(named);
  check(fish.getVariant()==7,"missing variant key preserves existing selection");
  for(int input:new int[]{-128,-12,-1,0,11,12,127,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
   empty.setInteger("Variant",input);fish.readFromBucket(named);
   check(fish.getVariant()>=0&&fish.getVariant()<12,"malformed variant remains safe for texture array");
   if(input>=-128&&input<=127)check(fish.getVariant()==Math.abs(input)%12,"legacy signed-byte mapping unchanged");
  }
  System.out.println("PASS tropical111: "+checks+" checks for 12 variants, spawn/update watcher, legacy NBT, buckets and malformed input");
 }
 static void check(boolean b,String name){if(!b)throw new AssertionError(name);checks++;}
}
