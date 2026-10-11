package com.mod.htgt6.server.utils.Netty;

import com.mod.htgt6.asm.HTGT6Config;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.io.File;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import net.minecraft.network.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Shared client/server debugger; keeps type names, never packet bodies, players or buffers. */
final class NetworkDebugLog {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-PacketFailure");
    private static final AttributeKey<State> STATE = new AttributeKey<State>("htgt6_network_debug_context");
    private static final AtomicLong IDS = new AtomicLong();
    private static final Map<String, java.util.logging.Logger> FILES = new HashMap<String, java.util.logging.Logger>();
    private static long windowStart = System.nanoTime();
    private static int reports, suppressed;

    private static final class State {
        final long id = IDS.incrementAndGet();
        String side = "unknown";
        final ArrayDeque<String> history = new ArrayDeque<String>();
        final LinkedHashSet<String> failures = new LinkedHashSet<String>();
    }

    private static State state(Channel channel) {
        State found = channel.attr(STATE).get();
        if (found != null) return found;
        State created = new State();
        State raced = channel.attr(STATE).setIfAbsent(created);
        return raced == null ? created : raced;
    }

    static void side(Channel channel, String side) {
        if (!HTGT6Config.NIO_DEBUG || channel == null) return;
        State state = state(channel);
        synchronized (state) { state.side = side; }
    }

    static void record(Channel channel, String direction, Object packet) {
        if (!HTGT6Config.NIO_DEBUG || channel == null) return;
        State state = state(channel);
        synchronized (state) {
            if (state.history.size() == 8) state.history.removeFirst();
            state.history.addLast(direction + ":" + (packet == null ? "<null>" : packet.getClass().getName()));
        }
    }

    static synchronized boolean allowReport() {
        long now = System.nanoTime();
        if (now - windowStart >= 60000000000L) {
            if (suppressed > 0) LOGGER.warn("Network debugger suppressed {} reports in the previous minute", suppressed);
            windowStart = now; reports = 0; suppressed = 0;
        }
        if (reports >= 64) { suppressed++; return false; }
        reports++;
        return true;
    }

    static void report(Channel channel, String phase, Throwable failure, String handler) {
        if (!HTGT6Config.NIO_DEBUG || failure == null || failure instanceof ClosedChannelException) return;
        try {
            State state = channel == null ? new State() : state(channel);
            String side, message;
            synchronized (state) {
                // Keep different failures, suppress repeat propagation and cap retained diagnostic data.
                String signature = failure.getClass().getName() + ":" + bounded(failure.getMessage())
                        + ":" + (failure.getStackTrace().length == 0 ? "" : failure.getStackTrace()[0]);
                if (state.failures.contains(signature) || state.failures.size() >= 16 || !allowReport()) return;
                state.failures.add(signature);
                side = state.side;
                message = "connection=" + state.id + " side=" + side + " phase=" + phase
                        + " thread=" + Thread.currentThread().getName() + " handler=" + handler
                        + " recentPackets=" + state.history;
            }
            if (channel != null) message += " remote=" + channel.remoteAddress() + " local=" + channel.localAddress()
                    + " protocol=" + channel.attr(NetworkManager.attrKeyConnectionState).get()
                    + " active=" + channel.isActive() + " pipeline=" + channel.pipeline().names();
            LOGGER.error(message, failure);
            writeFile(side, message, failure);
        } catch (Throwable ignored) { /* Logging cannot change exception propagation or disconnect behaviour. */ }
    }

    private static String bounded(String value) {
        return value == null ? "" : value.substring(0, Math.min(256, value.length()));
    }

    private static synchronized void writeFile(String side, String message, Throwable failure) throws Exception {
        if (!"client".equals(side) && !"server".equals(side)) side = "unknown";
        java.util.logging.Logger logger = FILES.get(side);
        if (logger == null) {
            File directory = HTGT6Config.NETWORK_DEBUG_LOG_DIRECTORY;
            if (!directory.isDirectory() && !directory.mkdirs()) return;
            // JUL keeps this working independently of the host's Log4j version/configuration.
            FileHandler file = new FileHandler(new File(directory, "htgt6-network-" + side + "-%g.log").getPath(),
                    4 * 1024 * 1024, 3, true);
            file.setEncoding("UTF-8");
            file.setFormatter(new SimpleFormatter());
            logger = java.util.logging.Logger.getLogger("HTGT6.NetworkDebugFile." + side);
            logger.setUseParentHandlers(false);
            logger.addHandler(file);
            FILES.put(side, logger);
        }
        logger.log(Level.SEVERE, message, failure);
    }
}
