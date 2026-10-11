package com.mod.htgt6.api.hitech6.multiblock;

import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Controller-side contract for detachable HTGT6 gateways. New multiblocks only
 * implement this interface; the gateway module owns all external capability exposure.
 */
public interface IMultiblockGatewayController {
    boolean isGatewayControllerFormed();
    int getGatewayTier();
    long getGatewayMaxInputVoltage();
    long getGatewayMaxOutputVoltage();
    long getGatewayFluidCapacity(GatewayType type);
    IFluidHandler getGatewayFluidHandler(GatewayType type);
    IFluidHandler getGatewayFluidHandler(GatewayType type, int gatewayX, int gatewayY, int gatewayZ);
    default IFluidHandler getGatewayFluidTankHandler(GatewayType type, int x, int y, int z, int tank) {
        return tank == 0 ? getGatewayFluidHandler(type, x, y, z) : null;
    }
    IInventory getGatewayItemInventory(GatewayType type);
    int[] getGatewayItemSlots(GatewayType type);
    IEnergySink getGatewayEnergyHandler(GatewayType type);
    IEnergySource getGatewayEnergyOutputHandler(GatewayType type);
    /** Structural skin rendered on every non-functional face of a linked gateway. */
    Block getGatewayCasingBlock();
    int getGatewayCasingMetadata();

}
