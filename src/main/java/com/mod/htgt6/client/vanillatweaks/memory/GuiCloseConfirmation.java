package com.mod.htgt6.client.vanillatweaks.memory;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.GL11;

public class GuiCloseConfirmation extends GuiScreen {

    private final GuiScreen parentScreen;

    public GuiCloseConfirmation(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        this.buttonList.clear();
        int buttonWidth = 100;
        int buttonHeight = 20;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.add(new GuiButton(0, centerX - buttonWidth - 10, centerY + 10, buttonWidth, buttonHeight, "§aYes, Close"));
        this.buttonList.add(new GuiButton(1, centerX + 10, centerY + 10, buttonWidth, buttonHeight, "§cNo, Stay"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            // Player chose to leave -> Force standard termination sequence
            this.mc.shutdown();
        } else if (button.id == 1) {
            // Player wants to cancel -> Safely revert context screen mapping
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GL11.glPushMatrix();
        GL11.glScalef(1.2F, 1.2F, 1.2F);
        String title = "§c§lExit Confirmation Check";
        int titleWidth = this.fontRendererObj.getStringWidth(title);
        this.fontRendererObj.drawStringWithShadow(title, (int) ((this.width / 2.0F) / 1.2F) - (titleWidth / 2), (int) ((this.height / 2.0F - 30) / 1.2F), 0xFFFFFF);
        GL11.glPopMatrix();

        String question = "§7Are you sure you really want to close the game client?";
        int questionWidth = this.fontRendererObj.getStringWidth(question);
        this.fontRendererObj.drawStringWithShadow(question, this.width / 2 - (questionWidth / 2), this.height / 2 - 10, 0xCCCCCC);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) { // Esc key cancels exit
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}