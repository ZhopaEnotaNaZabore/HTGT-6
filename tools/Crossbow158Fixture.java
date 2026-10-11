package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import net.minecraft.item.*;import net.minecraft.init.Items;import net.minecraft.nbt.*;import net.minecraft.util.IIcon;
public final class Crossbow158Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.Grid w=BountifulMechanicsFixture120.initialize();
  java.lang.reflect.Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
  java.lang.reflect.Field potions=net.minecraft.potion.Potion.class.getDeclaredField("potionTypes");unsafe.putObject(unsafe.staticFieldBase(potions),unsafe.staticFieldOffset(potions),java.util.Arrays.copyOf(net.minecraft.potion.Potion.potionTypes,65536));
  java.lang.reflect.Field info=net.minecraft.world.World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(w,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));w.provider.worldObj=w;
  ItemCrossbow item=new ItemCrossbow();item.registerIcons(name->(IIcon)java.lang.reflect.Proxy.newProxyInstance(Crossbow158Fixture.class.getClassLoader(),new Class[]{IIcon.class},(p,m,a)->m.getName().equals("getIconName")?name:null));
  ItemStack stack=new ItemStack(item);
  EntityPillager mob=new EntityPillager(w);mob.setCurrentItemOrArmor(0,stack);
  for(int ticks:new int[]{0,1,14,15,24,25}){mob.setCrossbowChargeTicks(ticks);expect(item.getLivingIcon(stack,mob),ticks==0?"standby":ticks<15?"pulling_0":ticks<25?"pulling_1":"pulling_2");}
  expect(item.getLivingIcon(stack.copy(),mob),"standby");
  Swimming115Fixture.Player p=new Swimming115Fixture.Player(w);p.inventory=new Crossbow140Fixture.Inventory(p);p.inventory.mainInventory[0]=stack;p.setItemInUse(stack,72000-15);
  expect(item.inventoryIcon(stack,p),"pulling_1");expect(item.inventoryIcon(stack.copy(),p),"standby");
  for(Item ammo:new Item[]{Items.arrow,Items.fireworks}){
   NBTTagCompound tag=new NBTTagCompound();tag.setBoolean("Charged",true);NBTTagList list=new NBTTagList();NBTTagCompound projectile=new NBTTagCompound();new ItemStack(ammo).writeToNBT(projectile);list.appendTag(projectile);tag.setTag("ChargedProjectiles",list);stack.setTagCompound(tag);
   String expected=ammo==Items.arrow?"arrow":"firework";
   expect(item.inventoryIcon(stack,p),expected);expect(item.inventoryIcon(stack.copy(),p),expected);expect(item.getLivingIcon(stack,mob),expected);expect(item.chargeIcon(stack,25),expected);
  }
  stack.setTagCompound(null);p.clearItemInUse();expect(item.inventoryIcon(stack,p),"standby");
  System.out.println("PASS crossbow158: "+checks+" inventory/player/pillager state checks");
 }
 static void expect(IIcon icon,String suffix){if(icon==null||!icon.getIconName().endsWith("crossbow_"+suffix))throw new AssertionError("expected "+suffix+" got "+(icon==null?null:icon.getIconName()));checks++;}
}
