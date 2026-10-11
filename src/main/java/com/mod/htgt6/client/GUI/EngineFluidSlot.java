package com.mod.htgt6.client.GUI;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.UniversalEngineTE;
import com.mod.htgt6.common.network.PacketEngineFluidTransfer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.FluidStack;

@SideOnly(Side.CLIENT)
public class EngineFluidSlot extends Widget<EngineFluidSlot> implements Interactable {
    private final UniversalEngineTE engine;
    private FluidSlotSyncHandler fluidSync;
    public EngineFluidSlot(UniversalEngineTE engine) { this.engine = engine; tooltip(this::buildTooltip); }
    @Override public boolean isValidSyncHandler(SyncHandler handler) { if (!(handler instanceof FluidSlotSyncHandler)) return false; fluidSync = (FluidSlotSyncHandler) handler; return true; }
    @Override public void draw(GuiContext context) {
        FluidStack fluid = fluidSync == null ? null : fluidSync.getValue();
        if (fluid != null && fluid.amount > 0) GuiDraw.drawFluidTexture(fluid, 2, 2, getArea().width - 4, getArea().height - 4, 0F);
    }
    private void buildTooltip(Tooltip tooltip) {
        tooltip.setUpdateTooltipEveryTick(true);
        FluidStack fluid = fluidSync == null ? null : fluidSync.getValue();
        tooltip.addLine(fluid == null ? "Fuel tank: empty" : fluid.getLocalizedName());
        if (fluid != null) tooltip.addLine("Amount: " + fluid.amount + " / " + engine.fluidTank.getCapacity() + " mB");
        tooltip.addLine("Click with a canister to fill or drain");
    }
    @Override public Interactable.Result onMouseTapped(int mouseButton) {
        if (mouseButton == 0 || mouseButton == 1) { HTGT6.network.sendToServer(new PacketEngineFluidTransfer(engine)); return Interactable.Result.SUCCESS; }
        return Interactable.Result.IGNORE;
    }
}
