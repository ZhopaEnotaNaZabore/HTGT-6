package com.mod.htgt6.client.menu;

import com.mod.htgt6.common.ui.MenuTheme;

import java.awt.Desktop;
import java.net.URI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Responsive main menu backed exclusively by resources inside the HTGT6 jar. */
public final class GuiHTGT6MainMenu extends GuiScreen implements GuiYesNoCallback {

    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            "htgt6", "textures/gui/mainmenu/background.png");
    private static final ResourceLocation INDUSTRIAL_BACKGROUND = new ResourceLocation(
            "htgt6", "textures/gui/mainmenu/background_industrial.jpg");
    private static final String PROJECT_URL = "https://goodminecraft.com/vote/";
    private static final String DISCORD_URL = "https://discord.com/invite/h5VuaW5";

    private static final int SINGLEPLAYER = 1;
    private static final int MULTIPLAYER = 2;
    private static final int OPTIONS = 3;
    private static final int HELP = 4;
    private static final int SHUTDOWN = 5;
    private static final int WEBSITE = 10;
    private static final int DISCORD = 11;

    private URI pendingUri;
    private int menuCenterX;
    private int menuTop;
    private int menuWidth;
    private int buttonHeight;
    private int gap;

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.menuWidth = clamp((int) (this.width * 0.285F), 164, 286);
        this.buttonHeight = clamp(this.height / 20, 20, 28);
        this.gap = clamp(this.height / 90, 4, 9);
        int wingRoom = clamp(this.width / 28, 22, 42);
        this.menuCenterX = clamp((int) (this.width * 0.765F),
                this.menuWidth / 2 + wingRoom + 8,
                this.width - this.menuWidth / 2 - wingRoom - 8);

        int linksSize = clamp(this.height / 14, 24, 36);
        int menuBlockHeight = this.buttonHeight * 5 + this.gap * 4;
        int desiredTop = (int) (this.height * 0.335F);
        int maximumTop = this.height - menuBlockHeight - linksSize - this.gap * 3 - 10;
        this.menuTop = clamp(desiredTop, 48, Math.max(48, maximumTop));

        addMenuButton(SINGLEPLAYER, 0, I18n.format("htgt6.menu.singleplayer"));
        addMenuButton(MULTIPLAYER, 1, I18n.format("htgt6.menu.multiplayer"));
        addMenuButton(OPTIONS, 2, I18n.format("htgt6.menu.settings"));
        addMenuButton(HELP, 3, I18n.format("htgt6.menu.help"));
        addMenuButton(SHUTDOWN, 4, I18n.format("htgt6.menu.shutdown"));

        int linksY = this.menuTop + menuBlockHeight + this.gap * 2;
        int linkGap = clamp(this.width / 80, 8, 16);
        this.buttonList.add(new LinkIconButton(WEBSITE,
                this.menuCenterX - linkGap / 2 - linksSize, linksY, linksSize, LinkIcon.WEBSITE));
        this.buttonList.add(new LinkIconButton(DISCORD,
                this.menuCenterX + linkGap / 2, linksY, linksSize, LinkIcon.DISCORD));
    }

    private void addMenuButton(int id, int row, String label) {
        int x = this.menuCenterX - this.menuWidth / 2;
        int y = this.menuTop + row * (this.buttonHeight + this.gap);
        this.buttonList.add(new MoonButton(id, x, y, this.menuWidth, this.buttonHeight, label));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        switch (button.id) {
            case SINGLEPLAYER:
                this.mc.displayGuiScreen(new GuiSelectWorld(this));
                break;
            case MULTIPLAYER:
                this.mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case OPTIONS:
                this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                break;
            case HELP:
                openInGameWiki();
                break;
            case SHUTDOWN:
                this.mc.shutdown();
                break;
            case WEBSITE:
                requestOpenLink(PROJECT_URL, WEBSITE);
                break;
            case DISCORD:
                requestOpenLink(DISCORD_URL, DISCORD);
                break;
            default:
                break;
        }
    }

    /** Opens the bundled in-game wiki. Never opens an external URL. */
    private void openInGameWiki() {
        this.mc.displayGuiScreen(new GuiHTGT6Wiki(this));
    }

    private void requestOpenLink(String value, int id) {
        try {
            this.pendingUri = new URI(value);
            this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, value, id, true));
        } catch (Exception ignored) {
            this.pendingUri = null;
        }
    }

    @Override
    public void confirmClicked(boolean accepted, int id) {
        if (accepted && this.pendingUri != null) {
            try {
                Desktop.getDesktop().browse(this.pendingUri);
            } catch (Exception ignored) { }
        }
        this.pendingUri = null;
        this.mc.displayGuiScreen(this);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawCoverBackground();
        drawGradientRect(this.width / 2, 0, this.width, this.height,
                MenuTheme.color(0x08030A18, 0x08101113), MenuTheme.color(0x4A020817, 0x4A101113));

        int titleY = Math.max(14, this.menuTop - clamp(this.height / 8, 30, 68));
        float titleScale = clampFloat(this.menuWidth / 112.0F, 1.45F, 2.45F);
        drawScaledCentered("HiTech 6", this.menuCenterX, titleY, titleScale, MenuTheme.color(0xFFF4F1FF, 0xFFF3EEE5));
        drawMoonDivider(this.menuCenterX, titleY + (int) (13 * titleScale),
                Math.min(this.menuWidth, 230));

        super.drawScreen(mouseX, mouseY, partialTicks);

        int signatureX = clamp(this.width / 45, 8, 24);
        int signatureY = this.height - clamp(this.height / 12, 28, 50);
        int signatureWidth = Math.max(
                this.fontRendererObj.getStringWidth("Owned By GYMMC TEAM"),
                this.fontRendererObj.getStringWidth("HiTech 6 V 1.0.4")) + 12;
        drawGradientRect(signatureX - 4, signatureY - 5,
                signatureX + signatureWidth, signatureY + 22,
                MenuTheme.color(0xA0081024, 0xA0101113), MenuTheme.color(0xB0020713, 0xB0101113));
        drawFrame(signatureX - 4, signatureY - 5,
                signatureX + signatureWidth, signatureY + 22, MenuTheme.color(0xA8B9C9FF, 0xA8B8AA96));
        this.fontRendererObj.drawStringWithShadow("Owned By GYMMC TEAM",
                signatureX, signatureY, MenuTheme.color(0xFFE7EDFF, 0xFFF3EEE5));
        this.fontRendererObj.drawStringWithShadow("HiTech 6 V 1.0.4",
                signatureX, signatureY + 11, 0xFFFFFFFF);

        drawLinkTooltip(mouseX, mouseY);
    }

    private void drawLinkTooltip(int mouseX, int mouseY) {
        for (Object entry : this.buttonList) {
            if (!(entry instanceof LinkIconButton)) continue;
            LinkIconButton button = (LinkIconButton) entry;
            if (!button.contains(mouseX, mouseY)) continue;
            String text = I18n.format(button.icon == LinkIcon.WEBSITE
                    ? "htgt6.menu.website" : "htgt6.menu.discord");
            int textWidth = this.fontRendererObj.getStringWidth(text) + 8;
            int x = clamp(mouseX + 10, 2, this.width - textWidth - 2);
            int y = clamp(mouseY - 13, 2, this.height - 14);
            drawRect(x, y, x + textWidth, y + 13, MenuTheme.color(0xD0050A18, 0xD0101113));
            this.fontRendererObj.drawStringWithShadow(text, x + 4, y + 3, 0xFFFFFFFF);
            break;
        }
    }

    private void drawCoverBackground() {
        this.mc.getTextureManager().bindTexture(MenuTheme.isIndustrial() ? INDUSTRIAL_BACKGROUND : BACKGROUND);
        float screenAspect = (float) this.width / Math.max(1, this.height);
        float imageAspect = 1.5F;
        float u0 = 0.0F, u1 = 1.0F, v0 = 0.0F, v1 = 1.0F;
        if (screenAspect > imageAspect) {
            float visible = imageAspect / screenAspect;
            v0 = (1.0F - visible) * 0.5F;
            v1 = 1.0F - v0;
        } else if (screenAspect < imageAspect) {
            float visible = screenAspect / imageAspect;
            u0 = (1.0F - visible) * 0.5F;
            u1 = 1.0F - u0;
        }

        GL11.glColor4f(1, 1, 1, 1);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0, this.height, this.zLevel, u0, v1);
        tessellator.addVertexWithUV(this.width, this.height, this.zLevel, u1, v1);
        tessellator.addVertexWithUV(this.width, 0, this.zLevel, u1, v0);
        tessellator.addVertexWithUV(0, 0, this.zLevel, u0, v0);
        tessellator.draw();
    }

    private void drawScaledCentered(String text, int centerX, int y, float scale, int color) {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1.0F);
        int scaledX = (int) (centerX / scale);
        int scaledY = (int) (y / scale);
        drawCenteredString(this.fontRendererObj, text, scaledX, scaledY, color);
        GL11.glPopMatrix();
    }

    private static void drawMoonDivider(int centerX, int y, int width) {
        int left = centerX - width / 2;
        int right = centerX + width / 2;
        drawRect(left, y, centerX - 8, y + 1, MenuTheme.color(0x7AB8C8FF, 0x7AB8AA96));
        drawRect(centerX + 8, y, right, y + 1, MenuTheme.color(0x7AB8C8FF, 0x7AB8AA96));
        drawRect(centerX - 2, y - 2, centerX + 2, y + 3, MenuTheme.color(0xDDECE9FF, 0xDDF3EEE5));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float clampFloat(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static void drawFrame(int left, int top, int right, int bottom, int color) {
        drawRect(left, top, right, top + 1, color);
        drawRect(left, bottom - 1, right, bottom, color);
        drawRect(left, top, left + 1, bottom, color);
        drawRect(right - 1, top, right, bottom, color);
    }

    private static final class MoonButton extends GuiButton {

        MoonButton(int id, int x, int y, int width, int height, String text) {
            super(id, x, y, width, height, text);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.visible) return;
            boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
            this.field_146123_n = hovered;

            int top = this.yPosition;
            int bottom = top + this.height;
            int left = this.xPosition;
            int right = left + this.width;
            drawGradientRect(left, top, right, bottom,
                    hovered ? MenuTheme.color(0xD3272762, 0xD349321B) : MenuTheme.color(0xA8081737, 0xA8222326),
                    hovered ? MenuTheme.color(0xD10A1130, 0xD1222326) : MenuTheme.color(0xB2050A1B, 0xB2101113));
            drawFrame(left, top, right, bottom,
                    hovered ? MenuTheme.color(0xFFF1EFFF, 0xFFFFAD38) : MenuTheme.color(0xC4A9BAF5, 0xC4B8AA96));
            drawFrame(left + 2, top + 2, right - 2, bottom - 2,
                    hovered ? MenuTheme.color(0xB9828CFF, 0xB9FFAD38) : MenuTheme.color(0x553E5591, 0x5577736D));

            if (hovered) drawWings(left, right, top, bottom);
            String label = hovered ? "-<<" + this.displayString + ">>-" : this.displayString;
            int color = this.enabled ? MenuTheme.color(0xFFF7F6FF, 0xFFF3EEE5) : 0xFF777777;
            drawCenteredString(minecraft.fontRenderer,
                    label, left + this.width / 2,
                    top + (this.height - 8) / 2, color);
        }

        private static void drawWings(int left, int right, int top, int bottom) {
            int middle = (top + bottom) / 2;
            int wing = clamp((bottom - top) + 8, 28, 42);
            drawChevron(left - 3, left - wing / 2, top + 1, middle, bottom - 1, MenuTheme.color(0xEEE6E4FF, 0xEEF3EEE5));
            drawChevron(left - 8, left - wing, top - 2, middle, bottom + 2, MenuTheme.color(0xB5838FFF, 0xB577736D));
            drawChevron(right + 3, right + wing / 2, top + 1, middle, bottom - 1, MenuTheme.color(0xEEE6E4FF, 0xEEF3EEE5));
            drawChevron(right + 8, right + wing, top - 2, middle, bottom + 2, MenuTheme.color(0xB5838FFF, 0xB577736D));
            drawRect(left - 2, middle - 1, left, middle + 2, MenuTheme.color(0xFFF6F1FF, 0xFFF3EEE5));
            drawRect(right, middle - 1, right + 2, middle + 2, MenuTheme.color(0xFFF6F1FF, 0xFFF3EEE5));
        }

        private static void drawChevron(int innerX, int outerX, int top,
                int middle, int bottom, int color) {
            drawLine(innerX, top, outerX, middle, color);
            drawLine(outerX, middle, innerX, bottom, color);
        }
    }

    private enum LinkIcon { WEBSITE, DISCORD }

    private static final class LinkIconButton extends GuiButton {
        private final LinkIcon icon;

        LinkIconButton(int id, int x, int y, int size, LinkIcon icon) {
            super(id, x, y, size, size, "");
            this.icon = icon;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= this.xPosition && mouseY >= this.yPosition
                    && mouseX < this.xPosition + this.width
                    && mouseY < this.yPosition + this.height;
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.visible) return;
            boolean hovered = contains(mouseX, mouseY);
            this.field_146123_n = hovered;
            int left = this.xPosition, top = this.yPosition;
            int right = left + this.width, bottom = top + this.height;
            drawGradientRect(left, top, right, bottom,
                    hovered ? MenuTheme.color(0xDD30306C, 0xDD49321B) : MenuTheme.color(0xBB081632, 0xBB222326),
                    hovered ? MenuTheme.color(0xDD111539, 0xDD222326) : MenuTheme.color(0xCC050A1A, 0xCC101113));
            drawFrame(left, top, right, bottom,
                    hovered ? MenuTheme.color(0xFFF2EEFF, 0xFFF3EEE5) : MenuTheme.color(0xD2AFC1FF, 0xD2B8AA96));
            if (this.icon == LinkIcon.WEBSITE) drawGlobe(left, top, this.width, hovered);
            else drawDiscord(left, top, this.width, hovered);
        }
    }

    private static void drawGlobe(int left, int top, int size, boolean hovered) {
        int color = hovered ? MenuTheme.color(0xFFF5F1FF, 0xFFF3EEE5) : MenuTheme.color(0xFFE2E8FF, 0xFFF3EEE5);
        float cx = left + size * 0.5F, cy = top + size * 0.5F;
        float radius = size * 0.29F;
        drawCircle(cx, cy, radius, color);
        drawCircle(cx, cy, radius * 0.52F, color);
        drawLine((int) (cx - radius), (int) cy, (int) (cx + radius), (int) cy, color);
        drawLine((int) cx, (int) (cy - radius), (int) cx, (int) (cy + radius), color);
    }

    private static void drawDiscord(int left, int top, int size, boolean hovered) {
        int color = hovered ? MenuTheme.color(0xFFF6F2FF, 0xFFF3EEE5) : MenuTheme.color(0xFFE0E4FF, 0xFFF3EEE5);
        int dark = MenuTheme.color(0xFF31345F, 0xFF49321B);
        int x0 = left + size / 4, x1 = left + size * 3 / 4;
        int y0 = top + size / 3, y1 = top + size * 2 / 3;
        drawRect(x0, y0, x1, y1, color);
        drawRect(x0 - 2, y0 + 3, x0 + 2, y1 - 2, color);
        drawRect(x1 - 2, y0 + 3, x1 + 2, y1 - 2, color);
        int eye = Math.max(2, size / 12);
        drawRect(left + size * 3 / 8 - eye / 2, top + size / 2 - eye / 2,
                left + size * 3 / 8 + eye, top + size / 2 + eye, dark);
        drawRect(left + size * 5 / 8 - eye / 2, top + size / 2 - eye / 2,
                left + size * 5 / 8 + eye, top + size / 2 + eye, dark);
        drawLine(left + size * 3 / 8, top + size * 5 / 8,
                left + size / 2, top + size * 2 / 3, dark);
        drawLine(left + size / 2, top + size * 2 / 3,
                left + size * 5 / 8, top + size * 5 / 8, dark);
    }

    private static void drawCircle(float centerX, float centerY, float radius, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < 24; i++) {
            double angle = Math.PI * 2.0D * i / 24.0D;
            GL11.glVertex2d(centerX + Math.cos(angle) * radius,
                    centerY + Math.sin(angle) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1, 1, 1, 1);
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
}
