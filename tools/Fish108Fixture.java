package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityAquaticFish120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemAquaticFishBucket120;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import java.lang.reflect.*;
import java.util.Random;

public final class Fish108Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  Method drop=EntityAquaticFish120.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);drop.setAccessible(true);
  Field random=Entity.class.getDeclaredField("rand");random.setAccessible(true);
  for(int meta=0;meta<4;meta++)for(boolean burning:new boolean[]{false,true})for(int looting:new int[]{0,1,3,20})for(final float roll:new float[]{0,.049F,.05F,.99F}) {
   EntityAquaticFish120 fish=ItemAquaticFishBucket120.create(world,meta);fish.captureDrops=true;
   if(burning)fish.setFire(10);
   random.set(fish,new Random(){@Override public float nextFloat(){return roll;}});
   drop.invoke(fish,false,looting);
   check(fish.capturedDrops.size()==(roll<.05F?2:1),"bone meal chance threshold");
   ItemStack main=((EntityItem)fish.capturedDrops.get(0)).getEntityItem();
   check(main.stackSize==1&&main.getItemDamage()==fish.species(),"single fish correct species regardless of looting");
   check(main.getItem()==(burning&&fish.species()<2?Items.cooked_fished:Items.fish),"only cod and salmon cook");
   if(roll<.05F){ItemStack bonus=((EntityItem)fish.capturedDrops.get(1)).getEntityItem();check(bonus.getItem()==Items.dye&&bonus.getItemDamage()==15&&bonus.stackSize==1,"bone meal legacy mapping");}
  }
  for(int meta=0;meta<4;meta++) {
   EntityAquaticFish120 fish=ItemAquaticFishBucket120.create(world,meta);
   ItemStack bucket=new ItemStack(Items.water_bucket);bucket.setStackDisplayName("Anvil name");fish.readFromBucket(bucket);
   check(fish.getCustomNameTag().equals("Anvil name"),"display name applied without entity payload");
   NBTTagCompound payload=new NBTTagCompound();payload.setString("CustomName","Old fish name");bucket.getTagCompound().setTag("BucketEntityTag",payload);fish.readFromBucket(bucket);
   check(fish.getCustomNameTag().equals("Anvil name"),"anvil name overrides old payload name");
   for(float health:new float[]{Float.NaN,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY}) {
    fish.setHealth(2);payload.setFloat("Health",health);fish.readFromBucket(bucket);
    check(fish.getHealth()==2,"invalid health cannot poison entity state");
   }
   payload.setString("Health","invalid");fish.readFromBucket(bucket);check(fish.getHealth()==2,"nonnumeric health ignored");
   payload.setFloat("Health",100);fish.readFromBucket(bucket);check(fish.getHealth()==3,"excess health clamped");
   payload.setFloat("Health",1.25F);fish.readFromBucket(bucket);check(fish.getHealth()==1.25F,"valid health retained");
  }
  System.out.println("PASS fish108: "+checks+" loot and bucket checks; species, fire, Looting, 5% boundary, name precedence, finite health");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
