package com.mod.htgt6.minecraft1202.Module.Registry.Modern;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.ArrayList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

/** Modern stone ingredient alternatives, without broadening global ore categories. */
public final class BlackstoneCrafting116 {
    private static boolean registered;

    public static synchronized void register() {
        if (registered) return;
        add(new ItemStack(Items.stone_pickaxe), new String[]{"XXX", " S ", " S "}, Items.stick);
        add(new ItemStack(Items.stone_axe), new String[]{"XX", "XS", " S"}, Items.stick);
        add(new ItemStack(Items.stone_hoe), new String[]{"XX", " S", " S"}, Items.stick);
        add(new ItemStack(Items.stone_shovel), new String[]{"X", "S", "S"}, Items.stick);
        add(new ItemStack(Items.stone_sword), new String[]{"X", "X", "S"}, Items.stick);
        add(new ItemStack(Blocks.furnace), new String[]{"XXX", "X X", "XXX"}, Items.stick);
        add(new ItemStack(Items.brewing_stand), new String[]{" S ", "XXX"}, Items.blaze_rod);
        registered = true;
    }

    private static void add(ItemStack output, String[] pattern, net.minecraft.item.Item handle) {
        ShapedOreRecipe recipe = new ShapedOreRecipe(output, pattern,
                'X', new ItemStack(Blocks.cobblestone), 'S', handle);
        // Forge 1.7.10 accepts ArrayList alternatives internally. Build private lists
        // before registration; NEI can display these through its normal ore recipe handler.
        Object[] input = recipe.getInput();
        for (int i = 0; i < input.length; i++) {
            if (input[i] instanceof ItemStack && ((ItemStack) input[i]).isItemEqual(new ItemStack(Blocks.cobblestone))) {
                ArrayList<ItemStack> stones = new ArrayList<ItemStack>();
                stones.add(new ItemStack(Blocks.cobblestone));
                stones.add(new ItemStack(EtFVanillaGapContent120.blackstone));
                stones.add(new ItemStack(Backport120Content.cobbledDeepslate));
                input[i] = stones;
            }
        }
        GameRegistry.addRecipe(recipe);
    }

    private BlackstoneCrafting116() {}
}
