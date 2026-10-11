package com.mod.htgt6.client.vanillatweaks.WSUI;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

import java.util.List;

public class GuiWorldList extends GuiSlot {

    private final GuiModernWorldSelect parent;
    private final List<WorldEntry> worlds;

    public GuiWorldList(
            GuiModernWorldSelect parent,
            Minecraft mc,
            List<WorldEntry> worlds
    ) {
        super(
                mc,
                parent.width,
                parent.height,
                48,
                parent.height - 64,
                36
        );
        this.parent = parent;
        this.worlds = worlds;

        this.setShowSelectionBox(false);
        this.setHasListHeader(false, 0);
    }

    @Override
    protected int getSize() {
        return worlds.size();
    }

    @Override
    protected void elementClicked(
            int index,
            boolean doubleClick,
            int mouseX,
            int mouseY
    ) {
        parent.selectedIndex = index;
        if (doubleClick) {
            parent.playSelectedWorld();
        }
    }

    @Override
    protected boolean isSelected(int index) {
        return index == parent.selectedIndex;
    }

    @Override
    protected void drawBackground() {
        parent.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(
            int index,
            int slotX, // We ignore this to avoid vanilla center-squishing
            int y,
            int height,
            Tessellator tess,
            int mouseX,
            int mouseY
    ) {
        if (index >= worlds.size()) {
            return;
        }

        parent.drawWorldEntry(
                worlds.get(index),
                y,
                isSelected(index)
        );
    }
}
