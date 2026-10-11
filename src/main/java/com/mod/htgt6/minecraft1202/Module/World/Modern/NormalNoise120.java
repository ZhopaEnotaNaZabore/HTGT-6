package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Minecraft-style double Perlin (called NormalNoise in 1.20.2). */
public final class NormalNoise120 {
    private static final double INPUT_FACTOR = 1.0181268882175227;
    private final OctavePerlinNoise120 first;
    private final OctavePerlinNoise120 second;
    private final double valueFactor;

    public NormalNoise120(long seed, int firstOctave, double... amplitudes) {
        this(new Xoroshiro128PlusPlus120(seed), firstOctave, amplitudes);
    }

    /** Accepts the registry-specific random stream produced by RandomState. */
    public NormalNoise120(Xoroshiro128PlusPlus120 random, int firstOctave, double... amplitudes) {
        if (random == null) throw new NullPointerException("random");
        first = new OctavePerlinNoise120(random, firstOctave, amplitudes);
        second = new OctavePerlinNoise120(random, firstOctave, amplitudes);
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int i = 0; i < amplitudes.length; i++) {
            if (amplitudes[i] != 0.0) {
                min = Math.min(min, i);
                max = Math.max(max, i);
            }
        }
        int span = min == Integer.MAX_VALUE ? 0 : max - min;
        valueFactor = 0.16666666666666666 / expectedDeviation(span);
    }

    /** State-provider noise uses a legacy RNG, unlike the terrain's xoroshiro stream. */
    public NormalNoise120(java.util.Random random,int firstOctave,double... amplitudes) {
        first=new OctavePerlinNoise120(random,firstOctave,amplitudes);
        second=new OctavePerlinNoise120(random,firstOctave,amplitudes);
        int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
        for(int i=0;i<amplitudes.length;i++)if(amplitudes[i]!=0){min=Math.min(min,i);max=Math.max(max,i);}
        valueFactor=0.16666666666666666/expectedDeviation(min==Integer.MAX_VALUE?0:max-min);
    }

    public double sample(double x, double y, double z) {
        return (first.sample(x, y, z)
                + second.sample(x * INPUT_FACTOR, y * INPUT_FACTOR, z * INPUT_FACTOR)) * valueFactor;
    }

    public double maxValue() {
        return (first.maxValue() + second.maxValue()) * valueFactor;
    }

    private static double expectedDeviation(int octaveSpan) {
        return 0.1 * (1.0 + 1.0 / (octaveSpan + 1.0));
    }
}
