package com.mod.htgt6.sulimited.client;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import com.mod.htgt6.sulimited.galacticraft.Glise792.sysGliese792;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.event.client.CelestialBodyRenderEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import org.lwjgl.opengl.GL11;

/** Keeps indexed Gliese and Ra celestial textures animated. */
public final class GlieseCelestialRotationHandler {
    private float partialTicks;
    private CelestialBody rotatedBody;

    @SubscribeEvent public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) partialTicks = event.renderTickTime;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void beforeMapBody(CelestialBodyRenderEvent.Pre event) {
        CelestialBody body = event.celestialBody;
        int period = body == sysGliese792.planetGlieseB ? 2400
                : body == sysGliese792.planetGlieseC ? 3600
                : body == sysGliese792.starGliese792 ? 4800 : 0;
        if (period == 0 || Minecraft.getMinecraft().theWorld == null) return;
        GL11.glPushMatrix();
        rotatedBody = body;
        GL11.glRotatef(GlieseDiscAnimation.angle(Minecraft.getMinecraft().theWorld.getTotalWorldTime(), partialTicks, period), 0, 0, 1);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void afterMapBody(CelestialBodyRenderEvent.Post event) {
        if (rotatedBody != null && rotatedBody == event.celestialBody) {
            GL11.glPopMatrix();
            rotatedBody = null;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getMinecraft().theWorld == null) return;
        CelestialTexturePainter.updateGlieseRotation(Minecraft.getMinecraft().theWorld.getTotalWorldTime());
    }
}
