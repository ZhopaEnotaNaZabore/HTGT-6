package com.mod.htgt6.api.hitech6.multiblock;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;

/**
 * FluidTank whose effective Forge capacity follows a multiblock controller tier.
 * Existing contents are never discarded when the tier is lowered.
 */
public final class TieredFluidTank extends FluidTank {
    private final MultiTileEntityBase controller;
    private final GatewayType gatewayType;

    public TieredFluidTank(MultiTileEntityBase controller, GatewayType gatewayType) {
        super(toForgeCapacity(controller == null ? 0L : controller.getGatewayFluidCapacity(gatewayType)));
        if (controller == null) throw new IllegalArgumentException("Controller cannot be null");
        if (gatewayType != GatewayType.FLUID_INPUT && gatewayType != GatewayType.FLUID_OUTPUT) {
            throw new IllegalArgumentException("TieredFluidTank requires a fluid gateway type");
        }
        this.controller = controller;
        this.gatewayType = gatewayType;
        refreshCapacity();
    }

    public void refreshCapacity() {
        setCapacity(toForgeCapacity(controller.getGatewayFluidCapacity(gatewayType)));
    }

    @Override public int fill(FluidStack resource, boolean doFill) {
        refreshCapacity();
        return super.fill(resource, doFill);
    }

    @Override public int getCapacity() {
        refreshCapacity();
        return super.getCapacity();
    }

    @Override public FluidTankInfo getInfo() {
        refreshCapacity();
        return super.getInfo();
    }

    @Override public FluidTank readFromNBT(NBTTagCompound nbt) {
        FluidTank result = super.readFromNBT(nbt);
        refreshCapacity();
        return result;
    }

    private static int toForgeCapacity(long capacity) {
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, capacity));
    }
}
