package com.mod.htgt6.client.nei;

import codechicken.lib.gui.GuiDraw;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

/** Machine recipes store chances in basis points, including hundredths of a percent. */
public final class NEIOutputChance {
    private NEIOutputChance() { }
    public static String label(int chance) {
        if (chance >= 10000) return "";
        return java.math.BigDecimal.valueOf(Math.max(0, chance), 2).stripTrailingZeros().toPlainString() + "%";
    }
    public static void draw(int[] chances, int index, int x, int y) {
        if (chances == null || index >= chances.length) return;
        String text = label(chances[index]);
        if (text.isEmpty()) return;
        int width = Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
        float scale = Math.min(0.5F, 17F / Math.max(1, width));
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 8, y + 17, 200);
        GL11.glScalef(scale, scale, 1);
        GuiDraw.drawString(text, -width / 2, 0, 0xFFE080, true);
        GL11.glPopMatrix();
    }
}
