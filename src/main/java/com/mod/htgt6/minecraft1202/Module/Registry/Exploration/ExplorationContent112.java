package com.mod.htgt6.minecraft1202.Module.Registry.Exploration;

import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockObserver112;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.BlockShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Blocks.Exploration.TileEntityShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.EnchantmentCurse112;
import com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemBlockShulkerBox112;
import com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemExplorerMap112;
import com.mod.htgt6.minecraft1202.Module.Items.Exploration.ItemTotemOfUndying112;
import com.mod.htgt6.minecraft1202.Module.Recipes.Exploration.RecipeShulkerColor112;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

/** Stable 1.12 registry surface for content introduced during Minecraft 1.11. */
public final class ExplorationContent112 {
    public static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime",
            "pink", "gray", "silver", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    public static final Block observer = new BlockObserver112();
    public static final BlockShulkerBox112[] shulkerBoxes = createShulkerBoxes();
    public static final BlockShulkerBox112 undyedShulkerBox = new BlockShulkerBox112("undyed");
    public static final Item ironNugget = item("iron_nugget", CreativeTabs.tabMaterials);
    public static final Item shulkerShell = item("shulker_shell", CreativeTabs.tabMaterials);
    public static final Item totemOfUndying = new ItemTotemOfUndying112();
    public static final ItemExplorerMap112 explorerMap = new ItemExplorerMap112();
    public static final Enchantment bindingCurse = new EnchantmentCurse112(findEnchantmentId(),
            EnumEnchantmentType.armor, "bindingCurse");
    public static final Enchantment vanishingCurse = new EnchantmentCurse112(findEnchantmentId(),
            EnumEnchantmentType.all, "vanishingCurse");

    public static void register() {
        Enchantment.addToBookList(bindingCurse);
        Enchantment.addToBookList(vanishingCurse);
        GameRegistry.registerBlock(observer, "observer");
        GameRegistry.registerItem(ironNugget, "iron_nugget");
        GameRegistry.registerItem(shulkerShell, "shulker_shell");
        GameRegistry.registerItem(totemOfUndying, "totem_of_undying");
        GameRegistry.registerItem(explorerMap, "explorer_map");
        for (int i = 0; i < shulkerBoxes.length; ++i)
            GameRegistry.registerBlock(shulkerBoxes[i], ItemBlockShulkerBox112.class,
                    COLORS[i] + "_shulker_box");
        GameRegistry.registerTileEntity(TileEntityShulkerBox112.class, "minecraft:shulker_box");
        GameRegistry.registerBlock(undyedShulkerBox, ItemBlockShulkerBox112.class, "shulker_box");
        GameRegistry.addRecipe(new ItemStack(observer), "CCC", "RRQ", "CCC",
                'C', Blocks.cobblestone, 'R', Items.redstone, 'Q', Items.quartz);
        GameRegistry.addShapelessRecipe(new ItemStack(ironNugget, 9), Items.iron_ingot);
        GameRegistry.addRecipe(new ItemStack(Items.iron_ingot), "NNN", "NNN", "NNN",
                'N', ironNugget);
        GameRegistry.addRecipe(new ItemStack(undyedShulkerBox), "S", "C", "S",
                'S', shulkerShell, 'C', Blocks.chest);
        CraftingManager.getInstance().getRecipeList().add(new RecipeShulkerColor112());
        GameRegistry.addSmelting(Items.iron_sword, new ItemStack(ironNugget), 0.1F);
        GameRegistry.addSmelting(Items.iron_pickaxe, new ItemStack(ironNugget), 0.1F);
        GameRegistry.addSmelting(Items.iron_axe, new ItemStack(ironNugget), 0.1F);
        GameRegistry.addSmelting(Items.iron_shovel, new ItemStack(ironNugget), 0.1F);
        GameRegistry.addSmelting(Items.iron_hoe, new ItemStack(ironNugget), 0.1F);
    }

    private static BlockShulkerBox112[] createShulkerBoxes() {
        BlockShulkerBox112[] boxes = new BlockShulkerBox112[COLORS.length];
        for (int i = 0; i < boxes.length; ++i) boxes[i] = new BlockShulkerBox112(COLORS[i]);
        return boxes;
    }

    private static Item item(String name, CreativeTabs tab) {
        return new Item().setUnlocalizedName(name)
                .setTextureName("htgt6:exploration_update/" + name).setCreativeTab(tab);
    }

    private static int findEnchantmentId() {
        for (int i = Enchantment.enchantmentsList.length - 1; i >= 0; --i)
            if (Enchantment.enchantmentsList[i] == null) return i;
        throw new IllegalStateException("No free enchantment ID for Minecraft 1.11 module");
    }

    private ExplorationContent112() { }
}
