package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.*;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Network.Combat.PacketOffhandCrossbowState;
import cpw.mods.fml.common.network.simpleimpl.*;
import net.minecraft.entity.player.*;import net.minecraft.entity.Entity;import net.minecraft.item.*;import net.minecraft.init.Items;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;import java.lang.reflect.*;import java.util.*;import io.netty.buffer.*;
public class OffhandCrossbow142Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static class Recorder extends SimpleNetworkWrapper{final List<IMessage> sent=new ArrayList<IMessage>();Recorder(){super("Offhand142Test");}@Override public void sendTo(IMessage m,EntityPlayerMP p){sent.add(m);}}
 static PacketOffhandCrossbowState round(PacketOffhandCrossbowState p){ByteBuf b=Unpooled.buffer();try{p.toBytes(b);PacketOffhandCrossbowState q=new PacketOffhandCrossbowState();q.fromBytes(b);return q;}finally{b.release();}}
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();w.provider.worldObj=w;
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));Field accesses=World.class.getDeclaredField("worldAccesses");accesses.setAccessible(true);accesses.set(w,new ArrayList());
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);Field registry=net.minecraft.potion.Potion.class.getDeclaredField("potionTypes");u.putObject(u.staticFieldBase(registry),u.staticFieldOffset(registry),Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536));
  ItemCrossbow item=new ItemCrossbow();cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"offhand142_crossbow");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);p.setEntityId(142);p.inventory.mainInventory[2]=new ItemStack(Items.arrow,10);ItemStack stack=new ItemStack(item);OffhandHooks.setOffhand(p,stack);
  OffhandCrossbowUse120.start(p);for(int t=0;t<7;t++)OffhandCrossbowUse120.tick(p);
  PacketOffhandCrossbowState snapshot=round(new PacketOffhandCrossbowState(p,7));check(!snapshot.applyTo(p,false),"server cannot apply watcher snapshot");
  Field uid=Entity.class.getDeclaredField("entityUniqueID");uid.setAccessible(true);
  Swimming115Fixture.Player remote=new Swimming115Fixture.Player(w);remote.inventory=new Crossbow140Fixture.Inventory(remote);remote.setEntityId(142);uid.set(remote,p.getUniqueID());
  w.isRemote=true;check(!snapshot.applyTo(remote,true),"owner inventory protected");check(snapshot.applyTo(remote,false),"matching remote receives stack and state");
  ItemStack copy=OffhandHooks.getOffhand(remote);check(copy!=stack&&copy.getItem()==item,"snapshot copies offhand equipment");check(OffhandCrossbowUse120.useTicks(remote,copy.copy())==7,"initial elapsed");remote.ticksExisted+=5;check(OffhandCrossbowUse120.useTicks(remote,copy)==12,"remote timer interpolates by client tick");
  remote.ticksExisted+=61;check(OffhandCrossbowUse120.useTicks(remote,copy)==-1,"stale remote state expires");snapshot.applyTo(remote,false);check(OffhandCrossbowUse120.useTicks(remote,copy)==7,"periodic state repairs timer");
  check(round(snapshot.stopped()).applyTo(remote,false)&&OffhandCrossbowUse120.useTicks(remote,copy)==-1,"stop clears pose");
  remote.dimension=1;check(!snapshot.applyTo(remote,false),"dimension mismatch rejected");remote.dimension=0;remote.setEntityId(999);check(!snapshot.applyTo(remote,false),"entity ID mismatch rejected");remote.setEntityId(142);uid.set(remote,UUID.randomUUID());check(!snapshot.applyTo(remote,false),"reused entity ID UUID rejected");uid.set(remote,p.getUniqueID());
  for(int elapsed:new int[]{-2,72001}){ByteBuf b=Unpooled.buffer();try{new PacketOffhandCrossbowState(p,elapsed).toBytes(b);boolean rejected=false;try{new PacketOffhandCrossbowState().fromBytes(b);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid elapsed rejected");}finally{b.release();}}
  ByteBuf shortPacket=Unpooled.buffer(1).writeByte(0);try{boolean rejected=false;try{new PacketOffhandCrossbowState().fromBytes(shortPacket);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"truncated packet rejected");}finally{shortPacket.release();}
  w.isRemote=false;
  Field network=OffhandSystem.class.getDeclaredField("NETWORK");Recorder recorder=new Recorder();u.putObject(u.staticFieldBase(network),u.staticFieldOffset(network),recorder);
  EntityPlayerMP watcher=(EntityPlayerMP)u.allocateInstance(EntityPlayerMP.class);uid.set(watcher,UUID.randomUUID());watcher.worldObj=w;
  OffhandSystem system=(OffhandSystem)u.allocateInstance(OffhandSystem.class);system.crossbowStartTracking(new net.minecraftforge.event.entity.player.PlayerEvent.StartTracking(watcher,p));check(recorder.sent.size()==1,"late watcher gets current state");
  Method sync=OffhandSystem.class.getDeclaredMethod("syncCrossbowObservers",EntityPlayer.class);sync.setAccessible(true);p.ticksExisted=1;sync.invoke(null,p);check(recorder.sent.size()==1,"unchanged state avoids redundant send");p.ticksExisted=20;sync.invoke(null,p);check(recorder.sent.size()==2,"periodic correction");
  OffhandCrossbowUse120.cancel(p);p.ticksExisted=21;sync.invoke(null,p);check(recorder.sent.size()==3,"cancel broadcasts immediately");
  system.crossbowStopTracking(new net.minecraftforge.event.entity.player.PlayerEvent.StopTracking(watcher,p));check(recorder.sent.size()==4,"stop tracking clears observer pose");p.ticksExisted=40;sync.invoke(null,p);check(recorder.sent.size()==4,"stopped watcher receives no more updates");
  system.crossbowStartTracking(new net.minecraftforge.event.entity.player.PlayerEvent.StartTracking(watcher,p));
  Method clear=OffhandSystem.class.getDeclaredMethod("clearState",EntityPlayer.class);clear.setAccessible(true);clear.invoke(null,p);check(recorder.sent.size()==6,"logout sends stop to watchers");
  system.crossbowStartTracking(new net.minecraftforge.event.entity.player.PlayerEvent.StartTracking(watcher,p));clear.invoke(null,watcher);p.ticksExisted=60;sync.invoke(null,p);check(recorder.sent.size()==7,"watcher logout removes watcher references");
  system.crossbowStartTracking(new net.minecraftforge.event.entity.player.PlayerEvent.StartTracking(watcher,p));p.dimension=1;
  system.onDimension(new cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent(p,0,1));check(recorder.sent.size()==9,"dimension change sends final stop");
  w.isRemote=true;remote.dimension=0;check(((PacketOffhandCrossbowState)recorder.sent.get(8)).applyTo(remote,false),"dimension cleanup retains old identity for old watchers");w.isRemote=false;p.dimension=0;
  ByteBuf trailing=Unpooled.buffer();try{snapshot.toBytes(trailing);trailing.writeByte(9);boolean rejected=false;try{new PacketOffhandCrossbowState().fromBytes(trailing);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"trailing bytes rejected");}finally{trailing.release();}
  OffhandHooks.setOffhand(p,new ItemStack(Items.stick));ByteBuf invalidStack=Unpooled.buffer();try{new PacketOffhandCrossbowState(p,10).toBytes(invalidStack);boolean rejected=false;try{new PacketOffhandCrossbowState().fromBytes(invalidStack);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"active pose requires crossbow");}finally{invalidStack.release();}
  System.out.println("PASS offhand crossbow142: "+checks+" checks");
 }
}

