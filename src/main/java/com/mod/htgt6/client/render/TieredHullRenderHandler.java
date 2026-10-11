package com.mod.htgt6.client.render;

import com.mod.htgt6.common.block.greg.technological.hulls.BlockTieredHull;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.opengl.GL11;

/** Subtle hull RGB and an independent bright circular output marker. */
public final class TieredHullRenderHandler implements ISimpleBlockRenderingHandler {
    private final int id;
    public TieredHullRenderHandler(int id) { this.id = id; }
    @Override public int getRenderId() { return id; }
    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }

    @Override public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
            Block block, int modelId, RenderBlocks renderer) {
        renderer.setRenderBoundsFromBlock(block);
        boolean rendered = renderer.renderStandardBlock(block, x, y, z);
        if (renderer.hasOverrideBlockTexture()) return rendered;
        BlockTieredHull hull = (BlockTieredHull) block;
        int side = BlockTieredHull.front(world.getBlockMetadata(x, y, z));
        ForgeDirection direction = ForgeDirection.getOrientation(side);
        if (!block.shouldSideBeRendered(world, x + direction.offsetX, y, z + direction.offsetZ, side)) return rendered;
        boolean ao = renderer.enableAO;
        renderer.enableAO = false;
        try {
            Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(world,
                    x + direction.offsetX, y, z + direction.offsetZ));
            tint(hull.getInputColor(), side >= 4 ? .6F : .8F);
            face(renderer, block, side, x + direction.offsetX * .001,
                    y, z + direction.offsetZ * .001, hull.getInputOverlay());
        } finally { renderer.enableAO = ao; }
        return true;
    }

    @Override public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        BlockTieredHull hull = (BlockTieredHull) block;
        boolean ao = renderer.enableAO;
        GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glPushMatrix();
        try {
            renderer.enableAO = false;
            renderer.setRenderBounds(0, 0, 0, 1, 1, 1);
            GL11.glTranslatef(-.5F, -.5F, -.5F);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, .1F);
            for (int side = 0; side < 6; side++) {
                ForgeDirection d = ForgeDirection.getOrientation(side);
                Tessellator.instance.startDrawingQuads();
                Tessellator.instance.setNormal(d.offsetX, d.offsetY, d.offsetZ);
                tint(hull.getBodyColor(), 1);
                face(renderer, block, side, 0, 0, 0, block.getIcon(side, 3));
                Tessellator.instance.draw();
            }
            Tessellator.instance.startDrawingQuads();
            Tessellator.instance.setNormal(0, 0, 1);
            tint(hull.getInputColor(), 1);
            face(renderer, block, 3, 0, 0, .001, hull.getInputOverlay());
            Tessellator.instance.draw();
        } finally {
            renderer.enableAO = ao;
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
    private static void tint(int rgb, float shade) {
        Tessellator.instance.setColorOpaque_F(((rgb >> 16) & 255) / 255F * shade,
                ((rgb >> 8) & 255) / 255F * shade, (rgb & 255) / 255F * shade);
    }
    private static void face(RenderBlocks r, Block block, int side, double x, double y, double z, IIcon icon) {
        switch (side) {
            case 0: r.renderFaceYNeg(block,x,y,z,icon); break;
            case 1: r.renderFaceYPos(block,x,y,z,icon); break;
            case 2: r.renderFaceZNeg(block,x,y,z,icon); break;
            case 3: r.renderFaceZPos(block,x,y,z,icon); break;
            case 4: r.renderFaceXNeg(block,x,y,z,icon); break;
            case 5: r.renderFaceXPos(block,x,y,z,icon); break;
        }
    }
}
