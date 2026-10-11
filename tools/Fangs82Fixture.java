import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvokerFangs112;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import net.minecraft.world.World;
import net.minecraft.util.DamageSource;
import net.minecraft.nbt.NBTTagCompound;
import io.netty.buffer.*;
public final class Fangs82Fixture {
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid world=BountifulMechanicsFixture120.initialize();
  java.lang.reflect.Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(world,new net.minecraft.profiler.Profiler());
  Target victim=new Target(world);victim.setPosition(0,2,0);world.loadedEntityList.add(victim);
  for(int delay=0;delay<16;delay++){
   victim.hits=0;EntityEvokerFangs112 fang=new EntityEvokerFangs112(world,0,2,0,(float)Math.PI/2,delay,null);
   check(Math.abs(fang.rotationYaw-90)<.001,"radians not converted");
   for(int t=1;t<=delay+23;t++){fang.onUpdate();check(victim.hits==(t<delay+8?0:1),"strike delay "+delay+" tick "+t);check(fang.isDead==(t==delay+23),"lifetime");}
  }
  EntityEvokerFangs112 active=new EntityEvokerFangs112(world,0,2,0,0,3,victim);
  for(int t=0;t<8;t++)active.onUpdate();
  ByteBuf packet=Unpooled.buffer();EntityEvokerFangs112 client=new EntityEvokerFangs112(world);
  try{active.writeSpawnData(packet);client.readSpawnData(packet);}finally{packet.release();}
  check(client.animationProgress(.5F)==active.animationProgress(.5F),"late spawn animation");
  NBTTagCompound nbt=new NBTTagCompound();active.writeToNBT(nbt);
  check(nbt.getString("OwnerUUID").equals(victim.getUniqueID().toString()),"owner NBT");
  EntityEvokerFangs112 restored=new EntityEvokerFangs112(world);restored.readFromNBT(nbt);victim.hits=0;
  for(int t=0;t<10;t++)restored.onUpdate();check(victim.hits==0,"restored owner hurt");
  EntityEvokerFangs112 waiting=new EntityEvokerFangs112(world);world.isRemote=true;
  try{waiting.onUpdate();check(waiting.animationProgress(0)==0,"client starts before event");waiting.handleHealthUpdate((byte)4);waiting.onUpdate();check(waiting.animationProgress(.5F)>0,"client start event");}
  finally{world.isRemote=false;}
  System.out.println("PASS: all 16 delays, one hit, full lifetime, degrees, late spawn, owner NBT and client start event");
 }
 static final class Target extends net.minecraft.entity.passive.EntitySheep {
  int hits;Target(World world){super(world);}
  @Override public boolean attackEntityFrom(DamageSource source,float amount){check(amount==6,"damage");hits++;return true;}
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
