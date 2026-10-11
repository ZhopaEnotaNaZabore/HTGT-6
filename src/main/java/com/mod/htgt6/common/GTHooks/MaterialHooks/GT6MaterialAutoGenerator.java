package com.mod.htgt6.common.GTHooks.MaterialHooks;

import com.mod.htgt6.common.GTHooks.UIHooks.AnimatedMaterialFormula;
import gregapi.code.TagData;
import gregapi.data.CS;
import gregapi.data.MT;
import gregapi.data.OP;
import gregapi.data.TD;
import gregapi.oredict.OreDictMaterial;
import gregapi.oredict.OreDictMaterialStack;
import gregapi.oredict.OreDictPrefix;
import gregapi.oredict.configurations.OreDictConfigurationComponent;
import gregapi.render.TextureSet;

/** Registers HTGT6 materials early enough for GT6's deferred PrefixItem generation pass. */
public final class GT6MaterialAutoGenerator {
    /** Addon-owned IDs must remain stable because GT6 stores them as item metadata. */
    public static final int PHERITHIUM_ID = 16000;
    public static final int DEEPSLATE_ID = 16001;
    public static final int ETHERNIUM_ID = 16002;
    public static final int PHASED_COPPER_ID = 16003;
    public static final int NITINOL_ID = 16004;
    public static final int QUANTINUM_ID = 16005;
    public static final int TRANSCENDENTIC_ALLOY_ID = 16006;
    public static final int STABLE_TIME_SPACE_ID = 16007;
    public static final int QUANTUM_ALLOY_ID = 16008;
    public static final int POLY_ETHYLENE_ID = 16009;
    public static final int VANITE_ID = 16010;
    public static final int SUPERCONDUCTOR_BASE_FIRST_ID = 16011;
    public static final int SUPERCONDUCTOR_FIRST_ID = 16027;
    public static final int SUPERCONDUCTOR_TIER_COUNT = 16;

    /** Assigned during {@link #registerAll()} in HTGT6 preInit. */
    public static OreDictMaterial PHERITHIUM;
    public static OreDictMaterial DEEPSLATE;
    public static OreDictMaterial ETHERNIUM;
    public static OreDictMaterial PHASED_COPPER;
    public static OreDictMaterial NITINOL;
    public static OreDictMaterial QUANTINUM;
    public static OreDictMaterial TRANSCENDENTIC_ALLOY;
    public static OreDictMaterial STABLE_TIME_SPACE;
    public static OreDictMaterial QUANTUM_ALLOY;
    public static OreDictMaterial POLY_ETHYLENE;
    public static OreDictMaterial VANITE;
    /** Indexed by HTGT6 tier - 1 (LV=0, MAX=15). */
    public static final OreDictMaterial[] SUPERCONDUCTOR_BASES =
            new OreDictMaterial[SUPERCONDUCTOR_TIER_COUNT];
    /** Indexed by HTGT6 tier - 1 (LV=0, MAX=15). */
    public static final OreDictMaterial[] SUPERCONDUCTORS =
            new OreDictMaterial[SUPERCONDUCTOR_TIER_COUNT];

    private static boolean registered;

    public static synchronized void registerAll() {
        if (registered) return;
        com.mod.htgt6.common.handler.recipes.PlatinumGroupSludgeRecipes.prepareItems();
        // IronChest obsidian upgrades use four-unit plates; diamond uses existing crystalline plates.
        OP.plateQuadruple.forceItemGeneration(MT.Obsidian);

        DEEPSLATE = registerSelectiveMaterial(DEEPSLATE_ID, "Deepslate", "Глубинный сланец",
                TextureSet.SET_STONE, 0x50, 0x51, 0x55,
                new TagData[] { TD.Properties.STONE, TD.Properties.BRITTLE, TD.Processing.MORTAR },
                OP.dust, OP.dustImpure);
        MaterialTextureHooks.applyItems(DEEPSLATE, "HTGT_DEEPSLATE", OP.dust, OP.dustImpure);

        POLY_ETHYLENE = register(new MaterialDefinition(
                POLY_ETHYLENE_ID,
                "PolyEthyLene",
                "Полиэтилен",
                TextureSet.SET_DULL,
                0xC8, 0xC8, 0xC8,
                423L, 600L,
                new Component[] {
                        new Component(MT.C, 2),
                        new Component(MT.H, 4)
                },
                plasticForms(),
                new TagData[] {
                        TD.Compounds.APPROXIMATE,
                        TD.Properties.FLAMMABLE,
                        TD.Properties.BOUNCY,
                        TD.Properties.BRITTLE,
                        TD.Processing.EXTRUDER,
                        TD.Processing.EXTRUDER_SIMPLE,
                        TD.Processing.MORTAR,
                        TD.Processing.FURNACE
                }));
        POLY_ETHYLENE.tooltip("(C2H4)n").steal(MT.Plastic)
                .setDensity(MT.Plastic.mGramPerCubicCentimeter);
        GT6OreDictionaryOverlapHook.install(POLY_ETHYLENE);

        VANITE = register(new MaterialDefinition(VANITE_ID, "Vanite", "Ванит",
                TextureSet.SET_METALLIC, 0xA7, 0xA0, 0xB4, 1800L, 4200L,
                new Component[] {
                        c(MT.VanadiumPentoxide, 4), c(MT.Pyrite, 6), c(MT.OREMATS.Bastnasite, 5)
                }, allSolidForms(), metalProcessing()));
        // GT6 dust units: 4/7 V2O5 + 6/3 FeS2 + 5/6 CeCO3F (multiply by 42).
        VANITE.tooltip("V48Fe84S168Ce35C35O225F35").qual(3L, 9.0D, 1536L, 3L).setDensity(7.0D);
        // Keep this explicit even though G_ALL currently contains ORES: oreVanite
        // must remain generated if GT6 changes the composition of that tag group.
        OP.ore.forceItemGeneration(VANITE);

        PHERITHIUM = register(new MaterialDefinition(
                PHERITHIUM_ID,
                "Pherithium",
                "Ферритиум",
                TextureSet.SET_METALLIC,
                0x90, 0xEE, 0x90,
                1450L, 3200L,
                new Component[] {
                        c(MT.Pyrite, 1),
                        c(MT.SiO2, 1),
                        c(MT.OREMATS.Uraninite, 1),
                        c(MT.Water, 7)
                },
                allSolidForms(),
                metalProcessing()));

        // Enables the generated tool/armor forms with metal-like working statistics.
        // GT6 Uraninite has divider 1, whereas Pyrite, SiO2 and Water have divider 3.
        // 1/3 FeS2 + 1/3 SiO2 + UO2 + 7/3 H2O (multiply by 3).
        // Separation is registered explicitly in the HV industrial centrifuge, not auto-electrolysis.
        PHERITHIUM.tooltip("FeS2SiU3O15H14").qual(3L, 8.0D, 1024L, 3L).setDensity(5.0D);

        ETHERNIUM = register(elementLike(ETHERNIUM_ID, "Ethernium", "Этерниум",
                TextureSet.SET_SHINY, 0xD8, 0xF4, 0xFF, 4800L, 12000L));
        ETHERNIUM.tooltip("Ett").qual(5L, 18.0D, 16384L, 6L).setDensity(12.0D);
        MaterialTextureHooks.applyAll(ETHERNIUM, "HTGT_ETERNITY");

        PHASED_COPPER = register(new MaterialDefinition(
                PHASED_COPPER_ID, "PhasedCopper", "Фазированная медь", TextureSet.SET_SHINY,
                0xFF, 0x55, 0x16, 1358L, 2835L,
                new Component[] { new Component(MT.Cu, 1) }, allSolidForms(), metalProcessing()));
        PHASED_COPPER.tooltip("Cu->>>").qual(3L, 10.0D, 2048L, 3L).setDensity(9.0D);

        NITINOL = register(new MaterialDefinition(
                NITINOL_ID, "Nitinol", "Нитинол", TextureSet.SET_METALLIC,
                0xC2, 0x8A, 0xE8, 1583L, 3200L,
                new Component[] { new Component(MT.Ni, 1), new Component(MT.Ti, 1) },
                allSolidForms(), metalProcessing()));
        NITINOL.tooltip("NiTi").qual(4L, 12.0D, 4096L, 4L).setDensity(6.5D);

        QUANTINUM = register(elementLike(QUANTINUM_ID, "Quantinum", "Квантиниум",
                TextureSet.SET_SHINY, 0x0C, 0x4A, 0x30, 5200L, 14000L));
        QUANTINUM.tooltip("Qt").qual(6L, 22.0D, 32768L, 7L).setDensity(15.0D);

        TRANSCENDENTIC_ALLOY = register(elementLike(TRANSCENDENTIC_ALLOY_ID,
                "TranscendenticAlloy", "Трансцендентный сплав", TextureSet.SET_SPACE,
                0xFF, 0xFF, 0xFF, 6000L, 16000L));
        TRANSCENDENTIC_ALLOY.tooltip("TrA").qual(7L, 28.0D, 65536L, 8L).setDensity(18.0D);
        MaterialTextureHooks.applyAll(TRANSCENDENTIC_ALLOY, "HTGT_TRANSCENDENT");

        STABLE_TIME_SPACE = register(elementLike(STABLE_TIME_SPACE_ID,
                "StableTimeSpace", "Стабильное время-пространство", TextureSet.SET_SPACE,
                0xFF, 0xFF, 0xFF, 7000L, 18000L));
        STABLE_TIME_SPACE.tooltip("<<<<COSMOS>>>>").qual(8L, 32.0D, 131072L, 9L).setDensity(20.0D);
        MaterialTextureHooks.applyAll(STABLE_TIME_SPACE, "HTGT_COSMOS");

        QUANTUM_ALLOY = register(new MaterialDefinition(
                QUANTUM_ALLOY_ID, "QuantumAlloy", "Квантовый сплав", TextureSet.SET_SHINY,
                0x38, 0x8C, 0x78, 5600L, 15000L,
                new Component[] {
                        new Component(QUANTINUM, 2),
                        new Component(MT.Adamantium, 5),
                        new Component(NITINOL, 5),
                        new Component(MT.F, 56)
                }, allSolidForms(), metalProcessing()));
        QUANTUM_ALLOY.tooltip("Qt2Ad5NiTi5F56").qual(7L, 25.0D, 65536L, 8L).setDensity(16.0D);

        registerTieredSuperconductors();

        // This proves that an existing GT6 material can receive an HTGT6 palette too.
        // The cloned icon lists keep the change local to Bedrock and do not modify SET_STONE.
        MaterialTextureHooks.applyAll(MT.Bedrock, "HTGT_BEDROCK");
        AnimatedMaterialFormula.register(STABLE_TIME_SPACE);
        registered = true;
    }

    private static void registerTieredSuperconductors() {
        final String[] tiers = {
                "LV", "MV", "HV", "EV", "IV", "LuV", "ZPM", "UV",
                "PUV1", "UX", "OLV", "OMV", "OHV", "OEV", "OIV", "MAX"
        };
        final String[] baseFormulas = {
                "RedAlloy+Si2Fe5", "Cd5Cu12Si3O16", "Nb3Sn", "V3Ga",
                "NbTi", "YBa2Cu3O7", "SmFeAsO", "HgBa2Ca2Cu3O8",
                "HfNNaq", "NaqE2TrGr", "NaqR2TritIr", "Dur2TrinOs",
                "CosN-BedR-NaqR2", "Inf-Dra2-STS", "TrA2-QtA-Inf", "STS4-TrA4-Inf2-CosN"
        };
        final int[][] colours = {
                {0xD8, 0x2F, 0x2F}, {0x9A, 0xA0, 0xA8}, {0xE0, 0x92, 0x32}, {0xA8, 0x42, 0xD8},
                {0x49, 0x72, 0xD1}, {0xD5, 0x5A, 0xA8}, {0xD9, 0x48, 0x73}, {0x3B, 0xE0, 0xD0},
                {0x51, 0xC8, 0xFF}, {0xDE, 0x58, 0xFF}, {0x65, 0xEE, 0xFF}, {0x63, 0xFF, 0x91},
                {0xFF, 0xEF, 0x69}, {0xFF, 0x9B, 0x45}, {0xFF, 0x53, 0x74}, {0xFF, 0xFF, 0xFF}
        };
        final Component[][] baseComponents = {
                { c(MT.RedAlloy, 1), c(MT.Si, 2), c(MT.Fe, 5) },
                { c(MT.Cd, 5), c(MT.Cu, 12), c(MT.Si, 3), c(MT.O, 16) },
                { c(MT.Nb, 3), c(MT.Sn, 1) },
                { c(MT.V, 3), c(MT.Ga, 1) },
                { c(MT.Nb, 1), c(MT.Ti, 1) },
                { c(MT.Y, 1), c(MT.Ba, 2), c(MT.Cu, 3), c(MT.O, 7) },
                { c(MT.Sm, 1), c(MT.Fe, 1), c(MT.As, 1), c(MT.O, 1) },
                { c(MT.Hg, 1), c(MT.Ba, 2), c(MT.Ca, 2), c(MT.Cu, 3), c(MT.O, 8) },
                { c(MT.Hf, 1), c(MT.N, 1), c(MT.Naquadah, 1) },
                { c(MT.NaquadahEnriched, 2), c(MT.Trinium, 1), c(MT.Graphene, 1) },
                { c(MT.Naquadria, 2), c(MT.TritaniumAlloy, 1), c(MT.Ir, 1) },
                { c(MT.DuraniumAlloy, 2), c(MT.Trinitanium, 1), c(MT.Os, 1) },
                { c(MT.CosmicNeutronium, 1), c(MT.Bedrockium, 1), c(MT.Naquadria, 2) },
                { c(MT.Infinity, 1), c(MT.DraconiumAwakened, 2), c(STABLE_TIME_SPACE, 1) },
                { c(TRANSCENDENTIC_ALLOY, 2), c(QUANTUM_ALLOY, 1), c(MT.Infinity, 1) },
                { c(STABLE_TIME_SPACE, 4), c(TRANSCENDENTIC_ALLOY, 4),
                        c(MT.Infinity, 2), c(MT.CosmicNeutronium, 1) }
        };
        final OreDictMaterial[] activators = {
                MT.Ag, MT.Nb, MT.NiobiumNitride, MT.VanadiumGallium,
                MT.NiobiumTitanium, MT.YttriumBariumCuprate, MT.Eu, MT.Am,
                MT.Naquadah, MT.NaquadahEnriched, MT.Naquadria, MT.Trinium,
                MT.CosmicNeutronium, MT.Infinity, TRANSCENDENTIC_ALLOY, STABLE_TIME_SPACE
        };

        for (int i = 0; i < SUPERCONDUCTOR_TIER_COUNT; i++) {
            long meltingPoint = 1100L + i * 360L;
            SUPERCONDUCTOR_BASES[i] = registerConductorMaterial(
                    SUPERCONDUCTOR_BASE_FIRST_ID + i,
                    "SuperconductorBase" + tiers[i],
                    "Основа сверхпроводника " + tiers[i],
                    TextureSet.SET_METALLIC, colours[i], meltingPoint,
                    baseComponents[i], baseFormulas[i]);
        }
        for (int i = 0; i < SUPERCONDUCTOR_TIER_COUNT; i++) {
            long meltingPoint = 1250L + i * 390L;
            SUPERCONDUCTORS[i] = registerConductorMaterial(
                    SUPERCONDUCTOR_FIRST_ID + i,
                    "Superconductor" + tiers[i],
                    "Сверхпроводник " + tiers[i],
                    TextureSet.SET_SHINY, colours[i], meltingPoint,
                    new Component[] { c(SUPERCONDUCTOR_BASES[i], 4), c(activators[i], 1) },
                    "(" + baseFormulas[i] + ")4+" + activators[i].mNameInternal);
        }
    }

    private static OreDictMaterial registerConductorMaterial(int id, String internalName,
            String localName, TextureSet[] textureSet, int[] colour, long meltingPoint,
            Component[] components, String formula) {
        OreDictMaterial material = register(new MaterialDefinition(
                id, internalName, localName, textureSet,
                colour[0], colour[1], colour[2], meltingPoint, meltingPoint * 2L,
                components, new TagData[0], metalProcessing()));
        material.tooltip(formula).qual(3L, 8.0D, 2048L, 3L).setDensity(9.0D);
        restrictItemGeneration(material, OP.dust, OP.ingot, OP.ingotHot);
        return material;
    }

    private static void restrictItemGeneration(OreDictMaterial material,
            OreDictPrefix... allowedPrefixes) {
        for (OreDictPrefix prefix : OreDictPrefix.VALUES) {
            if (prefix != null && !containsIdentity(allowedPrefixes, prefix))
                prefix.disableItemGeneration(material);
        }
        for (OreDictPrefix prefix : allowedPrefixes)
            if (prefix != null) prefix.forceItemGeneration(material);
    }

    private static Component c(OreDictMaterial material, long amount) {
        return new Component(material, amount);
    }

    public static OreDictMaterial superconductorBase(int tier) {
        return SUPERCONDUCTOR_BASES[Math.max(1, Math.min(tier, SUPERCONDUCTOR_TIER_COUNT)) - 1];
    }

    public static OreDictMaterial superconductor(int tier) {
        return SUPERCONDUCTORS[Math.max(1, Math.min(tier, SUPERCONDUCTOR_TIER_COUNT)) - 1];
    }

    private static MaterialDefinition elementLike(int id, String internalName, String localName,
            TextureSet[] textureSet, long red, long green, long blue,
            long meltingPoint, long boilingPoint) {
        return new MaterialDefinition(id, internalName, localName, textureSet,
                red, green, blue, meltingPoint, boilingPoint,
                new Component[0], allSolidForms(), metalProcessing());
    }

    private static TagData[] metalProcessing() {
        return new TagData[] {
                TD.Atomic.METAL,
                TD.Processing.SMITHABLE,
                TD.Processing.MELTING,
                TD.Processing.EXTRUDER,
                TD.Processing.EXTRUDER_SIMPLE,
                TD.Processing.FURNACE,
                TD.Processing.MORTAR
        };
    }

    private static TagData[] plasticForms() {
        TagData[] result = new TagData[TD.ItemGenerator.G_INGOT_MACHINE.length
                + 1];
        System.arraycopy(TD.ItemGenerator.G_INGOT_MACHINE, 0, result, 0,
                TD.ItemGenerator.G_INGOT_MACHINE.length);
        result[TD.ItemGenerator.G_INGOT_MACHINE.length] = TD.ItemGenerator.WIRES;
        return result;
    }

    /**
     * Creates a material with an exact OP whitelist instead of broad ItemGenerator groups.
     * This is useful for rocks and intermediates that must not acquire tiny dusts, plates,
     * tools or other automatically-derived forms.
     */
    private static OreDictMaterial registerSelectiveMaterial(int id, String internalName,
            String localName, TextureSet[] textureSet, long red, long green, long blue,
            TagData[] properties, OreDictPrefix... allowedPrefixes) {
        validateIdentity(id, internalName);
        OreDictMaterial material = OreDictMaterial.createMaterial(id, internalName, localName)
                .setLocal(localName)
                .setOriginalMod("HTGT6", "Hi-Tech GregTech 6")
                .setTextures(textureSet)
                .setRGBa(red, green, blue, 255)
                .put(properties);

        // Blacklist first, then force the explicit whitelist. Forced generation has
        // priority in GT6 and remains deterministic if a prefix condition changes later.
        for (OreDictPrefix prefix : OreDictPrefix.VALUES) {
            if (prefix != null && !containsIdentity(allowedPrefixes, prefix))
                prefix.disableItemGeneration(material);
        }
        for (OreDictPrefix prefix : allowedPrefixes)
            if (prefix != null) prefix.forceItemGeneration(material);
        return material;
    }

    private static boolean containsIdentity(OreDictPrefix[] prefixes, OreDictPrefix target) {
        if (prefixes != null) for (OreDictPrefix prefix : prefixes)
            if (prefix == target) return true;
        return false;
    }

    private static OreDictMaterial register(MaterialDefinition definition) {
        validateDefinition(definition);

        OreDictMaterial material = OreDictMaterial.createMaterial(
                        definition.id, definition.internalName, definition.localName)
                .setLocal(definition.localName)
                .setOriginalMod("HTGT6", "Hi-Tech GregTech 6")
                .setTextures(definition.textureSet)
                .setRGBa(definition.red, definition.green, definition.blue, 255)
                .heat(definition.meltingPoint, definition.boilingPoint)
                .put(definition.generationTags)
                .put(definition.processingTags);

        if (definition.components.length > 0)
            applyComposition(material, definition.components);
        return material;
    }

    private static void validateDefinition(MaterialDefinition definition) {
        validateIdentity(definition.id, definition.internalName);
    }

    private static void validateIdentity(int id, String internalName) {
        if (id <= 0 || id >= OreDictMaterial.MATERIAL_ARRAY.length || id == Short.MAX_VALUE)
            throw new IllegalArgumentException("Invalid GT6 material ID: " + id);

        OreDictMaterial materialAtId = OreDictMaterial.MATERIAL_ARRAY[id];
        if (materialAtId != null) {
            throw new IllegalStateException("GT6 material ID " + id + " is already occupied by "
                    + materialAtId.mNameInternal);
        }

        OreDictMaterial materialWithName = OreDictMaterial.MATERIAL_MAP.get(internalName);
        if (materialWithName != null) {
            throw new IllegalStateException("GT6 material name " + internalName
                    + " is already registered with ID " + materialWithName.mID);
        }
    }

    private static void applyComposition(OreDictMaterial material, Component[] components) {
        OreDictMaterialStack[] stacks = new OreDictMaterialStack[components.length];
        long commonDivider = 0;
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if (component.material == null || component.amount <= 0) {
                throw new IllegalArgumentException("Invalid component at index " + i + " for "
                        + material.mNameInternal);
            }
            commonDivider += component.amount;
            stacks[i] = new OreDictMaterialStack(component.material, CS.U * component.amount);
        }
        material.setMoleculeConfiguration(new OreDictConfigurationComponent(commonDivider, stacks));
    }

    private static TagData[] allSolidForms() {
        TagData[] result = new TagData[TD.ItemGenerator.G_ALL.length + 4];
        System.arraycopy(TD.ItemGenerator.G_ALL, 0, result, 0, TD.ItemGenerator.G_ALL.length);
        int offset = TD.ItemGenerator.G_ALL.length;
        result[offset] = TD.ItemGenerator.TOOLS;
        result[offset + 1] = TD.ItemGenerator.WEAPONS;
        result[offset + 2] = TD.ItemGenerator.OPTICALFIBERS;
        result[offset + 3] = TD.ItemGenerator.MOLTEN;
        return result;
    }

    private static final class MaterialDefinition {
        private final int id;
        private final String internalName;
        private final String localName;
        private final TextureSet[] textureSet;
        private final long red;
        private final long green;
        private final long blue;
        private final long meltingPoint;
        private final long boilingPoint;
        private final Component[] components;
        private final TagData[] generationTags;
        private final TagData[] processingTags;

        private MaterialDefinition(int id, String internalName, String localName, TextureSet[] textureSet,
                long red, long green, long blue, long meltingPoint, long boilingPoint,
                Component[] components, TagData[] generationTags, TagData[] processingTags) {
            this.id = id;
            this.internalName = internalName;
            this.localName = localName;
            this.textureSet = textureSet;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.meltingPoint = meltingPoint;
            this.boilingPoint = boilingPoint;
            this.components = components;
            this.generationTags = generationTags;
            this.processingTags = processingTags;
        }
    }

    private static final class Component {
        private final OreDictMaterial material;
        private final long amount;

        private Component(OreDictMaterial material, long amount) {
            this.material = material;
            this.amount = amount;
        }
    }

    private GT6MaterialAutoGenerator() { }
}
