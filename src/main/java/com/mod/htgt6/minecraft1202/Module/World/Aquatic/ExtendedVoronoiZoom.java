package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

/** Vanilla 1.7.10 Voronoi zoom without the two destructive biomeId & 255 casts. */
public final class ExtendedVoronoiZoom extends GenLayer {
    public ExtendedVoronoiZoom(long seed, GenLayer parent) {
        super(seed);
        this.parent = parent;
    }

    @Override
    public int[] getInts(int x, int z, int width, int height) {
        x -= 2;
        z -= 2;
        int parentX = x >> 2, parentZ = z >> 2;
        int parentWidth = (width >> 2) + 2, parentHeight = (height >> 2) + 2;
        int[] source = parent.getInts(parentX, parentZ, parentWidth, parentHeight);
        int zoomWidth = (parentWidth - 1) << 2, zoomHeight = (parentHeight - 1) << 2;
        int[] zoomed = IntCache.getIntCache(zoomWidth * zoomHeight);

        for (int sourceZ = 0; sourceZ < parentHeight - 1; sourceZ++) {
            int sourceX = 0;
            int northWest = source[sourceZ * parentWidth];
            int southWest = source[(sourceZ + 1) * parentWidth];
            for (; sourceX < parentWidth - 1; sourceX++) {
                initChunkSeed((long)(sourceX + parentX << 2), (long)(sourceZ + parentZ << 2));
                double nwX = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                double nwZ = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                initChunkSeed((long)(sourceX + parentX + 1 << 2), (long)(sourceZ + parentZ << 2));
                double neX = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                double neZ = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                initChunkSeed((long)(sourceX + parentX << 2), (long)(sourceZ + parentZ + 1 << 2));
                double swX = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D;
                double swZ = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                initChunkSeed((long)(sourceX + parentX + 1 << 2), (long)(sourceZ + parentZ + 1 << 2));
                double seX = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                double seZ = (nextInt(1024) / 1024.0D - 0.5D) * 3.6D + 4.0D;
                int northEast = source[sourceX + 1 + sourceZ * parentWidth];
                int southEast = source[sourceX + 1 + (sourceZ + 1) * parentWidth];

                for (int localZ = 0; localZ < 4; localZ++) {
                    int target = ((sourceZ << 2) + localZ) * zoomWidth + (sourceX << 2);
                    for (int localX = 0; localX < 4; localX++) {
                        double dNW = square(localZ - nwZ) + square(localX - nwX);
                        double dNE = square(localZ - neZ) + square(localX - neX);
                        double dSW = square(localZ - swZ) + square(localX - swX);
                        double dSE = square(localZ - seZ) + square(localX - seX);
                        if (dNW < dNE && dNW < dSW && dNW < dSE) zoomed[target++] = northWest;
                        else if (dNE < dNW && dNE < dSW && dNE < dSE) zoomed[target++] = northEast;
                        else if (dSW < dNW && dSW < dNE && dSW < dSE) zoomed[target++] = southWest;
                        else zoomed[target++] = southEast;
                    }
                }
                northWest = northEast;
                southWest = southEast;
            }
        }

        int[] result = IntCache.getIntCache(width * height);
        for (int row = 0; row < height; row++) {
            System.arraycopy(zoomed, (row + (z & 3)) * zoomWidth + (x & 3),
                    result, row * width, width);
        }
        return result;
    }

    private static double square(double value) { return value * value; }
}
