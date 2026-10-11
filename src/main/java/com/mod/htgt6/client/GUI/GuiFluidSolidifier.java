package com.mod.htgt6.client.GUI;



import codechicken.nei.api.API;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.DefaultOverlayHandler;
import com.mod.htgt6.common.TE.FluidSolidifierTE;

import com.mod.htgt6.common.inventory.ContainerFluidSolidifier;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiFluidSolidifier extends GuiContainer {

    private static final int BUTTON_NEI_ID = 100;

    private static final ResourceLocation texture =
            new ResourceLocation("htgt6", "textures/gui/FluidSolidifier.png");

    private final FluidSolidifierTE te;
    private final int iconX = 158;
    private final int iconY = 32;
    private final int iconWidth = 12;
    private final int iconHeight = 12;

    public GuiFluidSolidifier(ContainerFluidSolidifier container, FluidSolidifierTE te) {
        super(container);
        this.te = te;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(BUTTON_NEI_ID, guiLeft + 148, guiTop + 4, 24, 12,
                EnumChatFormatting.YELLOW + "NEI"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_NEI_ID) {
            if (!GuiCraftingRecipe.openRecipeGui("fluid_solidifier")) {
                GuiCraftingRecipe.openRecipeGui("item", new ItemStack(te.getBlockType(), 1, te.getBlockMetadata()));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int actualIconX = guiLeft + iconX;
        int actualIconY = guiTop + iconY;

        // Hover tooltip diagnostics box
        if (mouseX >= actualIconX && mouseX <= actualIconX + iconWidth &&
                mouseY >= actualIconY && mouseY <= actualIconY + iconHeight) {

            List<String> tooltipText = new ArrayList<String>();
            tooltipText.add("\u00a7b\u00a7lSolidifier Diagnostics");
            tooltipText.add("\u00a77Stored Energy: \u00a7a" + te.energy + " EU");
            tooltipText.add("\u00a77Max Input: \u00a7e" + te.getMaxInputVoltage() + " EU/t");
            tooltipText.add("\u00a77Machine Tier: " + te.getTierName());

            tooltipText.add("\u00a77Fluid input: \u00a7dMultiblock gateways");

            if (te.maxProgress > 0) {
                int percent = (te.progress * 100) / te.maxProgress;
                tooltipText.add("\u00a77Progress: \u00a7f" + percent + "% (" + te.progress + "/" + te.maxProgress + ")");
            }

            if (!te.enabled) {
                tooltipText.add("\u00a7cSTATUS: OFFLINE");
            } else {
                tooltipText.add("\u00a7aSTATUS: ONLINE");
            }

            this.func_146283_a(tooltipText, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(texture);

        // Standard texture filter adjustments
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        int guiLeft = (width - xSize) / 2;
        int guiTop = (height - ySize) / 2;

        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Progress Arrow Rendering
        if (te.maxProgress > 0 && te.progress > 0) {
            int progressWidth = (te.progress * 24) / te.maxProgress;
            drawTexturedModalRect(guiLeft + 72, guiTop + 26, 176, 0, progressWidth, 16);
        }

        // Texture optimization reset
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Fluid Solidifier", 8, 6, 0x404040);

        if (te.maxProgress > 0) {
            int percent = (te.progress * 100) / te.maxProgress;
            fontRendererObj.drawString(percent + "%", 76, 16, 0xFFFFFF);
        }

        if (!te.enabled) {
            fontRendererObj.drawString("OFF", 138, 22, 0xFF0000);
        }

        // Diagnostics info marker "i" box mapping
        drawRect(iconX, iconY, iconX + iconWidth, iconY + iconHeight, 0xFF373737);
        drawRect(iconX + 1, iconY + 1, iconX + iconWidth - 1, iconY + iconHeight - 1, 0xFF8B8B8B);
        fontRendererObj.drawString("i", iconX + 4, iconY + 2, 0x0044FF);

        // NEI Support mapping links
        // Replace "fluid_solidifier" string with your custom registered recipe identifier if applicable
        API.registerGuiOverlay(GuiFluidSolidifier.class, "fluid_solidifier");
        API.registerGuiOverlayHandler(GuiFluidSolidifier.class, new DefaultOverlayHandler(), "fluid_solidifier");
    }
}
