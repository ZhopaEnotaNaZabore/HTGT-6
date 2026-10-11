package com.mod.htgt6.minecraft1202.Module.Client.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockTurtleEgg120;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Four official turtle-egg cuboids, including deterministic 0/90/180/270 rotation. */
public final class TurtleEggRenderHandler120 implements ISimpleBlockRenderingHandler {
    public static final int[][] REFERENCE_CUBES = {
            {5, 0, 4, 9, 7, 8}, {1, 0, 7, 5, 5, 11},
            {11, 0, 7, 14, 4, 10}, {6, 0, 9, 10, 4, 13}
    };
    private final int renderId;

    public TurtleEggRenderHandler120(int renderId) { this.renderId = renderId; }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        renderer.setRenderBounds(5 / 16.0D, 0, 4 / 16.0D, 9 / 16.0D, 7 / 16.0D, 8 / 16.0D);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads(); renderer.renderFaceYNeg(block, 0, 0, 0, block.getIcon(0, metadata)); tessellator.draw();
        tessellator.startDrawingQuads(); renderer.renderFaceYPos(block, 0, 0, 0, block.getIcon(1, metadata)); tessellator.draw();
        tessellator.startDrawingQuads(); renderer.renderFaceZNeg(block, 0, 0, 0, block.getIcon(2, metadata)); tessellator.draw();
        tessellator.startDrawingQuads(); renderer.renderFaceZPos(block, 0, 0, 0, block.getIcon(3, metadata)); tessellator.draw();
        tessellator.startDrawingQuads(); renderer.renderFaceXNeg(block, 0, 0, 0, block.getIcon(4, metadata)); tessellator.draw();
        tessellator.startDrawingQuads(); renderer.renderFaceXPos(block, 0, 0, 0, block.getIcon(5, metadata)); tessellator.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        int count = BlockTurtleEgg120.eggCount(world.getBlockMetadata(x, y, z));
        int rotation = (int)((x * 3129871L ^ z * 116129781L ^ y) & 3L);
        boolean rendered = false;
        for (int index = 0; index < count; index++) {
            int[] cube = rotate(REFERENCE_CUBES[index], rotation);
            renderer.setRenderBounds(cube[0] / 16.0D, cube[1] / 16.0D, cube[2] / 16.0D,
                    cube[3] / 16.0D, cube[4] / 16.0D, cube[5] / 16.0D);
            rendered |= renderer.renderStandardBlock(block, x, y, z);
        }
        renderer.setRenderBoundsFromBlock(block);
        return rendered;
    }

    private static int[] rotate(int[] box, int turns) {
        int x0 = box[0], z0 = box[2], x1 = box[3], z1 = box[5];
        for (int turn = 0; turn < turns; turn++) {
            int nextX0 = 16 - z1, nextX1 = 16 - z0;
            z0 = x0; z1 = x1; x0 = nextX0; x1 = nextX1;
        }
        return new int[]{x0, box[1], z0, x1, box[4], z1};
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
