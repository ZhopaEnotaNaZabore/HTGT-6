package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.HTGT6;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** Non-random-ticking stem segment; only {@link BlockKelp} tops can grow. */
public final class BlockKelpPlant extends Block implements IGrowable {
    public BlockKelpPlant() {
        super(Material.water);
        setBlockName("kelp_plant");
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setBlockTextureName(HTGT6.MOD_ID.toLowerCase() + ":kelp_plant");
        setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
    }

    @Override public int getRenderType() { return OceanContent.getKelpRenderId(); }
    @Override public int getRenderBlockPass() { return 1; }
    @Override public boolean canRenderInPass(int pass) {
        // The shared renderer reads the pass selected by the render thread.
        return OceanContent.kelp.canRenderInPass(pass);
    }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) { return true; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        if (!BlockKelp.canRootOn(world.getBlock(x, y - 1, z))) {
            dropBlockAsItem(world, x, y, z, 0, 0);
            world.setBlock(x, y, z, BlockKelp.waterFor(world, x, y, z), 0, 3);
        } else if (!BlockKelp.isKelp(world.getBlock(x, y + 1, z))) {
            world.setBlock(x, y, z, OceanContent.kelp, 8 + world.rand.nextInt(8), 2);
        }
    }

    private int findHead(World world, int x, int y, int z) {
        while (y < world.getHeight() && world.getBlock(x, y, z) == this) y++;
        return y < world.getHeight() && world.getBlock(x, y, z) == OceanContent.kelp ? y : -1;
    }

    @Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        int head = findHead(world, x, y, z);
        return head >= 0 && OceanContent.kelp.func_149851_a(world, x, head, z, client);
    }

    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) {
        return true;
    }

    @Override public void func_149853_b(World world, Random random, int x, int y, int z) {
        if (world.isRemote) return;
        int head = findHead(world, x, y, z);
        if (head >= 0) OceanContent.kelp.func_149853_b(world, random, x, head, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMetadata) {
        super.breakBlock(world, x, y, z, oldBlock, oldMetadata);
        BlockKelp.restoreWaterAfterRemoval(world, x, y, z);
    }

    @Override
    public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(OceanContent.kelp);
    }

    @Override public int quantityDropped(Random random) { return 1; }
}
