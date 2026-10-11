package com.mod.htgt6.client.vanillatweaks.TAB;




import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TABDataHolder {
    public static volatile double serverTPS = 20.0D;
    /** Complete, immutable snapshot: never expose a map while it is being changed. */
    public static volatile Map<String, Integer> playerPings = Collections.emptyMap();

    public static void update(double tps, Map<String, Integer> pings) {
        serverTPS = tps;
        playerPings = Collections.unmodifiableMap(new HashMap<String, Integer>(pings));
    }
}
