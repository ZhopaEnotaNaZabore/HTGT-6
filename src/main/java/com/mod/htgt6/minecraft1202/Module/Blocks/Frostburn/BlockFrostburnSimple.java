package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public final class BlockFrostburnSimple extends Block {
    private final boolean hoeEffective;
    public BlockFrostburnSimple(Material material, String name, String texture, float hardness,
                         float resistance, SoundType sound) {
        super(material);
        hoeEffective = "nether_wart_block".equals(name);
        if (material == Material.rock) setHarvestLevel("pickaxe", 0);
        setBlockName(name);
        setBlockTextureName("htgt6:frostburn/" + texture);
        setHardness(hardness);
        setResistance(Backport120Content.legacyResistance(resistance));
        setStepSound(sound);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override public int getRenderType() { return FrostburnContent.getJsonRenderId(); }

    @Override public float getPlayerRelativeBlockHardness(net.minecraft.entity.player.EntityPlayer player,
            net.minecraft.world.World world, int x, int y, int z) {
        net.minecraft.item.ItemStack stack = player.getHeldItem();
        if (!hoeEffective || stack == null || !(stack.getItem() instanceof net.minecraft.item.ItemHoe))
            return super.getPlayerRelativeBlockHardness(player, world, x, y, z);
        float speed = hoeSpeed((net.minecraft.item.ItemHoe) stack.getItem());
        int efficiency = net.minecraft.enchantment.EnchantmentHelper.getEfficiencyModifier(player);
        if (speed > 1F && efficiency > 0) speed += efficiency * efficiency + 1;
        if (player.isPotionActive(net.minecraft.potion.Potion.digSpeed))
            speed *= 1F + .2F * (player.getActivePotionEffect(net.minecraft.potion.Potion.digSpeed).getAmplifier() + 1);
        if (player.isPotionActive(net.minecraft.potion.Potion.digSlowdown)) {
            int level = player.getActivePotionEffect(net.minecraft.potion.Potion.digSlowdown).getAmplifier();
            speed *= level == 0 ? .3F : level == 1 ? .09F : level == 2 ? .0027F : .00081F;
        }
        if (player.isInsideOfMaterial(Material.water)
                && !net.minecraft.enchantment.EnchantmentHelper.getAquaAffinityModifier(player)) speed /= 5F;
        if (!player.onGround) speed /= 5F;
        speed = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed(player, this,
                world.getBlockMetadata(x, y, z), speed, x, y, z);
        return Math.max(0F, speed) / getBlockHardness(world, x, y, z) / 30F;
    }

    public static float hoeSpeed(net.minecraft.item.ItemHoe hoe) {
        return net.minecraft.item.Item.ToolMaterial.valueOf(hoe.getToolMaterialName()).getEfficiencyOnProperMaterial();
    }

    @Override public void harvestBlock(net.minecraft.world.World world,
            net.minecraft.entity.player.EntityPlayer player, int x, int y, int z, int metadata) {
        super.harvestBlock(world, player, x, y, z, metadata);
        net.minecraft.item.ItemStack stack = player.getHeldItem();
        if (!world.isRemote && hoeEffective && !player.capabilities.isCreativeMode
                && stack != null && stack.getItem().getClass() == net.minecraft.item.ItemHoe.class)
            stack.damageItem(1, player);
    }
}
