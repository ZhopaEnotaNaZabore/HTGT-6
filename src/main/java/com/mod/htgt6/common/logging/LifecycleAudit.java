package com.mod.htgt6.common.logging;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Emits stable, grep-friendly markers for the Forge 1.7.10 loading phases. */
public final class LifecycleAudit {

    private static final Logger LOGGER = LogManager.getLogger("HTGT6-Lifecycle");

    public static long begin(String modId, String phase) {
        LOGGER.info("LIFECYCLE [{}] {} BEGIN", modId, phase);
        return System.nanoTime();
    }

    public static void end(String modId, String phase, long startedAt) {
        long elapsedNanos = Math.max(0L, System.nanoTime() - startedAt);
        double elapsedMillis = elapsedNanos / (double) TimeUnit.MILLISECONDS.toNanos(1L);
        LOGGER.info("LIFECYCLE [{}] {} COMPLETE ({} ms)", modId, phase,
                String.format(Locale.ROOT, "%.3f", elapsedMillis));
    }

    private LifecycleAudit() { }
}
