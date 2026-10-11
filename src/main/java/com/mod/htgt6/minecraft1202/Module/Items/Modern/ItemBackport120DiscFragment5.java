package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Disc fragment 5, including the source item's identifying tooltip. */
public final class ItemBackport120DiscFragment5 extends Item {
    public ItemBackport120DiscFragment5() {
        setUnlocalizedName("backport_1_20.disc_fragment_5");
        setTextureName("htgt6:1_20_backport/disc_fragment_5");
        setCreativeTab(CreativeTabs.tabMaterials);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        lines.add(StatCollector.translateToLocal("item.backport_1_20.disc_fragment_5.desc"));
    }
}
