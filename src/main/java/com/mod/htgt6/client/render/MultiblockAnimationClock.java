package com.mod.htgt6.client.render;

import java.util.WeakHashMap;

/** Client-only elapsed active time. Pauses preserve the pose; unloaded tiles are not retained. */
final class MultiblockAnimationClock {
    /** Widen before addition: long + float otherwise rounds old worlds to 64/128-tick steps. */
    static double frameTime(long worldTicks, float partialTicks) {
        return (double) worldTicks + partialTicks;
    }

    private final WeakHashMap<Object, Motion> motions=new WeakHashMap<Object, Motion>();
    private static final class Motion { double previous, elapsed; boolean initialized; }
    double sample(Object machine, double now, boolean active) {
        Motion m=motions.get(machine);
        if(m==null) { m=new Motion(); motions.put(machine,m); }
        if(m.initialized && active) m.elapsed+=Math.max(0,Math.min(2,now-m.previous));
        m.previous=now; m.initialized=true;
        return m.elapsed;
    }
}
