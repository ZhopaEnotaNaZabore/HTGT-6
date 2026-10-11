package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/** Tip and body variants of hanging cave vines. Metadata bit 3 stores berries. */
public final class BlockBackport120CaveVines extends Block implements IGrowable {
    public static final int BERRIES = 8;
    private final boolean tip;

    public BlockBackport120CaveVines(boolean tip) {
        super(Material.plants);
        this.tip = tip;
        setBlockName("backport_1_20." + (tip ? "cave_vines" : "cave_vines_plant"));
        setBlockTextureName("htgt6:1_20_backport/" + (tip ? "cave_vines" : "cave_vines_plant"));
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        if (tip) setCreativeTab(CreativeTabs.tabDecorations);
        setTickRandomly(tip);
        setBlockBounds(0.1F, 0, 0.1F, 0.9F, 1, 0.9F);
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }
    @Override public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return (world.getBlockMetadata(x, y, z) & BERRIES) != 0 ? 14 : 0;
    }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return (metadata & BERRIES) != 0 ? Backport120Content.glowBerries : null;
    }
    @Override public int quantityDropped(int metadata, int fortune, Random random) {
        return (metadata & BERRIES) != 0 ? 1 : 0;
    }

    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return super.canPlaceBlockAt(world, x, y, z) && canHangFrom(world, x, y, z);
    }
    @Override public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!canHangFrom(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
    }
    private boolean canHangFrom(World world, int x, int y, int z) {
        Block above = world.getBlock(x, y + 1, z);
        return above == Backport120Content.caveVines || above == Backport120Content.caveVinesPlant
                || world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!tip || world.isRemote) return;
        int state = world.getBlockMetadata(x, y, z);
        int age = state & 7;
        if (age >= 7 || random.nextInt(10) != 0 || !world.isAirBlock(x, y - 1, z)) return;
        world.setBlock(x, y, z, Backport120Content.caveVinesPlant, state & BERRIES, 2);
        int next = age + 1;
        int berries = random.nextInt(11) == 0 ? BERRIES : 0;
        world.setBlock(x, y - 1, z, Backport120Content.caveVines, next | berries, 3);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        int state = world.getBlockMetadata(x, y, z);
        if ((state & BERRIES) == 0) return false;
        if (!world.isRemote) {
            dropBlockAsItem(world, x, y, z, new ItemStack(Backport120Content.glowBerries,
                    1 + world.rand.nextInt(2)));
            world.setBlockMetadataWithNotify(x, y, z, state & ~BERRIES, 3);
        }
        return true;
    }

    @Override public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        return (world.getBlockMetadata(x, y, z) & BERRIES) == 0;
    }
    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) { return true; }
    @Override public void func_149853_b(World world, Random random, int x, int y, int z) {
        int state = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, state | BERRIES, 3);
    }
}
