package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.init.Items;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.*;

/** Own content identities; no generator or external Nether mod lookup. */
public final class NetherWoodContent116 {
    public static final Family CRIMSON=new Family("crimson"),WARPED=new Family("warped");
    public static final Family[] FAMILIES={CRIMSON,WARPED};
    private static boolean registered;
    public static final class Family {
        public final String name;
        public final Block stem,hyphae,strippedStem,strippedHyphae;
        public final Backport120WoodFamily wood;
        private Family(String name){
            this.name=name;
            stem=new BlockBackport120Wood(name+"_stem",name+"_stem",false);
            hyphae=new BlockBackport120Wood(name+"_hyphae",name+"_stem",true);
            strippedStem=new BlockBackport120Wood("stripped_"+name+"_stem","stripped_"+name+"_stem",false);
            strippedHyphae=new BlockBackport120Wood("stripped_"+name+"_hyphae","stripped_"+name+"_stem",true);
            wood=new Backport120WoodFamily(name,new BlockBackport120Simple(NetherWoodMaterial116.MATERIAL,name+"_planks",2,3,BuildingSounds116.NETHER_WOOD,"axe",0),name+"_planks",name+"_planks");
        }
        public Block[] blocks(){return new Block[]{stem,hyphae,strippedStem,strippedHyphae,wood.planks,wood.stairs,wood.slab,wood.doubleSlab,wood.fence,wood.fenceGate,wood.doorBlock,wood.trapdoor,wood.button,wood.pressurePlate,wood.standingSign,wood.wallSign,wood.hangingSignBlock,wood.wallHangingSignBlock};}
        public boolean sign(Block b){return b==wood.standingSign||b==wood.wallSign||b==wood.hangingSignBlock||b==wood.wallHangingSignBlock;}
    }
    public static void register(){
        if(registered)return;
        List recipes=net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
        int firstRecipe=recipes.size();
        for(Family f:FAMILIES){
            GameRegistry.registerBlock(f.stem,"backport_1_20_"+f.name+"_stem");GameRegistry.registerBlock(f.hyphae,"backport_1_20_"+f.name+"_hyphae");
            GameRegistry.registerBlock(f.strippedStem,"backport_1_20_stripped_"+f.name+"_stem");GameRegistry.registerBlock(f.strippedHyphae,"backport_1_20_stripped_"+f.name+"_hyphae");
            f.wood.register();f.wood.addRecipes(Backport120Content.chain,f.strippedStem,f.stem,f.hyphae,f.strippedStem,f.strippedHyphae);
            GameRegistry.addRecipe(new ItemStack(f.hyphae,3),"SS","SS",'S',f.stem);
            GameRegistry.addRecipe(new ItemStack(f.strippedHyphae,3),"SS","SS",'S',f.strippedStem);
            GameRegistry.addRecipe(new ItemStack(Items.stick,4),"P","P",'P',f.wood.planks);
            OreDictionary.registerOre("plankWood",f.wood.planks);
            for(Block log:new Block[]{f.stem,f.hyphae,f.strippedStem,f.strippedHyphae})OreDictionary.registerOre("logWood",new ItemStack(log,1,OreDictionary.WILDCARD_VALUE));
            for(Block b:f.blocks()){b.setStepSound(BuildingSounds116.NETHER_WOOD);NetherWoodMaterial116.markNonFuel(Item.getItemFromBlock(b));}
            NetherWoodMaterial116.markNonFuel(f.wood.door);NetherWoodMaterial116.markNonFuel(f.wood.sign);NetherWoodMaterial116.markNonFuel(f.wood.hangingSign);
        }
        // Species-specific recipes must precede Forge's generic plankWood recipes.
        List ownRecipes=new ArrayList(recipes.subList(firstRecipe,recipes.size()));
        recipes.subList(firstRecipe,recipes.size()).clear();
        recipes.addAll(0,ownRecipes);
        registered=true;
    }
    public static int fuelTime(int vanilla,ItemStack stack){return NetherWoodMaterial116.fuelTime(vanilla,stack);}
    public static Family signFamily(Block block){for(Family f:FAMILIES)if(f.sign(block))return f;return null;}
    private NetherWoodContent116(){}
}
