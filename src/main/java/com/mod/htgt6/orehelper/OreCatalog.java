package com.mod.htgt6.orehelper;

import gregapi.code.ItemStackContainer;
import gregapi.data.CS;
import gregapi.oredict.OreDictMaterial;
import gregapi.util.WD;
import gregapi.worldgen.*;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldChunkManagerSpace;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.DimensionManager;
import java.util.*;

/** Read live GT6 objects, preserving object identity and per-dimension enablement. */
public final class OreCatalog {
    private OreCatalog() {}
    public static int material(ItemStack stack) {
        if (stack == null) return -1;
        gregapi.oredict.OreDictItemData data = gregapi.util.OM.data(stack);
        if (data == null || !data.hasValidPrefixMaterialData() || !data.mPrefix.mNameInternal.startsWith("ore")) return -1;
        return data.mMaterial.mMaterial.mID;
    }
    private static final class WorldRule {
        final WorldProvider provider;
        final String route;
        final String biome;
        WorldRule(WorldProvider provider, String route, String biome) {
            this.provider = provider; this.route = route; this.biome = biome;
        }
        NBTTagCompound tag() {
            NBTTagCompound n = new NBTTagCompound();
            n.setInteger("id", provider.dimensionId);
            n.setString("name", provider.getDimensionName());
            n.setString("route", route);
            n.setString("biome", biome);
            return n;
        }
    }
    public static List<NBTTagCompound> query(int material) {
        List<NBTTagCompound> result = new ArrayList<NBTTagCompound>();
        IdentityHashMap<WorldgenObject, NBTTagCompound> records = new IdentityHashMap<WorldgenObject, NBTTagCompound>();
        Set<WorldgenObject> included = Collections.newSetFromMap(new IdentityHashMap<WorldgenObject, Boolean>());
        Map<String, List<WorldgenObject>> routes = new HashMap<String, List<WorldgenObject>>();
        NBTTagList[] hostLists = new NBTTagList[4];
        List<WorldRule> layerWorlds = new ArrayList<WorldRule>();
        for (WorldRule world : worlds()) {
            List<WorldgenObject> route = routes.get(world.route);
            if (route == null) { route = generators(world.route); routes.put(world.route, route); }
            for (WorldgenObject generator : route) {
                if (generator instanceof gregtech.worldgen.WorldgenStoneLayers) {
                    if (enabled(generator, world.provider)) layerWorlds.add(world);
                    continue;
                }
                // Cache misses as well; most generators do not contain the requested material.
                if (!records.containsKey(generator)) records.put(generator, describe(generator, material, hostLists));
                NBTTagCompound n = records.get(generator);
                if (n == null || !enabled(generator, world.provider)) continue;
                if (included.add(generator)) result.add(n);
                n.getTagList("worlds", 10).appendTag(world.tag());
            }
        }
        if (!layerWorlds.isEmpty()) {
            Set<StoneLayer> visited = Collections.newSetFromMap(new IdentityHashMap<StoneLayer, Boolean>());
            for (StoneLayer layer : StoneLayer.LAYERS) {
                // Repetition in LAYERS weights the geology, not the ore's placement roll.
                if (!visited.add(layer)) continue;
                for (StoneLayerOres ore : layer.mOres) {
                    if (ore.mMaterial.mID != material) continue;
                    NBTTagCompound n = layer(ore, layer.mMaterial.mNameInternal, layerWorlds);
                    NBTTagList hosts = new NBTTagList();
                    addStack(hosts, new ItemStack(layer.mStone, 1, layer.mMetaStone));
                    n.setTag("hosts", hosts);
                    result.add(n);
                }
            }
            for (Map.Entry<OreDictMaterial, Map<OreDictMaterial, List<StoneLayerOres>>> top : StoneLayer.MAP.entrySet()) {
                for (Map.Entry<OreDictMaterial, List<StoneLayerOres>> bottom : top.getValue().entrySet()) {
                    for (StoneLayerOres ore : bottom.getValue()) {
                        if (ore.mMaterial.mID != material) continue;
                        NBTTagCompound n = layer(ore, top.getKey().mNameInternal + " / " + bottom.getKey().mNameInternal, layerWorlds);
                        n.setBoolean("boundary", true);
                        NBTTagList hosts = new NBTTagList();
                        for (StoneLayer host : StoneLayer.LAYERS)
                            if (host.mMaterial == top.getKey() || host.mMaterial == bottom.getKey())
                                addStack(hosts, new ItemStack(host.mStone, 1, host.mMetaStone));
                        n.setTag("hosts", hosts);
                        result.add(n);
                    }
                }
            }
        }
        Collections.sort(result, new Comparator<NBTTagCompound>() {
            public int compare(NBTTagCompound a, NBTTagCompound b) {
                return (a.getInteger("type") + a.getString("name")).compareTo(b.getInteger("type") + b.getString("name"));
            }
        });
        return result;
    }
    private static NBTTagCompound base(String name, int type, int... materials) {
        NBTTagCompound n = new NBTTagCompound();
        n.setString("name", name); n.setInteger("type", type); n.setIntArray("ores", materials);
        n.setTag("worlds", new NBTTagList());
        return n;
    }
    private static NBTTagCompound describe(WorldgenObject g, int material, NBTTagList[] hostLists) {
        NBTTagCompound n;
        if (g instanceof WorldgenOresLarge) {
            WorldgenOresLarge o = (WorldgenOresLarge) g;
            if (o.mTop.mID != material && o.mBottom.mID != material && o.mBetween.mID != material && o.mSpread.mID != material) return null;
            n = base(g.mName, 0, o.mTop.mID, o.mBottom.mID, o.mBetween.mID, o.mSpread.mID);
            // Anchor is [minY,maxY-6], bottom reaches anchor-1 and top anchor+5.
            n.setInteger("min", o.mMinY - 1); n.setInteger("max", o.mMaxY - 1);
            n.setInteger("weight", o.mWeight); n.setInteger("density", o.mDensity);
            n.setInteger("size", o.mSize); n.setInteger("distance", o.mDistance);
            n.setBoolean("rocks", o.mIndicatorRocks);
        } else if (g instanceof WorldgenOresSmall) {
            WorldgenOresSmall o = (WorldgenOresSmall) g;
            if (o.mMaterial.mID != material) return null;
            n = base(g.mName, 2, material);
            n.setInteger("min", o.mMinY); n.setInteger("max", Math.max(o.mMinY, o.mMaxY - 1));
            n.setInteger("amount", o.mAmount);
        } else if (g instanceof WorldgenOresBedrock) {
            WorldgenOresBedrock o = (WorldgenOresBedrock) g;
            boolean hexorium = o.mMaterial == gregapi.data.ANY.Hexorium && material > 0 && material < OreDictMaterial.MATERIAL_ARRAY.length
                    && o.mMaterial.mToThis.contains(OreDictMaterial.MATERIAL_ARRAY[material]);
            if (o.mMaterial.mID != material && !hexorium) return null;
            n = base(g.mName, 3, material);
            n.setInteger("min", 0); n.setInteger("max", 6); n.setInteger("probability", o.mProbability);
            n.setBoolean("rocks", o.mIndicatorRocks); n.setBoolean("flowers", o.mIndicatorFlowers);
            if (o.mIndicatorFlowers && o.mFlower != null) {
                NBTTagList flowers = new NBTTagList(); addStack(flowers, new ItemStack(o.mFlower, 1, o.mFlowerMeta));
                n.setTag("indicators", flowers);
            }
        } else return null;
        NBTTagList rocks = new NBTTagList();
        if (n.getBoolean("rocks")) for (int id : n.getIntArray("ores")) {
            if (id > 0 && id < OreDictMaterial.MATERIAL_ARRAY.length && OreDictMaterial.MATERIAL_ARRAY[id] != null)
                addStack(rocks, gregapi.data.OP.rockGt.mat(OreDictMaterial.MATERIAL_ARRAY[id], 1));
        }
        n.setTag("rockIndicators", rocks);
        int type = n.getInteger("type");
        NBTTagList hosts = hostLists[type];
        if (hosts == null) {
            hosts = new NBTTagList();
            Map<ItemStackContainer, ?> hostMap = type == 2 ? CS.BlocksGT.stoneToSmallOres : CS.BlocksGT.stoneToNormalOres;
            for (ItemStackContainer host : hostMap.keySet()) addStack(hosts, host.toStack());
            if (type == 3) addStack(hosts, new ItemStack(Blocks.bedrock));
            hostLists[type] = hosts;
        }
        n.setTag("hosts", hosts);
        n.setBoolean("hostRegistry", true);
        return n;
    }
    private static NBTTagCompound layer(StoneLayerOres ore, String name, List<WorldRule> worlds) {
        NBTTagCompound n = base(name, 1, ore.mMaterial.mID);
        n.setInteger("min", ore.mMinY); n.setInteger("max", ore.mMaxY); n.setLong("chance", ore.mChance);
        NBTTagList biomes = new NBTTagList();
        for (String biome : ore.mBiomes) biomes.appendTag(new NBTTagString(biome));
        n.setTag("biomes", biomes);
        for (WorldRule world : worlds) n.getTagList("worlds", 10).appendTag(world.tag());
        return n;
    }
    private static void addStack(NBTTagList list, ItemStack stack) {
        if (stack == null || stack.getItem() == null) return;
        NBTTagCompound n = stack.writeToNBT(new NBTTagCompound());
        for (int i = 0; i < list.tagCount(); i++) if (list.getCompoundTagAt(i).equals(n)) return;
        list.appendTag(n);
    }
    private static boolean enabled(WorldgenObject g, WorldProvider p) {
        // GT6 overrides enabled() for the stone-layer generator and ignores base flags.
        if (g instanceof gregtech.worldgen.WorldgenStoneLayers) return CS.GENERATE_STONE;
        if (!g.mEnabled || g.mInvalid) return false;
        Boolean cached = g.mDimEnabled.get(p.dimensionId);
        return cached != null ? cached : CS.ConfigsGT.WORLDGEN.get(g.mCategory + ".dim", p.getDimensionName().replace(' ', '_'), true);
    }
    private static List<WorldgenObject> generators(String route) {
        List<WorldgenObject> list = new ArrayList<WorldgenObject>();
        // These are the public GT6 routing tables, not a copied ore database.
        try {
            list.addAll((List<WorldgenObject>) CS.class.getField("GEN_" + route).get(null));
            if (!"GT".equals(route)) list.addAll((List<WorldgenObject>) CS.class.getField("ORE_" + route).get(null));
        } catch (ReflectiveOperationException e) { throw new IllegalStateException("Unknown GT6 worldgen route " + route, e); }
        return list;
    }
    private static String overworld() {
        return GT6WorldGenerator.TFC ? "TFC" : GT6WorldGenerator.PFAA ? "PFAA" : CS.GENERATE_STONE ? "GT" : "OVERWORLD";
    }
    private static List<WorldRule> worlds() {
        List<WorldRule> result = new ArrayList<WorldRule>();
        Integer[] ids = DimensionManager.getStaticDimensionIDs();
        Arrays.sort(ids);
        for (int id : ids) {
            if (id == Integer.MIN_VALUE) continue;
            try {
                WorldServer loaded = DimensionManager.getWorld(id);
                WorldProvider p = loaded != null ? loaded.provider : DimensionManager.createProviderFor(id);
                if (p == null) continue;
                String route = id == 0 ? overworld() : id == -1 ? "NETHER" : id == 1 ? "END" :
                        WD.dimMYST(p) ? overworld() : WD.dimTF(p) ? "TWILIGHT" : WD.dimAETHER(p) ? "AETHER" :
                        WD.dimERE(p) ? "EREBUS" : WD.dimBTL(p) ? "BETWEENLANDS" : WD.dimATUM(p) ? "ATUM" :
                        WD.dimALF(p) ? "ALFHEIM" : WD.dimDD(p) ? "DEEPDARK" : WD.dimENVM(p) ? "ENVM" :
                        WD.dimTROPIC(p) ? "TROPICS" : WD.dimCANDY(p) ? "CANDY" : null;
                if (route != null) { result.add(new WorldRule(p, route, "")); continue; }
                if (p instanceof WorldProviderSpace) {
                    WorldProviderSpace space = (WorldProviderSpace) p;
                    Class<?> manager = space.getWorldChunkManagerClass();
                    if (manager != null && WorldChunkManagerSpace.class.isAssignableFrom(manager)) {
                        BiomeGenBase biome = ((WorldChunkManagerSpace) manager.newInstance()).getBiome();
                        route = biomeRoute(biome);
                        if (route != null) result.add(new WorldRule(p, route, biome.biomeName));
                    }
                    // HiTech6 explicitly invokes the PLANETS route in these chunk providers.
                    String chunk = space.getChunkProviderClass() == null ? "" : space.getChunkProviderClass().getName();
                    boolean own = chunk.startsWith("com.mod.htgt6.sulimited.") &&
                            (chunk.contains("Gliese") || chunk.contains("SolSysExpanded.minor.") ||
                             chunk.contains("SolSysExpanded.mars.") || chunk.contains("SolSysExpanded.saturn.") ||
                             chunk.contains("SolSysExpanded.uranus."));
                    if (own && !"PLANETS".equals(route)) result.add(new WorldRule(p, "PLANETS", ""));
                }
            } catch (ReflectiveOperationException | RuntimeException e) {
                // Unsupported provider: omit it instead of inventing a dimension or loading a world.
                org.apache.logging.log4j.LogManager.getLogger(OreHelperMod.ID).debug("Cannot inspect dimension " + id, e);
            }
        }
        return result;
    }
    private static String biomeRoute(BiomeGenBase biome) {
        if (biome == null || CS.BIOMES_VOID.contains(biome.biomeName)) return null;
        String b = biome.biomeName;
        return CS.BIOMES_EREBUS.contains(b) ? "EREBUS" : CS.BIOMES_MOON.contains(b) ? "MOON" :
                CS.BIOMES_MARS.contains(b) ? "MARS" : CS.BIOMES_ASTEROIDS.contains(b) ? "ASTEROIDS" :
                CS.BIOMES_SPACE.contains(b) ? "PLANETS" : null;
    }
}
