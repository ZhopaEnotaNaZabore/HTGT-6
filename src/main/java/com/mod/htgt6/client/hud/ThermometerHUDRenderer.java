package com.mod.htgt6.client.hud;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

public class ThermometerHUDRenderer {

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {

        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        if (!ThermometerHUD.isVisible())
            return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRenderer;

        int width = event.resolution.getScaledWidth();
        int height = event.resolution.getScaledHeight();

        int x = width / 2;
        int y = height - 55;

        float alpha = ThermometerHUD.getAlpha();

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1F, 1F, 1F, alpha);

        int titleWidth = fr.getStringWidth(ThermometerHUD.getTitle());
        int textWidth = fr.getStringWidth(ThermometerHUD.getText());

        int boxWidth = Math.max(titleWidth, textWidth) + 16;
        int boxHeight = 28;

        int left = x - boxWidth / 2;
        int top = y - boxHeight / 2;

        int bg = ((int)(alpha * 160) << 24);
        int border = ((int)(alpha * 255) << 24) | 0x404040;

        drawRect(left, top, left + boxWidth, top + boxHeight, bg);

        drawRect(left, top, left + boxWidth, top + 1, border);
        drawRect(left, top + boxHeight - 1, left + boxWidth, top + boxHeight, border);
        drawRect(left, top, left + 1, top + boxHeight, border);
        drawRect(left + boxWidth - 1, top, left + boxWidth, top + boxHeight, border);

        fr.drawStringWithShadow(
                ThermometerHUD.getTitle(),
                x - titleWidth / 2,
                top + 5,
                0xFFFF55);

        fr.drawStringWithShadow(
                ThermometerHUD.getText(),
                x - textWidth / 2,
                top + 16,
                0xFFFFFF);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawRect(int left, int top, int right, int bottom, int color) {

        net.minecraft.client.gui.Gui.drawRect(
                left,
                top,
                right,
                bottom,
                color);
    }
}
