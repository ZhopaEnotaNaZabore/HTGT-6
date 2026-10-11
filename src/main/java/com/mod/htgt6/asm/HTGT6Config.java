package com.mod.htgt6.asm;

import com.mod.htgt6.common.Migaration.LegacyConfigMigration;

import net.minecraftforge.common.config.Configuration;
import java.io.File;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public final class HTGT6Config {
    public static boolean FIX_SKIN_PORT_BROKEN_RENDER = true;
    /** Common client/server network debugger; explicit config false disables it. */
    public static volatile boolean NIO_DEBUG = true;
    public static File NETWORK_DEBUG_LOG_DIRECTORY = new File("logs");

    public static boolean ENABLE_NEW_FML_LOADING_SCREEN = true;

    /** Early LWJGL window title/icon branding owned by HTGT6. */
    public static boolean ENABLE_WINDOW_BRANDING = true;
    public static String WINDOW_TITLE =
            "Minecraft 1.7.20.2 // HiTech6 V1.0.4 - BUILD-13-Unstable";

    /** Responsive, localized main menu owned by HTGT6. */
    public static boolean ENABLE_CUSTOM_MAIN_MENU = true;

    /** Responsive, localized root options menu owned by HTGT6. */
    public static boolean ENABLE_CUSTOM_OPTIONS_MENU = true;

    /** Compact left-side pause menu owned by HTGT6. */
    public static boolean ENABLE_CUSTOM_PAUSE_MENU = true;

    /** One switch for every implemented Minecraft 1.8+ backport. */
    public static boolean RUN_MINECRAFT_1_7_9999 = true;

    /** AUTO, SIDE_BY_SIDE or OVERLAY; mirrors the responsive 1.12 recipe-book layout. */
    public static String INVENTORY_112_RECIPE_BOOK_LAYOUT = "AUTO";

    /** Minimum distance in scaled pixels between the extended inventory UI and the screen edge. */
    public static int INVENTORY_112_UI_SAFE_MARGIN = 4;

    /** LEFT or RIGHT side of the vanilla hotbar for the BackHand HUD slot. */
    public static String INVENTORY_112_HUD_OFFHAND_SIDE = "LEFT";

    // 1. Define the public global boolean field
    public static boolean ENABLE_CLIMATE_SYSTEM = true;

    /** Derived from the unified backport switch; Futura requires these patches. */
    public static boolean ENABLE_EXTENDED_BIOME_ASM = true;

    /** Physical cloud-plane Y used by the translated 0..383 Futura renderer. */
    public static int FUTURA_CLOUD_HEIGHT = 384;

    /** Keeps GT6 special waters static after worldgen to prevent scheduled-tick cascades. */
    public static boolean FIX_GT6_WATER_TICK_UPDATE = true;

    /** Builds GT6 stone layers in the raw chunk arrays instead of a populate-stage replacement pass. */
    public static boolean FIX_GT6_STONE_GEN_LAG = true;

    /** Recounts loaded chunk sections from packed IDs without per-block virtual lookups. */
    public static boolean OPTIMIZE_CHUNK_SECTION_LOADING = true;

    /** Prevents GT6 cauldron ore washing from passing a zero bound to Random.nextInt. */
    public static boolean FIX_GT6_CAUDRON_ISE = true;

    /** 1.13 ocean biome family stored through the extended-biome ASM map. */
    public static int WARM_OCEAN_BIOME_ID = 350;
    public static int DEEP_WARM_OCEAN_BIOME_ID = 351;
    public static int LUKEWARM_OCEAN_BIOME_ID = 352;
    public static int DEEP_LUKEWARM_OCEAN_BIOME_ID = 353;
    public static int COLD_OCEAN_BIOME_ID = 354;
    public static int DEEP_COLD_OCEAN_BIOME_ID = 355;
    public static int DEEP_FROZEN_OCEAN_BIOME_ID = 356;

    /** Stable 1.7 numeric IDs; enchantment NBT stores these values directly. */
    public static int LOYALTY_ENCHANTMENT_ID = 250;
    public static int IMPALING_ENCHANTMENT_ID = 249;
    public static int RIPTIDE_ENCHANTMENT_ID = 248;
    public static int CHANNELING_ENCHANTMENT_ID = 247;
    public static int QUICK_CHARGE_ENCHANTMENT_ID = 246;
    public static int MULTISHOT_ENCHANTMENT_ID = 245;
    public static int PIERCING_ENCHANTMENT_ID = 244;
    public static int SOUL_SPEED_ENCHANTMENT_ID = 243;

    /** Shared 1.7 potion registry slots; configurable because large packs reuse vanilla 1.9 IDs 24-27. */
    public static int GLOWING_POTION_ID = 1024;
    public static int LEVITATION_POTION_ID = 1025;
    public static int LUCK_POTION_ID = 1026;
    public static int UNLUCK_POTION_ID = 1027;
    public static int DOLPHINS_GRACE_POTION_ID = 1028;
    public static int CONDUIT_POWER_POTION_ID = 1029;
    public static int BAD_OMEN_POTION_ID = 1030;
    public static int HERO_OF_VILLAGE_POTION_ID = 1031;

    public static void load(File minecraftDir) {
        NETWORK_DEBUG_LOG_DIRECTORY = new File(minecraftDir, "logs");
        File cfgFile = getConfigFile(new File(minecraftDir, "config"), "HiTech6.cfg");

        Configuration cfg = new Configuration(cfgFile, null, true);
        cfg.load();
        FIX_SKIN_PORT_BROKEN_RENDER = cfg.get("compatibility", "FixSkinPortBrokenRender", true,
                "Give each SkinPort profile its own default skin so cache eviction cannot corrupt other players. Restart required; ignored without SkinPort.").getBoolean(true);
        NIO_DEBUG = cfg.get("==DEV DEBUG==", "NIODebug", true,
                "Common client/server network debugger: encoding, decoding and packet-processing exceptions, "
                        + "connection context and rotating logs/htgt6-network-*. Restart required.")
                .getBoolean(true);

        ENABLE_NEW_FML_LOADING_SCREEN = cfg.get(
                "client",
                "EnableHiTech6LoadingScreen",
                true,
                "Use the HTGT6-owned loading screen. The Forge splash and BetterLoadingScreen are not required. "
                        + "Restart required."
        ).getBoolean(true);

        ENABLE_WINDOW_BRANDING = cfg.get(
                "client",
                "EnableHiTech6WindowBranding",
                true,
                "Set the HiTech6 window title and icon immediately after LWJGL creates the window. "
                        + "LittleThings is not required. Restart required."
        ).getBoolean(true);
        WINDOW_TITLE = cfg.get(
                "client",
                "HiTech6WindowTitle",
                "Minecraft 1.7.10/1.20.2 // HiTech6 V1.0.4 - BUILD-13",
                "Window title applied during early client startup."
        ).getString();

        ENABLE_CUSTOM_MAIN_MENU = cfg.get(
                "client",
                "EnableHiTech6MainMenu",
                true,
                "Use the responsive, localized HiTech6 main menu. CustomMainMenu and "
                        + "ResourceLoader are not required. Restart required."
          ).getBoolean(true);

        ENABLE_CUSTOM_OPTIONS_MENU = cfg.get(
                "client",
                "EnableHiTech6OptionsMenu",
                true,
                "Use the responsive HiTech6 root options menu. Angelica remains responsible "
                        + "for its video-options screen. Restart required."
        ).getBoolean(true);

        ENABLE_CUSTOM_PAUSE_MENU = cfg.get(
                "client",
                "EnableHiTech6PauseMenu",
                true,
                "Use the compact HiTech6 pause menu and integrate Galacticraft SpaceRace into it. "
                        + "Restart required."
        ).getBoolean(true);

        // 2. Read and populate the new config option from the file
        ENABLE_CLIMATE_SYSTEM = cfg.get(
                "general",
                "EnableClimateSystem",
                true, // Default value if the file doesn't exist yet
                "Set to false to completely turn off the climate, season, and armor mechanics."
        ).getBoolean(true);

        RUN_MINECRAFT_1_7_9999 = cfg.get(
                "modules",
                "Run minecraft 1.7.9999",
                true,
                "Run minecraft 1.7.9999=true enables every implemented backport: "
                        + "Bountiful Update, Combat Update, Frostburn Update, Exploration Update, "
                        + "1.12 Inventory, Update Aquatic, Village & Pillage and the staged 1.20.2 module. "
                        + "Must match on client and server. Restart required."
        ).getBoolean(true);
        removeLegacyBackportSwitches(cfg);

        INVENTORY_112_RECIPE_BOOK_LAYOUT = cfg.getString(
                "RecipeBookLayout", "inventory112_ui", "AUTO",
                "Responsive recipe-book layout: AUTO, SIDE_BY_SIDE or OVERLAY. "
                        + "Unsafe SIDE_BY_SIDE resolutions automatically fall back to OVERLAY.",
                new String[] {"AUTO", "SIDE_BY_SIDE", "OVERLAY"});
        INVENTORY_112_UI_SAFE_MARGIN = cfg.getInt(
                "SafeMargin", "inventory112_ui", 4, 0, 32,
                "Minimum scaled-pixel margin used when positioning the inventory and recipe book.");
        INVENTORY_112_HUD_OFFHAND_SIDE = cfg.getString(
                "HudOffhandSide", "inventory112_ui", "LEFT",
                "Side of the hotbar used by the BackHand HUD slot: LEFT or RIGHT.",
                new String[] {"LEFT", "RIGHT"});

        // Futura and its 1.20.2 biome source are one part of the unified backport
        // runtime. Keeping a second switch allowed the modules to load while the
        // generator's mandatory extended-biome storage was absent.
        ENABLE_EXTENDED_BIOME_ASM = RUN_MINECRAFT_1_7_9999;
        if (cfg.hasCategory("asm")) cfg.getCategory("asm").remove("EnableExtendedBiomeASM");

        FUTURA_CLOUD_HEIGHT = cfg.getInt(
                "CloudHeight", "futura_world", 384, 192, 384,
                "Physical cloud-plane height in Futura's translated 0..383 world. "
                        + "Y=384 places the plane immediately above the highest buildable block."
        );

        FIX_GT6_WATER_TICK_UPDATE = cfg.get(
                "asm",
                "Fix GT6 WaterTickUpdate",
                true,
                "Keep GT6 ocean, river and swamp water blocks, but disable their aggressive spreading, "
                        + "terrain conversion and recursive scheduled ticks. Restart required."
        ).getBoolean(true);

        FIX_GT6_STONE_GEN_LAG = cfg.get(
                "asm",
                "FIxGT6StoneGenLag",
                true,
                "Generate GT6 stone layers directly in raw chunk arrays and defer only sparse layer ores. "
                        + "Disables the expensive post-generation stone replacement scan. Restart required."
        ).getBoolean(true);

        OPTIMIZE_CHUNK_SECTION_LOADING = cfg.get(
                "asm",
                "OptimizeChunkSectionLoading",
                true,
                "Speed up Anvil chunk loading by recounting packed section block IDs directly. "
                        + "Does not change region files or chunk NBT and supports NotEnoughIDs. Restart required."
        ).getBoolean(true);

        FIX_GT6_CAUDRON_ISE = cfg.get(
                "asm",
                "FIX_GT6_Caudron_ISE",
                true,
                "Fix Internal Server Error while washing ores in a vanilla Cauldron. "
                        + "Keeps GT6 washing enabled and guards invalid zero-sized random selections. "
                        + "Must match on client and server. Restart required."
        ).getBoolean(true);

        WARM_OCEAN_BIOME_ID = cfg.getInt(
                "WarmOceanBiomeId", "biome_ids", 350, 300, 600,
                "Biome ID for the 1.13-style Warm Ocean. Active with Minecraft 1.7.9999 backports."
        );
        DEEP_WARM_OCEAN_BIOME_ID = cfg.getInt(
                "DeepWarmOceanBiomeId", "biome_ids", 351, 300, 600,
                "Biome ID for Deep Warm Ocean. Active with Minecraft 1.7.9999 backports."
        );
        LUKEWARM_OCEAN_BIOME_ID = cfg.getInt(
                "LukewarmOceanBiomeId", "biome_ids", 352, 300, 600,
                "Biome ID for the 1.13-style Lukewarm Ocean. Active with Minecraft 1.7.9999 backports."
        );
        DEEP_LUKEWARM_OCEAN_BIOME_ID = cfg.getInt(
                "DeepLukewarmOceanBiomeId", "biome_ids", 353, 300, 600,
                "Biome ID for Deep Lukewarm Ocean. Active with Minecraft 1.7.9999 backports."
        );
        COLD_OCEAN_BIOME_ID = cfg.getInt(
                "ColdOceanBiomeId", "biome_ids", 354, 300, 600,
                "Biome ID for the 1.13-style Cold Ocean. Active with Minecraft 1.7.9999 backports."
        );
        DEEP_COLD_OCEAN_BIOME_ID = cfg.getInt(
                "DeepColdOceanBiomeId", "biome_ids", 355, 300, 600,
                "Biome ID for Deep Cold Ocean. Active with Minecraft 1.7.9999 backports."
        );
        DEEP_FROZEN_OCEAN_BIOME_ID = cfg.getInt(
                "DeepFrozenOceanBiomeId", "biome_ids", 356, 300, 600,
                "Biome ID for Deep Frozen Ocean. Active with Minecraft 1.7.9999 backports."
        );
        validateDistinctOceanBiomeIds();

        LOYALTY_ENCHANTMENT_ID = cfg.getInt(
                "Loyalty", "aquatic_enchantment_ids", 250, 0, 255,
                "Stable numeric ID stored in 1.7.10 item NBT. Must match on client and server.");
        IMPALING_ENCHANTMENT_ID = cfg.getInt(
                "Impaling", "aquatic_enchantment_ids", 249, 0, 255,
                "Stable numeric ID stored in 1.7.10 item NBT. Must match on client and server.");
        RIPTIDE_ENCHANTMENT_ID = cfg.getInt(
                "Riptide", "aquatic_enchantment_ids", 248, 0, 255,
                "Stable numeric ID stored in 1.7.10 item NBT. Must match on client and server.");
        CHANNELING_ENCHANTMENT_ID = cfg.getInt(
                "Channeling", "aquatic_enchantment_ids", 247, 0, 255,
                "Stable numeric ID stored in 1.7.10 item NBT. Must match on client and server.");
        QUICK_CHARGE_ENCHANTMENT_ID = cfg.getInt("QuickCharge", "village_enchantment_ids", 246, 0, 255,
                "Stable numeric ID stored in item NBT. Must match on client and server; occupied IDs fail without remapping saves.");
        MULTISHOT_ENCHANTMENT_ID = cfg.getInt("Multishot", "village_enchantment_ids", 245, 0, 255,
                "Stable numeric ID stored in item NBT. Must match on client and server; occupied IDs fail without remapping saves.");
        PIERCING_ENCHANTMENT_ID = cfg.getInt("Piercing", "village_enchantment_ids", 244, 0, 255,
                "Stable numeric ID stored in item NBT. Must match on client and server; occupied IDs fail without remapping saves.");
        SOUL_SPEED_ENCHANTMENT_ID = cfg.getInt("SoulSpeed", "nether_enchantment_ids", 243, 0, 255,
                "Stable numeric ID stored in item NBT. Must match on client and server; conflicts fail without remapping saves.");
        validateDistinctAquaticEnchantmentIds();

        GLOWING_POTION_ID = cfg.getInt("Glowing", "combat_potion_ids", 1024, 1001, 65535,
                "Stable Backport potion ID. Must match on client and server; an occupied value fails with the owning effect instead of silently remapping saves.");
        LEVITATION_POTION_ID = cfg.getInt("Levitation", "combat_potion_ids", 1025, 1001, 65535,
                "Stable Backport potion ID. Must match on client and server; an occupied value fails with the owning effect instead of silently remapping saves.");
        LUCK_POTION_ID = cfg.getInt("Luck", "combat_potion_ids", 1026, 1001, 65535,
                "Stable Backport potion ID. Must match on client and server; an occupied value fails with the owning effect instead of silently remapping saves.");
        UNLUCK_POTION_ID = cfg.getInt("BadLuck", "combat_potion_ids", 1027, 1001, 65535,
                "Stable Backport potion ID. Must match on client and server; an occupied value fails with the owning effect instead of silently remapping saves.");
        DOLPHINS_GRACE_POTION_ID = cfg.getInt("DolphinsGrace", "aquatic_potion_ids", 1028, 1001, 65535,
                "Stable Backport potion ID. Must match on client and server; occupied values are rejected without remapping existing effects.");
        CONDUIT_POWER_POTION_ID = cfg.getInt("ConduitPower", "aquatic_potion_ids", 1029, 1001, 65535,
                "Stable Backport potion ID. Must match on client and server; occupied values are rejected without remapping existing effects.");
        BAD_OMEN_POTION_ID = cfg.getInt("BadOmen", "village_potion_ids", 1030, 1001, 65535,
                "Stable potion ID. Must match client and server; occupied values are rejected.");
        HERO_OF_VILLAGE_POTION_ID = cfg.getInt("HeroOfTheVillage", "village_potion_ids", 1031, 1001, 65535,
                "Stable potion ID. Must match client and server; occupied values are rejected.");
        validateDistinctCombatPotionIds();

        cfg.save();

        System.out.println("[HTGT6 ASM] config successfully saved at: " + cfgFile.getAbsolutePath());
    }

    /** Returns config/HTGT6 and creates it before a child mod reads its file. */
    public static File getConfigDirectory(File forgeConfigDirectory) {
        File directory = new File(forgeConfigDirectory, "HTGT6");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Unable to create HTGT6 config directory: " + directory);
        }
        return directory;
    }

    /**
     * Moves the HTGT6-owned configuration namespace without discarding an
     * existing file. The legacy file is copied once and is then left intact as
     * a rollback backup.
     */
    public static File getConfigFile(File forgeConfigDirectory, String filename) {
        File target = new File(getConfigDirectory(forgeConfigDirectory), filename);
        File legacy = new File(forgeConfigDirectory, legacyName(filename));
        LegacyConfigMigration.copyIfMissing(legacy, target, "config " + legacy.getName());
        return target;
    }

    /** Called from the AmunRa bytecode redirect in HTGT6Transformer. */
    public static File getAmunRaConfigFile(FMLPreInitializationEvent event) {
        File configDirectory = event.getModConfigurationDirectory();
        File target = new File(getConfigDirectory(configDirectory), "AmunRa.cfg");
        File legacy = new File(configDirectory, "GalacticraftAmunRa.cfg");
        LegacyConfigMigration.copyIfMissing(legacy, target, "GalacticraftAmunRa.cfg");
        ensureAmunRaShuttleIds(target);
        return target;
    }

    /**
     * AmunRa 0.4.8 creates its shuttle schematic with ID 11 by default. That
     * ID is already claimed in this modpack during a clean first launch, so
     * seed the compatible values before AmunRa reads its own configuration.
     * Existing server values are read and preserved unchanged.
     */
    private static void ensureAmunRaShuttleIds(File configFile) {
        Configuration configuration = new Configuration(configFile);
        configuration.load();
        configuration.get("schematics", "shuttleSchematicsId", 18,
                "ID of the Shuttle schematics. 18 avoids the modpack's existing page-ID conflict.");
        configuration.get("schematics", "shuttleGuiId", 8,
                "ID of the Shuttle schematics GUI. 0-7 are reserved by Galacticraft.");
        if (configuration.hasChanged()) configuration.save();
    }

    private static String legacyName(String filename) {
        return filename;
    }

    /** Removes the five pre-unification switches from existing config files. */
    private static void removeLegacyBackportSwitches(Configuration cfg) {
        if (!cfg.hasCategory("modules")) return;
        String[] legacyKeys = {
                "EnableAquaticUpdate", "EnableBountifulUpdate", "EnableCombatUpdate",
                "EnableInventoryUpdate112", "EnableVillageAndPillageUpdate"
        };
        for (String key : legacyKeys) cfg.getCategory("modules").remove(key);
    }

    private static void validateDistinctOceanBiomeIds() {
        int[] ids = {WARM_OCEAN_BIOME_ID, DEEP_WARM_OCEAN_BIOME_ID,
                LUKEWARM_OCEAN_BIOME_ID, DEEP_LUKEWARM_OCEAN_BIOME_ID,
                COLD_OCEAN_BIOME_ID, DEEP_COLD_OCEAN_BIOME_ID,
                DEEP_FROZEN_OCEAN_BIOME_ID};
        for (int i = 0; i < ids.length; i++) {
            for (int j = i + 1; j < ids.length; j++) {
                if (ids[i] == ids[j]) {
                    throw new IllegalStateException("Aquatic ocean biome IDs must be unique; duplicate " + ids[i]);
                }
            }
        }
    }

    private static void validateDistinctAquaticEnchantmentIds() {
        int[] ids = {LOYALTY_ENCHANTMENT_ID, IMPALING_ENCHANTMENT_ID,
                RIPTIDE_ENCHANTMENT_ID, CHANNELING_ENCHANTMENT_ID, QUICK_CHARGE_ENCHANTMENT_ID, MULTISHOT_ENCHANTMENT_ID, PIERCING_ENCHANTMENT_ID, SOUL_SPEED_ENCHANTMENT_ID};
        for (int i = 0; i < ids.length; i++) for (int j = i + 1; j < ids.length; j++) {
            if (ids[i] == ids[j]) throw new IllegalStateException(
                    "Backport enchantment IDs must be unique; duplicate " + ids[i]);
        }
    }

    private static void validateDistinctCombatPotionIds() {
        int[] ids = {GLOWING_POTION_ID, LEVITATION_POTION_ID, LUCK_POTION_ID, UNLUCK_POTION_ID, DOLPHINS_GRACE_POTION_ID, CONDUIT_POWER_POTION_ID, BAD_OMEN_POTION_ID, HERO_OF_VILLAGE_POTION_ID};
        for (int i = 0; i < ids.length; ++i) for (int j = i + 1; j < ids.length; ++j)
            if (ids[i] == ids[j]) throw new IllegalStateException(
                    "Backport potion IDs must be unique; duplicate " + ids[i]);
    }

    private HTGT6Config() {}
}
