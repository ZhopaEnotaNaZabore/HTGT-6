package com.mod.htgt6.minecraft1202.Module.Registry.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.BuildingSounds116;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import java.util.List;

/** Nether building items do not depend on a Nether terrain generator. */
public final class NetherMasonry116 {
    public static final Block chiseledNetherBricks=brick("chiseled_nether_bricks");
    public static final Block crackedNetherBricks=brick("cracked_nether_bricks");
    public static final BlockBackport120Button button=new BlockBackport120Button("polished_blackstone_button","polished_blackstone",false);
    public static final BlockBlackstonePressurePlate116 pressurePlate=new BlockBlackstonePressurePlate116();
    private static boolean registered;
    private static Block brick(String name) {
        return new BlockBackport120Simple(Material.rock,name,2,6,BuildingSounds116.NETHER_BRICKS,"pickaxe",0);
    }
    public static void register() {
        if (registered) return;
        GameRegistry.registerBlock(chiseledNetherBricks,"backport_1_20_chiseled_nether_bricks");
        GameRegistry.registerBlock(crackedNetherBricks,"backport_1_20_cracked_nether_bricks");
        GameRegistry.registerBlock(button,"backport_1_20_polished_blackstone_button");
        GameRegistry.registerBlock(pressurePlate,"backport_1_20_polished_blackstone_pressure_plate");
        GameRegistry.addRecipe(new ItemStack(chiseledNetherBricks),"S","S",'S',new ItemStack(Blocks.stone_slab,1,6));
        GameRegistry.addSmelting(Blocks.nether_brick,new ItemStack(crackedNetherBricks),.1F);
        GameRegistry.addRecipe(new ItemStack(Backport120Content.polishedBasalt,4),"BB","BB",'B',EtFVanillaGapContent120.basalt);
        GameRegistry.addShapelessRecipe(new ItemStack(button),EtFVanillaGapContent120.polishedBlackstone);
        GameRegistry.addRecipe(new ItemStack(pressurePlate),"BB",'B',EtFVanillaGapContent120.polishedBlackstone);
        registered=true;
    }
    public static void appendStonecutting(ItemStack input,List<ItemStack> out) {
        if(input==null || input.getItemDamage()!=0) return;
        Block block=Block.getBlockFromItem(input.getItem());
        if(block==EtFVanillaGapContent120.basalt) add(out,new ItemStack(Backport120Content.polishedBasalt));
        if(block==Blocks.nether_brick) add(out,new ItemStack(chiseledNetherBricks));
        if(block==Blocks.quartz_block) add(out,new ItemStack(EtFVanillaGapContent120.quartzBricks));
    }
    private static void add(List<ItemStack> out,ItemStack stack) {
        for(ItemStack existing:out) if(existing.isItemEqual(stack)) return;
        out.add(stack);
    }
    private NetherMasonry116() {}
}
