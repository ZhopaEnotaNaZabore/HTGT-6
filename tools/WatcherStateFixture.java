import com.mod.htgt6.watcher.WatcherState;

public final class WatcherStateFixture {
    public static void main(String[] args) {
        WatcherState clean = new WatcherState();
        for (WatcherState.Phase phase : WatcherState.Phase.values()) {
            clean.checkpoint(phase);
            check(!clean.shouldDisplay(), "clean startup must remain unchanged");
        }
        for (WatcherState.Phase detectedAt : WatcherState.Phase.values()) {
            WatcherState state = new WatcherState();
            for (WatcherState.Phase phase : WatcherState.Phase.values()) {
                state.checkpoint(phase);
                if (phase == detectedAt) state.report("confirmed");
                check(state.shouldDisplay() == (phase == WatcherState.Phase.LOAD_COMPLETE),
                        "a finding must wait until LoadComplete");
            }
            state.report("second");
            check("confirmed".equals(state.getFinding()), "retain first finding");
        }
        System.out.println("Watcher state fixture: PASS");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
