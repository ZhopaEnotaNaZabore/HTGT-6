package com.mod.htgt6.common.TE;

import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
import cpw.mods.fml.common.Optional;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.HashMap;
import java.util.Map;

@Optional.InterfaceList({
        @Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2")
})
public class UniversalGasTurbineTE
        extends TileEntity
        implements IInventory, IEnergySource, IFluidHandler {

    // ==========================================
    // GAS FUEL REGISTRY
    // ==========================================

    public static final Map<String, Integer> VALID_GAS_FUELS = new HashMap<String, Integer>();

    public static void addGasFuel(String fluidName, int euPerMb) {
        VALID_GAS_FUELS.put(fluidName, euPerMb);
    }

    // ==========================================
    // INVENTORY
    // ==========================================

    public ItemStack[] inventory = new ItemStack[4];

    // ==========================================
    // FLUID TANK (128,000 mB)
    // ==========================================

    public FluidTank fluidTank = new FluidTank(128000);

    // ==========================================
    // ENERGY
    // ==========================================

    public double energy = 0;
    public double maxEnergy = 1000000;

    // ==========================================
    // MACHINE
    // ==========================================

    public int machineTier = 1;
    public boolean enabled = true;

    // ==========================================
    // IC2 ENERGY NET
    // ==========================================

    private boolean addedToEnergyNet = false;

    // ==========================================
    // VOLTAGES
    // ==========================================

    public static final int[] VOLTAGES = {
            32, 128, 512, 2048, 8192, 32768, 131072,
            524288, 2097152, 8388608, 33554432,
            134217728, 536870912, Integer.MAX_VALUE
    };

    // ==========================================
    // UPDATE LOGIC
    // ==========================================

    @Override
    public void updateEntity() {
        super.updateEntity();

        updateMachineTier();

        if (worldObj.isRemote) return;

        processFluidCells();


        if (!enabled) return;

        generateEnergy();


    }

    private void processFluidCells() {
        if (inventory[0] != null) {
            ItemStack inputCell = inventory[0];

            if (FluidContainerRegistry.isFilledContainer(inputCell)) {
                FluidStack fluidInCell = FluidContainerRegistry.getFluidForFilledItem(inputCell);

                if (fluidInCell != null && VALID_GAS_FUELS.containsKey(fluidInCell.getFluid().getName())) {
                    int capacity = fluidTank.getCapacity() - fluidTank.getFluidAmount();

                    if (capacity >= fluidInCell.amount) {
                        ItemStack emptyCell = FluidContainerRegistry.drainFluidContainer(inputCell);

                        if (inventory[1] == null || (inventory[1].isItemEqual(emptyCell) && inventory[1].stackSize < inventory[1].getMaxStackSize())) {

                            fluidTank.fill(fluidInCell, true);

                            inventory[0].stackSize--;
                            if (inventory[0].stackSize <= 0) {
                                inventory[0] = null;
                            }

                            if (inventory[1] == null) {
                                inventory[1] = emptyCell.copy();
                            } else {
                                inventory[1].stackSize++;
                            }

                            markDirty();
                        }
                    }
                }
            }
        }
    }

    private void generateEnergy() {
        // 1. Basic check: If the tank is completely empty, do nothing
        if (fluidTank.getFluid() == null || fluidTank.getFluidAmount() <= 0) return;

        // 2. Fuel validation check
        String fluidName = fluidTank.getFluid().getFluid().getName();
        if (!VALID_GAS_FUELS.containsKey(fluidName)) return;

        int euPerMb = VALID_GAS_FUELS.get(fluidName);
        int targetEU = getOutputVoltage(); // Maximum power for current tier (e.g., 512 EU/t)

        // 3. Calculate how many mB are needed for a full, maximum power tick
        int mbNeeded = (int) Math.ceil((double) targetEU / euPerMb);

        int mbToDrain = 0;
        int euToGenerate = 0;

        // 4. Determine if we have enough fluid for a full tick or a partial tick
        if (fluidTank.getFluidAmount() >= mbNeeded) {
            // We have plenty of fuel: drain the standard amount and make full target power
            mbToDrain = mbNeeded;
            euToGenerate = targetEU;
        } else {
            // We are short on fuel: burn everything left in the tank and scale down EU
            mbToDrain = fluidTank.getFluidAmount();
            euToGenerate = mbToDrain * euPerMb;
        }

        // 5. Safety Guard: Only proceed if there is enough space in our internal buffer
        if (energy + euToGenerate > maxEnergy) {
            return;
        }

        // 6. Execute the drain and add the precisely scaled energy
        fluidTank.drain(mbToDrain, true);
        energy += euToGenerate;
        markDirty();
    }
    private boolean isEnergyStorageNearby() {
        if (this.worldObj == null) return false;

        // Loop through North, South, East, West, Up, and Down
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity adjacentTE = this.worldObj.getTileEntity(
                    this.xCoord + dir.offsetX,
                    this.yCoord + dir.offsetY,
                    this.zCoord + dir.offsetZ
            );

            // Check if the neighbor is an IC2 Energy Storage block (BatBox, MFE, MFSU)
            if (adjacentTE instanceof ic2.api.tile.IEnergyStorage) {
                return true;
            }
        }
        return false;
    }

    // ==========================================
    // IC2 ENERGY SOURCE INTERFACE
    // ==========================================

    @Override
    public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getOfferedEnergy() {
        // If an energy storage block is directly touching this turbine, stop emitting energy
        if (isEnergyStorageNearby()) {
            return 0;
        }
        return Math.min(energy, getOutputVoltage());
    }
    @Override
    public void drawEnergy(double amount) {
        energy -= amount;
        markDirty();
    }

    @Override
    public int getSourceTier() {
        return machineTier;
    }

    // ==========================================
    // IC2 LOAD & UNLOAD
    // ==========================================

    @Override
    public void validate() {
        super.validate();
        if (!worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
    }

    @Override
    public void invalidate() {
        if (!worldObj.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
        addedToEnergyNet = false;
        super.invalidate();
    }

    // ==========================================
    // MACHINE TIER
    // ==========================================

    private void updateMachineTier() {
        machineTier = 1;

        ItemStack transformer = inventory[3];
        ItemStack module = inventory[2];

        if (transformer == null || module == null) return;

        String transformerName = transformer.getUnlocalizedName().toLowerCase();
        Item item = module.getItem();

        int moduleTier = 1;
        if (item == Tmodules.TierModuleMV) moduleTier = 2;
        else if (item == Tmodules.TierModuleHV) moduleTier = 3;
        else if (item == Tmodules.TierModuleEV) moduleTier = 4;
        else if (item == Tmodules.TierModuleIV) moduleTier = 5;
        else if (item == Tmodules.TierModuleLuV) moduleTier = 6;
        else if (item == Tmodules.TierModuleZPM) moduleTier = 7;
        else if (item == Tmodules.TierModuleUV) moduleTier = 8;
        else if (item == Tmodules.TierModulePUV1) moduleTier = 9;
        else if (item == Tmodules.TierModuleUX) moduleTier = 10;
        else if (item == Tmodules.TierModuleOLV) moduleTier = 11;
        else if (item == Tmodules.TierModuleOMV) moduleTier = 12;
        else if (item == Tmodules.TierModuleOHV) moduleTier = 13;
        else if (item == Tmodules.TierModuleOEV) moduleTier = 14;
        else if (item == Tmodules.TierModuleOIV) moduleTier = 15;
        else if (item == Tmodules.TierModuleMAX) moduleTier = 16;

        int maxTier = 1;
        if (transformerName.contains("lowleveltransformer")) maxTier = 4;
        else if (transformerName.contains("mediumleveltransformer")) maxTier = 6;
        else if (transformerName.contains("highleveltransformer")) maxTier = 8;
        else if (transformerName.contains("ultimateleveltransformer")) maxTier = 15;
        else if (transformerName.contains("omegatransformer")) maxTier = 16;

        if (moduleTier <= maxTier) {
            machineTier = moduleTier;
        } else {
            machineTier = 1;
        }
    }

    public int getOutputVoltage() {
        int index = machineTier - 1;
        if (index < 0) index = 0;
        if (index >= VOLTAGES.length) index = VOLTAGES.length - 1;
        return VOLTAGES[index];
    }

    // ==========================================
    // FORGE FLUID HANDLER
    // ==========================================

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || !VALID_GAS_FUELS.containsKey(resource.getFluid().getName())) {
            return 0;
        }
        return fluidTank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || !resource.isFluidEqual(fluidTank.getFluid())) {
            return null;
        }
        return fluidTank.drain(resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return fluidTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluid != null && VALID_GAS_FUELS.containsKey(fluid.getName());
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { fluidTank.getInfo() };
    }

    // ==========================================
    // IINVENTORY
    // ==========================================

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            ItemStack itemstack;
            if (inventory[slot].stackSize <= amount) {
                itemstack = inventory[slot];
                inventory[slot] = null;
                markDirty();
                return itemstack;
            }
            itemstack = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize <= 0) {
                inventory[slot] = null;
            }
            markDirty();
            return itemstack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (inventory[slot] != null) {
            ItemStack stack = inventory[slot];
            inventory[slot] = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "UniversalGasTurbine";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    // ==========================================
    // READ / WRITE NBT
    // ==========================================

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getDouble("Energy");
        machineTier = nbt.getInteger("MachineTier");
        enabled = nbt.getBoolean("Enabled");

        fluidTank.readFromNBT(nbt);

        NBTTagList items = nbt.getTagList("Inventory", 10);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setDouble("Energy", energy);
        nbt.setInteger("MachineTier", machineTier);
        nbt.setBoolean("Enabled", enabled);

        fluidTank.writeToNBT(nbt);

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }
        nbt.setTag("Inventory", items);
    }
}