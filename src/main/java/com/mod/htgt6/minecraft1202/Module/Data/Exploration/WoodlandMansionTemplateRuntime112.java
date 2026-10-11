package com.mod.htgt6.minecraft1202.Module.Data.Exploration;

import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityEvoker112;
import com.mod.htgt6.minecraft1202.Module.Entity.Exploration.EntityVindicator112;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.WoodlandMansionLayout112;
import com.mod.htgt6.minecraft1202.Module.Registry.Exploration.ExplorationContent112;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;

/**
 * Isolated runtime for the unmodified Minecraft 1.12 woodland-mansion NBT set.
 * Loading and transforms are production-ready; the old generator is deliberately
 * not connected until structure-start persistence makes marker/entity placement
 * idempotent across chunk repopulation.
 */
public final class WoodlandMansionTemplateRuntime112 {
    private static final String PATH = "/assets/htgt6/structures/mansion/";
    public static final String[] TEMPLATE_NAMES = {
        "1x1_a1","1x1_a2","1x1_a3","1x1_a4","1x1_a5","1x1_as1","1x1_as2","1x1_as3","1x1_as4",
        "1x1_b1","1x1_b2","1x1_b3","1x1_b4","1x1_b5","1x2_a1","1x2_a2","1x2_a3","1x2_a4",
        "1x2_a5","1x2_a6","1x2_a7","1x2_a8","1x2_a9","1x2_b1","1x2_b2","1x2_b3","1x2_b4",
        "1x2_b5","1x2_c_stairs","1x2_c1","1x2_c2","1x2_c3","1x2_c4","1x2_d_stairs","1x2_d1",
        "1x2_d2","1x2_d3","1x2_d4","1x2_d5","1x2_s1","1x2_s2","1x2_se1","2x2_a1","2x2_a2",
        "2x2_a3","2x2_a4","2x2_b1","2x2_b2","2x2_b3","2x2_b4","2x2_b5","2x2_s1","carpet_east",
        "carpet_north","carpet_south_2","carpet_south","carpet_west_2","carpet_west","corridor_floor",
        "entrance","indoors_door_2","indoors_door","indoors_wall_2","indoors_wall","roof_corner","roof_front",
        "roof_inner_corner","roof","small_wall_corner","small_wall","wall_corner","wall_flat","wall_window"
    };

    private static final Map<String, Template> CACHE = new HashMap<String, Template>();
    private static volatile Field tagListField;

    private WoodlandMansionTemplateRuntime112() { }

    public static synchronized Template load(String name) {
        Template old = CACHE.get(name);
        if (old != null) return old;
        if (!isKnown(name)) throw new IllegalArgumentException("Unknown mansion template: " + name);
        InputStream input = WoodlandMansionTemplateRuntime112.class.getResourceAsStream(PATH + name + ".nbt");
        if (input == null) throw new IllegalStateException("Missing mansion template: " + name);
        try {
            NBTTagCompound root = CompressedStreamTools.readCompressed(input);
            int[] size = intList(root.getTagList("size", 3));
            if (size.length != 3) throw new IllegalStateException("Invalid size in mansion template " + name);
            NBTTagList paletteTag = root.getTagList("palette", 10);
            List<State> palette = new ArrayList<State>(paletteTag.tagCount());
            for (int i = 0; i < paletteTag.tagCount(); ++i) {
                NBTTagCompound stateTag = paletteTag.getCompoundTagAt(i);
                Map<String, String> properties = new LinkedHashMap<String, String>();
                if (stateTag.hasKey("Properties", 10)) {
                    NBTTagCompound values = stateTag.getCompoundTag("Properties");
                    List<String> keys = new ArrayList<String>();
                    for (Object key : values.func_150296_c()) keys.add(String.valueOf(key));
                    Collections.sort(keys);
                    for (String key : keys) properties.put(key, values.getString(key));
                }
                palette.add(new State(stateTag.getString("Name"), properties));
            }
            NBTTagList blockTag = root.getTagList("blocks", 10);
            List<BlockEntry> blocks = new ArrayList<BlockEntry>(blockTag.tagCount());
            for (int i = 0; i < blockTag.tagCount(); ++i) {
                NBTTagCompound value = blockTag.getCompoundTagAt(i);
                int[] pos = intList(value.getTagList("pos", 3));
                int state = value.getInteger("state");
                if (pos.length != 3 || state < 0 || state >= palette.size())
                    throw new IllegalStateException("Invalid block in mansion template " + name + " at " + i);
                blocks.add(new BlockEntry(pos[0], pos[1], pos[2], state,
                        value.hasKey("nbt", 10) ? copy(value.getCompoundTag("nbt")) : null));
            }
            NBTTagList entityTag = root.getTagList("entities", 10);
            List<EntityEntry> entities = new ArrayList<EntityEntry>(entityTag.tagCount());
            for (int i = 0; i < entityTag.tagCount(); ++i) {
                NBTTagCompound value = entityTag.getCompoundTagAt(i);
                double[] pos = doubleList(value.getTagList("pos", 6));
                int[] blockPos = intList(value.getTagList("blockPos", 3));
                if (pos.length != 3 || blockPos.length != 3 || !value.hasKey("nbt", 10))
                    throw new IllegalStateException("Invalid entity in mansion template " + name + " at " + i);
                entities.add(new EntityEntry(pos[0], pos[1], pos[2], blockPos[0], blockPos[1], blockPos[2],
                        copy(value.getCompoundTag("nbt"))));
            }
            Template result = new Template(name, size[0], size[1], size[2], palette, blocks, entities);
            CACHE.put(name, result);
            return result;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read mansion template: " + name, error);
        } finally {
            try { input.close(); } catch (IOException ignored) { }
        }
    }

    /** Places only cells whose final X/Z lie in the requested chunk. */
    public static PlacementResult placeInChunk(World world, Template template,
            WoodlandMansionLayout112.Piece piece, int originX, int originY, int originZ,
            int chunkX, int chunkZ, Random random) {
        if (world == null || template == null || piece == null || random == null)
            throw new IllegalArgumentException("world/template/piece/random");
        int minX = chunkX << 4, minZ = chunkZ << 4;
        int maxX = minX + 15, maxZ = minZ + 15;
        int blocks = 0, blockEntities = 0, markers = 0, entities = 0;
        for (BlockEntry entry : template.blocks) {
            Point point = transform(entry.x, entry.y, entry.z, piece.mirror, piece.rotation);
            int x = originX + piece.x + point.x, y = originY + piece.y + point.y;
            int z = originZ + piece.z + point.z;
            if (x < minX || x > maxX || z < minZ || z > maxZ || y < 0 || y >= world.getActualHeight()) continue;
            State state = transform(template.palette.get(entry.state), piece.mirror, piece.rotation);
            if ("structure_block".equals(state.shortName())) {
                String marker = entry.nbt == null ? "" : entry.nbt.getString("metadata");
                handleMarker(world, marker, x, y, z, piece.mirror, piece.rotation, random);
                ++markers;
                continue;
            }
            Placed placed = map(state);
            if (placed == null) throw new IllegalStateException("Unsupported mansion state: " + state);
            world.setBlock(x, y, z, placed.block, placed.meta, 2);
            ++blocks;
            if (entry.nbt != null && applyBlockEntity(world, entry.nbt, x, y, z)) ++blockEntities;
        }
        for (EntityEntry entry : template.entities) {
            DPoint point = transform(entry.x, entry.y, entry.z, piece.mirror, piece.rotation);
            double x = originX + piece.x + point.x, y = originY + piece.y + point.y;
            double z = originZ + piece.z + point.z;
            if (x < minX || x >= maxX + 1.0D || z < minZ || z >= maxZ + 1.0D
                    || y < 0.0D || y >= world.getActualHeight()) continue;
            NBTTagCompound data = copy(entry.nbt);
            NBTTagList pos = new NBTTagList();
            pos.appendTag(new net.minecraft.nbt.NBTTagDouble(x));
            pos.appendTag(new net.minecraft.nbt.NBTTagDouble(y));
            pos.appendTag(new net.minecraft.nbt.NBTTagDouble(z));
            data.setTag("Pos", pos);
            normalizeEntityId(data);
            Entity entity = EntityList.createEntityFromNBT(data, world);
            if (entity != null) {
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw + piece.rotation.quarters * 90.0F,
                        entity.rotationPitch);
                world.spawnEntityInWorld(entity);
                ++entities;
            }
        }
        return new PlacementResult(blocks, blockEntities, markers, entities);
    }

    public static Point transform(int x, int y, int z, WoodlandMansionLayout112.Mirror mirror,
            WoodlandMansionLayout112.Rotation rotation) {
        int mx = mirror == WoodlandMansionLayout112.Mirror.FRONT_BACK ? -x : x;
        int mz = mirror == WoodlandMansionLayout112.Mirror.LEFT_RIGHT ? -z : z;
        switch (rotation) {
            case CLOCKWISE_90: return new Point(-mz, y, mx);
            case CLOCKWISE_180: return new Point(-mx, y, -mz);
            case COUNTERCLOCKWISE_90: return new Point(mz, y, -mx);
            default: return new Point(mx, y, mz);
        }
    }

    private static DPoint transform(double x, double y, double z, WoodlandMansionLayout112.Mirror mirror,
            WoodlandMansionLayout112.Rotation rotation) {
        double mx = mirror == WoodlandMansionLayout112.Mirror.FRONT_BACK ? -x : x;
        double mz = mirror == WoodlandMansionLayout112.Mirror.LEFT_RIGHT ? -z : z;
        switch (rotation) {
            case CLOCKWISE_90: return new DPoint(-mz, y, mx);
            case CLOCKWISE_180: return new DPoint(-mx, y, -mz);
            case COUNTERCLOCKWISE_90: return new DPoint(mz, y, -mx);
            default: return new DPoint(mx, y, mz);
        }
    }

    public static State transform(State source, WoodlandMansionLayout112.Mirror mirror,
            WoodlandMansionLayout112.Rotation rotation) {
        Map<String, String> values = new LinkedHashMap<String, String>(source.properties);
        String originalFacing = values.get("facing");
        if (values.containsKey("facing")) values.put("facing", transformFacing(values.get("facing"), mirror, rotation));
        if (("up_x".equals(originalFacing) || "up_z".equals(originalFacing))
                && (rotation == WoodlandMansionLayout112.Rotation.CLOCKWISE_90
                || rotation == WoodlandMansionLayout112.Rotation.COUNTERCLOCKWISE_90))
            values.put("facing", "up_x".equals(originalFacing) ? "up_z" : "up_x");
        if (source.shortName().endsWith("_stairs") && values.containsKey("shape")
                && !"straight".equals(values.get("shape"))) {
            int facing = direction(originalFacing);
            boolean swap = mirror == WoodlandMansionLayout112.Mirror.LEFT_RIGHT && (facing & 1) == 0
                    || mirror == WoodlandMansionLayout112.Mirror.FRONT_BACK && (facing & 1) == 1;
            if (swap) values.put("shape", swapLeftRight(values.get("shape")));
        }
        if (values.containsKey("shape") && source.shortName().contains("rail"))
            values.put("shape", transformRail(values.get("shape"), mirror, rotation));
        if (source.shortName().equals("vine")) {
            Map<String, String> old = new HashMap<String, String>(values);
            for (String direction : HORIZONTAL) values.put(direction, "false");
            for (String direction : HORIZONTAL) if ("true".equals(old.get(direction)))
                values.put(transformFacing(direction, mirror, rotation), "true");
        }
        if (values.containsKey("axis") && (rotation == WoodlandMansionLayout112.Rotation.CLOCKWISE_90
                || rotation == WoodlandMansionLayout112.Rotation.COUNTERCLOCKWISE_90)) {
            if ("x".equals(values.get("axis"))) values.put("axis", "z");
            else if ("z".equals(values.get("axis"))) values.put("axis", "x");
        }
        return new State(source.name, values);
    }

    public static boolean supports(State state) { return mapDescriptor(state) != null; }

    public static boolean isInsideChunk(double x, double z, int chunkX, int chunkZ) {
        int minX = chunkX << 4, minZ = chunkZ << 4;
        return x >= minX && x < minX + 16.0D && z >= minZ && z < minZ + 16.0D;
    }

    public static boolean intersectsChunk(Template template, WoodlandMansionLayout112.Piece piece,
            int originX, int originZ, int chunkX, int chunkZ) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        int[] xs = {0, template.sizeX - 1}, zs = {0, template.sizeZ - 1};
        for (int x : xs) for (int z : zs) {
            Point p = transform(x, 0, z, piece.mirror, piece.rotation);
            int worldX = originX + piece.x + p.x, worldZ = originZ + piece.z + p.z;
            minX = Math.min(minX, worldX); maxX = Math.max(maxX, worldX);
            minZ = Math.min(minZ, worldZ); maxZ = Math.max(maxZ, worldZ);
        }
        int chunkMinX = chunkX << 4, chunkMinZ = chunkZ << 4;
        return maxX >= chunkMinX && minX <= chunkMinX + 15
                && maxZ >= chunkMinZ && minZ <= chunkMinZ + 15;
    }

    private static final String[] HORIZONTAL = {"north", "east", "south", "west"};
    private static String transformFacing(String facing, WoodlandMansionLayout112.Mirror mirror,
            WoodlandMansionLayout112.Rotation rotation) {
        int index = direction(facing);
        if (index < 0) return facing;
        if (mirror == WoodlandMansionLayout112.Mirror.LEFT_RIGHT && (index & 1) == 0) index = (index + 2) & 3;
        if (mirror == WoodlandMansionLayout112.Mirror.FRONT_BACK && (index & 1) == 1) index = (index + 2) & 3;
        return HORIZONTAL[(index + rotation.quarters) & 3];
    }

    private static String transformRail(String shape, WoodlandMansionLayout112.Mirror mirror,
            WoodlandMansionLayout112.Rotation rotation) {
        if (shape.startsWith("ascending_"))
            return "ascending_" + transformFacing(shape.substring(10), mirror, rotation);
        String[] parts = shape.split("_");
        if (parts.length != 2) return shape;
        String a = transformFacing(parts[0], mirror, rotation);
        String b = transformFacing(parts[1], mirror, rotation);
        if (direction(a) < 0 || direction(b) < 0) return shape;
        if ((a.equals("north") && b.equals("south")) || (a.equals("south") && b.equals("north"))) return "north_south";
        if ((a.equals("east") && b.equals("west")) || (a.equals("west") && b.equals("east"))) return "east_west";
        boolean north = "north".equals(a) || "north".equals(b);
        boolean south = "south".equals(a) || "south".equals(b);
        boolean east = "east".equals(a) || "east".equals(b);
        boolean west = "west".equals(a) || "west".equals(b);
        if (south && east) return "south_east";
        if (south && west) return "south_west";
        if (north && west) return "north_west";
        return "north_east";
    }

    private static int direction(String facing) {
        for (int i = 0; i < HORIZONTAL.length; ++i) if (HORIZONTAL[i].equals(facing)) return i;
        return -1;
    }

    private static String swapLeftRight(String shape) {
        if ("inner_left".equals(shape)) return "inner_right";
        if ("inner_right".equals(shape)) return "inner_left";
        if ("outer_left".equals(shape)) return "outer_right";
        if ("outer_right".equals(shape)) return "outer_left";
        return shape;
    }

    private static Placed map(State state) {
        Descriptor value = mapDescriptor(state);
        if (value == null) return null;
        Block block;
        if ("bountiful_wall_banner".equals(value.name)) block = BountifulContent.wallBanner;
        else if ("bountiful_stone".equals(value.name)) block = BountifulContent.stone;
        else if ("bountiful_coarse_dirt".equals(value.name)) block = BountifulContent.coarseDirt;
        else if ("bountiful_birch_fence".equals(value.name)) block = BountifulContent.woodFences[1];
        else if ("bountiful_dark_oak_fence".equals(value.name)) block = BountifulContent.woodFences[4];
        else if ("bountiful_dark_oak_fence_gate".equals(value.name)) block = BountifulContent.woodFenceGates[4];
        else if ("bountiful_dark_oak_door".equals(value.name)) block = BountifulContent.woodDoors[4];
        else block = Block.getBlockFromName("minecraft:" + value.name);
        if (block == null) return null;
        return new Placed(block, value.meta);
    }

    /** Mapping is kept block-free so the headless fixture can validate every palette property. */
    static Descriptor mapDescriptor(State state) {
        String n = state.shortName();
        int meta = 0;
        if ("structure_block".equals(n)) return null;
        if ("wall_banner".equals(n)) return new Descriptor("bountiful_wall_banner", sideMeta(state.get("facing")));
        if ("stone".equals(n) && "smooth_andesite".equals(state.get("variant"))) return new Descriptor("bountiful_stone", 5);
        if ("birch_fence".equals(n)) return new Descriptor("bountiful_birch_fence", 0);
        if ("dark_oak_fence".equals(n)) return new Descriptor("bountiful_dark_oak_fence", 0);
        if ("dark_oak_fence_gate".equals(n)) return new Descriptor("bountiful_dark_oak_fence_gate",
                doorFacingMeta(state.get("facing")) | ("true".equals(state.get("open")) ? 4 : 0));
        if ("dark_oak_door".equals(n)) return new Descriptor("bountiful_dark_oak_door", doorMeta(state));
        if (n.endsWith("_stairs")) meta = stairMeta(state);
        else if ("chest".equals(n) || "trapped_chest".equals(n) || "ladder".equals(n)) meta = sideMeta(state.get("facing"));
        else if ("torch".equals(n)) meta = torchMeta(state.get("facing"));
        else if ("anvil".equals(n)) meta = (parse(state.get("damage"), 0) << 2) | (direction(state.get("facing")) & 1);
        else if ("carpet".equals(n) || "wool".equals(n)) meta = colorMeta(state.get("color"));
        else if ("cauldron".equals(n) || "water".equals(n) || "lava".equals(n)) meta = parse(state.get("level"), 0);
        else if ("dirt".equals(n) && "coarse_dirt".equals(state.get("variant"))) return new Descriptor("bountiful_coarse_dirt", 0);
        else if ("dirt".equals(n)) meta = "podzol".equals(state.get("variant")) ? 2 : 0;
        else if ("log2".equals(n)) meta = ("dark_oak".equals(state.get("variant")) ? 1 : 0) | axisMeta(state.get("axis"));
        else if ("leaves2".equals(n)) meta = ("dark_oak".equals(state.get("variant")) ? 1 : 0)
                | (!"true".equals(state.get("decayable")) ? 4 : 0) | ("true".equals(state.get("check_decay")) ? 8 : 0);
        else if ("planks".equals(n)) meta = woodMeta(state.get("variant"));
        else if ("wooden_slab".equals(n)) meta = woodMeta(state.get("variant")) | ("top".equals(state.get("half")) ? 8 : 0);
        else if ("stone_slab".equals(n)) meta = slabMeta(state.get("variant")) | ("top".equals(state.get("half")) ? 8 : 0);
        else if ("double_stone_slab".equals(n)) meta = slabMeta(state.get("variant"));
        else if ("dark_oak_door".equals(state.shortName()) || "iron_door".equals(n) || "wooden_door".equals(n)) meta = doorMeta(state);
        else if ("fence_gate".equals(n)) meta = doorFacingMeta(state.get("facing")) | ("true".equals(state.get("open")) ? 4 : 0);
        else if ("rail".equals(n)) meta = railMeta(state.get("shape"));
        else if ("redstone_wire".equals(n)) meta = parse(state.get("power"), 0);
        else if ("wheat".equals(n) || "pumpkin_stem".equals(n) || "melon_stem".equals(n)) meta = parse(state.get("age"), 0);
        else if ("pumpkin".equals(n)) meta = pumpkinMeta(state.get("facing"));
        else if ("monster_egg".equals(n)) meta = monsterEggMeta(state.get("variant"));
        else if ("lever".equals(n)) meta = leverMeta(state.get("facing")) | ("true".equals(state.get("powered")) ? 8 : 0);
        else if ("sapling".equals(n)) meta = woodMeta(state.get("type")) | (parse(state.get("stage"), 0) << 3);
        else if ("stone".equals(n)) meta = stoneMeta(state.get("variant"));
        if (!KNOWN.containsKey(n)) return null;
        return new Descriptor(n, meta & 15);
    }

    private static final Map<String, Boolean> KNOWN = known();
    private static Map<String, Boolean> known() {
        String names = "air anvil fence birch_stairs bookshelf brown_mushroom carpet cauldron chest cobblestone cobblestone_wall dark_oak_stairs diamond_block dirt double_stone_slab farmland fence_gate flower_pot glass glass_pane iron_bars iron_door ladder lapis_block lava leaves2 lever log2 melon_block melon_stem mob_spawner monster_egg oak_stairs obsidian planks pumpkin pumpkin_stem rail red_mushroom redstone_wire sapling stone stone_slab stone_stairs tnt torch trapped_chest vine water waterlily web wheat wooden_door wooden_slab wool";
        Map<String, Boolean> result = new HashMap<String, Boolean>();
        for (String name : names.split(" ")) result.put(name, Boolean.TRUE);
        return result;
    }

    private static boolean applyBlockEntity(World world, NBTTagCompound source, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) return false;
        NBTTagCompound data = copy(source);
        data.setInteger("x", x); data.setInteger("y", y); data.setInteger("z", z);
        if (tile instanceof TileEntityMobSpawner) adaptSpawner112To17(data);
        tile.readFromNBT(data);
        tile.markDirty();
        world.markBlockForUpdate(x, y, z);
        return true;
    }

    private static void handleMarker(World world, String marker, int x, int y, int z,
            WoodlandMansionLayout112.Mirror mirror, WoodlandMansionLayout112.Rotation rotation,
            Random random) {
        world.setBlockToAir(x, y, z);
        if (marker.startsWith("Chest")) {
            String facing = marker.substring(5).toLowerCase(java.util.Locale.ROOT);
            facing = transformFacing(facing, mirror, rotation);
            world.setBlock(x, y, z, Blocks.chest, sideMeta(facing), 2);
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof IInventory) fillChest((IInventory) tile, random);
        } else if ("Mage".equals(marker)) {
            EntityEvoker112 mob = new EntityEvoker112(world);
            mob.func_110163_bv(); mob.setLocationAndAngles(x + .5D, y, z + .5D, 0, 0);
            world.spawnEntityInWorld(mob);
        } else if ("Warrior".equals(marker)) {
            EntityVindicator112 mob = new EntityVindicator112(world);
            mob.func_110163_bv(); mob.setLocationAndAngles(x + .5D, y, z + .5D, 0, 0);
            world.spawnEntityInWorld(mob);
        }
    }

    private static void fillChest(IInventory chest, Random random) {
        com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionLoot120.fill(chest,random);
    }

    private static void normalizeEntityId(NBTTagCompound data) {
        String id = data.getString("id");
        if (id.startsWith("minecraft:")) data.setString("id", legacyEntityId(id));
    }

    private static String legacyEntityId(String id) {
        if (id.startsWith("minecraft:")) {
            String simple = id.substring(10);
            StringBuilder legacy = new StringBuilder();
            boolean upper = true;
            for (int i = 0; i < simple.length(); ++i) {
                char c = simple.charAt(i);
                if (c == '_') { upper = true; continue; }
                legacy.append(upper ? Character.toUpperCase(c) : c); upper = false;
            }
            return legacy.toString();
        }
        return id;
    }

    /** Converts only the schema keys consumed differently by the 1.7 spawner logic. */
    static void adaptSpawner112To17(NBTTagCompound data) {
        if (data.hasKey("SpawnData", 10)) {
            NBTTagCompound spawn = data.getCompoundTag("SpawnData");
            if (spawn.hasKey("id", 8)) data.setString("EntityId", legacyEntityId(spawn.getString("id")));
        }
        if (!data.hasKey("SpawnPotentials", 9)) return;
        NBTTagList modern = data.getTagList("SpawnPotentials", 10), legacy = new NBTTagList();
        for (int i = 0; i < modern.tagCount(); ++i) {
            NBTTagCompound source = modern.getCompoundTagAt(i), value = new NBTTagCompound();
            value.setInteger("Weight", source.getInteger("Weight"));
            NBTTagCompound entity = source.hasKey("Entity", 10)
                    ? copy(source.getCompoundTag("Entity")) : copy(source.getCompoundTag("Properties"));
            String id = source.hasKey("Entity", 10) ? entity.getString("id") : source.getString("Type");
            value.setString("Type", legacyEntityId(id));
            entity.removeTag("id");
            value.setTag("Properties", entity);
            legacy.appendTag(value);
        }
        data.setTag("SpawnPotentials", legacy);
    }

    private static int stairMeta(State s) { int m = stairFacingMeta(s.get("facing")); return m | ("top".equals(s.get("half")) ? 4 : 0); }
    private static int stairFacingMeta(String f) { return "east".equals(f)?0:"west".equals(f)?1:"south".equals(f)?2:3; }
    private static int sideMeta(String f) { return "north".equals(f)?2:"south".equals(f)?3:"west".equals(f)?4:"east".equals(f)?5:0; }
    private static int torchMeta(String f) { return "east".equals(f)?1:"west".equals(f)?2:"south".equals(f)?3:"north".equals(f)?4:5; }
    private static int axisMeta(String a) { return "x".equals(a)?4:"z".equals(a)?8:"none".equals(a)?12:0; }
    private static int doorFacingMeta(String f) { return "east".equals(f)?0:"south".equals(f)?1:"west".equals(f)?2:3; }
    private static int doorMeta(State s) { if ("upper".equals(s.get("half"))) return 8 | ("right".equals(s.get("hinge"))?1:0) | ("true".equals(s.get("powered"))?2:0); return doorFacingMeta(s.get("facing")) | ("true".equals(s.get("open"))?4:0); }
    private static int woodMeta(String v) { return "spruce".equals(v)?1:"birch".equals(v)?2:"jungle".equals(v)?3:"acacia".equals(v)?4:"dark_oak".equals(v)?5:0; }
    private static int slabMeta(String v) { return "sandstone".equals(v)?1:"wood_old".equals(v)?2:"cobblestone".equals(v)?3:"brick".equals(v)?4:"stone_brick".equals(v)?5:"nether_brick".equals(v)?6:"quartz".equals(v)?7:0; }
    private static int railMeta(String s) { return "east_west".equals(s)?1:"ascending_east".equals(s)?2:"ascending_west".equals(s)?3:"ascending_north".equals(s)?4:"ascending_south".equals(s)?5:"south_east".equals(s)?6:"south_west".equals(s)?7:"north_west".equals(s)?8:"north_east".equals(s)?9:0; }
    private static int pumpkinMeta(String f) { return "west".equals(f)?1:"north".equals(f)?2:"east".equals(f)?3:0; }
    private static int monsterEggMeta(String v) { return "cobblestone".equals(v)?1:"stone_brick".equals(v)?2:"mossy_brick".equals(v)?3:"cracked_brick".equals(v)?4:"chiseled_brick".equals(v)?5:0; }
    private static int leverMeta(String f) { return "east".equals(f)?1:"west".equals(f)?2:"south".equals(f)?3:"north".equals(f)?4:"up_z".equals(f)?5:"up_x".equals(f)?6:0; }
    private static int stoneMeta(String v) { return "granite".equals(v)?1:"smooth_granite".equals(v)?2:"diorite".equals(v)?3:"smooth_diorite".equals(v)?4:"andesite".equals(v)?5:"smooth_andesite".equals(v)?6:0; }
    private static int colorMeta(String c) { String[] names={"white","orange","magenta","light_blue","yellow","lime","pink","gray","silver","cyan","purple","blue","brown","green","red","black"}; for(int i=0;i<names.length;i++)if(names[i].equals(c))return i;return 0; }
    private static int parse(String v, int fallback) { try { return Integer.parseInt(v); } catch (Exception ignored) { return fallback; } }

    private static boolean isKnown(String name) { for (String value : TEMPLATE_NAMES) if (value.equals(name)) return true; return false; }
    private static NBTTagCompound copy(NBTTagCompound tag) { return (NBTTagCompound) tag.copy(); }
    private static int[] intList(NBTTagList list) { List<NBTBase> raw=rawList(list); int[] v=new int[raw.size()]; for(int i=0;i<v.length;i++)v[i]=((NBTBase.NBTPrimitive)raw.get(i)).func_150287_d(); return v; }
    private static double[] doubleList(NBTTagList list) { List<NBTBase> raw=rawList(list); double[] v=new double[raw.size()]; for(int i=0;i<v.length;i++)v[i]=((NBTBase.NBTPrimitive)raw.get(i)).func_150286_g(); return v; }
    @SuppressWarnings("unchecked") private static List<NBTBase> rawList(NBTTagList list) {
        try { if(tagListField==null) for(Field f:NBTTagList.class.getDeclaredFields()) if(List.class.isAssignableFrom(f.getType())) { f.setAccessible(true); tagListField=f; break; } if(tagListField==null)throw new IllegalStateException("NBTTagList backing field not found"); return (List<NBTBase>)tagListField.get(list); }
        catch(Exception error){throw new IllegalStateException("Cannot inspect NBTTagList",error);}
    }

    public static final class Template {
        public final String name; public final int sizeX,sizeY,sizeZ;
        public final List<State> palette; public final List<BlockEntry> blocks; public final List<EntityEntry> entities;
        Template(String n,int x,int y,int z,List<State> p,List<BlockEntry>b,List<EntityEntry>e){name=n;sizeX=x;sizeY=y;sizeZ=z;palette=Collections.unmodifiableList(new ArrayList<State>(p));blocks=Collections.unmodifiableList(new ArrayList<BlockEntry>(b));entities=Collections.unmodifiableList(new ArrayList<EntityEntry>(e));}
    }
    public static final class State {
        public final String name; public final Map<String,String> properties;
        State(String n,Map<String,String>p){name=n;properties=Collections.unmodifiableMap(new LinkedHashMap<String,String>(p));}
        public String get(String key){return properties.get(key);} public String shortName(){return name.startsWith("minecraft:")?name.substring(10):name;}
        @Override public String toString(){return name+properties;}
    }
    public static final class BlockEntry { public final int x,y,z,state; private final NBTTagCompound nbt; BlockEntry(int x,int y,int z,int s,NBTTagCompound n){this.x=x;this.y=y;this.z=z;state=s;nbt=n;} public NBTTagCompound copyNbt(){return nbt==null?null:copy(nbt);} }
    public static final class EntityEntry { public final double x,y,z; public final int blockX,blockY,blockZ; private final NBTTagCompound nbt; EntityEntry(double x,double y,double z,int bx,int by,int bz,NBTTagCompound n){this.x=x;this.y=y;this.z=z;blockX=bx;blockY=by;blockZ=bz;nbt=n;} public NBTTagCompound copyNbt(){return copy(nbt);} }
    public static final class Point { public final int x,y,z; Point(int x,int y,int z){this.x=x;this.y=y;this.z=z;} }
    private static final class DPoint { final double x,y,z; DPoint(double x,double y,double z){this.x=x;this.y=y;this.z=z;} }
    static final class Descriptor { final String name; final int meta; Descriptor(String n,int m){name=n;meta=m;} }
    private static final class Placed { final Block block; final int meta; Placed(Block b,int m){block=b;meta=m;} }
    public static final class PlacementResult { public final int blocks,blockEntities,markers,entities; PlacementResult(int b,int be,int m,int e){blocks=b;blockEntities=be;markers=m;entities=e;} }
}
