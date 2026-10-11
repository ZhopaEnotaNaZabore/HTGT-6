package com.mod.htgt6.api.hitech6.multiblock;

/** Stable metadata contract shared by every HTGT6 multiblock gateway. */
public enum GatewayType {
    FLUID_INPUT(0, "fluid_input", "mining_unit_port_fluid_input"),
    FLUID_OUTPUT(1, "fluid_output", "mining_unit_port_fluid_output"),
    ITEM_INPUT(2, "item_input", "mining_unit_port_item_input"),
    ITEM_OUTPUT(3, "item_output", "mining_unit_port_item_output"),
    ENERGY_INPUT(4, "energy_input", "mining_unit_port_energy_input"),
    // Metadata 5-8 is permanently reserved by FluidGatewayVariant.
    ENERGY_OUTPUT(9, "energy_output", "mining_unit_port_energy_output");

    public static final GatewayType[] VALUES = values();
    public final int metadata;
    public final String serializedName;
    public final String textureName;

    GatewayType(int metadata, String serializedName, String textureName) {
        this.metadata = metadata;
        this.serializedName = serializedName;
        this.textureName = textureName;
    }

    public static GatewayType byMetadata(int metadata) {
        for (GatewayType type : VALUES) if (type.metadata == metadata) return type;
        return FLUID_INPUT;
    }
}
