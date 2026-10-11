package com.mod.htgt6.api.hitech6.BaseMachines;

import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;



public abstract class GuiBaseMachine extends GuiContainer {

    protected final TileEntityBaseMachine tile;
    protected final ResourceLocation texture;

    // Configurable info icon location ("i" box)
    protected int iconX = 158;
    protected int iconY = 32;
    protected int iconWidth = 12;
    protected int iconHeight = 12;






    public GuiBaseMachine(Container container, TileEntityBaseMachine tile, ResourceLocation texture) {
        super(container);
        this.tile = tile;
        this.texture = texture;
        this.xSize = 176;
        this.ySize = 166;
    }

    /** Provide localized machine display title (e.g., "Assembler") */
    public abstract String getGuiTitle();

    /** Build dynamic lines of text to be shown inside the "i" info tooltip */
    protected void addDiagnosticsTooltip(List<String> tooltip) {
        tooltip.add("\u00a7b\u00a7l" + getGuiTitle() + " Diagnostics");
        tooltip.add("\u00a77Stored Energy: \u00a7a" + tile.energy + " EU");
        tooltip.add("\u00a77Max Input: \u00a7e" + tile.getMaxInputVoltage() + " EU/t");
        tooltip.add("\u00a77Tier: \u00a7d" + tile.machineTier);

        if (tile.maxProgress > 0) {
            int percent = (tile.progress * 100) / tile.maxProgress;
            int currentSec = tile.progress / 20;
            int maxSec = tile.maxProgress / 20;

            tooltip.add("\u00a77Progress: \u00a7f" + percent + "% (" + currentSec + "s / " + maxSec + "s)");
        }

        tooltip.add(tile.enabled ? "\u00a7aSTATUS: ONLINE" : "\u00a7cSTATUS: OFFLINE");
    }

    /** Override in subclasses to draw machine-specific progress bars or animations */
    protected void drawExtraBackground(int guiLeft, int guiTop) {
        // Left empty so base machines don't render an unneeded default progress bar
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // Render hover diagnostics tooltip
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int actualIconX = guiLeft + iconX;
        int actualIconY = guiTop + iconY;

        if (mouseX >= actualIconX && mouseX <= actualIconX + iconWidth &&
                mouseY >= actualIconY && mouseY <= actualIconY + iconHeight) {

            List<String> tooltipText = new ArrayList<String>();
            addDiagnosticsTooltip(tooltipText);
            this.func_146283_a(tooltipText, mouseX, mouseY); // drawHoveringText
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(texture);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        int guiLeft = (width - xSize) / 2;
        int guiTop = (height - ySize) / 2;

        // Base background GUI sheet
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Render progress bar or custom graphics
        drawExtraBackground(guiLeft, guiTop);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Draw title
        fontRendererObj.drawString(getGuiTitle(), 8, 6, 0x404040);

        // Draw info box "i"
        drawRect(iconX, iconY, iconX + iconWidth, iconY + iconHeight, 0xFF373737);
        drawRect(iconX + 1, iconY + 1, iconX + iconWidth - 1, iconY + iconHeight - 1, 0xFF8B8B8B);
        fontRendererObj.drawString("i", iconX + 4, iconY + 2, 0x0044FF);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            tile.enabled = !tile.enabled;
            button.displayString = tile.enabled ? "ON" : "OFF";
        }

    }



}
