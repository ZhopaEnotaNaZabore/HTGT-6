package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.CrossbowEnchantments140;
import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatArrow;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import net.minecraft.nbt.*;
import net.minecraft.world.*;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import java.util.*;
import java.lang.reflect.*;
public final class Crossbow141Fixture {
 static int checks;
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);checks++;}
 static Field field(Class<?> type,String name)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
 public static void main(String[] args)throws Exception {
  Crossbow140Fixture.main(args);
  World base=BountifulMechanicsFixture120.initialize();
  sun.misc.Unsafe unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  SpawnWorld w=(SpawnWorld)unsafe.allocateInstance(SpawnWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;w.accepted=new ArrayList<EntityCombatArrow>();
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);p.setPosition(.5,10,.5);
  ItemCrossbow item=new ItemCrossbow();cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"fixture_multi141");
  net.minecraft.enchantment.Enchantment multi=CrossbowEnchantments140.multishot;
  check(multi!=null&&multi.getMaxLevel()==1&&multi.getMinEnchantability(1)==20&&multi.getMaxEnchantability(1)==50,"modern Multishot contract");
  check(multi.canApply(new ItemStack(item))&&!multi.canApply(new ItemStack(Items.bow)),"applies only to crossbow");
  check(net.minecraft.enchantment.EnchantmentHelper.mapEnchantmentData(20,new ItemStack(item)).containsKey(multi.effectId),"real enchanting table selection");
  check(multi.canApplyTogether(CrossbowEnchantments140.quickCharge)&&CrossbowEnchantments140.quickCharge.canApplyTogether(multi),"compatible with Quick Charge");
  for(int mask=0;mask<8;mask++) {
   ItemStack weapon=new ItemStack(item);weapon.addEnchantment(multi,1);p.inventory.mainInventory[0]=weapon;p.inventory.mainInventory[1]=new ItemStack(Items.arrow,2);
   check(ItemCrossbow.load(p,weapon)&&p.inventory.mainInventory[1].stackSize==1,"one ammo for three projectiles mask "+mask);
   check(weapon.getTagCompound().getTagList("ChargedProjectiles",10).tagCount()==3,"three persisted projectiles");
   w.accepted.clear();w.mask=mask;w.attempt=0;
   check(ItemCrossbow.fire(weapon,w,p,null)==(mask!=0),"accepted volley result");
   int count=Integer.bitCount(mask);check(w.accepted.size()==count&&weapon.getItemDamage()==count,"durability per accepted arrow");
   check(ItemCrossbow.isCharged(weapon)==(mask!=7),"retain only refused members");
   weapon=ItemStack.loadItemStackFromNBT(weapon.writeToNBT(new NBTTagCompound()));
   if(mask!=7) {
    check(weapon.getTagCompound().getTagList("ChargedProjectiles",10).tagCount()==3-count,"pending count after save");
    w.mask=7;w.attempt=0;check(ItemCrossbow.fire(weapon,w,p,null),"retry refused members");
   }
   check(w.accepted.size()==3&&!ItemCrossbow.isCharged(weapon)&&weapon.getItemDamage()==3,"no duplicate accepted projectiles after retry");
   int collectible=0;for(EntityCombatArrow arrow:w.accepted)if(arrow.canBePickedUp==1)collectible++;
   check(collectible==1,"only original center arrow collectible, even after retry");
  }
  for(float pitch:new float[]{-90,-60,0,60,90})for(float yaw:new float[]{0,90,179}) {
   double y=Math.toRadians(yaw),q=Math.toRadians(pitch);Vec3 forward=Vec3.createVectorHelper(-Math.sin(y)*Math.cos(q),-Math.sin(q),Math.cos(y)*Math.cos(q));
   Vec3 left=ItemCrossbow.volleyDirection(forward,yaw,-10),right=ItemCrossbow.volleyDirection(forward,yaw,10);
   check(Math.abs(left.lengthVector()-1)<1e-6&&Math.abs(right.lengthVector()-1)<1e-6,"normalized spread including vertical aim");
   check(Math.abs(forward.dotProduct(left)-Math.cos(Math.toRadians(10)))<1e-6,"ten degree offset");
   check(Math.abs(left.dotProduct(right)-Math.cos(Math.toRadians(20)))<1e-6,"twenty degree total spread");
  }
  ItemStack weapon=new ItemStack(item);weapon.addEnchantment(multi,1);p.inventory.mainInventory[1]=new ItemStack(Items.arrow);ItemCrossbow.load(p,weapon);
  NBTTagList list=weapon.getTagCompound().getTagList("ChargedProjectiles",10);list.getCompoundTagAt(1).setInteger("HTGT6VolleySlot",0);w.attempt=0;check(!ItemCrossbow.fire(weapon,w,p,null)&&w.attempt==0,"invalid duplicate slots rejected before spawning");
  weapon=new ItemStack(item);weapon.addEnchantment(multi,1);p.capabilities.isCreativeMode=true;ItemCrossbow.load(p,weapon);w.mask=7;w.attempt=0;w.accepted.clear();ItemCrossbow.fire(weapon,w,p,null);check(weapon.getItemDamage()==0&&w.accepted.size()==3,"creative triple volley");for(EntityCombatArrow arrow:w.accepted)check(arrow.canBePickedUp==2,"creative pickup restriction");
  p.capabilities.isCreativeMode=false;
  ItemStack tagged=new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.tippedArrow,2,8194);NBTTagCompound potion=new NBTTagCompound();potion.setInteger("CustomPotionColor",123456);tagged.setTagCompound(potion);p.inventory.mainInventory[1]=tagged;
  weapon=new ItemStack(item);weapon.addEnchantment(multi,1);ItemCrossbow.load(p,weapon);list=weapon.getTagCompound().getTagList("ChargedProjectiles",10);
  tagged.getTagCompound().setInteger("CustomPotionColor",111);check(list.getCompoundTagAt(0).getCompoundTag("tag").getInteger("CustomPotionColor")==123456,"ammunition NBT independent from source stack");
  list.getCompoundTagAt(0).getCompoundTag("tag").setInteger("CustomPotionColor",222);check(list.getCompoundTagAt(1).getCompoundTag("tag").getInteger("CustomPotionColor")==123456,"each projectile has independent NBT");
  w.mask=7;w.attempt=0;w.accepted.clear();ItemCrossbow.fire(weapon,w,p,null);check(w.accepted.get(0).getPotionColor()==222&&w.accepted.get(1).getPotionColor()==123456&&w.accepted.get(2).getPotionColor()==123456,"all three tipped arrows preserve their effects");
  com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager pillager=new com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager(w);pillager.setPosition(0,10,3);weapon=new ItemStack(item);weapon.addEnchantment(multi,1);
  check(ItemCrossbow.loadMob(pillager,weapon),"mob loads three virtual arrows");w.mask=7;w.attempt=0;w.accepted.clear();check(ItemCrossbow.fire(weapon,w,pillager,p)&&w.accepted.size()==3,"mob triple volley");check(w.accepted.get(0).canBePickedUp==0&&w.accepted.get(1).canBePickedUp==2&&w.accepted.get(2).canBePickedUp==2,"mob side arrows creative-only as reference");
  item.registerIcons(new net.minecraft.client.renderer.texture.IIconRegister(){public net.minecraft.util.IIcon registerIcon(final String name){return (net.minecraft.util.IIcon)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{net.minecraft.util.IIcon.class},new InvocationHandler(){public Object invoke(Object proxy,Method method,Object[] values){if(method.getReturnType()==String.class)return name;if(method.getReturnType()==float.class)return 0F;if(method.getReturnType()==int.class)return 16;return null;}});}});
  p.inventory.mainInventory[0]=null;p.inventory.mainInventory[1]=new ItemStack(Items.arrow,2);ItemStack secondary=new ItemStack(item);com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks.setOffhand(p,secondary);w.isRemote=true;
  for(int t=0;t<26;t++)com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.clientTick(p,true);
  check(item.getIcon(secondary.copy(),0,p,null,0).getIconName().endsWith("crossbow_pulling_2"),"offhand copied renderer item uses charge-state sprite");
  com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandCrossbowUse120.clientTick(p,false);check(item.getIcon(secondary.copy(),0,p,null,0).getIconName().endsWith("crossbow_standby"),"offhand release resets sprite");
  w.isRemote=false;ItemCrossbow.load(p,secondary);check(item.getIconIndex(secondary).getIconName().endsWith("crossbow_arrow"),"loaded offhand has charged sprite");
  System.out.println("PASS crossbow141: "+checks);
 }
 static final class SpawnWorld extends World {
  List<EntityCombatArrow> accepted;int attempt,mask;
  SpawnWorld(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
  public boolean spawnEntityInWorld(Entity e){boolean ok=(mask&(1<<attempt++))!=0;if(ok)accepted.add((EntityCombatArrow)e);return ok;}
  public net.minecraft.block.Block getBlock(int x,int y,int z){return net.minecraft.init.Blocks.air;}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected int func_152379_p(){return 0;}
  public Entity getEntityByID(int id){return null;}
 }
}
