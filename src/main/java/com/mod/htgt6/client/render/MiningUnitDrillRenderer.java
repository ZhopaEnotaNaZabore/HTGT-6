package com.mod.htgt6.client.render;

import com.mod.htgt6.common.mining.MultiTEMiningUnit;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

/** Three-block-wide animated stepped drill rendered inside a formed Mining Unit shaft. */
public final class MiningUnitDrillRenderer extends TileEntitySpecialRenderer {
    private final MultiblockAnimationClock clock = new MultiblockAnimationClock();
    private static final double PLATE_RADIUS = 1.47D;
    private static final double[] STEP_RADII = { 0.34D, 0.50D, 0.68D, 0.86D, 1.04D, 1.20D, 1.34D };

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof MultiTEMiningUnit)) return;
        MultiTEMiningUnit unit = (MultiTEMiningUnit) tile;
        if (!unit.isStructureFormed() || unit.getWorldObj() == null) return;

        int facing = unit.getWorldObj().getBlockMetadata(unit.xCoord, unit.yCoord, unit.zCoord);
        double dx = facing == 4 ? 2.0D : facing == 5 ? -2.0D : 0.0D;
        double dz = facing == 2 ? 2.0D : facing == 3 ? -2.0D : 0.0D;
        double time = clock.sample(tile, MultiblockAnimationClock.frameTime(unit.getWorldObj().getTotalWorldTime(), partialTicks), unit.isMiningActive());
        float drillAngle = (float) (time * 18.0D % 360.0D);
        int miningY = unit.getActiveMiningY();
        double descent = miningY >= 0 && miningY < unit.yCoord ? unit.yCoord - miningY : 0.0D;

        MultiblockRenderState state = new MultiblockRenderState("mining");
        try {
        GL11.glTranslated(x + 0.5D + dx, y + 0.05D, z + 0.5D + dz);


        if (unit.shouldRenderWorkArea()) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawWorkArea(unit.getWorkAreaSize());
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        // Stationary housing fills the complete 3x3 opening in the casing ring.
        drawMountingPlate();
        // Four guide rails and carriage shoes carry the descending spindle.
        for(int i=0;i<4;i++) {
            GL11.glPushMatrix(); GL11.glRotatef(i*90,0,1,0);
            drawBox(1.25,1.04,-.12,1.39,3.78,.12,.55F,.58F,.60F,1);
            drawBox(1.18,3.28,-.22,1.44,3.62,.22,.72F,.57F,.20F,1);
            drawBox(.74,3.47,-.08,1.26,3.57,.08,.45F,.49F,.52F,1);
            GL11.glPopMatrix();
        }
        if (descent > 0.0D) {
            drawCylinder(0.11D, 3.62D - descent, 3.68D, 16,
                    0.16F, 0.20F, 0.22F, 1.0F);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(0, -descent, 0);
        if (unit.isMiningActive()) GL11.glTranslated(0, Math.sin(time * 0.48D) * 0.025D, 0);
        GL11.glRotatef(drillAngle, 0, 1, 0);
        drawShaft();
        drawHub();
        drawSteppedDrill(unit.isMiningActive(), time);
        GL11.glPopMatrix();

        } finally { state.close(); }
    }

    /** Cyan surface outline of the selected square scan field, shown for ten seconds after adjustment. */
    private static void drawWorkArea(int side) {
        double half = side / 2.0D;
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(3.0F);
        GL11.glColor4f(0.10F, 0.90F, 1.0F, 0.88F);
        t.startDrawing(GL11.GL_LINE_STRIP);
        t.setColorRGBA_F(0.10F, 0.90F, 1.0F, 0.88F);
        vertex(t,-half, 0.08D, -half);
        vertex(t,half, 0.08D, -half);
        vertex(t,half, 0.08D, half);
        vertex(t,-half, 0.08D, half);
        vertex(t,-half, 0.08D, -half);
        t.draw();

        // Crosshair makes the controller-relative center unambiguous at large tiers.
        GL11.glLineWidth(1.5F);
        t.startDrawing(GL11.GL_LINES);
        t.setColorRGBA_F(0.10F, 0.90F, 1.0F, 0.48F);
        vertex(t,-half, 0.075D, 0); vertex(t,half, 0.075D, 0);
        vertex(t,0, 0.075D, -half); vertex(t,0, 0.075D, half);
        t.draw();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private static void drawMountingPlate() {
        drawBox(-PLATE_RADIUS, 3.82D, -PLATE_RADIUS, PLATE_RADIUS, 4.08D, PLATE_RADIUS,
                0.16F, 0.22F, 0.25F, 1.0F);
        drawBox(-0.62D, 3.55D, -0.62D, 0.62D, 3.84D, 0.62D,
                0.10F, 0.48F, 0.54F, 1.0F);
        drawBox(-1.34D, 3.78D, -0.08D, 1.34D, 3.86D, 0.08D,
                0.28F, 0.36F, 0.39F, 1.0F);
        drawBox(-0.08D, 3.78D, -1.34D, 0.08D, 3.86D, 1.34D,
                0.28F, 0.36F, 0.39F, 1.0F);
    }

    private static void drawShaft() {
        drawBox(-0.14D, 0.48D, -0.14D, 0.14D, 3.68D, 0.14D,
                0.12F, 0.15F, 0.17F, 1.0F);
    }

    private static void drawHub() {
        drawCylinder(0.52D, 3.48D, 3.76D, 24, 0.14F, 0.65F, 0.72F, 1.0F);
    }

    /** Seven solid steps fill the 3x3 shaft while retaining a recognizable drill silhouette. */
    private static void drawSteppedDrill(boolean active, double time) {
        final double bottom = 0.38D;
        final double stepHeight = 0.43D;
        float pulse = active ? 0.78F + 0.22F * (float) Math.sin(time * 0.42D) : 0.62F;
        for (int i = 0; i < STEP_RADII.length; i++) {
            double minY = bottom + i * stepHeight;
            double maxY = minY + stepHeight;
            float shade = 0.32F + i * 0.025F;
            drawCappedCylinder(STEP_RADII[i], minY, maxY, 32,
                    shade * pulse, (shade + 0.12F) * pulse, (shade + 0.15F) * pulse, 1.0F);
            // Bright cutting lip and asymmetric teeth make rotation visible.
            drawCylinder(STEP_RADII[i] + 0.035D, minY, minY + 0.065D, 32,
                    active ? 0.16F : 0.22F, active ? 0.84F : 0.52F, active ? 0.92F : 0.58F, 1.0F);
            drawStepTeeth(STEP_RADII[i], minY, maxY, i, active);
        }
        drawTip(active);
    }

    private static void drawStepTeeth(double radius, double minY, double maxY, int step, boolean active) {
        float red = active ? 0.96F : 0.48F;
        float green = active ? 0.44F : 0.38F;
        float blue = active ? 0.10F : 0.24F;
        GL11.glPushMatrix();
        GL11.glRotatef(step * 19.0F, 0, 1, 0);
        for (int tooth = 0; tooth < 3; tooth++) {
            GL11.glPushMatrix();
            GL11.glRotatef(tooth * 120.0F, 0, 1, 0);
            drawBox(radius * 0.48D, minY + 0.06D, -0.055D,
                    radius + 0.075D, maxY - 0.045D, 0.055D, red, green, blue, 1.0F);
            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    private static void drawTip(boolean active) {
        Tessellator t = Tessellator.instance;
        float red = active ? 1.0F : 0.48F;
        float green = active ? 0.46F : 0.34F;
        float blue = active ? 0.08F : 0.20F;
        GL11.glColor4f(red, green, blue, 1.0F);
        t.startDrawing(GL11.GL_TRIANGLES);
        t.setColorRGBA_F(red, green, blue, 1.0F);
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            double a = Math.PI * 2.0D * i / segments;
            double b = Math.PI * 2.0D * (i + 1) / segments;
            vertex(t,Math.cos(a) * STEP_RADII[0], 0.38D, Math.sin(a) * STEP_RADII[0]);
            vertex(t,Math.cos(b) * STEP_RADII[0], 0.38D, Math.sin(b) * STEP_RADII[0]);
            vertex(t,0, -0.04D, 0);
        }
        t.draw();
    }

    private static void drawCappedCylinder(double radius, double minY, double maxY, int segments,
                                            float red, float green, float blue, float alpha) {
        drawCylinder(radius, minY, maxY, segments, red, green, blue, alpha);
        Tessellator t = Tessellator.instance;
        GL11.glColor4f(red, green, blue, alpha);
        t.startDrawing(GL11.GL_TRIANGLES);
        t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
                alpha >= .99F ? .30F + green * .70F : green,
                alpha >= .99F ? .30F + blue * .70F : blue, alpha);
        for (int i = 0; i < segments; i++) {
            double a = Math.PI * 2.0D * i / segments;
            double b = Math.PI * 2.0D * (i + 1) / segments;
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
        GL11.glColor4f(red, green, blue, alpha);
        t.startDrawingQuads(); t.setBrightness(0x00F000F0);
        t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
                alpha >= .99F ? .30F + green * .70F : green,
                alpha >= .99F ? .30F + blue * .70F : blue, alpha);
        for (int i = 0; i < segments; i++) {
            double a = Math.PI * 2.0D * i / segments;
            double b = Math.PI * 2.0D * (i + 1) / segments;
            double ax = Math.cos(a) * radius, az = Math.sin(a) * radius;
            double bx = Math.cos(b) * radius, bz = Math.sin(b) * radius;
            vertex(t,ax, minY, az); vertex(t,bx, minY, bz);
            vertex(t,bx, maxY, bz); vertex(t,ax, maxY, az);
        }
        t.draw();
    }

    private static void drawBox(double minX, double minY, double minZ,
                                double maxX, double maxY, double maxZ,
                                float red, float green, float blue, float alpha) {
        Tessellator t = Tessellator.instance;
        GL11.glColor4f(red, green, blue, alpha);
        t.startDrawingQuads(); t.setBrightness(0x00F000F0);
        t.setColorRGBA_F(alpha >= .99F ? .30F + red * .70F : red,
                alpha >= .99F ? .30F + green * .70F : green,
                alpha >= .99F ? .30F + blue * .70F : blue, alpha);
        t.addVertexWithUV(minX, minY, minZ,0,0); t.addVertexWithUV(maxX, minY, minZ,1,0); t.addVertexWithUV(maxX, minY, maxZ,1,1); t.addVertexWithUV(minX, minY, maxZ,0,1);
        t.addVertexWithUV(minX, maxY, minZ,0,0); t.addVertexWithUV(minX, maxY, maxZ,1,0); t.addVertexWithUV(maxX, maxY, maxZ,1,1); t.addVertexWithUV(maxX, maxY, minZ,0,1);
        t.addVertexWithUV(minX, minY, minZ,0,0); t.addVertexWithUV(minX, maxY, minZ,1,0); t.addVertexWithUV(maxX, maxY, minZ,1,1); t.addVertexWithUV(maxX, minY, minZ,0,1);
        t.addVertexWithUV(minX, minY, maxZ,0,0); t.addVertexWithUV(maxX, minY, maxZ,1,0); t.addVertexWithUV(maxX, maxY, maxZ,1,1); t.addVertexWithUV(minX, maxY, maxZ,0,1);
        t.addVertexWithUV(minX, minY, minZ,0,0); t.addVertexWithUV(minX, minY, maxZ,1,0); t.addVertexWithUV(minX, maxY, maxZ,1,1); t.addVertexWithUV(minX, maxY, minZ,0,1);
        t.addVertexWithUV(maxX, minY, minZ,0,0); t.addVertexWithUV(maxX, maxY, minZ,1,0); t.addVertexWithUV(maxX, maxY, maxZ,1,1); t.addVertexWithUV(maxX, minY, maxZ,0,1);
        t.draw();
    }
    private static void vertex(Tessellator t, double x, double y, double z) {
        t.addVertexWithUV(x,y,z,x+z,y);
    }
}
