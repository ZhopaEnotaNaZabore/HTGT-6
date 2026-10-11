package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/** Preserves the metadata-backed Aquatic plant identity in inventory stacks. */
public final class ItemBlockMarinePlant extends ItemBlock {
    private static final String[] NAMES = {
            "seagrass", "seagrass", "seagrass",
            "tube_coral", "brain_coral", "bubble_coral", "fire_coral", "horn_coral",
            "sea_pickle", "sea_pickle", "sea_pickle", "sea_pickle"
    };

    public ItemBlockMarinePlant(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override public int getMetadata(int damage) {
        // Old inventory stacks may contain block-state metadata. One item always
        // places one pickle or one short seagrass, never an orphan upper half.
        int value = Math.max(0, Math.min(11, damage));
        return value <= 2 ? 0 : Math.min(8, value);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world,
                                int x, int y, int z, int side, float hitX, float hitY, float hitZ,
                                int metadata) {
        if (metadata == 0 && !com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockMarinePlant
                .supportsSeagrass(world, x, y, z)) return false;
        return super.placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, metadata);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        int metadata = world.getBlockMetadata(x, y, z);
        if (stack.stackSize > 0 && getMetadata(stack.getItemDamage()) == 8
                && !player.isSneaking() && world.getBlock(x, y, z) == field_150939_a
                && metadata >= 8 && metadata < 11) {
            if (!world.canMineBlock(player, x, y, z)
                    || !player.canPlayerEdit(x, y, z, side, stack)) return false;
            // ItemStack.tryPlaceItemIntoWorld owns Forge placement snapshots/events,
            // as it does for vanilla ItemSlab. Keep this mutation in that path.
            if (world.isRemote) return true;
            if (!world.setBlock(x, y, z, field_150939_a, metadata + 1, 3)) return false;
            world.playSoundEffect(x + .5D, y + .5D, z + .5D,
                    field_150939_a.stepSound.func_150496_b(),
                    (field_150939_a.stepSound.getVolume() + 1F) / 2F,
                    field_150939_a.stepSound.getPitch() * .8F);
            if (!player.capabilities.isCreativeMode) --stack.stackSize;
            return true;
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int metadata = Math.max(0, Math.min(NAMES.length - 1, stack.getItemDamage()));
        return "tile.marine_plant." + NAMES[metadata];
    }
}
