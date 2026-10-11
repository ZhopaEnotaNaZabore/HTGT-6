import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.BountifulMechanicsFixture120;
import com.mod.htgt6.minecraft1202.Module.Recipes.Inventory.*;
import net.minecraft.item.*;
import net.minecraft.init.Items;

public final class RecipeRepeat98Fixture {
 public static void main(String[] args)throws Exception {
  BountifulMechanicsFixture120.initialize();
  RecipeBookRegistry112.Entry recipe=RecipePlacement87Fixture.recipe(new ItemStack(Items.stick),new ItemStack(Items.coal));
  ItemStack[] inventory=new ItemStack[36],grid=new ItemStack[4];
  inventory[0]=new ItemStack(Items.stick,8);inventory[1]=new ItemStack(Items.coal,8);
  grid[0]=new ItemStack(Items.stick,2);grid[1]=new ItemStack(Items.coal,2);
  RecipePlacement112.Plan p=RecipePlacement112.plan(inventory,grid,recipe,2,false,true);
  check(p.grid[0].stackSize==3&&p.grid[1].stackSize==3&&p.inventory[0].stackSize==7,"repeat +1");
  check(grid[0].stackSize==2&&inventory[0].stackSize==8,"inputs untouched");
  p=RecipePlacement112.plan(inventory,grid,recipe,2,true,true);
  check(p.grid[0].stackSize==10&&p.grid[1].stackSize==10,"Shift fills available");
  grid[0].stackSize=4;
  p=RecipePlacement112.plan(inventory,grid,recipe,2,false,true);
  check(p.grid[0].stackSize==3&&p.grid[1].stackSize==3,"uneven stacks use minimum +1");
  inventory[1]=null;
  check(RecipePlacement112.plan(inventory,grid,recipe,2,false,true)==null,"scarce ingredient prevents collapse");
  check(RecipePlacement112.plan(inventory,grid,recipe,2,true,true)==null,"Shift also preserves exhausted grid");
  inventory[1]=new ItemStack(Items.coal,64);grid[0].stackSize=64;
  check(RecipePlacement112.plan(inventory,grid,recipe,2,false,true)==null,"full stack no-op");
  RecipeBookRegistry112.Entry tool=RecipePlacement87Fixture.recipe(new ItemStack(Items.wooden_pickaxe));
  grid=new ItemStack[4];grid[0]=new ItemStack(Items.wooden_pickaxe);
  check(RecipePlacement112.plan(inventory,grid,tool,2,false,true)==null,"unstackable ingredient no-op");
  System.out.println("PASS repeat placement: +1, Shift, uneven grid, exhausted ingredients, full/unstackable limits, no input mutation");
 }
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
}
