package com.mod.htgt6.minecraft1202.Module.Items.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** ItemBed's exact two-block placement contract, parameterized by the colored block. */
public final class ItemBlockColoredBed120 extends ItemBlock {
    public ItemBlockColoredBed120(Block block) {
        super(block);
        setMaxStackSize(1);
        BlockColoredBed120 bed = (BlockColoredBed120)block;
        setTextureName("htgt6:village_pillage/bed/" + VillagePillageContent.BED_COLORS[bed.getColor()] + "_item");
    }

    /** ItemBlock normally registers and renders a block-atlas face; beds use a flat item-atlas sprite. */
    @SideOnly(Side.CLIENT)
    @Override public int getSpriteNumber() { return 1; }

    @SideOnly(Side.CLIENT)
    @Override public void registerIcons(IIconRegister register) { itemIcon = register.registerIcon(getIconString()); }

    @SideOnly(Side.CLIENT)
    @Override public IIcon getIconFromDamage(int damage) { return itemIcon; }

    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
            int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (stack == null || stack.stackSize <= 0) return false;
        if (world.isRemote) return true;
        if (side != 1) return false;
        ++y;
        int facing = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + .5D) & 3;
        int dx = facing == 1 ? -1 : facing == 3 ? 1 : 0;
        int dz = facing == 0 ? 1 : facing == 2 ? -1 : 0;
        if (!player.canPlayerEdit(x, y, z, side, stack)
                || !player.canPlayerEdit(x + dx, y, z + dz, side, stack)) return false;
        if (!world.isAirBlock(x, y, z) || !world.isAirBlock(x + dx, y, z + dz)) return false;
        if (!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)
                || !World.doesBlockHaveSolidTopSurface(world, x + dx, y - 1, z + dz)) return false;
        Block bed = field_150939_a;
        if (!world.setBlock(x, y, z, bed, facing, 3) || world.getBlock(x, y, z) != bed) return false;
        if (!world.setBlock(x + dx, y, z + dz, bed, facing | 8, 3)
                || world.getBlock(x + dx, y, z + dz) != bed
                || world.getBlock(x, y, z) != bed) {
            // Only undo our halves; a placement hook may have substituted another block.
            if (world.getBlock(x, y, z) == bed) world.setBlock(x, y, z, net.minecraft.init.Blocks.air, 0, 2);
            if (world.getBlock(x + dx, y, z + dz) == bed)
                world.setBlock(x + dx, y, z + dz, net.minecraft.init.Blocks.air, 0, 2);
            return false;
        }
        if (!player.capabilities.isCreativeMode) --stack.stackSize;
        return true;
    }
}
