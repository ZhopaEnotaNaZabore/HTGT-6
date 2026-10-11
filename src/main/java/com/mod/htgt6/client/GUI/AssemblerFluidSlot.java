package com.mod.htgt6.client.GUI;

import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.Tooltip;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.mod.htgt6.HTGT6;
import com.mod.htgt6.common.TE.TileEntityAssembler;
import com.mod.htgt6.common.network.PacketFluidMachineTransfer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.fluids.FluidStack;

/** Visual and interactive ModularUI tank for the standalone assembler. */
@SideOnly(Side.CLIENT)
public class AssemblerFluidSlot extends Widget<AssemblerFluidSlot> implements Interactable {
    private final TileEntityAssembler assembler;
    private FluidSlotSyncHandler fluidSync;

    public AssemblerFluidSlot(TileEntityAssembler assembler) {
        this.assembler = assembler;
        tooltip(this::buildTooltip);
    }

    @Override
    public boolean isValidSyncHandler(SyncHandler handler) {
        if (!(handler instanceof FluidSlotSyncHandler)) return false;
        fluidSync = (FluidSlotSyncHandler) handler;
        return true;
    }

    @Override
    public void draw(GuiContext context) {
        FluidStack fluid = getDisplayedFluid();
        if (fluid == null || fluid.amount <= 0) return;
        int innerHeight = getArea().height - 4;
        int filledHeight = Math.max(1, innerHeight * fluid.amount / assembler.tanks[0].getCapacity());
        GuiDraw.drawFluidTexture(fluid, 2, getArea().height - 2 - filledHeight,
                getArea().width - 4, filledHeight, 0.0F);
    }

    private void buildTooltip(Tooltip tooltip) {
        tooltip.setUpdateTooltipEveryTick(true);
        FluidStack fluid = getDisplayedFluid();
        if (fluid == null || fluid.amount <= 0) {
            tooltip.addLine("Assembler tank: empty");
        } else {
            tooltip.addLine(fluid.getLocalizedName());
            tooltip.addLine("Amount: " + fluid.amount + " / " + assembler.tanks[0].getCapacity() + " mB");
        }
        tooltip.addLine("Click with a canister to fill or drain");
    }

    /** The handler owns the authoritative client-side copy received from the server. */
    private FluidStack getDisplayedFluid() {
        return fluidSync == null ? null : fluidSync.getValue();
    }

    @Override
    public Interactable.Result onMouseTapped(int mouseButton) {
        if (mouseButton == 0 || mouseButton == 1) {
            HTGT6.network.sendToServer(new PacketFluidMachineTransfer(assembler));
            return Interactable.Result.SUCCESS;
        }
        return Interactable.Result.IGNORE;
    }
}
