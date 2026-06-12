package com.mod.htgt6.common.TE;


import com.mod.htgt6.common.handler.recipe.compressor.CompressorRecipeHandler;
import com.mod.htgt6.common.item.htgt6.modulesystem.Tmodules;
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

@Optional.Interface(
        iface = "ic2.api.energy.tile.IEnergySink",
        modid = "IC2"
)
public class TESuperMasicCompressor extends TileEntity implements IInventory, IEnergySink {

    public ItemStack[] inventory = new ItemStack[20];
    public int energy = 0;
    public int maxEnergy = 1100000000;
    public int machineTier = 1;
    public boolean enabled = true;

    // Assumes an equivalent Compressor recipe system structure
    public CompressorRecipeHandler.CompressorRecipe currentRecipe;
    public int recipeEUt = 0;
    public int progress = 0;
    public int maxProgress = 200;

    private boolean addedToEnergyNet = false;

    public static final int[] VOLTAGES = {
            32, 128, 512, 2048, 8192, 32768, 131072, 524288,
            2097152, 8388608, 33554432, 134217728, 536870912, Integer.MAX_VALUE
    };

    @Override
    public void updateEntity() {
        super.updateEntity();
        updateMachineTier();

        if (worldObj.isRemote) return;
        if (!enabled) return;

        // Scans slots 0, 1, 2 for compression combinations
        currentRecipe = CompressorRecipeHandler.findRecipe(inventory);

        if (currentRecipe == null) {
            progress = 0;
            recipeEUt = 0;
            return;
        }

        if (machineTier < currentRecipe.tier) {
            progress = 0;
            recipeEUt = 0;
            return;
        }

        maxProgress = CompressorRecipeHandler.getOverclockedDuration(currentRecipe, machineTier);
        recipeEUt = CompressorRecipeHandler.getOverclockedEUt(currentRecipe, machineTier);

        if (energy < recipeEUt) return;

        energy -= recipeEUt;
        progress++;

        if (progress >= maxProgress) {
            CompressorRecipeHandler.consumeInputs(currentRecipe, inventory);
            CompressorRecipeHandler.outputRecipe(currentRecipe, inventory);
            progress = 0;
            markDirty();
        }
    }

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

    @Override
    public double injectEnergy(ForgeDirection direction, double amount, double voltage) {
        if (!enabled) return amount;

        if (voltage > getMaxInputVoltage()) {
            explodeMachine();
            return 0;
        }

        int accepted = (int) Math.min(amount, maxEnergy - energy);
        energy += accepted;
        return amount - accepted;
    }

    @Override
    public double getDemandedEnergy() { return maxEnergy - energy; }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection side) { return true; }

    @Override
    public int getSinkTier() { return machineTier; }

    private void updateMachineTier() {
        machineTier = 1;
        ItemStack transformer = inventory[6]; // Shifted to index 6
        ItemStack module = inventory[5];      // Shifted to index 5

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

    public int getMaxInputVoltage() {
        int index = machineTier - 1;
        if (index < 0) index = 0;
        if (index >= VOLTAGES.length) index = VOLTAGES.length - 1;
        return VOLTAGES[index];
    }

    public void explodeMachine() {
        if (worldObj == null || worldObj.isRemote) return;
        float power = 2F + (machineTier * 0.4F);
        worldObj.createExplosion(null, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, power, true);
        worldObj.setBlockToAir(xCoord, yCoord, zCoord);
    }

    @Override public int getSizeInventory() { return inventory.length; }
    @Override public ItemStack getStackInSlot(int slot) { return inventory[slot]; }

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
            if (inventory[slot].stackSize <= 0) inventory[slot] = null;
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

    @Override public String getInventoryName() { return "Super Masic Compressor"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }

    @Override public void openInventory() {}
    @Override public void closeInventory() {}
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getInteger("Energy");
        machineTier = nbt.getInteger("MachineTier");
        enabled = nbt.getBoolean("Enabled");
        progress = nbt.getInteger("Progress");
        maxProgress = nbt.getInteger("MaxProgress");

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
        nbt.setInteger("Energy", energy);
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
