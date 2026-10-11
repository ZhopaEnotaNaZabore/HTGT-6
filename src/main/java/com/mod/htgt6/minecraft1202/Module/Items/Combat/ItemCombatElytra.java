package com.mod.htgt6.minecraft1202.Module.Items.Combat;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;

public final class ItemCombatElytra extends Item {
    public ItemCombatElytra() {
        setUnlocalizedName("elytra");
        setTextureName("htgt6:combat_update/elytra");
        setCreativeTab(CreativeTabs.tabTransport);
        setMaxStackSize(1);
        setMaxDamage(432);
    }
    @Override public boolean isValidArmor(ItemStack stack,int armorType,Entity entity){return armorType==1;}
    @Override public boolean getIsRepairable(ItemStack damaged,ItemStack material){return material!=null&&material.getItem()==OceanContent.phantomMembrane;}
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        ItemStack chest = player.inventory.armorInventory[2];
        if (chest == null) {
            player.inventory.armorInventory[2] = stack.copy();
            player.inventory.armorInventory[2].stackSize = 1;
            --stack.stackSize;
        }
        return stack;
    }
}
