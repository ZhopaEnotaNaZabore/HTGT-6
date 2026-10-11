import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockColoredBed120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.world.World;
import java.lang.reflect.*;
import java.util.*;

public final class Placement105Fixture {
 static int checks;
 public static void main(String[] args)throws Exception {
  World template=BountifulMechanicsFixture120.initialize();
  for(int size=2;size<=3;size++)for(int width=1;width<=size;width++)for(int height=1;height<=size;height++) {
   ItemStack[] ingredients=new ItemStack[width*height];Arrays.fill(ingredients,new ItemStack(Items.stick));
   ShapedRecipes recipe=new ShapedRecipes(width,height,ingredients,new ItemStack(Items.apple));
   CraftingManager.getInstance().getRecipeList().add(recipe);
   ItemStack[] stock=new ItemStack[36];stock[0]=new ItemStack(Items.stick,64);
   RecipePlacement112.Plan plan=RecipePlacement112.plan(stock,new ItemStack[size*size],RecipeBookRegistry112.byRecipe(recipe),size,false);
   check(plan!=null,"shaped plan exists");
   for(int y=0;y<size;y++)for(int x=0;x<size;x++) {
    boolean expected=x>=(size-width)/2&&x<(size-width)/2+width&&y>=(size-height)/2&&y<(size-height)/2+height;
    check((plan.grid[y*size+x]!=null)==expected,"centered rectangle "+width+"x"+height+" in "+size);
   }
   InventoryCrafting matrix=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},size,size);
   for(int i=0;i<plan.grid.length;i++)matrix.setInventorySlotContents(i,plan.grid[i]);
   check(recipe.matches(matrix,template),"centered recipe matches vanilla");
  }
  // Empty cells of shaped recipes remain empty after centering.
  ShapedRecipes hole=new ShapedRecipes(1,3,new ItemStack[]{new ItemStack(Items.stick),null,new ItemStack(Items.stick)},new ItemStack(Items.apple));
  CraftingManager.getInstance().getRecipeList().add(hole);ItemStack[] stock=new ItemStack[36];stock[0]=new ItemStack(Items.stick,8);
  RecipePlacement112.Plan plan=RecipePlacement112.plan(stock,new ItemStack[9],RecipeBookRegistry112.byRecipe(hole),3,true);
  check(plan.grid[1].stackSize==4&&plan.grid[4]==null&&plan.grid[7].stackSize==4,"centered shaped holes and Shift");

  Field uf=Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");uf.setAccessible(true);Object unsafe=uf.get(null);
  TestWorld world=(TestWorld)unsafe.getClass().getMethod("allocateInstance",Class.class).invoke(unsafe,TestWorld.class);
  for(Class<?> type=World.class;type!=Object.class;type=type.getSuperclass())for(Field f:type.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);f.set(world,f.get(template));}
  world.cells=new HashMap<String,Object[]>();
  Field info=World.class.getDeclaredField("worldInfo");info.setAccessible(true);info.set(world,new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()));
  Field providerWorld=net.minecraft.world.WorldProvider.class.getDeclaredField("worldObj");providerWorld.setAccessible(true);providerWorld.set(world.provider,world);
  Constructor<?> ctor=Class.forName("LlamaInteraction77Fixture$Player").getDeclaredConstructor(World.class);ctor.setAccessible(true);EntityPlayer player=(EntityPlayer)ctor.newInstance(world);
  for(int color=0;color<16;color++)for(int facing=0;facing<4;facing++) {
   BlockColoredBed120 bed=new BlockColoredBed120("fixture",color);ItemBlockColoredBed120 item=new ItemBlockColoredBed120(bed);
   int dx=facing==1?-1:facing==3?1:0,dz=facing==0?1:facing==2?-1:0;player.rotationYaw=facing*90;
   world.cells.clear();world.reject=null;world.setBlock(0,9,0,Blocks.stone,0,2);world.setBlock(dx,9,dz,Blocks.stone,0,2);
   ItemStack stack=new ItemStack(item,2);player.capabilities.isCreativeMode=false;
   check(item.onItemUse(stack,player,world,0,9,0,1,0,0,0)&&stack.stackSize==1,"survival bed success");
   check(world.getBlock(0,10,0)==bed&&world.getBlockMetadata(0,10,0)==facing&&world.getBlock(dx,10,dz)==bed&&world.getBlockMetadata(dx,10,dz)==(facing|8),"both halves facing and color");
   world.setBlock(0,10,0,Blocks.air,0,2);world.setBlock(dx,10,dz,Blocks.air,0,2);world.reject=dx+",10,"+dz;
   check(!item.onItemUse(stack,player,world,0,9,0,1,0,0,0)&&stack.stackSize==1&&world.getBlock(0,10,0)==Blocks.air,"head failure undoes foot without item loss");
   world.reject="0,10,0";
   check(!item.onItemUse(stack,player,world,0,9,0,1,0,0,0)&&world.getBlock(dx,10,dz)==Blocks.air&&stack.stackSize==1,"foot rejection leaves head and item untouched");
   world.reject=null;player.capabilities.isCreativeMode=true;
   check(item.onItemUse(stack,player,world,0,9,0,1,0,0,0)&&stack.stackSize==1,"creative preserves item");
   world.cells.clear();world.isRemote=true;
   check(item.onItemUse(stack,player,world,0,9,0,1,0,0,0)&&world.cells.isEmpty()&&stack.stackSize==1,"client does not mutate");world.isRemote=false;
   stack.stackSize=0;check(!item.onItemUse(stack,player,world,0,9,0,1,0,0,0),"empty stack rejected");
  }
  System.out.println("PASS placement105: "+checks+" checks, shaped dimensions/holes and 16 bed colors x 4 facings, rejected writes, creative/client/empty guards");
 }
 static void check(boolean value,String name){if(!value)throw new AssertionError(name);checks++;}
 public static final class TestWorld extends World {
  Map<String,Object[]> cells;String reject;
  private TestWorld(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
  public Block getBlock(int x,int y,int z){Object[] c=cells.get(x+","+y+","+z);return c==null?Blocks.air:(Block)c[0];}
  public int getBlockMetadata(int x,int y,int z){Object[] c=cells.get(x+","+y+","+z);return c==null?0:(Integer)c[1];}
  public boolean setBlock(int x,int y,int z,Block block,int meta,int flags){String key=x+","+y+","+z;if(key.equals(reject))return false;cells.put(key,new Object[]{block,meta});return true;}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected int func_152379_p(){return 0;}
  public net.minecraft.entity.Entity getEntityByID(int id){return null;}
 }
}
