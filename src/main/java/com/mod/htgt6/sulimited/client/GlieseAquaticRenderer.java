package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.BlockGlieseCPlant;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.blocksGliese792C;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology;
import com.mod.htgt6.sulimited.world.GlieseWaterloggedAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.RenderBlockFluid;
import org.lwjgl.opengl.GL11;

/** Cutout connected plants in pass zero; one continuous ocean in pass one. */
public final class GlieseAquaticRenderer implements ISimpleBlockRenderingHandler {
    public static void register() {
        SolSysGeology.aquaticRenderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new GlieseAquaticRenderer());
    }

    @Override public int getRenderId() { return SolSysGeology.aquaticRenderId; }
    @Override public boolean shouldRender3DInInventory(int id) { return true; }
    @Override public void renderInventoryBlock(Block block, int metadata, int id, RenderBlocks renderer) {
        if (!(block instanceof BlockGlieseCPlant)) return;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        Tessellator.instance.startDrawingQuads();
        Tessellator.instance.setNormal(0, 1, 0);
        renderer.drawCrossedSquares(block.getIcon(0, metadata), 0, 0, 0, 1);
        Tessellator.instance.draw();
        GL11.glPopMatrix();
    }

    @Override public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                              Block block, int id, RenderBlocks renderer) {
        if (block instanceof BlockGlieseCPlant
                && ((BlockGlieseCPlant) block).getActiveRenderPass() == 0) {
            return renderPlant(world, x, y, z, (BlockGlieseCPlant) block, renderer);
        }
        // Apply the same neighbour view to water AND plants, otherwise the ordinary
        // fluid below a plant still draws its top face inside the water column.
        IBlockAccess previous = renderer.blockAccess;
        IBlockAccess water = new GlieseWaterloggedAccess(world, SolSysGeology.saltWater);
        renderer.blockAccess = water;
        try {
            return RenderBlockFluid.instance.renderWorldBlock(water, x, y, z,
                    SolSysGeology.saltWater, id, renderer);
        } finally {
            renderer.blockAccess = previous;
        }
    }

    private boolean renderPlant(IBlockAccess world, int x, int y, int z, BlockGlieseCPlant block, RenderBlocks r) {
        boolean above = connects(world, x, y + 1, z, block, world.getBlockMetadata(x, y, z));
        int metadata = world.getBlockMetadata(x, y, z);
        Block stem = block.isCoral() ? blocksGliese792C.reefRock : blocksGliese792C.GlieseCMossyPetrifiedEarth;
        double minX = r.renderMinX, minY = r.renderMinY, minZ = r.renderMinZ;
        double maxX = r.renderMaxX, maxY = r.renderMaxY, maxZ = r.renderMaxZ;
        net.minecraft.util.IIcon previous = r.overrideBlockTexture;
        try {
            r.setOverrideBlockTexture(stem.getIcon(2, 0));
            box(r, block, x, y, z, .44, 0, .44, .56, above ? 1 : .65, .56);
            if (block.isCoral()) {
                if (connects(world, x - 1, y, z, block, metadata)) box(r, block, x, y, z, 0, .44, .44, .5, .56, .56);
                if (connects(world, x + 1, y, z, block, metadata)) box(r, block, x, y, z, .5, .44, .44, 1, .56, .56);
                if (connects(world, x, y, z - 1, block, metadata)) box(r, block, x, y, z, .44, .44, 0, .56, .56, .5);
                if (connects(world, x, y, z + 1, block, metadata)) box(r, block, x, y, z, .44, .44, .5, .56, .56, 1);
            }
        } finally {
            r.overrideBlockTexture = previous;
            r.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
        }
        Tessellator t = Tessellator.instance;
        int colour = block.getRenderColor(metadata);
        t.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        t.setColorOpaque_I(colour);
        // Fixed centres line up across blocks; lower segments have smaller branches.
        float size = above ? .65F : 1F;
        double inset = (1 - size) * .5;
        r.drawCrossedSquares(block.getIcon(0, metadata), x + inset, y + (above ? .15 : 0), z + inset, size);
        return true;
    }

    private static boolean connects(IBlockAccess world, int x, int y, int z, Block block, int metadata) {
        return world.getBlock(x, y, z) == block && world.getBlockMetadata(x, y, z) == metadata;
    }

    private static void box(RenderBlocks r, Block b, int x, int y, int z,
                            double x0, double y0, double z0, double x1, double y1, double z1) {
        r.setRenderBounds(x0, y0, z0, x1, y1, z1);
        r.renderStandardBlock(b, x, y, z);
    }
}
