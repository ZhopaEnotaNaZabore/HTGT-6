package com.mod.htgt6.minecraft1202.Module.World.Modern;

/**
 * Direct Java-8 representation of the built-in 1.20.2 NoiseData registry.
 * The first amplitude is kept explicitly because that is how
 * NormalNoise.NoiseParameters is constructed in the vanilla bootstrap.
 */
public enum NoiseParameters120 {
    TEMPERATURE("temperature", -10, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0),
    VEGETATION("vegetation", -8, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0),
    CONTINENTALNESS("continentalness", -9, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0),
    EROSION("erosion", -9, 1.0, 1.0, 0.0, 1.0, 1.0),
    TEMPERATURE_LARGE("temperature_large", -12, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0),
    VEGETATION_LARGE("vegetation_large", -10, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0),
    CONTINENTALNESS_LARGE("continentalness_large", -11, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0),
    EROSION_LARGE("erosion_large", -11, 1.0, 1.0, 0.0, 1.0, 1.0),
    RIDGE("ridge", -7, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0),
    OFFSET("offset", -3, 1.0, 1.0, 1.0, 0.0),

    AQUIFER_BARRIER("aquifer_barrier", -3, 1.0),
    AQUIFER_FLUID_LEVEL_FLOODEDNESS("aquifer_fluid_level_floodedness", -7, 1.0),
    AQUIFER_LAVA("aquifer_lava", -1, 1.0),
    AQUIFER_FLUID_LEVEL_SPREAD("aquifer_fluid_level_spread", -5, 1.0),
    PILLAR("pillar", -7, 1.0, 1.0),
    PILLAR_RARENESS("pillar_rareness", -8, 1.0),
    PILLAR_THICKNESS("pillar_thickness", -8, 1.0),
    SPAGHETTI_2D("spaghetti_2d", -7, 1.0),
    SPAGHETTI_2D_ELEVATION("spaghetti_2d_elevation", -8, 1.0),
    SPAGHETTI_2D_MODULATOR("spaghetti_2d_modulator", -11, 1.0),
    SPAGHETTI_2D_THICKNESS("spaghetti_2d_thickness", -11, 1.0),
    SPAGHETTI_3D_1("spaghetti_3d_1", -7, 1.0),
    SPAGHETTI_3D_2("spaghetti_3d_2", -7, 1.0),
    SPAGHETTI_3D_RARITY("spaghetti_3d_rarity", -11, 1.0),
    SPAGHETTI_3D_THICKNESS("spaghetti_3d_thickness", -8, 1.0),
    SPAGHETTI_ROUGHNESS("spaghetti_roughness", -5, 1.0),
    SPAGHETTI_ROUGHNESS_MODULATOR("spaghetti_roughness_modulator", -8, 1.0),
    CAVE_ENTRANCE("cave_entrance", -7, 0.4, 0.5, 1.0),
    CAVE_LAYER("cave_layer", -8, 1.0),
    CAVE_CHEESE("cave_cheese", -8, 0.5, 1.0, 2.0, 1.0, 2.0, 1.0, 0.0, 2.0, 0.0),
    ORE_VEININESS("ore_veininess", -8, 1.0),
    ORE_VEIN_A("ore_vein_a", -7, 1.0),
    ORE_VEIN_B("ore_vein_b", -7, 1.0),
    ORE_GAP("ore_gap", -5, 1.0),
    NOODLE("noodle", -8, 1.0),
    NOODLE_THICKNESS("noodle_thickness", -8, 1.0),
    NOODLE_RIDGE_A("noodle_ridge_a", -7, 1.0),
    NOODLE_RIDGE_B("noodle_ridge_b", -7, 1.0),
    JAGGED("jagged", -16, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0,
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0),
    SURFACE("surface", -6, 1.0, 1.0, 1.0),
    SURFACE_SECONDARY("surface_secondary", -6, 1.0, 1.0, 0.0, 1.0),
    CLAY_BANDS_OFFSET("clay_bands_offset", -8, 1.0),
    BADLANDS_PILLAR("badlands_pillar", -2, 1.0, 1.0, 1.0, 1.0),
    BADLANDS_PILLAR_ROOF("badlands_pillar_roof", -8, 1.0),
    BADLANDS_SURFACE("badlands_surface", -6, 1.0, 1.0, 1.0),
    ICEBERG_PILLAR("iceberg_pillar", -6, 1.0, 1.0, 1.0, 1.0),
    ICEBERG_PILLAR_ROOF("iceberg_pillar_roof", -3, 1.0),
    ICEBERG_SURFACE("iceberg_surface", -6, 1.0, 1.0, 1.0),
    SURFACE_SWAMP("surface_swamp", -2, 1.0),
    CALCITE("calcite", -9, 1.0, 1.0, 1.0, 1.0),
    GRAVEL("gravel", -8, 1.0, 1.0, 1.0, 1.0),
    POWDER_SNOW("powder_snow", -6, 1.0, 1.0, 1.0, 1.0),
    PACKED_ICE("packed_ice", -7, 1.0, 1.0, 1.0, 1.0),
    ICE("ice", -4, 1.0, 1.0, 1.0, 1.0),
    SOUL_SAND_LAYER("soul_sand_layer", -8, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334),
    GRAVEL_LAYER("gravel_layer", -8, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334),
    PATCH("patch", -5, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334),
    NETHERRACK("netherrack", -3, 1.0, 0.0, 0.0, 0.35),
    NETHER_WART("nether_wart", -3, 1.0, 0.0, 0.0, 0.9),
    NETHER_STATE_SELECTOR("nether_state_selector", -4, 1.0);

    private final String path;
    private final int firstOctave;
    private final double[] amplitudes;

    private NoiseParameters120(String path, int firstOctave, double firstAmplitude, double... remainingAmplitudes) {
        this.path = path;
        this.firstOctave = firstOctave;
        this.amplitudes = new double[remainingAmplitudes.length + 1];
        this.amplitudes[0] = firstAmplitude;
        System.arraycopy(remainingAmplitudes, 0, this.amplitudes, 1, remainingAmplitudes.length);
    }

    public String resourceLocation() { return "minecraft:" + path; }
    public int firstOctave() { return firstOctave; }
    public double[] amplitudes() { return amplitudes.clone(); }
}
