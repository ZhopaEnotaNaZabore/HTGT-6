package com.mod.htgt6.api.hitech6.multiblock;

/** Generator constructor: subclasses define structure, fuel/process and module slots. */
public abstract class MultiTileGeneratorBase extends MultiTileEntityBase {
    protected MultiTileGeneratorBase(int inventorySize) { super(inventorySize); }
    @Override public final MultiblockEnergyRole getEnergyRole() { return MultiblockEnergyRole.GENERATOR; }
}
