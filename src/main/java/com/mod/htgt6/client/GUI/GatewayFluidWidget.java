package com.mod.htgt6.client.GUI;

import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widget.Widget;
import com.mod.htgt6.common.multiblock.gateway.TileEntityMultiblockGateway;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.network.PacketGatewayFluidTransfer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.util.StatCollector;

/** GT5 turbine-inspired tall tank display implemented entirely with HTGT6/ModularUI. */
@SideOnly(Side.CLIENT)
public final class GatewayFluidWidget extends Widget<GatewayFluidWidget> implements Interactable {
    private final TileEntityMultiblockGateway gateway;
    private final int tankIndex;

    public GatewayFluidWidget(TileEntityMultiblockGateway gateway) {
        this(gateway, 0);
    }

    public GatewayFluidWidget(TileEntityMultiblockGateway gateway, int tankIndex) {
        this.gateway = gateway;
        this.tankIndex = tankIndex;
        tooltip(this::buildTooltip);
    }

    @Override
    public void draw(GuiContext context) {
        FluidStack fluid = gateway.getDisplayedFluid(tankIndex);
        int capacity = gateway.getDisplayedFluidCapacity(tankIndex);
        if (fluid == null || fluid.amount <= 0 || capacity <= 0) return;
        int innerHeight = getArea().height - 4;
        int filledHeight = Math.max(1, Math.min(innerHeight,
                (int) ((long) innerHeight * fluid.amount / capacity)));
        GuiDraw.drawFluidTexture(fluid, 2, getArea().height - 2 - filledHeight,
                getArea().width - 4, filledHeight, 0.0F);
    }

    private void buildTooltip(Tooltip tooltip) {
        tooltip.setUpdateTooltipEveryTick(true);
        FluidStack fluid = gateway.getDisplayedFluid(tankIndex);
        if (fluid == null || fluid.amount <= 0) {
            tooltip.addLine("Empty");
        } else {
            tooltip.addLine(fluid.getLocalizedName());
            tooltip.addLine(fluid.amount + " / " + gateway.getDisplayedFluidCapacity(tankIndex) + " L");
        }
        tooltip.addLine(StatCollector.translateToLocal("ui.htgt6.gateway.fluid_transfer_hint"));
    }

    @Override
    public Interactable.Result onMouseTapped(int mouseButton) {
        if (mouseButton != 0 && mouseButton != 1) return Interactable.Result.IGNORE;
        HTGT6.network.sendToServer(new PacketGatewayFluidTransfer(gateway, tankIndex));
        return Interactable.Result.SUCCESS;
    }
}
