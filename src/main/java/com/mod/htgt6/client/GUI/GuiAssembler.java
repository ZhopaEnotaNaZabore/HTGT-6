package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.inventory.ContainerAssembler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiAssembler extends GuiContainer {
    private static final ResourceLocation texture = new ResourceLocation("htgt6", "textures/gui/Assembler.png");
    private TileEntityAssembler te;

    public GuiAssembler(ContainerAssembler container, TileEntityAssembler te) {
        super(container);
        this.te = te;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float p, int mx, int my) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);

        if (this.te.maxProgress > 0 && this.te.progress > 0) {
            int prog = (this.te.progress * 22) / this.te.maxProgress;
            this.drawTexturedModalRect(k + 78, l + 25, 176, 0, prog, 16);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString("Assembler", 8, 6, 4210752);

        if (this.te.maxProgress > 0 && this.te.progress > 0) {
            int percent = (this.te.progress * 100) / this.te.maxProgress;
            String s = Math.min(100, percent) + "%";
            this.fontRendererObj.drawString(s, 89 - (fontRendererObj.getStringWidth(s) / 2), 14, 4210752);
        }

        if (this.te.progress > 0 && this.te.energy < 8) {
            String warn = "LOW POWER";
            this.fontRendererObj.drawString(warn, 88 - (fontRendererObj.getStringWidth(warn) / 2), 45, 0xFF0000);
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        // ESSENTIAL: This allows GuiContainer to handle slot clicks,
        // shift-clicks, and dragging items into/out of slots.
        super.mouseClicked(x, y, button);

        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // Custom logic for clicking the progress bar (e.g., for NEI integration)
        if (x >= guiLeft + 78 && x <= guiLeft + 100 && y >= guiTop + 25 && y <= guiTop + 41) {
            // Add recipe look-up logic here if using NEI/JEI
            if (x >= guiLeft + 74 && x <= guiLeft + 74 + 24 && y >= guiTop + 23 && y <= guiTop + 23 + 17) {
                GuiCraftingRecipe.openRecipeGui("assembling_machine");
        }
    }
}
}