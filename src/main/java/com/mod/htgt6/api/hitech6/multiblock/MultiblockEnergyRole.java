package com.mod.htgt6.api.hitech6.multiblock;

/** Separates a generator's output capability from simply installing an output hatch. */
public enum MultiblockEnergyRole {
    CONSUMER, GENERATOR;
    public int requiredOutputs() { return this == GENERATOR ? 1 : 0; }
    public boolean canExport(boolean formed, boolean enabled) {
        return this == GENERATOR && formed && enabled;
    }
}
