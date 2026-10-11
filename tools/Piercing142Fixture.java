package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import net.minecraft.entity.*;import net.minecraft.entity.monster.EntitySlime;import net.minecraft.world.*;import net.minecraft.util.*;import net.minecraft.nbt.*;import java.util.*;import java.lang.reflect.*;
public final class Piercing142Fixture {
 static int checks;static void check(boolean b,String s){if(!b)throw new AssertionError(s);checks++;}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 public static class Target extends EntitySlime {int hits;boolean accept=true;Target(World w){super(w);setSize(.2F,.2F);}public boolean attackEntityFrom(DamageSource d,float f){hits++;return !net.minecraftforge.common.ForgeHooks.onLivingAttack(this,d,f)&&accept;}}
 public static class Arrow extends EntityCombatArrow {Arrow(World w){super(w);}public boolean isWet(){return false;}public boolean isInWater(){return false;}}
 public static class Cancel implements cpw.mods.fml.common.eventhandler.IEventListener {Target target;public void invoke(cpw.mods.fml.common.eventhandler.Event value){if(!(value instanceof net.minecraftforge.event.entity.living.LivingAttackEvent))return;net.minecraftforge.event.entity.living.LivingAttackEvent e=(net.minecraftforge.event.entity.living.LivingAttackEvent)value;if(e.entityLiving==target)e.setCanceled(true);}}
 static final class TestWorld extends World {
  List<Entity> targets;boolean wall;
  TestWorld(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
  public List getEntitiesWithinAABBExcludingEntity(Entity e,AxisAlignedBB box){return targets;}
  public net.minecraft.block.Block getBlock(int x,int y,int z){return net.minecraft.init.Blocks.air;}
  public MovingObjectPosition func_147447_a(Vec3 a,Vec3 b,boolean c,boolean d,boolean e){return wall?new MovingObjectPosition(2,10,0,4,Vec3.createVectorHelper(2,10,0)):null;}
  public int getBlockMetadata(int x,int y,int z){return 0;}
  public boolean blockExists(int x,int y,int z){return false;}
  public boolean checkChunksExist(int a,int b,int c,int d,int e,int f){return false;}
  public boolean handleMaterialAcceleration(AxisAlignedBB b,net.minecraft.block.material.Material m,Entity e){return false;}
  public void playSoundAtEntity(Entity e,String s,float v,float p){}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}public Entity getEntityByID(int i){return null;}
 }
 static Arrow arrow(TestWorld w,int level){Arrow a=new Arrow(w);a.setPierceLevel(level);a.setPosition(0,10,0);a.motionX=8;a.motionY=0;a.motionZ=0;return a;}
 static Target target(TestWorld w,double x){Target t=new Target(w);t.setPosition(x,10,0);w.targets.add(t);return t;}
 public static void main(String[] args)throws Exception{
  World base=BountifulMechanicsFixture120.initialize();sun.misc.Unsafe unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);TestWorld w=(TestWorld)unsafe.allocateInstance(TestWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));field(World.class,"theProfiler").set(w,new net.minecraft.profiler.Profiler());field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;w.targets=new ArrayList<Entity>();
  for(int level=0;level<=4;level++){
   w.targets.clear();for(int n=1;n<=6;n++)target(w,n);Arrow a=arrow(w,level);a.onUpdate();
   for(int n=0;n<6;n++)check(((Target)w.targets.get(n)).hits==(n<=level?1:0),"N+1 collision budget level="+level+" target="+n);
   check(a.isDead,"over-budget target discards arrow level "+level);check(((Target)w.targets.get(0)).getArrowCountInEntity()==(level==0?1:0),"piercing does not leave embedded arrow render");
  }
  w.targets.clear();Target t1=target(w,1),t2=target(w,2);Arrow a=arrow(w,4);a.onUpdate();check(t1.hits==1&&t2.hits==1&&!a.isDead,"multiple hits in same tick, survives");check(Math.abs(a.posX-8)<.001&&Math.abs(a.motionX-7.92)<.001&&Math.abs(a.motionY+.05)<.001,"movement and drag applied once");
  a.setPosition(0,10,0);a.motionX=8;a.motionY=0;a.onUpdate();check(t1.hits==1&&t2.hits==1,"cannot hit same targets again");
  NBTTagCompound tag=new NBTTagCompound();a.writeEntityToNBT(tag);Arrow loaded=arrow(w,0);loaded.readEntityFromNBT(tag);check(loaded.getPierceLevel()==4&&!EntityCombatArrow.canHit(t1,loaded),"NBT preserves level and exclusions");
  io.netty.buffer.ByteBuf data=io.netty.buffer.Unpooled.buffer();a.writeSpawnData(data);Arrow client=arrow(w,0);client.readSpawnData(data);check(client.getPierceLevel()==4,"spawn sync");data.release();
  w.targets.clear();t1=target(w,1);t1.accept=false;t2=target(w,2);a=arrow(w,4);a.onUpdate();check(t1.hits==1&&t2.hits==0&&!a.isDead&&a.motionX<0,"rejected damage bounces and stops collision loop");
  w.targets.clear();t1=target(w,1);t2=target(w,3);w.wall=true;a=arrow(w,4);a.onUpdate();check(t1.hits==1&&t2.hits==0,"wall limits all repeated rays");w.wall=false;
  w.targets.clear();t1=target(w,1);t2=target(w,2);Target t3=target(w,3);a=arrow(w,4);
  NBTTagCompound effectTag=new NBTTagCompound();a.writeEntityToNBT(effectTag);effectTag.setInteger("CombatType",1);NBTTagCompound potion=new NBTTagCompound();NBTTagList effects=new NBTTagList();NBTTagCompound effect=new NBTTagCompound();new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.moveSpeed.id,123,0).writeCustomPotionEffectToNBT(effect);effects.appendTag(effect);potion.setTag("CustomPotionEffects",effects);effectTag.setTag("PotionTag",potion);a.readEntityFromNBT(effectTag);
  Cancel cancel=new Cancel();cancel.target=t2;int bus=field(cpw.mods.fml.common.eventhandler.EventBus.class,"busID").getInt(net.minecraftforge.common.MinecraftForge.EVENT_BUS);new net.minecraftforge.event.entity.living.LivingAttackEvent(t2,DamageSource.generic,1).getListenerList().register(bus,cpw.mods.fml.common.eventhandler.EventPriority.NORMAL,cancel);a.onUpdate();cpw.mods.fml.common.eventhandler.ListenerList.unregisterAll(bus,cancel);
  check(t1.isPotionActive(net.minecraft.potion.Potion.moveSpeed)&&t1.getActivePotionEffect(net.minecraft.potion.Potion.moveSpeed).getDuration()==123,"first accepted target gets custom potion");check(t2.hits==1&&!t2.isPotionActive(net.minecraft.potion.Potion.moveSpeed)&&t3.hits==0,"Forge cancellation prevents effect and next hit");
  w.targets.clear();t1=target(w,1);t2=target(w,2);a=arrow(w,4);a.readEntityFromNBT(effectTag);a.onUpdate();check(t1.isPotionActive(net.minecraft.potion.Potion.moveSpeed)&&t2.isPotionActive(net.minecraft.potion.Potion.moveSpeed),"each accepted target gets effect");
  a.setPierceLevel(99);check(a.getPierceLevel()==4,"max clamp");a.setPierceLevel(-1);check(a.getPierceLevel()==0,"min clamp");
  System.out.println("PASS Piercing142 runtime transformed arrow: "+checks);
 }
}
