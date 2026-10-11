package com.mod.htgt6.minecraft1202.Module.Client.Combat;

import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.lwjgl.opengl.GL11;

/** Through-wall entity outline adapter for the 1.9 Glowing effect. */
public final class GlowingOutlineRenderer120 {
    private boolean renderingOutline;

    @SubscribeEvent
    public void afterLivingRender(RenderLivingEvent.Post event) {
        if (renderingOutline || event.entity == null
                || !event.entity.isPotionActive(CombatContent.glowing)) return;
        renderingOutline = true;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GL11.glLineWidth(2.5F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.9F);
            event.renderer.doRender(event.entity, event.x, event.y, event.z,
                    event.entity.rotationYaw, 1.0F);
        } finally {
            GL11.glPopAttrib();
            renderingOutline = false;
        }
    }
}
