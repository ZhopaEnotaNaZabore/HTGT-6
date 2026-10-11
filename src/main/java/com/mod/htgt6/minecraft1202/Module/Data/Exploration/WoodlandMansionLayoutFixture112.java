package com.mod.htgt6.minecraft1202.Module.Data.Exploration;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.WoodlandMansionLayout112;

import java.util.List;

/** Fixed-seed regression for the isolated Minecraft 1.12 mansion layout port. */
public final class WoodlandMansionLayoutFixture112 {
    private static final long[] SEEDS = {0L, 1L, -1L, 0x1125A17E5L};
    private static final long[] EXPECTED = {0xb5c1154109dadd94L, 0x4f5377ebc5c49c9aL,
            0xe8623c68905b15a1L, 0x688a08e8da1155a2L};

    public static void main(String[] args) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < SEEDS.length; i++) {
            List<WoodlandMansionLayout112.Piece> first = WoodlandMansionLayout112.generate(SEEDS[i]);
            List<WoodlandMansionLayout112.Piece> second = WoodlandMansionLayout112.generate(SEEDS[i]);
            long hash = WoodlandMansionLayout112.fingerprint(SEEDS[i]);
            require(hash == WoodlandMansionLayout112.fingerprint(SEEDS[i]), "nondeterministic fingerprint");
            require(first.size() == second.size() && first.toString().equals(second.toString()),
                    "nondeterministic piece list");
            require(first.size() > 100, "mansion layout is implausibly small: " + first.size());
            List<WoodlandMansionLayout112.Piece> rotated = WoodlandMansionLayout112.generate(
                    new java.util.Random(SEEDS[i]), WoodlandMansionLayout112.Rotation.CLOCKWISE_90);
            require(rotated.size() == first.size(), "initial rotation changed layout piece count");
            require(!rotated.toString().equals(first.toString()), "initial rotation was ignored");
            if (EXPECTED[i] != 0L) require(hash == EXPECTED[i], "fingerprint changed for seed " + SEEDS[i]
                    + ": expected 0x" + Long.toHexString(EXPECTED[i]) + ", got 0x" + Long.toHexString(hash));
            boolean immutable = false;
            try { first.clear(); } catch (UnsupportedOperationException expected) { immutable = true; }
            require(immutable, "piece list must be immutable");
            if (i > 0) result.append(", ");
            result.append(SEEDS[i]).append('=').append(first.size()).append("/0x")
                    .append(Long.toHexString(hash));
        }
        System.out.println("Woodland mansion 1.12 layout fixture OK; " + result);
    }

    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    private WoodlandMansionLayoutFixture112() { }
}
