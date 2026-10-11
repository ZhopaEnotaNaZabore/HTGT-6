import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.AnimalDispenser116;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.LeatherHorseHooks151;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.LeatherHorseArmor151;
import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityStrider116;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120.Grid;
import net.minecraft.init.*;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.*;
import net.minecraft.entity.passive.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
public final class AnimalDispenser116Fixture {
 static int checks,fallback;static void check(boolean b,String why){if(!b)throw new AssertionError(why);checks++;}
 public static void main(String[] args)throws Exception{
  final Grid w=B11605Fixture.initialize();w.loaded=true;LeatherHorseArmor151.register();
  BlockDispenser.dispenseBehaviorRegistry.putObject(Items.saddle,new IBehaviorDispenseItem(){public ItemStack dispense(IBlockSource src,ItemStack stack){fallback++;return stack;}});
  AnimalDispenser116.register();AnimalDispenser116.register();
  EntityStrider116 strider=new EntityStrider116(w);ItemStack saddles=new ItemStack(Items.saddle,3);check(AnimalDispenser116.equip(strider,saddles)&&strider.isSaddled()&&saddles.stackSize==2,"adult strider saddle consumes one");check(!AnimalDispenser116.equip(strider,saddles)&&saddles.stackSize==2,"already saddled no consumption");
  EntityStrider116 baby=new EntityStrider116(w);baby.setGrowingAge(-24000);check(!AnimalDispenser116.equip(baby,saddles),"baby cannot be saddled");
  EntityPig pig=new EntityPig(w);check(AnimalDispenser116.equip(pig,saddles)&&pig.getSaddled(),"native pig saddle");
  EntityHorse horse=new EntityHorse(w);check(!AnimalDispenser116.equip(horse,saddles),"untamed horse rejected");horse.setHorseTamed(true);check(AnimalDispenser116.equip(horse,saddles)&&horse.isHorseSaddled()&&AnimalDispenser116.inventory(horse).getStackInSlot(0).stackSize==1,"tamed horse gets persistent inventory saddle");
  for(Item armor:new Item[]{Items.iron_horse_armor,Items.golden_horse_armor,Items.diamond_horse_armor,LeatherHorseArmor151.ITEM}){
   EntityHorse h=new EntityHorse(w);h.setHorseTamed(true);ItemStack stack=new ItemStack(armor,2);stack.setStackDisplayName("preserved");
   check(AnimalDispenser116.equip(h,stack)&&stack.stackSize==1&&AnimalDispenser116.inventory(h).getStackInSlot(1).hasDisplayName(),"horse armor preserves NBT and consumes one");check(!AnimalDispenser116.equip(h,stack)&&stack.stackSize==1,"armor not overwritten");
   if(armor==LeatherHorseArmor151.ITEM)check(LeatherHorseHooks151.color(h)>=0&&h.getTotalArmorValue()==3,"leather adapter synced and protects");
   NBTTagCompound saved=new NBTTagCompound();h.writeEntityToNBT(saved);EntityHorse loaded=new EntityHorse(w);loaded.readEntityFromNBT(saved);check(AnimalDispenser116.inventory(loaded).getStackInSlot(1).getItem()==armor,"horse equipment survives NBT");
  }
  for(int type=0;type<5;type++){EntityHorse h=new EntityHorse(w);h.setHorseType(type);h.setHorseTamed(true);ItemStack chest=new ItemStack(Blocks.chest,2);boolean expected=type==1||type==2;
   check(AnimalDispenser116.equip(h,chest)==expected&&chest.stackSize==(expected?1:2),"chest only for donkey or mule");if(expected){check(h.isChested()&&AnimalDispenser116.inventory(h).getSizeInventory()==17,"chest actually expands inventory");check(!AnimalDispenser116.equip(h,chest),"second chest rejected");}}
  IBlockSource source=new IBlockSource(){public net.minecraft.world.World getWorld(){return w;}public double getX(){return .5;}public double getY(){return 80.5;}public double getZ(){return .5;}public int getXInt(){return 0;}public int getYInt(){return 80;}public int getZInt(){return 0;}public int getBlockMetadata(){return 5;}public net.minecraft.tileentity.TileEntity getBlockTileEntity(){return null;}};
  IBehaviorDispenseItem handler=(IBehaviorDispenseItem)BlockDispenser.dispenseBehaviorRegistry.getObject(Items.saddle);ItemStack stack=new ItemStack(Items.saddle,2);handler.dispense(source,stack);check(fallback==1&&stack.stackSize==2,"previous mod behavior retained with no target");
  EntityStrider116 near=new EntityStrider116(w);near.setPosition(1.5,80,.5);w.loadedEntityList.add(near);handler.dispense(source,stack);check(near.isSaddled()&&stack.stackSize==1&&fallback==1,"dispenser targets only facing block and consumes once");
  System.out.println("Animal dispenser 116: "+checks+" checks passed");
 }
}
