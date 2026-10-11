package com.mod.htgt6.client.GUI;

import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;
import com.mod.htgt6.common.TE.RadioisotopeGeneratorTE;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/** Animated vector radiation indicator for the RTG work area. */
@SideOnly(Side.CLIENT)
public final class RadioisotopeActivityWidget extends Widget<RadioisotopeActivityWidget> {
    private static final int SEGMENTS = 12;
    private final RadioisotopeGeneratorTE generator;

    public RadioisotopeActivityWidget(RadioisotopeGeneratorTE generator) {
        this.generator = generator;
        tooltip(t -> {
            t.setUpdateTooltipEveryTick(true);
            t.addLine(isActive() ? "RTG active" : generator.enabled ? "RTG idle" : "RTG disabled");
        });
    }

    private boolean isActive() {
        return generator.enabled && generator.actualEnergyProduction > 0;
    }

    @Override
    public void draw(GuiContext context) {
        boolean active = isActive();
        double time = System.currentTimeMillis() / 1000.0D;
        float pulse = active ? 0.72F + 0.28F * (float) Math.sin(time * 4.5D) : 0.45F;

        int symbol = active
                ? Color.HSBtoRGB((float) ((time * 0.10D + 0.16D) % 1.0D), 0.82F, 1.0F)
                : 0x4A4D50;

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LINE_BIT);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        GL11.glTranslatef(29F, 29F, 0F);

        // The radiation trefoil rotates only while the isotope is actively decaying.
        if (active) GL11.glRotatef((float) (time * 42.0D), 0F, 0F, 1F);
        // Dark, slightly larger silhouette provides contrast against the animated colour.
        setColor(0x080B0D, active ? 0.95F : 0.72F);
        for (int blade = 0; blade < 3; blade++) {
            drawBlade(-90.0D + blade * 120.0D, 4.5F, 19.0F, 72.0D);
        }
        drawDisc(0F, 1F, 4.2F);

        setColor(symbol, active ? pulse : 0.78F);
        for (int blade = 0; blade < 3; blade++) {
            drawBlade(-90.0D + blade * 120.0D, 6.5F, 16.8F, 62.0D);
        }
        drawDisc(0F, 1F, 2.8F);

        GL11.glPopMatrix();
        GL11.glPopAttrib();
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private static void drawBlade(double centreAngle, float innerRadius, float outerRadius, double width) {
        double start = centreAngle - width / 2.0D;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= SEGMENTS; i++) {
            double angle = Math.toRadians(start + i * (width / SEGMENTS));
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            GL11.glVertex2f(cos * innerRadius, 1F + sin * innerRadius);
            GL11.glVertex2f(cos * outerRadius, 1F + sin * outerRadius);
        }
        GL11.glEnd();
    }

    private static void drawDisc(float x, float y, float radius) {
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x, y);
        for (int i = 0; i <= 20; i++) {
            double angle = Math.PI * 2.0D * i / 20.0D;
            GL11.glVertex2f(x + (float) Math.cos(angle) * radius,
                    y + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();
    }

    private static void setColor(int rgb, float alpha) {
        GL11.glColor4f(((rgb >> 16) & 255) / 255F,
                ((rgb >> 8) & 255) / 255F,
                (rgb & 255) / 255F, alpha);
    }
}
