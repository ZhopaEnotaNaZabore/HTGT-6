package com.mod.htgt6.sulimited.world;


import net.minecraft.world.biome.BiomeGenBase;

public class BiomeGenSpaceUltimate extends BiomeGenBase {

    // The single instance we will reference everywhere
    public static BiomeGenSpaceUltimate spaceUltimate;
    /** Dedicated real biome for C, allowing its grass without recolouring B. */
    public static BiomeGenSpaceUltimate glieseC;
    public static BiomeGenSpaceUltimate raHorus;
    public static BiomeGenSpaceUltimate raNeper;
    public static BiomeGenSpaceUltimate raMaahes;
    public static BiomeGenSpaceUltimate raSeth;
    public static BiomeGenSpaceUltimate raAnubis;
    /** Charon biome used by the SolSysExpanded dimension. */
    public static BiomeGenSpaceUltimate charon;
    /** Dark cratered biome used by the SolSysExpanded Proteus dimension. */
    public static BiomeGenSpaceUltimate proteus;
    public static BiomeGenSpaceUltimate neptune;
    public static BiomeGenSpaceUltimate mimas;
    public static BiomeGenSpaceUltimate tethys;
    public static BiomeGenSpaceUltimate dione;
    public static BiomeGenSpaceUltimate rhea;
    public static BiomeGenSpaceUltimate iapetus;
    public static BiomeGenSpaceUltimate phobos;
    public static BiomeGenSpaceUltimate deimos;
    public static BiomeGenSpaceUltimate umbriel;
    public static BiomeGenSpaceUltimate titania;
    public static BiomeGenSpaceUltimate oberon;
    public static BiomeGenSpaceUltimate ariel;
    public static BiomeGenSpaceUltimate haumea;
    public static BiomeGenSpaceUltimate makemake;
    public static BiomeGenSpaceUltimate eris;
    public static BiomeGenSpaceUltimate deeDee;
    private static final int GLIESE_C_MOSS = 0x5F6B57;
    private final int grassColour;
    private final int foliageColour;
    private final GlieseCSubBiomeGenerator glieseCSubBiomeGenerator = new GlieseCSubBiomeGenerator();

    public BiomeGenSpaceUltimate(int id) {
        this(id, "Space Ultimate", -1, -1, 0.5F, 0.0F, false);
    }

    private BiomeGenSpaceUltimate(int id, String name, int grassColour, int foliageColour,
                                  float temperature, float rainfall, boolean snow) {
        super(id);
        this.grassColour = grassColour;
        this.foliageColour = foliageColour;
        this.setBiomeName(name);
        this.setTemperatureRainfall(temperature, rainfall);
        if (snow) this.setEnableSnow();
        else if (rainfall == 0.0F) this.setDisableRain();
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCreatureList.clear();
    }

    // Call this in your Main Mod preInit phase!
    // Example: BiomeGenSpaceUltimate.init(140);
    public static void init(int biomeID) {
        spaceUltimate = new BiomeGenSpaceUltimate(biomeID);
    }

    public static void initGlieseC(int biomeID) {
        glieseC = new BiomeGenSpaceUltimate(biomeID, "Space Ultimate - Gliese 792 C",
                GLIESE_C_MOSS, GLIESE_C_MOSS, 0.7F, 0.9F, false);
    }

    /** Real biome identities for Ra; AmunRa remains responsible for terrain and decoration. */
    public static void initRaBiomes(int horusId, int neperId, int maahesId, int sethId, int anubisId) {
        raHorus = new BiomeGenSpaceUltimate(horusId, "Space Ultimate - Ra / Horus",
                0x8D6748, 0x766044, 1.6F, 0.0F, false);
        raNeper = new BiomeGenSpaceUltimate(neperId, "Space Ultimate - Ra / Neper",
                0x6FAE54, 0x4F9149, 0.8F, 0.7F, false);
        raMaahes = new BiomeGenSpaceUltimate(maahesId, "Space Ultimate - Ra / Maahes",
                0x78905A, 0x617B49, 0.65F, 0.35F, false);
        raSeth = new BiomeGenSpaceUltimate(sethId, "Space Ultimate - Ra / Seth",
                0xB8C8C8, 0xA9BABA, 0.0F, 0.2F, true);
        raAnubis = new BiomeGenSpaceUltimate(anubisId, "Space Ultimate - Ra / Anubis",
                0x706866, 0x665F5D, 0.2F, 0.0F, false);
    }

    public static void initCharon(int biomeId) {
        charon = new BiomeGenSpaceUltimate(biomeId, "Space Ultimate - Sol / Charon",
                0xC6C3C1, 0xB4B9C0, 0.0F, 0.0F, false);
        charon.spawnableCaveCreatureList.clear();
    }

    public static void initProteus(int biomeId) {
        proteus = new BiomeGenSpaceUltimate(biomeId, "Space Ultimate - Sol / Proteus",
                0x30343B, 0x252A31, 0.0F, 0.0F, false);
        proteus.spawnableCaveCreatureList.clear();
    }

    public static void initNeptune(int biomeId) {
        neptune = new BiomeGenSpaceUltimate(biomeId, "Space Ultimate - Sol / Neptune",
                0x1E73C7, 0x14559C, 0.0F, 0.0F, false);
        neptune.spawnableCaveCreatureList.clear();
    }

    public static void initSaturnMoons(int mimasId, int tethysId, int dioneId, int rheaId, int iapetusId) {
        mimas = frozenMoon(mimasId, "Space Ultimate - Sol / Mimas", 0xC9C4BA);
        tethys = frozenMoon(tethysId, "Space Ultimate - Sol / Tethys", 0xE5EDF2);
        dione = frozenMoon(dioneId, "Space Ultimate - Sol / Dione", 0xB9B8B4);
        rhea = frozenMoon(rheaId, "Space Ultimate - Sol / Rhea", 0xC6C7C5);
        iapetus = frozenMoon(iapetusId, "Space Ultimate - Sol / Iapetus", 0x9B9187);
    }

    public static void initMarsMoons(int phobosId, int deimosId) {
        phobos = frozenMoon(phobosId, "Space Ultimate - Sol / Phobos", 0x776A60);
        deimos = frozenMoon(deimosId, "Space Ultimate - Sol / Deimos", 0x985C3E);
    }

    public static void initUranusMoons(int umbrielId, int titaniaId, int oberonId) {
        umbriel = frozenMoon(umbrielId, "Space Ultimate - Sol / Umbriel", 0x363331);
        titania = frozenMoon(titaniaId, "Space Ultimate - Sol / Titania", 0xC9C4B9);
        oberon = frozenMoon(oberonId, "Space Ultimate - Sol / Oberon", 0x777069);
    }

    public static void initOuterSolWorlds(int arielId, int haumeaId, int makemakeId,
                                          int erisId, int deeDeeId) {
        ariel = frozenMoon(arielId, "Space Ultimate - Sol / Ariel", 0xD8E1DF);
        haumea = frozenMoon(haumeaId, "Space Ultimate - Sol / Haumea", 0xDDD8CA);
        makemake = frozenMoon(makemakeId, "Space Ultimate - Sol / Makemake", 0xA96B54);
        eris = frozenMoon(erisId, "Space Ultimate - Sol / Eris", 0xC7C2B8);
        deeDee = frozenMoon(deeDeeId, "Space Ultimate - Sol / DeeDee", 0x8F8275);
    }

    private static BiomeGenSpaceUltimate frozenMoon(int biomeId, String name, int colour) {
        BiomeGenSpaceUltimate biome = new BiomeGenSpaceUltimate(biomeId, name,
                colour, colour, 0.0F, 0.0F, false);
        biome.spawnableCaveCreatureList.clear();
        return biome;
    }

    @Override
    public int getBiomeGrassColor(int x, int y, int z) {
        return grassColour >= 0 ? grassColour : super.getBiomeGrassColor(x, y, z);
    }

    @Override
    public int getBiomeFoliageColor(int x, int y, int z) {
        return foliageColour >= 0 ? foliageColour : super.getBiomeFoliageColor(x, y, z);
    }

    /** The virtual biome source used exclusively by Gliese 792 C. */
    public GlieseCSubBiomeGenerator getGlieseCSubBiomeGenerator() {
        return this.glieseCSubBiomeGenerator;
    }
}
