package com.mod.htgt6.minecraft1202.Module.Client.Exploration;

import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * Compatibility boundary for backported renderers.
 *
 * Minecraft 1.12 routes state changes through GlStateManager.  Forge 1.7 renderers commonly
 * change raw GL state, so a partial GlStateManager port would cache stale values.  This guard
 * provides the useful part of that contract without replacing the global renderer: every
 * HTGT6 render section restores server, client, texture-unit and matrix-mode state on exit.
 */
public final class RenderStateGuard112 implements AutoCloseable {
    private static final int CLIENT_ALL_ATTRIB_BITS = -1;
    private final int activeTexture;
    private final int matrixMode;
    private boolean closed;

    private RenderStateGuard112() {
        activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(CLIENT_ALL_ATTRIB_BITS);
    }

    public static RenderStateGuard112 capture() {
        return new RenderStateGuard112();
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        GL11.glPopClientAttrib();
        GL11.glPopAttrib();
        OpenGlHelper.setActiveTexture(activeTexture);
        GL11.glMatrixMode(matrixMode);
    }
}
