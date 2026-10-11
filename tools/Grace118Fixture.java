package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.asm.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Aquatic.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionMarker;
import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDolphin120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import net.minecraft.potion.*;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.Arrays;
public final class Grace118Fixture {
 static int checks;
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));world.provider.worldObj=world;
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);Field registry=Potion.class.getDeclaredField("potionTypes");Potion[] original=Potion.potionTypes;unsafe.putObject(unsafe.staticFieldBase(registry),unsafe.staticFieldOffset(registry),Arrays.copyOf(original,65536));
  int id=HTGT6Config.DOLPHINS_GRACE_POTION_ID;new PotionMarker(id,false,0,"occupied");boolean rejected=false;try{AquaticEffects120.init();}catch(IllegalStateException e){rejected=e.getMessage().contains("occupied");}check(rejected,"occupied ID is not replaced");Potion.potionTypes[id]=null;AquaticEffects120.init();Potion effect=Potion.potionTypes[id];AquaticEffects120.init();check(effect==Potion.potionTypes[id],"registration idempotent");for(int i=0;i<original.length;i++)check(original[i]==Potion.potionTypes[i],"existing registry slot "+i);
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(world);p.setPosition(.5,10,.5);p.wet=p.eyes=true;p.setSprinting(true);PlayerSwimming120.updateState(p);world.loadedEntityList.add(p);
  EntityDolphin120 dolphin=new EntityDolphin120(world);dolphin.setPosition(.5,10,.5);Method grant=EntityDolphin120.class.getDeclaredMethod("updateGrace");grant.setAccessible(true);grant.invoke(dolphin);
  check(p.isPotionActive(id)&&p.getActivePotionEffect(effect).getDuration()==100,"dolphin grants 100 ticks");check(!p.isPotionActive(Potion.moveSpeed),"no Speed substitution");
  for(int level=0;level<=3;level++){ItemStack boots=new ItemStack(Items.diamond_boots);if(level>0)boots.addEnchantment(BountifulContent.depthStrider,level);p.inventory.armorInventory[0]=boots;check(HTGT6Transformer.depthDrag120(.8,p)==(double).96F,"grace overrides depth strider "+level);}
  double speed=p.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getAttributeValue();p.curePotionEffects(new ItemStack(Items.milk_bucket));check(!p.isPotionActive(id),"milk removes effect");check(p.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.movementSpeed).getAttributeValue()==speed,"no movement attribute modifier");
  AquaticEffects120.grant(p);PotionEffect active=p.getActivePotionEffect(effect);for(int i=0;i<99;i++)check(active.onUpdate(p),"duration tick "+i);check(!active.onUpdate(p),"expires at 100 ticks");p.removePotionEffect(id);
  p.setSprinting(false);PlayerSwimming120.updateState(p);grant.invoke(dolphin);check(!p.isPotionActive(id),"non-swimmer receives no effect");
  p.wet=p.eyes=true;p.setSprinting(true);PlayerSwimming120.updateState(p);dolphin.setAttackTarget(p);grant.invoke(dolphin);check(!p.isPotionActive(id),"attack target excluded");dolphin.setAttackTarget(null);
  p.setPosition(11,10,0);grant.invoke(dolphin);check(!p.isPotionActive(id),"outside initial 10-block range");p.setPosition(.5,10,.5);grant.invoke(dolphin);check(p.isPotionActive(id),"reacquire nearby swimmer");
  p.removePotionEffect(id);Field remote=World.class.getDeclaredField("isRemote");remote.setAccessible(true);remote.setBoolean(world,true);AquaticEffects120.grant(p);check(!p.isPotionActive(id),"client cannot grant");
  System.out.println("PASS grace118: "+checks+" registration, ID preservation, duration, milk, movement and dolphin eligibility checks");
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
}
