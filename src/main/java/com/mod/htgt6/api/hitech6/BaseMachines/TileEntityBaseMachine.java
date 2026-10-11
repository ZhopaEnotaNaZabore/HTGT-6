package com.mod.htgt6.api.hitech6.BaseMachines;


import com.mod.htgt6.api.hitech6.Misc.MachineTierSystem;
import cpw.mods.fml.common.Optional;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

@Optional.Interface(iface = "ic2.api.energy.tile.IEnergySink", modid = "IC2")
public abstract class TileEntityBaseMachine extends TileEntity implements IInventory, IEnergySink {

    public static final long[] VOLTAGES = MachineTierSystem.VOLTAGES;

    public ItemStack[] inventory;
//    public int energy = 0;
  //  public int maxEnergy = 1100000000;
    public long energy = 0L;
    public long maxEnergy = 1_100_000_000_000_000_000L;
    public int machineTier = 1;
    public boolean enabled = true;
    public int progress = 0;
    public int maxProgress = 200;

    private boolean addedToEnergyNet = false;

    public TileEntityBaseMachine(int inventorySize) {
        this.inventory = new ItemStack[inventorySize];
    }

    /** Slot index containing the Module item */
    public abstract int getModuleSlot();
    /** Slot index containing the Transformer item */
    public abstract int getTransformerSlot();

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;
        updateMachineTier();
        if (!enabled) return;

        processMachineLogic();
    }

    /** Override this method in child TE to implement custom recipe/processing behavior */
    protected abstract void processMachineLogic();

    // ==========================================
    // TIER & EXPLOSION MECHANICS
    // ==========================================
    public void updateMachineTier() {
        int mSlot = getModuleSlot();
        int tSlot = getTransformerSlot();

        if (mSlot < 0 || tSlot < 0 || mSlot >= getSizeInventory() || tSlot >= getSizeInventory()) {
            this.machineTier = 1;
            return;
        }

        ItemStack transformer = inventory[tSlot];
        ItemStack module = inventory[mSlot];

        if (transformer == null || module == null) {
            this.machineTier = 1;
            return;
        }

        this.machineTier = MachineTierSystem.resolveMachineTier(module, transformer);
    }

    public long getMaxInputVoltage() {
        return MachineTierSystem.voltage(machineTier);
    }

    public void explodeMachine() {
        if (worldObj != null && !worldObj.isRemote) {
            float power = 2F + (machineTier * 0.4F);
            worldObj.createExplosion(null, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, power, true);
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }
    }

    // ==========================================
    // IC2 INTEGRATION
    // ==========================================
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

    @Override
    public double injectEnergy(ForgeDirection direction, double amount, double voltage) {
        if (!enabled) return amount;
        if (voltage > getMaxInputVoltage()) {
            explodeMachine();
            return 0;
        }

        // Safely cast calculation using long logic
        long accepted = (long) Math.min(amount, maxEnergy - energy);
        energy += accepted;
        if (accepted > 0) markDirty();
        return amount - (double) accepted;
    }

    @Override
    public double getDemandedEnergy() {
        return (double) Math.max(0L, maxEnergy - energy);
    }
    @Override public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection side) { return true; }
    @Override public int getSinkTier() { return machineTier; }

    // ==========================================
    // INVENTORY & NBT ABSTRACTED IMPLEMENTATION
    // ==========================================
    @Override public int getSizeInventory() { return inventory.length; }
    @Override public ItemStack getStackInSlot(int slot) { return inventory[slot]; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            ItemStack stack;
            if (inventory[slot].stackSize <= amount) {
                stack = inventory[slot];
                inventory[slot] = null;
            } else {
                stack = inventory[slot].splitStack(amount);
                if (inventory[slot].stackSize <= 0) inventory[slot] = null;
            }
            markDirty();
            return stack;
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

    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
    @Override public boolean hasCustomInventoryName() { return false; }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("EnergyLong")) {
            this.energy = nbt.getLong("EnergyLong");
        } else {
            this.energy = nbt.getInteger("Energy");
        }
      //  energy = nbt.getInteger("Energy");
        machineTier = nbt.getInteger("MachineTier");
        enabled = !nbt.hasKey("Enabled") || nbt.getBoolean("Enabled");
        progress = nbt.getInteger("Progress");
        maxProgress = nbt.getInteger("MaxProgress");

        NBTTagList items = nbt.getTagList("Inventory", 10);
        this.inventory = new ItemStack[getSizeInventory()];
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
      //  nbt.setInteger("Energy", energy);
        nbt.setLong("EnergyLong", energy);
        nbt.setInteger("Energy", (int) Math.min(Integer.MAX_VALUE, energy));
        nbt.setInteger("MachineTier", machineTier);
        nbt.setBoolean("Enabled", enabled);
        nbt.setInteger("Progress", progress);
        nbt.setInteger("MaxProgress", maxProgress);

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
