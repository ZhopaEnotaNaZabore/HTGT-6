package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketOffhandCrossbowState;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.CrossbowPlayerPoseHooks142;
import net.minecraft.entity.Entity;import net.minecraft.item.*;import net.minecraft.init.Items;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;import java.lang.reflect.*;import java.util.*;
public final class CrossbowObserver146Fixture {
 static int checks;static void check(boolean value,String m){if(!value)throw new AssertionError(m);checks++;}
 public static void main(String[]args)throws Exception{
  OffhandCrossbow142Fixture.main(args);
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();w.provider.worldObj=w;Field f=World.class.getDeclaredField("worldInfo");f.setAccessible(true);f.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  ItemCrossbow item=new ItemCrossbow();cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"observer146_crossbow");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);p.setEntityId(146);p.inventory.mainInventory[0]=new ItemStack(item);ItemStack held=p.getHeldItem();
  PacketOffhandCrossbowState idle=new PacketOffhandCrossbowState(p,-1);p.setItemInUse(held,71990);check(OffhandCrossbowUse120.mainUseTicks(p)==10,"server main-hand duration");PacketOffhandCrossbowState active=OffhandCrossbow142Fixture.round(new PacketOffhandCrossbowState(p,-1));check(!idle.samePresentation(active),"main use starts immediate observer update");
  Swimming115Fixture.Player remote=new Swimming115Fixture.Player(w);remote.inventory=new Crossbow140Fixture.Inventory(remote);remote.inventory.mainInventory[0]=held.copy();remote.setEntityId(146);f=Entity.class.getDeclaredField("entityUniqueID");f.setAccessible(true);f.set(remote,p.getUniqueID());
  w.isRemote=true;check(active.applyTo(remote,false),"packet applies remote state");check(OffhandCrossbowUse120.mainUseTicks(remote)==10,"remote main starts correct elapsed");remote.ticksExisted+=4;check(OffhandCrossbowUse120.renderUseTicks(remote,held.copy())==14,"copied main-hand renderer timer");
  net.minecraft.client.model.ModelBiped model=new net.minecraft.client.model.ModelBiped();CrossbowPlayerPoseHooks142.apply(model,remote);check(Math.abs(model.bipedRightArm.rotateAngleY+.8F)<.00001,"remote main arm charge pose");
  remote.inventory.mainInventory[0]=new ItemStack(Items.stick);check(OffhandCrossbowUse120.mainUseTicks(remote)==-1,"equipment mismatch suppresses stale pose");remote.inventory.mainInventory[0]=held.copy();remote.ticksExisted+=61;check(OffhandCrossbowUse120.mainUseTicks(remote)==-1,"missing snapshots expire");
  active.applyTo(remote,false);check(OffhandCrossbowUse120.mainUseTicks(remote)==10,"periodic snapshot corrects elapsed");OffhandCrossbow142Fixture.round(active.stopped()).applyTo(remote,false);check(OffhandCrossbowUse120.mainUseTicks(remote)==-1,"tracking stop clears main timer");
  active.applyTo(remote,false);OffhandCrossbowUse120.cancel(remote);check(OffhandCrossbowUse120.mainUseTicks(remote)==-1,"world cleanup cancels remote main timer");
  w.isRemote=false;p.clearItemInUse();check(!active.samePresentation(new PacketOffhandCrossbowState(p,-1)),"release triggers immediate update");
  System.out.println("PASS crossbow observer146: "+checks);
 }
}
