package com.mod.htgt6.client.inventory112;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** The original green recipe-book button stored at u=178 in the 1.12 inventory texture. */
final class RecipeBookButton112 extends GuiButton {
    private static final ResourceLocation INVENTORY =
            new ResourceLocation("htgt6", "textures/gui/container/inventory_1_12.png");

    RecipeBookButton112(int id, int x, int y) {
        super(id, x, y, 20, 18, "");
    }

    @Override public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (!visible) return;
        boolean hovered = mouseX >= xPosition && mouseY >= yPosition
                && mouseX < xPosition + width && mouseY < yPosition + height;
        GL11.glColor4f(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(INVENTORY);
        drawTexturedModalRect(xPosition, yPosition, 178, hovered ? 19 : 0, width, height);
    }
}
