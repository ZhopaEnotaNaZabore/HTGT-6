package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120PointedDripstone;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/** Chooses the vertical direction before the legacy ItemBlock placement commits metadata. */
public final class ItemBlockBackport120PointedDripstone extends ItemBlock {
    @SideOnly(Side.CLIENT) private IIcon itemIcon;

    public ItemBlockBackport120PointedDripstone(Block block) { super(block); }

    @SideOnly(Side.CLIENT) @Override public int getSpriteNumber() { return 1; }
    @SideOnly(Side.CLIENT) @Override public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("htgt6:1_20_backport/pointed_dripstone");
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIconFromDamage(int damage) { return itemIcon; }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                                int side, float hitX, float hitY, float hitZ, int metadata) {
        boolean canUp = BlockBackport120PointedDripstone.canSurvive(world, x, y, z, false);
        boolean canDown = BlockBackport120PointedDripstone.canSurvive(world, x, y, z, true);
        if (!canUp && !canDown) return false;

        boolean down;
        if (side == 0 && canDown) down = true;
        else if (side == 1 && canUp) down = false;
        else if (canUp && canDown) down = player.rotationPitch < 0.0F;
        else down = canDown;

        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ,
                BlockBackport120PointedDripstone.metadata(down, BlockBackport120PointedDripstone.TIP));
    }
}
