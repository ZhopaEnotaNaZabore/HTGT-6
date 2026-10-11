package com.mod.htgt6.minecraft1202.Module.Recipes.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageWorkstationLogic120;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

/** Legacy 3x3 implementation of the geometric banner-pattern recipes added in 1.8. */
public final class RecipeBountifulBannerPattern implements IRecipe {
    private static final Pattern[] PATTERNS={
        p("bl","......#.."),p("br","........#"),p("tl","#........"),p("tr","..#......"),
        p("bs","......###"),p("ts","###......"),p("ls","#..#..#.."),p("rs","..#..#..#"),
        p("cs",".#..#..#."),p("ms","...###..."),p("drs","#...#...#"),p("dls","..#.#.#.."),
        p("ss","#.##.##.#"),p("cr","#.#.#.#.#"),p("sc",".#.###.#."),
        p("hh","######..."),p("hhb","...######"),p("vh","##.##.##."),p("vhr",".##.##.##"),
        p("bo","####.####"),p("mc","....#...."),p("mr",".#.#.#.#."),
        p("ld","##.#....."),p("rd",".##..#..."),p("lud",".....#.##"),p("rud","...#..##."),
        p("bt","....#..#."),p("tt",".#..#...."),p("bts","#.#.#...."),p("tts","....#.#.#"),
        p("gra","#.#.#..#."),p("gru",".#..#.#.#")
    };
    private Pattern matched;

    @Override public boolean matches(InventoryCrafting inv,World world){matched=find(inv);return matched!=null;}
    private Pattern find(InventoryCrafting inv){if(inv.getSizeInventory()!=9)return null;Pattern special=findSpecial(inv);if(special!=null)return special;for(Pattern pattern:PATTERNS){int bannerCount=0,color=-1;boolean valid=true;for(int slot=0;slot<9;slot++){ItemStack stack=inv.getStackInSlot(slot);boolean dye=pattern.mask.charAt(slot)=='#';if(dye){int found=VillageWorkstationLogic120.dyeColor(stack);if(found<0){valid=false;break;}if(color<0)color=found;else if(color!=found){valid=false;break;}}else if(stack!=null){if(stack.getItem()!=BountifulContent.banner||++bannerCount>1){valid=false;break;}NBTTagList old=patterns(stack);if(old.tagCount()>=6){valid=false;break;}}}if(valid&&bannerCount==1&&color>=0)return new Pattern(pattern.code,pattern.mask,color);}return null;}
    private Pattern findSpecial(InventoryCrafting inv){ItemStack banner=null,catalyst=null;int color=-1;for(int i=0;i<9;i++){ItemStack stack=inv.getStackInSlot(i);if(stack==null)continue;if(stack.getItem()==BountifulContent.banner&&banner==null)banner=stack;else if(VillageWorkstationLogic120.dyeColor(stack)>=0&&color<0)color=VillageWorkstationLogic120.dyeColor(stack);else if(catalyst==null)catalyst=stack;else return null;}if(banner==null||catalyst==null||color<0||patterns(banner).tagCount()>=6)return null;String code=null;if(catalyst.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.vine))code="cbo";else if(catalyst.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.brick_block))code="bri";else if(catalyst.getItem()==net.minecraft.init.Items.skull)code=catalyst.getItemDamage()==4?"cre":"sku";else if(catalyst.getItem()==net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.red_flower)&&catalyst.getItemDamage()==8)code="flo";else if(catalyst.getItem()==net.minecraft.init.Items.golden_apple&&catalyst.getItemDamage()==1)code="moj";return code==null?null:new Pattern(code,"",color);}
    @Override public ItemStack getCraftingResult(InventoryCrafting inv){Pattern pattern=find(inv);if(pattern==null)return null;ItemStack banner=null;for(int i=0;i<inv.getSizeInventory();i++){ItemStack stack=inv.getStackInSlot(i);if(stack!=null&&stack.getItem()==BountifulContent.banner){banner=stack.copy();break;}}if(banner==null)return null;banner.stackSize=1;NBTTagCompound root=banner.hasTagCompound()?(NBTTagCompound)banner.getTagCompound().copy():new NBTTagCompound();NBTTagCompound block=root.hasKey("BlockEntityTag")?root.getCompoundTag("BlockEntityTag"):new NBTTagCompound();NBTTagList list=block.hasKey("Patterns")?block.getTagList("Patterns",10):new NBTTagList();NBTTagCompound entry=new NBTTagCompound();entry.setString("Pattern",pattern.code);entry.setInteger("Color",pattern.color);list.appendTag(entry);block.setInteger("Base",banner.getItemDamage()&15);block.setTag("Patterns",list);root.setTag("BlockEntityTag",block);banner.setTagCompound(root);return banner;}
    private static NBTTagList patterns(ItemStack stack){if(!stack.hasTagCompound()||!stack.getTagCompound().hasKey("BlockEntityTag"))return new NBTTagList();NBTTagCompound block=stack.getTagCompound().getCompoundTag("BlockEntityTag");return block.hasKey("Patterns")?block.getTagList("Patterns",10):new NBTTagList();}
    @Override public int getRecipeSize(){return 10;} @Override public ItemStack getRecipeOutput(){return new ItemStack(BountifulContent.banner);}
    private static Pattern p(String code,String mask){return new Pattern(code,mask,-1);}
    private static final class Pattern{final String code,mask;final int color;Pattern(String code,String mask,int color){this.code=code;this.mask=mask;this.color=color;}}
}
