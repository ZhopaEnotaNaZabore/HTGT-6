package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.InventoryPlayer112;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import java.util.*;
import java.lang.reflect.*;
public final class Crossbow139Fixture {
 static int checks;
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static class Inventory extends InventoryPlayer implements InventoryPlayer112 {
  ItemStack[] hand=new ItemStack[1]; List<ItemStack[]> all;
  Inventory(EntityPlayer p){super(p);}
  public ItemStack[] htgt6$getOffHandInventory(){return hand;} public void htgt6$setOffHandInventory(ItemStack[] v){hand=v;}
  public List<ItemStack[]> htgt6$getAllInventories(){return all;} public void htgt6$setAllInventories(List<ItemStack[]> v){all=v;}
 }
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));w.provider.worldObj=w;
  Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(w,new ArrayList());
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);Field registry=net.minecraft.potion.Potion.class.getDeclaredField("potionTypes");unsafe.putObject(unsafe.staticFieldBase(registry),unsafe.staticFieldOffset(registry),Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536));
  ItemCrossbow item=new ItemCrossbow();
  cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"fixture_crossbow139");
  cpw.mods.fml.common.registry.GameRegistry.registerItem(CombatContent.tippedArrow,"fixture_tipped139");
  cpw.mods.fml.common.registry.GameRegistry.registerItem(CombatContent.spectralArrow,"fixture_spectral139");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Inventory(p);p.setPosition(.5,10,.5);
  ItemStack crossbow=new ItemStack(item);p.inventory.mainInventory[0]=crossbow;p.inventory.currentItem=0;
  check(!ItemBow.class.isAssignableFrom(item.getClass()),"independent of bow");check(item.getMaxDamage()==465&&item.getItemStackLimit()==1,"durability and stack size");check(item.getItemUseAction(crossbow)==EnumAction.none,"no legacy bow pose");
  item.onItemRightClick(crossbow,w,p);check(p.getItemInUse()==null,"no ammo cannot charge");
  p.inventory.mainInventory[2]=new ItemStack(Items.arrow,3);
  item.onItemRightClick(crossbow,w,p);check(p.getItemInUse()==crossbow,"charge begins");
  item.onPlayerStoppedUsing(crossbow,w,p,72000-24);check(!ItemCrossbow.isCharged(crossbow)&&p.inventory.mainInventory[2].stackSize==3,"early release consumes nothing");
  item.onPlayerStoppedUsing(crossbow,w,p,72000-25);check(ItemCrossbow.isCharged(crossbow)&&p.inventory.mainInventory[2].stackSize==2,"25 ticks loads one arrow");check(w.lastSpawn==null,"loading does not fire");
  item.onPlayerStoppedUsing(crossbow,w,p,72000-50);check(p.inventory.mainInventory[2].stackSize==2,"duplicate release cannot reload");
  crossbow.setStackDisplayName("kept name");ItemStack restored=ItemStack.loadItemStackFromNBT(crossbow.writeToNBT(new NBTTagCompound()));check(ItemCrossbow.isCharged(restored)&&ItemCrossbow.loadedProjectile(restored).getItem()==Items.arrow,"charged NBT roundtrip");
  w.rejectSpawns=true;check(!ItemCrossbow.fire(crossbow,w,p,null)&&ItemCrossbow.isCharged(crossbow)&&crossbow.getItemDamage()==0,"rejected spawn preserves charge and durability");
  w.rejectSpawns=false;check(ItemCrossbow.fire(crossbow,w,p,null),"accepted shot");EntityCombatArrow arrow=(EntityCombatArrow)w.lastSpawn;
  check(!ItemCrossbow.isCharged(crossbow)&&crossbow.getItemDamage()==1,"accepted shot clears charge and damages once");check(crossbow.getDisplayName().equals("kept name"),"unrelated NBT preserved");
  check(arrow.canBePickedUp==1&&arrow.getIsCritical()&&arrow.getDamage()==2D,"player arrow semantics");check(!ItemCrossbow.fire(crossbow,w,p,null),"no second free shot");
  ItemStack tipped=new ItemStack(CombatContent.tippedArrow,2,8194);NBTTagCompound tag=new NBTTagCompound();tag.setInteger("CustomPotionColor",123456);tipped.setTagCompound(tag);OffhandHooks.setOffhand(p,tipped);
  check(ItemCrossbow.load(p,crossbow)&&tipped.stackSize==1&&p.inventory.mainInventory[2].stackSize==2,"offhand ammo priority");
  tipped.getTagCompound().setInteger("CustomPotionColor",111);check(ItemCrossbow.loadedProjectile(crossbow).getTagCompound().getInteger("CustomPotionColor")==123456,"stored NBT is independent copy");
  check(ItemCrossbow.fire(crossbow,w,p,null),"tipped shot");arrow=(EntityCombatArrow)w.lastSpawn;check(arrow.getCombatType()==EntityCombatArrow.TYPE_TIPPED&&arrow.getPotionDamage()==8194&&arrow.getPotionColor()==123456,"tipped projectile data");
  OffhandHooks.setOffhand(p,new ItemStack(CombatContent.spectralArrow));ItemCrossbow.load(p,crossbow);check(OffhandHooks.getOffhand(p)==null,"empty offhand cleared");ItemCrossbow.fire(crossbow,w,p,null);check(((EntityCombatArrow)w.lastSpawn).getCombatType()==EntityCombatArrow.TYPE_SPECTRAL,"spectral shot");
  p.inventory.mainInventory[2]=null;p.capabilities.isCreativeMode=true;int damage=crossbow.getItemDamage();check(ItemCrossbow.load(p,crossbow),"creative synthetic ammo");ItemCrossbow.fire(crossbow,w,p,null);check(crossbow.getItemDamage()==damage&&((EntityCombatArrow)w.lastSpawn).canBePickedUp==2,"creative durability and pickup");
  p.capabilities.isCreativeMode=false;p.inventory.mainInventory[2]=new ItemStack(Items.arrow,2);w.isRemote=true;check(!ItemCrossbow.load(p,crossbow)&&p.inventory.mainInventory[2].stackSize==2,"client cannot load");check(!ItemCrossbow.fire(restored,w,p,null),"client cannot spawn");w.isRemote=false;
  NBTTagCompound malformed=new NBTTagCompound();malformed.setBoolean("Charged",true);crossbow.setTagCompound(malformed);check(!ItemCrossbow.fire(crossbow,w,p,null),"missing projectile cannot spawn");
  Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(w,new net.minecraft.profiler.Profiler());
  com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager mob=new com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager(w);
  mob.setPosition(.5,10,2.5);mob.setAttackTarget(p);ItemStack mobBow=new ItemStack(item);mob.setCurrentItemOrArmor(0,mobBow);
  com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.PillagerCrossbowGoal139 goal=new com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.PillagerCrossbowGoal139(mob);
  check(goal.shouldExecute(),"mob goal needs live target and crossbow");
  Field seen=goal.getClass().getDeclaredField("visibleTicks");seen.setAccessible(true);seen.setInt(goal,4);
  w.lastSpawn=null;goal.updateTask();check(mob.getCrossbowChargeTicks()==1&&w.lastSpawn==null,"mob starts charging without shot");
  for(int i=0;i<24;i++)goal.updateTask();check(ItemCrossbow.isCharged(mobBow)&&w.lastSpawn==null,"mob completes charge without early shot");
  for(int i=0;i<41;i++)goal.updateTask();check(w.lastSpawn instanceof EntityCombatArrow,"mob fires after delay");check(((EntityCombatArrow)w.lastSpawn).canBePickedUp==0,"mob arrow not collectible");
  goal.resetTask();check(mob.getCrossbowChargeTicks()==0,"goal reset cancels charge pose");
  mob.setCurrentItemOrArmor(0,new ItemStack(Items.iron_axe));check(!goal.shouldExecute(),"weapon replacement stops crossbow AI");
  System.out.println("PASS crossbow139: "+checks);
 }
}
