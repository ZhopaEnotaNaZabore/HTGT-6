package com.mod.htgt6.client.render;

import com.mod.htgt6.common.chemical.MultiTileChemicalPlant;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/** Animated 3x3 reaction vessel occupying the Chemical Plant's hollow core. */
public final class ChemicalPlantCoreRenderer extends TileEntitySpecialRenderer {
    private final MultiblockAnimationClock clock = new MultiblockAnimationClock();
    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        // TESR lookup in 1.7.10 walks the TileEntity superclass chain.  The
        // compressor reuses Chemical Plant recipe/UI plumbing, but must never
        // inherit its reaction-vessel visual.
        if (tile == null || tile.getClass() != MultiTileChemicalPlant.class) return;
        MultiTileChemicalPlant plant = (MultiTileChemicalPlant) tile;
        if (!plant.isStructureFormed() || plant.getWorldObj() == null) return;

        int facing = plant.getWorldObj().getBlockMetadata(plant.xCoord, plant.yCoord, plant.zCoord);
        double dx = facing == 4 ? 4D : facing == 5 ? -4D : 0D;
        double dz = facing == 2 ? 4D : facing == 3 ? -4D : 0D;
        double time = clock.sample(tile, MultiblockAnimationClock.frameTime(plant.getWorldObj().getTotalWorldTime(), partialTicks), plant.isMachineActive() && plant.getStructureDiagnostic().startsWith("PROCESSING:"));
        boolean active = plant.isMachineActive() && plant.getStructureDiagnostic().startsWith("PROCESSING:");

        MultiblockRenderState state = new MultiblockRenderState("chemical");
        try {
        GL11.glTranslated(x + 0.5D + dx, y + 3.04D, z + 0.5D + dz);


        // Stationary vessel and four structural rails fill the full hollow 3x3 core.
        drawCappedCylinder(1.38D, 0D, 0.28D, 40, 0.13F, 0.19F, 0.22F, 1F);
        drawCappedCylinder(1.38D, 4.54D, 4.82D, 40, 0.13F, 0.19F, 0.22F, 1F);
        drawBox(-1.32D, 0.20D, -1.32D, -1.18D, 4.62D, -1.18D, 0.24F, 0.32F, 0.35F, 1F);
        drawBox(1.18D, 0.20D, -1.32D, 1.32D, 4.62D, -1.18D, 0.24F, 0.32F, 0.35F, 1F);
        drawBox(-1.32D, 0.20D, 1.18D, -1.18D, 4.62D, 1.32D, 0.24F, 0.32F, 0.35F, 1F);
        drawBox(1.18D, 0.20D, 1.18D, 1.32D, 4.62D, 1.32D, 0.24F, 0.32F, 0.35F, 1F);

        // Heat-exchanger collars and external circulation pipes, distinct from a mixer.
        for(int level=0;level<6;level++) {
            double cy=.45+level*.73;
            drawCylinder(1.27,cy,cy+.09,32,.60F,.65F,.62F,1);
        }
        drawBox(-1.42,.28,-.12,-1.29,4.54,.12,.48F,.61F,.47F,1);
        drawBox(1.29,.28,-.12,1.42,4.54,.12,.48F,.61F,.47F,1);
        drawBox(-1.40,3.72,-.22,-1.18,4.10,.22,.30F,.42F,.33F,1);

        GL11.glPushMatrix();
        GL11.glRotatef((float) (time * 16D % 360D), 0F, 1F, 0F);
        drawCylinder(0.13D, 0.42D, 4.40D, 20,
                active ? 0.30F : 0.40F, active ? 0.95F : 0.46F, active ? 0.90F : 0.48F, 1F);
        for (int level = 0; level < 4; level++) {
            GL11.glPushMatrix();
            GL11.glRotatef(level * 37F, 0F, 1F, 0F);
            double bladeY = 0.86D + level * 0.95D;
            float glow = active ? 0.82F + 0.18F * (float) Math.sin(time * 0.35D + level) : 0.46F;
            drawBox(-1.02D, bladeY, -0.10D, 1.02D, bladeY + 0.16D, 0.10D,
                    active ? 0.16F : 0.10F, 0.82F * glow, 0.94F * glow, 1F);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        // Visible reaction medium in idle; additive full-bright volume and rotating inner vanes while active.
        float pulse = active ? 0.48F + 0.12F * (float) Math.sin(time * 0.28D) : 0.28F;
        drawCappedCylinder(0.98D, 0.50D, active ? 3.94D : 2.10D, 40,
                active ? 0.08F : 0.16F, active ? 0.90F : 0.42F, active ? 0.76F : 0.48F, pulse);
        if (active) {
            GL11.glDepthMask(false);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            float innerPulse = 0.30F + 0.10F * (float) Math.sin(time * 0.45D);
            drawCappedCylinder(0.72D, 0.62D, 3.82D, 32, 0.10F, 0.95F, 0.82F, innerPulse);
            drawCappedCylinder(0.38D, 0.72D, 3.72D, 24, 0.55F, 1.00F, 0.92F, innerPulse + 0.12F);
            GL11.glPushMatrix();
            GL11.glRotatef((float) (-time * 24D % 360D), 0F, 1F, 0F);
            for (int vane = 0; vane < 3; vane++) {
                GL11.glPushMatrix();
                GL11.glRotatef(vane * 120F, 0F, 1F, 0F);
                drawBox(0.34D, 0.82D, -0.035D, 0.88D, 3.62D, 0.035D,
                        0.30F, 1.00F, 0.88F, 0.52F);
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDepthMask(true);
        }

        GL11.glDepthMask(false);
        drawCylinder(1.18,.28,4.54,40,.20F,.62F,.55F,.12F);
        drawCylinder(1.24,.38,4.44,40,.40F,.55F,.52F,.14F);
        GL11.glDepthMask(true);
        } finally { state.close(); }
    }

    private static void drawCappedCylinder(double radius, double minY, double maxY, int segments,
                                            float red, float green, float blue, float alpha) {
        drawCylinder(radius, minY, maxY, segments, red, green, blue, alpha);
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_TRIANGLES); t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
                alpha >= .99F ? .30F + green * .70F : green,
                alpha >= .99F ? .30F + blue * .70F : blue, alpha);
        for (int i = 0; i < segments; i++) {
            double a = Math.PI * 2D * i / segments, b = Math.PI * 2D * (i + 1) / segments;
            double ax = Math.cos(a) * radius, az = Math.sin(a) * radius;
            double bx = Math.cos(b) * radius, bz = Math.sin(b) * radius;
            vertex(t,0, minY, 0); vertex(t,bx, minY, bz); vertex(t,ax, minY, az);
            vertex(t,0, maxY, 0); vertex(t,ax, maxY, az); vertex(t,bx, maxY, bz);
        }
        t.draw();
    }

    private static void drawCylinder(double radius, double minY, double maxY, int segments,
                                     float red, float green, float blue, float alpha) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads(); t.setBrightness(0x00F000F0); t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
                alpha >= .99F ? .30F + green * .70F : green,
                alpha >= .99F ? .30F + blue * .70F : blue, alpha);
        for (int i = 0; i < segments; i++) {
            double a = Math.PI * 2D * i / segments, b = Math.PI * 2D * (i + 1) / segments;
            double ax = Math.cos(a) * radius, az = Math.sin(a) * radius;
            double bx = Math.cos(b) * radius, bz = Math.sin(b) * radius;
            vertex(t,ax, minY, az); vertex(t,bx, minY, bz);
            vertex(t,bx, maxY, bz); vertex(t,ax, maxY, az);
        }
        t.draw();
    }

    private static void drawBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
                                float red, float green, float blue, float alpha) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads(); t.setBrightness(0x00F000F0); t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
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
