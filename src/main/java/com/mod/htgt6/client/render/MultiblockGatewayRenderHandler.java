package com.mod.htgt6.client.render;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.api.hitech6.multiblock.FluidGatewayVariant;
import com.mod.htgt6.common.multiblock.gateway.BlockMultiblockGateway;
import com.mod.htgt6.common.multiblock.gateway.TileEntityMultiblockGateway;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Renders inherited casing first, then a visible functional marker on every face. */
public final class MultiblockGatewayRenderHandler implements ISimpleBlockRenderingHandler {
    private static final double OFFSET = 0.002D;
    private final int renderId;

    public MultiblockGatewayRenderHandler(int renderId) { this.renderId = renderId; }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        renderer.setRenderBounds(0D, 0D, 0D, 1D, 1D, 1D);
        renderInventoryFaces(renderer, block, block.getIcon(0, metadata));
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        BlockMultiblockGateway gatewayBlock = (BlockMultiblockGateway) block;
        TileEntity tile = world.getTileEntity(x, y, z);
        Block casing = null;
        int casingMeta = 0;
        if (tile instanceof TileEntityMultiblockGateway) {
            TileEntityMultiblockGateway gateway = (TileEntityMultiblockGateway) tile;
            casing = gateway.getInheritedCasingBlock();
            casingMeta = gateway.getInheritedCasingMetadata();
        }

        renderer.setRenderBoundsFromBlock(block);
        if (casing != null && casing != block) {
            renderer.setOverrideBlockTexture(casing.getIcon(1, casingMeta));
            renderer.renderStandardBlock(block, x, y, z);
            renderer.clearOverrideBlockTexture();
        } else {
            renderer.renderStandardBlock(block, x, y, z);
        }

        IIcon overlay = gatewayBlock.getOverlayIcon();
        if (overlay == null) return true;
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        float[] color = colorFor(FluidGatewayVariant.type(world.getBlockMetadata(x, y, z)));
        tessellator.setColorOpaque_F(color[0], color[1], color[2]);
        renderer.renderFaceYNeg(block, x, y - OFFSET, z, overlay);
        renderer.renderFaceYPos(block, x, y + OFFSET, z, overlay);
        renderer.renderFaceZNeg(block, x, y, z - OFFSET, overlay);
        renderer.renderFaceZPos(block, x, y, z + OFFSET, overlay);
        renderer.renderFaceXNeg(block, x - OFFSET, y, z, overlay);
        renderer.renderFaceXPos(block, x + OFFSET, y, z, overlay);
        return true;
    }

    private static float[] colorFor(GatewayType type) {
        switch (type) {
            case FLUID_OUTPUT: return new float[] { 1.0F, 0.55F, 0.15F };
            case ITEM_INPUT: return new float[] { 0.30F, 1.0F, 0.45F };
            case ITEM_OUTPUT: return new float[] { 0.85F, 0.35F, 1.0F };
            case ENERGY_INPUT: return new float[] { 1.0F, 0.90F, 0.20F };
            case ENERGY_OUTPUT: return new float[] { 0.25F, 0.70F, 1.0F };
            default: return new float[] { 0.25F, 0.90F, 1.0F };
        }
    }

    private static void renderInventoryFaces(RenderBlocks renderer, Block block, IIcon icon) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads(); t.setNormal(0, -1, 0); renderer.renderFaceYNeg(block, 0, 0, 0, icon); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 1, 0); renderer.renderFaceYPos(block, 0, 0, 0, icon); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 0, -1); renderer.renderFaceZNeg(block, 0, 0, 0, icon); t.draw();
        t.startDrawingQuads(); t.setNormal(0, 0, 1); renderer.renderFaceZPos(block, 0, 0, 0, icon); t.draw();
        t.startDrawingQuads(); t.setNormal(-1, 0, 0); renderer.renderFaceXNeg(block, 0, 0, 0, icon); t.draw();
        t.startDrawingQuads(); t.setNormal(1, 0, 0); renderer.renderFaceXPos(block, 0, 0, 0, icon); t.draw();
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
