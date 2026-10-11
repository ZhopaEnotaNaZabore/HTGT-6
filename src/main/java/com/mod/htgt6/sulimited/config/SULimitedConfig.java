package com.mod.htgt6.sulimited.config;

import com.mod.htgt6.asm.HTGT6Config;
import java.io.File;
import net.minecraftforge.common.config.Configuration;

/** Configuration file owned by the SU-Limited child mod. */
public final class SULimitedConfig {
    public static final String FILE_NAME = "SU_Limited.cfg";
    public static int spaceUltimateBiomeId = 630;
    public static int gliese792CBiomeId = 640;
    public static int raHorusBiomeId = 641;
    public static int raNeperBiomeId = 642;
    public static int raMaahesBiomeId = 643;
    public static int raSethBiomeId = 644;
    public static int raAnubisBiomeId = 645;
    public static int charonBiomeId = 646;
    public static int charonDimensionId = -889;
    public static int proteusBiomeId = 647;
    public static int proteusDimensionId = -890;
    public static int neptuneBiomeId = 648;
    public static int neptuneDimensionId = -891;
    public static int mimasBiomeId = 649;
    public static int mimasDimensionId = -892;
    public static int tethysBiomeId = 650;
    public static int tethysDimensionId = -893;
    public static int dioneBiomeId = 651;
    public static int dioneDimensionId = -894;
    public static int rheaBiomeId = 652;
    public static int rheaDimensionId = -895;
    public static int iapetusBiomeId = 653;
    public static int iapetusDimensionId = -896;
    public static int phobosBiomeId = 654;
    public static int phobosDimensionId = -897;
    public static int deimosBiomeId = 655;
    public static int deimosDimensionId = -898;
    public static int umbrielBiomeId = 656;
    public static int umbrielDimensionId = -899;
    public static int titaniaBiomeId = 657;
    public static int titaniaDimensionId = -900;
    public static int oberonBiomeId = 658;
    public static int oberonDimensionId = -901;
    public static int arielBiomeId = 659;
    public static int arielDimensionId = -902;
    public static int haumeaBiomeId = 660;
    public static int haumeaDimensionId = -903;
    public static int makemakeBiomeId = 661;
    public static int makemakeDimensionId = -904;
    public static int erisBiomeId = 662;
    public static int erisDimensionId = -905;
    public static int deeDeeBiomeId = 663;
    public static int deeDeeDimensionId = -906;

    private SULimitedConfig() { }

    public static void load(File forgeConfigDirectory) {
        Configuration configuration = new Configuration(HTGT6Config.getConfigFile(forgeConfigDirectory, FILE_NAME));
        configuration.load();
        configuration.get("general", "enabled", true,
                "Keeps SU-Limited configuration separate from the main HTGT6 config.");
        configuration.addCustomCategoryComment("biome_ids",
                "ВАЖНО: Не меняйте ID выше 255 если ASM HiTech6 не включен!");
        spaceUltimateBiomeId = configuration.getInt("spaceUltimateBiomeId", "biome_ids", 630,
                0, Integer.MAX_VALUE,
                "Biome ID for Space Ultimate. IDs above 255 require EnableExtendedBiomeASM on both client and server.");
        gliese792CBiomeId = configuration.getInt("gliese792CBiomeId", "biome_ids", 640,
                0, Integer.MAX_VALUE,
                "Biome ID for Gliese 792 C. Must differ from spaceUltimateBiomeId; IDs above 255 require EnableExtendedBiomeASM.");
        raHorusBiomeId = biomeId(configuration, "raHorusBiomeId", 641, "Horus");
        raNeperBiomeId = biomeId(configuration, "raNeperBiomeId", 642, "Neper");
        raMaahesBiomeId = biomeId(configuration, "raMaahesBiomeId", 643, "Maahes");
        raSethBiomeId = biomeId(configuration, "raSethBiomeId", 644, "Seth");
        raAnubisBiomeId = biomeId(configuration, "raAnubisBiomeId", 645, "Anubis");
        charonBiomeId = biomeId(configuration, "charonBiomeId", 646, "Charon");
        charonDimensionId = configuration.getInt("charonDimensionId", "dimension_ids", -889,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                "Dimension ID assigned to GalaxySpace's existing Charon object by SolSysExpanded.");
        proteusBiomeId = biomeId(configuration, "proteusBiomeId", 647, "Proteus");
        proteusDimensionId = configuration.getInt("proteusDimensionId", "dimension_ids", -890,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                "Dimension ID assigned to GalaxySpace's existing Proteus object by SolSysExpanded.");
        neptuneBiomeId = biomeId(configuration, "neptuneBiomeId", 648, "Neptune");
        neptuneDimensionId = configuration.getInt("neptuneDimensionId", "dimension_ids", -891,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                "Dimension ID assigned to GalaxySpace's existing Neptune object by SolSysExpanded.");
        mimasBiomeId = biomeId(configuration, "mimasBiomeId", 649, "Mimas");
        mimasDimensionId = dimensionId(configuration, "mimasDimensionId", -892, "Mimas");
        tethysBiomeId = biomeId(configuration, "tethysBiomeId", 650, "Tethys");
        tethysDimensionId = dimensionId(configuration, "tethysDimensionId", -893, "Tethys");
        dioneBiomeId = biomeId(configuration, "dioneBiomeId", 651, "Dione");
        dioneDimensionId = dimensionId(configuration, "dioneDimensionId", -894, "Dione");
        rheaBiomeId = biomeId(configuration, "rheaBiomeId", 652, "Rhea");
        rheaDimensionId = dimensionId(configuration, "rheaDimensionId", -895, "Rhea");
        iapetusBiomeId = biomeId(configuration, "iapetusBiomeId", 653, "Iapetus");
        iapetusDimensionId = dimensionId(configuration, "iapetusDimensionId", -896, "Iapetus");
        phobosBiomeId = biomeId(configuration, "phobosBiomeId", 654, "Phobos");
        phobosDimensionId = dimensionId(configuration, "phobosDimensionId", -897, "Phobos");
        deimosBiomeId = biomeId(configuration, "deimosBiomeId", 655, "Deimos");
        deimosDimensionId = dimensionId(configuration, "deimosDimensionId", -898, "Deimos");
        umbrielBiomeId = biomeId(configuration, "umbrielBiomeId", 656, "Umbriel");
        umbrielDimensionId = dimensionId(configuration, "umbrielDimensionId", -899, "Umbriel");
        titaniaBiomeId = biomeId(configuration, "titaniaBiomeId", 657, "Titania");
        titaniaDimensionId = dimensionId(configuration, "titaniaDimensionId", -900, "Titania");
        oberonBiomeId = biomeId(configuration, "oberonBiomeId", 658, "Oberon");
        oberonDimensionId = dimensionId(configuration, "oberonDimensionId", -901, "Oberon");
        arielBiomeId = biomeId(configuration, "arielBiomeId", 659, "Ariel");
        arielDimensionId = dimensionId(configuration, "arielDimensionId", -902, "Ariel");
        haumeaBiomeId = biomeId(configuration, "haumeaBiomeId", 660, "Haumea");
        haumeaDimensionId = dimensionId(configuration, "haumeaDimensionId", -903, "Haumea");
        makemakeBiomeId = biomeId(configuration, "makemakeBiomeId", 661, "Makemake");
        makemakeDimensionId = dimensionId(configuration, "makemakeDimensionId", -904, "Makemake");
        erisBiomeId = biomeId(configuration, "erisBiomeId", 662, "Eris");
        erisDimensionId = dimensionId(configuration, "erisDimensionId", -905, "Eris");
        deeDeeBiomeId = biomeId(configuration, "deeDeeBiomeId", 663, "DeeDee");
        deeDeeDimensionId = dimensionId(configuration, "deeDeeDimensionId", -906, "DeeDee");
        validateUniqueBiomeIds();
        if (configuration.hasChanged()) configuration.save();
    }

    private static int biomeId(Configuration configuration, String key, int defaultId, String planet) {
        return configuration.getInt(key, "biome_ids", defaultId, 0, Integer.MAX_VALUE,
                "SpaceUltimate biome ID for " + planet + ". IDs above 255 require EnableExtendedBiomeASM.");
    }

    private static int dimensionId(Configuration configuration, String key, int defaultId, String world) {
        return configuration.getInt(key, "dimension_ids", defaultId, Integer.MIN_VALUE, Integer.MAX_VALUE,
                "Dimension ID assigned to GalaxySpace's existing " + world + " object by SolSysExpanded.");
    }

    private static void validateUniqueBiomeIds() {
        int[] ids = {spaceUltimateBiomeId, gliese792CBiomeId, raHorusBiomeId, raNeperBiomeId,
                raMaahesBiomeId, raSethBiomeId, raAnubisBiomeId, charonBiomeId, proteusBiomeId, neptuneBiomeId,
                mimasBiomeId, tethysBiomeId, dioneBiomeId, rheaBiomeId, iapetusBiomeId,
                phobosBiomeId, deimosBiomeId, umbrielBiomeId, titaniaBiomeId, oberonBiomeId,
                arielBiomeId, haumeaBiomeId, makemakeBiomeId, erisBiomeId, deeDeeBiomeId};
        java.util.HashSet<Integer> unique = new java.util.HashSet<Integer>();
        for (int id : ids) {
            if (!unique.add(id)) throw new IllegalStateException("SU_Limited biome IDs must be unique; duplicate: " + id);
        }
    }
}
