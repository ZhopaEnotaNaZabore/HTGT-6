import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionLoot120;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.InventoryBasic;
import java.util.*;
import java.io.*;
import java.util.zip.*;
public final class MansionLoot83Fixture {
 public static void main(String[] args)throws Exception{
  BountifulMechanicsFixture120.initialize();
  com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
  com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
  try(ZipFile jar=new ZipFile(args[0]);InputStream official=jar.getInputStream(jar.getEntry("data/minecraft/loot_tables/chests/woodland_mansion.json"));
      InputStream bundled=MansionLoot83Fixture.class.getResourceAsStream("/assets/htgt6/loot_tables/chests/woodland_mansion.json")){
   check(Arrays.equals(bytes(official),bytes(bundled)),"table differs from official 1.20.2");
  }
  Random random=new Random(83);int rareApples=0,books=0,min=99,max=0;boolean beetroot=false;
  for(int i=0;i<10000;i++){
   List<ItemStack> loot=WoodlandMansionLoot120.generate(random);min=Math.min(min,loot.size());max=Math.max(max,loot.size());
   for(ItemStack stack:loot){
    check(stack.getItem()!=ExplorationContent112.totemOfUndying&&stack.getItem()!=Items.emerald,"old fixed loot remains");
    check(stack.stackSize>0&&stack.stackSize<=8,"invalid count");
    if(stack.getItem()==Items.golden_apple&&stack.getItemDamage()==1)rareApples++;
    if(stack.getItem()==Items.enchanted_book){books++;check(Items.enchanted_book.func_92110_g(stack).tagCount()==1,"missing random enchantment");}
    if(stack.getItem()==com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent.beetrootSeeds)beetroot=true;
   }
  }
  check(min==5&&max==10,"pool roll ranges");check(rareApples>220&&rareApples<420,"rare apple weighting");check(books>1400&&books<1750&&beetroot,"books/seeds missing");
  InventoryBasic chest=new InventoryBasic("fixture",false,27);ItemStack occupied=new ItemStack(Items.diamond,17);chest.setInventorySlotContents(0,occupied);
  WoodlandMansionLoot120.fill(chest,new Random(4));check(chest.getStackInSlot(0)==occupied&&occupied.stackSize==17,"occupied slot overwritten");
  for(int i=0;i<27;i++)chest.setInventorySlotContents(i,new ItemStack(Items.diamond,17));
  WoodlandMansionLoot120.fill(chest,new Random(5));for(int i=0;i<27;i++)check(chest.getStackInSlot(i).getItem()==Items.diamond&&chest.getStackInSlot(i).stackSize==17,"full chest overwritten");
  InventoryBasic first=new InventoryBasic("fixture",false,27),second=new InventoryBasic("fixture",false,27);
  WoodlandMansionLoot120.fill(first,new Random(17));WoodlandMansionLoot120.fill(second,new Random(17));
  for(int i=0;i<27;i++)check(ItemStack.areItemStacksEqual(first.getStackInSlot(i),second.getStackInSlot(i)),"seed nondeterminism");
  System.out.println("PASS: exact reference JSON, 10000 rolls, counts/weights, enchantments, no totems, occupied/full chest and deterministic placement");
 }
 static byte[] bytes(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))>=0)out.write(buffer,0,n);return out.toByteArray();}
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
}
