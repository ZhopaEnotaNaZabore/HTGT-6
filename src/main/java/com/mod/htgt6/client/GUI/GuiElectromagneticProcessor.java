package com.mod.htgt6.client.GUI;

import codechicken.nei.recipe.GuiCraftingRecipe;
import com.mod.htgt6.api.hitech6.BaseMachines.GuiBaseMachine;
import com.mod.htgt6.common.TE.TEElectromagneticProcessor;
import com.mod.htgt6.common.inventory.ContainerElectromagneticProcessor;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import java.util.ArrayList;
import java.util.List;

public class GuiElectromagneticProcessor extends GuiBaseMachine {

    // Uses standard machine texture layout (176x166)
    public static final ResourceLocation TEXTURE = new ResourceLocation("htgt6", "textures/gui/em_processor.png");

    private final TEElectromagneticProcessor tileEM;

    // Fluid Slot UI Coordinates & Bounds (Slot 4: X=54, Y=63)
    private static final int FLUID_SLOT_X = 54;
    private static final int FLUID_SLOT_Y = 63;
    private static final int FLUID_SLOT_SIZE = 16;

    // Progress Arrow Coordinates - Aligned with Compressor (X: 78, Y: 23)
    private static final int PROGRESS_X = 61;
    private static final int PROGRESS_Y = 24;
    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;

    public GuiElectromagneticProcessor(InventoryPlayer playerInv, TEElectromagneticProcessor tile) {
        super(new ContainerElectromagneticProcessor(playerInv, tile), tile, TEXTURE);
        this.tileEM = tile;
        this.xSize = 176;
        this.ySize = 166;

        // Reposition info box ("i") matching compressor
        this.iconX = 158;
        this.iconY = 32;
    }

    @Override
    public String getGuiTitle() {
        return "Electromagnetic Processor";
    }

    @Override
    protected void drawExtraBackground(int guiLeft, int guiTop) {
        // Render Progress Arrow
        if (tileEM.maxProgress > 0 && tileEM.progress > 0) {
            int progressScaled = (tileEM.progress * PROGRESS_WIDTH) / tileEM.maxProgress;
            drawTexturedModalRect(
                    guiLeft + PROGRESS_X,
                    guiTop + PROGRESS_Y,
                    176, 0,
                    progressScaled, PROGRESS_HEIGHT
            );
        }
    }
    //176 0

    @Override
    protected void addDiagnosticsTooltip(List<String> tooltip) {
        super.addDiagnosticsTooltip(tooltip);

        // Machine Status
        tooltip.add(EnumChatFormatting.GRAY + "State: " + EnumChatFormatting.YELLOW + tileEM.state.name());

        // Tank Status inside Info Tooltip
        FluidTank output=tileEM.tanks[1];
        tooltip.add(EnumChatFormatting.GRAY+"Output: "+(output.getFluid()==null?"Empty":
                output.getFluid().getLocalizedName()+" ("+output.getFluidAmount()+"/"+output.getCapacity()+" mB)"));
        FluidTank tank = tileEM.tanks[0];
        if (tank != null && tank.getFluid() != null) {
            tooltip.add(EnumChatFormatting.GRAY + "Fluid: " + EnumChatFormatting.AQUA
                    + tank.getFluid().getLocalizedName() + " (" + tank.getFluidAmount() + "/" + tank.getCapacity() + " mB)");
        } else {
            tooltip.add(EnumChatFormatting.GRAY + "Fluid: " + EnumChatFormatting.DARK_GRAY + "Empty");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        // Show Fluid Tank hover tooltip when hovering specifically over the Fluid Input Slot (X=54, Y=63)
        int actualSlotX = guiLeft + FLUID_SLOT_X;
        int actualSlotY = guiTop + FLUID_SLOT_Y;

        if (mouseX >= actualSlotX && mouseX < actualSlotX + FLUID_SLOT_SIZE &&
                mouseY >= actualSlotY && mouseY < actualSlotY + FLUID_SLOT_SIZE) {

            List<String> fluidTooltip = new ArrayList<String>();
            FluidTank tank = tileEM.tanks[0];

            if (tank != null && tank.getFluid() != null && tank.getFluidAmount() > 0) {
                FluidStack fluid = tank.getFluid();
                fluidTooltip.add(EnumChatFormatting.WHITE + fluid.getLocalizedName());
                fluidTooltip.add(EnumChatFormatting.GRAY + "" + tank.getFluidAmount() + " / " + tank.getCapacity() + " mB");
            } else {
                fluidTooltip.add(EnumChatFormatting.GRAY + "Empty Tank");
                fluidTooltip.add(EnumChatFormatting.DARK_GRAY + "0 / " + TEElectromagneticProcessor.TANK_CAPACITY + " mB");
            }

            this.func_146283_a(fluidTooltip, mouseX, mouseY);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButton(BUTTON_NEI_ID, guiLeft + 148, guiTop + 4, 24, 12,
                EnumChatFormatting.YELLOW + "NEI"));
    }

    private void openNEIPage() {
        // Tries opening by NEI recipe handler string identifier first, falls back to machine item block
        if (!GuiCraftingRecipe.openRecipeGui("em_processor")) {
            ItemStack machineStack = new ItemStack(tile.getBlockType(), 1, tile.getBlockMetadata());
            GuiCraftingRecipe.openRecipeGui("item", machineStack);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == BUTTON_NEI_ID) {
            openNEIPage();
        }

    }

    private static final int BUTTON_NEI_ID = 100;
}
