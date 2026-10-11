package com.mod.htgt6.minecraft1202.Module.Items.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockTurtleEgg120;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Allows the vanilla 1.20.2 one-to-four egg stacking interaction. */
public final class ItemBlockTurtleEgg120 extends ItemBlock {
    public ItemBlockTurtleEgg120(Block block) { super(block); }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ) {
        if (world.getBlock(x, y, z) == field_150939_a) {
            int meta = world.getBlockMetadata(x, y, z);
            int eggs = BlockTurtleEgg120.eggCount(meta);
            if (eggs < 4) {
                if (!world.isRemote) {
                    world.setBlockMetadataWithNotify(x, y, z,
                            BlockTurtleEgg120.metadata(eggs + 1,
                                    BlockTurtleEgg120.hatchStage(meta)), 2);
                    if (!player.capabilities.isCreativeMode) stack.stackSize--;
                }
                return true;
            }
        }
        return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
    }
}
