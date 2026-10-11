package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import java.util.Collections;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** The legacy hoe is not an ItemTool: it needs its own material/harvest contract. */
public class ItemNetheriteHoe116 extends ItemHoe {
    public ItemNetheriteHoe116(ToolMaterial material) { super(material); }

    @Override public boolean getIsRepairable(ItemStack tool, ItemStack material) {
        ItemStack repair = theToolMaterial.getRepairItemStack();
        return material != null && repair != null && material.getItem() == repair.getItem()
                && material.getItemDamage() == repair.getItemDamage()
                || super.getIsRepairable(tool, material);
    }
    @Override public int getItemEnchantability() { return theToolMaterial.getEnchantability(); }
    /** Called by the legacy Enchantment.canApply adapter for both table and anvil. */
    public static boolean allowsEnchantment(ItemStack stack,net.minecraft.enchantment.Enchantment enchantment){
        if(stack==null||stack.stackSize<=0||!(stack.getItem() instanceof ItemHoe))return false;
        return enchantment==net.minecraft.enchantment.Enchantment.efficiency
                ||enchantment==net.minecraft.enchantment.Enchantment.silkTouch
                ||enchantment==net.minecraft.enchantment.Enchantment.fortune;
    }
    @Override public Set<String> getToolClasses(ItemStack stack) { return Collections.singleton("hoe"); }
    @Override public int getHarvestLevel(ItemStack stack, String toolClass) {
        return "hoe".equals(toolClass) ? theToolMaterial.getHarvestLevel() : -1;
    }
    @Override public float getDigSpeed(ItemStack stack, Block block, int metadata) {
        // 1.20.2 mineable/hoe: native leaves/hay/sponge plus backports declaring the hoe tool.
        if (block.getMaterial() == Material.leaves || block == Blocks.hay_block || block == Blocks.sponge
                || block == com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent.sponge
                || block == com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.netherWartBlock
                || "hoe".equals(block.getHarvestTool(metadata))) return theToolMaterial.getEfficiencyOnProperMaterial();
        return 1.0F;
    }
    @Override public boolean onBlockDestroyed(ItemStack stack, World world, Block block,
            int x, int y, int z, EntityLivingBase miner) {
        if (!world.isRemote && block.getBlockHardness(world, x, y, z) != 0.0F) stack.damageItem(1, miner);
        return true;
    }
    @Override public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(2, attacker);
        return true;
    }
}
