package com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;

import com.mod.htgt6.HTGT6;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

/**
 * 1.13-style kelp adapted to the pre-waterlogging block model. The block uses
 * water material, so it remains swimmable and is also recognised by GT6 ocean
 * generation. Only the exposed top grows; lower stem ticks immediately exit.
 */
public final class BlockKelp extends Block implements IGrowable {
    private static int activeRenderPass;
    private IIcon topIcon;

    public BlockKelp() {
        super(Material.water);
        setBlockName("kelp");
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setTickRandomly(true);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockTextureName(HTGT6.MOD_ID.toLowerCase() + ":kelp");
        setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 1.0F, 0.875F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        topIcon = register.registerIcon(HTGT6.MOD_ID.toLowerCase() + ":kelp");
        blockIcon = topIcon;
    }

    @Override
    public int getRenderType() { return OceanContent.getKelpRenderId(); }

    @Override
    public int getRenderBlockPass() { return 1; }

    @Override
    public boolean canRenderInPass(int pass) {
        activeRenderPass = pass;
        return pass == 0 || pass == 1;
    }

    public static int getActiveRenderPass() { return activeRenderPass; }

    @Override
    public boolean isOpaqueCube() { return false; }

    @Override
    public boolean renderAsNormalBlock() { return false; }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }

    @Override
    public boolean getBlocksMovement(net.minecraft.world.IBlockAccess world, int x, int y, int z) { return true; }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return isFullWater(world, x, y, z) && canRootOn(world.getBlock(x, y - 1, z));
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        if (!canRootOn(world.getBlock(x, y - 1, z))) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlock(x, y, z, waterFor(world, x, y, z), 0, 3);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote) return;
        int age = world.getBlockMetadata(x, y, z);
        // Values 8+ are rendered by the legacy fluid renderer as a full source
        // block. Older 0-7 kelp cells are upgraded on their next random tick.
        if (age < 8) {
            age = 8;
            world.setBlockMetadataWithNotify(x, y, z, age, 2);
        }
        // Vanilla 1.20 uses a 0.14 random-tick growth probability.
        if (age < 15 && random.nextDouble() < 0.14D && isOpenWater(world.getBlock(x, y + 1, z))) {
            grow(world, x, y, z, age + 1);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (world.getBlockMetadata(x, y, z) < 8) {
            world.setBlockMetadataWithNotify(x, y, z, 8, 2);
        }
    }

    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        return isOpenWater(world.getBlock(x, y + 1, z));
    }

    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) {
        return true;
    }

    @Override
    public void func_149853_b(World world, Random random, int x, int y, int z) {
        if (world.isRemote || !func_149851_a(world, x, y, z, false)) return;
        int age = Math.max(8, world.getBlockMetadata(x, y, z));
        grow(world, x, y, z, Math.min(15, age + 1));
    }

    private void grow(World world, int x, int y, int z, int age) {
        // Keep the original head when placement is denied by the world.
        if (world.setBlock(x, y + 1, z, this, age, 2)) {
            world.setBlock(x, y, z, OceanContent.kelpPlant, 0, 2);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMetadata) {
        super.breakBlock(world, x, y, z, oldBlock, oldMetadata);
        restoreWaterAfterRemoval(world, x, y, z);
    }

    @Override
    public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random random) { return 1; }

    static boolean canRootOn(Block below) {
        return isKelp(below) || (below != Blocks.air && !isWater(below)
                && below.getMaterial().blocksMovement());
    }

    public static boolean isKelp(Block block) {
        return block == OceanContent.kelp || block == OceanContent.kelpPlant;
    }

    public static boolean isWater(Block block) {
        return block == Blocks.water || block == Blocks.flowing_water
                || block.getMaterial() == Material.water;
    }

    public static boolean isOpenWater(Block block) {
        // Material.water is also used by waterlogged plants; it does not mean
        // that the block can safely be replaced. Accept actual legacy liquids
        // and Forge water-fluid blocks (including GT6 ocean/river/swamp).
        return block == Blocks.water || block == Blocks.flowing_water
                || block instanceof net.minecraft.block.BlockLiquid && block.getMaterial() == Material.water
                || block instanceof net.minecraftforge.fluids.IFluidBlock
                && block.getMaterial() == Material.water;
    }

    public static boolean isFullWater(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (!isOpenWater(block)) return false;
        if (block instanceof net.minecraft.block.BlockLiquid) {
            int level = world.getBlockMetadata(x, y, z);
            return level == 0 || level >= 8;
        }
        return true; // GT6 ocean/river/swamp cells have their own metadata contract.
    }

    static Block waterFor(World world, int x, int y, int z) {
        Block[] neighbors = {
                world.getBlock(x + 1, y, z), world.getBlock(x - 1, y, z),
                world.getBlock(x, y, z + 1), world.getBlock(x, y, z - 1),
                world.getBlock(x, y + 1, z)
        };
        for (Block block : neighbors) if (isOpenWater(block)) return block;
        return Blocks.water;
    }

    static void restoreWaterAfterRemoval(World world, int x, int y, int z) {
        // 1.20 plants are waterlogged. In the 1.7 single-block-state model the
        // plant itself occupies that cell, so explicitly restore its water.
        if (!world.isRemote && world.getBlock(x, y, z) == Blocks.air) {
            world.setBlock(x, y, z, waterFor(world, x, y, z), 0, 2);
        }
    }
}
