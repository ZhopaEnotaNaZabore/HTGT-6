package com.mod.htgt6.client.inventory112;

import java.util.Collections;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Minecraft 1.12 crafting-table screen using the shared 3x3 recipe book. */
public final class GuiCrafting112 extends GuiContainer {
    private static final ResourceLocation CRAFTING_TABLE =
            new ResourceLocation("htgt6", "textures/gui/container/crafting_table_1_12.png");
    private final RecipeBook112 recipeBook = new RecipeBook112(3);
    private GuiButton recipeButton;
    private Inventory112Layout layout;

    public GuiCrafting112(ContainerWorkbench container) {
        super(container);
    }

    @Override public void initGui() {
        super.initGui();
        updateLayout();
        recipeButton = new RecipeBookButton112(112, guiLeft + 5, guiTop + 34);
        buttonList.add(recipeButton);
        recipeBook.initialize(mc, fontRendererObj);
    }

    private void updateLayout() {
        layout = Inventory112Layout.calculate(width, height, recipeBook.isOpen());
        guiLeft = layout.inventoryLeft();
        guiTop = layout.top();
        if (recipeButton != null) {
            recipeButton.xPosition = guiLeft + 5;
            recipeButton.yPosition = guiTop + 34;
        }
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 112) {
            recipeBook.toggle();
            updateLayout();
            return;
        }
        super.actionPerformed(button);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        updateLayout();
        if (recipeBook.isOpen() && layout.overlay()) {
            drawDefaultBackground();
            drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            recipeBook.draw(recipeLeft(), guiTop, mouseX, mouseY, partialTicks);
            drawRecipeTooltip(mouseX, mouseY);
            return;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (recipeBook.isOpen()) {
            recipeBook.draw(recipeLeft(), guiTop, mouseX, mouseY, partialTicks);
            drawRecipeTooltip(mouseX, mouseY);
        }
    }

    private void drawRecipeTooltip(int mouseX, int mouseY) {
        ItemStack output = recipeBook.hoveredOutput(recipeLeft(), guiTop, mouseX, mouseY);
        if (output != null) renderToolTip(output, mouseX, mouseY);
        else {
            String control = recipeBook.hoveredControl(recipeLeft(), guiTop, mouseX, mouseY);
            if (control != null)
                drawHoveringText(Collections.singletonList(control), mouseX, mouseY, fontRendererObj);
        }
    }

    @Override protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (recipeBook.isOpen()
                && recipeBook.mouseClicked(recipeLeft(), guiTop, mouseX, mouseY, button)) return;
        if (recipeBook.isOpen() && layout.overlay()) return;
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override protected void keyTyped(char character, int keyCode) {
        if (recipeBook.isOpen() && recipeBook.keyTyped(character, keyCode)) return;
        super.keyTyped(character, keyCode);
    }

    private int recipeLeft() {
        return recipeBook.isOpen() && !layout.overlay()
                ? guiLeft - Inventory112Layout.RECIPE_WIDTH - 1 : layout.recipeLeft();
    }

    List<Rectangle> neiOccupiedAreas() {
        List<Rectangle> areas = new ArrayList<Rectangle>(2);
        areas.add(new Rectangle(guiLeft, guiTop, xSize, ySize));
        if (recipeBook.isOpen())
            areas.add(new Rectangle(recipeLeft() - 28, guiTop,
                    Inventory112Layout.RECIPE_WIDTH + 28, ySize));
        return areas;
    }

    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(CRAFTING_TABLE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 28, 6, 4210752);
        fontRendererObj.drawString(I18n.format("container.inventory", new Object[0]), 8, ySize - 96 + 2, 4210752);
    }
}
