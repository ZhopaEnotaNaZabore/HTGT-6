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
        // Top cap: 52, Bottom cap: 70 from bottom, Slot Height: 72
        super(
                mc,
                parent.width,
                parent.height,
                52,
                parent.height - 70,
                72
        );
        this.parent = parent;
        this.worlds = worlds;

        // Turn these off so we draw our own selection graphics inside the cards
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
        // Left empty so GuiModernWorldSelect handles the core background
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

        // Pass the card drawing straight to the parent screen using screen space coordinates
        parent.drawWorldCard(
                worlds.get(index),
                y,
                isSelected(index)
        );
    }
}