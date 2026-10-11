package com.mod.htgt6.api.hitech6.multiblock;

/** Optional multi-tank fluid gateway metadata, retaining the same logical I/O type. */
public final class FluidGatewayVariant {
    public static final int FLUID_INPUT_4 = 5;
    public static final int FLUID_OUTPUT_4 = 6;
    public static final int FLUID_INPUT_8 = 7;
    public static final int FLUID_OUTPUT_8 = 8;

    private FluidGatewayVariant() { }

    public static GatewayType type(int metadata) {
        if (metadata == FLUID_INPUT_4 || metadata == FLUID_INPUT_8) return GatewayType.FLUID_INPUT;
        if (metadata == FLUID_OUTPUT_4 || metadata == FLUID_OUTPUT_8) return GatewayType.FLUID_OUTPUT;
        return GatewayType.byMetadata(metadata);
    }

    public static int tankCount(int metadata) {
        if (metadata == FLUID_INPUT_4 || metadata == FLUID_OUTPUT_4) return 4;
        if (metadata == FLUID_INPUT_8 || metadata == FLUID_OUTPUT_8) return 8;
        GatewayType type = GatewayType.byMetadata(metadata);
        return type == GatewayType.FLUID_INPUT || type == GatewayType.FLUID_OUTPUT ? 1 : 0;
    }

    public static String localizationSuffix(int metadata) {
        GatewayType type = type(metadata);
        int tanks = tankCount(metadata);
        return tanks > 1 ? type.serializedName + "_" + tanks : type.serializedName;
    }
}
