package com.mod.htgt6.client.GUI;

import com.mod.htgt6.common.TE.UniversalGasTurbineTE;
import com.mod.htgt6.common.inventory.ContainerUniversalGasTurbine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;

public class GuiUniversalGasTurbine extends GuiContainer {

    private static final ResourceLocation texture =
            new ResourceLocation("htgt6", "textures/gui/UniversalGasTurbine.png");

    private final UniversalGasTurbineTE te;
    private float rotorAngle = 0F;

    // "i" Icon positioning (relative to the GUI top-left corner)
    private final int iconX = 155;
    private final int iconY = 6;
    private final int iconWidth = 12;
    private final int iconHeight = 12;

    public GuiUniversalGasTurbine(ContainerUniversalGasTurbine container, UniversalGasTurbineTE te) {
        super(container);
        this.te = te;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 1. Draw the default container layout and slots
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 2. Calculate actual screen-space position of the icon
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        int actualIconX = guiLeft + iconX;
        int actualIconY = guiTop + iconY;

        // 3. Hover detection check
        if (mouseX >= actualIconX && mouseX <= actualIconX + iconWidth &&
                mouseY >= actualIconY && mouseY <= actualIconY + iconHeight) {

            List<String> tooltipText = new ArrayList<String>();
            tooltipText.add("\u00a7b\u00a7lTurbine Diagnostics"); // Blue & Bold title

            // Gather fluid info
            FluidStack fluid = te.fluidTank.getFluid();
            String fluidName = (fluid != null) ? fluid.getLocalizedName() : "Empty";
            tooltipText.add("\u00a77Gas: \u00a7f" + fluidName);
            tooltipText.add("\u00a79" + te.fluidTank.getFluidAmount() + " / " + te.fluidTank.getCapacity() + " mB");

            // Gather energy metrics
            tooltipText.add("\u00a77Buffer: \u00a7a" + (int)te.energy + " EU");
            tooltipText.add("\u00a7eOutput: " + te.getOutputVoltage() + " EU/t");
            tooltipText.add("\u00a7dTier: " + te.machineTier);

            if (!te.enabled) {
                tooltipText.add("\u00a7cSTATUS: OFFLINE");
            } else {
                tooltipText.add("\u00a7aSTATUS: ONLINE");
            }

            // Draw vanilla styling hovering tooltip box
            this.func_146283_a(tooltipText, mouseX, mouseY); // 1.7.10 obfuscated mapping for drawHoveringText
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

        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        if (te.enabled) {
            rotorAngle += 8F;
            if (rotorAngle >= 360F) {
                rotorAngle -= 360F;
            }
        }

        drawRotor(guiLeft + 76, guiTop + 50, rotorAngle);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private void drawRotor(int x, int y, float angle) {
        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);
        GL11.glRotatef(angle, 0F, 0F, 1F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Rotor blades
        GL11.glColor3f(0.65F, 0.65F, 0.70F);
        for (int i = 0; i < 8; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(i * 45F, 0F, 0F, 1F);
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glVertex2f(-2F, -8F);
            GL11.glVertex2f( 2F, -8F);
            GL11.glVertex2f( 3F, -16F);
            GL11.glVertex2f(-3F, -16F);
            GL11.glEnd();
            GL11.glPopMatrix();
        }

        // Outer ring
        GL11.glColor3f(0.25F, 0.25F, 0.30F);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = 0; i < 32; i++) {
            double a = Math.toRadians(i * 11.25);
            GL11.glVertex2d(Math.cos(a) * 10, Math.sin(a) * 10);
        }
        GL11.glEnd();

        // Hub
        GL11.glColor3f(0.85F, 0.70F, 0.20F);
        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = 0; i < 32; i++) {
            double a = Math.toRadians(i * 11.25);
            GL11.glVertex2d(Math.cos(a) * 5, Math.sin(a) * 5);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        GL11.glPopMatrix();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title
        fontRendererObj.drawString("Universal Gas Turbine", 8, 6, 0x404040);

        // --- DRAW "i" ELEMENT VIA OPENGL/FONT ---
        // Render a dark blue/gray box boundary for the info badge
        drawRect(iconX, iconY, iconX + iconWidth, iconY + iconHeight, 0xFF373737);
        drawRect(iconX + 1, iconY + 1, iconX + iconWidth - 1, iconY + iconHeight - 1, 0xFF8B8B8B);

        // Draw the text string "i" right in the middle of our custom button box
        fontRendererObj.drawString("i", iconX + 4, iconY + 2, 0x0044FF);

        // Standard diagnostic prints on screen removed from here to cleanly move to tooltip,
        // or left running concurrently. (Currently kept empty here to isolate text to tooltip!)
        if (!te.enabled) {
            fontRendererObj.drawString("OFF", 120, 50, 0xFF0000);
        }
    }
}