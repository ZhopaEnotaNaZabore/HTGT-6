import com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage.RaidHudState160;
import com.mod.htgt6.minecraft1202.Module.Network.VillageAndPillage.RaidNetwork150.Snapshot;

/** Diagnose semantic failures separately after only compiler-blocking errors are repaired. */
public final class Gemma160ReviewFixture {
    private static int passed, failed;
    public static void main(String[] args) {
        Object world = new Object(), nextWorld = new Object();
        Snapshot packet = new Snapshot(0, true, 0, 3, .5F);
        RaidHudState160 state = new RaidHudState160();
        state.accept(packet, world, 0);
        check(state.snapshot() != null, "first valid packet must be accepted");

        state = new RaidHudState160();
        state.tick(world); // Establish identity independently of accept's first-packet defect.
        state.accept(packet, world, 0);
        check(state.snapshot() != null, "same-world valid packet");
        state.accept(new Snapshot(2, true, 0, 3, .5F), nextWorld, 1);
        check(state.snapshot() == null, "world change invalidates old state before dimension rejection");

        state = new RaidHudState160(); state.tick(world); state.accept(packet, world, 0);
        for (int i = 0; i < 60; i++) state.tick(world);
        check(state.snapshot() != null, "alive at tick 60");
        state.tick(world); check(state.snapshot() == null, "expired at tick 61");

        state = new RaidHudState160(); state.tick(world); state.accept(packet, world, 0);
        Snapshot copy = state.snapshot(); copy.remaining = 100;
        check(state.snapshot().remaining == 3, "defensive copy");
        System.out.println("REVIEW: passed=" + passed + " failed=" + failed);
        if (failed != 0) throw new AssertionError("Semantic defects: " + failed);
    }
    private static void check(boolean value, String name) {
        System.out.println((value ? "PASS " : "FAIL ") + name);
        if (value) passed++; else failed++;
    }
}
