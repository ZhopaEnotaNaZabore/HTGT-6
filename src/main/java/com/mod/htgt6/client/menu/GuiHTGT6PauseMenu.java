package com.mod.htgt6.client.menu;

import com.mod.htgt6.common.ui.MenuTheme;

import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiNewSpaceRace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.achievement.GuiAchievements;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/** Compact left-side pause menu using the HiTech6 settings-screen visual language. */
public final class GuiHTGT6PauseMenu extends GuiScreen {

    private static final int OPTIONS = 0;
    private static final int EXIT_GAME = 1;
    private static final int RETURN_TO_GAME = 4;
    private static final int HELP = 20;
    private static final int SPACE_RACE = 21;
    private static final int ACHIEVEMENTS = 22;
    private static final int BUTTON_COUNT = 6;

    private int panelWidth;
    private int panelCenterX;

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(false);

        this.panelWidth = clamp((int) (this.width * 0.38F), 180, 420);
        this.panelWidth = Math.min(this.panelWidth, this.width);
        this.panelCenterX = this.panelWidth / 2;

        int horizontalPadding = clamp(this.panelWidth / 10, 14, 34);
        int buttonWidth = this.panelWidth - horizontalPadding * 2;
        int buttonTop = this.height / 2;
        int bottomMargin = clamp(this.height / 30, 8, 20);
        int gap = clamp(this.height / 90, 4, 8);
        int availableHeight = Math.max(90, this.height - buttonTop - bottomMargin);
        int buttonHeight = clamp((availableHeight - gap * (BUTTON_COUNT - 1)) / BUTTON_COUNT, 18, 30);
        int blockHeight = buttonHeight * BUTTON_COUNT + gap * (BUTTON_COUNT - 1);
        if (buttonTop + blockHeight > this.height - 4) {
            buttonTop = Math.max(48, this.height - blockHeight - 4);
        }

        int x = this.panelCenterX - buttonWidth / 2;
        addButton(RETURN_TO_GAME, x, buttonTop, buttonWidth, buttonHeight,
                I18n.format("htgt6.pause.return"));
        addButton(HELP, x, buttonTop + (buttonHeight + gap), buttonWidth, buttonHeight,
                I18n.format("htgt6.pause.help"));
        addButton(ACHIEVEMENTS, x, buttonTop + (buttonHeight + gap) * 2, buttonWidth, buttonHeight,
                I18n.format("htgt6.pause.achievements"));
        addButton(OPTIONS, x, buttonTop + (buttonHeight + gap) * 3, buttonWidth, buttonHeight,
                I18n.format("htgt6.pause.options"));
        addButton(SPACE_RACE, x, buttonTop + (buttonHeight + gap) * 4, buttonWidth, buttonHeight,
                I18n.format("htgt6.pause.space_race"));
        addButton(EXIT_GAME, x, buttonTop + (buttonHeight + gap) * 5, buttonWidth, buttonHeight,
                I18n.format("htgt6.pause.exit"));
    }

    private void addButton(int id, int x, int y, int width, int height, String label) {
        this.buttonList.add(new MoonButton(id, x, y, width, height, label));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        switch (button.id) {
            case RETURN_TO_GAME:
                resumeGame();
                break;
            case HELP:
                this.mc.displayGuiScreen(new GuiHTGT6Wiki(this));
                break;
            case OPTIONS:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case ACHIEVEMENTS:
                if (this.mc.thePlayer != null) {
                    this.mc.displayGuiScreen(new GuiAchievements(this, this.mc.thePlayer.getStatFileWriter()));
                }
                break;
            case SPACE_RACE:
                if (this.mc.thePlayer != null) {
                    this.mc.displayGuiScreen(new GuiNewSpaceRace(this.mc.thePlayer));
                }
                break;
            case EXIT_GAME:
                button.enabled = false;
                if (this.mc.theWorld != null) {
                    this.mc.theWorld.sendQuittingDisconnectingPacket();
                }
                this.mc.loadWorld((WorldClient) null);
                this.mc.displayGuiScreen(new GuiMainMenu());
                break;
            default:
                break;
        }
    }

    private void resumeGame() {
        this.mc.displayGuiScreen(null);
        this.mc.setIngameFocus();
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) resumeGame();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawLeftPanel();

        float titleScale = clampFloat(this.panelWidth / 150.0F, 1.25F, 2.0F);
        int titleY = clamp(this.height / 5, 24, 92);
        drawScaledCentered(I18n.format("htgt6.pause.title"),
                this.panelCenterX, titleY, titleScale, MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5));
        drawDivider(this.panelCenterX, titleY + (int) (13 * titleScale),
                Math.min(this.panelWidth - 44, 220));

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawLeftPanel() {
        drawGradientRect(0, 0, this.panelWidth, this.height,
                MenuTheme.color(0xDD10121A, 0xDD101113), MenuTheme.color(0xEA20222D, 0xEA222326));
        drawRect(this.panelWidth - 2, 0, this.panelWidth, this.height, MenuTheme.color(0xAA656B89, 0xAA77736D));
        drawRect(this.panelWidth - 5, 0, this.panelWidth - 4, this.height, MenuTheme.color(0x44656B89, 0x4477736D));

        int inset = clamp(this.panelWidth / 32, 5, 12);
        drawFrame(inset, inset, this.panelWidth - inset, this.height - inset, MenuTheme.color(0x80656B89, 0x8077736D));
        int radius = clamp(Math.min(this.panelWidth, this.height) / 3, 48, 132);
        drawDiamond(this.panelCenterX, this.height / 2, radius, MenuTheme.color(0x24656B89, 0x2477736D));
        drawDiamond(this.panelCenterX, this.height / 2, radius / 2, MenuTheme.color(0x1C656B89, 0x1C77736D));
        drawLine(this.panelCenterX, inset, this.panelCenterX,
                Math.max(inset, this.height / 2 - radius), MenuTheme.color(0x35656B89, 0x3577736D));
        drawLine(this.panelCenterX, Math.min(this.height - inset, this.height / 2 + radius),
                this.panelCenterX, this.height - inset, MenuTheme.color(0x35656B89, 0x3577736D));
    }

    private void drawScaledCentered(String text, int centerX, int y, float scale, int color) {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);
        drawCenteredString(this.fontRendererObj, text,
                (int) (centerX / scale), (int) (y / scale), color);
        GL11.glPopMatrix();
    }

    private static void drawDivider(int centerX, int y, int width) {
        int left = centerX - width / 2;
        int right = centerX + width / 2;
        drawRect(left, y, centerX - 7, y + 1, MenuTheme.color(0x7A9DA3CA, 0x7AB8AA96));
        drawRect(centerX + 7, y, right, y + 1, MenuTheme.color(0x7A9DA3CA, 0x7AB8AA96));
        drawLine(centerX, y - 3, centerX + 3, y, MenuTheme.color(0xDDEBEAF5, 0xDDF3EEE5));
        drawLine(centerX + 3, y, centerX, y + 3, MenuTheme.color(0xDDEBEAF5, 0xDDF3EEE5));
        drawLine(centerX, y + 3, centerX - 3, y, MenuTheme.color(0xDDEBEAF5, 0xDDF3EEE5));
        drawLine(centerX - 3, y, centerX, y - 3, MenuTheme.color(0xDDEBEAF5, 0xDDF3EEE5));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private static final class MoonButton extends GuiButton {

        private MoonButton(int id, int x, int y, int width, int height, String label) {
            super(id, x, y, width, height, label);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.visible) return;
            boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
            this.field_146123_n = hovered;

            int left = this.xPosition;
            int top = this.yPosition;
            int right = left + this.width;
            int bottom = top + this.height;
            int middle = top + this.height / 2;
            drawRect(left, top, right, middle, hovered ? MenuTheme.color(0xE1333447, 0xE149321B) : MenuTheme.color(0xC720222D, 0xC7222326));
            drawRect(left, middle, right, bottom, hovered ? MenuTheme.color(0xE020222D, 0xE0222326) : MenuTheme.color(0xD010121A, 0xD0101113));
            drawFrame(left, top, right, bottom,
                    hovered ? MenuTheme.color(0xFFB3ABEF, 0xFFFFAD38) : MenuTheme.color(0xCC9DA3CA, 0xCCB8AA96));
            if (this.width > 5 && this.height > 5) {
                drawFrame(left + 2, top + 2, right - 2, bottom - 2,
                        hovered ? MenuTheme.color(0xBAB3ABEF, 0xBAFFAD38) : MenuTheme.color(0x55656B89, 0x5577736D));
            }
            if (hovered) drawWings(left, right, top, bottom);

            String label = fitText(minecraft, this.displayString, this.width - 18);
            drawCenteredString(minecraft.fontRenderer, label, left + this.width / 2,
                    top + (this.height - 8) / 2,
                    this.enabled ? MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5) : MenuTheme.color(0xFF656B89, 0xFF77736D));
        }

        private static String fitText(Minecraft minecraft, String text, int maximumWidth) {
            if (minecraft.fontRenderer.getStringWidth(text) <= maximumWidth) return text;
            String suffix = "...";
            return minecraft.fontRenderer.trimStringToWidth(text,
                    Math.max(1, maximumWidth - minecraft.fontRenderer.getStringWidth(suffix))) + suffix;
        }
    }

    private static void drawWings(int left, int right, int top, int bottom) {
        int middle = (top + bottom) / 2;
        int wing = clamp((bottom - top) + 8, 26, 42);
        drawLine(left - 3, top + 2, left - wing / 2, middle, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
        drawLine(left - wing / 2, middle, left - 3, bottom - 2, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
        drawLine(right + 3, top + 2, right + wing / 2, middle, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
        drawLine(right + wing / 2, middle, right + 3, bottom - 2, MenuTheme.color(0xEEEBEAF5, 0xEEF3EEE5));
    }

    private static void drawDiamond(int centerX, int centerY, int radius, int color) {
        drawLine(centerX, centerY - radius, centerX + radius, centerY, color);
        drawLine(centerX + radius, centerY, centerX, centerY + radius, color);
        drawLine(centerX, centerY + radius, centerX - radius, centerY, color);
        drawLine(centerX - radius, centerY, centerX, centerY - radius, color);
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

    private static float clampFloat(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
