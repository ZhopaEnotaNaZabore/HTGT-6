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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Metadata aquatic flora: seagrass, tall seagrass, five corals and sea pickles. */
public final class BlockMarinePlant extends Block implements IShearable, IGrowable {
    private final IIcon[] icons = new IIcon[12];
    private static final String[] NAMES = {
            "seagrass", "tall_seagrass_bottom", "tall_seagrass_top",
            "tube_coral", "brain_coral", "bubble_coral", "fire_coral", "horn_coral",
            "sea_pickle", "sea_pickle", "sea_pickle", "sea_pickle"
    };

    public BlockMarinePlant() {
        super(Material.water);
        setBlockName("marine_plant");
        setHardness(0.0F);
        setStepSound(soundTypeGrass);
        setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0.1F, 0.0F, 0.1F, 0.9F, 0.85F, 0.9F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        for (int i = 0; i < icons.length; i++) {
            icons[i] = register.registerIcon(HTGT6.MOD_ID.toLowerCase() + ":" + NAMES[i]);
        }
        blockIcon = icons[0];
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) { return icons[Math.max(0, Math.min(icons.length - 1, meta))]; }
    @Override public int getRenderType() { return OceanContent.getKelpRenderId(); }
    @Override public int getRenderBlockPass() { return 1; }
    @Override public boolean canRenderInPass(int pass) { return OceanContent.kelp.canRenderInPass(pass); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) { return true; }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) { return null; }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta >= 8 && meta <= 11) {
            float min = meta == 8 ? 6F : meta == 9 ? 3F : 2F;
            float max = meta == 8 ? 10F : meta == 9 ? 13F : 14F;
            setBlockBounds(min / 16F, 0F, min / 16F, max / 16F,
                    (meta == 11 ? 7F : 6F) / 16F, max / 16F);
        } else if (meta == 0) {
            setBlockBounds(2F / 16F, 0F, 2F / 16F, 14F / 16F, 12F / 16F, 14F / 16F);
        } else if (meta == 1 || meta == 2) {
            setBlockBounds(2F / 16F, 0F, 2F / 16F, 14F / 16F, 1F, 14F / 16F);
        } else {
            setBlockBounds(0.1F, 0F, 0.1F, 0.9F, 0.85F, 0.9F);
        }
    }

    @Override public void setBlockBoundsForItemRender() {
        setBlockBounds(0.1F, 0F, 0.1F, 0.9F, 0.85F, 0.9F);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return BlockKelp.isFullWater(world, x, y, z)
                && BlockKelp.canRootOn(world.getBlock(x, y - 1, z));
    }

    public static boolean supportsSeagrass(World world, int x, int y, int z) {
        Block below = world.getBlock(x, y - 1, z);
        return below != com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.magmaBlock
                && below.isSideSolid(world, x, y - 1, z, net.minecraftforge.common.util.ForgeDirection.UP);
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta >= 8 ? 6 + (meta - 8) * 3 : 0;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote || world.getBlock(x, y, z) != this) return;
        int meta = world.getBlockMetadata(x, y, z);
        Block below = world.getBlock(x, y - 1, z);
        boolean supported = meta == 2
                ? below == this && world.getBlockMetadata(x, y - 1, z) == 1
                : BlockKelp.canRootOn(below);
        if (meta == 0 || meta == 1) supported = supportsSeagrass(world, x, y, z);
        if (meta == 1) supported &= world.getBlock(x, y + 1, z) == this
                && world.getBlockMetadata(x, y + 1, z) == 2;
        if (!supported) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlock(x, y, z, BlockKelp.waterFor(world, x, y, z), 0, 3);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMetadata) {
        super.breakBlock(world, x, y, z, oldBlock, oldMetadata);
        BlockKelp.restoreWaterAfterRemoval(world, x, y, z);
        // Remove only the matching half, never a replacement block placed there.
        if (!world.isRemote && world.getBlock(x, y, z) != this
                && (oldMetadata == 1 || oldMetadata == 2)) {
            int partnerY = y + (oldMetadata == 1 ? 1 : -1);
            int partnerMeta = oldMetadata == 1 ? 2 : 1;
            if (world.getBlock(x, partnerY, z) == this
                    && world.getBlockMetadata(x, partnerY, z) == partnerMeta) {
                world.setBlock(x, partnerY, z, BlockKelp.waterFor(world, x, partnerY, z), 0, 3);
            }
        }
    }

    @Override public Item getItemDropped(int meta, Random random, int fortune) {
        return meta >= 8 ? Item.getItemFromBlock(this) : null;
    }
    @Override public int quantityDropped(Random random) { return 0; }
    @Override public int quantityDropped(int meta, int fortune, Random random) {
        return meta >= 8 ? meta - 7 : 0;
    }
    @Override public int damageDropped(int meta) { return meta >= 8 ? 8 : 0; }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z) <= 2;
    }

    @Override
    public ArrayList<ItemStack> onSheared(ItemStack item, IBlockAccess world, int x, int y, int z,
                                         int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        int meta = world.getBlockMetadata(x, y, z);
        if (meta >= 0 && meta <= 2) {
            drops.add(new ItemStack(this, meta == 1 || meta == 2 ? 2 : 1, 0));
        }
        return drops;
    }

    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean client) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta == 0 && BlockKelp.isOpenWater(world.getBlock(x, y + 1, z))
                || meta >= 8;
    }

    @Override public boolean func_149852_a(World world, Random random, int x, int y, int z) {
        return true;
    }

    @Override
    public void func_149853_b(World world, Random random, int x, int y, int z) {
        if (world.isRemote || world.getBlock(x, y, z) != this) return;
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 0 && BlockKelp.isOpenWater(world.getBlock(x, y + 1, z))) {
            Block oldWater = world.getBlock(x, y + 1, z);
            int oldWaterMeta = world.getBlockMetadata(x, y + 1, z);
            if (world.setBlock(x, y + 1, z, this, 2, 2)
                    && !world.setBlock(x, y, z, this, 1, 2)
                    && world.getBlock(x, y + 1, z) == this
                    && world.getBlockMetadata(x, y + 1, z) == 2) {
                world.setBlock(x, y + 1, z, oldWater, oldWaterMeta, 2);
            }
            return;
        }
        if (meta < 8 || meta > 11 || !BlockCoralBlock120.isLive(world.getBlock(x, y - 1, z),
                world.getBlockMetadata(x, y - 1, z))) return;
        // 1.20 SeaPickleBlock: diamond of radius two, two vertical layers,
        // one-in-six attempt per cell, then fill the original cluster.
        for (int dx = -2; dx <= 2; dx++) {
            int radius = 2 - Math.abs(dx);
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -1; dy <= 0; dy++) {
                    int nx = x + dx, ny = y + dy, nz = z + dz;
                    if (random.nextInt(6) == 0 && BlockKelp.isOpenWater(world.getBlock(nx, ny, nz))
                            && BlockCoralBlock120.isLive(world.getBlock(nx, ny - 1, nz),
                            world.getBlockMetadata(nx, ny - 1, nz))) {
                        world.setBlock(nx, ny, nz, this, 8 + random.nextInt(4), 3);
                    }
                }
            }
        }
        world.setBlockMetadataWithNotify(x, y, z, 11, 2);
    }

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        for (int meta = 3; meta <= 8; meta++) list.add(new ItemStack(item, 1, meta));
    }
}
