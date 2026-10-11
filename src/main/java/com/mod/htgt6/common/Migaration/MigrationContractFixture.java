package com.mod.htgt6.common.Migaration;

/** Lightweight checks for the namespace rules used during a 1.7.10 world load. */
public final class MigrationContractFixture {
    private MigrationContractFixture() {}

    public static void main(String[] args) {
        expect("end_rod", MigrationRegistryName.pathFrom("HTGT6:end_rod", "HTGT6"));
        expect("tile.baseBlockRock", MigrationRegistryName.pathFrom(
                "GalacticraftAmunRa:tile.baseBlockRock", "GalacticraftAmunRa"));
        expect(null, MigrationRegistryName.pathFrom("HTGT6", "HTGT6"));
        expect(null, MigrationRegistryName.pathFrom("etfuturum:end_rod", "HTGT6"));
        expect(null, MigrationRegistryName.pathFrom("HTGT6:", "HTGT6"));
    }

    private static void expect(String expected, String actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
