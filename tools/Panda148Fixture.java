package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityPanda120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.entity.*;import net.minecraft.entity.item.EntityItem;import net.minecraft.entity.ai.EntityAIBase;import net.minecraft.entity.monster.EntitySlime;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.world.*;import net.minecraft.nbt.*;import net.minecraft.util.*;import java.lang.reflect.*;import java.util.*;
public final class Panda148Fixture {
 static int checks;static void check(boolean ok,String m){if(!ok)throw new AssertionError(m);checks++;}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static EntityAIBase goal(EntityPanda120 panda,String name)throws Exception{for(Class<?> c:EntityPanda120.class.getDeclaredClasses())if(c.getSimpleName().equals(name)){Constructor<?> ctor=c.getDeclaredConstructor(EntityPanda120.class);ctor.setAccessible(true);return (EntityAIBase)ctor.newInstance(panda);}throw new AssertionError(name);}
 public static void main(String[]args)throws Exception{
  World base=BountifulMechanicsFixture120.initialize();cpw.mods.fml.common.registry.GameRegistry.registerBlock(Backport120Content.bamboo,"panda148_bamboo");
  sun.misc.Unsafe u=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);TestWorld w=(TestWorld)u.allocateInstance(TestWorld.class);for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));w.provider.worldObj=w;w.entities=new ArrayList<Entity>();w.drops=new ArrayList<EntityItem>();w.tracker=(Tracker)u.allocateInstance(Tracker.class);
  EntityPanda120 p=new EntityPanda120(w);p.setPosition(0,10,0);EntityItem food=new EntityItem(w,6,10,0,new ItemStack(Backport120Content.bamboo,3));w.entities.add(food);
  EntityAIBase sit=goal(p,"PandaSitGoal");check(sit.shouldExecute(),"food search locates distant bamboo");check(!food.isDead&&food.getEntityItem().stackSize==3&&p.getHeldItem()==null,"AI search does not teleport food into mouth");
  Method pickup=EntityPanda120.class.getDeclaredMethod("pickupNearbyFood");pickup.setAccessible(true);pickup.invoke(p);check(p.getHeldItem()==null,"distant food not picked up");
  food.setPosition(.5,10,0);food.delayBeforeCanPickup=10;pickup.invoke(p);check(p.getHeldItem()==null,"pickup delay respected");food.delayBeforeCanPickup=0;w.getGameRules().setOrCreateGameRule("mobGriefing","false");pickup.invoke(p);check(p.getHeldItem()==null&&!food.isDead,"mobGriefing forbids pickup");w.getGameRules().setOrCreateGameRule("mobGriefing","true");
  NBTTagCompound named=new NBTTagCompound();named.setString("fixture","preserved");food.getEntityItem().setTagCompound(named);pickup.invoke(p);check(food.isDead&&p.getHeldItem().stackSize==3&&"preserved".equals(p.getHeldItem().getTagCompound().getString("fixture")),"contact picks whole stack and preserves NBT");check(w.tracker.packets==1,"pickup animation uses entity tracker");
  check(EntityPanda120.isPandaFood(new ItemStack(Items.cake))&&!p.isBreedingItem(new ItemStack(Items.cake)),"cake is edible but not breeding food");check(p.isBreedingItem(new ItemStack(Backport120Content.bamboo)),"bamboo still breeds");
  p.setCurrentItemOrArmor(0,null);EntityItem cake=new EntityItem(w,.2,10,0,new ItemStack(Items.cake));w.entities.add(cake);pickup.invoke(p);check(cake.isDead&&p.getHeldItem().getItem()==Items.cake,"nearby cake picked up");p.setCurrentItemOrArmor(0,null);
  Method loot=EntityPanda120.class.getDeclaredMethod("dropFewItems",boolean.class,int.class);loot.setAccessible(true);for(int level:new int[]{0,1,3,20}){w.drops.clear();loot.invoke(p,true,level);check(w.drops.size()==1&&w.drops.get(0).getEntityItem().stackSize==1,"exactly one bamboo irrespective looting");}
  field(EntityLiving.class,"navigator").set(p,new Nav(p,w));Target victim=new Target(w);victim.setPosition(.5,10,0);p.setMainGene(EntityPanda120.Gene.AGGRESSIVE);p.setAttackTarget(victim);EntityAIBase attack=goal(p,"PandaAttackGoal");for(int i=0;i<10;i++)attack.updateTask();check(victim.hits==1,"attack goal no longer attacks every tick");for(int i=0;i<11;i++)attack.updateTask();check(victim.hits==2,"twenty-tick melee cooldown");p.setSitting(true);check(!attack.shouldExecute(),"sitting prevents attack goal");
  System.out.println("PASS panda148: "+checks);
 }
 static final class Tracker extends EntityTracker{int packets;Tracker(){super(null);}@Override public void func_151247_a(Entity e,net.minecraft.network.Packet p){packets++;}}
 static final class Nav extends net.minecraft.pathfinding.PathNavigate{Nav(EntityLiving e,World w){super(e,w);}@Override public boolean tryMoveToEntityLiving(Entity e,double s){return true;}}
 static final class Target extends EntitySlime{int hits;Target(World w){super(w);setHealth(20);}@Override public boolean attackEntityFrom(DamageSource source,float damage){hits++;return true;}}
 static final class TestWorld extends WorldServer{
  List<Entity> entities;List<EntityItem> drops;Tracker tracker;TestWorld(){super(null,null,"fixture",0,null,null);}
  @Override public EntityTracker getEntityTracker(){return tracker;}
  @Override public List getEntitiesWithinAABB(Class type,AxisAlignedBB box){List<Entity> out=new ArrayList<Entity>();for(Entity e:entities)if(type.isInstance(e)&&!e.isDead&&box.intersectsWith(e.boundingBox))out.add(e);return out;}
  @Override public boolean spawnEntityInWorld(Entity e){if(e instanceof EntityItem)drops.add((EntityItem)e);return true;}
  @Override public net.minecraft.block.Block getBlock(int x,int y,int z){return Blocks.air;}
 }
}
