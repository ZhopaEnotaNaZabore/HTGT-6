package com.mod.htgt6.common.TE;


import com.mod.htgt6.common.handler.recipe.assembler.AssemblerRecipeHandler;
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
public class TileEntityAssembler
        extends TileEntity
        implements IInventory, IEnergySink {

    // ==========================================
    // INVENTORY
    // ==========================================

    public ItemStack[] inventory =
            new ItemStack[20];

    // ==========================================
    // ENERGY
    // ==========================================

    public int energy = 0;

    public int maxEnergy = 1100000000;

    // ==========================================
    // MACHINE
    // ==========================================

    public int machineTier = 1;

    public boolean enabled = true;

    // ==========================================
    // RECIPE
    // ==========================================

    public AssemblerRecipeHandler.AssemblerRecipe currentRecipe;

    public int recipeEUt = 0;

    // ==========================================
    // PROGRESS
    // ==========================================

    public int progress = 0;

    public int maxProgress = 200;

    // ==========================================
    // IC2 ENERGY NET
    // ==========================================

    private boolean addedToEnergyNet =
            false;

    // ==========================================
    // VOLTAGES
    // ==========================================

    public static final int[] VOLTAGES = {

            32,
            128,
            512,
            2048,
            8192,
            32768,
            131072,
            524288,
            2097152,
            8388608,
            33554432,
            134217728,
            536870912,
            Integer.MAX_VALUE
    };

    // ==========================================
    // UPDATE
    // ==========================================

    @Override
    public void updateEntity() {

        super.updateEntity();

        updateMachineTier();

        if (worldObj.isRemote)
            return;

        if (!enabled)
            return;

        // ==========================================
        // FIND RECIPE
        // ==========================================

        currentRecipe =
                AssemblerRecipeHandler.findRecipe(
                        inventory
                );

        // ==========================================
        // NO RECIPE
        // ==========================================

        if (currentRecipe == null) {

            progress = 0;

            recipeEUt = 0;

            return;
        }

        // ==========================================
        // TIER CHECK
        // ==========================================

        if (machineTier
                < currentRecipe.tier) {

            progress = 0;

            recipeEUt = 0;

            return;
        }

        // ==========================================
        // OVERCLOCK
        // ==========================================

        maxProgress =
                AssemblerRecipeHandler
                        .getOverclockedDuration(
                                currentRecipe,
                                machineTier
                        );

        recipeEUt =
                AssemblerRecipeHandler
                        .getOverclockedEUt(
                                currentRecipe,
                                machineTier
                        );

        // ==========================================
        // ENERGY CHECK
        // ==========================================

        if (energy < recipeEUt)
            return;

        // ==========================================
        // USE ENERGY
        // ==========================================

        energy -= recipeEUt;

        progress++;

        // ==========================================
        // FINISH
        // ==========================================

        if (progress >= maxProgress) {

            AssemblerRecipeHandler.consumeInputs(
                    currentRecipe,
                    inventory
            );

            AssemblerRecipeHandler.outputRecipe(
                    currentRecipe,
                    inventory
            );

            progress = 0;

            markDirty();
        }

    }


    // ==========================================
    // IC2 LOAD
    // ==========================================

    @Override
    public void validate() {

        super.validate();

        if (!worldObj.isRemote) {

            MinecraftForge.EVENT_BUS.post(
                    new EnergyTileLoadEvent(this)
            );

            addedToEnergyNet = true;
        }
    }

    // ==========================================
    // IC2 UNLOAD
    // ==========================================

    @Override
    public void invalidate() {

        if (!worldObj.isRemote
                && addedToEnergyNet) {

            MinecraftForge.EVENT_BUS.post(
                    new EnergyTileUnloadEvent(this)
            );
        }

        addedToEnergyNet = false;

        super.invalidate();
    }

    // ==========================================
    // ENERGY INPUT
    // ==========================================

    @Override
    public double injectEnergy(
            ForgeDirection direction,
            double amount,
            double voltage
    ) {

        if (!enabled)
            return amount;

        // ==========================================
        // OVERVOLTAGE
        // ==========================================

        if (voltage > getMaxInputVoltage()) {

            explodeMachine();

            return 0;
        }

        int accepted =
                (int)Math.min(
                        amount,
                        maxEnergy - energy
                );

        energy += accepted;

        return amount - accepted;
    }

    @Override
    public double getDemandedEnergy() {

        return maxEnergy - energy;
    }

    @Override
    public boolean acceptsEnergyFrom(
            TileEntity emitter,
            ForgeDirection side
    ) {

        return true;
    }

    @Override
    public int getSinkTier() {

        return machineTier;
    }

    // ==========================================
    // MACHINE TIER
    // ==========================================

    private void updateMachineTier() {

        machineTier = 1;

        ItemStack transformer =
                inventory[12];

        ItemStack module =
                inventory[11];

        if (transformer == null)
            return;

        if (module == null)
            return;

        String transformerName =
                transformer.getUnlocalizedName()
                        .toLowerCase();

        Item item =
                module.getItem();

        int moduleTier = 1;

        if (item == Tmodules.TierModuleMV)
            moduleTier = 2;

        else if (item == Tmodules.TierModuleHV)
            moduleTier = 3;

        else if (item == Tmodules.TierModuleEV)
            moduleTier = 4;

        else if (item == Tmodules.TierModuleIV)
            moduleTier = 5;

        else if (item == Tmodules.TierModuleLuV)
            moduleTier = 6;

        else if (item == Tmodules.TierModuleZPM)
            moduleTier = 7;

        else if (item == Tmodules.TierModuleUV)
            moduleTier = 8;

        else if (item == Tmodules.TierModulePUV1)
            moduleTier = 9;

        else if (item == Tmodules.TierModuleUX)
            moduleTier = 10;

        else if (item == Tmodules.TierModuleOLV)
            moduleTier = 11;

        else if (item == Tmodules.TierModuleOMV)
            moduleTier = 12;

        else if (item == Tmodules.TierModuleOHV)
            moduleTier = 13;

        else if (item == Tmodules.TierModuleOEV)
            moduleTier = 14;

        else if (item == Tmodules.TierModuleOIV)
            moduleTier = 15;

        else if (item == Tmodules.TierModuleMAX)
            moduleTier = 16;

        int maxTier = 1;

        if (transformerName.contains("lowleveltransformer")) {

            maxTier = 4;

        } else if (transformerName.contains("mediumleveltransformer")) {

            maxTier = 6;

        } else if (transformerName.contains("highleveltransformer")) {

            maxTier = 8;

        } else if (transformerName.contains("ultimateleveltransformer")) {

            maxTier = 15;

        } else if (transformerName.contains("omegatransformer")) {

            maxTier = 16;
        }

        if (moduleTier <= maxTier) {

            machineTier = moduleTier;

        } else {

            machineTier = 1;
        }
    }

    // ==========================================
    // TIER NAME
    // ==========================================

    public String getTierName() {

        switch (machineTier) {

            case 2: return "MV";
            case 3: return "HV";
            case 4: return "EV";
            case 5: return "IV";
            case 6: return "LuV";
            case 7: return "ZPM";
            case 8: return "UV";
            case 9: return "PUV1";
            case 10: return "UX";
            case 11: return "OLV";
            case 12: return "OMV";
            case 13: return "OHV";
            case 14: return "OEV";
            case 15: return "OIV";
            case 16: return "MAX";
        }

        return "LV";
    }

    // ==========================================
    // TIER COLOR
    // ==========================================

    public int getTierColor() {

        switch (machineTier) {

            case 2: return 0x00AAFF;
            case 3: return 0xFFFF00;
            case 4: return 0xFF8800;
            case 5: return 0xFF0000;
            case 6: return 0xAA00FF;
            case 7: return 0xFF00FF;
            case 8: return 0x00FFFF;
        }

        return 0xAAAAAA;
    }

    // ==========================================
    // MAX INPUT
    // ==========================================

    public int getMaxInputVoltage() {

        int index =
                machineTier - 1;

        if (index < 0)
            index = 0;

        if (index >= VOLTAGES.length)
            index = VOLTAGES.length - 1;

        return VOLTAGES[index];
    }

    // ==========================================
    // EXPLOSION
    // ==========================================

    public void explodeMachine() {

        if (worldObj == null)
            return;

        if (worldObj.isRemote)
            return;

        float power =
                2F + (machineTier * 0.4F);

        worldObj.createExplosion(
                null,
                xCoord + 0.5D,
                yCoord + 0.5D,
                zCoord + 0.5D,
                power,
                true
        );

        worldObj.setBlockToAir(
                xCoord,
                yCoord,
                zCoord
        );
    }

    // ==========================================
    // INVENTORY
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
    public ItemStack decrStackSize(
            int slot,
            int amount
    ) {

        if (inventory[slot] != null) {

            ItemStack itemstack;

            if (inventory[slot].stackSize <= amount) {

                itemstack = inventory[slot];

                inventory[slot] = null;

                markDirty();

                return itemstack;
            }

            itemstack =
                    inventory[slot]
                            .splitStack(amount);

            if (inventory[slot].stackSize <= 0) {

                inventory[slot] = null;
            }

            markDirty();

            return itemstack;
        }

        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(
            int slot
    ) {

        if (inventory[slot] != null) {

            ItemStack stack =
                    inventory[slot];

            inventory[slot] = null;

            return stack;
        }

        return null;
    }

    @Override
    public void setInventorySlotContents(
            int slot,
            ItemStack stack
    ) {

        inventory[slot] = stack;

        if (stack != null
                && stack.stackSize
                > getInventoryStackLimit()) {

            stack.stackSize =
                    getInventoryStackLimit();
        }

        markDirty();
    }

    @Override
    public String getInventoryName() {

        return "Assembler";
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
    public boolean isUseableByPlayer(
            EntityPlayer player
    ) {

        return worldObj.getTileEntity(
                xCoord,
                yCoord,
                zCoord
        ) == this
                && player.getDistanceSq(
                xCoord + 0.5D,
                yCoord + 0.5D,
                zCoord + 0.5D
        ) <= 64D;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(
            int slot,
            ItemStack stack
    ) {

        return true;
    }

    // ==========================================
    // READ NBT
    // ==========================================

    @Override
    public void readFromNBT(
            NBTTagCompound nbt
    ) {

        super.readFromNBT(nbt);

        energy =
                nbt.getInteger("Energy");

        machineTier =
                nbt.getInteger("MachineTier");

        enabled =
                nbt.getBoolean("Enabled");

        progress =
                nbt.getInteger("Progress");

        maxProgress =
                nbt.getInteger("MaxProgress");

        NBTTagList items =
                nbt.getTagList(
                        "Inventory",
                        10
                );

        inventory =
                new ItemStack[
                        getSizeInventory()
                        ];

        for (int i = 0; i < items.tagCount(); i++) {

            NBTTagCompound itemTag =
                    items.getCompoundTagAt(i);

            int slot =
                    itemTag.getByte("Slot")
                            & 255;

            if (slot >= 0
                    && slot < inventory.length) {

                inventory[slot] =
                        ItemStack.loadItemStackFromNBT(
                                itemTag
                        );
            }
        }
    }

    // ==========================================
    // WRITE NBT
    // ==========================================

    @Override
    public void writeToNBT(
            NBTTagCompound nbt
    ) {

        super.writeToNBT(nbt);

        nbt.setInteger(
                "Energy",
                energy
        );

        nbt.setInteger(
                "MachineTier",
                machineTier
        );

        nbt.setBoolean(
                "Enabled",
                enabled
        );

        nbt.setInteger(
                "Progress",
                progress
        );

        nbt.setInteger(
                "MaxProgress",
                maxProgress
        );

        NBTTagList items =
                new NBTTagList();

        for (int i = 0; i < inventory.length; i++) {

            if (inventory[i] != null) {

                NBTTagCompound itemTag =
                        new NBTTagCompound();

                itemTag.setByte(
                        "Slot",
                        (byte)i
                );

                inventory[i].writeToNBT(
                        itemTag
                );

                items.appendTag(
                        itemTag
                );
            }
        }

        nbt.setTag(
                "Inventory",
                items
        );
    }
}