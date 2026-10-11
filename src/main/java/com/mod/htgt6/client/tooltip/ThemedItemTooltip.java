package com.mod.htgt6.client.tooltip;

import com.gtnewhorizon.gtnhlib.client.event.RenderTooltipEvent;
import com.cleanroommc.modularui.api.widget.IGuiElement;
import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.integration.nei.NEIIngredientProvider;
import com.cleanroommc.modularui.screen.GuiScreenWrapper;
import com.mod.htgt6.common.ui.MenuTheme;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Uses the shared GTNHLib/NEI tooltip event; no item information is replaced. */
public final class ThemedItemTooltip {
    private static final float TITLE_SCALE = 1.5F;
    private String lastKey = "";
    private int page;
    private boolean previousUp, previousDown;
    private long lastDraw;

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onTooltip(final RenderTooltipEvent event) {
        if (event.itemStack == null) return;
        if (event.gui instanceof GuiScreenWrapper) {
            IGuiElement hovered = ((GuiScreenWrapper) event.gui).getScreen().context.getHovered();
            // ModularUI draws its widget tooltip in drawForeground, separately
            // from NEI's RenderTooltipEvent. Suppress only NEI's duplicate for
            // that same widget item; NEI's external item panel remains themed.
            if (hasNativeTooltip(hovered, event.itemStack)) {
                event.setCanceled(true);
                return;
            }
        }
        if (event.alternativeRenderer != null) return;
        String id = Item.itemRegistry.getNameForObject(event.itemStack.getItem());
        if (id == null || !id.toLowerCase(java.util.Locale.ROOT).startsWith("htgt6:")) return;
        event.setBackground(MenuTheme.color(0xFA141B2C, 0xFA1C1916));
        event.borderStart = MenuTheme.color(0xFF9299DD, 0xFFDEA15B);
        event.calculateBorderEnd();
        event.alternativeRenderer = lines -> draw(event, lines);
    }

    static boolean hasNativeTooltip(IGuiElement hovered, ItemStack stack) {
        if (!(hovered instanceof NEIIngredientProvider) || !(hovered instanceof ITooltip)) return false;
        ItemStack nativeStack = ((NEIIngredientProvider) hovered).getStackForNEI();
        return nativeStack != null && nativeStack.isItemEqual(stack)
                && ItemStack.areItemStackTagsEqual(nativeStack, stack)
                && ((ITooltip<?>) hovered).hasTooltip();
    }

    private void draw(RenderTooltipEvent e, List<String> source) {
        if (source == null || source.isEmpty()) return;
        // Preserve NEI's embedded item/fluid/progress renderers and paging protocol.
        for (String line : source) if (line != null && line.startsWith("\u00a7x")
                && cpw.mods.fml.common.Loader.isModLoaded("NotEnoughItems")) {
            codechicken.nei.guihook.GuiContainerManager.drawPagedTooltip(e.font, e.x + 12, e.y - 12, source);
            return;
        }
        FontRenderer font = e.font;
        int available = Math.max(24, e.gui.width - 20);
        int maxTextWidth = Math.min(330, available - 20);
        List<String> title = wrap(font, source.get(0), (int) (maxTextWidth / TITLE_SCALE));
        List<String> body = new ArrayList<String>();
        for (int i = 1; i < source.size(); i++) {
            String line = clean(source.get(i));
            // Replace decorative ASCII dividers with the panel's actual separator.
            String plain = EnumChatFormatting.getTextWithoutFormattingCodes(line).trim();
            if (plain.matches("[=\\-_]{5,}")) continue;
            body.addAll(wrap(font, line, maxTextWidth));
        }
        int width = 100;
        for (String line : title) width = Math.max(width,
                (int) Math.ceil(font.getStringWidth(line) * TITLE_SCALE));
        for (String line : body) width = Math.max(width, font.getStringWidth(line));
        width = Math.min(available, width + 20);
        int lineHeight = font.FONT_HEIGHT + 2;
        int headerHeight = 13 + (int) Math.ceil(title.size() * lineHeight * TITLE_SCALE);
        int capacity = Math.max(1, (e.gui.height - headerHeight - 42) / lineHeight);
        int pages = Math.max(1, (body.size() + capacity - 1) / capacity);
        String key = Item.itemRegistry.getNameForObject(e.itemStack.getItem()) + ":"
                + e.itemStack.getItemDamage() + ":" + source.hashCode();
        long now = System.currentTimeMillis();
        if (!key.equals(lastKey) || now - lastDraw > 250) page = 0;
        lastKey = key; lastDraw = now;
        boolean up = Keyboard.isKeyDown(Keyboard.KEY_PRIOR), down = Keyboard.isKeyDown(Keyboard.KEY_NEXT);
        if (up && !previousUp) page--;
        if (down && !previousDown) page++;
        previousUp = up; previousDown = down;
        page = Math.max(0, Math.min(pages - 1, page));
        int first = page * capacity, count = Math.min(capacity, body.size() - first);
        int height = headerHeight + count * lineHeight + (pages > 1 ? 22 : 10);
        int x = e.x + 14;
        if (x + width > e.gui.width - 5) x = e.x - width - 14;
        x = Math.max(5, Math.min(x, e.gui.width - width - 5));
        int y = Math.max(5, Math.min(e.y - 12, e.gui.height - height - 5));
        int accent = MenuTheme.color(0xFFB4ADFF, 0xFFFFB566);
        int rim = MenuTheme.color(0xFF626E9D, 0xFF826044);
        int background = MenuTheme.color(0xFA171E30, 0xFA201C18);
        int well = MenuTheme.color(0xFF0E1525, 0xFF121311);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(0, 0, 400);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            Gui.drawRect(x + 3, y + 3, x + width + 3, y + height + 3, 0x70000000);
            Gui.drawRect(x, y + 3, x + width, y + height - 3, rim);
            Gui.drawRect(x + 3, y, x + width - 3, y + height, rim);
            Gui.drawRect(x + 1, y + 4, x + width - 1, y + height - 4, background);
            Gui.drawRect(x + 4, y + 1, x + width - 4, y + height - 1, background);
            Gui.drawRect(x + 7, y + 5, x + 29, y + 7, accent);
            Gui.drawRect(x + width - 18, y + 5, x + width - 7, y + 7,
                    MenuTheme.color(0xFF85CFE3, 0xFF6EAFA8));
            GL11.glPushMatrix();
            try {
                GL11.glTranslatef(x + 10, y + 11, 0);
                GL11.glScalef(TITLE_SCALE, TITLE_SCALE, 1);
                int titleY = 0;
                for (String line : title) {
                    font.drawStringWithShadow(line, 0, titleY, 0xFFF0E9E2);
                    titleY += lineHeight;
                }
            } finally {
                GL11.glPopMatrix();
            }
            if (!body.isEmpty()) {
                Gui.drawRect(x + 7, y + headerHeight, x + width - 7, y + height - 7, well);
                Gui.drawRect(x + 10, y + headerHeight, x + width - 10, y + headerHeight + 1, rim);
                int ty = y + headerHeight + 5;
                for (int i = first; i < first + count; i++) {
                    font.drawStringWithShadow(body.get(i), x + 10, ty, 0xFFDFE3E8); ty += lineHeight;
                }
            }
            if (pages > 1) font.drawString(I18n.format("htgt6.tooltip.pages", page + 1, pages),
                    x + 10, y + height - 14, accent, false);
        } finally {
            GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }

    private static String clean(String line) { return line == null ? "" : line.replace("\u00a7h", ""); }
    private static List<String> wrap(FontRenderer font, String line, int width) {
        return font.listFormattedStringToWidth(clean(line), Math.max(8, width));
    }
}
