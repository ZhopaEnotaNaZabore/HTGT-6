package com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Native generated-item sprite while retaining ItemBlock placement and the existing registry ID. */
public final class ItemBlockBell120 extends ItemBlock {
    public ItemBlockBell120(Block block) { super(block); }
    @Override public boolean placeBlockAt(net.minecraft.item.ItemStack stack, net.minecraft.entity.player.EntityPlayer player,
            net.minecraft.world.World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ, int metadata) {
        boolean placed = super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
        // A failed wall attachment can fall back to floor/ceiling. Vanilla keeps the wall axis,
        // but 1.7's later onBlockPlacedBy callback has no clicked-side argument.
        if (placed && side >= 2 && world.getBlock(x, y, z) == field_150939_a)
            world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
        return placed;
    }
    @Override @SideOnly(Side.CLIENT) public int getSpriteNumber() { return 1; }
    @Override @SideOnly(Side.CLIENT) public void registerIcons(IIconRegister icons) {
        itemIcon = icons.registerIcon("htgt6:village_pillage/bell");
    }
    @Override @SideOnly(Side.CLIENT) public IIcon getIconFromDamage(int damage) { return itemIcon; }
}
