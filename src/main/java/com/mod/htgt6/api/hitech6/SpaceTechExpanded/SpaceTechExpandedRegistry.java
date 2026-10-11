package com.mod.htgt6.api.hitech6.SpaceTechExpanded;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import micdoodle8.mods.galacticraft.api.galaxies.Star;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;

/** Public registry for custom cosmic objects independent from GalaxySpace GUI code. */
public final class SpaceTechExpandedRegistry {
    public static final int NO_DESTINATION = Integer.MIN_VALUE;
    private static final Map<String, SpaceMapObject> OBJECTS = new LinkedHashMap<String, SpaceMapObject>();
    private static final Map<String, SpaceStarData> STARS = new LinkedHashMap<String, SpaceStarData>();
    private static final Map<String, BinaryStarSystemData> BINARY_SYSTEMS =
            new LinkedHashMap<String, BinaryStarSystemData>();
    private static final Map<String, SpaceHabitableZoneData> HABITABLE_ZONES =
            new LinkedHashMap<String, SpaceHabitableZoneData>();
    private static final Map<String, Integer> SYSTEM_TRAVEL_TIERS = new LinkedHashMap<String, Integer>();
    private static final Map<String, Star> SECONDARY_STARS = new LinkedHashMap<String, Star>();

    private SpaceTechExpandedRegistry() {}

    public static synchronized void register(SpaceMapObject object) {
        if (OBJECTS.containsKey(object.id)) throw new IllegalArgumentException("Duplicate SpaceTech object: " + object.id);
        OBJECTS.put(object.id, object);
    }

    public static synchronized SpaceMapObject get(String id) {
        return OBJECTS.get(id);
    }

    /** Objects are append-only, so the count also serves as a catalogue revision. */
    public static synchronized int getObjectCount() { return OBJECTS.size(); }

    public static synchronized void registerStarData(SpaceStarData data) {
        STARS.put(data.starName, data);
    }

    /** Registers a real Star which Galacticraft cannot store as a system's second main star. */
    public static synchronized void registerSecondaryStar(Star star) {
        if (star == null || star.getParentSolarSystem() == null) {
            throw new IllegalArgumentException("A secondary star and its solar system are required");
        }
        SECONDARY_STARS.put(normalizeStarKey(star.getName()), star);
    }

    public static synchronized Star getSecondaryStar(String starName) {
        return SECONDARY_STARS.get(normalizeStarKey(starName));
    }

    public static synchronized List<Star> getSecondaryStars() {
        return Collections.unmodifiableList(new ArrayList<Star>(SECONDARY_STARS.values()));
    }

    /** Bodies owned by the extensible SpaceTech map pass rather than GalaxyRegistry. */
    public static synchronized List<CelestialBody> getCustomMapBodies() {
        List<CelestialBody> bodies = new ArrayList<CelestialBody>();
        bodies.addAll(SECONDARY_STARS.values());
        return Collections.unmodifiableList(bodies);
    }

    public static synchronized SpaceStarData getStarData(String starName) {
        SpaceStarData direct = STARS.get(starName);
        if (direct != null) return direct;
        String normalized = normalizeStarKey(starName);
        for (SpaceStarData data : STARS.values()) {
            if (normalizeStarKey(data.starName).equals(normalized)) return data;
        }
        return null;
    }

    /** Registers metadata for a visual binary pair; it does not create a travel destination. */
    public static synchronized void registerBinaryStarSystem(BinaryStarSystemData data) {
        BINARY_SYSTEMS.put(normalizeStarKey(data.solarSystemName), data);
    }

    public static synchronized BinaryStarSystemData getBinarySystemForBody(String bodyName) {
        for (BinaryStarSystemData data : BINARY_SYSTEMS.values()) {
            if (data.containsBody(bodyName)) return data;
        }
        return null;
    }

    public static synchronized BinaryStarSystemData getBinarySystem(String solarSystemName) {
        return BINARY_SYSTEMS.get(normalizeStarKey(solarSystemName));
    }

    /** Relative light emitted by a registered star; ordinary stars default to one sun. */
    public static synchronized float getStellarLuminosity(String bodyName) {
        BinaryStarSystemData data = getBinarySystemForBody(bodyName);
        return data == null ? 1.0F : data.getLuminosity(bodyName);
    }

    /** Sum of both emitters for binary systems; ordinary systems default to one sun. */
    public static synchronized float getCombinedStellarLuminosity(String solarSystemName) {
        BinaryStarSystemData data = getBinarySystem(solarSystemName);
        return data == null ? 1.0F : data.getCombinedLuminosity();
    }

    /** Sets an explicit Galacticraft rocket tier for systems outside GalaxySpace's route graph. */
    public static synchronized void registerSystemTravelTier(String solarSystemName, int tier) {
        if (tier <= 0) throw new IllegalArgumentException("Travel tier must be positive");
        SYSTEM_TRAVEL_TIERS.put(normalizeStarKey(solarSystemName), Integer.valueOf(tier));
    }

    public static synchronized int getSystemTravelTier(String solarSystemName) {
        Integer tier = SYSTEM_TRAVEL_TIERS.get(normalizeStarKey(solarSystemName));
        return tier == null ? -1 : tier.intValue();
    }

    public static synchronized boolean isBinaryCompanion(String bodyName) {
        BinaryStarSystemData data = getBinarySystemForBody(bodyName);
        return data != null && data.isCompanion(bodyName);
    }

    public static synchronized void registerHabitableZone(SpaceHabitableZoneData data) {
        HABITABLE_ZONES.put(normalizeStarKey(data.solarSystemName), data);
    }

    public static synchronized SpaceHabitableZoneData getHabitableZone(String solarSystemName) {
        return HABITABLE_ZONES.get(normalizeStarKey(solarSystemName));
    }

    public static synchronized SpaceHabitableZoneData getHabitableZoneForBody(String bodyName) {
        if (bodyName == null) return null;
        for (SpaceHabitableZoneData data : HABITABLE_ZONES.values()) {
            if (data.referenceBodyName.equalsIgnoreCase(bodyName)) return data;
        }
        return null;
    }

    public static synchronized List<SpaceMapObject> getObjects() {
        return Collections.unmodifiableList(new ArrayList<SpaceMapObject>(OBJECTS.values()));
    }

    /** Returns the visual-only centre registered for a galaxy, if any. */
    public static synchronized GalaxyCentre getGalaxyCentre(String galaxyKey) {
        for (SpaceMapObject object : OBJECTS.values()) {
            if (object.galaxyKey != null && galaxyKeysMatch(object.galaxyKey, galaxyKey)
                    && object.type == SpaceMapObjectType.GALAXY && object.blackHoleName != null) {
                return new GalaxyCentre(object.galaxyKey, object.blackHoleName, 0.0F, 0.0F);
            }
        }
        return null;
    }

    private static boolean galaxyKeysMatch(String first, String second) {
        return normalizeGalaxyKey(first).equals(normalizeGalaxyKey(second));
    }

    private static String normalizeGalaxyKey(String key) {
        if (key == null) return "";
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("galaxy.")) normalized = normalized.substring("galaxy.".length());
        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private static String normalizeStarKey(String key) {
        if (key == null) return "";
        String normalized = key.toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("star.")) normalized = normalized.substring("star.".length());
        return normalized.replaceAll("[^a-z0-9]", "");
    }

    private static void registerDefault(SpaceMapObject object) {
        if (!OBJECTS.containsKey(object.id)) register(object);
    }

    public static synchronized void bootstrapDefaults() {
        {
            registerDefault(new SpaceMapObject("milky_way", "galaxy.milkyWay", "sulimited.galaxy.milky_way", SpaceMapObjectType.GALAXY, GalaxyMapForm.SPIRAL, "Sagittarius A*", 0.23F, 0.31F, NO_DESTINATION));
            registerDefault(new SpaceMapObject("andromeda", "galaxy.andromeda", "sulimited.galaxy.andromeda", SpaceMapObjectType.GALAXY, GalaxyMapForm.SPIRAL, "Andromeda A*", 0.72F, 0.26F, NO_DESTINATION));
            registerDefault(new SpaceMapObject("triangle", "galaxy.Triangle", "sulimited.galaxy.triangle", SpaceMapObjectType.GALAXY, GalaxyMapForm.SPIRAL, "M33 X-7", 0.49F, 0.54F, NO_DESTINATION));
            registerDefault(new SpaceMapObject("m12", "galaxy.M12", "sulimited.galaxy.m12", SpaceMapObjectType.CLUSTER, GalaxyMapForm.OTHER, null, 0.81F, 0.66F, NO_DESTINATION));
            // Micro Galaxy is the dedicated home for fan-made systems.  Like
            // spiral galaxies it has a non-travel centre, while retaining its
            // distinct non-spiral presentation in the catalog.
            registerDefault(new SpaceMapObject("micro", "galaxy.Micro", "sulimited.galaxy.micro", SpaceMapObjectType.GALAXY, GalaxyMapForm.OTHER, "UXDS-700", 0.18F, 0.72F, NO_DESTINATION));
            registerDefault(new SpaceMapObject("nebula", "galaxy.Nebula", "sulimited.galaxy.nebula", SpaceMapObjectType.NEBULA, GalaxyMapForm.NEBULA, null, 0.57F, 0.82F, NO_DESTINATION));
        }
        if (getStarData("Gliese792_star") == null) registerStarData(new SpaceStarData("Gliese792_star", "sulimited.star.type.red_dwarf", 3500.0D));
        if (getStarData("sirius_a") == null) registerStarData(new SpaceStarData("sirius_a", "sulimited.star.type.white_dwarf", 9940.0D));
    }
}
