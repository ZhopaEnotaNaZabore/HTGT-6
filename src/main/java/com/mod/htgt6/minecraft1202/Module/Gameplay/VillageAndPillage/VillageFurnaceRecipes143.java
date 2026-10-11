package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

/** Exact vanilla metal-equipment recycling set; no invented blast recipes for arbitrary ore tags. */
public final class VillageFurnaceRecipes143 {
    private static final Item[] IRON={Items.iron_pickaxe,Items.iron_shovel,Items.iron_axe,Items.iron_hoe,Items.iron_sword,
        Items.iron_helmet,Items.iron_chestplate,Items.iron_leggings,Items.iron_boots,Items.iron_horse_armor,
        Items.chainmail_helmet,Items.chainmail_chestplate,Items.chainmail_leggings,Items.chainmail_boots};
    private static final Item[] GOLD={Items.golden_pickaxe,Items.golden_shovel,Items.golden_axe,Items.golden_hoe,Items.golden_sword,
        Items.golden_helmet,Items.golden_chestplate,Items.golden_leggings,Items.golden_boots,Items.golden_horse_armor};
    private VillageFurnaceRecipes143(){}
    public static void registerRecycling(){register(IRON,ExplorationContent112.ironNugget);register(GOLD,Items.gold_nugget);}
    private static void register(Item[] inputs,Item nugget){
        for(Item input:inputs){
            boolean incompatible=false,already=false;ItemStack output=new ItemStack(nugget);
            for(Object raw:FurnaceRecipes.smelting().getSmeltingList().entrySet()){
                Map.Entry entry=(Map.Entry)raw;ItemStack key=(ItemStack)entry.getKey();if(key.getItem()!=input)continue;
                if(!ItemStack.areItemStacksEqual((ItemStack)entry.getValue(),output))incompatible=true;
                if(key.getItemDamage()==OreDictionary.WILDCARD_VALUE)already=true;
            }
            // A pack's explicit override keeps ownership, including damage-specific variants.
            if(!incompatible&&!already)FurnaceRecipes.smelting().func_151394_a(new ItemStack(input,1,OreDictionary.WILDCARD_VALUE),output,.1F);
        }
    }
    /** Called after modern ItemBlocks exist. Preserve any pack-provided recipe. */
    public static void registerOreSmelting(){
        ore(Backport120Content.deepslateCoalOre,new ItemStack(Items.coal),.1F);
        ore(Backport120Content.deepslateDiamondOre,new ItemStack(Items.diamond),1F);
        ore(Backport120Content.deepslateEmeraldOre,new ItemStack(Items.emerald),1F);
        ore(Backport120Content.deepslateLapisOre,new ItemStack(Items.dye,1,4),.2F);
        ore(Backport120Content.deepslateRedstoneOre,new ItemStack(Items.redstone),.7F);
        ore(EtFVanillaGapContent120.netherGoldOre,new ItemStack(Items.gold_ingot),1F);
    }
    private static void ore(Block block,ItemStack output,float experience){
        Item item=Item.getItemFromBlock(block);
        if(item==null)return;
        ItemStack input=new ItemStack(item);
        if(FurnaceRecipes.smelting().getSmeltingResult(input)==null)
            FurnaceRecipes.smelting().func_151394_a(input,output,experience);
    }
    private static boolean oreBlock(Block block){
        return block==Blocks.iron_ore||block==Blocks.gold_ore||block==Blocks.coal_ore
            ||block==Blocks.diamond_ore||block==Blocks.emerald_ore||block==Blocks.redstone_ore
            ||block==Blocks.lit_redstone_ore||block==Blocks.lapis_ore||block==Blocks.quartz_ore
            ||block==Backport120Content.copperOre||block==Backport120Content.deepslateCopperOre
            ||block==Backport120Content.deepslateCoalOre||block==Backport120Content.deepslateIronOre
            ||block==Backport120Content.deepslateGoldOre||block==Backport120Content.deepslateRedstoneOre
            ||block==Backport120Content.deepslateEmeraldOre||block==Backport120Content.deepslateLapisOre
            ||block==Backport120Content.deepslateDiamondOre||block==EtFVanillaGapContent120.netherGoldOre
            ||block==EtFVanillaGapContent120.ancientDebris;
    }
    private static boolean contains(Item[] array,Item item){for(Item value:array)if(value==item)return true;return false;}
    public static boolean isBlastable(ItemStack input){
        if(input==null)return false;Item item=input.getItem();
        if(contains(IRON,item)||contains(GOLD,item)||item==Backport120Content.rawCopper||item==Backport120Content.rawIron||item==Backport120Content.rawGold)return true;
        Block block=Block.getBlockFromItem(item);if(oreBlock(block))return true;
        for(int id:OreDictionary.getOreIDs(input)){String name=OreDictionary.getOreName(id);if("oreIron".equals(name)||"oreGold".equals(name)||"oreCopper".equals(name))return true;}
        return false;
    }
}
