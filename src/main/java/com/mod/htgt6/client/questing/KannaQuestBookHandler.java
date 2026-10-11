package com.mod.htgt6.client.questing;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.IGuiCanvas;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.client.gui2.GuiHome;
import betterquesting.client.gui2.GuiQuest;
import betterquesting.client.gui2.GuiQuestLines;
import betterquesting.client.themes.ThemeRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.opengl.GL11;

/** Optional client integration; quest settings and server-side data are never modified. */
public final class KannaQuestBookHandler {
    private static final ResourceLocation THEME = new ResourceLocation("htgt6:kanna");
    private static final ResourceLocation ART = new ResourceLocation("htgt6:textures/gui/quests/kanna_home.png");

    private static boolean isKanna() {
        IGuiTheme theme = ThemeRegistry.INSTANCE.getCurrentTheme();
        return theme != null && THEME.equals(theme.getID());
    }

    @SubscribeEvent
    public void onOpen(GuiOpenEvent event) {
        // Only a fresh book opening from gameplay. Navigation inside BQ keeps its parent/bookmark.
        if (Minecraft.getMinecraft().currentScreen == null && isKanna()
                && (event.gui instanceof GuiQuestLines || event.gui instanceof GuiQuest)) {
            event.gui = new GuiHome(null);
        }
    }

    @SubscribeEvent
    public void onInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!isKanna()) return;
        if (event.gui instanceof GuiQuestLines) {
            IGuiCanvas screen = (GuiQuestLines) event.gui;
            if (!screen.getChildren().isEmpty() && screen.getChildren().get(0) instanceof CanvasTextured) {
                IGuiCanvas original = (IGuiCanvas) screen.getChildren().get(0);
                CanvasEmpty patterned = new CanvasEmpty(original.getTransform()) {
                    @Override
                    public void drawPanel(int mx, int my, float partialTick) {
                        drawPattern(getTransform());
                        super.drawPanel(mx, my, partialTick);
                    }
                };
                // Keep the live controls and their transforms; addPanel would reinitialize them.
                patterned.getChildren().addAll(original.getChildren());
                screen.getChildren().set(0, patterned);
            }
            installGraphPattern(screen);
            return;
        }
        if (!(event.gui instanceof GuiHome)) return;
        // BQ 3.7.15: main panel -> inner canvas -> splash canvas. Preserve all native buttons.
        IGuiCanvas home = (GuiHome) event.gui;
        if (home.getChildren().isEmpty() || !(home.getChildren().get(0) instanceof CanvasTextured)) return;
        IGuiCanvas outer = (IGuiCanvas) home.getChildren().get(0);
        if (outer.getChildren().isEmpty() || !(outer.getChildren().get(0) instanceof CanvasEmpty)) return;
        IGuiCanvas inner = (IGuiCanvas) outer.getChildren().get(0);
        if (inner.getChildren().isEmpty() || !(inner.getChildren().get(0) instanceof CanvasTextured)) return;
        IGuiPanel splash = inner.getChildren().get(0);
        // Reuse the attached transform, replacing only the original illustration and its title.
        inner.getChildren().set(0, new KannaSplash(splash.getTransform()));
    }

    /** Place the ornament after the opaque tray background, before the scrolling graph. */
    private static void installGraphPattern(IGuiCanvas canvas) {
        for (int i = 0; i < canvas.getChildren().size(); i++) {
            IGuiPanel child = canvas.getChildren().get(i);
            if (child instanceof CanvasQuestLine) {
                if (i == 0 || !(canvas.getChildren().get(i - 1) instanceof GraphPattern)) {
                    canvas.getChildren().add(i, new GraphPattern(child.getTransform()));
                }
                return;
            }
            if (child instanceof IGuiCanvas) installGraphPattern((IGuiCanvas) child);
        }
    }

    private static final class GraphPattern extends CanvasEmpty {
        GraphPattern(IGuiRect rect) { super(rect); }
        @Override
        public void drawPanel(int mx, int my, float partialTick) { drawPattern(getTransform()); }
    }

    /** Viewport-anchored woven diamonds and circuit traces; batched to keep large maps cheap. */
    private static void drawPattern(IGuiRect r) {
        int x = r.getX(), y = r.getY(), w = r.getWidth(), h = r.getHeight();
        if (w <= 0 || h <= 0) return;
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT | GL11.GL_LINE_BIT);
        Gui.drawRect(x, y, x + w, y + h, 0xFF181715);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.0F);
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_LINES);
        // Complete tiles stay within the viewport at every GUI scale and tray width.
        for (int cy = y + 42; cy + 38 < y + h; cy += 80) {
            for (int cx = x + 42; cx + 38 < x + w; cx += 80) {
                t.setColorOpaque_I(0x343029);
                diamond(t, cx, cy, 35);
                diamond(t, cx, cy, 29);
                t.setColorOpaque_I(0x2B2924);
                segment(t, cx - 38, cy - 38, cx - 18, cy - 38);
                segment(t, cx - 18, cy - 38, cx - 18, cy - 29);
                segment(t, cx + 38, cy + 38, cx + 18, cy + 38);
                segment(t, cx + 18, cy + 38, cx + 18, cy + 29);
                segment(t, cx - 38, cy + 38, cx - 38, cy + 18);
                segment(t, cx + 38, cy - 38, cx + 38, cy - 18);
                t.setColorOpaque_I(0x473829);
                diamond(t, cx, cy, 5);
                segment(t, cx - 12, cy, cx - 7, cy);
                segment(t, cx + 7, cy, cx + 12, cy);
                segment(t, cx, cy - 12, cx, cy - 7);
                segment(t, cx, cy + 7, cx, cy + 12);
                t.setColorOpaque_I(0x29403C);
                diamond(t, cx - 34, cy - 34, 2);
                diamond(t, cx + 34, cy + 34, 2);
            }
        }
        t.draw();
        Gui.drawRect(x, y, x + w, y + 1, 0xFF62452D);
        Gui.drawRect(x, y + h - 1, x + w, y + h, 0xFF45382B);
        Gui.drawRect(x, y, x + 1, y + h, 0xFF45382B);
        Gui.drawRect(x + w - 1, y, x + w, y + h, 0xFF45382B);
        GL11.glPopAttrib();
    }

    private static void segment(Tessellator t, int x1, int y1, int x2, int y2) {
        t.addVertex(x1, y1, 0);
        t.addVertex(x2, y2, 0);
    }

    private static void diamond(Tessellator t, int x, int y, int radius) {
        segment(t, x, y - radius, x + radius, y);
        segment(t, x + radius, y, x, y + radius);
        segment(t, x, y + radius, x - radius, y);
        segment(t, x - radius, y, x, y - radius);
    }

    private static final class KannaSplash extends CanvasEmpty {
        KannaSplash(IGuiRect rect) { super(rect); }

        @Override
        public void drawPanel(int mx, int my, float partialTick) {
            IGuiRect r = getTransform();
            int x = r.getX(), y = r.getY(), w = r.getWidth(), h = r.getHeight();
            if (w <= 0 || h <= 0) return;
            // Fit the complete illustration: never crop the mascot's ears or feet at wide GUI scales.
            drawPattern(r);
            double scale = Math.min(w / 1536.0, h / 1024.0);
            double iw = 1536 * scale, ih = 1024 * scale;
            double ix = x + (w - iw) / 2, iy = y + (h - ih) / 2;
            Minecraft mc = Minecraft.getMinecraft();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(ART);
            Tessellator t = Tessellator.instance;
            t.startDrawingQuads();
            t.addVertexWithUV(ix, iy + ih, 0, 0, 1);
            t.addVertexWithUV(ix + iw, iy + ih, 0, 1, 1);
            t.addVertexWithUV(ix + iw, iy, 0, 1, 0);
            t.addVertexWithUV(ix, iy, 0, 0, 0);
            t.draw();
            int titleWidth = Math.min(w, Math.max(100, w / 2));
            Gui.drawRect(x, y, x + titleWidth, y + Math.min(52, h), 0xB0100D0A);
            Gui.drawRect(x, y, x + w, y + 2, 0xFFFFA449);
            if (w >= 100 && h >= 52) {
                mc.fontRenderer.drawStringWithShadow("HiTech6 / KANNA", x + 12, y + 13, 0xFFFFBC75);
                String subtitle = I18n.format("htgt6.questbook.kanna.subtitle");
                mc.fontRenderer.drawStringWithShadow(mc.fontRenderer.trimStringToWidth(subtitle, titleWidth - 24),
                        x + 12, y + 30, 0xFFE5D5C2);
            }
            GL11.glPopAttrib();
        }
    }
}
