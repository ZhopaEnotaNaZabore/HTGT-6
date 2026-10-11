package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Achievements.BackportAchievements120;
import net.minecraft.stats.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;
public final class Achievements123Fixture {
 static int checks;
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  Field registryUnsafe=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");registryUnsafe.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)registryUnsafe.get(null);Field potions=net.minecraft.potion.Potion.class.getDeclaredField("potionTypes");unsafe.putObject(unsafe.staticFieldBase(potions),unsafe.staticFieldOffset(potions),Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536));
  List<Achievement> old=new ArrayList<Achievement>(AchievementList.achievementList);Map<Achievement,String> positions=new HashMap<Achievement,String>();for(Achievement a:old)positions.put(a,a.displayColumn+","+a.displayRow+":"+a.statId);
  Method register=BackportAchievements120.class.getDeclaredMethod("registerAchievements");register.setAccessible(true);register.invoke(null);int size=AchievementList.achievementList.size();register.invoke(null);check(size==AchievementList.achievementList.size(),"idempotent registration");
  Field initialized=BackportAchievements120.class.getDeclaredField("initialized");initialized.setAccessible(true);initialized.setBoolean(null,true);
  Field entries=BackportAchievements120.class.getDeclaredField("ENTRIES");entries.setAccessible(true);Map<String,Achievement> all=(Map<String,Achievement>)entries.get(null);check(all.size()==19,"19 implemented achievements");
  Set<String> ids=new HashSet<String>(),coords=new HashSet<String>();for(Achievement a:old){check(positions.get(a).equals(a.displayColumn+","+a.displayRow+":"+a.statId),"old identity and coordinates retained");coords.add(a.displayColumn+","+a.displayRow);}
  for(Achievement a:all.values()){
   check(ids.add(a.statId),"unique persistent ID");check(coords.add(a.displayColumn+","+a.displayRow),"no occupied coordinates");
   check(!net.minecraftforge.common.AchievementPage.isAchievementInPages(a),"belongs to vanilla page");check(a.theItemStack!=null&&a.theItemStack.getItem()!=null,"valid icon");
   Set<Achievement> ancestry=new HashSet<Achievement>();for(Achievement n=a;n!=null;n=n.parentAchievement)check(ancestry.add(n),"acyclic parent chain");
  }
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);FakePlayer p=(FakePlayer)((sun.misc.Unsafe)uf.get(null)).allocateInstance(FakePlayer.class);p.worldObj=w;p.stats=new Stats();
  BackportAchievements120.complete(p,"totem_of_undying");Achievement totem=all.get("totem_of_undying");
  check(!p.stats.hasAchievementUnlocked(totem),"does not bypass parent");
  NBTTagCompound persisted=p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
  check(persisted.getCompoundTag("HTGT6BackportAchievements").getBoolean("totem_of_undying"),"rare action retained before parent");
  java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();CompressedStreamTools.writeCompressed(persisted,bytes);
  NBTTagCompound restored=CompressedStreamTools.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()));p.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG,restored);
  p.stats.func_150873_a(p,totem.parentAchievement,1);
  Method flush=BackportAchievements120.class.getDeclaredMethod("flush",EntityPlayer.class,StatFileWriter.class,NBTTagCompound.class);flush.setAccessible(true);
  flush.invoke(null,p,p.stats,restored.getCompoundTag("HTGT6BackportAchievements"));
  check(p.stats.hasAchievementUnlocked(totem),"restored pending criterion unlocks after parent");
  BackportAchievements120.complete(p,"totem_of_undying");check(p.stats.writeStat(totem)==1,"no repeat award");
  Achievement lev=all.get("levitate");p.stats.func_150873_a(p,lev.parentAchievement,1);BackportAchievements120.complete(p,"levitate");BackportAchievements120.complete(p,"levitate");check(p.xp==50,"challenge XP exactly once");
  w.isRemote=true;BackportAchievements120.complete(p,"elytra");check(!p.stats.hasAchievementUnlocked(all.get("elytra")),"client cannot grant");w.isRemote=false;
  BackportAchievements120.complete(p,"unknown");check(!restored.getCompoundTag("HTGT6BackportAchievements").hasKey("unknown"),"unknown criterion rejected");
  Method inv=BackportAchievements120.class.getDeclaredMethod("inventoryCriterion",ItemStack.class);inv.setAccessible(true);
  check(inv.invoke(null,new ItemStack(Items.skull,1,0))==null,"ordinary skull rejected");check("get_wither_skull".equals(inv.invoke(null,new ItemStack(Items.skull,1,1))),"wither metadata required");
  check(inv.invoke(null,new ItemStack(Items.iron_pickaxe,0))==null,"empty stack rejected");
  check("shiny_gear".equals(inv.invoke(null,new ItemStack(Items.diamond_boots))),"any diamond armor piece satisfies OR criterion");
  check(inv.invoke(null,new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.totemOfUndying))==null,"owning totem does not trigger use criterion");
  check(inv.invoke(null,new ItemStack(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.fishBucket))==null,"owning bucket does not trigger capture criterion");
  System.out.println("PASS achievements123: "+checks+" checks");
 }
 static class FakePlayer extends EntityPlayerMP{
  Stats stats;int xp;
  private FakePlayer(){super(null,null,null,null);}
  public StatisticsFile func_147099_x(){return stats;}
  public void triggerAchievement(StatBase a){stats.func_150871_b(this,a,1);}
  public void addExperience(int amount){xp+=amount;}
 }
 static class Stats extends StatisticsFile{
  Stats(){super(null,new java.io.File("build/achievements123/unused-stats.json"));}
  public void func_150873_a(EntityPlayer p,StatBase s,int value){net.minecraft.util.TupleIntJsonSerializable v=new net.minecraft.util.TupleIntJsonSerializable();v.setIntegerValue(value);field_150875_a.put(s,v);}
 }
 static void check(boolean v,String n){if(!v)throw new AssertionError(n);checks++;}
}
