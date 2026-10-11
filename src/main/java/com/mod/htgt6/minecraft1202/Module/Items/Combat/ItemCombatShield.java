package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldMechanics120;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.ShieldBannerData120;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemCombatShield extends Item {
    public ItemCombatShield() {
        setUnlocalizedName("shield");
        setTextureName("htgt6:combat_update/shield");
        setCreativeTab(CreativeTabs.tabCombat);
        setMaxStackSize(1);
        setMaxDamage(ShieldMechanics120.MAX_DAMAGE);
    }
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.block; }
    @Override public int getMaxItemUseDuration(ItemStack stack) { return ShieldMechanics120.USE_DURATION; }
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.OffhandSystem.isShieldDisabled(player)) {
            player.setItemInUse(stack, getMaxItemUseDuration(stack));
        }
        return stack;
    }
    @Override public boolean getIsRepairable(ItemStack shield, ItemStack ingredient) {
        if (ingredient == null) return false;
        Block block = Block.getBlockFromItem(ingredient.getItem());
        String name = block == null ? null : (String) Block.blockRegistry.getNameForObject(block);
        return ShieldMechanics120.isPlankRegistryName(name) || super.getIsRepairable(shield, ingredient);
    }
    @Override @SuppressWarnings("rawtypes") public void addInformation(ItemStack stack, EntityPlayer player,
            List lines, boolean advanced) {
        ShieldBannerData120.appendTooltip(stack, player, lines);
    }
}
