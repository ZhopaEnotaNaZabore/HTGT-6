package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Sealed shipwreck loot which creates the vanilla filled map only when opened. */
public final class ItemTreasureMapScroll120 extends Item {
    public ItemTreasureMapScroll120() {
        setUnlocalizedName("buried_treasure_map_scroll");
        setTextureName("htgt6:treasure_map/filled_map");
        setCreativeTab(CreativeTabs.tabMisc);
        setMaxStackSize(1);
    }

    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    private net.minecraft.util.IIcon markings;

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister register) {
        super.registerIcons(register);
        markings = register.registerIcon("htgt6:treasure_map/filled_map_markings");
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public boolean requiresMultipleRenderPasses() { return true; }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public net.minecraft.util.IIcon getIconFromDamageForRenderPass(int damage, int pass) {
        return pass == 0 ? itemIcon : markings;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) return stack;
        ItemStack opened = VanillaTreasureMap120.open(stack, world);
        if (opened != stack) world.playSoundAtEntity(player, "random.pop", 0.5F, 1.0F);
        return opened;
    }
}
