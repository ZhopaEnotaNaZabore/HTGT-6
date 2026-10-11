package com.mod.htgt6.watcher;

/** Thread-safe violation latch shared by background checks and the client thread. */
public final class WatcherState {
    public enum Phase { CONSTRUCTION, PRE_INIT, INIT, POST_INIT, LOAD_COMPLETE }

    private Phase phase;
    private String finding;
    private boolean complete;

    public synchronized void checkpoint(Phase next) {
        if (next == null) throw new IllegalArgumentException("phase");
        phase = next;
        if (next == Phase.LOAD_COMPLETE) complete = true;
    }

    /** Detectors report policy violations here; the first reason is retained. */
    public synchronized void report(String reason) {
        if (reason == null || reason.trim().isEmpty()) throw new IllegalArgumentException("reason");
        if (finding == null) finding = reason;
    }

    public synchronized boolean shouldDisplay() { return complete && finding != null; }
    public synchronized Phase getPhase() { return phase; }
    public synchronized String getFinding() { return finding; }
}
