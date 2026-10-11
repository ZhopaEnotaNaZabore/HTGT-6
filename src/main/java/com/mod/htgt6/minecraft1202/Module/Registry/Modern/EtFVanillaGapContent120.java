package com.mod.htgt6.minecraft1202.Module.Registry.Modern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120PolishedBasalt;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBlackstoneSlab116;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlackstoneSlab116;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120ColoredConcrete;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120ConcretePowder;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120CopperFamily;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120DirtPath;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Button;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Door;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120EtFGapSimple;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Fence;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120FenceGate;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120HangingSign;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Honey;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120NetherGoldOre;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SoulLantern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120SoulTorch;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Stairs;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120PressurePlate;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Sign;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120TrapDoor;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Target;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120WitherRose;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120HoneyBottle;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120Door;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120HangingSign;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120Record;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBackport120Sign;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Colored;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120CopperFamily;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Items.Modern.ItemBlockBackport120Technical;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Vanilla content present in the installed Et Futurum build but previously absent from HTGT6.
 * Kept separate from the Futura-critical registry so ownership and later migrations stay explicit.
 */
public final class EtFVanillaGapContent120 {
    public static final Block ancientDebris=stone("ancient_debris","ancient_debris_side","ancient_debris_top",30,1200,3);
    public static final Block netheriteBlock=metal("netherite_block",50,1200,3);
    public static final Block netherGoldOre=new BlockBackport120NetherGoldOre();
    public static final Block basalt=new BlockBackport120PolishedBasalt("basalt");
    public static final Block blackstone=stone("blackstone","blackstone","blackstone_top",1.5F,6,0);
    public static final Block gildedBlackstone=new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120GildedBlackstone();
    public static final Block polishedBlackstone=stone("polished_blackstone","polished_blackstone",null,2,6,0);
    public static final BlockBackport120Slab polishedBlackstoneSlab=new BlockBlackstoneSlab116(false,"polished_blackstone",polishedBlackstone);
    public static final BlockBackport120Slab doublePolishedBlackstoneSlab=new BlockBlackstoneSlab116(true,"polished_blackstone",polishedBlackstone);
    public static final Block polishedBlackstoneBricks=stone("polished_blackstone_bricks","polished_blackstone_bricks",null,1.5F,6,0);
    public static final Block crackedPolishedBlackstoneBricks=stone("cracked_polished_blackstone_bricks","cracked_polished_blackstone_bricks",null,1.5F,6,0);
    public static final Block chiseledPolishedBlackstone=stone("chiseled_polished_blackstone","chiseled_polished_blackstone",null,1.5F,6,0);
    public static final Block cryingObsidian=new com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockCryingObsidian116();
    public static final Block soulSoil=new BlockBackport120EtFGapSimple(Material.ground,"soul_soil","soul_soil",null,
            0.5F,0.5F,Block.soundTypeSand,"shovel",0);
    public static final Block soulTorch=new BlockBackport120SoulTorch();
    public static final Block soulLantern=new BlockBackport120SoulLantern();
    public static final Block target=new BlockBackport120Target();
    public static final Block quartzBricks=stone("quartz_bricks","quartz_bricks",null,0.8F,0.8F,0);
    public static final Block smoothStone=stone("smooth_stone","minecraft:stone_slab_top",null,2,6,0);
    public static final Block smoothSandstone=stone("smooth_sandstone","minecraft:sandstone_top",null,2.0F,6.0F,0);
    public static final Block smoothSandstoneStairs=new BlockBackport120Stairs(
            smoothSandstone,"smooth_sandstone_stairs","smooth_sandstone");
    public static final BlockBackport120Slab smoothSandstoneSlab=new BlockBackport120Slab(
            false,"smooth_sandstone","smooth_sandstone",smoothSandstone,6.0F);
    public static final BlockBackport120Slab doubleSmoothSandstoneSlab=new BlockBackport120Slab(
            true,"smooth_sandstone","smooth_sandstone",smoothSandstone,6.0F);
    public static final Block smoothRedSandstone=stone("smooth_red_sandstone","red_sandstone_top",null,2.0F,6.0F,0);
    public static final Block smoothQuartz=stone("smooth_quartz","minecraft:quartz_block_bottom",null,2.0F,6.0F,0);
    public static final Block honeyBlock=new BlockBackport120Honey();
    public static final Block honeycombBlock=new BlockBackport120EtFGapSimple(Material.clay,"honeycomb_block","honeycomb_block",null,
            0.6F,0.6F,Block.soundTypeCloth,null,0);
    public static final Block concrete=new BlockBackport120ColoredConcrete();
    public static final BlockBackport120ConcretePowder concretePowder=new BlockBackport120ConcretePowder();
    public static final Block dirtPath=new BlockBackport120DirtPath();
    public static final Block witherRose=new BlockBackport120WitherRose();
    public static final Block bambooPlanks=new BlockBackport120EtFGapSimple(Material.wood,"bamboo_planks","bamboo_planks",null,
            2,3,Block.soundTypeWood,"axe",0);
    public static final Block bambooBlock=new BlockBackport120EtFGapSimple(Material.wood,"bamboo_block","bamboo_block","bamboo_block_top",
            2,3,Block.soundTypeWood,"axe",0);
    public static final Block strippedBambooBlock=new BlockBackport120EtFGapSimple(Material.wood,"stripped_bamboo_block","stripped_bamboo_block","stripped_bamboo_block_top",
            2,3,Block.soundTypeWood,"axe",0);
    public static final Block bambooMosaic=new BlockBackport120EtFGapSimple(Material.wood,"bamboo_mosaic","bamboo_mosaic",null,
            2,3,Block.soundTypeWood,"axe",0);
    private static final Backport120WoodFamily BAMBOO=
            new Backport120WoodFamily("bamboo",bambooPlanks,"bamboo_planks","bamboo_fence");
    public static final Block bambooStairs=BAMBOO.stairs;
    public static final BlockBackport120Slab bambooSlab=BAMBOO.slab,doubleBambooSlab=BAMBOO.doubleSlab;
    public static final Block bambooMosaicStairs=new BlockBackport120Stairs(bambooMosaic,"bamboo_mosaic_stairs","bamboo_mosaic","axe",0);
    public static final BlockBackport120Slab bambooMosaicSlab=new BlockBackport120Slab(false,"bamboo_mosaic","bamboo_mosaic",bambooMosaic,3,
            Material.wood,Block.soundTypeWood,"axe",0);
    public static final BlockBackport120Slab doubleBambooMosaicSlab=new BlockBackport120Slab(true,"bamboo_mosaic","bamboo_mosaic",bambooMosaic,3,
            Material.wood,Block.soundTypeWood,"axe",0);
    public static final Block bambooFence=BAMBOO.fence,bambooFenceGate=BAMBOO.fenceGate;
    public static final BlockBackport120Door bambooDoorBlock=BAMBOO.doorBlock;
    public static final Item bambooDoor=BAMBOO.door;
    public static final Block bambooTrapdoor=BAMBOO.trapdoor,bambooPressurePlate=BAMBOO.pressurePlate,
            bambooButton=BAMBOO.button;
    public static final BlockBackport120Sign bambooStandingSign=BAMBOO.standingSign,bambooWallSign=BAMBOO.wallSign;
    public static final Item bambooSign=BAMBOO.sign;
    public static final BlockBackport120HangingSign bambooHangingSignBlock=BAMBOO.hangingSignBlock,
            bambooWallHangingSignBlock=BAMBOO.wallHangingSignBlock;
    public static final Item bambooHangingSign=BAMBOO.hangingSign;
    public static final BlockBackport120CopperFamily copperBlocks=new BlockBackport120CopperFamily("copper",false);
    public static final BlockBackport120CopperFamily cutCopper=new BlockBackport120CopperFamily("cut_copper",false);
    public static final BlockBackport120CopperFamily chiseledCopper=new BlockBackport120CopperFamily("chiseled_copper",false);
    public static final BlockBackport120CopperFamily copperGrate=new BlockBackport120CopperFamily("copper_grate",true);

    public static final Item netheriteScrap=item("netherite_scrap",CreativeTabs.tabMaterials);
    public static final Item netheriteIngot=item("netherite_ingot",CreativeTabs.tabMaterials);
    public static final Item honeycomb=item("honeycomb",CreativeTabs.tabMaterials);
    public static final Item honeyBottle=new ItemBackport120HoneyBottle();
    public static final Item musicDiscPigstep=new ItemBackport120Record("pigstep");
    public static final Item musicDiscOtherside=new ItemBackport120Record("otherside");
    public static final Item whiteDye=item("white_dye",CreativeTabs.tabMaterials);
    public static final Item blueDye=item("blue_dye",CreativeTabs.tabMaterials);
    public static final Item brownDye=item("brown_dye",CreativeTabs.tabMaterials);
    public static final Item blackDye=item("black_dye",CreativeTabs.tabMaterials);

    static {
        polishedBlackstoneSlab.setSingle(polishedBlackstoneSlab);doublePolishedBlackstoneSlab.setSingle(polishedBlackstoneSlab);
        smoothSandstoneSlab.setSingle(smoothSandstoneSlab);
        doubleSmoothSandstoneSlab.setSingle(smoothSandstoneSlab);
        bambooMosaicSlab.setSingle(bambooMosaicSlab);doubleBambooMosaicSlab.setSingle(bambooMosaicSlab);
    }

    public static void register(){
        GameRegistry.registerBlock(polishedBlackstoneSlab,ItemBlackstoneSlab116.class,"backport_1_20_polished_blackstone_slab",polishedBlackstoneSlab,doublePolishedBlackstoneSlab,Boolean.FALSE);
        GameRegistry.registerBlock(doublePolishedBlackstoneSlab,ItemBlockBackport120Technical.class,"backport_1_20_double_polished_blackstone_slab");
        block(ancientDebris,"ancient_debris");block(netheriteBlock,"netherite_block");
        block(netherGoldOre,"nether_gold_ore");block(basalt,"basalt");block(blackstone,"blackstone");
        block(gildedBlackstone,"gilded_blackstone");block(polishedBlackstone,"polished_blackstone");
        block(polishedBlackstoneBricks,"polished_blackstone_bricks");
        block(crackedPolishedBlackstoneBricks,"cracked_polished_blackstone_bricks");
        block(chiseledPolishedBlackstone,"chiseled_polished_blackstone");block(cryingObsidian,"crying_obsidian");
        block(soulSoil,"soul_soil");block(soulTorch,"soul_torch");block(soulLantern,"soul_lantern");
        block(target,"target");block(quartzBricks,"quartz_bricks");
        block(smoothStone,"smooth_stone");block(smoothSandstone,"smooth_sandstone");
        block(smoothSandstoneStairs,"smooth_sandstone_stairs");
        GameRegistry.registerBlock(smoothSandstoneSlab,ItemBlockBackport120Slab.class,
                "backport_1_20_smooth_sandstone_slab",smoothSandstoneSlab,doubleSmoothSandstoneSlab,Boolean.FALSE);
        GameRegistry.registerBlock(doubleSmoothSandstoneSlab,ItemBlockBackport120Technical.class,
                "backport_1_20_double_smooth_sandstone_slab");
        block(smoothRedSandstone,"smooth_red_sandstone");block(smoothQuartz,"smooth_quartz");
        block(honeyBlock,"honey_block");block(honeycombBlock,"honeycomb_block");
        GameRegistry.registerBlock(concrete,ItemBlockBackport120Colored.class,"backport_1_20_concrete");
        GameRegistry.registerBlock(concretePowder,ItemBlockBackport120Colored.class,"backport_1_20_concrete_powder");
        concretePowder.setHardened(concrete);
        block(dirtPath,"dirt_path");block(witherRose,"wither_rose");
        BAMBOO.register();block(bambooBlock,"bamboo_block");
        block(strippedBambooBlock,"stripped_bamboo_block");block(bambooMosaic,"bamboo_mosaic");
        block(bambooMosaicStairs,"bamboo_mosaic_stairs");slab(bambooMosaicSlab,doubleBambooMosaicSlab,"bamboo_mosaic");
        copper(copperBlocks,"copper_blocks");copper(cutCopper,"cut_copper");
        copper(chiseledCopper,"chiseled_copper");copper(copperGrate,"copper_grate");

        item(netheriteScrap,"netherite_scrap");item(netheriteIngot,"netherite_ingot");
        item(honeycomb,"honeycomb");item(honeyBottle,"honey_bottle");
        item(musicDiscPigstep,"music_disc_pigstep");item(musicDiscOtherside,"music_disc_otherside");
        item(whiteDye,"white_dye");item(blueDye,"blue_dye");item(brownDye,"brown_dye");item(blackDye,"black_dye");

        OreDictionary.registerOre("scrapNetherite",netheriteScrap);
        OreDictionary.registerOre("ingotNetherite",netheriteIngot);
        OreDictionary.registerOre("blockNetherite",netheriteBlock);
        OreDictionary.registerOre("oreGold",netherGoldOre);
        OreDictionary.registerOre("plankWood",bambooPlanks);

        registerTargetRecipe116();
        registerNetheriteRecipes();
        GameRegistry.addRecipe(new ItemStack(polishedBlackstone,4),"BB","BB",'B',blackstone);
        GameRegistry.addRecipe(new ItemStack(polishedBlackstoneBricks,4),"PP","PP",'P',polishedBlackstone);
        GameRegistry.addSmelting(polishedBlackstoneBricks,new ItemStack(crackedPolishedBlackstoneBricks),0.1F);
        GameRegistry.addRecipe(new ItemStack(polishedBlackstoneSlab,6),"BBB",'B',polishedBlackstone);
        GameRegistry.addRecipe(new ItemStack(chiseledPolishedBlackstone),"B","B",'B',polishedBlackstoneSlab);
        GameRegistry.addRecipe(new ItemStack(quartzBricks,4),"QQ","QQ",'Q',Blocks.quartz_block);
        GameRegistry.addSmelting(Blocks.stone,new ItemStack(smoothStone),0.1F);
        GameRegistry.addSmelting(Blocks.sandstone,new ItemStack(smoothSandstone),0.1F);
        GameRegistry.addRecipe(new ItemStack(smoothSandstoneStairs,4),"S  ","SS ","SSS",'S',smoothSandstone);
        GameRegistry.addRecipe(new ItemStack(smoothSandstoneSlab,6),"SSS",'S',smoothSandstone);
        GameRegistry.addSmelting(new ItemStack(com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.redSandstone,1,0),
                new ItemStack(smoothRedSandstone),0.1F);
        GameRegistry.addSmelting(basalt,new ItemStack(Backport120Content.smoothBasalt),0.1F);
        GameRegistry.addRecipe(new ItemStack(honeyBlock),"HH","HH",'H',honeyBottle);
        GameRegistry.addShapelessRecipe(new ItemStack(honeyBottle,4),honeyBlock,Items.glass_bottle,Items.glass_bottle,Items.glass_bottle,Items.glass_bottle);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.sugar,3),honeyBottle);
        com.mod.htgt6.minecraft1202.Module.Registry.Modern.BuzzyBees115.register();
        GameRegistry.addRecipe(new ItemStack(honeycombBlock),"HH","HH",'H',honeycomb);
        GameRegistry.addRecipe(new ItemStack(bambooBlock),"BBB","BBB","BBB",'B',Backport120Content.bamboo);
        GameRegistry.addShapelessRecipe(new ItemStack(bambooPlanks,2),bambooBlock);
        GameRegistry.addShapelessRecipe(new ItemStack(bambooPlanks,2),strippedBambooBlock);
        BAMBOO.addRecipes(Backport120Content.chain,strippedBambooBlock);
        GameRegistry.addRecipe(new ItemStack(bambooMosaic),"S","S",'S',bambooSlab);
        GameRegistry.addRecipe(new ItemStack(bambooMosaicStairs,4),"M  ","MM ","MMM",'M',bambooMosaic);
        GameRegistry.addRecipe(new ItemStack(bambooMosaicSlab,6),"MMM",'M',bambooMosaic);
        GameRegistry.addRecipe(new ItemStack(copperBlocks),"CCC","CCC","CCC",'C',Backport120Content.copperIngot);
        GameRegistry.addShapelessRecipe(new ItemStack(Backport120Content.copperIngot,9),new ItemStack(copperBlocks,1,0));
        GameRegistry.addRecipe(new ItemStack(cutCopper,4),"CC","CC",'C',new ItemStack(copperBlocks,1,0));
        GameRegistry.addRecipe(new ItemStack(chiseledCopper),"C","C",'C',new ItemStack(cutCopper,1,0));
        GameRegistry.addRecipe(new ItemStack(copperGrate,4),"C C"," C ","C C",'C',Backport120Content.copperIngot);
        for(Block soil:new Block[]{soulSoil,Blocks.soul_sand})
            GameRegistry.addRecipe(new ItemStack(soulTorch,4),"C","S","O",'C',new ItemStack(Items.coal,1,32767),'S',Items.stick,'O',soil);
        GameRegistry.addRecipe(new ItemStack(soulLantern),"NNN","NTN","NNN",'N',Backport120Content.ironNugget,'T',soulTorch);
    }

    /** Called by register(); isolated to exercise the exact runtime recipes. */
    public static void registerNetheriteRecipes(){
        GameRegistry.addSmelting(ancientDebris,new ItemStack(netheriteScrap),2.0F);
        GameRegistry.addShapelessRecipe(new ItemStack(netheriteIngot),netheriteScrap,netheriteScrap,
                netheriteScrap,netheriteScrap,Items.gold_ingot,Items.gold_ingot,Items.gold_ingot,Items.gold_ingot);
        GameRegistry.addRecipe(new ItemStack(netheriteBlock),"NNN","NNN","NNN",'N',netheriteIngot);
        GameRegistry.addShapelessRecipe(new ItemStack(netheriteIngot,9),netheriteBlock);
    }

    private static Block stone(String name,String side,String end,float hardness,float resistance,int level){
        return new BlockBackport120EtFGapSimple(Material.rock,name,side,end,hardness,resistance,Block.soundTypePiston,"pickaxe",level);
    }
    private static Block metal(String name,float hardness,float resistance,int level){
        return new BlockBackport120EtFGapSimple(Material.iron,name,name,null,hardness,resistance,Block.soundTypeMetal,"pickaxe",level);
    }
    private static Item item(String name,CreativeTabs tab){return new Item().setUnlocalizedName("backport_1_20."+name)
            .setTextureName("htgt6:1_20_backport/"+name).setCreativeTab(tab);}
    private static void block(Block block,String name){GameRegistry.registerBlock(block,"backport_1_20_"+name);}
    private static void item(Item item,String name){GameRegistry.registerItem(item,"backport_1_20_"+name);}
    private static void copper(Block block,String name){GameRegistry.registerBlock(block,ItemBlockBackport120CopperFamily.class,"backport_1_20_"+name);}
    private static void slab(BlockBackport120Slab half,BlockBackport120Slab full,String name){
        GameRegistry.registerBlock(half,ItemBlockBackport120Slab.class,"backport_1_20_"+name+"_slab",half,full,Boolean.FALSE);
        GameRegistry.registerBlock(full,ItemBlockBackport120Technical.class,"backport_1_20_double_"+name+"_slab");
    }
    public static void registerTargetRecipe116(){
        GameRegistry.addRecipe(new ItemStack(target)," R ","RHR"," R ",'R',Items.redstone,'H',Blocks.hay_block);
    }

    private EtFVanillaGapContent120(){}
}
