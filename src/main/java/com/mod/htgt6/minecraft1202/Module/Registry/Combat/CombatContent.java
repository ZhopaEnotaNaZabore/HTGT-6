package com.mod.htgt6.minecraft1202.Module.Registry.Combat;

import com.mod.htgt6.asm.HTGT6Config;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockBeetrootCrop;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockChorusFlower;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockChorusPlant;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockCombatSimple;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockCombatStairs;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockEndRod;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockFrostedIce;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockCombatCommand;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockDragonHead;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockPurpurPillar;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockPurpurSlab;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.EnchantmentCombat;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatPotionVariants120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionLevitation;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.PotionMarker;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemChorusFruit;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemCombatElytra;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemDragonBreath;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemBlockDragonHead;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemCombatShield;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemEndCrystal;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemLingeringPotion;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemCombatBoat;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemCombatSpawnEgg;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemPurpurSlab;
import com.mod.htgt6.minecraft1202.Module.Items.Combat.ItemTippedArrow;
import com.mod.htgt6.minecraft1202.Module.Recipes.Combat.RecipeShieldDecoration120;

import com.mod.htgt6.minecraft1202.Module.Items.Bountiful.ItemBlockBountifulTechnical;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockEndGateway;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Registries and recipes owned by the Minecraft Java 1.9 module. */
public final class CombatContent {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-CombatContent");
    public static final String[] BOAT_TYPES = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
    private static int endRodRenderId;
    private static int chorusRenderId;
    public static final Block purpurBlock = new BlockCombatSimple(Material.rock, "purpur_block", 1.5F, 6.0F);
    public static final Block purpurPillar = new BlockPurpurPillar();
    public static final Block purpurStairs = new BlockCombatStairs(purpurBlock);
    public static final BlockPurpurSlab purpurSlab = new BlockPurpurSlab(false);
    public static final BlockPurpurSlab doublePurpurSlab = new BlockPurpurSlab(true);
    public static final Block endBricks = new BlockCombatSimple(Material.rock, "end_bricks", 3.0F, 9.0F);
    public static final Block chorusPlant = new BlockChorusPlant();
    public static final Block chorusFlower = new BlockChorusFlower();
    public static final Block endRod = new BlockEndRod();
    public static final Block endGateway = new BlockEndGateway();
    public static final Block frostedIce = new BlockFrostedIce();
    public static final Block beetrootCrop = new BlockBeetrootCrop();
    public static final Block chainCommandBlock = new BlockCombatCommand(BlockCombatCommand.CHAIN);
    public static final Block repeatingCommandBlock = new BlockCombatCommand(BlockCombatCommand.REPEATING);
    public static final Block dragonHead = new BlockDragonHead();

    public static final Item chorusFruit = new ItemChorusFruit().setCreativeTab(CreativeTabs.tabFood);
    public static final Item poppedChorusFruit = item("chorus_fruit_popped", CreativeTabs.tabMaterials);
    public static final Item beetroot = new ItemFood(1, 0.6F, false).setUnlocalizedName("beetroot")
            .setTextureName("htgt6:combat_update/beetroot").setCreativeTab(CreativeTabs.tabFood);
    public static final Item beetrootSeeds = new ItemSeeds(beetrootCrop, Blocks.farmland).setUnlocalizedName("beetroot_seeds")
            .setTextureName("htgt6:combat_update/beetroot_seeds").setCreativeTab(CreativeTabs.tabMaterials);
    public static final Item beetrootSoup = new ItemSoup(6).setUnlocalizedName("beetroot_soup")
            .setTextureName("htgt6:combat_update/beetroot_soup").setCreativeTab(CreativeTabs.tabFood);
    public static final Item shield = new ItemCombatShield();
    public static final Item elytra = new ItemCombatElytra();
    public static final Item dragonBreath = new ItemDragonBreath();
    public static final Item spectralArrow = item("spectral_arrow", CreativeTabs.tabCombat);
    public static final Item tippedArrow = new ItemTippedArrow();
    public static final Item lingeringPotion = new ItemLingeringPotion();
    public static final Item endCrystal = new ItemEndCrystal();
    /** Oak already exists in 1.7; indices 0..4 represent modern variants 1..5. */
    public static final Item[] woodBoats = createWoodBoats();
    public static final Item shulkerSpawnEgg = new ItemCombatSpawnEgg();

    public static int getEndRodRenderId() { return endRodRenderId; }
    public static void setEndRodRenderId(int renderId) { endRodRenderId = renderId; }
    public static int getChorusRenderId() { return chorusRenderId; }
    public static void setChorusRenderId(int renderId) { chorusRenderId = renderId; }

    public static final Potion glowing = new PotionMarker(requireConfiguredPotionId(HTGT6Config.GLOWING_POTION_ID, "Glowing"), false,
            0x94A061, "potion.glowing");
    public static final Potion levitation = new PotionLevitation(requireConfiguredPotionId(HTGT6Config.LEVITATION_POTION_ID, "Levitation"));
    public static final Potion luck = new PotionMarker(requireConfiguredPotionId(HTGT6Config.LUCK_POTION_ID, "Luck"), false,
            0x339900, "potion.luck").func_111184_a(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatAttack120.LUCK,"43e56709-f18a-40bf-83b9-2383d5625328",1,0);
    public static final Potion unluck = new PotionMarker(requireConfiguredPotionId(HTGT6Config.UNLUCK_POTION_ID, "Bad Luck"), true,
            0xC0A44D, "potion.unluck").func_111184_a(com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.CombatAttack120.LUCK,"fe0a2b5b-19e5-48a4-8e9c-f5fbdc57a116",-1,0);
    public static final Enchantment frostWalker = new EnchantmentCombat(requireEnchantmentId(9, "Frost Walker"), 2,
            EnumEnchantmentType.armor_feet, "frostWalker", 2, 10, 10, 15, true, 8);
    public static final Enchantment sweepingEdge = new EnchantmentCombat(requireEnchantmentId(22, "Sweeping Edge"), 2,
            EnumEnchantmentType.weapon, "sweeping", 3, 5, 9, 15, false, -1){
        @Override public boolean canApply(net.minecraft.item.ItemStack stack){return stack!=null&&stack.getItem() instanceof net.minecraft.item.ItemSword;}
    };
    public static final Enchantment mending = new EnchantmentCombat(requireEnchantmentId(70, "Mending"), 2,
            EnumEnchantmentType.breakable, "mending", 1, 25, 0, 50, true,
            Enchantment.infinity.effectId);

    public static void register() {
        GameRegistry.registerBlock(purpurBlock, "purpur_block");
        GameRegistry.registerBlock(purpurPillar, "purpur_pillar");
        GameRegistry.registerBlock(purpurStairs, "purpur_stairs");
        GameRegistry.registerBlock(purpurSlab, ItemPurpurSlab.class, "purpur_slab", purpurSlab, doublePurpurSlab, Boolean.FALSE);
        GameRegistry.registerBlock(doublePurpurSlab, ItemBlockBountifulTechnical.class, "double_purpur_slab");
        GameRegistry.registerBlock(endBricks, "end_bricks");
        GameRegistry.registerBlock(chorusPlant, "chorus_plant");
        GameRegistry.registerBlock(chorusFlower, "chorus_flower");
        GameRegistry.registerBlock(endRod, "end_rod");
        GameRegistry.registerBlock(endGateway, ItemBlockBountifulTechnical.class, "end_gateway");
        GameRegistry.registerBlock(frostedIce, ItemBlockBountifulTechnical.class, "frosted_ice");
        GameRegistry.registerBlock(beetrootCrop, ItemBlockBountifulTechnical.class, "beetroots");
        GameRegistry.registerBlock(chainCommandBlock, "chain_command_block");
        GameRegistry.registerBlock(repeatingCommandBlock, "repeating_command_block");
        GameRegistry.registerBlock(dragonHead, ItemBlockDragonHead.class, "dragon_head");

        register(chorusFruit, "chorus_fruit"); register(poppedChorusFruit, "chorus_fruit_popped");
        register(beetroot, "beetroot"); register(beetrootSeeds, "beetroot_seeds"); register(beetrootSoup, "beetroot_soup");
        register(shield, "shield"); register(elytra, "elytra"); register(dragonBreath, "dragon_breath");
        register(spectralArrow, "spectral_arrow"); register(tippedArrow, "tipped_arrow");
        register(lingeringPotion, "lingering_potion"); register(endCrystal, "end_crystal");
        for (int i = 0; i < woodBoats.length; ++i) register(woodBoats[i], BOAT_TYPES[i + 1] + "_boat");
        register(shulkerSpawnEgg, "combat_shulker_spawn_egg");

        GameRegistry.addSmelting(chorusFruit, new ItemStack(poppedChorusFruit), 0.1F);
        GameRegistry.addRecipe(new ItemStack(purpurBlock, 4), "PP", "PP", 'P', poppedChorusFruit);
        GameRegistry.addRecipe(new ItemStack(purpurPillar), "P", "P", 'P', purpurSlab);
        GameRegistry.addRecipe(new ItemStack(purpurStairs, 4), "P  ", "PP ", "PPP", 'P', purpurBlock);
        GameRegistry.addRecipe(new ItemStack(purpurStairs, 4), "P  ", "PP ", "PPP", 'P', purpurPillar);
        GameRegistry.addRecipe(new ItemStack(purpurSlab, 6), "PPP", 'P', purpurBlock);
        GameRegistry.addRecipe(new ItemStack(purpurSlab, 6), "PPP", 'P', purpurPillar);
        GameRegistry.addRecipe(new ItemStack(endBricks, 4), "EE", "EE", 'E', Blocks.end_stone);
        GameRegistry.addRecipe(new ItemStack(endRod, 4), " B", " P", 'B', Items.blaze_rod, 'P', poppedChorusFruit);
        GameRegistry.addShapelessRecipe(new ItemStack(beetrootSoup), Items.bowl,
                beetroot, beetroot, beetroot, beetroot, beetroot, beetroot);
        GameRegistry.addRecipe(new ItemStack(shield), "WMW", "WWW", " W ", 'W', Blocks.planks, 'M', Items.iron_ingot);
        GameRegistry.addRecipe(new RecipeShieldDecoration120());
        GameRegistry.addRecipe(new ItemStack(endCrystal), "GGG", "GEG", "GTG", 'G', Blocks.glass,
                'E', Items.ender_eye, 'T', Items.ghast_tear);
        GameRegistry.addRecipe(new ItemStack(spectralArrow, 2), " G ", "GAG", " G ", 'G', Items.glowstone_dust, 'A', Items.arrow);
        GameRegistry.addRecipe(new com.mod.htgt6.minecraft1202.Module.Recipes.Combat.RecipeTippedArrow120());
        for (int i = 0; i < woodBoats.length; ++i) {
            ItemStack plank = new ItemStack(Blocks.planks, 1, i + 1);
            GameRegistry.addRecipe(new ItemStack(woodBoats[i]), "P P", "PPP", 'P', plank);
        }
    }

    private static Item item(String name, CreativeTabs tab) {
        return new Item().setUnlocalizedName(name).setTextureName("htgt6:combat_update/" + name).setCreativeTab(tab);
    }
    private static void register(Item item, String name) { GameRegistry.registerItem(item, name); }
    private static Item[] createWoodBoats() {
        Item[] boats = new Item[BOAT_TYPES.length - 1];
        for (int i = 0; i < boats.length; ++i) boats[i] = new ItemCombatBoat(BOAT_TYPES[i + 1], i + 1);
        return boats;
    }
    public static Item boatItem(int variant) {
        return variant <= 0 || variant >= BOAT_TYPES.length ? Items.boat : woodBoats[variant - 1];
    }
    private static int requireConfiguredPotionId(int id, String name) {
        if (id < 0 || id >= Potion.potionTypes.length)
            throw new IllegalStateException(name + " configured potion ID " + id
                    + " is outside registry length " + Potion.potionTypes.length);
        if (Potion.potionTypes[id] != null)
            throw new IllegalStateException(name + " configured potion ID " + id + " is occupied by "
                    + Potion.potionTypes[id].getClass().getName()
                    + "; choose the same free combat_potion_ids value on client and server");
        LOGGER.info("Registered {} at configured potion ID {}", name, id);
        return id;
    }
    private static int requireEnchantmentId(int id, String name) {
        if (id < 0 || id >= Enchantment.enchantmentsList.length || Enchantment.enchantmentsList[id] != null)
            throw new IllegalStateException(name + " requires stable legacy enchantment ID " + id);
        return id;
    }
    private CombatContent() { }
}
