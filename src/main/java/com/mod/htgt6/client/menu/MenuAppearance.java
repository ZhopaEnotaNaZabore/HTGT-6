package com.mod.htgt6.client.menu;

import com.mod.htgt6.common.ui.MenuTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

/** Shared palette for the menu screens; does not alter inventories or world rendering. */
public final class MenuAppearance extends Gui {
    private MenuAppearance() { }

    public static void background(int width, int height) {
        drawRect(0, 0, width, height, MenuTheme.color(0xFF10121A, 0xFF101113));
        drawRect(0, height / 3, width, height, MenuTheme.color(0xFF191C27, 0xFF191A1D));
        drawRect(0, height - 2, width, height, accent());
    }

    public static int accent() { return MenuTheme.color(0xFFABA1EC, 0xFFFFAD38); }
    public static int panel() { return MenuTheme.color(0xE020222D, 0xE0222326); }
    public static int selected() { return MenuTheme.color(0xE03A3655, 0xE049321B); }

    /** Cover GuiSlot's private dirt-texture header/footer after the list has drawn. */
    public static void listChrome(int width, int height, int top, int bottom) {
        int color = MenuTheme.color(0xFF10121A, 0xFF101113);
        drawRect(0, 0, width, top, color);
        drawRect(0, bottom, width, height, color);
        drawRect(0, top - 1, width, top, accent());
        drawRect(0, bottom, width, bottom + 1, accent());
    }

    public static void drawButton(GuiButton button, Minecraft mc, int mouseX, int mouseY) {
        if (!button.visible) return;
        boolean hovered = button.enabled && mouseX >= button.xPosition && mouseY >= button.yPosition
                && mouseX < button.xPosition + button.width && mouseY < button.yPosition + button.height;
        int x = button.xPosition, y = button.yPosition, right = x + button.width, bottom = y + button.height;
        int edge = hovered ? accent() : MenuTheme.color(0xFF656B89, 0xFF77736D);
        drawRect(x, y, right, bottom, edge);
        drawRect(x + 1, y + 1, right - 1, bottom - 1, hovered ? selected() | 0xFF000000 : panel() | 0xFF000000);
        drawRect(x + 2, y + button.height / 2, right - 2, bottom - 2, MenuTheme.color(0xFF10121A, 0xFF101113));
        int text = !button.enabled ? 0xFF777777 : hovered ? accent() : 0xFFF1EFF5;
        mc.fontRenderer.drawStringWithShadow(button.displayString,
                x + (button.width - mc.fontRenderer.getStringWidth(button.displayString)) / 2,
                y + (button.height - 8) / 2, text);
    }

    public static final class Button extends GuiButton {
        public Button(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }
        @Override public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            MenuAppearance.drawButton(this, mc, mouseX, mouseY);
        }
    }
}
