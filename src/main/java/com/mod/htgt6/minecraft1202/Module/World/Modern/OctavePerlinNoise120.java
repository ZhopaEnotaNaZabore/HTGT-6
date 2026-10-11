package com.mod.htgt6.minecraft1202.Module.World.Modern;

/** Weighted octave stack matching modern Minecraft's frequency/value progression. */
public final class OctavePerlinNoise120 {
    private static final double WRAP = 33554432.0;
    private final ImprovedNoise120[] levels;
    private final double[] amplitudes;
    private final double lowestFrequencyInputFactor;
    private final double lowestFrequencyValueFactor;

    public OctavePerlinNoise120(long seed, int firstOctave, double... amplitudes) {
        this(new Xoroshiro128PlusPlus120(seed), firstOctave, amplitudes);
    }

    public OctavePerlinNoise120(Xoroshiro128PlusPlus120 random, int firstOctave, double... amplitudes) {
        if (amplitudes == null || amplitudes.length == 0) throw new IllegalArgumentException("at least one octave is required");
        this.amplitudes = amplitudes.clone();
        this.levels = new ImprovedNoise120[amplitudes.length];
        PositionalRandomFactory120 positional = new PositionalRandomFactory120(random);
        for (int i = 0; i < levels.length; i++)
            if (amplitudes[i] != 0.0)
                levels[i] = new ImprovedNoise120(positional.fromHashOf("octave_" + (firstOctave + i)));
        this.lowestFrequencyInputFactor = Math.pow(2.0, firstOctave);
        this.lowestFrequencyValueFactor = Math.pow(2.0, levels.length - 1)
                / (Math.pow(2.0, levels.length) - 1.0);
    }

    /** Modern positional octaves seeded by LegacyRandomSource (state providers). */
    public OctavePerlinNoise120(java.util.Random random,int firstOctave,double... amplitudes) {
        if(amplitudes==null||amplitudes.length==0)throw new IllegalArgumentException("empty amplitudes");
        this.amplitudes=amplitudes.clone();this.levels=new ImprovedNoise120[amplitudes.length];
        long positionalSeed=random.nextLong();
        for(int i=0;i<levels.length;i++)if(amplitudes[i]!=0)
            levels[i]=new ImprovedNoise120(new java.util.Random(positionalSeed^(long)("octave_"+(firstOctave+i)).hashCode()));
        lowestFrequencyInputFactor=Math.pow(2.0,firstOctave);
        lowestFrequencyValueFactor=Math.pow(2.0,levels.length-1)/(Math.pow(2.0,levels.length)-1.0);
    }

    public double sample(double x, double y, double z) {
        double result = 0.0;
        double inputFactor = lowestFrequencyInputFactor;
        double valueFactor = lowestFrequencyValueFactor;
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] != null) {
                result += amplitudes[i] * valueFactor * levels[i].sample(
                        wrap(x * inputFactor), wrap(y * inputFactor), wrap(z * inputFactor));
            }
            inputFactor *= 2.0;
            valueFactor *= 0.5;
        }
        return result;
    }

    public double maxValue() {
        double result = 0.0;
        double factor = lowestFrequencyValueFactor;
        for (double amplitude : amplitudes) {
            result += Math.abs(amplitude) * factor;
            factor *= 0.5;
        }
        return result;
    }

    private static double wrap(double value) {
        return value - Math.floor(value / WRAP + 0.5) * WRAP;
    }
}
