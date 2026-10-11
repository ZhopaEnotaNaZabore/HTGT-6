package com.mod.htgt6.minecraft1202.Module.Registry.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnBone;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnMagma;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnSimple;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnStructure;
import com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.BlockFrostburnStructureVoid;
import com.mod.htgt6.minecraft1202.Module.Items.Frostburn.ItemBlockFrostburnStructureVoid;
import com.mod.htgt6.minecraft1202.Module.World.Frostburn.FuturaBubbleColumn120;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Blocks and recipes introduced by Minecraft Java 1.10. */
public final class FrostburnContent {
    private static int jsonRenderId;
    public static final Block magmaBlock = new BlockFrostburnMagma();
    public static final Block netherWartBlock = new BlockFrostburnSimple(
            Material.grass, "nether_wart_block", "nether_wart_block", 1.0F, 1.0F, new com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.FrostburnSound120("wart_block"));
    public static final Block redNetherBrick = new BlockFrostburnSimple(
            Material.rock, "red_nether_brick", "red_nether_brick", 2.0F, 6.0F, new com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.FrostburnSound120("nether_bricks"));
    public static final Block boneBlock = new BlockFrostburnBone();
    public static final Block structureBlock = new BlockFrostburnStructure();
    public static final Block structureVoid = new BlockFrostburnStructureVoid();

    public static int getJsonRenderId() { return jsonRenderId; }
    public static void setJsonRenderId(int renderId) { jsonRenderId = renderId; }

    public static void register() {
        GameRegistry.registerBlock(magmaBlock, "frostburn_magma");
        GameRegistry.registerBlock(netherWartBlock, "frostburn_nether_wart_block");
        GameRegistry.registerBlock(redNetherBrick, "frostburn_red_nether_brick");
        GameRegistry.registerBlock(boneBlock, "frostburn_bone_block");
        GameRegistry.registerBlock(structureBlock, "frostburn_structure_block");
        GameRegistry.registerBlock(structureVoid, ItemBlockFrostburnStructureVoid.class,
                "frostburn_structure_void");
        GameRegistry.registerTileEntity(com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn.TileEntityStructure120.class,"htgt6:structure_block");

        FMLCommonHandler.instance().bus().register(new FuturaBubbleColumn120());

        GameRegistry.addRecipe(new ItemStack(magmaBlock), "MM", "MM", 'M', Items.magma_cream);
        GameRegistry.addShapelessRecipe(new ItemStack(netherWartBlock), Items.nether_wart,
                Items.nether_wart, Items.nether_wart, Items.nether_wart, Items.nether_wart,
                Items.nether_wart, Items.nether_wart, Items.nether_wart, Items.nether_wart);
        GameRegistry.addRecipe(new ItemStack(redNetherBrick), "NW", "WN",
                'N', Items.netherbrick, 'W', Items.nether_wart);
        GameRegistry.addRecipe(new ItemStack(boneBlock), "BBB", "BBB", "BBB",
                'B', new ItemStack(Items.dye, 1, 15));
        GameRegistry.addShapelessRecipe(new ItemStack(Items.dye, 9, 15), boneBlock);
    }

    private FrostburnContent() { }
}
