package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.ImprovedNoise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.InterpolatedNoise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NormalNoise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.OctavePerlinNoise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.PositionalRandomFactory120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.Xoroshiro128PlusPlus120;

import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseParameters120;
import java.util.HashSet;
import java.util.Set;

/** Fixed-seed regression vectors. Call validate() from a dev test or diagnostic command. */
public final class NoiseFixture120 {
    private NoiseFixture120() {}

    public static void main(String[] args) {
        validate();
    }

    public static void validate() {
        validateRegistryShape();
        Xoroshiro128PlusPlus120 random = new Xoroshiro128PlusPlus120(0x0123456789ABCDEFL);
        long[] expectedLongs = {
                3300321642504257170L, -8275431626087785575L,
                -2888040651324837629L, 5234921442573541129L
        };
        for (int i = 0; i < expectedLongs.length; i++) assertLong("xoroshiro[" + i + "]", expectedLongs[i], random.nextLong());

        PositionalRandomFactory120 positional = new PositionalRandomFactory120(123456789L);
        // Independent dlr/dlt integer oracle, not a hash of the port's output.
        assertLong("position", -5750987074503092447L, positional.at(17, 241, -33).nextLong());
        assertLong("hash", 8943212876840677010L, positional.fromHashOf("minecraft:continentalness").nextLong());

        ImprovedNoise120 improved = new ImprovedNoise120(new Xoroshiro128PlusPlus120(42L));
        assertDouble("improved", -0.06533869021348865, improved.sample(12.25, 64.5, -7.75));
        OctavePerlinNoise120 octave = new OctavePerlinNoise120(42L, -3, 1.0, 1.0, 0.5, 0.25);
        assertDouble("octave", 0.34092573182643804, octave.sample(12.25, 64.5, -7.75));
        NormalNoise120 normal = new NormalNoise120(42L, -3, 1.0, 1.0, 0.5, 0.25);
        assertDouble("normal", 0.39815175962255467, normal.sample(12.25, 64.5, -7.75));
        InterpolatedNoise120 interpolated = new InterpolatedNoise120(42L);
        assertDouble("interpolated", -0.01094065335030775, interpolated.sample(12.25, 64.5, -7.75));

        assertParameters("continentalness", NoiseParameters120.CONTINENTALNESS, -9,
                1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0);
        assertParameters("erosion", NoiseParameters120.EROSION, -9, 1.0, 1.0, 0.0, 1.0, 1.0);
        assertParameters("ridge", NoiseParameters120.RIDGE, -7, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0);
    }

    /** Guards the complete built-in NoiseData registry, not only routed entries. */
    private static void validateRegistryShape() {
        NoiseParameters120[] entries = NoiseParameters120.values();
        if (entries.length != 60)
            throw new IllegalStateException("1.20.2 NoiseData entry count changed: " + entries.length);
        Set<String> locations = new HashSet<String>();
        for (NoiseParameters120 entry : entries) {
            String location = entry.resourceLocation();
            if (!location.startsWith("minecraft:") || !locations.add(location))
                throw new IllegalStateException("Invalid or duplicate NoiseData key: " + location);
            double[] amplitudes = entry.amplitudes();
            if (amplitudes.length == 0)
                throw new IllegalStateException("NoiseData entry has no amplitudes: " + location);
            for (double amplitude : amplitudes) if (!Double.isFinite(amplitude))
                throw new IllegalStateException("NoiseData entry has non-finite amplitude: " + location);
        }
    }

    private static void assertLong(String name, long expected, long actual) {
        if (expected != actual) throw new IllegalStateException(name + ": expected " + expected + ", got " + actual);
    }

    private static void assertDouble(String name, double expected, double actual) {
        if (Math.abs(expected - actual) > 1.0E-12)
            throw new IllegalStateException(name + ": expected " + expected + ", got " + actual);
    }

    private static void assertParameters(String name, NoiseParameters120 parameters,
                                         int expectedFirstOctave, double... expectedAmplitudes) {
        if (parameters.firstOctave() != expectedFirstOctave)
            throw new IllegalStateException(name + " first octave: expected " + expectedFirstOctave
                    + ", got " + parameters.firstOctave());
        double[] actual = parameters.amplitudes();
        if (actual.length != expectedAmplitudes.length)
            throw new IllegalStateException(name + " amplitude count: expected " + expectedAmplitudes.length
                    + ", got " + actual.length);
        for (int i = 0; i < actual.length; i++) assertDouble(name + " amplitude[" + i + "]", expectedAmplitudes[i], actual[i]);
    }
}
