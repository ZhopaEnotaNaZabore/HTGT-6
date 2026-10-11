import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemBlockShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Recipes.Exploration.RecipeShulkerColor112;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import java.lang.reflect.*;

public final class Shulker85Fixture {
 public static void main(String[] args)throws Exception {
  World world=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);
  info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Constructor<?> pc=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);pc.setAccessible(true);
  EntityPlayer player=(EntityPlayer)pc.newInstance(world);
  ExplorationContent112.register();
  net.minecraft.inventory.InventoryCrafting base=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
  base.setInventorySlotContents(1,new ItemStack(ExplorationContent112.shulkerShell));
  base.setInventorySlotContents(4,new ItemStack(Blocks.chest));
  base.setInventorySlotContents(7,new ItemStack(ExplorationContent112.shulkerShell));
  ItemStack crafted=net.minecraft.item.crafting.CraftingManager.getInstance().findMatchingRecipe(base,world);
  check(crafted!=null&&crafted.getItem()==Item.getItemFromBlock(ExplorationContent112.undyedShulkerBox),"registered base recipe is neutral");
  for(int color=0;color<16;color++) {
   ItemStack box=new ItemStack(ExplorationContent112.shulkerBoxes[color]);
   NBTTagCompound root=new NBTTagCompound(),tile=new NBTTagCompound();
   NBTTagList contents=new NBTTagList();NBTTagCompound entry=new NBTTagCompound();
   new ItemStack(Items.diamond,23).writeToNBT(entry);entry.setByte("Slot",(byte)4);contents.appendTag(entry);
   tile.setTag("Items",contents);tile.setString("Lock","fixture");root.setTag("BlockEntityTag",tile);
   box.setTagCompound(root);box.setStackDisplayName("Stored gems");NBTTagCompound before=(NBTTagCompound)box.getTagCompound().copy();
   player.inventory.setInventorySlotContents(0,box);world.setBlock(1,2,3,Blocks.cauldron,3,3);
   ItemBlockShulkerBox112 item=(ItemBlockShulkerBox112)box.getItem();
   check(item.onItemUseFirst(box,player,world,1,2,3,1,0,0,0),"wash handled");
   ItemStack washed=player.getHeldItem();
   check(washed.getItem()==Item.getItemFromBlock(ExplorationContent112.undyedShulkerBox),"neutral output");
   check(before.equals(washed.getTagCompound())&&world.getBlockMetadata(1,2,3)==2,"NBT and one water level");
   com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.TileEntityShulkerBox112 saved=new com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.TileEntityShulkerBox112();
   saved.readInventory(washed.getTagCompound().getCompoundTag("BlockEntityTag"));
   saved.setCustomName(washed.getDisplayName());
   ItemStack portable=ExplorationContent112.undyedShulkerBox.portableStack(saved);
   check(portable.getItem()==washed.getItem()&&saved.getStackInSlot(4).stackSize==23&&portable.hasDisplayName(),"neutral portable inventory round trip");
   check(!((ItemBlockShulkerBox112)washed.getItem()).onItemUseFirst(washed,player,world,1,2,3,1,0,0,0),"neutral no-op");
   net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer p){return true;}},2,2);
   grid.setInventorySlotContents(0,washed);grid.setInventorySlotContents(1,new ItemStack(Items.dye,1,15-color));
   ItemStack dyed=new RecipeShulkerColor112().getCraftingResult(grid);
   check(dyed.getItem()==box.getItem()&&before.equals(dyed.getTagCompound()),"recolor keeps contents");
   dyed.getTagCompound().getCompoundTag("BlockEntityTag").setString("Lock","changed");
   check("fixture".equals(washed.getTagCompound().getCompoundTag("BlockEntityTag").getString("Lock")),"NBT deep copy");
   world.isRemote=true;player.inventory.setInventorySlotContents(0,box);
   check(!item.onItemUseFirst(box,player,world,1,2,3,1,0,0,0)&&player.getHeldItem()==box&&world.getBlockMetadata(1,2,3)==2,"client allows use packet without mutation");
   world.isRemote=false;world.setBlockMetadataWithNotify(1,2,3,0,3);
   check(!item.onItemUseFirst(box,player,world,1,2,3,1,0,0,0),"empty cauldron");
  }
  System.out.println("PASS 16 colors: wash, recolor, deep NBT, water, neutral/empty no-op, client authority");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
