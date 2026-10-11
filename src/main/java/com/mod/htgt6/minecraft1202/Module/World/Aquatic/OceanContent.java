package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockCoralBlock120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelpPlant;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockMarinePlant;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockTurtleEgg120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockWallCoralFan120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockDeadCoralPlant120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockDriedKelp120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockConduit120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.TileEntityConduit120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockCoral120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockMarinePlant;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockTurtleEgg120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemSuspiciousStew120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemTreasureMapScroll120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemTrident;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemTurtleHelmet120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemBlockDeadCoral120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemDriedKelp120;
import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemAquaticFishBucket120;

import com.mod.htgt6.asm.HTGT6Config;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;

/** Blocks added by the backported Update Aquatic world generation. */
public final class OceanContent {
    public static final BlockKelp kelp = new BlockKelp();
    public static final Block kelpPlant = new BlockKelpPlant();
    public static final Block marinePlant = new BlockMarinePlant();
    public static final Block coralBlock = new BlockCoralBlock120();
    public static final Block deadCoral = new BlockDeadCoralPlant120(false);
    public static final Block deadCoralFan = new BlockDeadCoralPlant120(true);
    public static final BlockWallCoralFan120[] wallCoralFans = {
            new BlockWallCoralFan120("tube", 0), new BlockWallCoralFan120("brain", 1),
            new BlockWallCoralFan120("bubble", 2), new BlockWallCoralFan120("fire", 3),
            new BlockWallCoralFan120("horn", 4)
    };
    public static final BlockWallCoralFan120[] deadWallCoralFans = {
            new BlockWallCoralFan120("tube",0,true),new BlockWallCoralFan120("brain",1,true),
            new BlockWallCoralFan120("bubble",2,true),new BlockWallCoralFan120("fire",3,true),
            new BlockWallCoralFan120("horn",4,true)
    };
    public static final Block driedKelpBlock=new BlockDriedKelp120();
    public static final Item driedKelp=new ItemDriedKelp120();
    public static final Item fishBucket=new ItemAquaticFishBucket120();
    public static final Item nautilusShell=new Item().setUnlocalizedName("nautilus_shell").setTextureName("htgt6:nautilus_shell").setCreativeTab(CreativeTabs.tabMaterials);
    public static final Item phantomMembrane=new Item().setUnlocalizedName("phantom_membrane").setTextureName("htgt6:phantom_membrane").setCreativeTab(CreativeTabs.tabMaterials);
    public static final Block conduit=new BlockConduit120();
    public static final Block turtleEgg = new BlockTurtleEgg120();
    public static final Item trident = new ItemTrident();
    public static final Item treasureMapScroll = new ItemTreasureMapScroll120();
    public static final Item scute = new Item().setUnlocalizedName("scute")
            .setTextureName("htgt6:scute").setCreativeTab(CreativeTabs.tabMaterials);
    public static final ItemTurtleHelmet120 turtleHelmet = new ItemTurtleHelmet120();
    public static final int TURTLE_HELMET_SCUTE_COUNT=5;
    public static final Enchantment loyalty = new EnchantmentAquaticTrident(
            HTGT6Config.LOYALTY_ENCHANTMENT_ID, 5, EnchantmentAquaticTrident.Kind.LOYALTY).setName("loyalty");
    public static final Enchantment impaling = new EnchantmentAquaticTrident(
            HTGT6Config.IMPALING_ENCHANTMENT_ID, 2, EnchantmentAquaticTrident.Kind.IMPALING).setName("impaling");
    public static final Enchantment riptide = new EnchantmentAquaticTrident(
            HTGT6Config.RIPTIDE_ENCHANTMENT_ID, 2, EnchantmentAquaticTrident.Kind.RIPTIDE).setName("riptide");
    public static final Enchantment channeling = new EnchantmentAquaticTrident(
            HTGT6Config.CHANNELING_ENCHANTMENT_ID, 1, EnchantmentAquaticTrident.Kind.CHANNELING).setName("channeling");
    public static final Item heartOfTheSea = new Item().setUnlocalizedName("heart_of_the_sea")
            .setTextureName("htgt6:1_20_backport/heart_of_the_sea")
            .setCreativeTab(CreativeTabs.tabMaterials);
    public static final Item coastArmorTrim = new Item().setUnlocalizedName("coast_armor_trim_smithing_template")
            .setTextureName("htgt6:1_20_backport/coast_armor_trim_smithing_template")
            .setCreativeTab(CreativeTabs.tabMaterials);
    public static final Item suspiciousStew = new ItemSuspiciousStew120();
    private static int kelpRenderId = 1;
    private static int turtleEggRenderId = 0;

    public static void registerBlocks() {
        GameRegistry.registerBlock(kelp, "kelp");
        GameRegistry.registerBlock(kelpPlant, "kelp_plant");
        GameRegistry.registerBlock(marinePlant, ItemBlockMarinePlant.class, "marine_plant");
        GameRegistry.registerBlock(coralBlock, ItemBlockCoral120.class, "coral_block");
        GameRegistry.registerBlock(deadCoral,ItemBlockDeadCoral120.class,"dead_coral");
        GameRegistry.registerBlock(deadCoralFan,ItemBlockDeadCoral120.class,"dead_coral_fan");
        String[] fanNames = {"tube", "brain", "bubble", "fire", "horn"};
        for (int type = 0; type < wallCoralFans.length; type++) {
            GameRegistry.registerBlock(wallCoralFans[type], fanNames[type] + "_coral_wall_fan");
            GameRegistry.registerBlock(deadWallCoralFans[type], "dead_"+fanNames[type] + "_coral_wall_fan");
        }
        GameRegistry.registerBlock(driedKelpBlock,"dried_kelp_block");
        GameRegistry.registerItem(driedKelp,"dried_kelp");
        GameRegistry.registerItem(fishBucket,"fish_bucket");
        GameRegistry.registerItem(nautilusShell,"nautilus_shell");
        GameRegistry.registerItem(phantomMembrane,"phantom_membrane");
        GameRegistry.registerBlock(conduit,"conduit");
        GameRegistry.registerTileEntity(TileEntityConduit120.class,"htgt6:conduit");
        GameRegistry.registerBlock(turtleEgg, ItemBlockTurtleEgg120.class, "turtle_egg");
        GameRegistry.registerItem(trident, "trident");
        GameRegistry.registerItem(treasureMapScroll, "buried_treasure_map_scroll");
        GameRegistry.registerItem(scute, "scute");
        GameRegistry.registerItem(turtleHelmet, "turtle_helmet");
        GameRegistry.registerItem(heartOfTheSea, "heart_of_the_sea");
        GameRegistry.registerItem(coastArmorTrim, "coast_armor_trim_smithing_template");
        GameRegistry.registerItem(suspiciousStew, "suspicious_stew");
        GameRegistry.addRecipe(new ItemStack(turtleHelmet),"S S","SSS",'S',scute);
        GameRegistry.addSmelting(kelp,new ItemStack(driedKelp),.1F);
        GameRegistry.addRecipe(new ItemStack(driedKelpBlock),"KKK","KKK","KKK",'K',driedKelp);
        GameRegistry.addShapelessRecipe(new ItemStack(driedKelp,9),driedKelpBlock);
        GameRegistry.addRecipe(new ItemStack(conduit),"NNN","NHN","NNN",'N',nautilusShell,'H',heartOfTheSea);
    }

    public static int getKelpRenderId() { return kelpRenderId; }
    public static void setKelpRenderId(int renderId) { kelpRenderId = renderId; }
    public static int getTurtleEggRenderId() { return turtleEggRenderId; }
    public static void setTurtleEggRenderId(int renderId) { turtleEggRenderId = renderId; }

    public static boolean isWallCoralFan(Block block) {
        for (Block fan : wallCoralFans) if (fan == block) return true;
        for (Block fan : deadWallCoralFans) if (fan == block) return true;
        return false;
    }

    private OceanContent() { }
}
