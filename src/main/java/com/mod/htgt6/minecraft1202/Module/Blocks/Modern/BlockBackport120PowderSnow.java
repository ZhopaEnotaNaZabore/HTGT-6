package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/** Powder snow with leather-boots support, sinking slowdown, and persistent freezing exposure. */
public final class BlockBackport120PowderSnow extends Block {
    private static final DamageSource FREEZE = new DamageSource("freeze").setDamageBypassesArmor();
    public BlockBackport120PowderSnow() {
        super(Material.craftedSnow);
        setBlockName("backport_1_20.powder_snow");
        setBlockTextureName("htgt6:1_20_backport/powder_snow");
        setHardness(0.25F);
        setResistance(Backport120Content.legacyResistance(0.25F));
        setStepSound(soundTypeSnow);
        setCreativeTab(CreativeTabs.tabBlock);
    }
    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return null; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask,
                                        List boxes, Entity entity) {
        if (wearingLeatherBoots(entity) && entity.fallDistance < 2.5F) {
            AxisAlignedBB surface = AxisAlignedBB.getBoundingBox(x, y + 0.9D, z, x + 1, y + 1, z + 1);
            if (mask.intersectsWith(surface)) boxes.add(surface);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        entity.motionX *= 0.9D;
        entity.motionZ *= 0.9D;
        entity.motionY *= 0.95D;
        long now = world.getTotalWorldTime();
        long last = entity.getEntityData().getLong("HTGT6PowderSnowLast");
        int ticks = last + 1L == now ? entity.getEntityData().getInteger("HTGT6PowderSnowTicks") + 1 : 1;
        entity.getEntityData().setLong("HTGT6PowderSnowLast", now);
        entity.getEntityData().setInteger("HTGT6PowderSnowTicks", ticks);
        if (!world.isRemote && ticks > 140 && ticks % 40 == 0) {
            int leather = leatherPieces(entity);
            if (leather < 4) entity.attackEntityFrom(FREEZE, 1.0F);
        }
    }

    private static boolean wearingLeatherBoots(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return false;
        ItemStack boots = ((EntityLivingBase)entity).getEquipmentInSlot(1);
        return boots != null && boots.getItem() == Items.leather_boots;
    }
    private static int leatherPieces(Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return 0;
        EntityLivingBase living = (EntityLivingBase)entity;
        Item[] leather = {Items.leather_boots, Items.leather_leggings, Items.leather_chestplate, Items.leather_helmet};
        int count = 0;
        for (int slot = 1; slot <= 4; ++slot) {
            ItemStack stack = living.getEquipmentInSlot(slot);
            if (stack != null && stack.getItem() == leather[slot - 1]) ++count;
        }
        return count;
    }
}
