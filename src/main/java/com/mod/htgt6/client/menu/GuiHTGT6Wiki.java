package com.mod.htgt6.client.menu;

import com.mod.htgt6.common.ui.MenuTheme;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/** Responsive, resource-backed HTGT6 in-game wiki. */
public final class GuiHTGT6Wiki extends GuiScreen {

    private static final int PAGE_MODULES = 0;
    private static final int PAGE_SECTIONS = 1;
    private static final int PAGE_ARTICLE = 2;

    private static final ResourceLocation FORGING_IMAGE = new ResourceLocation(
            "htgt6", "textures/gui/wiki/gregtech6_forging_anvil.png");
    private static final int FORGING_IMAGE_WIDTH = 498;
    private static final int FORGING_IMAGE_HEIGHT = 334;

    private static final String[] MODULE_KEYS = {
            "htgt6.wiki.module.gregtech6",
            "htgt6.wiki.module.ae2",
            "htgt6.wiki.module.ic2",
            "htgt6.wiki.module.galacticraft",
            "htgt6.wiki.module.forestry"
    };
    private static final String[] GREGTECH_SECTION_KEYS = {
            "htgt6.wiki.section.forging",
            "htgt6.wiki.section.materials",
            "htgt6.wiki.section.machines",
            "htgt6.wiki.section.energy",
            "htgt6.wiki.section.tools"
    };
    private static final String[] OVERVIEW_SECTION_KEYS = {
            "htgt6.wiki.section.overview"
    };
    private static final String[] FORGING_PARAGRAPH_KEYS = {
            "htgt6.wiki.article.forging.p1",
            "htgt6.wiki.article.forging.p2",
            "htgt6.wiki.article.forging.p3",
            "htgt6.wiki.article.forging.p4",
            "htgt6.wiki.article.forging.warning"
    };

    private final GuiScreen parent;

    private int margin;
    private int gap;
    private int backX;
    private int backY;
    private int backWidth;
    private int backHeight;
    private int workTop;
    private int workBottom;
    private int modulesX;
    private int modulesWidth;
    private int sectionsX;
    private int sectionsWidth;
    private int articleX;
    private int articleWidth;
    private int panelHeaderHeight;
    private int listTop;
    private int listBottom;
    private int rowHeight;
    private int rowGap;
    private int moduleVisibleCount;
    private int sectionVisibleCount;

    private int page = PAGE_MODULES;
    private int selectedModule = -1;
    private int selectedSection = -1;
    private int moduleOffset;
    private int sectionOffset;
    private int articleScroll;
    private int articleMaxScroll;

    public GuiHTGT6Wiki(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(false);
        layout();
    }

    private void layout() {
        this.margin = clamp(Math.min(this.width, this.height) / 48, 6, 18);
        this.gap = clamp(this.width / 160, 4, 10);
        this.backX = this.margin;
        this.backY = this.margin;
        this.backHeight = clamp(this.height / 14, 25, 38);
        this.backWidth = clamp((int) (this.width * 0.30F), 138, 300);
        this.backWidth = Math.min(this.backWidth, this.width - this.margin * 2);

        this.workTop = this.backY + this.backHeight + this.margin;
        this.workBottom = this.height - this.margin;
        int usableWidth = this.width - this.margin * 2;
        int catalogWidth = clamp((int) (usableWidth * 0.72F), 180, 720);
        catalogWidth = Math.min(catalogWidth, usableWidth);
        this.modulesWidth = catalogWidth;
        this.sectionsWidth = catalogWidth;
        this.articleWidth = usableWidth;
        this.modulesX = (this.width - this.modulesWidth) / 2;
        this.sectionsX = (this.width - this.sectionsWidth) / 2;
        this.articleX = this.margin;

        int panelHeight = Math.max(1, this.workBottom - this.workTop);
        this.panelHeaderHeight = clamp(panelHeight / 12, 20, 32);
        this.listTop = this.workTop + this.panelHeaderHeight + 7;
        this.listBottom = this.workBottom - 7;
        this.rowGap = clamp(panelHeight / 90, 3, 7);
        this.rowHeight = clamp(panelHeight / 8, 21, 38);
        this.moduleVisibleCount = Math.max(1,
                (this.listBottom - this.listTop + this.rowGap) / (this.rowHeight + this.rowGap));
        this.sectionVisibleCount = this.moduleVisibleCount;
        clampOffsets();
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) navigateBack();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;
        if (contains(mouseX, mouseY, this.backX, this.backY, this.backWidth, this.backHeight)) {
            navigateBack();
            return;
        }

        if (this.page == PAGE_MODULES && contains(mouseX, mouseY, this.modulesX, this.listTop,
                this.modulesWidth, this.listBottom - this.listTop)) {
            int index = listIndexAt(mouseY, this.moduleOffset);
            if (index >= 0 && index < MODULE_KEYS.length) {
                this.selectedModule = index;
                this.selectedSection = -1;
                this.sectionOffset = 0;
                this.articleScroll = 0;
                this.page = PAGE_SECTIONS;
                clampOffsets();
            }
            return;
        }

        if (this.page == PAGE_SECTIONS && contains(mouseX, mouseY, this.sectionsX, this.listTop,
                this.sectionsWidth, this.listBottom - this.listTop)) {
            String[] sections = currentSectionKeys();
            int index = listIndexAt(mouseY, this.sectionOffset);
            if (index >= 0 && index < sections.length) {
                this.selectedSection = index;
                this.articleScroll = 0;
                this.page = PAGE_ARTICLE;
            }
        }
    }

    private int listIndexAt(int mouseY, int offset) {
        int localY = mouseY - this.listTop;
        int stride = this.rowHeight + this.rowGap;
        int row = localY / stride;
        if (localY % stride >= this.rowHeight) return -1;
        return offset + row;
    }

    @Override
    public void handleMouseInput() {
        int wheel = Mouse.getEventDWheel();
        super.handleMouseInput();
        if (wheel == 0) return;

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int direction = wheel < 0 ? 1 : -1;
        if (this.page == PAGE_MODULES
                && mouseX >= this.modulesX && mouseX < this.modulesX + this.modulesWidth
                && mouseY >= this.workTop && mouseY < this.workBottom) {
            this.moduleOffset += direction;
        } else if (this.page == PAGE_SECTIONS
                && mouseX >= this.sectionsX && mouseX < this.sectionsX + this.sectionsWidth
                && mouseY >= this.workTop && mouseY < this.workBottom) {
            this.sectionOffset += direction;
        } else if (this.page == PAGE_ARTICLE
                && mouseX >= this.articleX && mouseX < this.articleX + this.articleWidth
                && mouseY >= this.workTop && mouseY < this.workBottom) {
            this.articleScroll = clamp(this.articleScroll + direction * 18,
                    0, this.articleMaxScroll);
        }
        clampOffsets();
    }

    private void clampOffsets() {
        this.moduleOffset = clamp(this.moduleOffset, 0,
                Math.max(0, MODULE_KEYS.length - this.moduleVisibleCount));
        if (this.selectedModule >= 0) {
            String[] sections = currentSectionKeys();
            this.sectionOffset = clamp(this.sectionOffset, 0,
                    Math.max(0, sections.length - this.sectionVisibleCount));
            if (this.selectedSection >= 0) {
                this.selectedSection = clamp(this.selectedSection, 0,
                        Math.max(0, sections.length - 1));
            }
        }
    }

    private String[] currentSectionKeys() {
        return this.selectedModule == 0 ? GREGTECH_SECTION_KEYS : OVERVIEW_SECTION_KEYS;
    }

    private void navigateBack() {
        if (this.page == PAGE_ARTICLE) {
            this.page = PAGE_SECTIONS;
            this.articleScroll = 0;
        } else if (this.page == PAGE_SECTIONS) {
            this.page = PAGE_MODULES;
            this.selectedSection = -1;
            this.sectionOffset = 0;
        } else {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawThemedBackground();
        drawBackControl(mouseX, mouseY);
        if (this.page == PAGE_MODULES) {
            drawListPanel(this.modulesX, this.modulesWidth, I18n.format("htgt6.wiki.modules"),
                    MODULE_KEYS, this.moduleOffset, this.moduleVisibleCount,
                    this.selectedModule, mouseX, mouseY);
        } else if (this.page == PAGE_SECTIONS) {
            drawListPanel(this.sectionsX, this.sectionsWidth,
                    I18n.format(MODULE_KEYS[this.selectedModule]), currentSectionKeys(),
                    this.sectionOffset, this.sectionVisibleCount,
                    this.selectedSection, mouseX, mouseY);
        } else {
            drawArticlePanel();
        }
    }

    private void drawBackControl(int mouseX, int mouseY) {
        boolean hovered = contains(mouseX, mouseY,
                this.backX, this.backY, this.backWidth, this.backHeight);
        int iconSize = this.backHeight;
        drawPanel(this.backX, this.backY, iconSize, this.backHeight, hovered, false);
        drawPanel(this.backX + iconSize + 4, this.backY,
                this.backWidth - iconSize - 4, this.backHeight, hovered, false);
        int middleY = this.backY + this.backHeight / 2;
        int arrowLeft = this.backX + iconSize / 3;
        int arrowRight = this.backX + iconSize * 2 / 3;
        int color = hovered ? 0xFFFFFFFF : MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5);
        drawLine(arrowLeft, middleY, arrowRight, this.backY + this.backHeight / 3, color);
        drawLine(arrowLeft, middleY, arrowRight, this.backY + this.backHeight * 2 / 3, color);
        drawLine(arrowLeft, middleY, this.backX + iconSize * 3 / 4, middleY, color);
        String backLabel = this.page == PAGE_MODULES
                ? I18n.format("htgt6.wiki.back")
                : this.page == PAGE_SECTIONS
                        ? I18n.format("htgt6.wiki.modules")
                        : I18n.format("htgt6.wiki.sections");
        drawCenteredString(this.fontRendererObj, backLabel,
                this.backX + iconSize + 4 + (this.backWidth - iconSize - 4) / 2,
                this.backY + (this.backHeight - 8) / 2, MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5));
    }

    private void drawListPanel(int x, int panelWidth, String heading, String[] entries,
            int offset, int visibleCount, int selected, int mouseX, int mouseY) {
        drawPanel(x, this.workTop, panelWidth,
                this.workBottom - this.workTop, false, true);
        drawCenteredString(this.fontRendererObj, heading, x + panelWidth / 2,
                this.workTop + (this.panelHeaderHeight - 8) / 2, MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5));
        drawDivider(x + 8, x + panelWidth - 8,
                this.workTop + this.panelHeaderHeight - 2);

        int end = Math.min(entries.length, offset + visibleCount);
        int rowWidth = Math.max(20, panelWidth - 22);
        for (int index = offset; index < end; index++) {
            int row = index - offset;
            int y = this.listTop + row * (this.rowHeight + this.rowGap);
            boolean hovered = contains(mouseX, mouseY, x + 7, y, rowWidth, this.rowHeight);
            boolean isSelected = index == selected;
            drawPanel(x + 7, y, rowWidth, this.rowHeight, hovered, isSelected);
            if (hovered || isSelected) drawCompactWings(x + 7,
                    x + 7 + rowWidth, y, y + this.rowHeight,
                    isSelected ? MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5) : MenuTheme.color(0xB89DA3CA, 0xB8B8AA96));
            String label = fitText(I18n.format(entries[index]), rowWidth - 18);
            drawCenteredString(this.fontRendererObj, label, x + 7 + rowWidth / 2,
                    y + (this.rowHeight - 8) / 2,
                    isSelected ? 0xFFFFFFFF : MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5));
        }
        drawScrollbar(x + panelWidth - 6, this.listTop,
                this.listBottom - this.listTop, entries.length, visibleCount, offset);
    }

    private void drawArticlePanel() {
        drawPanel(this.articleX, this.workTop, this.articleWidth,
                this.workBottom - this.workTop, false, true);
        String title = isForgingArticle()
                ? I18n.format("htgt6.wiki.article.forging.title")
                : I18n.format(currentSectionKeys()[this.selectedSection]);
        drawCenteredString(this.fontRendererObj, title,
                this.articleX + this.articleWidth / 2,
                this.workTop + (this.panelHeaderHeight - 8) / 2, MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5));
        drawDivider(this.articleX + 9, this.articleX + this.articleWidth - 9,
                this.workTop + this.panelHeaderHeight - 2);

        if (!isForgingArticle()) {
            this.articleMaxScroll = 0;
            drawCenteredString(this.fontRendererObj, I18n.format("htgt6.wiki.coming_soon"),
                    this.articleX + this.articleWidth / 2,
                    (this.workTop + this.panelHeaderHeight + this.workBottom) / 2,
                    MenuTheme.color(0xFF9DA3CA, 0xFFB8AA96));
            return;
        }

        int padding = clamp(this.articleWidth / 42, 6, 12);
        int contentX = this.articleX + padding;
        int contentWidth = this.articleWidth - padding * 2 - 6;
        int contentTop = this.workTop + this.panelHeaderHeight + 7;
        int contentBottom = this.workBottom - 7;
        int availableHeight = Math.max(1, contentBottom - contentTop);
        int imageHeight = Math.min(availableHeight * 47 / 100,
                contentWidth * FORGING_IMAGE_HEIGHT / FORGING_IMAGE_WIDTH);
        imageHeight = Math.max(34, imageHeight);
        int imageWidth = imageHeight * FORGING_IMAGE_WIDTH / FORGING_IMAGE_HEIGHT;
        imageWidth = Math.min(contentWidth, imageWidth);
        int imageX = contentX + (contentWidth - imageWidth) / 2;

        this.mc.getTextureManager().bindTexture(FORGING_IMAGE);
        drawTexturedQuad(imageX, contentTop, imageWidth, imageHeight);
        drawFrame(imageX - 1, contentTop - 1,
                imageX + imageWidth + 1, contentTop + imageHeight + 1, MenuTheme.color(0xCB9DA3CA, 0xCBB8AA96));
        drawCallouts(imageX, contentTop, imageWidth, imageHeight);

        int dividerY = contentTop + imageHeight + 8;
        drawDivider(contentX, contentX + contentWidth, dividerY);
        int textTop = dividerY + 7;
        int textHeight = Math.max(0, contentBottom - textTop);
        drawArticleText(contentX, textTop, contentWidth, textHeight);
        drawScrollbar(this.articleX + this.articleWidth - 6, textTop,
                textHeight, textHeight + this.articleMaxScroll,
                textHeight, this.articleScroll);
    }

    private boolean isForgingArticle() {
        return this.selectedModule == 0 && this.selectedSection == 0;
    }

    private void drawArticleText(int x, int top, int width, int height) {
        List<WikiLine> lines = new ArrayList<WikiLine>();
        int wrapWidth = Math.max(20, width - 4);
        for (int paragraph = 0; paragraph < FORGING_PARAGRAPH_KEYS.length; paragraph++) {
            int color = paragraph == FORGING_PARAGRAPH_KEYS.length - 1
                    ? 0xFFFFCE55 : MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5);
            List<?> wrapped = this.fontRendererObj.listFormattedStringToWidth(
                    I18n.format(FORGING_PARAGRAPH_KEYS[paragraph]), wrapWidth);
            for (Object line : wrapped) lines.add(new WikiLine(String.valueOf(line), color));
            if (paragraph + 1 < FORGING_PARAGRAPH_KEYS.length) lines.add(new WikiLine("", color));
        }

        int lineHeight = this.fontRendererObj.FONT_HEIGHT + 2;
        int totalHeight = lines.size() * lineHeight;
        this.articleMaxScroll = Math.max(0, totalHeight - height);
        this.articleScroll = clamp(this.articleScroll, 0, this.articleMaxScroll);
        int y = top - this.articleScroll;
        for (WikiLine line : lines) {
            if (y >= top && y + this.fontRendererObj.FONT_HEIGHT <= top + height) {
                this.fontRendererObj.drawStringWithShadow(line.text, x + 2, y, line.color);
            }
            y += lineHeight;
        }
    }

    private void drawCallouts(int x, int y, int width, int height) {
        if (width < 150 || height < 80) return;
        int white = MenuTheme.color(0xFFEBEAF5, 0xFFF3EEE5);
        String side1 = I18n.format("htgt6.wiki.article.side1");
        String side2 = I18n.format("htgt6.wiki.article.side2");
        String side3 = I18n.format("htgt6.wiki.article.side3");
        String mid = I18n.format("htgt6.wiki.article.mid");
        int centerY = y + height * 54 / 100;

        int side1X = x + 5;
        this.fontRendererObj.drawStringWithShadow(side1, side1X, centerY - 10, white);
        drawHorizontalArrow(side1X + this.fontRendererObj.getStringWidth(side1) + 4,
                x + width * 34 / 100, centerY - 5, true, white);

        int side3X = x + width - this.fontRendererObj.getStringWidth(side3) - 5;
        this.fontRendererObj.drawStringWithShadow(side3, side3X, centerY - 10, white);
        drawHorizontalArrow(side3X - 4, x + width * 68 / 100,
                centerY - 5, false, white);

        int side2X = x + width * 45 / 100 - this.fontRendererObj.getStringWidth(side2) / 2;
        int side2Y = y + 5;
        this.fontRendererObj.drawStringWithShadow(side2, side2X, side2Y, white);
        drawVerticalArrow(x + width * 45 / 100, side2Y + 11,
                y + height * 37 / 100, white);

        int midX = x + width * 61 / 100 - this.fontRendererObj.getStringWidth(mid) / 2;
        int midY = y + height * 18 / 100;
        this.fontRendererObj.drawStringWithShadow(mid, midX, midY, white);
        drawVerticalArrow(x + width * 61 / 100, midY + 11,
                y + height * 39 / 100, white);
    }

    private static void drawHorizontalArrow(int fromX, int toX, int y,
            boolean pointsRight, int color) {
        drawLine(fromX, y, toX, y, color);
        int tip = pointsRight ? toX : toX;
        int direction = pointsRight ? -1 : 1;
        drawLine(tip, y, tip + direction * 5, y - 3, color);
        drawLine(tip, y, tip + direction * 5, y + 3, color);
    }

    private static void drawVerticalArrow(int x, int fromY, int toY, int color) {
        drawLine(x, fromY, x, toY, color);
        drawLine(x, toY, x - 3, toY - 5, color);
        drawLine(x, toY, x + 3, toY - 5, color);
    }

    private void drawThemedBackground() {
        drawGradientRect(0, 0, this.width, this.height, MenuTheme.color(0xFF10121A, 0xFF101113), MenuTheme.color(0xFF20222D, 0xFF222326));
        drawGradientRect(0, this.height / 3, this.width, this.height,
                MenuTheme.color(0x0020222D, 0x00222326), MenuTheme.color(0xC010121A, 0xC0101113));
        int spacing = clamp(Math.min(this.width, this.height) / 7, 28, 58);
        for (int x = -this.height; x < this.width + this.height; x += spacing) {
            drawLine(x, 0, x + this.height, this.height, MenuTheme.color(0x18333447, 0x1849321B));
        }
        for (int x = 0; x < this.width + this.height * 2; x += spacing * 2) {
            drawLine(x, 0, x - this.height, this.height, MenuTheme.color(0x10333447, 0x1049321B));
        }
        int centerX = this.width / 2;
        int centerY = this.height / 3;
        int radius = clamp(Math.min(this.width, this.height) / 3, 55, 150);
        drawDiamond(centerX, centerY, radius, MenuTheme.color(0x1E656B89, 0x1E77736D));
        drawDiamond(centerX, centerY, radius / 2, MenuTheme.color(0x27656B89, 0x2777736D));
    }

    private void drawPanel(int x, int y, int width, int height,
            boolean hovered, boolean selected) {
        int topColor = selected ? MenuTheme.color(0xD5333447, 0xD549321B) : hovered ? MenuTheme.color(0xC6333447, 0xC649321B) : MenuTheme.color(0xB420222D, 0xB4222326);
        int bottomColor = selected ? MenuTheme.color(0xDA20222D, 0xDA222326) : hovered ? MenuTheme.color(0xCC20222D, 0xCC222326) : MenuTheme.color(0xC810121A, 0xC8101113);
        drawGradientRect(x, y, x + width, y + height, topColor, bottomColor);
        drawFrame(x, y, x + width, y + height,
                selected ? MenuTheme.color(0xFFB3ABEF, 0xFFFFAD38) : hovered ? MenuTheme.color(0xE3EBEAF5, 0xE3F3EEE5) : MenuTheme.color(0xCC9DA3CA, 0xCCB8AA96));
        if (width > 5 && height > 5) {
            drawFrame(x + 2, y + 2, x + width - 2, y + height - 2,
                    selected ? MenuTheme.color(0xBAB3ABEF, 0xBAFFAD38) : MenuTheme.color(0x55656B89, 0x5577736D));
        }
    }

    private static void drawScrollbar(int x, int y, int height,
            int totalUnits, int visibleUnits, int offset) {
        if (height <= 2) return;
        drawRect(x, y, x + 2, y + height, MenuTheme.color(0x55656B89, 0x5577736D));
        if (totalUnits <= 0 || visibleUnits >= totalUnits) {
            drawRect(x, y, x + 2, y + height, MenuTheme.color(0x88656B89, 0x8877736D));
            return;
        }
        int thumbHeight = Math.max(8, height * visibleUnits / totalUnits);
        int range = height - thumbHeight;
        int maxOffset = Math.max(1, totalUnits - visibleUnits);
        int thumbY = y + range * offset / maxOffset;
        drawRect(x - 1, thumbY, x + 3, thumbY + thumbHeight, MenuTheme.color(0xCCB3ABEF, 0xCCFFAD38));
    }

    private static void drawDivider(int left, int right, int y) {
        int center = (left + right) / 2;
        drawRect(left, y, center - 5, y + 1, MenuTheme.color(0x779DA3CA, 0x77B8AA96));
        drawRect(center + 5, y, right, y + 1, MenuTheme.color(0x779DA3CA, 0x77B8AA96));
        drawLine(center, y - 3, center + 3, y, MenuTheme.color(0xAA9DA3CA, 0xAAB8AA96));
        drawLine(center + 3, y, center, y + 3, MenuTheme.color(0xAA9DA3CA, 0xAAB8AA96));
        drawLine(center, y + 3, center - 3, y, MenuTheme.color(0xAA9DA3CA, 0xAAB8AA96));
        drawLine(center - 3, y, center, y - 3, MenuTheme.color(0xAA9DA3CA, 0xAAB8AA96));
    }

    private static void drawCompactWings(int left, int right, int top, int bottom, int color) {
        int middle = (top + bottom) / 2;
        drawLine(left + 1, top + 4, left + 6, middle, color);
        drawLine(left + 6, middle, left + 1, bottom - 4, color);
        drawLine(right - 1, top + 4, right - 6, middle, color);
        drawLine(right - 6, middle, right - 1, bottom - 4, color);
    }

    private static void drawDiamond(int centerX, int centerY, int radius, int color) {
        drawLine(centerX, centerY - radius, centerX + radius, centerY, color);
        drawLine(centerX + radius, centerY, centerX, centerY + radius, color);
        drawLine(centerX, centerY + radius, centerX - radius, centerY, color);
        drawLine(centerX - radius, centerY, centerX, centerY - radius, color);
    }

    private void drawTexturedQuad(int x, int y, int width, int height) {
        GL11.glColor4f(1, 1, 1, 1);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, this.zLevel, 0, 1);
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, 1, 1);
        tessellator.addVertexWithUV(x + width, y, this.zLevel, 1, 0);
        tessellator.addVertexWithUV(x, y, this.zLevel, 0, 0);
        tessellator.draw();
    }

    private String fitText(String text, int maximumWidth) {
        if (this.fontRendererObj.getStringWidth(text) <= maximumWidth) return text;
        String suffix = "...";
        return this.fontRendererObj.trimStringToWidth(text,
                Math.max(1, maximumWidth - this.fontRendererObj.getStringWidth(suffix))) + suffix;
    }

    private static boolean contains(int mouseX, int mouseY,
            int x, int y, int width, int height) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
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

    @Override
    public boolean doesGuiPauseGame() {
        return this.parent != null && this.parent.doesGuiPauseGame();
    }

    private static final class WikiLine {
        private final String text;
        private final int color;

        private WikiLine(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }
}
