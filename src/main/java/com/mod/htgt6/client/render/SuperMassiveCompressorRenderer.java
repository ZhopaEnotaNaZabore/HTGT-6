package com.mod.htgt6.client.render;

import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/** Hydraulic compression module visible through the front 3x3 observation window. */
public final class SuperMassiveCompressorRenderer extends TileEntitySpecialRenderer {
    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof TESuperMasicCompressor)) return;
        TESuperMasicCompressor compressor = (TESuperMasicCompressor) tile;
        if (!compressor.isStructureFormed() || compressor.getWorldObj() == null) return;

        int[] center = compressor.localToWorld(0, 0, 2);
        double centerX = x + center[0] - compressor.xCoord + 0.5D;
        double centerZ = z + center[2] - compressor.zCoord + 0.5D;
        boolean active = compressor.isMachineActive() && compressor.getStructureDiagnostic().startsWith("PROCESSING:");
        float progress = compressor.getProcessProgressFraction(partialTicks);
        double cycle = progress * 2.0D;
        double stroke = cycle <= 1.0D ? cycle : 2.0D - cycle;
        stroke = Math.max(0.0D, Math.min(1.0D, stroke));

        MultiblockRenderState state = new MultiblockRenderState("compressor");
        try {
            GL11.glTranslated(centerX, y + 0.5D, centerZ);


            // Rigid press bed and four columns fill the internal 3x3 chamber.
            drawBox(-1.34D, 0.50D, -1.34D, 1.34D, 0.72D, 1.34D,
                    0.14F, 0.18F, 0.21F, 1F);
            drawBox(-1.18D, 0.72D, -1.18D, 1.18D, 0.88D, 1.18D,
                    0.30F, 0.36F, 0.39F, 1F);
            drawColumn(-1.20D, -1.20D); drawColumn(1.00D, -1.20D);
            drawColumn(-1.20D, 1.00D); drawColumn(1.00D, 1.00D);
            drawBox(-1.34D, 3.18D, -1.34D, 1.34D, 3.48D, 1.34D,
                    0.17F, 0.22F, 0.25F, 1F);

            // Hydraulic reservoir, cylinder, moving ram and compression platen.
            drawBox(-0.92D, 2.82D, -0.82D, 0.92D, 3.28D, 0.82D,
                    0.10F, 0.25F, 0.31F, 1F);
            drawBox(-0.64D, 2.52D, -0.64D, 0.64D, 2.91D, 0.64D,
                    0.24F, 0.34F, 0.38F, 1F);
            double ramBottom = 2.46D - stroke * 1.22D;
            drawBox(-0.23D, ramBottom, -0.23D, 0.23D, 2.60D, 0.23D,
                    0.66F, 0.69F, 0.70F, 1F);
            drawBox(-1.02D, ramBottom - 0.20D, -1.02D, 1.02D, ramBottom, 1.02D,
                    0.28F, 0.33F, 0.35F, 1F);
            drawBox(-0.82D, 0.89D, -0.82D, 0.82D, 1.02D, 0.82D,
                    0.42F, 0.45F, 0.46F, 1F);

            // Sliding guides and hydraulic piston collars.
            for(int corner=0;corner<4;corner++) {
                GL11.glPushMatrix(); GL11.glRotatef(corner*90,0,1,0);
                drawBox(1.01,ramBottom-.16,1.01,1.25,ramBottom+.16,1.25,.76F,.67F,.38F,1);
                GL11.glPopMatrix();
            }
            drawBox(-.35,2.42,-.35,.35,2.55,.35,.64F,.67F,.69F,1);
            drawBox(-.38,2.80,-.38,.38,2.91,.38,.64F,.67F,.69F,1);

            if (compressor.getCurrentRecipePrimaryOutputForRender()!=null) {
                double chargeHeight=.22-.17*stroke;
                drawBox(-.58,1.03,-.58,.58,1.03+chargeHeight,.58,.65F,.59F,.46F,1);
            }
            // Pressure lines make the module recognizable as hydraulic rather than chemical.
            drawBox(-1.13D, 2.72D, -0.07D, -0.62D, 2.86D, 0.07D,
                    0.82F, 0.48F, 0.08F, 1F);
            drawBox(0.62D, 2.72D, -0.07D, 1.13D, 2.86D, 0.07D,
                    0.82F, 0.48F, 0.08F, 1F);

            if (active) {
                double time = progress * 160D;
                float pulse = 0.28F + 0.12F * (float) Math.sin(time * 0.55D);
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glDepthMask(false);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                drawBox(-0.88D, 1.03D, -0.88D, 0.88D, 1.09D, 0.88D,
                        0.10F, 0.72F, 1.00F, pulse);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDepthMask(true);
            }
        } finally {
            state.close();
        }
    }

    private static void drawColumn(double x, double z) {
        drawBox(x, 0.66D, z, x + 0.20D, 3.26D, z + 0.20D,
                0.25F, 0.31F, 0.34F, 1F);
    }

    private static void drawBox(double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ,
                                float red, float green, float blue, float alpha) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads(); t.setBrightness(0x00F000F0);
        t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
                alpha >= .99F ? .30F + green * .70F : green,
                alpha >= .99F ? .30F + blue * .70F : blue, alpha);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(maxX,minY,minZ,1,0);t.addVertexWithUV(maxX,minY,maxZ,1,1);t.addVertexWithUV(minX,minY,maxZ,0,1);
        t.addVertexWithUV(minX,maxY,minZ,0,0);t.addVertexWithUV(minX,maxY,maxZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(maxX,maxY,minZ,0,1);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(minX,maxY,minZ,1,0);t.addVertexWithUV(maxX,maxY,minZ,1,1);t.addVertexWithUV(maxX,minY,minZ,0,1);
        t.addVertexWithUV(minX,minY,maxZ,0,0);t.addVertexWithUV(maxX,minY,maxZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(minX,maxY,maxZ,0,1);
        t.addVertexWithUV(minX,minY,minZ,0,0);t.addVertexWithUV(minX,minY,maxZ,1,0);t.addVertexWithUV(minX,maxY,maxZ,1,1);t.addVertexWithUV(minX,maxY,minZ,0,1);
        t.addVertexWithUV(maxX,minY,minZ,0,0);t.addVertexWithUV(maxX,maxY,minZ,1,0);t.addVertexWithUV(maxX,maxY,maxZ,1,1);t.addVertexWithUV(maxX,minY,maxZ,0,1);
        t.draw();
    }
    private static void vertex(Tessellator t, double x, double y, double z) {
        t.addVertexWithUV(x,y,z,x+z,y);
    }
}
