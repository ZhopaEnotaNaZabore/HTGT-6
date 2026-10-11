package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

/** Copper ore with the 1.20.2 raw-copper drop contract. */
public final class BlockBackport120CopperOre extends Block {
    private final boolean deepslate;
    private final Random experienceRandom = new Random();

    public BlockBackport120CopperOre(boolean deepslate) {
        super(Material.rock);
        this.deepslate = deepslate;
        String name = deepslate ? "deepslate_copper_ore" : "copper_ore";
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + name);
        setHardness(deepslate ? 4.5F : 3.0F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(soundTypePiston);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public Item getItemDropped(int metadata, Random random, int fortune) {
        return Backport120Content.rawCopper;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(4);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune <= 0) return quantityDropped(random);
        int multiplier = Math.max(1, random.nextInt(fortune + 2));
        return quantityDropped(random) * multiplier;
    }

    @Override
    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        return MathHelper.getRandomIntegerInRange(experienceRandom, 0, 2);
    }

    @Override
    public int getRenderType() {
        return Backport120Content.getJsonRenderId();
    }

    public boolean isDeepslateVariant() {
        return deepslate;
    }
}
