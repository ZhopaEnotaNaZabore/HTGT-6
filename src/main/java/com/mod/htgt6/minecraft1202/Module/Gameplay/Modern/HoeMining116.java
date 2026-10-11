package com.mod.htgt6.minecraft1202.Module.Gameplay.Modern;
import net.minecraft.item.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import java.util.*;
/** Shared native-hoe behavior introduced in 1.16. */
public final class HoeMining116 {
    public static Item.ToolMaterial material(ItemHoe hoe){return Item.ToolMaterial.valueOf(hoe.getToolMaterialName());}
    public static boolean effective(Block block,int meta){return block.getMaterial()==Material.leaves||block==Blocks.hay_block||block==Blocks.sponge||block==com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.sponge||block==com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.netherWartBlock||"hoe".equals(block.getHarvestTool(meta));}
    public static float speed(ItemHoe hoe,ItemStack stack,Block block,int meta){return effective(block,meta)?material(hoe).getEfficiencyOnProperMaterial():1;}
    public static Set<String> classes(ItemHoe hoe,ItemStack stack){return Collections.singleton("hoe");}
    public static int level(ItemHoe hoe,ItemStack stack,String type){return "hoe".equals(type)?material(hoe).getHarvestLevel():-1;}
    public static boolean repair(ItemHoe hoe,ItemStack tool,ItemStack ingredient){
        if(ingredient==null)return false;Item.ToolMaterial material=material(hoe);
        if(material==Item.ToolMaterial.WOOD)for(ItemStack wood:net.minecraftforge.oredict.OreDictionary.getOres("plankWood"))if(net.minecraftforge.oredict.OreDictionary.itemMatches(wood,ingredient,false))return true;
        ItemStack repair=material.getRepairItemStack();return repair!=null&&net.minecraftforge.oredict.OreDictionary.itemMatches(repair,ingredient,false);
    }
    public static int enchantability(ItemHoe hoe){return material(hoe).getEnchantability();}
    public static boolean destroy(ItemHoe hoe,ItemStack stack,World w,Block block,int x,int y,int z,EntityLivingBase miner){if(!w.isRemote&&block.getBlockHardness(w,x,y,z)!=0)stack.damageItem(1,miner);return true;}
    public static boolean hit(ItemHoe hoe,ItemStack stack,EntityLivingBase target,EntityLivingBase attacker){if(!attacker.worldObj.isRemote)stack.damageItem(2,attacker);return true;}
    private HoeMining116(){}
}
