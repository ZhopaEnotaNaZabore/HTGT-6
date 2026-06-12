package com.mod.htgt6.client.GUI;


import codechicken.nei.api.API;
import codechicken.nei.recipe.DefaultOverlayHandler;
import com.mod.htgt6.common.TE.TESuperMasicCompressor;
import com.mod.htgt6.common.handler.recipe.compressor.CSrecipeHandler;
import com.mod.htgt6.common.handler.recipe.compressor.CompressorRecipeHandler;
import com.mod.htgt6.common.inventory.ContainerSuperMasicCompressor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiSuperMasicCompressor extends GuiContainer {

    // ==========================================
    // TEXTURE
    // ==========================================
    private static final ResourceLocation texture =
            new ResourceLocation(
                    "htgt6",
                    "textures/gui/Compressor.png"
            );

    // ==========================================
    // TILE
    // ==========================================
    private final TESuperMasicCompressor te;

    // ==========================================
    // BUTTON
    // ==========================================
    private GuiButton powerButton;

    // ==========================================
    // GUI CONSTRUCTOR
    // ==========================================
    public GuiSuperMasicCompressor(
            ContainerSuperMasicCompressor container,
            TESuperMasicCompressor te
    ) {
        super(container);
        this.te = te;

        // STANDARD SIZE
        this.xSize = 176;
        this.ySize = 166;
    }

    // ==========================================
    // BUTTON ACTIONS
    // ==========================================
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            te.enabled = !te.enabled;
            button.displayString = te.enabled ? "ON" : "OFF";
        }
    }

    // ==========================================
    // BACKGROUND DRAWING
    // ==========================================
    @Override
    protected void drawGuiContainerBackgroundLayer(
            float partialTicks,
            int mouseX,
            int mouseY
    ) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(texture);

        // ==========================================
        // SMOOTH TEXTURE FILTERING
        // ==========================================
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        int guiLeft = (width - xSize) / 2;
        int guiTop = (height - ySize) / 2;

        // Draw Base GUI Layout
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // ==========================================
        // PROGRESS BAR (Positioned between row inputs and output)
        // ==========================================
        // Inputs end around x=89, Output begins at x=116.
        // Placing arrow nicely at x=90 horizontally.
        if (te.maxProgress > 0 && te.progress > 0) {
            int progressWidth = (te.progress * 24) / te.maxProgress;

            drawTexturedModalRect(
                    guiLeft + 78,
                    guiTop + 23,
                    176,
                    0,
                    progressWidth,
                    16
            );
        }

        // ==========================================
        // RESET OPENGL ALTERATIONS
        // ==========================================
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    // ==========================================
    // FOREGROUND DRAWING
    // ==========================================
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title Text
        fontRendererObj.drawString("SMCompressor", 8, 6, 0x404040);

        // Energy Tracking Information
        fontRendererObj.drawString("EU: " + te.energy, 145, 14, 0x00FF00);
        fontRendererObj.drawString(te.getMaxInputVoltage() + " EU/t", 145, 20, 0xFFFF00);

        // Machine Tier Layout
        fontRendererObj.drawString(getTierName(te.machineTier), 145, 8, getTierColor(te.machineTier));

        // Progress Percentage Marker
        if (te.maxProgress > 0) {
            int percent = (te.progress * 100) / te.maxProgress;
            fontRendererObj.drawString(percent + "%", 94, 22, 0xFFFFFF);
        }

        // Offline Notification Indicator
        if (!te.enabled) {
            fontRendererObj.drawString("OFF", 138, 22, 0xFF0000);
        }

        // ==========================================
        // NEI
        // ==========================================
        API.registerUsageHandler(new CSrecipeHandler());
        API.registerRecipeHandler(new CSrecipeHandler());
        API.registerGuiOverlay(GuiSuperMasicCompressor.class, "SMCompressor");
        API.registerGuiOverlayHandler(GuiSuperMasicCompressor.class, new DefaultOverlayHandler(), "SMCompressor");
    }

    // ==========================================
    // LOCAL HELPER METHODS
    // ==========================================
    private String getTierName(int tier) {
        switch (tier) {
            case 2: return "MV";
            case 3: return "HV";
            case 4: return "EV";
            case 5: return "IV";
            case 6: return "LuV";
            case 7: return "ZPM";
            case 8: return "UV";
            case 9: return "PUV1";
            case 10: return "UX";
            case 11: return "OLV";
            case 12: return "OMV";
            case 13: return "OHV";
            case 14: return "OEV";
            case 15: return "OIV";
            case 16: return "MAX";
            default: return "LV";
        }
    }

    private int getTierColor(int tier) {
        switch (tier) {
            case 2: return 0x00AAFF;
            case 3: return 0xFFFF00;
            case 4: return 0xFF8800;
            case 5: return 0xFF0000;
            case 6: return 0xAA00FF;
            case 7: return 0xFF00FF;
            case 8: return 0x00FFFF;
            default: return 0xAAAAAA;
        }
    }
}
