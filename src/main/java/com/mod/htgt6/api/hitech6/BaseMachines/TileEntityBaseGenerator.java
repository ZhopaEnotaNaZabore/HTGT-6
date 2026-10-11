package com.mod.htgt6.api.hitech6.BaseMachines;

import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;

import cpw.mods.fml.common.Optional;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Shared foundation for HiTech6 generators.
 *
 * It owns the IC2 source lifecycle, buffered energy, tier voltage, inventory
 * storage and their NBT representation. Concrete generators only implement
 * their fuel/heat conversion and any specialised UI or fluid behaviour.
 */
@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySource", modid = "IC2")
public abstract class TileEntityBaseGenerator extends TileEntity implements IInventory, IEnergySource {

    /**
     * Generator voltage ladder. Values after tier 13 exceed {@code int}; use
     * long end-to-end so OHV, OEV, OIV and MAX retain their ×4 progression.
     */
    public static final long[] VOLTAGES = MachineTierSystem.VOLTAGES;

    public ItemStack[] inventory;
    public double energy;
    public double maxEnergy;
    public int machineTier = 1;
    public boolean enabled = true;

    private boolean addedToEnergyNet;

    protected TileEntityBaseGenerator(int inventorySize, double maxEnergy) {
        this.inventory = new ItemStack[inventorySize];
        this.maxEnergy = maxEnergy;
    }

    public long getOutputVoltage() {
        return MachineTierSystem.voltage(machineTier);
    }

    /** Adds energy without overflowing the buffer and returns the accepted EU. */
    protected double addGeneratedEnergy(double amount) {
        if (amount <= 0D || Double.isNaN(amount) || Double.isInfinite(amount)) return 0D;
        double accepted = Math.min(amount, Math.max(0D, maxEnergy - energy));
        energy += accepted;
        if (accepted > 0) markDirty();
        return accepted;
    }

    @Override
    public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getOfferedEnergy() {
        return Math.min(energy, getOutputVoltage());
    }

    @Override
    public void drawEnergy(double amount) {
        if (amount <= 0D || Double.isNaN(amount) || Double.isInfinite(amount)) return;
        energy = Math.max(0D, energy - amount);
        markDirty();
    }

    @Override
    public int getSourceTier() {
        return machineTier;
    }

    @Override
    public void validate() {
        super.validate();
        if (worldObj != null && !worldObj.isRemote) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }
    }

    @Override
    public void onChunkUnload() {
        if (worldObj != null && !worldObj.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
        addedToEnergyNet = false;
        super.onChunkUnload();
    }

    @Override
    public void invalidate() {
        if (worldObj != null && !worldObj.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
        }
        addedToEnergyNet = false;
        super.invalidate();
    }

    @Override public int getSizeInventory() { return inventory.length; }
    @Override public ItemStack getStackInSlot(int slot) { return inventory[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = inventory[slot];
        if (stack == null) return null;
        if (stack.stackSize <= amount) {
            inventory[slot] = null;
        } else {
            stack = stack.splitStack(amount);
            if (inventory[slot].stackSize <= 0) inventory[slot] = null;
        }
        markDirty();
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getDouble("Energy");
        energy = Double.isNaN(energy) || Double.isInfinite(energy) ? 0D
                : Math.max(0D, Math.min(maxEnergy, energy));
        machineTier = nbt.getInteger("MachineTier");
        enabled = !nbt.hasKey("Enabled") || nbt.getBoolean("Enabled");

        NBTTagList items = nbt.getTagList("Inventory", 10);
        inventory = new ItemStack[inventory.length];
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

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }
        nbt.setTag("Inventory", items);
    }
}
