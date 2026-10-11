import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import com.mod.htgt6.minecraft1202.Module.Network.Inventory.PacketPlaceRecipe112;
import net.minecraft.entity.player.*;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import java.util.*;
import java.lang.reflect.*;
public final class RecipePlacement87Fixture {
 static RecipeBookRegistry112.Entry recipe(ItemStack... inputs){
  IRecipe r=new ShapelessRecipes(new ItemStack(Items.apple),Arrays.asList(inputs));
  CraftingManager.getInstance().getRecipeList().add(r);return RecipeBookRegistry112.byRecipe(r);
 }
 public static void main(String[] args)throws Exception {
  World world=BountifulMechanicsFixture120.initialize();
  RecipeBookRegistry112.Entry three=recipe(new ItemStack(Items.stick),new ItemStack(Items.diamond),new ItemStack(Items.coal));
  check(three.fits(2),"shapeless 3 fits 2x2");
  check(recipe(new ItemStack(Items.stick),new ItemStack(Items.diamond),new ItemStack(Items.coal),new ItemStack(Items.wheat)).fits(2),"shapeless 4 fits 2x2");
  check(!recipe(new ItemStack(Items.stick),new ItemStack(Items.diamond),new ItemStack(Items.coal),new ItemStack(Items.wheat),new ItemStack(Items.iron_ingot)).fits(2),"shapeless 5 rejected by 2x2");
  IRecipe shaped=new ShapedRecipes(3,1,new ItemStack[]{new ItemStack(Items.stick),new ItemStack(Items.stick),new ItemStack(Items.stick)},new ItemStack(Items.apple));
  CraftingManager.getInstance().getRecipeList().add(shaped);
  check(!RecipeBookRegistry112.byRecipe(shaped).fits(2),"shaped width still respected");
  ItemStack[] inventory=new ItemStack[36];inventory[0]=new ItemStack(Items.stick,5);inventory[1]=new ItemStack(Items.diamond,3);inventory[2]=new ItemStack(Items.coal,9);
  RecipePlacement112.Plan plan=RecipePlacement112.plan(inventory,new ItemStack[4],three,2,true);
  check(plan!=null&&plan.grid[2].stackSize==3&&plan.grid[3]==null,"shift maximum and 2x2 layout");
  check(inventory[0].stackSize==5&&plan.inventory[0].stackSize==2,"plan leaves inputs untouched");
  ItemStack[] split=new ItemStack[36];split[0]=new ItemStack(Items.stick,2);split[3]=new ItemStack(Items.stick,3);
  split[1]=new ItemStack(Items.diamond,5);split[2]=new ItemStack(Items.coal,5);
  plan=RecipePlacement112.plan(split,new ItemStack[4],three,2,true);
  check(plan.grid[0].stackSize==5&&plan.inventory[0]==null&&plan.inventory[3]==null,"Shift combines partial stacks");
  RecipeBookRegistry112.Entry greedy=recipe(new ItemStack(Items.dye,1,32767),new ItemStack(Items.dye,1,0));
  ItemStack[] dyes={new ItemStack(Items.dye,1,0),new ItemStack(Items.dye,1,1)};
  int[] selection=RecipePlacement112.chooseSources(dyes,greedy,1);
  check(selection!=null&&selection[0]==1&&selection[1]==0,"wildcard reassigned");
  for(int black=0;black<8;black++)for(int red=0;red<8;red++)for(int amount=1;amount<=7;amount++) {
   dyes[0]=new ItemStack(Items.dye,black,0);dyes[1]=new ItemStack(Items.dye,red,1);
   boolean expected=black>=2*amount || black>=amount&&red>=amount;
   check((RecipePlacement112.chooseSources(dyes,greedy,amount)!=null)==expected,"capacity matching");
  }
  ItemStack[] full=new ItemStack[36];for(int i=0;i<36;i++)full[i]=new ItemStack(Items.stone_axe);
  ItemStack[] occupied=new ItemStack[4];occupied[0]=new ItemStack(Items.diamond,4);
  check(RecipePlacement112.plan(full,occupied,three,2,false)==null&&occupied[0].stackSize==4,"full inventory refuses without loss");
  inventory[0]=null;
  check(RecipePlacement112.plan(inventory,occupied,three,2,false)==null&&inventory[1].stackSize==3&&occupied[0].stackSize==4,"missing ingredient preserves grid");
  occupied[0]=new ItemStack(Items.stick,2);occupied[0].setStackDisplayName("Retain NBT");
  plan=RecipePlacement112.plan(inventory,occupied,three,2,false);
  check(plan!=null&&plan.grid[0].hasDisplayName()&&occupied[0].stackSize==2,"grid ingredients reused with NBT");
  List list=CraftingManager.getInstance().getRecipeList();IRecipe replacement=new ShapelessRecipes(new ItemStack(Items.bread),Arrays.asList(new ItemStack(Items.wheat)));
  list.set(list.size()-1,replacement);
  check(RecipeBookRegistry112.byRecipe(replacement)!=null&&RecipeBookRegistry112.byRecipe(greedy.recipe)==null,"same-size replacement refresh");
  // Wire contract includes both the window and shift modifier.
  io.netty.buffer.ByteBuf buffer=io.netty.buffer.Unpooled.buffer();
  new PacketPlaceRecipe112(three.key,17,true).toBytes(buffer);PacketPlaceRecipe112 decoded=new PacketPlaceRecipe112();decoded.fromBytes(buffer);buffer.release();
  Field window=PacketPlaceRecipe112.class.getDeclaredField("window");window.setAccessible(true);
  Field all=PacketPlaceRecipe112.class.getDeclaredField("all");all.setAccessible(true);
  check(window.getInt(decoded)==17&&all.getBoolean(decoded),"packet window and shift");
  buffer=io.netty.buffer.Unpooled.buffer();cpw.mods.fml.common.network.ByteBufUtils.writeUTF8String(buffer,three.key);
  decoded=new PacketPlaceRecipe112();decoded.fromBytes(buffer);buffer.release();check(window.getInt(decoded)==-1,"legacy packet cannot target arbitrary window");
  server(world,three);
  System.out.println("PASS recipe placement: shapeless, Shift, 448 matching cases, NBT, no mutation on failure, cache, packets, server window guard and commit");
 }
 static void server(World world,RecipeBookRegistry112.Entry entry)throws Exception {
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()));
  Field pw=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");pw.setAccessible(true);pw.set(world.provider,world);
  Constructor<?> pc=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);pc.setAccessible(true);EntityPlayer original=(EntityPlayer)pc.newInstance(world);
  Class<?> unsafeClass=Class.forName("sun.misc.Unsafe");Field uf=unsafeClass.getDeclaredField("theUnsafe");uf.setAccessible(true);Object unsafe=uf.get(null);
  EntityPlayerMP player=(EntityPlayerMP)unsafeClass.getMethod("allocateInstance",Class.class).invoke(unsafe,EntityPlayerMP.class);
  for(Class<?> type=EntityPlayer.class;type!=Object.class;type=type.getSuperclass())for(Field f:type.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(player,f.get(original));}
  ContainerPlayer container=new ContainerPlayer(player.inventory,false,player);container.windowId=7;player.openContainer=player.inventoryContainer=container;
  player.inventory.mainInventory[0]=new ItemStack(Items.stick,2);player.inventory.mainInventory[1]=new ItemStack(Items.diamond,2);player.inventory.mainInventory[2]=new ItemStack(Items.coal,2);
  Class<?> progressClass=Class.forName(RecipeBookSystem112.class.getName()+"$Progress");Constructor<?> ctor=progressClass.getDeclaredConstructor();ctor.setAccessible(true);Object progress=ctor.newInstance();
  Field unlocked=progressClass.getDeclaredField("unlocked");unlocked.setAccessible(true);((Set)unlocked.get(progress)).add(entry.key);
  Class<?> requestClass=Class.forName(RecipeBookSystem112.class.getName()+"$PlacementRequest");Constructor<?> request=requestClass.getDeclaredConstructor(String.class,int.class,boolean.class);request.setAccessible(true);
  Method place=RecipeBookSystem112.class.getDeclaredMethod("place",EntityPlayerMP.class,progressClass,requestClass);place.setAccessible(true);
  place.invoke(null,player,progress,request.newInstance(entry.key,6,false));
  check(container.craftMatrix.getStackInSlot(0)==null&&player.inventory.mainInventory[0].stackSize==2,"stale request rejected before mutation");
  place.invoke(null,player,progress,request.newInstance(entry.key,7,false));
  check(container.craftMatrix.getStackInSlot(2).getItem()==Items.coal&&player.inventory.mainInventory[0].stackSize==1,"server commits valid plan");
  place.invoke(null,player,progress,request.newInstance(entry.key,7,false));
  check(container.craftMatrix.getStackInSlot(0).stackSize==2&&container.craftMatrix.getStackInSlot(2).stackSize==2&&player.inventory.mainInventory[0]==null,"repeat server click increases to two");
  place.invoke(null,player,progress,request.newInstance(entry.key,7,false));
  check(container.craftMatrix.getStackInSlot(0).stackSize==2,"exhausted click preserves grid");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
