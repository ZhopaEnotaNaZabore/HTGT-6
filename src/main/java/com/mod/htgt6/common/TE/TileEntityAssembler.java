package com.mod.htgt6.common.TE;

import com.mod.htgt6.common.handler.recipe.AssemblerRecipe;
import com.mod.htgt6.common.handler.recipe.AssemblerRecipes;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAssembler extends TileEntity implements IEnergySink, IInventory {

    private ItemStack[] inventory = new ItemStack[11];
    public double energy = 0;
    public final double maxEnergy = 32000;
    public int progress = 0;
    public int maxProgress = 100; // Updated by recipe
    public int machineTier = 1; // Change this to 2 to allow higher recipes
    private boolean addedToEnet = false;

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        if (!addedToEnet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnet = true;
        }

        AssemblerRecipe recipe = AssemblerRecipes.getMatchingRecipe(inventory, machineTier);

        if (recipe != null && canOutput(recipe.output)) {
            this.maxProgress = recipe.duration;

            if (this.energy >= recipe.euPerTick) {
                this.energy -= recipe.euPerTick;
                this.progress++;

                if (this.progress >= this.maxProgress) {
                    craftItem(recipe);
                    this.progress = 0;
                }
            }
        } else {
            this.progress = 0;
        }
    }

    private boolean canOutput(ItemStack stack) {
        if (inventory[9] == null) return true;
        if (!inventory[9].isItemEqual(stack)) return false;
        return inventory[9].stackSize + stack.stackSize <= inventory[9].getMaxStackSize();
    }

    private void craftItem(AssemblerRecipe recipe) {
        // Consume inputs
        for (int i = 0; i < 9; i++) {
            if (i < recipe.inputs.length && recipe.inputs[i] != null) {
                this.decrStackSize(i, recipe.inputs[i].stackSize);
            }
        }
        // Produce output
        if (inventory[9] == null) inventory[9] = recipe.output.copy();
        else inventory[9].stackSize += recipe.output.stackSize;
    }

    // --- Standard IInventory/IEnergySink/NBT Methods ---
    @Override public int getSizeInventory() { return 11; }
    @Override public ItemStack getStackInSlot(int i) { return inventory[i]; }
    @Override public void setInventorySlotContents(int i, ItemStack s) { inventory[i] = s; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public String getInventoryName() { return "Assembler"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public boolean isUseableByPlayer(EntityPlayer p) { return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this; }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int i, ItemStack s) { return i < 9 || i == 10; }

    @Override
    public ItemStack decrStackSize(int i, int count) {
        if (this.inventory[i] != null) {
            ItemStack is;
            if (this.inventory[i].stackSize <= count) {
                is = this.inventory[i];
                this.inventory[i] = null;
                return is;
            }
            is = this.inventory[i].splitStack(count);
            if (this.inventory[i].stackSize == 0) this.inventory[i] = null;
            return is;
        }
        return null;
    }

    @Override public ItemStack getStackInSlotOnClosing(int i) { return null; }

    @Override public double getDemandedEnergy() { return maxEnergy - energy; }
    @Override public int getSinkTier() { return 2; }
    @Override public double injectEnergy(ForgeDirection d, double amount, double voltage) {
        double intake = Math.min(maxEnergy - energy, amount);
        energy += intake;
        return amount - intake;
    }
    @Override public boolean acceptsEnergyFrom(TileEntity e, ForgeDirection d) { return true; }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setDouble("energy", energy);
        nbt.setInteger("prog", progress);
        nbt.setInteger("tier", machineTier);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte)i);
                inventory[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        nbt.setTag("Items", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.energy = nbt.getDouble("energy");
        this.progress = nbt.getInteger("prog");
        this.machineTier = nbt.getInteger("tier");
        NBTTagList list = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < inventory.length) inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
        }
    }

    @Override
    public void invalidate() {
        if (addedToEnet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnet = false;
        }
        super.invalidate();
    }
}