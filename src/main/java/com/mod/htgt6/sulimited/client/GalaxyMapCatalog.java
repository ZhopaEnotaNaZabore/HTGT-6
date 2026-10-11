package com.mod.htgt6.sulimited.client;

import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceMapObject;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceMapObjectType;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.GalaxyMapForm;
import com.mod.htgt6.api.hitech6.SpaceTechExpanded.SpaceTechExpandedRegistry;
import galaxyspace.api.BodiesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Cached top-level catalogue, refreshed when addons register more objects. */
public final class GalaxyMapCatalog {
    public static final class Entry {
        public final String id;
        public final String galaxyKey;
        public final String translationKey;
        public final SpaceMapObjectType type;
        public final GalaxyMapForm form;
        public final String blackHoleName;
        public final float mapX, mapY;

        private Entry(String id, String galaxyKey, String translationKey, SpaceMapObjectType type,
                      GalaxyMapForm form, String blackHoleName, float mapX, float mapY) {
            this.id = id;
            this.galaxyKey = galaxyKey;
            this.translationKey = translationKey;
            this.type = type;
            this.form = form;
            this.blackHoleName = blackHoleName;
            this.mapX = mapX;
            this.mapY = mapY;
        }
    }

    private static List<Entry> entries = Collections.emptyList();
    private static int revision = -1;

    static {
        SpaceTechExpandedRegistry.bootstrapDefaults();
    }

    public static List<Entry> getEntries() {
        int current = SpaceTechExpandedRegistry.getObjectCount();
        if (revision == current) return entries;
        List<Entry> next = new ArrayList<Entry>();
        for (SpaceMapObject object : SpaceTechExpandedRegistry.getObjects()) {
            if (object.opensGalaxyMap()) {
                Entry existing = null;
                for (Entry entry : entries) if (entry.id.equals(object.id)) { existing = entry; break; }
                next.add(existing != null ? existing : new Entry(object.id, object.galaxyKey, object.translationKey, object.type,
                        object.form, object.blackHoleName, object.mapX, object.mapY));
            }
        }
        entries = Collections.unmodifiableList(next);
        revision = current;
        return entries;
    }

    private GalaxyMapCatalog() {}

    /**
     * GalaxySpace uses this map to resolve a background for a galaxy's inner
     * view.  A shared stable fallback prevents missing-texture screens until
     * dedicated art is supplied for a catalog entry.
     */
    public static void registerInnerMapBackgrounds() {
        final String fallback = "galaxyspace:textures/gui/galaxy/galaxy.milkyway.png";
        for (Entry entry : getEntries()) {
            if ("galaxy.milkyWay".equals(entry.galaxyKey)) continue;
            BodiesHelper.registerGalaxyImg(entry.galaxyKey.replace("galaxy.", ""), fallback);
        }
    }
}
