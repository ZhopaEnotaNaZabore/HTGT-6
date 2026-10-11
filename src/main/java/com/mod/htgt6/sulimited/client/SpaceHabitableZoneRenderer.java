package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceHabitableZoneData;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/** Draws SpaceTech-defined green habitable zones on GalaxySpace system maps. */
public final class SpaceHabitableZoneRenderer {
    private static final int SEGMENTS = 144;

    @SubscribeEvent
    public void onCelestialRing(CelestialBodyRenderEvent.CelestialRingRenderEvent.Pre event) {
        SpaceHabitableZoneData zone = SpaceTechExpandedRegistry
                .getHabitableZoneForBody(event.celestialBody.getName());
        if (zone == null) return;

        event.setCanceled(true);
        float orbitRadius = 75.0F * event.celestialBody.getRelativeDistanceFromCenter().unScaledDistance;
        float innerRadius = orbitRadius * zone.innerOrbitFactor;
        float outerRadius = orbitRadius * zone.outerOrbitFactor;
        Vector3f center = event.parentOffset;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(1.5F);
        GL11.glTranslatef(center.x, center.y, center.z);
        GL11.glColor4f(0.12F, 0.82F, 0.24F, 0.12F);
        drawFilledBand(innerRadius, outerRadius);
        GL11.glColor4f(0.12F, 0.82F, 0.24F, 0.90F);
        drawCircle(innerRadius);
        drawCircle(outerRadius);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
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
