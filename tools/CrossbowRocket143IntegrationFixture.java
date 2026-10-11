package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.CrossbowEnchantments140;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityCrossbowRocket143;
import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.nbt.*;import net.minecraft.world.*;import net.minecraft.entity.Entity;
import java.util.*;import java.lang.reflect.*;
public final class CrossbowRocket143IntegrationFixture {
 static int checks;static void check(boolean value,String label){if(!value)throw new AssertionError(label);checks++;}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static ItemStack rockets(int count){ItemStack s=new ItemStack(Items.fireworks,count);NBTTagCompound root=new NBTTagCompound(),firework=new NBTTagCompound();firework.setByte("Flight",(byte)2);NBTTagList stars=new NBTTagList();NBTTagCompound star=new NBTTagCompound();star.setIntArray("Colors",new int[]{0x123456});stars.appendTag(star);firework.setTag("Explosions",stars);root.setTag("Fireworks",firework);s.setTagCompound(root);return s;}
 public static void main(String[] args)throws Exception {
  Crossbow140Fixture.main(args);World base=BountifulMechanicsFixture120.initialize();sun.misc.Unsafe unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);SpawnWorld w=(SpawnWorld)unsafe.allocateInstance(SpawnWorld.class);
  for(Field f:World.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(w,f.get(base));}
  field(World.class,"worldInfo").set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));field(World.class,"worldAccesses").set(w,new ArrayList());w.provider.worldObj=w;w.accepted=new ArrayList<Entity>();
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);ItemCrossbow item=new ItemCrossbow();cpw.mods.fml.common.registry.GameRegistry.registerItem(item,"fixture_rocket143");
  ItemStack weapon=new ItemStack(item);p.inventory.mainInventory[0]=weapon;p.inventory.mainInventory[2]=rockets(4);
  check(!ItemCrossbow.load(p,weapon),"inventory rockets alone are not ammunition: hold them");
  for(int mask=0;mask<8;mask++){
   weapon=new ItemStack(item);weapon.addEnchantment(CrossbowEnchantments140.multishot,1);p.inventory.mainInventory[0]=weapon;ItemStack ammo=rockets(2);OffhandHooks.setOffhand(p,ammo);
   check(ItemCrossbow.load(p,weapon)&&ammo.stackSize==1,"one rocket loads volley");check(ItemCrossbow.loadedProjectile(weapon).getItem()==Items.fireworks,"persisted rocket recognized");
   w.accepted.clear();w.attempt=0;w.mask=mask;check(ItemCrossbow.fire(weapon,w,p,null)==(mask!=0),"partial accepted spawn result");int count=Integer.bitCount(mask);check(weapon.getItemDamage()==3*count,"three durability per accepted rocket");
   weapon=ItemStack.loadItemStackFromNBT(weapon.writeToNBT(new NBTTagCompound()));if(mask!=7){w.attempt=0;w.mask=7;check(ItemCrossbow.fire(weapon,w,p,null),"retry saved pending rocket volley");}
   check(w.accepted.size()==3&&weapon.getItemDamage()==9&&!ItemCrossbow.isCharged(weapon),"no duplicate spawned rockets after retry");
   for(Entity e:w.accepted){check(e instanceof EntityCrossbowRocket143,"real firework entity");EntityCrossbowRocket143 r=(EntityCrossbowRocket143)e;check(r.getFireworkItem().getTagCompound().getCompoundTag("Fireworks").getByte("Flight")==2,"flight data preserved");check(r.getOwner()==p,"owner attribution");}
  }
  weapon=new ItemStack(item);OffhandHooks.setOffhand(p,weapon);p.inventory.mainInventory[0]=rockets(2);check(ItemCrossbow.hasAmmunition(p)&&ItemCrossbow.load(p,weapon),"offhand crossbow uses main-hand rocket");check(p.inventory.mainInventory[0].stackSize==1,"main-hand ammunition consumption");
  item.registerIcons(new net.minecraft.client.renderer.texture.IIconRegister(){public net.minecraft.util.IIcon registerIcon(final String name){return (net.minecraft.util.IIcon)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{net.minecraft.util.IIcon.class},new InvocationHandler(){public Object invoke(Object proxy,Method method,Object[] values){if(method.getReturnType()==String.class)return name;if(method.getReturnType()==float.class)return 0F;if(method.getReturnType()==int.class)return 16;return null;}});}});
  check(item.getIconIndex(weapon).getIconName().endsWith("crossbow_firework"),"distinct loaded rocket sprite");
  System.out.println("PASS crossbow rocket143 integration: "+checks);
 }
 static final class SpawnWorld extends World {
  List<Entity> accepted;int attempt,mask;SpawnWorld(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
  public boolean spawnEntityInWorld(Entity e){boolean ok=(mask&(1<<attempt++))!=0;if(ok)accepted.add(e);return ok;}
  public net.minecraft.block.Block getBlock(int x,int y,int z){return Blocks.air;}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}public Entity getEntityByID(int id){return null;}
 }
}
