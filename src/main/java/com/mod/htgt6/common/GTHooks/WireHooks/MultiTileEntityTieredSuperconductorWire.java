package com.mod.htgt6.common.GTHooks.WireHooks;

import gregapi.tileentity.connectors.MultiTileEntityWireElectric;
import net.minecraft.nbt.NBTTagCompound;

/**
 * GT6 electric wire with support for a real zero-loss conductor.
 *
 * Everything related to networks, amperage/voltage overload, burn counters,
 * fire, contact damage and tools stays in GT6's MultiTileEntityWireElectric.
 * Vanilla GT6 clamps gt.pipeloss to at least one during NBT loading, so only
 * that single limitation is relaxed for completed HTGT6 superconductors.
 */
public class MultiTileEntityTieredSuperconductorWire extends MultiTileEntityWireElectric {
    /** Forge 1.7.10 requires a unique global TileEntity mapping per subclass. */
    @Override
    public String getTileEntityName() {
        return "htgt6.multitileentity.connector.wire.superconductor";
    }

    @Override
    public void readFromNBT2(NBTTagCompound nbt) {
        super.readFromNBT2(nbt);
        if (nbt.hasKey("gt.pipeloss") && nbt.getLong("gt.pipeloss") == 0L) {
            mLoss = 0L;
        }
    }
}
