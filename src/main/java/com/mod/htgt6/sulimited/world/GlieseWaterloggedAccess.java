package com.mod.htgt6.sulimited.world;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.BlockGlieseCPlant;
/** Shared fluid view: plant metadata is a colour, never a water level. */
    public final class GlieseWaterloggedAccess implements IBlockAccess {
        private final IBlockAccess delegate;
        private final Block ocean;
        public GlieseWaterloggedAccess(IBlockAccess delegate, Block ocean) { this.delegate = delegate; this.ocean = ocean; }
        private boolean aquatic(Block block) {
            return block instanceof BlockGlieseCPlant;
        }
        @Override public Block getBlock(int x, int y, int z) {
            Block block = delegate.getBlock(x, y, z); return aquatic(block) ? ocean : block;
        }
        @Override public int getBlockMetadata(int x, int y, int z) {
            return aquatic(delegate.getBlock(x, y, z)) ? 0 : delegate.getBlockMetadata(x, y, z);
        }
        @Override public TileEntity getTileEntity(int x, int y, int z) { return delegate.getTileEntity(x, y, z); }
        @Override public int getLightBrightnessForSkyBlocks(int x, int y, int z, int min) {
            return delegate.getLightBrightnessForSkyBlocks(x, y, z, min);
        }
        @Override public int isBlockProvidingPowerTo(int x, int y, int z, int side) {
            return delegate.isBlockProvidingPowerTo(x, y, z, side);
        }
        @Override public boolean isAirBlock(int x, int y, int z) { return delegate.isAirBlock(x, y, z); }
        @Override public BiomeGenBase getBiomeGenForCoords(int x, int z) { return delegate.getBiomeGenForCoords(x, z); }
        @Override public int getHeight() { return delegate.getHeight(); }
        @Override public boolean extendedLevelsInChunkCache() { return delegate.extendedLevelsInChunkCache(); }
        @Override public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean fallback) {
            return delegate.isSideSolid(x, y, z, side, fallback);
        }
    }
