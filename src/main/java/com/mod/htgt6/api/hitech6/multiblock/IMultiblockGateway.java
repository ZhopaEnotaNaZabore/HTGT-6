package com.mod.htgt6.api.hitech6.multiblock;

import net.minecraft.tileentity.TileEntity;

/** API-side contract implemented by every detachable HTGT6 multiblock gateway. */
public interface IMultiblockGateway {
    GatewayType getGatewayType();
    int getFluidTankCount();
    boolean link(TileEntity controller);
    void unlink();
    boolean isLinked();
}
