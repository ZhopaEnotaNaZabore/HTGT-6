package com.mod.htgt6.client.menu;

import com.mod.htgt6.common.ui.MenuTheme;
import com.mod.htgt6.common.ui.MachineUiTheme;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

/** Menu and machine appearances share a settings entry, but remain independent preferences. */
public final class GuiHiTechAppearance extends GuiScreen {
    private final GuiScreen parent;
    private int top;
    public GuiHiTechAppearance(GuiScreen parent) { this.parent = parent; }

    @Override public void initGui() {
        buttonList.clear();
        top = Math.max(4, (height - 224) / 2);
        int w = Math.min(280, width - 16), x = (width - w) / 2;
        for (MenuTheme theme : MenuTheme.values()) {
            String label = I18n.format(theme.translationKey());
            if (theme == MenuTheme.getActive()) label = "> " + label + " <";
            buttonList.add(new MenuAppearance.Button(10 + theme.ordinal(), x,
                    top + 24 + theme.ordinal() * 22, w, 20, label));
        }
        for (MachineUiTheme theme : MachineUiTheme.values()) {
            String label = theme.getDisplayName();
            if (theme == MachineUiTheme.getActive()) label = "> " + label + " <";
            buttonList.add(new MenuAppearance.Button(20 + theme.ordinal(), x,
                    top + 88 + theme.ordinal() * 22, w, 20, label));
        }
        buttonList.add(new MenuAppearance.Button(100, x, top + 196, w, 20, I18n.format("gui.done")));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id >= 10 && button.id < 10 + MenuTheme.values().length) {
            MenuTheme.setActive(MenuTheme.values()[button.id - 10]);
            initGui();
        } else if (button.id >= 20 && button.id < 20 + MachineUiTheme.values().length) {
            MachineUiTheme.setActive(MachineUiTheme.values()[button.id - 20]);
            initGui();
        } else if (button.id == 100) mc.displayGuiScreen(parent);
    }

    @Override protected void keyTyped(char c, int key) {
        if (key == Keyboard.KEY_ESCAPE) mc.displayGuiScreen(parent);
    }

    @Override public void drawScreen(int x, int y, float partialTicks) {
        MenuAppearance.background(width, height);
        drawCenteredString(fontRendererObj, I18n.format("htgt6.theme.settings"), width / 2, top + 4, 0xFFF1EFF5);
        drawCenteredString(fontRendererObj, I18n.format("htgt6.theme.machines"), width / 2, top + 75, 0xFFBBBBBB);
        drawCenteredString(fontRendererObj, I18n.format("htgt6.theme.machine_hint"), width / 2, top + 181, 0xFF999999);
        super.drawScreen(x, y, partialTicks);
    }
}
