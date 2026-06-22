package com.mod.htgt6.client.vanillatweaks.SSUI;

import com.mod.htgt6.client.vanillatweaks.SSUI.GuiModernServerSelect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.Tessellator;

import java.util.List;

public class GuiServerList extends GuiSlot {

    private final GuiModernServerSelect parent;
    private final List<ServerData> servers;

    public GuiServerList(GuiModernServerSelect parent, Minecraft mc, List<ServerData> servers) {
        // Sets up matching bounds layout offsets
        super(mc, parent.width, parent.height, 52, parent.height - 70, 72);
        this.parent = parent;
        this.servers = servers;

        this.setShowSelectionBox(false);
        this.setHasListHeader(false, 0);
    }

    @Override
    protected int getSize() {
        return servers.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
        parent.selectedIndex = index;
        if (doubleClick) {
            parent.connectToSelectedServer();
        }
    }

    @Override
    protected boolean isSelected(int index) {
        return index == parent.selectedIndex;
    }

    @Override
    protected void drawBackground() {
        // Handled via parent rendering configurations
    }

    @Override
    protected void drawContainerBackground(Tessellator tess) {
        // Left clear to preserve modern theme elements over vanilla tile assets
    }

    @Override
    protected void drawSlot(int index, int slotX, int y, int height, Tessellator tess, int mouseX, int mouseY) {
        if (index >= servers.size()) {
            return;
        }

        parent.drawServerCard(servers.get(index), y, isSelected(index));
    }
}