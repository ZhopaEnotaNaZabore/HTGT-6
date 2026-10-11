package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.mod.htgt6.api.hitech6.BaseMachines.GuiBaseMachine;
import com.mod.htgt6.common.TE.MetalFormerTE;
import com.mod.htgt6.common.inventory.ContainerMetalFormer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public class GuiMetalFormer extends GuiBaseMachine {

    public static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/gui/metal_former.png");
    private final MetalFormerTE tileMetalFormer;

    private static final int PROGRESS_X = 61;
    private static final int PROGRESS_Y = 24;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;
    private static final int BUTTON_NEI_ID = 100;

    public GuiMetalFormer(InventoryPlayer playerInv, MetalFormerTE tile) {
        super(new ContainerMetalFormer(playerInv, tile), tile, TEXTURE);
        this.tileMetalFormer = tile;
        this.xSize = 176;
        this.ySize = 166;

        this.iconX = 158;
        this.iconY = 32;
    }

    @Override
    public String getGuiTitle() {
        return "Metal Former";
    }

    @Override
    protected void drawExtraBackground(int guiLeft, int guiTop) {
        // Draw progress arrow animation
        if (tileMetalFormer.maxProgress > 0 && tileMetalFormer.progress > 0) {
            int progressScaled = (tileMetalFormer.progress * PROGRESS_WIDTH) / tileMetalFormer.maxProgress;
            drawTexturedModalRect(
                    guiLeft + PROGRESS_X,
                    guiTop + PROGRESS_Y,
                    176, 0,
                    progressScaled, PROGRESS_HEIGHT
            );
        }
    }

    @Override
    protected void addDiagnosticsTooltip(List<String> tooltip) {
        super.addDiagnosticsTooltip(tooltip);
        tooltip.add(EnumChatFormatting.GRAY + "State: " + EnumChatFormatting.YELLOW + tileMetalFormer.state.name());

        // Display current active mold shape in the tooltip if present
        ItemStack moldStack = tileMetalFormer.getStackInSlot(MetalFormerTE.SLOT_MOLD);
        if (moldStack != null) {
            tooltip.add(EnumChatFormatting.GRAY + "Shape Mold: " + EnumChatFormatting.GREEN + moldStack.getDisplayName());
        } else {
            tooltip.add(EnumChatFormatting.GRAY + "Shape Mold: " + EnumChatFormatting.RED + "Missing Mold");
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // Moved to the top-right angle/corner of the GUI frame and set label to yellow "NEI"
        this.buttonList.add(new GuiButton(
                BUTTON_NEI_ID,
                guiLeft + 148,
                guiTop + 4,
                24,
                12,
                EnumChatFormatting.YELLOW + "NEI"
        ));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == BUTTON_NEI_ID) {
            if (!GuiCraftingRecipe.openRecipeGui("metal_former")) {
                ItemStack machineStack = new ItemStack(tile.getBlockType(), 1, tile.getBlockMetadata());
                GuiCraftingRecipe.openRecipeGui("item", machineStack);
            }
        }
    }
}
