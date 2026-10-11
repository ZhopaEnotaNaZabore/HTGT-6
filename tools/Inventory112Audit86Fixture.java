import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import com.mod.htgt6.minecraft1202.Module.Events.Combat.OffhandHooks;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.InventoryPlayer112;
import net.minecraft.entity.player.*;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import java.util.*;
import java.lang.reflect.*;

/** Audit 86 reproductions updated to regression assertions after the cycle 87 fixes. */
public final class Inventory112Audit86Fixture {
 public static void main(String[] args)throws Exception {
  World world=BountifulMechanicsFixture120.initialize();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);
  info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Constructor<?> pc=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);pc.setAccessible(true);
  EntityPlayer player=(EntityPlayer)pc.newInstance(world);
  Bridge inv=new Bridge(player);player.inventory=inv;OffhandHooks.initializeCompartments(inv);
  for(int slot=0;slot<41;slot++)OffhandHooks.setInventorySlotContents(inv,slot,new ItemStack(Items.diamond,slot+1));
  NBTTagList saved=OffhandHooks.writeToNBT(inv,new NBTTagList());
  Bridge loaded=new Bridge(player);OffhandHooks.readFromNBT(loaded,saved);
  check(saved.tagCount()==41,"all compartments saved");
  for(int slot=0;slot<41;slot++)check(OffhandHooks.getStackInSlot(loaded,slot).stackSize==slot+1,"NBT slot "+slot);
  check((saved.getCompoundTagAt(40).getByte("Slot")&255)==150,"offhand persistence slot");
  OffhandHooks.copyInventory(inv,loaded);loaded.hand[0].stackSize=1;
  check(inv.hand[0].stackSize==41,"copy independence");
  check(OffhandHooks.decrStackSize(inv,40,3).stackSize==3&&inv.hand[0].stackSize==38,"offhand split");
  System.out.println("PASS inventory algorithms: 41-slot NBT, slot 150, copy, offhand split");
  List recipes=CraftingManager.getInstance().getRecipeList();
  IRecipe shapeless=new ShapelessRecipes(new ItemStack(Items.apple),Arrays.asList(new ItemStack(Items.stick),new ItemStack(Items.diamond),new ItemStack(Items.coal)));
  recipes.add(shapeless);
  RecipeBookRegistry112.Entry descriptor=RecipeBookRegistry112.byRecipe(shapeless);
  check(descriptor.fits(2),"shapeless fits 2x2 after fix");
  net.minecraft.inventory.InventoryCrafting grid=new net.minecraft.inventory.InventoryCrafting(new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer p){return true;}},2,2);
  grid.setInventorySlotContents(0,new ItemStack(Items.stick));grid.setInventorySlotContents(1,new ItemStack(Items.diamond));grid.setInventorySlotContents(2,new ItemStack(Items.coal));
  check(shapeless.matches(grid,world),"recipe genuinely fits 2x2");
  System.out.println("PASS: valid 3-ingredient shapeless recipe accepted by 2x2 book");
  IRecipe replacement=new ShapelessRecipes(new ItemStack(Items.bread),Arrays.asList(new ItemStack(Items.wheat)));
  recipes.set(recipes.size()-1,replacement);
  check(RecipeBookRegistry112.byRecipe(replacement)!=null&&RecipeBookRegistry112.byRecipe(shapeless)==null,"stale same-size registry");
  System.out.println("PASS: same-size recipe replacement refreshes registry");
  recipes.add(shapeless);
  IRecipe greedy=new ShapelessRecipes(new ItemStack(Items.apple),Arrays.asList(new ItemStack(Items.dye,1,32767),new ItemStack(Items.dye,1,0)));
  recipes.add(greedy);descriptor=RecipeBookRegistry112.byRecipe(greedy);
  Arrays.fill(inv.mainInventory,null);inv.mainInventory[0]=new ItemStack(Items.dye,1,0);inv.mainInventory[1]=new ItemStack(Items.dye,1,1);
  int[] chosen=RecipePlacement112.chooseSources(inv.mainInventory,descriptor,1);
  check(chosen!=null&&chosen[0]==1&&chosen[1]==0,"wildcard allocation resolved");
  check(RecipeBookRegistry112.matches(inv.mainInventory[1],descriptor.ingredients.get(0).get(0))&&RecipeBookRegistry112.matches(inv.mainInventory[0],descriptor.ingredients.get(1).get(0)),"valid alternate allocation exists");
  System.out.println("PASS: wildcard + exact ingredients allocated correctly");
 }
 static class Bridge extends InventoryPlayer implements InventoryPlayer112 {
  ItemStack[] hand;List<ItemStack[]> all;Bridge(EntityPlayer p){super(p);}
  public ItemStack[] htgt6$getOffHandInventory(){return hand;}
  public List<ItemStack[]> htgt6$getAllInventories(){return all;}
  public void htgt6$setOffHandInventory(ItemStack[] value){hand=value;}
  public void htgt6$setAllInventories(List<ItemStack[]> value){all=value;}
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
