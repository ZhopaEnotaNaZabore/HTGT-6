package com.mod.htgt6.api.hitech6.BaseMachines;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Generator API extension for machines powered by one fluid tank.
 *
 * `TileEntityFluidMachine` cannot be a direct parent here because it is an
 * IC2 energy sink. This class provides the same Forge-fluid foundation while
 * preserving the mutually exclusive IC2 energy-source role of generators.
 */
public abstract class TileEntityFluidGenerator extends TileEntityBaseGenerator implements IFluidHandler {

    public final FluidTank fluidTank;

    protected TileEntityFluidGenerator(int inventorySize, int tankCapacity, double maxEnergy) {
        super(inventorySize, maxEnergy);
        this.fluidTank = new FluidTank(tankCapacity);
    }

    /** Override to constrain a generator to its registered fuel fluids. */
    protected boolean isFluidValid(FluidStack resource) {
        return resource != null && resource.getFluid() != null;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (!isFluidValid(resource)) return 0;
        int filled = fluidTank.fill(resource, doFill);
        if (filled > 0 && doFill) markDirty();
        return filled;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || !resource.isFluidEqual(fluidTank.getFluid())) return null;
        FluidStack drained = fluidTank.drain(resource.amount, doDrain);
        if (drained != null && doDrain) markDirty();
        return drained;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        FluidStack drained = fluidTank.drain(maxDrain, doDrain);
        if (drained != null && doDrain) markDirty();
        return drained;
    }

    @Override public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluid != null && isFluidValid(new FluidStack(fluid, 1));
    }
    @Override public boolean canDrain(ForgeDirection from, Fluid fluid) { return true; }
    @Override public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { fluidTank.getInfo() };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        fluidTank.setFluid(null);
        // Accept turbine saves written before the shared base existed.
        if (nbt.hasKey("FluidTank", 10)) {
            fluidTank.readFromNBT(nbt.getCompoundTag("FluidTank"));
        } else {
            fluidTank.readFromNBT(nbt);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagCompound tankTag = new NBTTagCompound();
        fluidTank.writeToNBT(tankTag);
        nbt.setTag("FluidTank", tankTag);
    }
}
