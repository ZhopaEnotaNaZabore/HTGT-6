package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraftforge.event.terraingen.WorldTypeEvent;

/** Applies the 1.13 ocean temperature family to vanilla ocean/deep-ocean cells. */
public final class OceanBiomeLayerHandler {

    @SubscribeEvent
    public void onInitBiomeGens(WorldTypeEvent.InitBiomeGens event) {
        // Futura obtains these registered 1.13 biome identities from its own
        // MultiNoise-compatible biome source. Applying this legacy GenLayer wrapper
        // there would select oceans twice from unrelated temperature fields.
        if (event.worldType == WorldType.FLAT || FuturaWorldType120.isFutura(event.worldType)
                || OceanBiomeRegistry.warmOcean == null) return;
        GenLayer oceanLayer = new OceanTemperatureLayer(event.seed, event.newBiomeGens[0]);
        GenLayer indexLayer = new ExtendedVoronoiZoom(10L, oceanLayer);
        oceanLayer.initWorldGenSeed(event.seed);
        indexLayer.initWorldGenSeed(event.seed);
        event.newBiomeGens[0] = oceanLayer;
        event.newBiomeGens[1] = indexLayer;
        if (event.newBiomeGens.length > 2) event.newBiomeGens[2] = oceanLayer;
    }

    private static final class OceanTemperatureLayer extends GenLayer {
        private static final int REGION_SCALE = 96;
        private final long worldSeed;

        private OceanTemperatureLayer(long seed, GenLayer parent) {
            super(seed);
            this.worldSeed = seed;
            this.parent = parent;
        }

        @Override
        public int[] getInts(int x, int z, int width, int height) {
            int[] values = this.parent.getInts(x, z, width, height);
            int[] result = new int[width * height];
            for (int dz = 0; dz < height; dz++) {
                for (int dx = 0; dx < width; dx++) {
                    int index = dx + dz * width;
                    int biomeId = values[index];
                    result[index] = selectOcean(biomeId, sampleNoise(x + dx, z + dz));
                }
            }
            return result;
        }

        private int selectOcean(int biomeId, double temperature) {
            boolean deep = biomeId == BiomeGenBase.deepOcean.biomeID;
            if (!deep && biomeId != BiomeGenBase.ocean.biomeID) return biomeId;
            if (temperature < 0.12D) {
                return deep ? OceanBiomeRegistry.deepFrozenOcean.biomeID
                        : BiomeGenBase.frozenOcean.biomeID;
            }
            if (temperature < 0.32D) {
                return deep ? OceanBiomeRegistry.deepColdOcean.biomeID
                        : OceanBiomeRegistry.coldOcean.biomeID;
            }
            if (temperature < 0.48D) return biomeId;
            if (temperature < 0.72D) {
                return deep ? OceanBiomeRegistry.deepLukewarmOcean.biomeID
                        : OceanBiomeRegistry.lukewarmOcean.biomeID;
            }
            // There is no deep_warm_ocean in the 1.20.2 biome registry. Deep
            // warm climate cells resolve to the ordinary warm ocean identity.
            return OceanBiomeRegistry.warmOcean.biomeID;
        }

        /** Low-frequency value noise forms broad temperature bands about 384 blocks across. */
        private double sampleNoise(int x, int z) {
            final int scale = REGION_SCALE;
            int cellX = floorDiv(x, scale);
            int cellZ = floorDiv(z, scale);
            double fx = (x - cellX * scale) / (double) scale;
            double fz = (z - cellZ * scale) / (double) scale;
            fx = fx * fx * (3.0D - 2.0D * fx);
            fz = fz * fz * (3.0D - 2.0D * fz);
            double north = lerp(hash(cellX, cellZ), hash(cellX + 1, cellZ), fx);
            double south = lerp(hash(cellX, cellZ + 1), hash(cellX + 1, cellZ + 1), fx);
            return lerp(north, south, fz);
        }

        private double hash(int x, int z) {
            long value = this.worldSeed;
            value ^= x * 341873128712L;
            value ^= z * 132897987541L;
            value = (value ^ (value >>> 33)) * 0xff51afd7ed558ccdl;
            value = (value ^ (value >>> 33)) * 0xc4ceb9fe1a85ec53l;
            value ^= value >>> 33;
            return ((value >>> 11) & ((1L << 53) - 1)) / (double) (1L << 53);
        }

        private static double lerp(double a, double b, double amount) {
            return a + (b - a) * amount;
        }

        private static int floorDiv(int value, int divisor) {
            int result = value / divisor;
            return value < 0 && value % divisor != 0 ? result - 1 : result;
        }
    }
}
