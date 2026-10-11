package com.mod.htgt6.client.vanillatweaks.CHAT;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/** Bounded, atomic admission from Netty threads; drained on the server thread. */
final class ChatImageIngress<T> {
    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);
    private final ArrayDeque<T> pending = new ArrayDeque<T>();
    private final Map<UUID, Long> accepted = new HashMap<UUID, Long>();
    private long windowStart, nextCleanup;
    private int windowCount;
    private boolean initialized;

    synchronized boolean offer(UUID sender, T value, long now) {
        if (!initialized) { windowStart = nextCleanup = now; initialized = true; }
        Long previous = accepted.get(sender);
        if (previous != null && now - previous < 2 * SECOND) return false;
        if (now - windowStart >= SECOND) { windowStart = now; windowCount = 0; }
        if (windowCount >= 8 || pending.size() >= 128) return false;
        if (now - nextCleanup >= 0) {
            Iterator<Long> it = accepted.values().iterator();
            while (it.hasNext()) if (now - it.next() >= 2 * SECOND) it.remove();
            nextCleanup = now + 2 * SECOND;
        }
        if (accepted.size() >= 1024 && !accepted.containsKey(sender)) return false;
        accepted.put(sender, now);
        pending.addLast(value);
        windowCount++;
        return true;
    }

    synchronized T poll() { return pending.pollFirst(); }
    synchronized void clear() { pending.clear(); accepted.clear(); windowCount = 0; initialized = false; }
}
