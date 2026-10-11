package com.mod.htgt6.watcher.client;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Static in-game stop screen, not an OS crash or a native desktop overlay. */
public final class GuiWatcherStop extends GuiScreen {
    private static final ResourceLocation IMAGE = new ResourceLocation("htgt6", "textures/gui/watcher/stop.png");

    @Override public void initGui() { buttonList.clear(); }
    @Override public boolean doesGuiPauseGame() { return true; }
    @Override protected void keyTyped(char character, int key) { }
    @Override public void handleKeyboardInput() { }
    @Override public void handleMouseInput() { }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xFF0078D7);
        float scale = Math.min(width / 1600.0F, height / 900.0F);
        float w = 1600 * scale, h = 900 * scale;
        float x = (width - w) / 2, y = (height - h) / 2;
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(IMAGE);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + h, 0, 0, 1);
        t.addVertexWithUV(x + w, y + h, 0, 1, 1);
        t.addVertexWithUV(x + w, y, 0, 1, 0);
        t.addVertexWithUV(x, y, 0, 0, 0);
        t.draw();
    }
}
