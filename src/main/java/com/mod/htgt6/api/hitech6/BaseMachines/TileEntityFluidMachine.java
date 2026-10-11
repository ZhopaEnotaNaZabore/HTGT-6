package com.mod.htgt6.api.hitech6.BaseMachines;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public abstract class TileEntityFluidMachine extends TileEntityBaseMachine implements IFluidHandler {

    public FluidTank[] tanks;

    public TileEntityFluidMachine(int inventorySize, int tankCapacity) {
        this(inventorySize, 1, tankCapacity);
    }

    public TileEntityFluidMachine(int inventorySize, int tankCount, int tankCapacity) {
        super(inventorySize);
        this.tanks = new FluidTank[tankCount];
        for (int i = 0; i < tankCount; i++) {
            this.tanks[i] = new FluidTank(tankCapacity);
        }
    }

    public abstract int getFluidSlotStart();
    public abstract int getFluidSlotCount();

    protected void drainFluidInputSlots() {
        int start = getFluidSlotStart();
        int count = getFluidSlotCount();

        if (start < 0 || count <= 0) return;

        for (int i = 0; i < count; i++) {
            int slotIndex = start + i;
            ItemStack stack = this.inventory[slotIndex];

            if (stack == null) continue;

            if (stack.getItem() instanceof IFluidContainerItem) {
                IFluidContainerItem container = (IFluidContainerItem) stack.getItem();
                FluidStack contained = container.getFluid(stack);

                if (contained != null && contained.amount > 0) {
                    int accepted = this.fill(ForgeDirection.UNKNOWN, contained, false);

                    if (accepted > 0) {
                        if (stack.stackSize == 1) {
                            FluidStack drained = container.drain(stack, accepted, true);
                            if (drained != null && drained.amount > 0) {
                                this.fill(ForgeDirection.UNKNOWN, drained, true);
                                this.markDirty();
                            }
                        } else {
                            // A single slot cannot hold both full and empty containers. Transform
                            // a homogeneous batch atomically, otherwise leave it for cursor transfer.
                            long total = (long) contained.amount * stack.stackSize;
                            if (total > Integer.MAX_VALUE) continue;
                            FluidStack batch = contained.copy();
                            batch.amount = (int) total;
                            if (this.fill(ForgeDirection.UNKNOWN, batch, false) != batch.amount) continue;
                            ItemStack singleUnit = stack.copy();
                            singleUnit.stackSize = 1;
                            FluidStack drained = container.drain(singleUnit, contained.amount, true);
                            if (drained == null || drained.amount != contained.amount) continue;
                            singleUnit.stackSize = stack.stackSize;
                            this.fill(ForgeDirection.UNKNOWN, batch, true);
                            this.inventory[slotIndex] = singleUnit;
                            this.markDirty();
                        }
                    }
                }
            } else if (FluidContainerRegistry.isFilledContainer(stack) && stack.stackSize == 1) {
                FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (contained != null && this.fill(ForgeDirection.UNKNOWN, contained, false) == contained.amount) {
                    this.fill(ForgeDirection.UNKNOWN, contained, true);
                    this.inventory[slotIndex] = FluidContainerRegistry.drainFluidContainer(stack);
                    this.markDirty();
                }
            }
        }
    }

    public boolean hasEnoughFluids(FluidStack[] requiredFluids) {
        if (requiredFluids == null || requiredFluids.length == 0) return true;

        for (FluidStack required : requiredFluids) {
            if (required == null) continue;
            int totalRequired = 0;
            for (FluidStack other : requiredFluids) {
                if (other != null && other.isFluidEqual(required)) totalRequired += other.amount;
            }
            int available = 0;
            for (FluidTank tank : this.tanks) {
                if (tank.getFluid() != null && tank.getFluid().isFluidEqual(required)) available += tank.getFluidAmount();
            }
            if (available < totalRequired) return false;
        }
        return true;
    }

    public void consumeFluids(FluidStack[] requiredFluids) {
        if (requiredFluids == null) return;

        for (FluidStack required : requiredFluids) {
            if (required == null) continue;
            int remaining = required.amount;
            for (FluidTank tank : this.tanks) {
                if (remaining > 0 && tank.getFluid() != null && tank.getFluid().isFluidEqual(required)) {
                    FluidStack drained = tank.drain(remaining, true);
                    if (drained != null) remaining -= drained.amount;
                }
            }
        }
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null) return 0;

        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() != null && tank.getFluid().isFluidEqual(resource)) {
                int filled = tank.fill(resource, doFill);
                if (filled > 0 && doFill) this.markDirty();
                return filled;
            }
        }

        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() == null) {
                int filled = tank.fill(resource, doFill);
                if (filled > 0 && doFill) this.markDirty();
                return filled;
            }
        }

        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null) return null;

        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() != null && tank.getFluid().isFluidEqual(resource)) {
                FluidStack drained = tank.drain(resource.amount, doDrain);
                if (drained != null && drained.amount > 0 && doDrain) this.markDirty();
                return drained;
            }
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) return null;

        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() != null && tank.getFluidAmount() > 0) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null && drained.amount > 0 && doDrain) this.markDirty();
                return drained;
            }
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) { return true; }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() != null && (fluid == null || tank.getFluid().getFluid() == fluid)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidTankInfo[] infos = new FluidTankInfo[this.tanks.length];
        for (int i = 0; i < this.tanks.length; i++) {
            infos[i] = this.tanks[i].getInfo();
        }
        return infos;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList tankList = nbt.getTagList("Tanks", 10);
        for (int i = 0; i < tankList.tagCount() && i < this.tanks.length; i++) {
            this.tanks[i].readFromNBT(tankList.getCompoundTagAt(i));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList tankList = new NBTTagList();
        for (FluidTank tank : this.tanks) {
            NBTTagCompound tankTag = new NBTTagCompound();
            tank.writeToNBT(tankTag);
            tankList.appendTag(tankTag);
        }
        nbt.setTag("Tanks", tankList);
    }
}
