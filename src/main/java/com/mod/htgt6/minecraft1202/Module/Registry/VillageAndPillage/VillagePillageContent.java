package com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillageBarrel120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillageFurnace120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillagePillageLantern;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockVillagePoi120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBell120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageFurnace120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageLectern120;
import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageComposter120;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockColoredBed120;
import com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemCrossbow;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.block.material.Material;

/** Content registry owned exclusively by the Village & Pillage module. */
public final class VillagePillageContent {
    public static final String[] BED_COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    public static final Block scaffolding = new com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockScaffolding152();
    public static final Item crossbow = new ItemCrossbow();
    public static final Block campfire = new com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockCampfire149();
    public static final Block lantern = new BlockVillagePillageLantern();
    public static final Block blastFurnace = new BlockVillageFurnace120("blast_furnace", "armorer", false);
    public static final Block smoker = new BlockVillageFurnace120("smoker", "butcher", true);
    public static final Block cartographyTable = poi("cartography_table", "cartographer", Material.wood, 2.5F);
    public static final Block composter = poi("composter", "farmer", Material.wood, 0.6F);
    public static final Block barrel = new BlockVillageBarrel120();
    public static final Block fletchingTable = poi("fletching_table", "fletcher", Material.wood, 2.5F);
    public static final Block lectern = poi("lectern", "librarian", Material.wood, 2.5F);
    public static final Block stonecutter = poi("stonecutter", "mason", Material.rock, 3.5F);
    public static final Block loom = poi("loom", "shepherd", Material.wood, 2.5F);
    public static final Block smithingTable = poi("smithing_table", "toolsmith", Material.wood, 2.5F);
    public static final Block grindstone = poi("grindstone", "weaponsmith", Material.rock, 2.0F);
    public static final Block bell = poi("bell", "meeting", Material.iron, 5.0F);
    public static final Block[] coloredBeds = beds();
    private static int jsonRenderId;
    private static boolean modernIngredientRecipesRegistered;
    private static boolean grindstoneRecipeRegistered;

    public static void register() {
        GameRegistry.registerTileEntity(com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityVillageBarrel120.class,"htgt6:village_barrel_120");
        GameRegistry.registerTileEntity(com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityColoredBed120.class,"htgt6:colored_bed_120");
        SmithingContent120.register();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.NetheriteEquipment120());
        CrossbowEnchantments140.init();
        BannerPatterns145.register();
        LeatherHorseArmor151.register();
        GameRegistry.registerBlock(scaffolding,com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockScaffolding152.class,"village_pillage_scaffolding");
        GameRegistry.registerItem(crossbow, "village_pillage_crossbow");
        GameRegistry.registerBlock(lantern, "village_pillage_lantern");
        GameRegistry.registerBlock(campfire, com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockCampfire149.class,"village_pillage_campfire");
        GameRegistry.registerTileEntity(com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.TileEntityCampfire149.class,"htgt6:campfire_149");
        for(int fuel=0;fuel<=1;fuel++)GameRegistry.addRecipe(new net.minecraftforge.oredict.ShapedOreRecipe(new ItemStack(campfire)," S ","SCS","LLL",
                'S',Items.stick,'C',new ItemStack(Items.coal,1,fuel),'L',"logWood"));
        register(blastFurnace, "blast_furnace"); register(smoker, "smoker");
        register(cartographyTable, "cartography_table"); register(composter, "composter");
        register(barrel, "barrel"); register(fletchingTable, "fletching_table");
        register(lectern, "lectern"); register(stonecutter, "stonecutter"); register(loom, "loom");
        register(smithingTable, "smithing_table"); register(grindstone, "grindstone"); register(bell, "bell");
        registerColoredBeds();
        GameRegistry.registerTileEntity(TileEntityVillageFurnace120.class, "htgt6:village_furnace_120");
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageFurnaceRecipes143.registerRecycling();
        GameRegistry.registerTileEntity(TileEntityVillageBell120.class, "htgt6:village_bell_120");
        GameRegistry.registerTileEntity(TileEntityVillageLectern120.class, "htgt6:village_lectern_120");
        GameRegistry.registerTileEntity(TileEntityVillageComposter120.class, "htgt6:village_composter_120");
        GameRegistry.addRecipe(new ItemStack(crossbow), "SIS", "TBT", " S ",
                'S', Items.stick, 'I', Items.iron_ingot, 'T', Items.string, 'B', net.minecraft.init.Blocks.tripwire_hook);
        GameRegistry.addRecipe(new ItemStack(lantern), "NNN", "NTN", "NNN",
                'N', com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112.ironNugget,
                'T', net.minecraft.init.Blocks.torch);
        // Official 1.20.2 shaped recipes using the available ported ingredients.
        GameRegistry.addRecipe(new ItemStack(barrel), "PSP", "P P", "PSP",
                'P', new ItemStack(net.minecraft.init.Blocks.planks,1,32767),
                'S', new ItemStack(net.minecraft.init.Blocks.wooden_slab,1,32767));
        GameRegistry.addRecipe(new ItemStack(cartographyTable), "PP", "WW", "WW",
                'P', Items.paper, 'W', new ItemStack(net.minecraft.init.Blocks.planks,1,32767));
        GameRegistry.addRecipe(new ItemStack(composter), "S S", "S S", "SSS",
                'S', new ItemStack(net.minecraft.init.Blocks.wooden_slab,1,32767));
        GameRegistry.addRecipe(new ItemStack(fletchingTable), "FF", "WW", "WW",
                'F', Items.flint, 'W', new ItemStack(net.minecraft.init.Blocks.planks,1,32767));
        GameRegistry.addRecipe(new ItemStack(lectern), "SSS", " B ", " S ",
                'S', new ItemStack(net.minecraft.init.Blocks.wooden_slab,1,32767),
                'B', net.minecraft.init.Blocks.bookshelf);
        GameRegistry.addRecipe(new ItemStack(loom), "TT", "WW",
                'T', Items.string, 'W', new ItemStack(net.minecraft.init.Blocks.planks,1,32767));
        GameRegistry.addRecipe(new ItemStack(smithingTable), "II", "WW", "WW",
                'I', Items.iron_ingot, 'W', new ItemStack(net.minecraft.init.Blocks.planks,1,32767));
        GameRegistry.addRecipe(new ItemStack(smoker), " L ", "LFL", " L ",
                'L', new ItemStack(net.minecraft.init.Blocks.log,1,32767),
                'F', net.minecraft.init.Blocks.furnace);
        GameRegistry.addRecipe(new ItemStack(smoker), " L ", "LFL", " L ",
                'L', new ItemStack(net.minecraft.init.Blocks.log2,1,32767),
                'F', net.minecraft.init.Blocks.furnace);
        GameRegistry.addRecipe(new ItemStack(stonecutter), " I ", "SSS",
                'I', Items.iron_ingot, 'S', net.minecraft.init.Blocks.stone);
    }

    public static void registerColoredBeds() {
        for (int color = 0; color < coloredBeds.length; ++color) {
            GameRegistry.registerBlock(coloredBeds[color], ItemBlockColoredBed120.class,
                    "village_pillage_" + BED_COLORS[color] + "_bed");
            GameRegistry.addRecipe(new ItemStack(coloredBeds[color]), "WWW", "PPP",
                    'W', new ItemStack(net.minecraft.init.Blocks.wool, 1, color),
                    'P', new ItemStack(net.minecraft.init.Blocks.planks, 1, 32767));
            java.util.List recipes=net.minecraft.item.crafting.CraftingManager.getInstance().getRecipeList();
            recipes.add(0,recipes.remove(recipes.size()-1));
        }
    }

    /** Called after modern blocks have ItemBlocks; constructing this recipe earlier loses smooth stone. */
    public static void registerModernIngredientRecipes() {
        Block stoneSlab=VillageBuildingContent114.SHAPES.get("stone_slab").block;
        if(!grindstoneRecipeRegistered&&Item.getItemFromBlock(grindstone)!=null&&Item.getItemFromBlock(stoneSlab)!=null){
            GameRegistry.addRecipe(new ItemStack(grindstone),"TST","W W",'T',Items.stick,'S',stoneSlab,
                    'W',new ItemStack(net.minecraft.init.Blocks.planks,1,32767));
            grindstoneRecipeRegistered=true;
        }
        com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage.VillageFurnaceRecipes143.registerOreSmelting();
        if (modernIngredientRecipesRegistered || Item.getItemFromBlock(scaffolding) == null
                || Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.bamboo) == null
                || Item.getItemFromBlock(blastFurnace) == null
                || Item.getItemFromBlock(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.smoothStone) == null) return;
        GameRegistry.addRecipe(new ItemStack(blastFurnace), "III", "IFI", "SSS",
                'I', Items.iron_ingot, 'F', net.minecraft.init.Blocks.furnace,
                'S', com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.smoothStone);
        modernIngredientRecipesRegistered = true;
        SmithingContent120.duplicationRecipes();
        GameRegistry.addRecipe(new ItemStack(scaffolding,6),"BSB","B B","B B",'B',com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.bamboo,'S',Items.string);
    }

    private static Block poi(String name, String type, Material material, float hardness) {
        return new BlockVillagePoi120(name, type, material, hardness);
    }
    private static Block[] beds() {
        Block[] result = new Block[BED_COLORS.length];
        for (int i = 0; i < result.length; ++i) result[i] = new BlockColoredBed120(BED_COLORS[i], i);
        return result;
    }
    public static Block coloredBed(String name) {
        for (int i = 0; i < BED_COLORS.length; ++i) if (BED_COLORS[i].equals(name)) return coloredBeds[i];
        return coloredBeds[14];
    }
    private static void register(Block block, String name) {
        if ("bell".equals(name)) GameRegistry.registerBlock(block,
                com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage.ItemBlockBell120.class, "village_pillage_" + name);
        else GameRegistry.registerBlock(block, "village_pillage_" + name);
    }
    public static int getJsonRenderId() { return jsonRenderId; }
    public static void setJsonRenderId(int value) { jsonRenderId = value; }

    private VillagePillageContent() { }
}
