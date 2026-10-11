package com.mod.htgt6.client.GUI;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.UniversalGasTurbineTE;
import com.mod.htgt6.common.network.PacketTurbineFluidTransfer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.FluidStack;

/**
 * ModularUI 2.0.8 exposes the FluidSlot interaction API but ships an empty
 * default container-transfer handler.  The turbine uses this client widget to
 * send a validated transfer request to its server-side tank.
 */
@SideOnly(Side.CLIENT)
public class TurbineFluidSlot extends Widget<TurbineFluidSlot> implements Interactable {
    private final UniversalGasTurbineTE turbine;
    private FluidSlotSyncHandler fluidSync;

    public TurbineFluidSlot(UniversalGasTurbineTE turbine) {
        this.turbine = turbine;
        tooltip(tooltip -> buildFluidTooltip(tooltip));
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler handler) {
        if (!(handler instanceof FluidSlotSyncHandler)) return false;
        fluidSync = (FluidSlotSyncHandler) handler;
        return true;
    }

    @Override
    public void draw(GuiContext context) {
        // ModularUI owns a separate client snapshot, not the TileEntity tank.
        FluidStack fluid = fluidSync == null ? null : fluidSync.getValue();
        if (fluid != null && fluid.amount > 0) {
            GuiDraw.drawFluidTexture(fluid, 2, 2,
                    getArea().width - 4, getArea().height - 4, 0.0F);
        }
    }

    private void buildFluidTooltip(Tooltip tooltip) {
        tooltip.setUpdateTooltipEveryTick(true);
        FluidStack fluid = fluidSync == null ? null : fluidSync.getValue();
        if (fluid == null || fluid.amount <= 0) {
            tooltip.addLine("Fuel tank: empty");
        } else {
            tooltip.addLine(fluid.getLocalizedName());
            tooltip.addLine("Amount: " + fluid.amount + " / "
                    + turbine.fluidTank.getCapacity() + " mB");
        }
        tooltip.addLine("Click with a canister to fill or drain");
    }

    @Override
    public Interactable.Result onMouseTapped(int mouseButton) {
        if (mouseButton == 0 || mouseButton == 1) {
            HTGT6.network.sendToServer(new PacketTurbineFluidTransfer(turbine));
            return Interactable.Result.SUCCESS;
        }
        return Interactable.Result.IGNORE;
    }
}
