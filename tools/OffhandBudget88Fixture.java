import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import net.minecraft.entity.player.*;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import java.lang.reflect.*;
import java.util.*;
public final class OffhandBudget88Fixture {
 public static void main(String[] args)throws Exception {
  World world=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Constructor<?> pc=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);pc.setAccessible(true);EntityPlayer original=(EntityPlayer)pc.newInstance(world);
  Class<?> uc=Class.forName("sun.misc.Unsafe");Field uf=uc.getDeclaredField("theUnsafe");uf.setAccessible(true);
  EntityPlayerMP player=(EntityPlayerMP)uc.getMethod("allocateInstance",Class.class).invoke(uf.get(null),EntityPlayerMP.class);
  for(Class<?> type=EntityPlayer.class;type!=Object.class;type=type.getSuperclass())for(Field f:type.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(player,f.get(original));}
  player.inventory=new Inventory112Audit86Fixture.Bridge(player);OffhandHooks.initializeCompartments(player.inventory);player.ticksExisted=1;
  Field last=OffhandSystem.class.getDeclaredField("LAST_SWAPS");last.setAccessible(true);((Map)last.get(null)).put(player.getUniqueID(),1000);
  // Duplicate/stale requests must consume budget too; none should mutate the inventory or send packets.
  for(int i=0;i<80;i++)OffhandSystem.requestSwap(player,i,i%9);
  Field pending=OffhandSystem.class.getDeclaredField("PENDING_SWAPS");pending.setAccessible(true);
  Queue queue=(Queue)((Map)pending.get(null)).get(player.getUniqueID());
  Constructor<OffhandSystem> hc=OffhandSystem.class.getDeclaredConstructor();hc.setAccessible(true);OffhandSystem handler=hc.newInstance();
  for(int remaining:new int[]{48,16,0}) {
   handler.onPlayerTick(new cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent(cpw.mods.fml.common.gameevent.TickEvent.Phase.END,player));
   if(queue.size()!=remaining)throw new AssertionError("Wrong remaining requests: "+queue.size());
   if(remaining>0){Field seq=queue.peek().getClass().getDeclaredField("sequence");seq.setAccessible(true);if(seq.getInt(queue.peek())!=80-remaining)throw new AssertionError("FIFO changed");}
  }
  for(int i=0;i<10000;i++)OffhandSystem.requestSwap(player,i,i%9);
  if(queue.size()!=128)throw new AssertionError("Queue must be bounded at 128");
  for(int i=0;i<128;i++){Object request=queue.poll();Field sequence=request.getClass().getDeclaredField("sequence");sequence.setAccessible(true);if(sequence.getInt(request)!=i)throw new AssertionError("Overflow changed accepted FIFO");}
  java.util.List<Thread> producers=new java.util.ArrayList<Thread>();
  final EntityPlayerMP concurrentPlayer=player;
  for(int t=0;t<4;t++){final int producer=t;Thread thread=new Thread(new Runnable(){public void run(){for(int i=0;i<1000;i++)OffhandSystem.requestSwap(concurrentPlayer,producer*1000+i,i%9);}});producers.add(thread);thread.start();}
  for(Thread thread:producers)thread.join();
  if(queue.size()!=128)throw new AssertionError("Concurrent overflow exceeded capacity");
  handler.onLogout(new cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent(player));
  if(((Map)pending.get(null)).containsKey(player.getUniqueID())||((Map)last.get(null)).containsKey(player.getUniqueID()))throw new AssertionError("Logout retained state");
  System.out.println("PASS offhand: tick budget, FIFO, 10k overflow, four concurrent producers, logout cleanup");
 }
}

