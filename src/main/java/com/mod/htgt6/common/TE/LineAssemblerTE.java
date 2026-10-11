package com.mod.htgt6.common.TE;

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.common.handler.recipe.RM.LineAssemblerRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class LineAssemblerTE extends TileEntityBaseMachine implements IFluidHandler {
    public FluidTank[] tanks = new FluidTank[4];
    public static final int FLUID_SLOT_START = 17;
    public static final int FLUID_SLOT_COUNT = 4;

    private MachineRecipe currentRecipe;

    public LineAssemblerTE() {
        super(24);

        for(int i = 0; i < this.tanks.length; ++i) {
            this.tanks[i] = new FluidTank(128000);
        }

    }



    private byte facing = 2;
    public byte getFacing() {
        return facing;
    }
    public void setFacing(byte facing) {
        this.facing = facing;
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }



    public int getModuleSlot() {
        return 21;
    }

    public int getTransformerSlot() {
        return 22;
    }

    public String getInventoryName() {
        return "Line Assembler";
    }

    @Override
    protected void processMachineLogic() {
        // Handle mid-craft processing
        if (this.progress > 0) {
            // If currentRecipe was cleared on world reload, attempt to restore it
            if (this.currentRecipe == null) {
                // Option A: If base class stored active output item, match recipe by output
                if (this.inventory[16] != null) {
                    this.currentRecipe = findRecipeByOutput(this.inventory[16]);
                }

                // Option B: Fallback - if recipe cannot be matched, safely cancel without crashing
                if (this.currentRecipe == null) {
                    this.progress = 0;
                    this.markDirty();
                    return;
                }
            }

            long recipeEUt = LineAssemblerRecipeHandler.getOverclockedEUt(this.currentRecipe, this.machineTier);

            if (this.energy >= recipeEUt) {
                this.energy -= recipeEUt;
                ++this.progress;

                if (this.progress >= this.maxProgress) {
                    LineAssemblerRecipeHandler.INSTANCE.outputRecipe(this.currentRecipe, this.inventory, 16);
                    this.progress = 0;
                    this.currentRecipe = null;
                    this.markDirty();
                }
            }
        }
        // Handle idle searching
        else {
            // Slot 0 through 15 = Inputs (16 slots count), Output slot = 16
            MachineRecipe recipe = LineAssemblerRecipeHandler.INSTANCE.findRecipe(this.inventory, 0, 16, 16);

            if (recipe != null && this.machineTier >= recipe.minTier) {
                if (this.hasEnoughFluids(recipe)) {
                    // CONSUME INPUTS AT START
                    LineAssemblerRecipeHandler.INSTANCE.consumeInputs(recipe, this.inventory, 0, 16);
                    this.consumeFluids(recipe);

                    this.currentRecipe = recipe;
                    this.maxProgress = LineAssemblerRecipeHandler.getOverclockedDuration(recipe, this.machineTier);
                    this.progress = 1;
                    this.markDirty();
                }
            }
        }
    }

    /**
     * Helper to recover active recipe upon world reload using current output item target.
     */
    private MachineRecipe findRecipeByOutput(ItemStack outputStack) {
        for (MachineRecipe recipe : LineAssemblerRecipeHandler.INSTANCE.getRecipeList()) {
            if (recipe.outputs != null && recipe.outputs.length > 0 && recipe.outputs[0] != null) {
                if (recipe.outputs[0].isItemEqual(outputStack)) {
                    return recipe;
                }
            }
        }
        return null;
    }

    /**
     * Drains fluid items in slots 17-20 directly into the internal tanks.
     */
    private void drainFluidInputSlots() {
        for (int i = 0; i < FLUID_SLOT_COUNT; i++) {
            int slotIndex = FLUID_SLOT_START + i;
            ItemStack stack = this.inventory[slotIndex];

            if (stack == null) continue;

            // 1. Universal Canisters & IFluidContainerItem
            if (stack.getItem() instanceof IFluidContainerItem) {
                IFluidContainerItem container = (IFluidContainerItem) stack.getItem();
                FluidStack contained = container.getFluid(stack);

                if (contained != null && contained.amount > 0) {
                    // Check if tanks can accept this fluid
                    int accepted = this.fill(ForgeDirection.UNKNOWN, contained, false);

                    if (accepted > 0) {
                        // Handle unstacked vs stacked items safely
                        if (stack.stackSize == 1) {
                            FluidStack drained = container.drain(stack, accepted, true);
                            if (drained != null && drained.amount > 0) {
                                this.fill(ForgeDirection.UNKNOWN, drained, true);
                                this.markDirty();
                            }
                        } else {
                            // If stacked, only drain 1 item unit from the stack
                            ItemStack singleUnit = stack.copy();
                            singleUnit.stackSize = 1;

                            FluidStack drained = container.drain(singleUnit, accepted, true);
                            if (drained != null && drained.amount > 0) {
                                this.fill(ForgeDirection.UNKNOWN, drained, true);
                                stack.stackSize--; // reduce main stack by 1
                                this.markDirty();
                            }
                        }
                    }
                }
            }
            // 2. Standard Vanilla Buckets & Forge Fluid Containers
            else if (FluidContainerRegistry.isFilledContainer(stack) && stack.stackSize == 1) {
                FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (contained != null && this.fill(ForgeDirection.UNKNOWN, contained, false) == contained.amount) {
                    this.fill(ForgeDirection.UNKNOWN, contained, true);
                    this.inventory[slotIndex] = FluidContainerRegistry.drainFluidContainer(stack);
                    this.markDirty();
                }
            }
        }
    }

    private void pushToOutputSlot(int outSlot, ItemStack stackToPush) {
        if (this.inventory[outSlot] == null) {
            this.inventory[outSlot] = stackToPush;
        } else if (this.inventory[outSlot].isItemEqual(stackToPush)
                && ItemStack.areItemStackTagsEqual(this.inventory[outSlot], stackToPush)) {
            this.inventory[outSlot].stackSize += stackToPush.stackSize;
        }
    }

    // --- Rest of class methods (hasEnoughFluids, consumeFluids, fill, readFromNBT, writeToNBT) remain unchanged ---


    private boolean hasEnoughFluids(MachineRecipe recipe) {
        if (recipe.fluidInputs != null && recipe.fluidInputs.length != 0) {
            FluidStack[] var2 = recipe.fluidInputs;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                FluidStack required = var2[var4];
                if (required != null) {
                    boolean matched = false;
                    FluidTank[] var7 = this.tanks;
                    int var8 = var7.length;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        FluidTank tank = var7[var9];
                        if (tank.getFluid() != null && tank.getFluid().isFluidEqual(required) && tank.getFluidAmount() >= required.amount) {
                            matched = true;
                            break;
                        }
                    }

                    if (!matched) {
                        return false;
                    }
                }
            }

            return true;
        } else {
            return true;
        }
    }

    private void consumeFluids(MachineRecipe recipe) {
        if (recipe.fluidInputs != null) {
            FluidStack[] var2 = recipe.fluidInputs;
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                FluidStack required = var2[var4];
                if (required != null) {
                    FluidTank[] var6 = this.tanks;
                    int var7 = var6.length;

                    for(int var8 = 0; var8 < var7; ++var8) {
                        FluidTank tank = var6[var8];
                        if (tank.getFluid() != null && tank.getFluid().isFluidEqual(required)) {
                            tank.drain(required.amount, true);
                            break;
                        }
                    }
                }
            }

        }
    }

    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        } else {
            FluidTank[] var4 = this.tanks;
            int var5 = var4.length;

            int var6;
            FluidTank tank;
            int filled;
            for(var6 = 0; var6 < var5; ++var6) {
                tank = var4[var6];
                if (tank.getFluid() != null && tank.getFluid().isFluidEqual(resource)) {
                    filled = tank.fill(resource, doFill);
                    if (filled > 0 && doFill) {
                        this.markDirty();
                    }

                    return filled;
                }
            }

            var4 = this.tanks;
            var5 = var4.length;

            for(var6 = 0; var6 < var5; ++var6) {
                tank = var4[var6];
                if (tank.getFluid() == null) {
                    filled = tank.fill(resource, doFill);
                    if (filled > 0 && doFill) {
                        this.markDirty();
                    }

                    return filled;
                }
            }

            return 0;
        }
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        }

        // Find matching fluid tank and drain specified amount
        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() != null && tank.getFluid().isFluidEqual(resource)) {
                FluidStack drained = tank.drain(resource.amount, doDrain);
                if (drained != null && drained.amount > 0 && doDrain) {
                    this.markDirty();
                }
                return drained;
            }
        }
        return null;
    }
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }

        // Drain from the first non-empty tank found
        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() != null && tank.getFluidAmount() > 0) {
                FluidStack drained = tank.drain(maxDrain, doDrain);
                if (drained != null && drained.amount > 0 && doDrain) {
                    this.markDirty();
                }
                return drained;
            }
        }
        return null;
    }

    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        // Allow draining if any tank contains this fluid (or standard query if fluid is null)
        for (FluidTank tank : this.tanks) {
            if (tank.getFluid() != null && (fluid == null || tank.getFluid().getFluid() == fluid)) {
                return true;
            }
        }
        return false;
    }

    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidTankInfo[] infos = new FluidTankInfo[this.tanks.length];

        for(int i = 0; i < this.tanks.length; ++i) {
            infos[i] = this.tanks[i].getInfo();
        }

        return infos;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        // Restore fluid tanks
        NBTTagList tankList = nbt.getTagList("Tanks", 10);
        for(int i = 0; i < tankList.tagCount() && i < this.tanks.length; ++i) {
            this.tanks[i].readFromNBT(tankList.getCompoundTagAt(i));
        }

        // Re-locate recipe if machine was mid-craft when world was saved
        if (this.progress > 0) {
            if (nbt.hasKey("ActiveRecipeOutput")) {
                ItemStack savedOutput = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("ActiveRecipeOutput"));
                this.currentRecipe = findRecipeByOutput(savedOutput);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        // Save fluid tanks
        NBTTagList tankList = new NBTTagList();
        for (FluidTank tank : this.tanks) {
            NBTTagCompound tankTag = new NBTTagCompound();
            tank.writeToNBT(tankTag);
            tankList.appendTag(tankTag);
        }
        nbt.setTag("Tanks", tankList);

        // Save active recipe output tag if crafting is in progress
        if (this.progress > 0 && this.currentRecipe != null && this.currentRecipe.outputs.length > 0) {
            NBTTagCompound recipeTag = new NBTTagCompound();
            this.currentRecipe.outputs[0].writeToNBT(recipeTag);
            nbt.setTag("ActiveRecipeOutput", recipeTag);
        }
    }



    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }
}
