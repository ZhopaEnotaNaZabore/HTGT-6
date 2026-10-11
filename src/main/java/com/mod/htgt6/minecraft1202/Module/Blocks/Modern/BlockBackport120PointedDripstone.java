package com.mod.htgt6.minecraft1202.Module.Blocks.Modern;

import com.mod.htgt6.minecraft1202.Module.Entity.Modern.EntityFallingPointedDripstone;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

/** Minecraft 1.20.2 pointed dripstone encoded in the 4-bit 1.7.10 block metadata. */
public final class BlockBackport120PointedDripstone extends BlockFalling {
    public static final int DOWN_BIT = 8;
    public static final int TIP = 0;
    public static final int TIP_MERGE = 1;
    public static final int FRUSTUM = 2;
    public static final int MIDDLE = 3;
    public static final int BASE = 4;

    private static final int FALL_DELAY = 2;
    private static final int MAX_GROWTH_LENGTH = 7;
    private static final int STALAGMITE_SEARCH = 10;
    private static final float GROWTH_CHANCE = 0.011377778F;

    public BlockBackport120PointedDripstone() {
        super(Material.rock);
        setBlockName("backport_1_20.pointed_dripstone");
        setHardness(1.5F);
        setResistance(Backport120Content.legacyResistance(3.0F));
        setStepSound(Block.soundTypePiston);
        setCreativeTab(CreativeTabs.tabBlock);
        setHarvestLevel("pickaxe", 0);
        setTickRandomly(true);
    }

    public static boolean isDown(int metadata) { return (metadata & DOWN_BIT) != 0; }
    public static int thickness(int metadata) { return metadata & 7; }
    public static int metadata(boolean down, int thickness) {
        return (down ? DOWN_BIT : 0) | Math.max(TIP, Math.min(BASE, thickness));
    }

    public static String thicknessName(int value) {
        switch (value) {
            case TIP_MERGE: return "tip_merge";
            case FRUSTUM: return "frustum";
            case MIDDLE: return "middle";
            case BASE: return "base";
            default: return "tip";
        }
    }

    @Override public int getRenderType() { return Backport120Content.getJsonRenderId(); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderBlockPass() { return 0; }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int shape = thickness(world.getBlockMetadata(x, y, z));
        float radius = shape == BASE ? 0.375F : shape == MIDDLE ? 0.3125F
                : shape == FRUSTUM ? 0.25F : 0.1875F;
        int metadata = world.getBlockMetadata(x, y, z);
        float minY = isDown(metadata) && shape == TIP ? 0.3125F : 0.0F;
        float maxY = !isDown(metadata) && shape == TIP ? 0.6875F : 1.0F;
        setBlockBounds(0.5F - radius, minY, 0.5F - radius, 0.5F + radius, maxY, 0.5F + radius);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override public int damageDropped(int metadata) { return 0; }
    @Override public Item getItemDropped(int metadata, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (world.isRemote) return;
        int state = world.getBlockMetadata(x, y, z);
        if (!canSurvive(world, x, y, z, isDown(state))) {
            if (isDown(state)) world.scheduleBlockUpdate(x, y, z, this, FALL_DELAY);
            else world.setBlockToAir(x, y, z);
        } else {
            refreshColumn(world, x, y, z);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        int state = world.getBlockMetadata(x, y, z);
        if (!canSurvive(world, x, y, z, isDown(state))) {
            if (isDown(state)) world.scheduleBlockUpdate(x, y, z, this, FALL_DELAY);
            else world.setBlockToAir(x, y, z);
            return;
        }
        refreshColumn(world, x, y, z);
    }

    @Override public int tickRate(World world) { return FALL_DELAY; }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (world.isRemote || world.getBlock(x, y, z) != this) return;
        int state = world.getBlockMetadata(x, y, z);
        if (!canSurvive(world, x, y, z, isDown(state))) {
            if (isDown(state)) fallStalactite(world, x, y, z);
            else world.setBlockToAir(x, y, z);
            return;
        }
        transferFluidToCauldron(world, x, y, z, random);
        if (random.nextFloat() < GROWTH_CHANCE) grow(world, x, y, z, random);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        if (random.nextFloat() >= 0.02F || !isDownTip(world, x, y, z)) return;
        Block fluid = sourceFluidAboveRoot(world, x, y, z);
        if (fluid != Blocks.water && fluid != Blocks.lava) return;
        world.spawnParticle(fluid == Blocks.lava ? "dripLava" : "dripWater",
                x + 0.5D, y + 0.3125D, z + 0.5D, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected void func_149829_a(EntityFallingBlock falling) {
        falling.func_145806_a(true); // legacy defaults are the modern 2x damage and 40 damage cap
    }

    @Override
    public void func_149828_a(World world, int x, int y, int z, int metadata) {
        world.setBlockToAir(x, y, z); // falling stalactites shatter rather than re-forming a column
    }

    @Override
    public void onFallenUpon(World world, int x, int y, int z, Entity entity, float distance) {
        int state = world.getBlockMetadata(x, y, z);
        if (!isDown(state) && thickness(state) == TIP && distance > 1.0F) {
            float damage = Math.min(40.0F, (float)Math.ceil(distance - 1.0F) * 2.0F);
            entity.attackEntityFrom(DamageSource.fall, damage);
        }
    }

    private void fallStalactite(World world, int x, int y, int z) {
        int bottom = y;
        while (bottom > 0 && isSameDirection(world, x, bottom - 1, z, true)) --bottom;
        int stalactiteLength = y - bottom + 1;
        for (int current = bottom; current <= y && world.getBlock(x, current, z) == this; ++current) {
            int state = world.getBlockMetadata(x, current, z);
            if (!isDown(state)) continue;
            EntityFallingBlock falling = thickness(state) == TIP
                    ? new EntityFallingPointedDripstone(world, x + 0.5D, current + 0.5D,
                            z + 0.5D, state, Math.min(stalactiteLength, 6))
                    : new EntityFallingBlock(world, x + 0.5D, current + 0.5D, z + 0.5D, this, state);
            world.spawnEntityInWorld(falling);
        }
    }

    private void grow(World world, int x, int y, int z, Random random) {
        int state = world.getBlockMetadata(x, y, z);
        if (!isDown(state) || thickness(state) != TIP) return;

        int root = y;
        int length = 1;
        while (length <= MAX_GROWTH_LENGTH && isSameDirection(world, x, root + 1, z, true)) {
            ++root;
            ++length;
        }
        if (length > MAX_GROWTH_LENGTH || world.getBlock(x, root + 1, z) != Backport120Content.dripstoneBlock
                || world.getBlock(x, root + 2, z) != Blocks.water
                || world.getBlockMetadata(x, root + 2, z) != 0) return;

        if (random.nextBoolean() && world.isAirBlock(x, y - 1, z)) {
            world.setBlock(x, y - 1, z, this, metadata(true, TIP), 3);
            refreshColumn(world, x, y - 1, z);
            return;
        }

        for (int distance = 1; distance <= STALAGMITE_SEARCH && y - distance > 0; ++distance) {
            int targetY = y - distance;
            if (!world.isAirBlock(x, targetY, z)) {
                int placeY = targetY + 1;
                if (placeY < y && world.isAirBlock(x, placeY, z)
                        && canSurvive(world, x, placeY, z, false)) {
                    world.setBlock(x, placeY, z, this, metadata(false, TIP), 3);
                    refreshColumn(world, x, placeY, z);
                }
                break;
            }
        }
    }

    private void transferFluidToCauldron(World world, int x, int y, int z, Random random) {
        if (!isDownTip(world, x, y, z)) return;
        Block fluid = sourceFluidAboveRoot(world, x, y, z);
        float chance = fluid == Blocks.water ? 0.17578125F : fluid == Blocks.lava ? 0.05859375F : 0.0F;
        if (chance == 0.0F || random.nextFloat() >= chance) return;
        for (int distance = 1; distance <= 11 && y - distance >= 0; ++distance) {
            Block below = world.getBlock(x, y - distance, z);
            if (below == Blocks.cauldron) {
                // 1.7.10 has only the three-level water cauldron; lava cauldrons have no legal state.
                if (fluid == Blocks.water) {
                    int level = world.getBlockMetadata(x, y - distance, z);
                    if (level < 3) world.setBlockMetadataWithNotify(x, y - distance, z, level + 1, 3);
                }
                return;
            }
            if (!below.isAir(world, x, y - distance, z) && below != this) return;
        }
    }

    private static boolean isDownTip(IBlockAccess world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != Backport120Content.pointedDripstone) return false;
        int state = world.getBlockMetadata(x, y, z);
        return isDown(state) && thickness(state) == TIP;
    }

    private static Block sourceFluidAboveRoot(IBlockAccess world, int x, int y, int z) {
        int root = y;
        while (root < world.getHeight() - 3 && isSameDirection(world, x, root + 1, z, true)) ++root;
        if (world.getBlock(x, root + 1, z) != Backport120Content.dripstoneBlock) return Blocks.air;
        Block fluid = world.getBlock(x, root + 2, z);
        return (fluid == Blocks.water || fluid == Blocks.lava) && world.getBlockMetadata(x, root + 2, z) == 0
                ? fluid : Blocks.air;
    }

    public static boolean canSurvive(World world, int x, int y, int z, boolean down) {
        int supportY = y + (down ? 1 : -1);
        Block support = world.getBlock(x, supportY, z);
        if (support == Backport120Content.pointedDripstone) {
            return isDown(world.getBlockMetadata(x, supportY, z)) == down;
        }
        return world.isSideSolid(x, supportY, z, down ? ForgeDirection.DOWN : ForgeDirection.UP);
    }

    private static boolean isSameDirection(IBlockAccess world, int x, int y, int z, boolean down) {
        return world.getBlock(x, y, z) == Backport120Content.pointedDripstone
                && isDown(world.getBlockMetadata(x, y, z)) == down;
    }

    private void refreshColumn(World world, int x, int y, int z) {
        for (int current = y - 16; current <= y + 16; ++current) {
            if (current < 0 || current >= world.getHeight() || world.getBlock(x, current, z) != this) continue;
            int oldState = world.getBlockMetadata(x, current, z);
            boolean down = isDown(oldState);
            int shape = computeThickness(world, x, current, z, down);
            int newState = metadata(down, shape);
            if (newState != oldState) world.setBlockMetadataWithNotify(x, current, z, newState, 2);
        }
    }

    private int computeThickness(IBlockAccess world, int x, int y, int z, boolean down) {
        int step = down ? -1 : 1;
        int nextY = y + step;
        if (world.getBlock(x, nextY, z) != this) return TIP;
        int nextState = world.getBlockMetadata(x, nextY, z);
        if (isDown(nextState) != down) return TIP_MERGE;

        int nextShape = thickness(nextState);
        int behindY = y - step;
        if (!isSameDirection(world, x, behindY, z, down)) return BASE;
        return nextShape == FRUSTUM || nextShape == MIDDLE ? MIDDLE : FRUSTUM;
    }

    @SideOnly(Side.CLIENT)
    @Override public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("htgt6:1_20_backport/pointed_dripstone_up_tip");
    }

    @SideOnly(Side.CLIENT)
    @Override public IIcon getIcon(int side, int metadata) { return blockIcon; }
}
