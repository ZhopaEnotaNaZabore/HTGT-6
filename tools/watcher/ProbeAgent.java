package watchertest;

/** Harmless integration-test agent, loaded only into a disposable fixture JVM. */
public final class ProbeAgent {
    public static void premain(String ignored) { System.setProperty("watcher.fixture.agentRan", "true"); }
    public static void agentmain(String ignored) { System.setProperty("watcher.fixture.agentRan", "true"); }
}
