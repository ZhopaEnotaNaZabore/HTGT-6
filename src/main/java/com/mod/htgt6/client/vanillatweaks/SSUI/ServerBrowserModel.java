package com.mod.htgt6.client.vanillatweaks.SSUI;

import java.util.*;
import net.minecraft.client.multiplayer.ServerData;

/** Pure list projection: filtering never changes the ordering or entries in servers.dat. */
public final class ServerBrowserModel {
    private ServerBrowserModel() { }
    public static List<ServerData> filter(List<ServerData> servers, final ServerBrowserPreferences preferences,
                                           int tab, String query) {
        List<ServerData> result = new ArrayList<ServerData>();
        String needle = query.trim().toLowerCase(Locale.ROOT);
        for (ServerData server : servers) {
            if (tab == 1 && !preferences.favorite(server.serverIP)) continue;
            if (tab == 2 && preferences.recent(server.serverIP) == 0) continue;
            if ((server.serverName + " " + server.serverIP).toLowerCase(Locale.ROOT).contains(needle)) result.add(server);
        }
        if (tab == 2) Collections.sort(result, new Comparator<ServerData>() {
            public int compare(ServerData a, ServerData b) {
                return Long.compare(preferences.recent(b.serverIP), preferences.recent(a.serverIP));
            }
        });
        return result;
    }
}
