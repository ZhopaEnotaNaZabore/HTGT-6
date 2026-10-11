package com.mod.htgt6.client.menu;

import com.mod.htgt6.common.ui.MenuTheme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiOptionSlider;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.ScreenChatOptions;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * Responsive HiTech6 root options screen. Child screens stay vanilla so
 * Angelica can continue replacing GuiVideoSettings with its own full UI.
 */
public final class GuiHTGT6Options extends GuiScreen {

    private static final int GRAPHICS = 101;
    private static final int SOUNDS = 106;
    private static final int CONTROLS = 100;
    private static final int LANGUAGE = 102;
    private static final int CHAT = 103;
    private static final int RESOURCE_PACKS = 105;
    private static final int DIFFICULTY = 108;
    private static final int BACK = 200;
    private static final int APPEARANCE = 201;

    private final GuiScreen parent;
    private final GameSettings settings;

    public GuiHTGT6Options(GuiScreen parent, GameSettings settings) {
        this.parent = parent;
        this.settings = settings;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(false);

        int margin = clamp(this.width / 48, 8, 24);
        int backHeight = clamp(this.height / 14, 24, 34);
        int backWidth = clamp((int) (this.width * 0.30F), 154, 286);
        backWidth = Math.min(backWidth, this.width - margin * 2);
        this.buttonList.add(new BackButton(BACK, margin, margin, backWidth, backHeight,
                I18n.format("htgt6.options.back")));

        int columnGap = clamp(this.width / 28, 10, 34);
        int sideMargin = clamp(this.width / 16, 14, 52);
        int cellWidth = Math.min(310, (this.width - sideMargin * 2 - columnGap) / 2);
        cellWidth = Math.max(112, cellWidth);
        int gridWidth = cellWidth * 2 + columnGap;
        int left = (this.width - gridWidth) / 2;

        int gridTop = Math.max(margin + backHeight + 18, (int) (this.height * 0.40F));
        int bottomMargin = clamp(this.height / 18, 10, 28);
        int available = Math.max(80, this.height - gridTop - bottomMargin);
        int rowGap = clamp(available / 30, 4, 12);
        int cellHeight = clamp((available - rowGap * 3) / 4, 20, 36);
        int usedHeight = cellHeight * 4 + rowGap * 3;
        if (gridTop + usedHeight > this.height - 4) {
            gridTop = Math.max(margin + backHeight + 8, this.height - usedHeight - 4);
        }

        addCategory(GRAPHICS, left, gridTop, cellWidth, cellHeight,
                I18n.format("htgt6.options.graphics"));
        addCategory(SOUNDS, left + cellWidth + columnGap, gridTop, cellWidth, cellHeight,
                I18n.format("htgt6.options.sounds"));
        addCategory(CONTROLS, left, gridTop + (cellHeight + rowGap), cellWidth, cellHeight,
                I18n.format("htgt6.options.controls"));
        addCategory(LANGUAGE, left + cellWidth + columnGap,
                gridTop + (cellHeight + rowGap), cellWidth, cellHeight,
                I18n.format("htgt6.options.language"));
        addCategory(CHAT, left, gridTop + (cellHeight + rowGap) * 2, cellWidth, cellHeight,
                I18n.format("htgt6.options.chat"));
        addCategory(RESOURCE_PACKS, left + cellWidth + columnGap,
                gridTop + (cellHeight + rowGap) * 2, cellWidth, cellHeight,
                I18n.format("htgt6.options.resources"));
        addCategory(DIFFICULTY, left, gridTop + (cellHeight + rowGap) * 3,
                cellWidth, cellHeight, this.settings.getKeyBinding(GameSettings.Options.DIFFICULTY));

        MoonOptionSlider fov = new MoonOptionSlider(left + cellWidth + columnGap,
                gridTop + (cellHeight + rowGap) * 3, cellWidth, cellHeight,
                GameSettings.Options.FOV);
        this.buttonList.add(fov);
        int themeWidth = Math.min(132, this.width - backWidth - margin * 3);
        this.buttonList.add(new MoonButton(APPEARANCE, this.width - margin - themeWidth,
                margin, themeWidth, backHeight, I18n.format("htgt6.theme.settings")));
    }

    private void addCategory(int id, int x, int y, int width, int height, String label) {
        this.buttonList.add(new MoonButton(id, x, y, width, height, label));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        switch (button.id) {
            case APPEARANCE:
                saveAndOpen(new GuiHiTechAppearance(this));
                break;
            case GRAPHICS:
                saveAndOpen(new GuiVideoSettings(this, this.settings));
                break;
            case SOUNDS:
                saveAndOpen(new GuiScreenOptionsSounds(this, this.settings));
                break;
            case CONTROLS:
                saveAndOpen(new GuiControls(this, this.settings));
                break;
            case LANGUAGE:
                saveAndOpen(new GuiLanguage(this, this.settings, this.mc.getLanguageManager()));
                break;
            case CHAT:
                saveAndOpen(new ScreenChatOptions(this, this.settings));
                break;
            case RESOURCE_PACKS:
                saveAndOpen(new GuiScreenResourcePacks(this));
                break;
            case DIFFICULTY:
                this.settings.setOptionValue(GameSettings.Options.DIFFICULTY, 1);
                button.displayString = this.settings.getKeyBinding(GameSettings.Options.DIFFICULTY);
                break;
            case BACK:
                closeToParent();
                break;
            default:
                break;
        }
    }

    private void saveAndOpen(GuiScreen screen) {
        this.settings.saveOptions();
        this.mc.displayGuiScreen(screen);
    }

    private void closeToParent() {
        this.settings.saveOptions();
        this.mc.displayGuiScreen(this.parent);
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) closeToParent();
    }

    @Override
    public void onGuiClosed() {
        this.settings.saveOptions();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawThemedBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawThemedBackground() {
        drawGradientRect(0, 0, this.width, this.height, MenuTheme.color(0xFF10121A, 0xFF101113), MenuTheme.color(0xFF20222D, 0xFF222326));
        drawGradientRect(0, this.height / 3, this.width, this.height,
                MenuTheme.color(0x0020222D, 0x00222326), MenuTheme.color(0xC010121A, 0xC0101113));

        int spacing = clamp(Math.min(this.width, this.height) / 7, 28, 58);
        int line = MenuTheme.color(0x18333447, 0x1849321B);
        for (int x = -this.height; x < this.width + this.height; x += spacing) {
            drawLine(x, 0, x + this.height, this.height, line);
        }
        for (int x = 0; x < this.width + this.height * 2; x += spacing * 2) {
            drawLine(x, 0, x - this.height, this.height, MenuTheme.color(0x10333447, 0x1049321B));
        }

        int centerX = this.width / 2;
        int centerY = this.height / 3;
        int radius = clamp(Math.min(this.width, this.height) / 3, 55, 150);
        drawDiamond(centerX, centerY, radius, MenuTheme.color(0x1E656B89, 0x1E77736D));
        drawDiamond(centerX, centerY, radius / 2, MenuTheme.color(0x27656B89, 0x2777736D));
        drawRect(0, this.height - 2, this.width, this.height, MenuTheme.color(0x66656B89, 0x6677736D));
    }

    private static void drawDiamond(int centerX, int centerY, int radius, int color) {
        drawLine(centerX, centerY - radius, centerX + radius, centerY, color);
        drawLine(centerX + radius, centerY, centerX, centerY + radius, color);
        drawLine(centerX, centerY + radius, centerX - radius, centerY, color);
        drawLine(centerX - radius, centerY, centerX, centerY - radius, color);
    }

    private static class MoonButton extends GuiButton {

        MoonButton(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.visible) return;
            boolean hovered = contains(mouseX, mouseY);
            this.field_146123_n = hovered;
            drawPanel(this.xPosition, this.yPosition, this.width, this.height, hovered);
            if (hovered) drawWings(this.xPosition, this.xPosition + this.width,
                    this.yPosition, this.yPosition + this.height);
            drawCenteredString(minecraft.fontRenderer, this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + (this.height - 8) / 2,
                    this.enabled ? MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5) : MenuTheme.color(0xFF656B89, 0xFF77736D));
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
        }
    }

    private static final class MoonOptionSlider extends GuiOptionSlider {

        MoonOptionSlider(int x, int y, int width, int height, GameSettings.Options option) {
            super(option.returnEnumOrdinal(), x, y, option);
            this.width = width;
            this.height = height;
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.visible) return;
            boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
            this.field_146123_n = hovered;
            mouseDragged(minecraft, mouseX, mouseY);
            drawPanel(this.xPosition, this.yPosition, this.width, this.height, hovered);

            float normalized = GameSettings.Options.FOV.normalizeValue(
                    minecraft.gameSettings.fovSetting);
            int trackLeft = this.xPosition + 8;
            int trackRight = this.xPosition + this.width - 8;
            int trackY = this.yPosition + this.height - 5;
            int thumb = trackLeft + (int) ((trackRight - trackLeft) * normalized);
            drawRect(trackLeft, trackY, trackRight, trackY + 1, MenuTheme.color(0x66656B89, 0x6677736D));
            drawRect(trackLeft, trackY, thumb, trackY + 2, MenuTheme.color(0xD1B3ABEF, 0xD1FFAD38));
            drawRect(thumb - 1, trackY - 2, thumb + 2, trackY + 4, MenuTheme.color(0xFFB3ABEF, 0xFFFFAD38));
            if (hovered) drawWings(this.xPosition, this.xPosition + this.width,
                    this.yPosition, this.yPosition + this.height);
            drawCenteredString(minecraft.fontRenderer, this.displayString,
                    this.xPosition + this.width / 2,
                    this.yPosition + Math.max(3, (this.height - 12) / 2), MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5));
        }
    }

    private static final class BackButton extends MoonButton {

        BackButton(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.visible) return;
            boolean hovered = contains(mouseX, mouseY);
            this.field_146123_n = hovered;
            int iconSize = this.height;
            drawPanel(this.xPosition, this.yPosition, iconSize, this.height, hovered);
            drawPanel(this.xPosition + iconSize + 4, this.yPosition,
                    this.width - iconSize - 4, this.height, hovered);
            int middleY = this.yPosition + this.height / 2;
            int arrowLeft = this.xPosition + iconSize / 3;
            int arrowRight = this.xPosition + iconSize * 2 / 3;
            int arrowColor = hovered ? 0xFFFFFFFF : MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5);
            drawLine(arrowLeft, middleY, arrowRight, this.yPosition + this.height / 3, arrowColor);
            drawLine(arrowLeft, middleY, arrowRight, this.yPosition + this.height * 2 / 3, arrowColor);
            drawLine(arrowLeft, middleY, this.xPosition + iconSize * 3 / 4, middleY, arrowColor);
            drawCenteredString(minecraft.fontRenderer, this.displayString,
                    this.xPosition + iconSize + 4 + (this.width - iconSize - 4) / 2,
                    this.yPosition + (this.height - 8) / 2, MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5));
        }
    }

    private static void drawPanel(int x, int y, int width, int height, boolean hovered) {
        int middle = y + height / 2;
        drawRect(x, y, x + width, middle,
                hovered ? MenuTheme.color(0xD5333447, 0xD549321B) : MenuTheme.color(0xB420222D, 0xB4222326));
        drawRect(x, middle, x + width, y + height,
                hovered ? MenuTheme.color(0xDA20222D, 0xDA222326) : MenuTheme.color(0xC810121A, 0xC8101113));
        drawFrame(x, y, x + width, y + height,
                hovered ? MenuTheme.color(0xFFB3ABEF, 0xFFFFAD38) : MenuTheme.color(0xCC9DA3CA, 0xCCB8AA96));
        drawFrame(x + 2, y + 2, x + width - 2, y + height - 2,
                hovered ? MenuTheme.color(0xBAB3ABEF, 0xBAFFAD38) : MenuTheme.color(0x55656B89, 0x5577736D));
    }

    private static void drawWings(int left, int right, int top, int bottom) {
        int middle = (top + bottom) / 2;
        int wing = clamp((bottom - top) + 10, 30, 48);
        drawLine(left - 3, top + 1, left - wing / 2, middle, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
        drawLine(left - wing / 2, middle, left - 3, bottom - 1, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
        drawLine(left - 8, top - 2, left - wing, middle, MenuTheme.color(0xB89DA3CA, 0xB8B8AA96));
        drawLine(left - wing, middle, left - 8, bottom + 2, MenuTheme.color(0xB89DA3CA, 0xB8B8AA96));
        drawLine(right + 3, top + 1, right + wing / 2, middle, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
        drawLine(right + wing / 2, middle, right + 3, bottom - 1, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
        drawLine(right + 8, top - 2, right + wing, middle, MenuTheme.color(0xB89DA3CA, 0xB8B8AA96));
        drawLine(right + wing, middle, right + 8, bottom + 2, MenuTheme.color(0xB89DA3CA, 0xB8B8AA96));
    }

    private static void drawFrame(int left, int top, int right, int bottom, int color) {
        drawRect(left, top, right, top + 1, color);
        drawRect(left, bottom - 1, right, bottom, color);
        drawRect(left, top, left + 1, bottom, color);
        drawRect(right - 1, top, right, bottom, color);
    }

    private static void drawLine(int x0, int y0, int x1, int y1, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2i(x0, y0);
        GL11.glVertex2i(x1, y1);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
