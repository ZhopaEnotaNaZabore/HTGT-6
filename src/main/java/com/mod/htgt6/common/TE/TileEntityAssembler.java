package com.mod.htgt6.common.TE;

import com.mod.htgt6.common.handler.recipe.AssemblerRecipe;
import com.mod.htgt6.common.handler.recipe.AssemblerRecipes;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityAssembler extends TileEntity implements IEnergySink, IInventory {

    public ItemStack[] inventory = new ItemStack[11]; // 0-8 Input, 9 Output, 10 Upgrade
    public double energy = 0;
    public final double maxEnergy = 32000;
    public int progress = 0;
    public int maxProgress = 100;
    private boolean addedToEnet = false;

    @Override
    public void updateEntity() {
        if (worldObj.isRemote) return;

        if (!addedToEnet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnet = true;
        }

        AssemblerRecipe recipe = AssemblerRecipes.getInstance().getRecipe(this.inventory);

        if (recipe != null && canOutput(recipe.output)) {
            this.maxProgress = recipe.duration;

            // Handle progress and energy consumption
            if (this.energy >= recipe.euPerTick) {
                this.energy -= recipe.euPerTick;
                this.progress++;

                if (this.progress >= this.maxProgress) {
                    this.craftItem(recipe);
                    this.progress = 0;
                }
            }
        } else {
            this.progress = 0;
        }
    }

    private void craftItem(AssemblerRecipe recipe) {
        for (int i = 0; i < 9; i++) {
            if (inventory[i] != null && recipe.input[i] != null) {
                this.decrStackSize(i, recipe.input[i].stackSize);
            }
        }

        if (inventory[9] == null) {
            inventory[9] = recipe.output.copy();
        } else {
            inventory[9].stackSize += recipe.output.stackSize;
        }

        this.markDirty();
    }

    private boolean canOutput(ItemStack stack) {
        if (stack == null) return false;
        ItemStack outputSlot = this.inventory[9];
        if (outputSlot == null) return true;
        if (!outputSlot.isItemEqual(stack)) return false;

        int resultSize = outputSlot.stackSize + stack.stackSize;
        return resultSize <= getInventoryStackLimit() && resultSize <= outputSlot.getMaxStackSize();
    }

    // --- IInventory Implementation ---

    @Override
    public int getSizeInventory() { return inventory.length; }

    @Override
    public ItemStack getStackInSlot(int i) { return inventory[i]; }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        if (this.inventory[slot] != null) {
            ItemStack itemstack;
            if (this.inventory[slot].stackSize <= count) {
                itemstack = this.inventory[slot];
                this.inventory[slot] = null;
                this.markDirty();
                return itemstack;
            } else {
                itemstack = this.inventory[slot].splitStack(count);
                if (this.inventory[slot].stackSize == 0) this.inventory[slot] = null;
                this.markDirty();
                return itemstack;
            }
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack s) {
        inventory[i] = s;
        if (s != null && s.stackSize > getInventoryStackLimit()) s.stackSize = getInventoryStackLimit();
        this.markDirty();
    }

    @Override public ItemStack getStackInSlotOnClosing(int slot) { return null; }
    @Override public String getInventoryName() { return "Assembler"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int i, ItemStack s) { return i != 9; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
                p.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    // --- IC2 IEnergySink Implementation ---

    @Override public double getDemandedEnergy() { return maxEnergy - energy; }
    @Override public int getSinkTier() { return 2; }
    @Override public boolean acceptsEnergyFrom(TileEntity e, ForgeDirection d) { return true; }

    @Override
    public double injectEnergy(ForgeDirection d, double amount, double voltage) {
        energy += amount;
        return 0;
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