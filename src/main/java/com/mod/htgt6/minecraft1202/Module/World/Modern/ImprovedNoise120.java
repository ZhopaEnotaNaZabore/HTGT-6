package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Improved 3-D Perlin kernel with the modern random-origin convention. */
public final class ImprovedNoise120 {
    private final byte[] permutation = new byte[256];
    private final double xo;
    private final double yo;
    private final double zo;

    public ImprovedNoise120(Xoroshiro128PlusPlus120 random) {
        xo = random.nextDouble() * 256.0;
        yo = random.nextDouble() * 256.0;
        zo = random.nextDouble() * 256.0;
        for (int i = 0; i < 256; i++) permutation[i] = (byte) i;
        for (int i = 0; i < 256; i++) {
            int j = i + random.nextInt(256 - i);
            byte swap = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = swap;
        }
    }

    public ImprovedNoise120(java.util.Random random) {
        xo = random.nextDouble() * 256.0;
        yo = random.nextDouble() * 256.0;
        zo = random.nextDouble() * 256.0;
        for (int i = 0; i < 256; i++) permutation[i] = (byte) i;
        for (int i = 0; i < 256; i++) {
            int j = i + random.nextInt(256 - i);
            byte swap = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = swap;
        }
    }

    public double sample(double x, double y, double z) {
        return sample(x,y,z,0.0D,0.0D);
    }

    /** Legacy BlendedNoise vertical-smear overload from ImprovedNoise 1.20.2. */
    public double sample(double x,double y,double z,double yScale,double yMax) {
        double px = x + xo, py = y + yo, pz = z + zo;
        int ix = floor(px), iy = floor(py), iz = floor(pz);
        double fx = px - ix, fy = py - iy, fz = pz - iz;
        double yFloor;
        if(yScale!=0.0D){
            double limited=yMax>=0.0D&&yMax<fy?yMax:fy;
            yFloor=Math.floor(limited/yScale+1.0000000116860974E-7D)*yScale;
        }else yFloor=0.0D;
        double sampledY=fy-yFloor;
        double u = fade(fx), v = fade(fy), w = fade(fz);
        int a = p(ix) + iy;
        int aa = p(a) + iz, ab = p(a + 1) + iz;
        int b = p(ix + 1) + iy;
        int ba = p(b) + iz, bb = p(b + 1) + iz;
        return lerp(w,
                lerp(v, lerp(u, grad(p(aa), fx, sampledY, fz), grad(p(ba), fx - 1, sampledY, fz)),
                        lerp(u, grad(p(ab), fx, sampledY - 1, fz), grad(p(bb), fx - 1, sampledY - 1, fz))),
                lerp(v, lerp(u, grad(p(aa + 1), fx, sampledY, fz - 1), grad(p(ba + 1), fx - 1, sampledY, fz - 1)),
                        lerp(u, grad(p(ab + 1), fx, sampledY - 1, fz - 1), grad(p(bb + 1), fx - 1, sampledY - 1, fz - 1))));
    }

    private int p(int index) { return permutation[index & 255] & 255; }
    private static int floor(double v) { int i = (int) v; return v < i ? i - 1 : i; }
    private static double fade(double t) { return t * t * t * (t * (t * 6.0 - 15.0) + 10.0); }
    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    private static double grad(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
