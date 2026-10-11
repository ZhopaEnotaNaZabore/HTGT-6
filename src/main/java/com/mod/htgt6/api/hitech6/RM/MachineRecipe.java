package com.mod.htgt6.api.hitech6.RM;




import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class MachineRecipe {

    public final ItemStack[] inputs;
    public final ItemStack[] outputs;
    public final int[] outputChances; // Basis points: 10000 = 100%, 5000 = 50%, 2500 = 25%
    public final FluidStack[] fluidInputs;
    public final FluidStack[] fluidOutputs;
    public final int duration;
    public final long euPerTick;
    public final int minTier;

    public MachineRecipe(ItemStack[] inputs, ItemStack[] outputs, int[] outputChances,
                         FluidStack[] fluidInputs, FluidStack[] fluidOutputs,
                         int duration, long euPerTick, int minTier) {
        this.inputs = inputs != null ? inputs : new ItemStack[0];
        this.outputs = outputs != null ? outputs : new ItemStack[0];
        this.fluidInputs = fluidInputs != null ? fluidInputs : new FluidStack[0];
        this.fluidOutputs = fluidOutputs != null ? fluidOutputs : new FluidStack[0];
        this.duration = duration;
        this.euPerTick = euPerTick;
        this.minTier = minTier;

        // Populate chances (default to 10000 / 100% if omitted)
        this.outputChances = new int[this.outputs.length];
        for (int i = 0; i < this.outputs.length; i++) {
            if (outputChances != null && i < outputChances.length) {
                this.outputChances[i] = outputChances[i];
            } else {
                this.outputChances[i] = 10000;
            }
        }
    }

    // Constructor with no chances specified (defaults all outputs to 100%)
    public MachineRecipe(ItemStack[] inputs, ItemStack[] outputs,
                         FluidStack[] fluidInputs, FluidStack[] fluidOutputs,
                         int duration, long euPerTick, int minTier) {
        this(inputs, outputs, null, fluidInputs, fluidOutputs, duration, euPerTick, minTier);
    }
}
