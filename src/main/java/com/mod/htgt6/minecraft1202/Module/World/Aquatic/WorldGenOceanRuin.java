package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Entity.Aquatic.EntityDrowned;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/** Loads and places the complete vanilla 1.20.2 underwater-ruin template set. */
public final class WorldGenOceanRuin {
    private static final Logger LOGGER = LogManager.getLogger("HTGT6-OceanRuins");
    public static final int BIG_MAP_WEIGHT = 10, BIG_SECOND_POOL_WEIGHT = 23;
    public static final int SMALL_MAP_WEIGHT = 5, SMALL_SECOND_POOL_WEIGHT = 12;
    public static final String[] COLD_SMALL = variants(new String[] {"brick", "cracked", "mossy"}, 1, 8);
    public static final String[] COLD_BIG = bigVariants(new String[] {"brick", "cracked", "mossy"}, new int[] {1, 2, 3, 8});
    public static final String[] WARM_SMALL = variants(new String[] {"warm"}, 1, 8);
    public static final String[] WARM_BIG = bigVariants(new String[] {"warm"}, new int[] {4, 5, 6, 7});

    private static final Map<String, Template> CACHE = new HashMap<String, Template>();
    private static Field tagListField;

    private final String templateName;
    private final Template template;
    private final int rotation;

    public WorldGenOceanRuin(String name, int rotation) {
        templateName = name;
        template = load(name);
        this.rotation = rotation & 3;
    }

    public static void validateTemplates() {
        Set<String> names = new HashSet<String>();
        Collections.addAll(names, COLD_SMALL);
        Collections.addAll(names, COLD_BIG);
        Collections.addAll(names, WARM_SMALL);
        Collections.addAll(names, WARM_BIG);
        if (names.size() != 48) throw new IllegalStateException("Ocean ruin template index must contain 48 unique names");

        int blockCount = 0;
        int markerCount = 0;
        for (String name : names) {
            Template template = load(name);
            if (template.sizeX < 1 || template.sizeY < 1 || template.sizeZ < 1 || template.blocks.isEmpty()) {
                throw new IllegalStateException("Empty ocean ruin template: " + name);
            }
            for (BlockEntry entry : template.blocks) {
                if (entry.x < 0 || entry.x >= template.sizeX || entry.y < 0 || entry.y >= template.sizeY
                        || entry.z < 0 || entry.z >= template.sizeZ) {
                    throw new IllegalStateException("Out-of-bounds block in ocean ruin template: " + name);
                }
                if (entry.state < 0 || entry.state >= template.palette.length) {
                    throw new IllegalStateException("Invalid palette index in ocean ruin template: " + name);
                }
                State state = template.palette[entry.state];
                if ("minecraft:structure_block".equals(state.name)) {
                    if (entry.marker == null || !(entry.marker.toLowerCase().contains("chest")
                            || entry.marker.toLowerCase().contains("drowned"))) {
                        throw new IllegalStateException("Unknown data marker in ocean ruin template " + name
                                + ": " + entry.marker);
                    }
                    markerCount++;
                } else if (!"minecraft:air".equals(state.name)) {
                    if (mapState(state,0) == null) {
                        throw new IllegalStateException("Unsupported block " + state.name
                                + " in ocean ruin template " + name);
                    }
                    blockCount++;
                }
            }
        }
        LOGGER.info("Validated {} vanilla 1.20.2 ocean ruin templates: {} mapped blocks, {} data markers",
                names.size(), blockCount, markerCount);
    }

    public int getWidth() { return (rotation & 1) == 0 ? template.sizeX : template.sizeZ; }
    public int getDepth() { return (rotation & 1) == 0 ? template.sizeZ : template.sizeX; }
    public String getTemplateName() { return templateName; }

    public boolean generate(World world, Random random, int x, int y, int z, float integrity) {
        return generate(world,random,x,y,z,integrity,true);
    }

    /** Overlay palettes reuse marker coordinates; only the base layer may process them. */
    public boolean generate(World world, Random random, int x, int y, int z, float integrity,
                            boolean processMarkers) {
        if (y < 2 || y + template.sizeY >= world.getActualHeight() || template.palette.length == 0) return false;
        List<Marker> markers = processMarkers ? new ArrayList<Marker>() : null;
        int placedCount = 0;

        for (BlockEntry entry : template.blocks) {
            if (entry.state < 0 || entry.state >= template.palette.length) continue;
            State state = template.palette[entry.state];
            int worldX = x + rotatedX(entry.x, entry.z, template.sizeX, template.sizeZ, rotation);
            int worldY = y + entry.y;
            int worldZ = z + rotatedZ(entry.x, entry.z, template.sizeX, template.sizeZ, rotation);

            if ("minecraft:structure_block".equals(state.name)) {
                if (processMarkers && entry.marker != null && entry.marker.length() > 0) {
                    markers.add(new Marker(worldX, worldY, worldZ, entry.marker));
                }
                continue;
            }
            if ("minecraft:air".equals(state.name) || random.nextFloat() > integrity) continue;

            PlacedBlock mapped = mapState(state,rotation);
            if (mapped != null) {
                world.setBlock(worldX, worldY, worldZ, mapped.block, mapped.meta, 2);
                placedCount++;
            }
        }

        if(processMarkers)for (Marker marker : markers) {
            handleMarker(world, random, marker, isBigTemplateName(templateName));
        }
        return placedCount > 0;
    }

    private static void handleMarker(World world, Random random, Marker marker, boolean bigRuin) {
        String type = marker.type.toLowerCase();
        if (type.contains("chest")) {
            world.setBlock(marker.x, marker.y, marker.z, Blocks.chest, random.nextInt(4) + 2, 2);
            TileEntity initialTile = world.getTileEntity(marker.x, marker.y, marker.z);
            if (initialTile instanceof TileEntityChest) {
                TileEntityChest chest = (TileEntityChest)initialTile;
                AquaticChestLoot120.fillOceanRuin(chest, random, world,
                        marker.x, marker.z, bigRuin);
            }
        } else if (type.contains("drowned")) {
            // 1.7.10 has no drowned entity. A persistent underwater zombie is
            // the closest behaviour-compatible guardian and needs no client mod entity ID.
            EntityDrowned guardian = new EntityDrowned(world);
            guardian.setLocationAndAngles(marker.x + 0.5D, marker.y, marker.z + 0.5D,
                    random.nextFloat() * 360.0F, 0.0F);
            guardian.func_110163_bv();
            guardian.onSpawnWithEgg(null);
            world.spawnEntityInWorld(guardian);
        }
    }

    private static PlacedBlock mapState(State state,int rotation) {
        String path = state.name.startsWith("minecraft:") ? state.name.substring(10) : state.name;
        if ("stone_bricks".equals(path)) return new PlacedBlock(Blocks.stonebrick,0);
        if ("cracked_stone_bricks".equals(path)) return new PlacedBlock(Blocks.stonebrick,2);
        if ("mossy_stone_bricks".equals(path)) return new PlacedBlock(Blocks.stonebrick,1);
        if ("chiseled_stone_bricks".equals(path)) return new PlacedBlock(Blocks.stonebrick,3);
        if ("stone_brick_stairs".equals(path)) return new PlacedBlock(
                Blocks.stone_brick_stairs,stairMeta(state,rotation));
        if ("cobblestone".equals(path)) return new PlacedBlock(Blocks.cobblestone,0);
        if ("mossy_cobblestone".equals(path)) return new PlacedBlock(Blocks.mossy_cobblestone,0);
        if ("prismarine".equals(path)) return new PlacedBlock(BountifulContent.prismarine,0);

        if ("sandstone".equals(path)) return new PlacedBlock(Blocks.sandstone,0);
        if ("cut_sandstone".equals(path)) return new PlacedBlock(Blocks.sandstone,2);
        if ("chiseled_sandstone".equals(path)) return new PlacedBlock(Blocks.sandstone,1);
        if ("sandstone_stairs".equals(path)) return new PlacedBlock(
                Blocks.sandstone_stairs,stairMeta(state,rotation));
        if ("bricks".equals(path)) return new PlacedBlock(Blocks.brick_block,0);
        if ("polished_diorite".equals(path)) return new PlacedBlock(BountifulContent.stone,3);
        if ("polished_granite".equals(path)) return new PlacedBlock(BountifulContent.stone,1);

        if ("sand".equals(path)) return new PlacedBlock(Blocks.sand, 0);
        if ("gravel".equals(path)) return new PlacedBlock(Blocks.gravel, 0);
        if ("magma_block".equals(path)) return new PlacedBlock(FrostburnContent.magmaBlock,0);
        if ("obsidian".equals(path)) return new PlacedBlock(Blocks.obsidian,0);
        if ("sea_lantern".equals(path)) return new PlacedBlock(BountifulContent.seaLantern,0);
        if ("light_blue_terracotta".equals(path)) return new PlacedBlock(Blocks.stained_hardened_clay, 3);
        if ("purple_glazed_terracotta".equals(path)) return new PlacedBlock(Blocks.stained_hardened_clay, 10);
        if ("spruce_planks".equals(path)) return new PlacedBlock(Blocks.planks, 1);
        if ("dark_oak_planks".equals(path)) return new PlacedBlock(Blocks.planks, 5);
        return null;
    }

    private static int stairMeta(State state,int rotation){
        String facing=state.get("facing");String[] values={"north","east","south","west"};
        for(int i=0;i<4;i++)if(values[i].equals(facing)){facing=values[(i+rotation)&3];break;}
        int meta="east".equals(facing)?0:"west".equals(facing)?1:"south".equals(facing)?2:3;
        return "top".equals(state.get("half"))?meta|4:meta;
    }

    private static synchronized Template load(String name) {
        Template cached = CACHE.get(name);
        if (cached != null) return cached;
        String path = "/assets/htgt6/structures/ocean_ruin/" + name + ".nbt";
        InputStream stream = WorldGenOceanRuin.class.getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Missing ocean ruin template: " + path);
        try {
            NBTTagCompound root = CompressedStreamTools.readCompressed(stream);
            int[] size = intList(root.getTagList("size", 3));
            State[] palette = readPalette(root.getTagList("palette", 10));
            NBTTagList tags = root.getTagList("blocks", 10);
            List<BlockEntry> blocks = new ArrayList<BlockEntry>(tags.tagCount());
            for (int i = 0; i < tags.tagCount(); i++) {
                NBTTagCompound block = tags.getCompoundTagAt(i);
                int[] pos = intList(block.getTagList("pos", 3));
                String marker = block.hasKey("nbt", 10)
                        ? block.getCompoundTag("nbt").getString("metadata") : null;
                blocks.add(new BlockEntry(pos[0], pos[1], pos[2], block.getInteger("state"), marker));
            }
            Template template = new Template(size[0], size[1], size[2], palette, blocks);
            CACHE.put(name, template);
            return template;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read ocean ruin template: " + path, e);
        } finally {
            try { stream.close(); } catch (IOException ignored) { }
        }
    }

    private static State[] readPalette(NBTTagList list) {
        State[] result = new State[list.tagCount()];
        for (int i = 0; i < result.length; i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            Map<String,String> properties=new HashMap<String,String>();
            if(tag.hasKey("Properties",10)){
                NBTTagCompound values=tag.getCompoundTag("Properties");
                for(Object key:values.func_150296_c())
                    properties.put(String.valueOf(key),values.getString(String.valueOf(key)));
            }
            result[i] = new State(tag.getString("Name"),properties);
        }
        return result;
    }

    private static int[] intList(NBTTagList list) {
        List<NBTBase> raw = rawList(list);
        int[] result = new int[raw.size()];
        for (int i = 0; i < result.length; i++) result[i] = ((NBTBase.NBTPrimitive) raw.get(i)).func_150287_d();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<NBTBase> rawList(NBTTagList list) {
        try {
            if (tagListField == null) {
                for (Field field : NBTTagList.class.getDeclaredFields()) {
                    if (List.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        tagListField = field;
                        break;
                    }
                }
            }
            if (tagListField == null) throw new IllegalStateException("NBTTagList backing list not found");
            return (List<NBTBase>) tagListField.get(list);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access ocean ruin NBT list", e);
        }
    }

    private static int rotatedX(int x, int z, int sizeX, int sizeZ, int rotation) {
        switch (rotation & 3) {
            case 1: return sizeZ - 1 - z;
            case 2: return sizeX - 1 - x;
            case 3: return z;
            default: return x;
        }
    }

    private static int rotatedZ(int x, int z, int sizeX, int sizeZ, int rotation) {
        switch (rotation & 3) {
            case 1: return x;
            case 2: return sizeZ - 1 - z;
            case 3: return sizeX - 1 - x;
            default: return z;
        }
    }

    private static String[] variants(String[] prefixes, int first, int last) {
        List<String> result = new ArrayList<String>();
        for (String prefix : prefixes) for (int i = first; i <= last; i++) result.add(prefix + "_" + i);
        return result.toArray(new String[result.size()]);
    }

    private static String[] bigVariants(String[] prefixes, int[] numbers) {
        List<String> result = new ArrayList<String>();
        for (String prefix : prefixes) for (int number : numbers) result.add("big_" + prefix + "_" + number);
        return result.toArray(new String[result.size()]);
    }

    public static boolean isBigTemplateName(String name) {
        return name != null && name.startsWith("big_");
    }

    private static final class Template {
        final int sizeX, sizeY, sizeZ;
        final State[] palette;
        final List<BlockEntry> blocks;
        Template(int x, int y, int z, State[] palette, List<BlockEntry> blocks) {
            sizeX = x; sizeY = y; sizeZ = z; this.palette = palette;
            this.blocks = Collections.unmodifiableList(blocks);
        }
    }

    private static final class State {
        final String name;
        final Map<String,String> properties;
        State(String name,Map<String,String> properties) { this.name = name;this.properties=properties; }
        String get(String key){String value=properties.get(key);return value==null?"":value;}
    }

    private static final class BlockEntry {
        final int x, y, z, state;
        final String marker;
        BlockEntry(int x, int y, int z, int state, String marker) {
            this.x = x; this.y = y; this.z = z; this.state = state; this.marker = marker;
        }
    }

    private static final class PlacedBlock {
        final Block block;
        final int meta;
        PlacedBlock(Block block, int meta) { this.block = block; this.meta = meta; }
    }

    private static final class Marker {
        final int x, y, z;
        final String type;
        Marker(int x, int y, int z, String type) { this.x = x; this.y = y; this.z = z; this.type = type; }
    }
}
