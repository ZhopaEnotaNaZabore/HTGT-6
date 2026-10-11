package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

/** Exact non-silk drop families for the seven 1.20.2 deepslate ore variants. */
public final class BlockBackport120DeepslateOre extends Block {
    public enum Drop { COAL, RAW_IRON, RAW_GOLD, REDSTONE, EMERALD, LAPIS, DIAMOND }
    private final Drop drop;
    private final Random experienceRandom = new Random();

    public BlockBackport120DeepslateOre(String name, Drop drop, int harvestLevel) {
        super(Material.rock);
        this.drop = drop;
        setBlockName("backport_1_20." + name);
        setBlockTextureName("htgt6:1_20_backport/" + name);
        setHardness(4.5F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(soundTypePiston);
        setHarvestLevel("pickaxe", harvestLevel);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override protected boolean canSilkHarvest() { return true; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        switch (drop) {
            case RAW_IRON: return Backport120Content.rawIron;
            case RAW_GOLD: return Backport120Content.rawGold;
            case REDSTONE: return Items.redstone;
            case EMERALD: return Items.emerald;
            case LAPIS: return Items.dye;
            case DIAMOND: return Items.diamond;
            default: return Items.coal;
        }
    }
    @Override public int damageDropped(int metadata) { return drop == Drop.LAPIS ? 4 : 0; }
    @Override public int quantityDropped(Random random) {
        if (drop == Drop.LAPIS) return 4 + random.nextInt(5);
        if (drop == Drop.REDSTONE) return 4 + random.nextInt(2);
        return 1;
    }
    @Override public int quantityDroppedWithBonus(int fortune, Random random) {
        if (fortune <= 0) return quantityDropped(random);
        if (drop == Drop.REDSTONE) return quantityDropped(random) + random.nextInt(fortune + 1);
        int multiplier = Math.max(1, random.nextInt(fortune + 2));
        return quantityDropped(random) * multiplier;
    }
    @Override public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        switch (drop) {
            case COAL: return MathHelper.getRandomIntegerInRange(experienceRandom, 0, 2);
            case REDSTONE: return MathHelper.getRandomIntegerInRange(experienceRandom, 1, 5);
            case LAPIS: return MathHelper.getRandomIntegerInRange(experienceRandom, 2, 5);
            case EMERALD:
            case DIAMOND: return MathHelper.getRandomIntegerInRange(experienceRandom, 3, 7);
            default: return 0;
        }
    }
}
