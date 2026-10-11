import com.mod.htgt6.minecraft1202.Module.Registry.Modern.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.oredict.OreDictionary;
import java.util.*;
public final class BlackstoneCrafting116Fixture {
    static int checks;
    static void check(boolean b,String message){if(!b)throw new AssertionError(message);checks++;}
    public static void main(String[] args)throws Exception {
        B11605Fixture.initialize();
        GameRegistry.registerBlock(EtFVanillaGapContent120.blackstone,"fixture_blackstone");
        GameRegistry.registerBlock(EtFVanillaGapContent120.polishedBlackstone,"fixture_polished_blackstone");
        GameRegistry.registerBlock(Backport120Content.cobbledDeepslate,"fixture_cobbled_deepslate");
        List recipes=CraftingManager.getInstance().getRecipeList();int before=recipes.size();
        int ores=OreDictionary.getOres("cobblestone").size();
        BlackstoneCrafting116.register();BlackstoneCrafting116.register();
        check(recipes.size()==before+7,"exactly seven recipes, idempotent registration");
        check(ores==OreDictionary.getOres("cobblestone").size(),"global ore dictionary unchanged");
        String[][] patterns={{"XXX"," S "," S "},{"XX","XS"," S"},{"XX"," S"," S"},{"X","S","S"},{"X","X","S"},{"XXX","X X","XXX"},{" S ","XXX"}};
        Item[] outputs={Items.stone_pickaxe,Items.stone_axe,Items.stone_hoe,Items.stone_shovel,Items.stone_sword,Item.getItemFromBlock(Blocks.furnace),Items.brewing_stand};
        ItemStack[] stones={new ItemStack(Blocks.cobblestone),new ItemStack(EtFVanillaGapContent120.blackstone),new ItemStack(Backport120Content.cobbledDeepslate)};
        InventoryCrafting grid=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},3,3);
        for(int r=0;r<7;r++) {
            IRecipe recipe=(IRecipe)recipes.get(before+r);String[] shape=patterns[r];int count=0;
            for(String row:shape)for(char c:row.toCharArray())if(c=='X')count++;
            int combinations=(int)Math.pow(3,count);
            for(int variant=0;variant<combinations;variant++)for(int mirror=0;mirror<2;mirror++)
            for(int ox=0;ox<=3-shape[0].length();ox++)for(int oy=0;oy<=3-shape.length;oy++) {
                for(int i=0;i<9;i++)grid.setInventorySlotContents(i,null);
                int code=variant,first=-1;
                for(int y=0;y<shape.length;y++)for(int x=0;x<shape[0].length();x++) {
                    char c=shape[y].charAt(mirror==0?x:shape[0].length()-1-x);int slot=(oy+y)*3+ox+x;
                    if(c=='X'){grid.setInventorySlotContents(slot,stones[code%3].copy());code/=3;first=slot;}
                    if(c=='S')grid.setInventorySlotContents(slot,new ItemStack(r==6?Items.blaze_rod:Items.stick));
                }
                check(recipe.matches(grid,null),"mixed recipe/offset/mirror "+r+":"+variant);
                ItemStack result=recipe.getCraftingResult(grid);check(result.getItem()==outputs[r]&&result.stackSize==1&&result.getItemDamage()==0,"output");
                result.stackSize=32;check(recipe.getCraftingResult(grid).stackSize==1,"result copy");
                ItemStack actual=CraftingManager.getInstance().findMatchingRecipe(grid,null);check(actual!=null&&actual.getItem()==outputs[r],"crafting manager integration");
                ItemStack saved=grid.getStackInSlot(first);
                grid.setInventorySlotContents(first,new ItemStack(EtFVanillaGapContent120.polishedBlackstone));check(!recipe.matches(grid,null),"polished is not tool material");
                grid.setInventorySlotContents(first,new ItemStack(saved.getItem(),1,1));check(!recipe.matches(grid,null),"invalid metadata rejected");
                grid.setInventorySlotContents(first,null);check(!recipe.matches(grid,null),"missing ingredient rejected");
                grid.setInventorySlotContents(first,saved);
                for(int i=0;i<9;i++)if(grid.getStackInSlot(i)==null){grid.setInventorySlotContents(i,new ItemStack(Items.diamond));check(!recipe.matches(grid,null),"extra ingredient rejected");grid.setInventorySlotContents(i,null);break;}
            }
        }
        System.out.println("Blackstone crafting 116: "+checks+" checks passed");
    }
}
