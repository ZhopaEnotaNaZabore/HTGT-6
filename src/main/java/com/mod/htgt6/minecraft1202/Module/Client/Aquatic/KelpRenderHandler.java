package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockWallCoralFan120;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import gregapi.data.CS;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.ResourceLocation;
import com.mod.htgt6.client.model.json.JsonBlockModelLoader;

/** Renders the plant and its displaced GT6 ocean-water cell in separate passes. */
public final class KelpRenderHandler implements ISimpleBlockRenderingHandler {
    private final int renderId;

    private final JsonBlockModelLoader pickleModels;

    public KelpRenderHandler(int renderId) {
        this.renderId = renderId;
        pickleModels = JsonBlockModelLoader.install();
        pickleModels.register(OceanContent.marinePlant,
                new ResourceLocation("htgt6", "aquatic/sea_pickle"),
                new JsonBlockModelLoader.StateMapper() {
                    @Override public String variant(IBlockAccess world, int x, int y, int z, int metadata) {
                        return "pickles=" + Math.max(1, Math.min(4, metadata - 7));
                    }
                }, new JsonBlockModelLoader.TextureResolver() {
                    @Override public ResourceLocation resolve(String texture) {
                        return new ResourceLocation("htgt6", "sea_pickle");
                    }
                });
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (block == OceanContent.marinePlant && metadata >= 8 && metadata <= 11) {
            pickleModels.renderInventoryBlock(block, metadata, pickleModels.getRenderId(), renderer);
            return;
        }
        Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.drawCrossedSquares(block.getIcon(0, metadata), 0.0D, 0.0D, 0.0D, 1.0F);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        if (BlockKelp.getActiveRenderPass() == 0) {
            int metadata = world.getBlockMetadata(x, y, z);
            if (block == OceanContent.marinePlant && metadata >= 8 && metadata <= 11) {
                return pickleModels.renderWorldBlock(world, x, y, z, block,
                        pickleModels.getRenderId(), renderer);
            }
            if (block instanceof BlockWallCoralFan120) {
                return renderWallFan(world, x, y, z, block);
            }
            return renderer.renderCrossedSquares(block, x, y, z);
        }
        Block ocean = CS.BlocksGT.Ocean != null ? CS.BlocksGT.Ocean : Blocks.water;
        IBlockAccess original = renderer.blockAccess;
        renderer.blockAccess = new WaterloggedAccess(world, ocean);
        try {
            return renderer.renderBlockLiquid(ocean, x, y, z);
        } finally {
            renderer.blockAccess = original;
        }
    }

    private static boolean renderWallFan(IBlockAccess world, int x, int y, int z, Block block) {
        Tessellator tessellator = Tessellator.instance;
        int direction = BlockWallCoralFan120.direction(world.getBlockMetadata(x, y, z));
        IIcon icon = block.getIcon(0, world.getBlockMetadata(x, y, z));
        double u0 = icon.getMinU(), u1 = icon.getMaxU(), v0 = icon.getMinV(), v1 = icon.getMaxV();
        double y0 = y + 0.08D, y1 = y + 0.92D;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);
        if (BlockWallCoralFan120.dx(direction) != 0) {
            double px = x + (BlockWallCoralFan120.dx(direction) > 0 ? 0.06D : 0.94D);
            double z0 = z + 0.08D, z1 = z + 0.92D;
            tessellator.addVertexWithUV(px, y0, z0, u0, v1);
            tessellator.addVertexWithUV(px, y0, z1, u1, v1);
            tessellator.addVertexWithUV(px, y1, z1, u1, v0);
            tessellator.addVertexWithUV(px, y1, z0, u0, v0);
            tessellator.addVertexWithUV(px, y0, z1, u0, v1);
            tessellator.addVertexWithUV(px, y0, z0, u1, v1);
            tessellator.addVertexWithUV(px, y1, z0, u1, v0);
            tessellator.addVertexWithUV(px, y1, z1, u0, v0);
        } else {
            double pz = z + (BlockWallCoralFan120.dz(direction) > 0 ? 0.06D : 0.94D);
            double x0 = x + 0.08D, x1 = x + 0.92D;
            tessellator.addVertexWithUV(x0, y0, pz, u0, v1);
            tessellator.addVertexWithUV(x1, y0, pz, u1, v1);
            tessellator.addVertexWithUV(x1, y1, pz, u1, v0);
            tessellator.addVertexWithUV(x0, y1, pz, u0, v0);
            tessellator.addVertexWithUV(x1, y0, pz, u0, v1);
            tessellator.addVertexWithUV(x0, y0, pz, u1, v1);
            tessellator.addVertexWithUV(x0, y1, pz, u1, v0);
            tessellator.addVertexWithUV(x1, y1, pz, u0, v0);
        }
        return true;
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }

    /** Makes every aquatic plant cell appear as a full GT ocean source to the legacy liquid renderer. */
    private static final class WaterloggedAccess implements IBlockAccess {
        private final IBlockAccess delegate;
        private final Block ocean;
        WaterloggedAccess(IBlockAccess delegate, Block ocean) { this.delegate = delegate; this.ocean = ocean; }
        private boolean aquatic(Block block) {
            return block == OceanContent.kelp || block == OceanContent.kelpPlant
                    || block == OceanContent.marinePlant || block == OceanContent.deadCoral
                    || block == OceanContent.deadCoralFan || OceanContent.isWallCoralFan(block);
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
}
