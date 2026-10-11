package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor.MinorWorlds;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import galaxyspace.systems.SolarSystem.SolarSystemBodies;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/** Marks the represented outer boundary of Sol on the system map. */
public final class SolarSystemBoundaryRenderer {
    private static final int SEGMENTS = 144;
    private static final float OUTER_MARGIN = 1.08F;

    @SubscribeEvent
    public void onCelestialRing(CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre event) {
        if (MinorWorlds.deeDee == null || event.celestialBody != MinorWorlds.deeDee
                || SolarSystemBodies.planetKuiperBelt == null) {
            return;
        }

        float innerRadius = orbitRadius(SolarSystemBodies.planetKuiperBelt);
        float outerRadius = orbitRadius(MinorWorlds.deeDee) * OUTER_MARGIN;
        if (outerRadius <= innerRadius) return;

        Vector3f center = event.parentOffset;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_LINE_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.5F);
        GL11.glTranslatef(center.x, center.y, center.z);

        GL11.glColor4f(0.08F, 0.82F, 0.78F, 0.11F);
        drawFilledBand(innerRadius, outerRadius);
        GL11.glColor4f(0.10F, 0.92F, 0.86F, 0.90F);
        drawCircle(innerRadius);
        drawCircle(outerRadius);

        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static float orbitRadius(micdoodle8.mods.galacticraft.api.galaxies.CelestialBody body) {
        return 75.0F * body.getRelativeDistanceFromCenter().unScaledDistance;
    }

    private static void drawCircle(float radius) {
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int point = 0; point < SEGMENTS; point++) {
            double angle = Math.PI * 2.0D * point / SEGMENTS;
            GL11.glVertex2d(Math.cos(angle) * radius, Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    private static void drawFilledBand(float innerRadius, float outerRadius) {
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int point = 0; point <= SEGMENTS; point++) {
            double angle = Math.PI * 2.0D * point / SEGMENTS;
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            GL11.glVertex2d(cosine * outerRadius, sine * outerRadius);
            GL11.glVertex2d(cosine * innerRadius, sine * innerRadius);
        }
        GL11.glEnd();
    }
}
