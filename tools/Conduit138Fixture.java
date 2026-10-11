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
public final class Conduit138Fixture {
 static int checks;
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));world.provider.worldObj=world;
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);Field registry=Potion.class.getDeclaredField("potionTypes");Potion[] original=Potion.potionTypes;unsafe.putObject(unsafe.staticFieldBase(registry),unsafe.staticFieldOffset(registry),Arrays.copyOf(original,65536));
  Field controller=cpw.mods.fml.common.Loader.class.getDeclaredField("modController");controller.setAccessible(true);Field owner=cpw.mods.fml.common.LoadController.class.getDeclaredField("activeContainer");owner.setAccessible(true);owner.set(controller.get(cpw.mods.fml.common.Loader.instance()),new cpw.mods.fml.common.DummyModContainer("fixture"));
  int id=HTGT6Config.CONDUIT_POWER_POTION_ID;
  new PotionMarker(id,false,0,"occupied");boolean rejected=false;try{AquaticEffects120.init();}catch(IllegalStateException e){rejected=e.getMessage().contains("occupied");}
  check(rejected,"occupied conduit id rejected");check(Potion.potionTypes[HTGT6Config.DOLPHINS_GRACE_POTION_ID]==null,"no partial registration");Potion.potionTypes[id]=null;
  AquaticEffects120.init();Potion effect=Potion.potionTypes[id];AquaticEffects120.init();check(effect==Potion.potionTypes[id],"idempotent");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(world);p.onGround=true;p.eyes=false;
  AquaticEffects120.grantConduitPower(p);check(AquaticEffects120.hasConduitPower(p),"grant");check(p.getActivePotionEffect(effect).getDuration()==260,"duration");
  check(!p.isPotionActive(Potion.nightVision)&&!p.isPotionActive(Potion.digSpeed)&&!p.isPotionActive(Potion.waterBreathing),"no synthetic vanilla effects");
  check(ConduitPowerHooks120.breathing(p,Potion.waterBreathing.id),"breathing hook");check(!ConduitPowerHooks120.vision(p,Potion.nightVision),"no vision on land");p.eyes=true;check(ConduitPowerHooks120.vision(p,Potion.nightVision),"underwater vision");
  p.addPotionEffect(new PotionEffect(Potion.nightVision.id,500));check(!ConduitPowerHooks120.conduitVisionOnly(p),"real night vision priority");p.removePotionEffect(Potion.nightVision.id);
  p.eyes=false;
  Constructor<AquaticEffects120> ctor=AquaticEffects120.class.getDeclaredConstructor();ctor.setAccessible(true);AquaticEffects120 listener=ctor.newInstance();
  for(int haste=-1;haste<4;haste++)for(int power=0;power<4;power++){
   p.removePotionEffect(Potion.digSpeed.id);p.removePotionEffect(id);if(haste>=0)p.addPotionEffect(new PotionEffect(Potion.digSpeed.id,200,haste));p.addPotionEffect(new PotionEffect(id,260,power));
   float base=p.getBreakSpeed(net.minecraft.init.Blocks.stone,false,0);net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(p,net.minecraft.init.Blocks.stone,0,base,0,0,0);listener.onBreakSpeed(event);float speed=event.newSpeed;check(Math.abs(speed-(1F+.2F*(Math.max(haste,power)+1)))<.00001F,"stronger haste power "+haste+"/"+power);
  }
  p.curePotionEffects(new ItemStack(Items.milk_bucket));check(!AquaticEffects120.hasConduitPower(p),"milk");
  AquaticEffects120.grantConduitPower(p);PotionEffect active=p.getActivePotionEffect(effect);for(int i=0;i<259;i++)check(active.onUpdate(p),"tick "+i);check(!active.onUpdate(p),"expiry");p.removePotionEffect(id);
  world.isRemote=true;AquaticEffects120.grantConduitPower(p);check(!AquaticEffects120.hasConduitPower(p),"client cannot grant");
  System.out.println("PASS conduit138: "+checks+" checks");
 }
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
}
