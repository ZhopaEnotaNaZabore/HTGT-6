package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

/** Eight 1.20.2 instruments encoded as item metadata. */
public final class ItemBackport120GoatHorn extends Item {
    static final String[] INSTRUMENTS = {"ponder", "sing", "seek", "feel", "admire", "call", "yearn", "dream"};
    public ItemBackport120GoatHorn() {
        setUnlocalizedName("backport_1_20.goat_horn");
        setTextureName("htgt6:1_20_backport/goat_horn");
        setHasSubtypes(true);
        setMaxDamage(0);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabMisc);
    }
    @Override public int getMaxItemUseDuration(ItemStack stack) { return 140; }
    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.bow; }
    @Override public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        long ready = player.getEntityData().getLong("HTGT6GoatHornReady");
        if (world.getTotalWorldTime() < ready) return stack;
        player.getEntityData().setLong("HTGT6GoatHornReady", world.getTotalWorldTime() + 140L);
        int instrument = Math.max(0, Math.min(7, stack.getItemDamage()));
        world.playSoundAtEntity(player, "htgt6:item.goat_horn.sound." + instrument, 16.0F, 1.0F);
        player.setItemInUse(stack, getMaxItemUseDuration(stack));
        return stack;
    }
    @Override public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + INSTRUMENTS[Math.max(0, Math.min(7, stack.getItemDamage()))];
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int metadata = 0; metadata < 8; ++metadata) list.add(new ItemStack(item, 1, metadata));
    }
}
