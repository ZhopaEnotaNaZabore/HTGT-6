package com.mod.htgt6.client.vanillatweaks.SSUI;

import java.util.*;
import java.util.concurrent.*;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.OldServerPinger;

/** DNS/connect work off the render thread; packet callbacks are processed on the client tick. */
final class ServerPingSession {
    private static final ThreadPoolExecutor WORKERS = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(32), new ThreadFactory() {
                public Thread newThread(Runnable work) {
                    Thread t = new Thread(work, "HTGT6-ServerPing"); t.setDaemon(true); return t;
                }
            }, new ThreadPoolExecutor.AbortPolicy());
    private final Map<ServerData, Ping> pings = new IdentityHashMap<ServerData, Ping>();
    private boolean closed;

    void request(final ServerData server) {
        if (closed || pings.containsKey(server)) return;
        final Ping ping = new Ping(server);
        try {
            ping.future = WORKERS.submit(new Runnable() {
                public void run() {
                    try {
                        if (!ping.closed) ping.pinger.func_147224_a(server);
                    } catch (Exception e) { ping.failed = true; }
                    finally {
                        ping.started = true;
                        // A DNS/connect operation may finish after this screen was closed.
                        if (ping.closed) ping.pinger.func_147226_b();
                    }
                }
            });
            pings.put(server, ping);
        } catch (RejectedExecutionException ignored) { /* Retry visible entries next frame. */ }
    }
    boolean pending(ServerData server) {
        Ping ping = pings.get(server);
        return ping == null || !ping.closed;
    }
    void tick() {
        for (Ping ping : pings.values()) {
            if (ping.closed) continue;
            if (ping.started && !ping.failed) {
                try { ping.pinger.func_147223_a(); }
                catch (Exception e) { ping.failed = true; }
            }
            if (ping.failed || System.nanoTime() - ping.created > TimeUnit.SECONDS.toNanos(12)) {
                ping.server.pingToServer = -1;
                ping.close();
            } else if (ping.server.pingToServer >= 0) ping.close();
        }
    }
    void close() {
        closed = true;
        for (Ping ping : pings.values()) ping.close();
        WORKERS.purge();
    }
    private static final class Ping {
        final ServerData server;
        final OldServerPinger pinger = new OldServerPinger();
        final long created = System.nanoTime();
        volatile boolean closed, started, failed;
        Future<?> future;
        Ping(ServerData server) { this.server = server; server.pingToServer = -2; server.serverMOTD = ""; server.populationInfo = ""; }
        void close() {
            closed = true;
            if (future != null) future.cancel(true);
            pinger.func_147226_b();
        }
    }
}
