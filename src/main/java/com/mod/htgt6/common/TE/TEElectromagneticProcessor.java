package com.mod.htgt6.common.TE;

//Разработка

import com.mod.htgt6.api.hitech6.RM.MachineRecipe;
import com.mod.htgt6.api.hitech6.Misc.MachineState;
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityBaseMachine;
import com.mod.htgt6.api.hitech6.BaseMachines.TileEntityFluidMachine;
import com.mod.htgt6.common.handler.recipe.RM.ElectromagneticProcessorRecipeMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class TEElectromagneticProcessor extends TileEntityFluidMachine   {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT_START = 1;
    public static final int OUTPUT_COUNT = 6;
    public static final int SLOT_FLUID_CONTAINER = 7;
    public static final int SLOT_MODULE = 8;
    public static final int SLOT_TRANSFORMER = 9;
    public static final int SLOT_BATTERY = 10;

    public static final int TOTAL_SLOTS = 11;
    public static final int TANK_CAPACITY = 16000;

    public MachineState state = MachineState.IDLE;
    private MachineRecipe currentRecipe;

    public TEElectromagneticProcessor() {
        super(TOTAL_SLOTS, 2, TANK_CAPACITY);
    }

    @Override
    public int getModuleSlot() {
        return SLOT_MODULE;
    }

    @Override
    public int getTransformerSlot() {
        return SLOT_TRANSFORMER;
    }

    @Override
    public int getFluidSlotStart() {
        return SLOT_FLUID_CONTAINER;
    }

    @Override
    public int getFluidSlotCount() {
        return 1;
    }

    @Override
    public String getInventoryName() {
        return "Electromagnetic Processor";
    }

    @Override
    public int getInventoryStackLimit() {
        return 4096;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + OUTPUT_COUNT) {
            return false;
        }
        return super.isItemValidForSlot(slot, stack);
    }

    // ==========================================
    // CORE MACHINE LOGIC
    // ==========================================
    @Override
    protected void processMachineLogic() {
        drainFluidInputSlots();
        ElectromagneticProcessorRecipeMap map=ElectromagneticProcessorRecipeMap.INSTANCE;
        if(currentRecipe==null) {
            MachineRecipe match=map.findRecipe(inventory[SLOT_INPUT],tanks[0]);
            if(match==null||machineTier<match.minTier){state=MachineState.IDLE;return;}
            if(!canFinish(match)){state=MachineState.OUTPUT_FULL;return;}
            long cost=ElectromagneticProcessorRecipeMap.getOverclockedEUt(match,machineTier);
            if(energy<cost){state=MachineState.NO_POWER;return;}
            currentRecipe=match;
            maxProgress=ElectromagneticProcessorRecipeMap.getOverclockedDuration(match,machineTier);
            progress=0;
            if(match.inputs.length>0&&match.inputs[0]!=null) {
                inventory[SLOT_INPUT].stackSize-=match.inputs[0].stackSize;
                if(inventory[SLOT_INPUT].stackSize<=0)inventory[SLOT_INPUT]=null;
            }
            if(match.fluidInputs.length>0&&match.fluidInputs[0]!=null)tanks[0].drain(match.fluidInputs[0].amount,true);
        }
        // Recheck outputs before completing: automation may have changed the inventory.
        if(progress>=maxProgress) {
            if(!canFinish(currentRecipe)){state=MachineState.OUTPUT_FULL;return;}
            finishRecipe();return;
        }
        long cost=ElectromagneticProcessorRecipeMap.getOverclockedEUt(currentRecipe,machineTier);
        if(energy<cost){state=MachineState.NO_POWER;return;}
        energy-=cost;progress++;state=MachineState.RUNNING;
        if(progress>=maxProgress&&canFinish(currentRecipe))finishRecipe();
        markDirty();
    }

    private boolean canFinish(MachineRecipe recipe) {
        if(!ElectromagneticProcessorRecipeMap.INSTANCE.canOutput(inventory,SLOT_OUTPUT_START,recipe))return false;
        return recipe.fluidOutputs.length==0||recipe.fluidOutputs[0]==null
                ||tanks[1].fill(recipe.fluidOutputs[0],false)==recipe.fluidOutputs[0].amount;
    }

    @Override public int fill(net.minecraftforge.common.util.ForgeDirection side,net.minecraftforge.fluids.FluidStack fluid,boolean apply) {
        if(fluid==null)return 0;
        int amount=tanks[0].fill(fluid,apply);if(apply&&amount>0)markDirty();return amount;
    }
    @Override public net.minecraftforge.fluids.FluidStack drain(net.minecraftforge.common.util.ForgeDirection side,int amount,boolean apply) {
        net.minecraftforge.fluids.FluidStack fluid=tanks[1].drain(amount,apply);
        if(apply&&fluid!=null)markDirty();return fluid;
    }
    @Override public net.minecraftforge.fluids.FluidStack drain(net.minecraftforge.common.util.ForgeDirection side,net.minecraftforge.fluids.FluidStack fluid,boolean apply) {
        return fluid!=null&&tanks[1].getFluid()!=null&&tanks[1].getFluid().isFluidEqual(fluid)?drain(side,fluid.amount,apply):null;
    }
    @Override public boolean canFill(net.minecraftforge.common.util.ForgeDirection side,net.minecraftforge.fluids.Fluid fluid) {
        return fluid!=null&&(tanks[0].getFluid()==null||tanks[0].getFluid().getFluid()==fluid);
    }
    @Override public boolean canDrain(net.minecraftforge.common.util.ForgeDirection side,net.minecraftforge.fluids.Fluid fluid) {
        return fluid!=null&&tanks[1].getFluid()!=null&&tanks[1].getFluid().getFluid()==fluid;
    }

    private void finishRecipe() {
        if (currentRecipe != null) {
            // Output items with chance roll
            if (currentRecipe.outputs != null) {
                for (int i = 0; i < currentRecipe.outputs.length; i++) {
                    ItemStack recipeOut = currentRecipe.outputs[i];
                    if (recipeOut == null) continue;

                    // Roll chance: 10000 = 100%
                    int chance = currentRecipe.outputChances[i];
                    if (chance < 10000 && this.worldObj.rand.nextInt(10000) >= chance) {
                        continue; // Failed chance roll, skip generating item
                    }

                    int targetSlot = SLOT_OUTPUT_START + i;
                    if (inventory[targetSlot] == null) {
                        inventory[targetSlot] = recipeOut.copy();
                    } else {
                        inventory[targetSlot].stackSize += recipeOut.stackSize;
                    }
                }
            }

            // Output fluids if defined
            if (currentRecipe.fluidOutputs != null && currentRecipe.fluidOutputs.length > 0 && currentRecipe.fluidOutputs[0] != null) {
                tanks[1].fill(currentRecipe.fluidOutputs[0].copy(), true);
            }
        }

        this.currentRecipe = null;
        this.progress = 0;
        this.state = MachineState.FINISHED;
        markDirty();
    }

    private MachineRecipe findRecipeByOutput(ItemStack outputStack) {
        for (MachineRecipe recipe : ElectromagneticProcessorRecipeMap.INSTANCE.getRecipeList()) {
            if (recipe.outputs != null && recipe.outputs.length > 0 && recipe.outputs[0] != null) {
                if (recipe.outputs[0].isItemEqual(outputStack)) {
                    return recipe;
                }
            }
        }
        return null;
    }

    // ==========================================
    // NBT & NETWORK SYNCHRONIZATION
    // ==========================================
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        // Restore Progress & Max Progress
        this.progress = nbt.getInteger("Progress");
        this.maxProgress = nbt.getInteger("MaxProgress");

        int stateIdx = nbt.getInteger("MachineState");
        if (stateIdx >= 0 && stateIdx < MachineState.values().length) {
            this.state = MachineState.values()[stateIdx];
        }

        // Restore active recipe using saved target output
        currentRecipe=null;
        if(nbt.hasKey("ActiveRecipeExact")) {
            NBTTagCompound key=nbt.getCompoundTag("ActiveRecipeExact");
            for(MachineRecipe recipe:ElectromagneticProcessorRecipeMap.INSTANCE.getRecipeList())
                if(recipeKey(recipe).equals(key)){currentRecipe=recipe;break;}
        } else if (this.progress > 0 && nbt.hasKey("ActiveRecipeOutput")) {
            ItemStack savedOutput = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("ActiveRecipeOutput"));
            this.currentRecipe = findRecipeByOutput(savedOutput);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        // Save Progress & Max Progress
        nbt.setInteger("Progress", this.progress);
        nbt.setInteger("MaxProgress", this.maxProgress);
        nbt.setInteger("MachineState", this.state.ordinal());

        nbt.removeTag("ActiveRecipeExact");
        nbt.removeTag("ActiveRecipeOutput");
        if(currentRecipe!=null)nbt.setTag("ActiveRecipeExact",recipeKey(currentRecipe));
        // Legacy compatibility for older saves.
        if (this.progress > 0 && this.currentRecipe != null && this.currentRecipe.outputs != null && this.currentRecipe.outputs.length > 0) {
            NBTTagCompound recipeTag = new NBTTagCompound();
            this.currentRecipe.outputs[0].writeToNBT(recipeTag);
            nbt.setTag("ActiveRecipeOutput", recipeTag);
        }
    }

    private static NBTTagCompound recipeKey(MachineRecipe recipe) {
        NBTTagCompound key=new NBTTagCompound();
        key.setInteger("Ticks",recipe.duration);key.setLong("EUt",recipe.euPerTick);key.setInteger("Tier",recipe.minTier);
        key.setIntArray("Chances",recipe.outputChances);
        ItemStack[][] items={recipe.inputs,recipe.outputs};
        for(int n=0;n<items.length;n++) {
            net.minecraft.nbt.NBTTagList list=new net.minecraft.nbt.NBTTagList();
            for(ItemStack item:items[n]) {NBTTagCompound tag=new NBTTagCompound();if(item!=null){item.writeToNBT(tag);tag.setInteger("ExactCount",item.stackSize);}list.appendTag(tag);}
            key.setTag("Items"+n,list);
        }
        net.minecraftforge.fluids.FluidStack[][] fluids={recipe.fluidInputs,recipe.fluidOutputs};
        for(int n=0;n<fluids.length;n++) {
            net.minecraft.nbt.NBTTagList list=new net.minecraft.nbt.NBTTagList();
            for(net.minecraftforge.fluids.FluidStack fluid:fluids[n]) {NBTTagCompound tag=new NBTTagCompound();if(fluid!=null)fluid.writeToNBT(tag);list.appendTag(tag);}
            key.setTag("Fluids"+n,list);
        }
        return key;
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
