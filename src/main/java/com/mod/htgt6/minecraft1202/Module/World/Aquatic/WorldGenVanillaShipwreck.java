package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.AquaticChestLoot120;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Loads the vanilla 1.20.2 shipwreck templates and places them in 1.7.10 worlds. */
public final class WorldGenVanillaShipwreck extends WorldGenerator {
    private static final Logger LOGGER=LogManager.getLogger("HTGT6-Shipwrecks");
    public static final String[] OCEAN_TEMPLATES = {
            "rightsideup_backhalf", "rightsideup_backhalf_degraded",
            "rightsideup_fronthalf", "rightsideup_fronthalf_degraded",
            "rightsideup_full", "rightsideup_full_degraded",
            "sideways_backhalf", "sideways_backhalf_degraded",
            "sideways_fronthalf", "sideways_fronthalf_degraded",
            "sideways_full", "sideways_full_degraded",
            "upsidedown_backhalf", "upsidedown_backhalf_degraded",
            "upsidedown_fronthalf", "upsidedown_fronthalf_degraded",
            "upsidedown_full", "upsidedown_full_degraded",
            "with_mast", "with_mast_degraded"
    };

    public static final String[] BEACHED_TEMPLATES = {
            "rightsideup_backhalf_degraded", "rightsideup_fronthalf_degraded",
            "rightsideup_full_degraded", "sideways_backhalf_degraded",
            "sideways_fronthalf_degraded", "sideways_full_degraded",
            "upsidedown_backhalf_degraded", "upsidedown_fronthalf_degraded",
            "upsidedown_full_degraded", "with_mast_degraded"
    };

    private static final Map<String, Template> CACHE = new HashMap<String, Template>();
    private static Field tagListField;

    private final Template template;
    private final int rotation;

    public WorldGenVanillaShipwreck(String templateName, int rotation) {
        this.template = load(templateName);
        this.rotation = rotation & 3;
    }

    /** Fails during mod initialization rather than much later during chunk decoration. */
    public static void validateTemplates() {
        Set<String> unsupported=new HashSet<String>();
        int blocks=0,markers=0;
        for (String name : OCEAN_TEMPLATES) {
            Template template=load(name);
            if(template.sizeX<1||template.sizeY<1||template.sizeZ<1||template.blocks.isEmpty())
                throw new IllegalStateException("Empty shipwreck template: "+name);
            for(State[] palette:template.palettes)for(BlockEntry entry:template.blocks){
                if(entry.state<0||entry.state>=palette.length)
                    throw new IllegalStateException("Invalid palette index in shipwreck template: "+name);
                State state=palette[entry.state];
                if("minecraft:structure_block".equals(state.name)){markers++;continue;}
                if("minecraft:air".equals(state.name))continue;
                if(mapState(state,0)==null)unsupported.add(state.name);
                else blocks++;
            }
        }
        if(!unsupported.isEmpty())throw new IllegalStateException(
                "Shipwreck templates contain unmapped structural blocks: "+unsupported);
        LOGGER.info("Validated {} vanilla shipwreck templates: {} mapped blocks, {} data markers",
                OCEAN_TEMPLATES.length,blocks,markers);
    }

    public int getWidth() {
        return (rotation & 1) == 0 ? template.sizeX : template.sizeZ;
    }

    public int getDepth() {
        return (rotation & 1) == 0 ? template.sizeZ : template.sizeX;
    }

    public int getHeight() {
        return template.sizeY;
    }

    @Override
    public boolean generate(World world, java.util.Random random, int x, int y, int z) {
        if (template.palettes.isEmpty() || y < 1 || y + template.sizeY >= world.getActualHeight()) return false;
        State[] palette = template.palettes.get(random.nextInt(template.palettes.size()));
        List<LootMarker> lootMarkers = new ArrayList<LootMarker>();

        for (BlockEntry entry : template.blocks) {
            if (entry.state < 0 || entry.state >= palette.length) continue;
            State state = palette[entry.state];
            int worldX = x + rotatedX(entry.x, entry.z, template.sizeX, template.sizeZ, rotation);
            int worldY = y + entry.y;
            int worldZ = z + rotatedZ(entry.x, entry.z, template.sizeX, template.sizeZ, rotation);

            if ("minecraft:structure_block".equals(state.name)) {
                if (entry.marker != null && entry.marker.length() > 0) {
                    lootMarkers.add(new LootMarker(worldX, worldY - 1, worldZ, entry.marker));
                }
                continue;
            }

            PlacedBlock placed = mapState(state, rotation);
            if (placed != null) world.setBlock(worldX, worldY, worldZ, placed.block, placed.meta, 2);
        }

        for (LootMarker marker : lootMarkers) fillLootChest(world, random, marker);
        return true;
    }

    private static void fillLootChest(World world, java.util.Random random, LootMarker marker) {
        if (world.getBlock(marker.x, marker.y, marker.z) != Blocks.chest) {
            world.setBlock(marker.x, marker.y, marker.z, Blocks.chest, 0, 2);
        }
        TileEntity initialTile = world.getTileEntity(marker.x, marker.y, marker.z);
        if (!(initialTile instanceof TileEntityChest)) return;

        TileEntityChest chest = (TileEntityChest)initialTile;
        if ("map_chest".equals(marker.type)) {
            AquaticChestLoot120.fillShipwreckMap(chest, random, world, marker.x, marker.z);
            return;
        }
        if ("supply_chest".equals(marker.type)) {
            AquaticChestLoot120.fillShipwreckSupply(chest, random);
        } else {
            AquaticChestLoot120.fillShipwreckTreasure(chest, random);
        }
    }

    private static PlacedBlock mapState(State state, int rotation) {
        String path = state.name.startsWith("minecraft:") ? state.name.substring(10) : state.name;
        if ("chest".equals(path)) return new PlacedBlock(Blocks.chest, chestMeta(rotateFacing(state.get("facing"), rotation)));
        if (path.endsWith("_planks")) return new PlacedBlock(Blocks.planks, woodMeta(path));
        if (path.endsWith("_slab")) {
            int wood = woodMeta(path);
            String type = state.get("type");
            if ("double".equals(type)) return new PlacedBlock(Blocks.double_wooden_slab, wood);
            return new PlacedBlock(Blocks.wooden_slab, wood | ("top".equals(type) ? 8 : 0));
        }
        if (path.endsWith("_log")) {
            int wood = woodMeta(path);
            int axis = "x".equals(state.get("axis")) ? 4 : "z".equals(state.get("axis")) ? 8 : 0;
            if ((rotation & 1) != 0 && axis != 0) axis = axis == 4 ? 8 : 4;
            return wood >= 4
                    ? new PlacedBlock(Blocks.log2, (wood - 4) | axis)
                    : new PlacedBlock(Blocks.log, wood | axis);
        }
        if (path.endsWith("_stairs")) {
            Block stairs = stairsFor(woodMeta(path));
            int meta = stairMeta(rotateFacing(state.get("facing"), rotation));
            if ("top".equals(state.get("half"))) meta |= 4;
            return new PlacedBlock(stairs, meta);
        }
        // 1.7.10 only has oak variants of these blocks.
        if (path.endsWith("_fence")) return new PlacedBlock(Blocks.fence, 0);
        if (path.endsWith("_trapdoor")) {
            int meta = trapdoorMeta(rotateFacing(state.get("facing"), rotation));
            if ("true".equals(state.get("open"))) meta |= 4;
            if ("top".equals(state.get("half"))) meta |= 8;
            return new PlacedBlock(Blocks.trapdoor, meta);
        }
        if (path.endsWith("_door")) {
            if ("upper".equals(state.get("half"))) {
                return new PlacedBlock(Blocks.wooden_door, 8 | ("right".equals(state.get("hinge")) ? 1 : 0));
            }
            return new PlacedBlock(Blocks.wooden_door, doorMeta(rotateFacing(state.get("facing"), rotation)));
        }
        return null;
    }

    private static int woodMeta(String path) {
        if (path.startsWith("spruce_")) return 1;
        if (path.startsWith("birch_")) return 2;
        if (path.startsWith("jungle_")) return 3;
        if (path.startsWith("acacia_")) return 4;
        if (path.startsWith("dark_oak_")) return 5;
        return 0;
    }

    private static Block stairsFor(int wood) {
        switch (wood) {
            case 1: return Blocks.spruce_stairs;
            case 2: return Blocks.birch_stairs;
            case 3: return Blocks.jungle_stairs;
            case 4: return Blocks.acacia_stairs;
            case 5: return Blocks.dark_oak_stairs;
            default: return Blocks.oak_stairs;
        }
    }

    private static String rotateFacing(String facing, int rotation) {
        String[] values = {"north", "east", "south", "west"};
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(facing)) return values[(i + rotation) & 3];
        }
        return facing;
    }

    private static int stairMeta(String facing) {
        if ("east".equals(facing)) return 0;
        if ("west".equals(facing)) return 1;
        if ("south".equals(facing)) return 2;
        return 3;
    }

    private static int doorMeta(String facing) {
        if ("south".equals(facing)) return 1;
        if ("west".equals(facing)) return 2;
        if ("north".equals(facing)) return 3;
        return 0;
    }

    private static int trapdoorMeta(String facing) {
        if ("north".equals(facing)) return 1;
        if ("east".equals(facing)) return 2;
        if ("west".equals(facing)) return 3;
        return 0;
    }

    private static int chestMeta(String facing) {
        if ("south".equals(facing)) return 3;
        if ("west".equals(facing)) return 4;
        if ("east".equals(facing)) return 5;
        return 2;
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

    private static synchronized Template load(String name) {
        Template cached = CACHE.get(name);
        if (cached != null) return cached;
        String path = "/assets/htgt6/structures/shipwreck/" + name + ".nbt";
        InputStream stream = WorldGenVanillaShipwreck.class.getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Missing shipwreck template: " + path);
        try {
            NBTTagCompound root = CompressedStreamTools.readCompressed(stream);
            Template template = readTemplate(root);
            CACHE.put(name, template);
            return template;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read shipwreck template: " + path, e);
        } finally {
            try { stream.close(); } catch (IOException ignored) {}
        }
    }

    private static Template readTemplate(NBTTagCompound root) {
        int[] size = intList(root.getTagList("size", 3));
        List<State[]> palettes = new ArrayList<State[]>();
        if (root.hasKey("palettes", 9)) {
            for (NBTBase base : rawList(root.getTagList("palettes", 9))) {
                if (base instanceof NBTTagList) palettes.add(readPalette((NBTTagList) base));
            }
        } else {
            palettes.add(readPalette(root.getTagList("palette", 10)));
        }

        NBTTagList blockTags = root.getTagList("blocks", 10);
        List<BlockEntry> blocks = new ArrayList<BlockEntry>(blockTags.tagCount());
        for (int i = 0; i < blockTags.tagCount(); i++) {
            NBTTagCompound block = blockTags.getCompoundTagAt(i);
            int[] pos = intList(block.getTagList("pos", 3));
            String marker = block.hasKey("nbt", 10) ? block.getCompoundTag("nbt").getString("metadata") : null;
            blocks.add(new BlockEntry(pos[0], pos[1], pos[2], block.getInteger("state"), marker));
        }
        return new Template(size[0], size[1], size[2], palettes, blocks);
    }

    private static State[] readPalette(NBTTagList list) {
        State[] states = new State[list.tagCount()];
        for (int i = 0; i < states.length; i++) {
            NBTTagCompound state = list.getCompoundTagAt(i);
            Map<String, String> properties = new HashMap<String, String>();
            if (state.hasKey("Properties", 10)) {
                NBTTagCompound propertyTag = state.getCompoundTag("Properties");
                for (Object key : propertyTag.func_150296_c()) {
                    properties.put(String.valueOf(key), propertyTag.getString(String.valueOf(key)));
                }
            }
            states[i] = new State(state.getString("Name"), properties);
        }
        return states;
    }

    private static int[] intList(NBTTagList list) {
        int[] result = new int[list.tagCount()];
        List<NBTBase> values = rawList(list);
        for (int i = 0; i < result.length; i++) {
            result[i] = ((NBTBase.NBTPrimitive) values.get(i)).func_150287_d();
        }
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
            throw new IllegalStateException("Cannot access nested NBT palette list", e);
        }
    }

    private static final class Template {
        final int sizeX, sizeY, sizeZ;
        final List<State[]> palettes;
        final List<BlockEntry> blocks;
        Template(int x, int y, int z, List<State[]> palettes, List<BlockEntry> blocks) {
            this.sizeX = x; this.sizeY = y; this.sizeZ = z;
            this.palettes = Collections.unmodifiableList(palettes);
            this.blocks = Collections.unmodifiableList(blocks);
        }
    }

    private static final class BlockEntry {
        final int x, y, z, state;
        final String marker;
        BlockEntry(int x, int y, int z, int state, String marker) {
            this.x = x; this.y = y; this.z = z; this.state = state; this.marker = marker;
        }
    }

    private static final class State {
        final String name;
        final Map<String, String> properties;
        State(String name, Map<String, String> properties) { this.name = name; this.properties = properties; }
        String get(String key) { return properties.get(key); }
    }

    private static final class PlacedBlock {
        final Block block;
        final int meta;
        PlacedBlock(Block block, int meta) { this.block = block; this.meta = meta; }
    }

    private static final class LootMarker {
        final int x, y, z;
        final String type;
        LootMarker(int x, int y, int z, String type) { this.x = x; this.y = y; this.z = z; this.type = type; }
    }
}
