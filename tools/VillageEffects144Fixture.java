package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageEffects120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionMarker;
import net.minecraft.potion.*;
import net.minecraft.world.World;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import java.lang.reflect.*;
import java.util.Arrays;
public final class VillageEffects144Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 public static void main(String[]args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Dolphin112Fixture.field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));w.provider.worldObj=w;
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
  Field registry=Potion.class.getDeclaredField("potionTypes");u.putObject(u.staticFieldBase(registry),u.staticFieldOffset(registry),Arrays.copyOf(Potion.potionTypes,65536));
  int omen=HTGT6Config.BAD_OMEN_POTION_ID,hero=HTGT6Config.HERO_OF_VILLAGE_POTION_ID;
  new PotionMarker(hero,false,0,"occupied");boolean refused=false;try{VillageEffects120.init();}catch(IllegalStateException e){refused=e.getMessage().contains("occupied");}
  check(refused&&Potion.potionTypes[omen]==null,"collision before partial registration");Potion.potionTypes[hero]=null;VillageEffects120.init();Potion first=Potion.potionTypes[omen];VillageEffects120.init();check(first==Potion.potionTypes[omen],"idempotent");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);
  for(int level=1;level<=7;level++){VillageEffects120.captainKilled(p);check(VillageEffects120.badOmenLevel(p)==Math.min(5,level),"captain stack cap");check(p.getActivePotionEffect(Potion.potionTypes[omen]).getDuration()==120000,"omen duration");}
  VillageEffects120.consumeBadOmen(p);check(VillageEffects120.badOmenLevel(p)==0,"raid consumes omen");
  VillageEffects120.rewardHero(p,4);check(VillageEffects120.heroAmplifier(p)==3&&p.getActivePotionEffect(Potion.potionTypes[hero]).getDuration()==48000,"hero amplifier and duration");
  VillageEffects120.captainKilled(p);p.curePotionEffects(new ItemStack(Items.milk_bucket));check(VillageEffects120.badOmenLevel(p)==0&&VillageEffects120.heroAmplifier(p)==-1,"milk removes both");
  p.getEntityData().setInteger("HTGT6VillagePillageBadOmen",3);p.getEntityData().setLong("HTGT6VillagePillageHeroUntil",w.getTotalWorldTime()+5);
  VillageEffects120.migrateLegacy(p);check(VillageEffects120.badOmenLevel(p)==3,"legacy omen level");PotionEffect h=p.getActivePotionEffect(Potion.potionTypes[hero]);check(h.getDuration()==5,"legacy hero only remaining duration");
  check(!p.getEntityData().hasKey("HTGT6VillagePillageBadOmen")&&!p.getEntityData().hasKey("HTGT6VillagePillageHeroUntil"),"legacy keys consumed");
  for(int i=0;i<4;i++)check(h.onUpdate(p),"effect runs through remaining ticks");check(!h.onUpdate(p),"effect expires");
  p.curePotionEffects(new ItemStack(Items.milk_bucket));VillageEffects120.migrateLegacy(p);check(VillageEffects120.badOmenLevel(p)==0&&VillageEffects120.heroAmplifier(p)==-1,"migration never resurrects milk-cleared effects");
  p.getEntityData().setLong("HTGT6VillagePillageHeroUntil",w.getTotalWorldTime()-1);VillageEffects120.migrateLegacy(p);check(VillageEffects120.heroAmplifier(p)==-1,"expired legacy ignored");
  w.isRemote=true;VillageEffects120.captainKilled(p);VillageEffects120.rewardHero(p,5);check(VillageEffects120.badOmenLevel(p)==0&&VillageEffects120.heroAmplifier(p)==-1,"client cannot grant");
  System.out.println("PASS villageEffects144: "+checks);
 }
}
