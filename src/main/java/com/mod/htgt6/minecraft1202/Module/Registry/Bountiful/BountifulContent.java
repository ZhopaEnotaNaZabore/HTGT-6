package com.mod.htgt6.minecraft1202.Module.Registry.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBarrier;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulSeaLantern;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulBanner;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulFenceGate;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulFence;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulSponge;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulStone;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockBountifulWoodDoor;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockCoarseDirt;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockInvertedDaylightDetector;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockIronTrapdoor;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockPrismarine;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockRedSandstone;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockRedSandstoneSlab;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockRedSandstoneStairs;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.BlockSlime;
import com.mod.htgt6.minecraft1202.Module.Blocks.Bountiful.TileEntityBountifulBanner;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Bountiful.EnchantmentBountifulDepthStrider;
import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBlockBountifulMeta;
import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBlockBountifulTechnical;
import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBlockRedSandstoneSlab;
import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBountifulArmorStand;
import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBountifulBanner;
import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBountifulLeapingPotion;
import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBountifulWoodDoor;
import com.mod.htgt6.minecraft1202.Module.Recipes.Bountiful.RecipeBountifulBannerPattern;
import com.mod.htgt6.minecraft1202.Module.Recipes.Bountiful.RecipeBountifulBannerDuplicate;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSoup;

/** Registration and recipes for the first Minecraft Java 1.8 content phase. */
public final class BountifulContent {
    public static final Block stone = new BlockBountifulStone();
    public static final Block prismarine = new BlockPrismarine();
    public static final Block seaLantern = new BlockBountifulSeaLantern();
    public static final Block slimeBlock = new BlockSlime();
    public static final Block barrier = new BlockBarrier();
    public static final Block ironTrapdoor = new BlockIronTrapdoor();
    public static final Block sponge = new BlockBountifulSponge();
    public static final Block standingBanner = new BlockBountifulBanner(false);
    public static final Block wallBanner = new BlockBountifulBanner(true);
    public static final Block coarseDirt = new BlockCoarseDirt();
    public static final Block redSandstone = new BlockRedSandstone();
    public static final Block redSandstoneStairs = new BlockRedSandstoneStairs();
    public static final BlockSlab redSandstoneSlab = new BlockRedSandstoneSlab(false);
    public static final BlockSlab doubleRedSandstoneSlab = new BlockRedSandstoneSlab(true);
    public static final Block invertedDaylightDetector = new BlockInvertedDaylightDetector();
    public static final Item rawMutton = food("mutton_raw", 2, 0.3F);
    public static final Item cookedMutton = food("mutton_cooked", 6, 0.8F);
    public static final Item rawRabbit = food("rabbit_raw", 3, 0.3F);
    public static final Item cookedRabbit = food("rabbit_cooked", 5, 0.6F);
    public static final Item rabbitStew = new ItemSoup(10).setUnlocalizedName("rabbit_stew").setTextureName("htgt6:bountiful/rabbit_stew");
    public static final Item rabbitHide = item("rabbit_hide", CreativeTabs.tabMaterials);
    public static final Item rabbitFoot = item("rabbit_foot", CreativeTabs.tabBrewing)
            .setPotionEffect("+0+1-2+3&4-4+13");
    public static final Item armorStand = new ItemBountifulArmorStand();
    public static final Item banner = new ItemBountifulBanner();
    public static final Item prismarineShard = item("prismarine_shard", CreativeTabs.tabMaterials);
    public static final Item prismarineCrystals = item("prismarine_crystals", CreativeTabs.tabMaterials);
    public static final Item leapingPotion = new ItemBountifulLeapingPotion();
    public static final String[] WOODS={"spruce","birch","jungle","acacia","big_oak"};
    public static final Block[] woodFences=createFences(),woodFenceGates=createGates();
    public static final BlockBountifulWoodDoor[] woodDoors=createDoors();
    public static final Item[] woodDoorItems=createDoorItems();
    public static final net.minecraft.enchantment.Enchantment depthStrider=new EnchantmentBountifulDepthStrider(findEnchantmentId());

    public static void register() {
        installLeapingBrewing();
        GameRegistry.registerBlock(stone, ItemBlockBountifulMeta.class, "bountiful_stone");
        GameRegistry.registerBlock(prismarine, ItemBlockBountifulMeta.class, "prismarine");
        GameRegistry.registerBlock(seaLantern, "sea_lantern");
        GameRegistry.registerBlock(slimeBlock, "slime_block");
        GameRegistry.registerBlock(barrier, "barrier");
        GameRegistry.registerBlock(ironTrapdoor, "iron_trapdoor");
        GameRegistry.registerBlock(sponge, ItemBlockBountifulMeta.class, "bountiful_sponge");
        GameRegistry.registerBlock(standingBanner,ItemBlockBountifulTechnical.class,"bountiful_standing_banner");
        GameRegistry.registerBlock(wallBanner,ItemBlockBountifulTechnical.class,"bountiful_wall_banner");
        GameRegistry.registerBlock(coarseDirt,"coarse_dirt");
        GameRegistry.registerBlock(redSandstone,ItemBlockBountifulMeta.class,"red_sandstone");
        GameRegistry.registerBlock(redSandstoneStairs,"red_sandstone_stairs");
        GameRegistry.registerBlock(redSandstoneSlab,ItemBlockRedSandstoneSlab.class,"red_sandstone_slab",redSandstoneSlab,doubleRedSandstoneSlab,Boolean.FALSE);
        GameRegistry.registerBlock(doubleRedSandstoneSlab,ItemBlockBountifulTechnical.class,"double_red_sandstone_slab");
        GameRegistry.registerBlock(invertedDaylightDetector,"inverted_daylight_detector");
        GameRegistry.registerItem(rawMutton,"mutton_raw"); GameRegistry.registerItem(cookedMutton,"mutton_cooked");
        GameRegistry.registerItem(rawRabbit,"rabbit_raw"); GameRegistry.registerItem(cookedRabbit,"rabbit_cooked");
        GameRegistry.registerItem(rabbitStew,"rabbit_stew"); GameRegistry.registerItem(rabbitHide,"rabbit_hide");
        GameRegistry.registerItem(rabbitFoot,"rabbit_foot");
        GameRegistry.registerItem(armorStand,"bountiful_armor_stand");
        GameRegistry.registerItem(banner,"bountiful_banner");
        GameRegistry.registerItem(prismarineShard,"prismarine_shard");GameRegistry.registerItem(prismarineCrystals,"prismarine_crystals");
        GameRegistry.registerItem(leapingPotion,"bountiful_leaping_potion");
        for(int i=0;i<WOODS.length;i++){String name=publicWoodName(WOODS[i]);GameRegistry.registerBlock(woodFences[i],name+"_fence");GameRegistry.registerBlock(woodFenceGates[i],name+"_fence_gate");GameRegistry.registerBlock(woodDoors[i],ItemBlockBountifulTechnical.class,name+"_door_block");GameRegistry.registerItem(woodDoorItems[i],name+"_door");woodDoors[i].setDoorItem(woodDoorItems[i]);}
        for(int i=0;i<WOODS.length;i++){Blocks.fire.setFireInfo(woodFences[i],5,20);Blocks.fire.setFireInfo(woodFenceGates[i],5,20);Blocks.fire.setFireInfo(woodDoors[i],5,20);}
        GameRegistry.registerTileEntity(TileEntityBountifulBanner.class,"htgt6:BountifulBanner");

        GameRegistry.addShapelessRecipe(new ItemStack(stone, 1, 0),
                new ItemStack(stone, 1, 2), net.minecraft.init.Items.quartz);
        GameRegistry.addRecipe(new ItemStack(stone, 4, 1), "SS", "SS", 'S', new ItemStack(stone, 1, 0));
        GameRegistry.addRecipe(new ItemStack(stone, 2, 2), "CQ", "QC",
                'C', Blocks.cobblestone, 'Q', net.minecraft.init.Items.quartz);
        GameRegistry.addRecipe(new ItemStack(stone, 4, 3), "SS", "SS", 'S', new ItemStack(stone, 1, 2));
        GameRegistry.addShapelessRecipe(new ItemStack(stone, 2, 4),
                new ItemStack(stone, 1, 2), Blocks.cobblestone);
        GameRegistry.addRecipe(new ItemStack(stone, 4, 5), "SS", "SS", 'S', new ItemStack(stone, 1, 4));
        GameRegistry.addRecipe(new ItemStack(slimeBlock), "SSS", "SSS", "SSS", 'S', Items.slime_ball);
        GameRegistry.addShapelessRecipe(new ItemStack(Items.slime_ball, 9), slimeBlock);
        GameRegistry.addRecipe(new ItemStack(ironTrapdoor), "II", "II", 'I', Items.iron_ingot);
        GameRegistry.addSmelting(rawMutton,new ItemStack(cookedMutton),0.35F);
        GameRegistry.addSmelting(rawRabbit,new ItemStack(cookedRabbit),0.35F);
        GameRegistry.addSmelting(new ItemStack(sponge,1,1),new ItemStack(sponge,1,0),0.15F);
        GameRegistry.addShapelessRecipe(new ItemStack(rabbitStew),cookedRabbit,Items.carrot,
                Items.baked_potato,Blocks.brown_mushroom,Items.bowl);
        GameRegistry.addShapelessRecipe(new ItemStack(rabbitStew),cookedRabbit,Items.carrot,
                Items.baked_potato,Blocks.red_mushroom,Items.bowl);
        GameRegistry.addRecipe(new ItemStack(Items.leather),"HH","HH",'H',rabbitHide);
        GameRegistry.addRecipe(new ItemStack(armorStand), "SSS", " S ", "SLS", 'S', Items.stick, 'L', new ItemStack(Blocks.stone_slab,1,0));
        for(int color=0;color<16;color++)GameRegistry.addRecipe(new ItemStack(banner,1,color),"WWW","WWW"," S ",'W',new ItemStack(Blocks.wool,1,15-color),'S',Items.stick);
        GameRegistry.addRecipe(new RecipeBountifulBannerPattern());
        GameRegistry.addRecipe(new RecipeBountifulBannerDuplicate());
        GameRegistry.addShapelessRecipe(new ItemStack(coarseDirt,4),new ItemStack(Blocks.dirt,1,0),new ItemStack(Blocks.dirt,1,0),Blocks.gravel,Blocks.gravel);
        GameRegistry.addRecipe(new ItemStack(redSandstone,1,0),"SS","SS",'S',new ItemStack(Blocks.sand,1,1));
        GameRegistry.addRecipe(new ItemStack(redSandstone,1,1),"S","S",'S',new ItemStack(redSandstoneSlab));
        GameRegistry.addRecipe(new ItemStack(redSandstone,4,2),"SS","SS",'S',new ItemStack(redSandstone,1,0));
        for(int meta=0;meta<3;meta++)GameRegistry.addRecipe(new ItemStack(redSandstoneStairs,4),"S  ","SS ","SSS",'S',new ItemStack(redSandstone,1,meta));
        for(int meta=0;meta<2;meta++)GameRegistry.addRecipe(new ItemStack(redSandstoneSlab,6),"SSS",'S',new ItemStack(redSandstone,1,meta));
        GameRegistry.addRecipe(new ItemStack(prismarine,1,0),"SS","SS",'S',prismarineShard);
        GameRegistry.addRecipe(new ItemStack(prismarine,1,1),"SSS","SSS","SSS",'S',new ItemStack(prismarineShard));
        GameRegistry.addRecipe(new ItemStack(prismarine,1,2),"SSS","SDS","SSS",'S',prismarineShard,'D',com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.blackDye);
        GameRegistry.addRecipe(new ItemStack(seaLantern),"SCS","CCC","SCS",'S',prismarineShard,'C',prismarineCrystals);
        // Rabbit foot uses the vanilla brewing-stand expression. Redstone, glowstone and gunpowder
        // consequently create the extended, strong and splash variants through the normal pipeline.
        for(int i=0;i<WOODS.length;i++){ItemStack plank=new ItemStack(Blocks.planks,1,i+1);GameRegistry.addRecipe(new ItemStack(woodFences[i],3),"PSP","PSP",'P',plank,'S',Items.stick);GameRegistry.addRecipe(new ItemStack(woodFenceGates[i]),"SPS","SPS",'P',plank,'S',Items.stick);GameRegistry.addRecipe(new ItemStack(woodDoorItems[i],3),"PP","PP","PP",'P',plank);}
    }

    /** Adds the jump effect omitted from 1.7's otherwise compatible potion-bit pipeline. */
    @SuppressWarnings("unchecked") public static void installLeapingBrewing() {
        try {
            java.lang.reflect.Field requirements=findPotionField("potionRequirements","field_77927_l");
            java.lang.reflect.Field amplifiers=findPotionField("potionAmplifiers","field_77928_m");
            ((java.util.Map<Integer,String>)requirements.get(null)).put(net.minecraft.potion.Potion.jump.id,
                    "0 & 1 & !2 & 3");
            ((java.util.Map<Integer,String>)amplifiers.get(null)).put(net.minecraft.potion.Potion.jump.id,"5");
        } catch (Exception error) { throw new IllegalStateException("Unable to install Potion of Leaping brewing",error); }
    }
    private static java.lang.reflect.Field findPotionField(String... names)throws NoSuchFieldException {
        for(String name:names)try{java.lang.reflect.Field field=net.minecraft.potion.PotionHelper.class.getDeclaredField(name);field.setAccessible(true);return field;}catch(NoSuchFieldException ignored){}
        throw new NoSuchFieldException(java.util.Arrays.toString(names));
    }

    private static Item food(String name,int heal,float saturation){return new ItemFood(heal,saturation,true).setUnlocalizedName(name).setTextureName("htgt6:bountiful/"+name).setCreativeTab(CreativeTabs.tabFood);}
    private static Item item(String name,CreativeTabs tab){return new Item().setUnlocalizedName(name).setTextureName("htgt6:bountiful/"+name).setCreativeTab(tab);}
    private static Block[] createFences() {
        Block[] blocks = new Block[WOODS.length];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new BlockBountifulFence(WOODS[i],publicWoodName(WOODS[i]));
        }
        return blocks;
    }
    private static Block[] createGates(){Block[] blocks=new Block[WOODS.length];for(int i=0;i<blocks.length;i++)blocks[i]=new BlockBountifulFenceGate(WOODS[i]);return blocks;}
    private static BlockBountifulWoodDoor[] createDoors(){BlockBountifulWoodDoor[] blocks=new BlockBountifulWoodDoor[WOODS.length];for(int i=0;i<blocks.length;i++)blocks[i]=new BlockBountifulWoodDoor(WOODS[i]);return blocks;}
    private static Item[] createDoorItems(){Item[] items=new Item[WOODS.length];for(int i=0;i<items.length;i++)items[i]=new ItemBountifulWoodDoor(WOODS[i],woodDoors[i]);return items;}
    private static String publicWoodName(String wood){return "big_oak".equals(wood)?"dark_oak":wood;}
    private static int findEnchantmentId(){final int vanilla18Id=8;if(vanilla18Id>=net.minecraft.enchantment.Enchantment.enchantmentsList.length)throw new IllegalStateException("Enchantment registry cannot hold Depth Strider ID 8");if(net.minecraft.enchantment.Enchantment.enchantmentsList[vanilla18Id]!=null)throw new IllegalStateException("Depth Strider requires stable vanilla ID 8, occupied by "+net.minecraft.enchantment.Enchantment.enchantmentsList[vanilla18Id].getName());return vanilla18Id;}

    private static final class SimpleBlock extends Block {
        SimpleBlock(Material material, String name, float hardness, float light) {
            super(material); setBlockName(name); setBlockTextureName("htgt6:bountiful/" + name);
            setHardness(hardness); setResistance(Backport120Content.legacyResistance(hardness)); setLightLevel(light);
            setCreativeTab(CreativeTabs.tabBlock);
        }
    }

    private BountifulContent() { }
}
