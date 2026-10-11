package com.mod.htgt6.minecraft1202.Module.Blocks.Frostburn;

import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/** 1.10 magma block: low light and hot-floor damage, with the vanilla exemptions. */
public final class BlockFrostburnMagma extends Block {
    private static final DamageSource HOT_FLOOR = new DamageSource("hotFloor").setFireDamage();

    public BlockFrostburnMagma() {
        super(Material.rock);
        setBlockName("magma");
        setBlockTextureName("htgt6:frostburn/magma");
        setHardness(0.5F);
        setHarvestLevel("pickaxe", 0);
        setResistance(Backport120Content.legacyResistance(0.5F));
        setLightLevel(0.2F);
        setStepSound(soundTypePiston);
        setCreativeTab(CreativeTabs.tabBlock);
    }

    @Override
    public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
        if (!world.isRemote && entity instanceof EntityLivingBase && !entity.isImmuneToFire()
                && !entity.isSneaking() && !hasFrostWalker((EntityLivingBase) entity)) {
            entity.attackEntityFrom(HOT_FLOOR, 1.0F);
        }
        super.onEntityWalking(world, x, y, z, entity);
    }

    private static boolean hasFrostWalker(EntityLivingBase entity) {
        ItemStack boots = entity.getEquipmentInSlot(1);
        return boots != null && CombatContent.frostWalker != null
                && EnchantmentHelper.getEnchantmentLevel(CombatContent.frostWalker.effectId, boots) > 0;
    }

    @Override public int getRenderType() { return FrostburnContent.getJsonRenderId(); }
}
