package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Items.Aquatic.ItemSuspiciousStew120;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

/** Relevant vanilla 1.20.2 loot-table pools without legacy ChestGenHooks substitutions. */
public final class AquaticChestLoot120 {
    public static final int SHIPWRECK_MAP_MISC_WEIGHT = 38;
    public static final int SHIPWRECK_SUPPLY_WEIGHT = 84;
    public static final int SHIPWRECK_TREASURE_MAIN_WEIGHT = 150;
    public static final int SHIPWRECK_TREASURE_NUGGET_WEIGHT = 80;
    public static final int BURIED_TREASURE_COMMON_WEIGHT = 35;
    public static final int BURIED_TREASURE_RARE_WEIGHT = 15;
    public static final int COAST_TRIM_DENOMINATOR = 6;

    public static void fillShipwreckMap(IInventory inventory, Random random, World world,
                                        int sourceX, int sourceZ) {
        // Project adaptation: the exploration result is a sealed scroll; locate
        // failure still retains the original empty map like 1.20.2.
        put(inventory, VanillaTreasureMap120.create(world, sourceX, sourceZ), random);
        // Pool 2: exactly three weighted rolls, total weight 38.
        for (int roll = 0; roll < 3; roll++) {
            int choice = random.nextInt(SHIPWRECK_MAP_MISC_WEIGHT);
            ItemStack stack;
            if (choice == 0) stack = new ItemStack(Items.compass);
            else if (choice == 1) stack = new ItemStack(Items.map);
            else if (choice == 2) stack = new ItemStack(Items.clock);
            else if (choice < 23) stack = new ItemStack(Items.paper, uniform(random, 1, 10));
            else if (choice < 33) stack = new ItemStack(Items.feather, uniform(random, 1, 5));
            else stack = new ItemStack(Items.book, uniform(random, 1, 5));
            put(inventory, stack, random);
        }
        addCoastTrim(inventory, random);
    }

    public static void fillShipwreckSupply(IInventory inventory, Random random) {
        for (int roll = 0, rolls = uniform(random, 3, 10); roll < rolls; roll++) {
            int choice = random.nextInt(SHIPWRECK_SUPPLY_WEIGHT);
            ItemStack stack;
            if (choice < 8) stack = new ItemStack(Items.paper, uniform(random, 1, 12));
            else if (choice < 15) stack = new ItemStack(Items.potato, uniform(random, 2, 6));
            else if (choice < 22) stack = block(Backport120Content.mossBlock, uniform(random, 1, 4));
            else if (choice < 29) stack = new ItemStack(Items.poisonous_potato, uniform(random, 2, 6));
            else if (choice < 36) stack = new ItemStack(Items.carrot, uniform(random, 4, 8));
            else if (choice < 43) stack = new ItemStack(Items.wheat, uniform(random, 8, 21));
            else if (choice < 53) stack = ItemSuspiciousStew120.create(random);
            else if (choice < 59) stack = new ItemStack(Items.coal, uniform(random, 2, 8));
            else if (choice < 64) stack = new ItemStack(Items.rotten_flesh, uniform(random, 5, 24));
            else if (choice < 66) stack = new ItemStack(Blocks.pumpkin, uniform(random, 1, 3));
            else if (choice < 68) stack = block(Backport120Content.bamboo, uniform(random, 1, 3));
            else if (choice < 71) stack = new ItemStack(Items.gunpowder, uniform(random, 1, 5));
            else if (choice == 71) stack = new ItemStack(Blocks.tnt, uniform(random, 1, 2));
            else if (choice < 75) stack = enchanted(random, new ItemStack(Items.leather_helmet));
            else if (choice < 78) stack = enchanted(random, new ItemStack(Items.leather_chestplate));
            else if (choice < 81) stack = enchanted(random, new ItemStack(Items.leather_leggings));
            else stack = enchanted(random, new ItemStack(Items.leather_boots));
            put(inventory, stack, random);
        }
        addCoastTrim(inventory, random);
    }

    public static void fillShipwreckTreasure(IInventory inventory, Random random) {
        for (int roll = 0, rolls = uniform(random, 3, 6); roll < rolls; roll++) {
            int choice = random.nextInt(SHIPWRECK_TREASURE_MAIN_WEIGHT);
            ItemStack stack;
            if (choice < 90) stack = new ItemStack(Items.iron_ingot, uniform(random, 1, 5));
            else if (choice < 100) stack = new ItemStack(Items.gold_ingot, uniform(random, 1, 5));
            else if (choice < 140) stack = new ItemStack(Items.emerald, uniform(random, 1, 5));
            else if (choice < 145) stack = new ItemStack(Items.diamond);
            else stack = new ItemStack(Items.experience_bottle);
            put(inventory, stack, random);
        }
        for (int roll = 0, rolls = uniform(random, 2, 5); roll < rolls; roll++) {
            int choice = random.nextInt(SHIPWRECK_TREASURE_NUGGET_WEIGHT);
            ItemStack stack = choice < 50
                    ? new ItemStack(Backport120Content.ironNugget, uniform(random, 1, 10))
                    : choice < 60
                    ? new ItemStack(Items.gold_nugget, uniform(random, 1, 10))
                    : new ItemStack(Items.dye, uniform(random, 1, 10), 4);
            put(inventory, stack, random);
        }
        addCoastTrim(inventory, random);
    }

    public static void fillBuriedTreasure(IInventory inventory, Random random) {
        put(inventory, new ItemStack(OceanContent.heartOfTheSea), random);
        for (int roll = 0, rolls = uniform(random, 5, 8); roll < rolls; roll++) {
            int choice = random.nextInt(BURIED_TREASURE_COMMON_WEIGHT);
            put(inventory, choice < 20
                    ? new ItemStack(Items.iron_ingot, uniform(random, 1, 4))
                    : choice < 30
                    ? new ItemStack(Items.gold_ingot, uniform(random, 1, 4))
                    : new ItemStack(Blocks.tnt, uniform(random, 1, 2)), random);
        }
        for (int roll = 0, rolls = uniform(random, 1, 3); roll < rolls; roll++) {
            int choice = random.nextInt(BURIED_TREASURE_RARE_WEIGHT);
            put(inventory, choice < 5
                    ? new ItemStack(Items.emerald, uniform(random, 4, 8))
                    : choice < 10
                    ? new ItemStack(Items.diamond, uniform(random, 1, 2))
                    : new ItemStack(BountifulContent.prismarineCrystals, uniform(random, 1, 5)), random);
        }
        if (random.nextBoolean()) {
            put(inventory, random.nextBoolean() ? new ItemStack(Items.leather_chestplate)
                    : new ItemStack(Items.iron_sword), random);
        }
        for (int roll = 0; roll < 2; roll++) {
            put(inventory, new ItemStack(Items.cooked_fished, uniform(random, 2, 4),
                    random.nextBoolean() ? 0 : 1), random);
        }
        for (int roll = 0, rolls = random.nextInt(3); roll < rolls; roll++) {
            put(inventory, new ItemStack(Items.potionitem, 1, 8205), random);
        }
    }

    public static void fillOceanRuin(IInventory inventory, Random random, World world,
                                     int sourceX, int sourceZ, boolean big) {
        int rolls = uniform(random, 2, 8);
        for (int roll = 0; roll < rolls; roll++) {
            int choice = random.nextInt(big ? 31 : 28);
            ItemStack stack;
            if (choice < 10) stack = new ItemStack(Items.coal, uniform(random, 1, 4));
            else if (big && choice < 20) stack = new ItemStack(Items.gold_nugget, uniform(random, 1, 3));
            else if (!big && choice < 12) stack = new ItemStack(Items.stone_axe);
            else if (!big && choice < 17) stack = new ItemStack(Items.rotten_flesh);
            else if (choice == (big ? 20 : 17)) stack = new ItemStack(Items.emerald);
            else stack = new ItemStack(Items.wheat, uniform(random, 2, 3));
            put(inventory, stack, random);
        }

        // Second pool: one weighted roll. The map weights are 10/23 and 5/12.
        int choice = random.nextInt(big
                ? WorldGenOceanRuin.BIG_SECOND_POOL_WEIGHT
                : WorldGenOceanRuin.SMALL_SECOND_POOL_WEIGHT);
        ItemStack stack;
        if (big) {
            if (choice == 0) stack = new ItemStack(Items.golden_apple);
            else if (choice < 6) stack = enchanted(random, new ItemStack(Items.book));
            else if (choice == 6) stack = new ItemStack(Items.leather_chestplate);
            else if (choice == 7) stack = new ItemStack(Items.golden_helmet);
            else if (choice < 13) stack = enchanted(random, new ItemStack(Items.fishing_rod));
            else stack = VanillaTreasureMap120.create(world, sourceX, sourceZ);
        } else {
            if (choice == 0) stack = new ItemStack(Items.leather_chestplate);
            else if (choice == 1) stack = new ItemStack(Items.golden_helmet);
            else if (choice < 7) stack = enchanted(random, new ItemStack(Items.fishing_rod));
            else stack = VanillaTreasureMap120.create(world, sourceX, sourceZ);
        }
        put(inventory, stack, random);
    }

    public static int uniform(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private static ItemStack enchanted(Random random, ItemStack stack) {
        List<Enchantment> candidates = new ArrayList<Enchantment>();
        for (Enchantment enchantment : Enchantment.enchantmentsList) {
            if (enchantment != null && (stack.getItem() == Items.book || enchantment.canApply(stack))) {
                candidates.add(enchantment);
            }
        }
        if (candidates.isEmpty()) return stack;
        Enchantment enchantment = candidates.get(random.nextInt(candidates.size()));
        int level = uniform(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
        if (stack.getItem() == Items.book) {
            ItemStack book = new ItemStack(Items.enchanted_book);
            Items.enchanted_book.addEnchantment(book, new EnchantmentData(enchantment, level));
            return book;
        }
        stack.addEnchantment(enchantment, level);
        return stack;
    }

    private static ItemStack block(Block block, int count) {
        Item item = Item.getItemFromBlock(block);
        return item == null ? null : new ItemStack(item, count);
    }

    private static void addCoastTrim(IInventory inventory, Random random) {
        if (random.nextInt(COAST_TRIM_DENOMINATOR) == 0) {
            put(inventory, new ItemStack(OceanContent.coastArmorTrim, 2), random);
        }
    }

    private static void put(IInventory inventory, ItemStack stack, Random random) {
        if (inventory == null || stack == null || inventory.getSizeInventory() <= 0) return;
        int start = random.nextInt(inventory.getSizeInventory());
        for (int offset = 0; offset < inventory.getSizeInventory(); offset++) {
            int slot = (start + offset) % inventory.getSizeInventory();
            if (inventory.getStackInSlot(slot) == null) {
                inventory.setInventorySlotContents(slot, stack);
                inventory.markDirty();
                return;
            }
        }
    }

    private AquaticChestLoot120() { }
}
