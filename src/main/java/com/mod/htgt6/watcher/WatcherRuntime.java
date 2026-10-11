package com.mod.htgt6.watcher;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Client monitor. Disk IO never runs on the rendering thread. */
public final class WatcherRuntime {
    private static final Logger LOG = LogManager.getLogger("HTGT6-Watcher");
    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override public Thread newThread(Runnable job) {
            Thread thread = new Thread(job, "HTGT6-Watcher");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        }
    });
    private static final Set<String> MESSAGES = new HashSet<String>();
    private static final WatcherChecks.Findings FINDINGS = new WatcherChecks.Findings() {
        @Override public void violation(String code, String detail) {
            Watcher.STATE.report(code + ": " + detail);
            logOnce("VIOLATION " + code + ": " + detail);
        }
        @Override public void unavailable(String check, Exception cause) {
            // An unreadable file / unsupported JVM is not proof of cheating.
            logOnce("CHECK_FAILED " + check + ": " + cause.getClass().getSimpleName());
        }
    };
    private static WatcherChecks checks;
    private static boolean bootstrapComplete;
    private static volatile boolean runtimePending;
    private static boolean firstTick = true;
    private static long nextPoll;
    private static long nextFiles;

    private WatcherRuntime() { }

    public static void checkpoint(WatcherState.Phase phase) {
        if (!FMLLaunchHandler.side().isClient()) return;
        try { submit(capture(), false, true, phase.name()); }
        catch (Exception e) { FINDINGS.unavailable("startup snapshot", e); }
    }

    /** Called from client tick END, after the full loader has dispatched LoadComplete. */
    public static void tick() {
        if (Watcher.STATE.getPhase() != WatcherState.Phase.LOAD_COMPLETE || Watcher.STATE.shouldDisplay()) return;
        long now = System.nanoTime();
        if (runtimePending || (!firstTick && now < nextPoll)) return;
        boolean seal = firstTick;
        boolean files = seal || now >= nextFiles;
        try {
            Snapshot snapshot = capture();
            firstTick = false;
            nextPoll = now + TimeUnit.SECONDS.toNanos(3);
            if (files) nextFiles = now + TimeUnit.SECONDS.toNanos(30);
            runtimePending = true;
            submit(snapshot, seal, files, "RUNTIME");
        } catch (Exception e) { FINDINGS.unavailable("runtime snapshot", e); }
    }

    private static void submit(final Snapshot snapshot, final boolean seal, final boolean files, final String phase) {
        WORKER.execute(new Runnable() {
            @Override public void run() {
                try {
                    if (Watcher.STATE.shouldDisplay()) return;
                    if (checks == null) {
                        File home = Launch.minecraftHome == null ? new File(".") : Launch.minecraftHome;
                        checks = new WatcherChecks(new File(home, "mods").toPath(), FINDINGS);
                        checks.initialize(snapshot.files, snapshot.arguments, snapshot.systemSources);
                        LOG.info("Watcher session baseline captured; startup mods, ASM and launcher agents accepted");
                    }
                    // Observe agents before admitting sources discovered during ordinary mod loading.
                    checkAgents(snapshot.systemSources);
                    if (files) checks.files(snapshot.files, !bootstrapComplete);
                    if (bootstrapComplete) checks.transformers(snapshot.transformerFiles);
                    if (seal) {
                        bootstrapComplete = true;
                        LOG.info("Watcher bootstrap complete; monitoring new agent/transformer code and content changes");
                    }
                    if (snapshot.unknownTransformerOrigin) logOnce("CHECK_FAILED ASM code source unavailable (generated proxy); no violation inferred");
                    LOG.debug("Watcher scan finished: {}", phase);
                } catch (Exception e) { FINDINGS.unavailable("scan " + phase, e); }
                catch (LinkageError e) { logOnce("CHECK_FAILED incompatible runtime: " + e.getClass().getSimpleName()); }
                finally { if ("RUNTIME".equals(phase)) runtimePending = false; }
            }
        });
    }

    private static void checkAgents(URL[] systemSources) {
        try {
            final ClassLoader system = ClassLoader.getSystemClassLoader();
            if (!(system instanceof URLClassLoader)) {
                logOnce("CHECK_FAILED dynamic agents: system loader is not a Java 8 URLClassLoader");
                return;
            }
            final Method lookup = ClassLoader.class.getDeclaredMethod("findLoadedClass", String.class);
            lookup.setAccessible(true);
            checks.agents(systemSources,
                    new WatcherChecks.LoadedClass() {
                        @Override public boolean contains(String name, Path source) throws Exception {
                            Class<?> type = (Class<?>) lookup.invoke(system, name);
                            if (type == null || type.getProtectionDomain().getCodeSource() == null) return false;
                            URL origin = type.getProtectionDomain().getCodeSource().getLocation();
                            return "file".equals(origin.getProtocol()) && Files.isSameFile(source, Paths.get(origin.toURI()));
                        }
                    });
        } catch (Exception e) { FINDINGS.unavailable("dynamic agents", e); }
    }

    private static Snapshot capture() throws Exception {
        Snapshot snapshot = new Snapshot();
        ClassLoader system = ClassLoader.getSystemClassLoader();
        snapshot.systemSources = system instanceof URLClassLoader ? ((URLClassLoader) system).getURLs() : new URL[0];
        snapshot.arguments = new ArrayList<String>(ManagementFactory.getRuntimeMXBean().getInputArguments());
        for (IClassTransformer transformer : Launch.classLoader.getTransformers()) {
            Class<?> type = transformer.getClass();
            URL origin = type.getProtectionDomain().getCodeSource() == null ? null
                    : type.getProtectionDomain().getCodeSource().getLocation();
            if (origin != null && "file".equals(origin.getProtocol())) snapshot.transformerFiles.add(Paths.get(origin.toURI()));
            else snapshot.unknownTransformerOrigin = true;
            snapshot.add(origin);
        }
        for (URL url : Launch.classLoader.getURLs()) snapshot.add(url);
        snapshot.add(WatcherRuntime.class.getProtectionDomain().getCodeSource().getLocation());
        return snapshot;
    }

    private static final class Snapshot {
        final Set<Path> files = new LinkedHashSet<Path>();
        URL[] systemSources;
        List<String> arguments;
        final Set<Path> transformerFiles = new LinkedHashSet<Path>();
        boolean unknownTransformerOrigin;
        void add(URL url) throws Exception {
            if (url != null && "file".equals(url.getProtocol())) files.add(Paths.get(url.toURI()));
        }
    }

    private static synchronized void logOnce(String message) {
        if (MESSAGES.size() < 128 && MESSAGES.add(message)) LOG.warn("Watcher {}", message);
    }
}
