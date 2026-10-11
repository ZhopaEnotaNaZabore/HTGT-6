package com.mod.htgt6.minecraft1202.Module.World.Shared;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Shared.GT6DeepslateOreIntegration;

import com.mod.htgt6.common.GTHooks.MaterialHooks.GT6MaterialAutoGenerator;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import cpw.mods.fml.common.IWorldGenerator;
import gregapi.util.WD;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.Random;

/** Generates Pherithium exclusively by replacing natural HTGT6 deepslate. */
public final class PherithiumDeepslateWorldGenerator implements IWorldGenerator {
    /** Physical Y values correspond to modern Y -59..8 in the translated Futura world. */
    public static final int MIN_Y = 5;
    public static final int MAX_Y = 72;
    public static final int VEINS_PER_CHUNK = 2;
    public static final int VEIN_SIZE = 7;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world == null || world.provider == null || world.provider.dimensionId != 0
                || !FuturaWorldType120.isFutura(world)
                || GT6MaterialAutoGenerator.PHERITHIUM == null
                || GT6DeepslateOreIntegration.normal == null) return;

        int originX = chunkX << 4;
        int originZ = chunkZ << 4;
        for (int vein = 0; vein < VEINS_PER_CHUNK; ++vein) {
            // Keep the centre away from the chunk edge. The ellipsoid can still join
            // naturally with an adjacent chunk without forcing distant chunk loads.
            int x = originX + 4 + random.nextInt(8);
            int y = MIN_Y + random.nextInt(MAX_Y - MIN_Y + 1);
            int z = originZ + 4 + random.nextInt(8);
            generateVein(world, random, x, y, z);
        }
    }

    private static void generateVein(World world, Random random, int centreX, int centreY, int centreZ) {
        float angle = random.nextFloat() * (float)Math.PI;
        double startX = centreX + Math.sin(angle) * VEIN_SIZE / 8.0D;
        double endX = centreX - Math.sin(angle) * VEIN_SIZE / 8.0D;
        double startZ = centreZ + Math.cos(angle) * VEIN_SIZE / 8.0D;
        double endZ = centreZ - Math.cos(angle) * VEIN_SIZE / 8.0D;
        double startY = centreY + random.nextInt(3) - 1;
        double endY = centreY + random.nextInt(3) - 1;

        for (int step = 0; step <= VEIN_SIZE; ++step) {
            double progress = step / (double)VEIN_SIZE;
            double x = startX + (endX - startX) * progress;
            double y = startY + (endY - startY) * progress;
            double z = startZ + (endZ - startZ) * progress;
            double randomScale = random.nextDouble() * VEIN_SIZE / 16.0D;
            double horizontalRadius = (Math.sin(Math.PI * progress) + 1.0D) * randomScale + 1.0D;
            double verticalRadius = horizontalRadius * 0.5D;

            int minX = floor(x - horizontalRadius / 2.0D);
            int maxX = floor(x + horizontalRadius / 2.0D);
            int minY = Math.max(MIN_Y, floor(y - verticalRadius / 2.0D));
            int maxY = Math.min(MAX_Y, floor(y + verticalRadius / 2.0D));
            int minZ = floor(z - horizontalRadius / 2.0D);
            int maxZ = floor(z + horizontalRadius / 2.0D);

            for (int blockX = minX; blockX <= maxX; ++blockX) {
                double normalizedX = (blockX + 0.5D - x) / (horizontalRadius / 2.0D);
                if (normalizedX * normalizedX >= 1.0D) continue;
                for (int blockY = minY; blockY <= maxY; ++blockY) {
                    double normalizedY = (blockY + 0.5D - y) / (verticalRadius / 2.0D);
                    if (normalizedX * normalizedX + normalizedY * normalizedY >= 1.0D) continue;
                    for (int blockZ = minZ; blockZ <= maxZ; ++blockZ) {
                        double normalizedZ = (blockZ + 0.5D - z) / (horizontalRadius / 2.0D);
                        if (normalizedX * normalizedX + normalizedY * normalizedY
                                + normalizedZ * normalizedZ >= 1.0D) continue;
                        placeIfDeepslate(world, blockX, blockY, blockZ);
                    }
                }
            }
        }
    }

    private static boolean placeIfDeepslate(World world, int x, int y, int z) {
        // This exact identity check is the hard host restriction. WD.setOre is
        // deliberately called only afterwards so no fallback stone ore can appear.
        return world.getBlock(x, y, z) == Backport120Content.deepslate
                && WD.setOre(world, x, y, z, GT6MaterialAutoGenerator.PHERITHIUM);
    }

    private static int floor(double value) {
        int integer = (int)value;
        return value < integer ? integer - 1 : integer;
    }
}
