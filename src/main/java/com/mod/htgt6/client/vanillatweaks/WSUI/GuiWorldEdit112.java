package com.mod.htgt6.client.vanillatweaks.WSUI;

import java.awt.Desktop;
import java.io.File;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.WorldInfo;
import org.lwjgl.input.Keyboard;

/** 1.12 world edit screen: rename, reset icon and open save folder. */
public final class GuiWorldEdit112 extends GuiScreen {
    private final GuiScreen parent;
    private final String folderName;
    private GuiTextField nameField;
    private GuiButton saveButton, resetIconButton;

    public GuiWorldEdit112(GuiScreen parent, String folderName) {
        this.parent = parent; this.folderName = folderName;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true); buttonList.clear();
        int x = width / 2 - 100, y = height / 4;
        buttonList.add(resetIconButton = new GuiButton(3, x, y + 36, 200, 20, I18n.format("selectWorld.edit.resetIcon")));
        buttonList.add(new GuiButton(4, x, y + 60, 200, 20, I18n.format("selectWorld.edit.openFolder")));
        buttonList.add(saveButton = new GuiButton(0, x, y + 108, 200, 20, I18n.format("selectWorld.edit.save")));
        buttonList.add(new GuiButton(1, x, y + 132, 200, 20, I18n.format("gui.cancel")));
        WorldInfo info = mc.getSaveLoader().getWorldInfo(folderName);
        nameField = new GuiTextField(fontRendererObj, x, y + 12, 200, 20);
        nameField.setMaxStringLength(32); nameField.setText(info == null ? "" : info.getWorldName()); nameField.setFocused(true);
        resetIconButton.enabled = iconFile().isFile(); updateSaveButton();
    }

    @Override public void updateScreen() { nameField.updateCursorCounter(); }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button.id == 0) { mc.getSaveLoader().renameWorld(folderName, nameField.getText().trim()); mc.displayGuiScreen(parent); }
        else if (button.id == 1) mc.displayGuiScreen(parent);
        else if (button.id == 3) { iconFile().delete(); resetIconButton.enabled = false; }
        else if (button.id == 4) try { if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(worldFolder()); } catch (Exception ignored) { }
    }

    @Override
    protected void keyTyped(char character, int key) {
        nameField.textboxKeyTyped(character, key); updateSaveButton();
        if (key == Keyboard.KEY_RETURN && saveButton.enabled) actionPerformed(saveButton);
        else if (key == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
    }

    @Override protected void mouseClicked(int x, int y, int button) { super.mouseClicked(x, y, button); nameField.mouseClicked(x, y, button); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partial) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("selectWorld.edit.title"), width / 2, 20, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("selectWorld.enterName"), width / 2 - 100, height / 4 - 1, 0xA0A0A0);
        nameField.drawTextBox(); super.drawScreen(mouseX, mouseY, partial);
    }

    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }
    private void updateSaveButton() { saveButton.enabled = !nameField.getText().trim().isEmpty(); }
    private File worldFolder() { return new File(new File(mc.mcDataDir, "saves"), folderName); }
    private File iconFile() { return new File(worldFolder(), "icon.png"); }
}
