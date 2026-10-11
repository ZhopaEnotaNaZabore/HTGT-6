package com.mod.htgt6.common.mining;

import com.mod.htgt6.api.hitech6.multiblock.GatewayType;
import com.mod.htgt6.common.multiblock.gateway.BlockMultiblockGateway;

/** Legacy registry adapter around the reusable HTGT6 gateway module. */
@Deprecated
public final class BlockMiningUnitPort extends BlockMultiblockGateway {
    public static final int FLUID_INPUT = GatewayType.FLUID_INPUT.metadata;
    public static final int FLUID_OUTPUT = GatewayType.FLUID_OUTPUT.metadata;
    public static final int ITEM_INPUT = GatewayType.ITEM_INPUT.metadata;
    public static final int ITEM_OUTPUT = GatewayType.ITEM_OUTPUT.metadata;
    public static final int ENERGY_INPUT = GatewayType.ENERGY_INPUT.metadata;
    public static final int ENERGY_OUTPUT = GatewayType.ENERGY_OUTPUT.metadata;
    public static final String[] NAMES = {
            GatewayType.FLUID_INPUT.serializedName, GatewayType.FLUID_OUTPUT.serializedName,
            GatewayType.ITEM_INPUT.serializedName, GatewayType.ITEM_OUTPUT.serializedName,
            GatewayType.ENERGY_INPUT.serializedName, GatewayType.ENERGY_OUTPUT.serializedName
    };

    public BlockMiningUnitPort() { super("mining_unit_port"); }
}
