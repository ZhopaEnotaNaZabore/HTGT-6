package com.mod.htgt6.client.nei.structure;

import com.mod.htgt6.common.multiblock.glass.BlockObservationGlass;
import com.mod.htgt6.common.multiblock.glass.ObservationGlassPattern;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

/** Uses preview neighbors rather than inventory textures, including the selected layer cut. */
public final class ObservationGlassPreview {
    private static final int[][] UV = {{-1,0},{1,0},{0,-1},{0,1},{-1,-1},{1,-1},{-1,1},{1,1}};
    private static final int[][] NORMAL = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    private ObservationGlassPreview() {}

    public static void render(RenderBlocks renderer, HTGT6MultiblockStructure structure,
                              int layer, int x, int y, int z, BlockObservationGlass block, int metadata) {
        renderer.setRenderBounds(0,0,0,1,1,1);
        int tint = block.getRenderColor(metadata);
        GL11.glTranslatef(-.5F,-.5F,-.5F);
        Tessellator tessellator = Tessellator.instance;
        for (int side = 0; side < 6; side++) {
            int[] normal = NORMAL[side];
            if (same(structure, layer, x + normal[0], y + normal[1], z + normal[2], block)) continue;
            int mask = 0;
            for (int i = 0; i < UV.length; i++) {
                int u = UV[i][0], v = UV[i][1];
                if (!same(structure, layer, x + ObservationGlassPattern.offset(side,u,v,0),
                        y + ObservationGlassPattern.offset(side,u,v,1),
                        z + ObservationGlassPattern.offset(side,u,v,2), block)) mask |= 1 << i;
            }
            IIcon icon = block.getConnectedIcon(metadata, mask);
            tessellator.startDrawingQuads();
            tessellator.setNormal(normal[0], normal[1], normal[2]);
            tessellator.setColorOpaque_I(tint);
            switch (side) {
                case 0: renderer.renderFaceYNeg(block,0,0,0,icon); break;
                case 1: renderer.renderFaceYPos(block,0,0,0,icon); break;
                case 2: renderer.renderFaceZNeg(block,0,0,0,icon); break;
                case 3: renderer.renderFaceZPos(block,0,0,0,icon); break;
                case 4: renderer.renderFaceXNeg(block,0,0,0,icon); break;
                case 5: renderer.renderFaceXPos(block,0,0,0,icon); break;
            }
            tessellator.draw();
        }
    }

    private static boolean same(HTGT6MultiblockStructure structure, int layer,
                                int x, int y, int z, BlockObservationGlass block) {
        if (x < 0 || y < 0 || z < 0 || x >= structure.getWidth()
                || y >= structure.getHeight() || z >= structure.getDepth() || layer >= 0 && y != layer) return false;
        HTGT6MultiblockStructure.StructureBlock cell = structure.getBlock(x,y,z);
        return cell != null && cell.block == block;
    }
}
