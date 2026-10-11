package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.HTGT6;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

/** Directional live wall-coral fan used by the 1.20.2 coral feature. */
public final class BlockWallCoralFan120 extends Block {
    private static final int[][] DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private final int coralType;

    public BlockWallCoralFan120(String typeName, int coralType) { this(typeName,coralType,false); }
    public BlockWallCoralFan120(String typeName, int coralType, boolean dead) {
        super(Material.water);
        this.coralType = coralType;
        setBlockName((dead?"dead_":"")+typeName + "_coral_wall_fan");
        setBlockTextureName(HTGT6.MOD_ID.toLowerCase() + ":"+(dead?"dead_":"") + typeName + "_coral_fan");
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
    }

    public int getCoralType() { return coralType; }
    public static int direction(int metadata) { return metadata & 3; }
    public static int dx(int direction) { return DIRECTIONS[direction & 3][0]; }
    public static int dz(int direction) { return DIRECTIONS[direction & 3][1]; }

    @Override public int getRenderType() { return OceanContent.getKelpRenderId(); }
    @Override public int getRenderBlockPass() { return 1; }
    @Override public boolean canRenderInPass(int pass) { return OceanContent.kelp.canRenderInPass(pass); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) { return true; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) { return null; }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return BlockKelp.isOpenWater(world.getBlock(x, y, z));
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        int direction = direction(world.getBlockMetadata(x, y, z));
        Block support = world.getBlock(x - dx(direction), y, z - dz(direction));
        if (!support.getMaterial().isSolid()) {
            world.setBlock(x, y, z, BlockKelp.waterFor(world, x, y, z), 0, 2);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oldBlock, int metadata) {
        super.breakBlock(world, x, y, z, oldBlock, metadata);
        BlockKelp.restoreWaterAfterRemoval(world, x, y, z);
    }
}
