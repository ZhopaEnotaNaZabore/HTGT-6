package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

/** Wooden fence with modern cross-family wooden-fence connections. */
public final class BlockBackport120Fence extends BlockFence {
    public BlockBackport120Fence(String name, String texture) {
        super("htgt6:1_20_backport/" + texture, com.mod.htgt6.minecraft1202.Module.Gameplay.Modern.NetherWoodMaterial116.forName(name));
        setBlockName("backport_1_20." + name);
        setHardness(2.0F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(Block.soundTypeWood);
    }

    @Override public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block instanceof BlockFence || block instanceof net.minecraft.block.BlockFenceGate) return true;
        return block.getMaterial().isOpaque() && block.renderAsNormalBlock()
                && block.getMaterial() != Material.gourd;
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
}
