package com.mod.htgt6.client.render;

import com.mod.htgt6.common.centrifuge.MultiTileIndustrialCentrifuge;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import java.util.Map;
import java.util.WeakHashMap;

/** Dedicated mechanical core. No chemical-plant rendering or spawned entities. */
public final class IndustrialCentrifugeRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation METAL = new ResourceLocation("htgt6", "textures/blocks/multiblock_v2_centrifuge_inner.png");
    private final Map<TileEntity, Motion> motions = new WeakHashMap<TileEntity, Motion>();
    private static final class Motion { double time = -1, angle; }

    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (!(tile instanceof MultiTileIndustrialCentrifuge)) return;
        MultiTileIndustrialCentrifuge machine = (MultiTileIndustrialCentrifuge) tile;
        if (!machine.isStructureFormed() || machine.getWorldObj() == null) return;
        boolean active = machine.isMachineActive() && machine.getStructureDiagnostic().startsWith("PROCESSING:");
        Motion motion = motions.get(tile);
        if (motion == null) { motion = new Motion(); motions.put(tile, motion); }
        double now = MultiblockAnimationClock.frameTime(machine.getWorldObj().getTotalWorldTime(), partialTicks);
        if (motion.time >= 0 && active) motion.angle = (motion.angle + Math.max(0, Math.min(2, now - motion.time)) * 16) % 360;
        motion.time = now;
        int[] center = machine.localToWorld(0, 0, 3);
        int oldTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        float oldLightX = OpenGlHelper.lastBrightnessX, oldLightY = OpenGlHelper.lastBrightnessY;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + center[0] - tile.xCoord + .5, y + 1.02, z + center[2] - tile.zCoord + .5);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            bindTexture(METAL);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 220, 220);
            // Lower bearing and motor housing, upper bearing and fixed braces.
            cylinder(1.15, 0, .22, .52F);
            cylinder(.66, .22, .57, .72F);
            cylinder(.43, .57, .76, .95F);
            cylinder(.85, 2.55, 2.93, .60F);
            box(-2.40, 2.72, -.15, 2.40, 2.91, .15, .68F);
            box(-.15, 2.72, -2.40, .15, 2.91, 2.40, .68F);
            // Fixed perimeter guards and bearing fasteners clear the rotating canisters.
            for(int brace=0;brace<8;brace++) {
                GL11.glPushMatrix(); GL11.glRotatef(brace*45,0,1,0);
                box(2.10,.10,-.10,2.28,2.52,.10,.65F);
                box(.79,2.63,-.07,.98,2.80,.07,.84F);
                GL11.glPopMatrix();
            }
            GL11.glPushMatrix();
            try {
                GL11.glRotated(motion.angle, 0, 1, 0);
                cylinder(.18, .48, 2.70, .96F);
                cylinder(2.0, .72, .92, .72F);
                cylinder(.52, .92, 1.18, .86F);
                for (int i = 0; i < 6; i++) {
                    GL11.glPushMatrix();
                    GL11.glRotatef(i * 60, 0, 1, 0);
                    box(.35, 1.05, -.10, 1.50, 1.20, .10, .66F);
                    GL11.glTranslated(1.48, 0, 0);
                    cylinder(.43, .92, 1.08, .42F);
                    cylinder(.36, 1.08, 1.98, .91F);
                    cylinder(.41, 1.98, 2.10, .59F);
                    cylinder(.23, 2.10, 2.20, .81F);
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    GL11.glColor4f(active ? 1F : .36F, active ? .65F : .28F, .08F, 1F);
                    indicator(.365, 1.34, -.08, 1.76, .08);
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    GL11.glPopMatrix();
                }
            } finally { GL11.glPopMatrix(); }
        } finally {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, oldLightX, oldLightY);
            GL11.glPopMatrix();
            GL11.glPopAttrib();
            OpenGlHelper.setActiveTexture(oldTexture);
        }
    }

    private static void cylinder(double radius, double bottom, double top, float shade) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8, b = (i + 1) * Math.PI / 8;
            double x1 = Math.cos(a) * radius, z1 = Math.sin(a) * radius;
            double x2 = Math.cos(b) * radius, z2 = Math.sin(b) * radius;
            float light = shade * (float) (.72 + .28 * Math.cos(a - .7));
            t.setColorOpaque_F(light, light, light);
            t.addVertexWithUV(x1,bottom,z1,0,1); t.addVertexWithUV(x2,bottom,z2,1,1);
            t.addVertexWithUV(x2,top,z2,1,0); t.addVertexWithUV(x1,top,z1,0,0);
            t.setColorOpaque_F(shade, shade, shade);
            t.addVertexWithUV(0,top,0,.5,.5); t.addVertexWithUV(x1,top,z1,.5+x1/(2*radius),.5+z1/(2*radius));
            t.addVertexWithUV(x2,top,z2,.5+x2/(2*radius),.5+z2/(2*radius)); t.addVertexWithUV(0,top,0,.5,.5);
        }
        t.draw();
    }

    private static void box(double x1, double y1, double z1, double x2, double y2, double z2, float shade) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads(); t.setColorOpaque_F(shade, shade, shade);
        quad(t,x1,y1,z1,x2,y1,z1,x2,y2,z1,x1,y2,z1);
        quad(t,x1,y1,z2,x2,y1,z2,x2,y2,z2,x1,y2,z2);
        quad(t,x1,y1,z1,x1,y1,z2,x1,y2,z2,x1,y2,z1);
        quad(t,x2,y1,z1,x2,y1,z2,x2,y2,z2,x2,y2,z1);
        quad(t,x1,y2,z1,x2,y2,z1,x2,y2,z2,x1,y2,z2);
        quad(t,x1,y1,z1,x2,y1,z1,x2,y1,z2,x1,y1,z2);
        t.draw();
    }
    private static void quad(Tessellator t,double a,double b,double c,double d,double e,double f,double g,double h,double i,double j,double k,double l) {
        t.addVertexWithUV(a,b,c,0,0); t.addVertexWithUV(d,e,f,1,0); t.addVertexWithUV(g,h,i,1,1); t.addVertexWithUV(j,k,l,0,1);
    }
    private static void indicator(double x,double y1,double z1,double y2,double z2) {
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(x,y1,z1); GL11.glVertex3d(x,y2,z1); GL11.glVertex3d(x,y2,z2); GL11.glVertex3d(x,y1,z2);
        GL11.glEnd();
    }
}
