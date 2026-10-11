package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityRavager;
import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.ModelRavager;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.world.World;
import net.minecraft.nbt.*;
import net.minecraft.util.DamageSource;
import net.minecraft.client.model.ModelRenderer;
import java.util.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class Ravager140Fixture {
 static int checks;
 static void check(boolean v,String s){if(!v)throw new AssertionError(s);checks++;}
 static void near(double a,double b,String s){check(Math.abs(a-b)<.00001D,s+": "+a+" != "+b);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static class Fixed extends Random {final double d;Fixed(double d){this.d=d;}public double nextDouble(){return d;}}
 static class Victim extends EntitySlime {
  int hits;float lastDamage;boolean accepts=true;
  Victim(World w){super(w);setHealth(4);}
  public boolean attackEntityFrom(DamageSource s,float amount){hits++;lastDamage=amount;return accepts;}
 }
 static final class Illager extends Victim implements com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.IRaider112 {Illager(World w){super(w);}}
 static void state(EntityRavager r,int attack,int stun,int roar){r.getDataWatcher().updateObject(20,attack);r.getDataWatcher().updateObject(21,stun);r.getDataWatcher().updateObject(22,roar);}
 static net.minecraftforge.event.entity.living.LivingAttackEvent event(EntityLivingBase player,EntityRavager r) {
  return new net.minecraftforge.event.entity.living.LivingAttackEvent(player,DamageSource.causeMobDamage(r),6){public boolean isCancelable(){return true;}};
 }
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  field(World.class,"theProfiler").set(w,new net.minecraft.profiler.Profiler());
  field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;
  EntityRavager r=new EntityRavager(w);r.setPosition(0,10,0);Victim defender=new Victim(w);defender.setPosition(2,10,0);
  Method tick=EntityRavager.class.getDeclaredMethod("tickCombatState");tick.setAccessible(true);
  Method immobile=EntityRavager.class.getDeclaredMethod("isMovementBlocked");immobile.setAccessible(true);
  defender.accepts=false;check(!r.attackEntityAsMob(defender),"rejected melee hit returns false");
  check(r.getAttackTick()==10&&defender.hits==1,"rejected hit still starts attack animation");near(defender.motionY,0,"no invented melee shove");
  check((Boolean)immobile.invoke(r),"attack is immobile");check(!r.attackEntityAsMob(defender)&&defender.hits==1,"cannot stack attack during animation");
  for(int i=0;i<10;i++)tick.invoke(r);check(r.getAttackTick()==0,"attack finishes after ten ticks");
  field(Entity.class,"rand").set(r,new Fixed(.75));defender.motionX=defender.motionY=defender.motionZ=0;
  r.onBlockedByShield(defender);check(r.getStunnedTick()==0,"upper half chance does not stun");near(defender.motionX,2,"reference inverse squared horizontal shove");near(defender.motionY,.2,"reference upward shove");
  check(defender.velocityChanged,"shield defender velocity synchronized");
  field(Entity.class,"rand").set(r,new Fixed(.25));r.onBlockedByShield(defender);
  check(r.getStunnedTick()==40,"lower half chance stuns for 40 ticks");check((Boolean)immobile.invoke(r)&&!r.canEntityBeSeen(defender),"stun immobilizes and blocks line of sight");
  check(!r.attackEntityAsMob(defender),"stun prevents melee");
  NBTTagCompound saved=new NBTTagCompound();r.writeEntityToNBT(saved);
  java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();CompressedStreamTools.writeCompressed(saved,bytes);
  saved=CompressedStreamTools.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()));
  EntityRavager copy=new EntityRavager(w);copy.readEntityFromNBT(saved);check(copy.getStunnedTick()==40,"compressed entity NBT restores stun");
  Victim victim=new Victim(w);victim.setPosition(2,10,0);Illager ally=new Illager(w);ally.setPosition(-2,10,0);
  EntityRavager other=new EntityRavager(w);other.setPosition(0,10,2);Victim far=new Victim(w);far.setPosition(12,10,0);
  Victim dead=new Victim(w);dead.setPosition(1,10,1);dead.setHealth(0);
  w.loadedEntityList.clear();w.loadedEntityList.add(victim);w.loadedEntityList.add(ally);w.loadedEntityList.add(other);w.loadedEntityList.add(far);w.loadedEntityList.add(dead);
  for(int i=0;i<39;i++)tick.invoke(r);check(r.getStunnedTick()==1&&r.getRoarTick()==0&&victim.hits==0,"no early roar");
  tick.invoke(r);check(r.getStunnedTick()==0&&r.getRoarTick()==20,"stun ends into twenty-tick roar");
  check((Boolean)immobile.invoke(r)&&!r.canEntityBeSeen(victim),"roar immobilizes and blocks line of sight");
  r.onBlockedByShield(defender);check(r.getStunnedTick()==0,"roar cannot be restarted by another shield block");
  for(int i=0;i<9;i++)tick.invoke(r);check(r.getRoarTick()==11&&victim.hits==0,"impact delayed until midpoint");
  tick.invoke(r);check(victim.hits==1&&victim.lastDamage==6,"roar deals six damage at tick ten");
  check(ally.hits==0&&ally.motionX<0,"illager ally shoved but not damaged");near(other.motionY,0,"other ravager excluded");
  check(far.hits==0&&dead.hits==0,"far and dead mobs excluded");near(victim.motionY,.2,"roar upward knockback");
  for(int i=0;i<10;i++)tick.invoke(r);check(r.getRoarTick()==0&&victim.hits==1,"one impact only");
  check(!(Boolean)immobile.invoke(r),"mob released after roar");
  w.isRemote=true;state(r,3,7,0);tick.invoke(r);r.onBlockedByShield(defender);
  check(r.getAttackTick()==3&&r.getStunnedTick()==7,"client cannot advance authoritative combat state");check(!r.attackEntityAsMob(defender),"client cannot attack");
  Method visual=EntityRavager.class.getDeclaredMethod("tickClientVisuals");visual.setAccessible(true);
  visual.invoke(r);visual.invoke(r);check(r.getAttackTick()==1&&r.getStunnedTick()==5,"visual countdown advances between metadata packets");
  check(r.getDataWatcher().getWatchableObjectInt(20)==3&&r.getDataWatcher().getWatchableObjectInt(21)==7,"visual prediction cannot change authoritative watcher state");
  state(r,0,1,0);visual.invoke(r);check(r.getStunnedTick()==0&&r.getRoarTick()==20,"client visual stun transitions to roar without damage");
  for(int i=0;i<10;i++)visual.invoke(r);check(victim.hits==1,"client visual roar cannot damage entities");
  state(r,0,0,8);check(r.getRoarTick()==8,"next metadata reconciles visual timer");w.isRemote=false;
  NBTTagCompound malformed=new NBTTagCompound();r.writeEntityToNBT(malformed);malformed.setInteger("AttackTick",999);malformed.setInteger("StunTick",-1);malformed.setInteger("RoarTick",999);
  copy.readEntityFromNBT(malformed);check(copy.getAttackTick()==10&&copy.getStunnedTick()==0&&copy.getRoarTick()==20,"NBT counters bounded");
  ModelRavager model=new ModelRavager();ModelRenderer neck=(ModelRenderer)field(ModelRavager.class,"neck").get(model),mouth=(ModelRenderer)field(ModelRavager.class,"mouth").get(model);
  state(r,5,0,0);model.setLivingAnimations(r,0,0,0);near(neck.rotationPointZ,-6.5,"attack extends neck at midpoint");check(mouth.rotateAngleX>0,"attack opens mouth");
  state(r,0,40,0);model.setLivingAnimations(r,0,0,0);near(neck.rotateAngleX,.21991149,"stun head tilt");check(Math.abs(neck.rotationPointX)>.1,"stun head shake");
  state(r,0,0,10);model.setLivingAnimations(r,0,0,0);check(mouth.rotateAngleX>.5,"roar mouth opens");
  state(r,0,0,0);model.setLivingAnimations(r,0,0,0);near(neck.rotationPointX,0,"shared model resets shake");near(neck.rotateAngleX,0,"shared model resets tilt");near(neck.rotationPointZ,5.5,"shared model resets extension");near(mouth.rotateAngleX,Math.PI*.01,"idle mouth restored");
  ClassNode cls=new ClassNode();new ClassReader(EntityRavager.class.getName()).accept(cls,0);int calls=0;
  for(MethodNode method:cls.methods)if(method.name.equals("onLivingUpdate"))for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof MethodInsnNode&&((MethodInsnNode)ins).name.equals("tickCombatState"))calls++;
  check(calls==1,"combat ticking connected exactly once to entity update");
  ClassNode combat=new ClassNode();new ClassReader(com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler.class.getName()).accept(combat,0);
  int shieldCalls=0;for(MethodNode method:combat.methods)if(method.name.equals("blockWithShield"))for(AbstractInsnNode ins:method.instructions.toArray())
   if(ins instanceof MethodInsnNode&&((MethodInsnNode)ins).name.equals("onBlockedByShield")&&((MethodInsnNode)ins).owner.endsWith("/EntityRavager"))shieldCalls++;
  check(shieldCalls==1,"effective shield handler calls ravager reaction exactly once");
  Field uf=field(sun.misc.Unsafe.class,"theUnsafe");sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
  Field potions=field(net.minecraft.potion.Potion.class,"potionTypes");unsafe.putObject(unsafe.staticFieldBase(potions),unsafe.staticFieldOffset(potions),Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536));
  Swimming115Fixture.Player player=new Swimming115Fixture.Player(w);player.setPosition(0,10,-2);player.rotationYaw=0;
  net.minecraft.item.ItemStack shield=new net.minecraft.item.ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.shield);
  player.inventory.mainInventory[0]=shield;player.inventory.currentItem=0;player.setItemInUse(shield,72000);field(net.minecraft.entity.player.EntityPlayer.class,"itemInUseCount").setInt(player,71995);
  com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler handler=new com.mod.htgt6.minecraft1202.Module.Events.Combat.CombatEventHandler();
  state(r,0,0,0);handler.onLivingAttack(event(player,r));check(r.getStunnedTick()==40,"front active shield reaches ravager stun hook");
  state(r,0,0,0);player.setPosition(0,10,2);handler.onLivingAttack(event(player,r));check(r.getStunnedTick()==0,"rear hit cannot trigger shield stun");
  player.setPosition(0,10,-2);field(net.minecraft.entity.player.EntityPlayer.class,"itemInUseCount").setInt(player,71996);
  handler.onLivingAttack(event(player,r));check(r.getStunnedTick()==0,"ineffective first four ticks cannot trigger stun");
  System.out.println("PASS ravager140: "+checks);
 }
}
