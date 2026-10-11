package com.mod.htgt6.client.inventory112;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Minecraft 1.12 survival inventory layout with its recipe-book side panel. */
public final class GuiInventory112 extends GuiInventory {
    private static final ResourceLocation INVENTORY =
            new ResourceLocation("htgt6", "textures/gui/container/inventory_1_12.png");
    private final RecipeBook112 recipeBook = new RecipeBook112(2);
    private float mouseX;
    private float mouseY;
    private GuiButton recipeButton;
    private Inventory112Layout layout;

    public GuiInventory112(EntityPlayer player) { super(player); }

    @Override public void initGui() {
        super.initGui();
        updateLayout();
        recipeButton = new RecipeBookButton112(112, guiLeft + 104, guiTop + 61);
        buttonList.add(recipeButton);
        recipeBook.initialize(mc, fontRendererObj);
    }

    private void updateLayout() {
        layout = Inventory112Layout.calculate(width, height, recipeBook.isOpen());
        guiLeft = layout.inventoryLeft();
        guiTop = layout.top();
        if (recipeButton != null) {
            recipeButton.xPosition = guiLeft + 104;
            recipeButton.yPosition = guiTop + 61;
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

    @Override public void drawScreen(int x, int y, float partialTicks) {
        mouseX = x;
        mouseY = y;
        updateLayout();
        if (recipeBook.isOpen() && layout.overlay()) {
            drawDefaultBackground();
            drawGuiContainerBackgroundLayer(partialTicks, x, y);
            recipeBook.draw(recipeLeft(), guiTop, x, y, partialTicks);
            drawRecipeTooltip(x, y);
            return;
        }
        super.drawScreen(x, y, partialTicks);
        // GuiContainer 1.7.10 paints its dim background inside super.drawScreen; the 1.12
        // panel must therefore be drawn afterwards or it is flattened/covered by the inventory.
        if (recipeBook.isOpen()) {
            recipeBook.draw(recipeLeft(), guiTop, x, y, partialTicks);
            drawRecipeTooltip(x, y);
        }
    }

    private void drawRecipeTooltip(int x, int y) {
        net.minecraft.item.ItemStack output = recipeBook.hoveredOutput(recipeLeft(), guiTop, x, y);
        if (output != null) renderToolTip(output, x, y);
        else {
            String control = recipeBook.hoveredControl(recipeLeft(), guiTop, x, y);
            if (control != null) drawHoveringText(java.util.Collections.singletonList(control), x, y, fontRendererObj);
        }
    }

    @Override protected void mouseClicked(int x, int y, int button) {
        if (recipeBook.isOpen() && recipeBook.mouseClicked(recipeLeft(), guiTop, x, y, button)) return;
        if (recipeBook.isOpen() && layout.overlay()) return;
        super.mouseClicked(x, y, button);
    }

    @Override protected void keyTyped(char character, int keyCode) {
        if (recipeBook.isOpen() && recipeBook.keyTyped(character, keyCode)) return;
        super.keyTyped(character, keyCode);
    }

    private int recipeLeft() {
        // Anchor to the actual guiLeft, including any compatibility adjustment
        // made by NEI/other GuiContainer hooks after initGui.
        return recipeBook.isOpen() && !layout.overlay() ? guiLeft - Inventory112Layout.RECIPE_WIDTH - 1
                : layout.recipeLeft();
    }

    List<Rectangle> neiOccupiedAreas() {
        List<Rectangle> areas = new ArrayList<Rectangle>(2);
        areas.add(new Rectangle(guiLeft, guiTop, xSize, ySize));
        if (recipeBook.isOpen())
            areas.add(new Rectangle(recipeLeft() - 28, guiTop,
                    Inventory112Layout.RECIPE_WIDTH + 28, ySize));
        return areas;
    }

    @Override protected void drawGuiContainerBackgroundLayer(float partialTicks, int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(INVENTORY);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        GuiInventory.func_147046_a(guiLeft + 51, guiTop + 75, 30,
                guiLeft + 51 - mouseX, guiTop + 25 - mouseY, mc.thePlayer);
    }

    @Override protected void drawGuiContainerForegroundLayer(int x, int y) {
        fontRendererObj.drawString(I18n.format("container.crafting", new Object[0]), 97, 8, 4210752);
    }
}
