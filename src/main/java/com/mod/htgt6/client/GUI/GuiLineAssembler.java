package com.mod.htgt6.client.GUI;

import com.mod.htgt6.api.hitech6.BaseMachines.GuiBaseMachine;
import com.mod.htgt6.common.TE.LineAssemblerTE;
import com.mod.htgt6.common.inventory.ContainerLineAssembler;
import codechicken.nei.recipe.GuiCraftingRecipe;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import gregapi.data.FL;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class GuiLineAssembler extends GuiBaseMachine {

    private static final int BUTTON_NEI_ID = 100;

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("htgt6", "textures/gui/LineAssembler.png");

    private final LineAssemblerTE te;

    public GuiLineAssembler(ContainerLineAssembler container, LineAssemblerTE te) {
        super(container, te, TEXTURE);
        this.te = te;
        this.xSize = 176;
        this.ySize = 186; // Expanded Y size for larger grid
    }

    @Override
    public String getGuiTitle() {
        return "Line Assembler";
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(new GuiButton(BUTTON_NEI_ID, guiLeft + 148, guiTop + 4, 24, 12,
                EnumChatFormatting.YELLOW + "NEI"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == BUTTON_NEI_ID) {
            if (!GuiCraftingRecipe.openRecipeGui("line_assembler")) {
                GuiCraftingRecipe.openRecipeGui("item", new ItemStack(te.getBlockType(), 1, te.getBlockMetadata()));
            }
        }
    }

    @Override
    protected void addDiagnosticsTooltip(List<String> tooltip) {
        super.addDiagnosticsTooltip(tooltip); // Adds EU, Tier, Progress, Status

        // Display contents of the 4 fluid tanks inside the diagnostic tooltip
        tooltip.add("\u00a7e--- Tanks ---");
        for (int i = 0; i < te.tanks.length; i++) {
            if (te.tanks[i].getFluid() != null) {
                tooltip.add("\u00a77Tank " + (i + 1) + ": \u00a7f"
                        + te.tanks[i].getFluid().getLocalizedName() + " ("
                        + te.tanks[i].getFluidAmount() + " mB)");
            } else {
                tooltip.add("\u00a77Tank " + (i + 1) + ": \u00a78Empty");
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        for (int i = 0; i < te.tanks.length; i++) {
            FluidStack fluid = te.tanks[i].getFluid();
            if (fluid != null) {
                itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(),
                        FL.display(fluid, false, false), 88, 18 + i * 18);
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int relativeX = mouseX - guiLeft;
        int relativeY = mouseY - guiTop;
        for (int i = 0; i < te.tanks.length; i++) {
            if (relativeX < 88 || relativeX >= 106 || relativeY < 18 + i * 18 || relativeY >= 36 + i * 18) continue;
            FluidStack fluid = te.tanks[i].getFluid();
            List<String> tooltip = new java.util.ArrayList<String>();
            if (fluid == null) tooltip.add(EnumChatFormatting.GRAY + "Empty fluid slot");
            else {
                tooltip.add(fluid.getLocalizedName());
                tooltip.add(EnumChatFormatting.GRAY + "" + te.tanks[i].getFluidAmount()
                        + " / " + te.tanks[i].getCapacity() + " mB");
                tooltip.add(EnumChatFormatting.DARK_GRAY + "Left-click with a canister to transfer fluid");
            }
            drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj);
            return;
        }
    }
}
