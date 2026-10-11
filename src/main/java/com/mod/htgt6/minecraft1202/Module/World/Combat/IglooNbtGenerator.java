package com.mod.htgt6.minecraft1202.Module.World.Combat;

import com.mod.htgt6.minecraft1202.Module.Entity.Combat.EntityCombatZombieVillager;
import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import com.mod.htgt6.minecraft1202.Module.Data.Combat.IglooStructureData120;

import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.common.util.StructurePlacement120;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/** Places the three original Minecraft 1.10 igloo structure templates on the 1.7 world API. */
public final class IglooNbtGenerator {
    private static final String TOP = "igloo_top";
    private static final String MIDDLE = "igloo_middle";
    private static final String BOTTOM = "igloo_bottom";
    private static final Map<String, Template> CACHE = new HashMap<String, Template>();
    private static Field tagListField;

    public static void validateTemplates() {
        load(TOP);
        load(MIDDLE);
        load(BOTTOM);
    }

    public static void generate(int chunkX, int chunkZ, World world) {
        if (!isCandidate(world, chunkX, chunkZ)) return;
        int anchorX = (chunkX << 4) + 8;
        int anchorZ = (chunkZ << 4) + 8;
        int surface = world.getTopSolidOrLiquidBlock(anchorX, anchorZ);
        if (surface < 10 || !isIglooBiome(world.getBiomeGenForCoords(anchorX, anchorZ))) return;

        // IWorldGenerator may be invoked again for an already-populated chunk by
        // loaders and regeneration tools. A real persistent structure-start index,
        // rather than inspecting blocks, makes that operation idempotent.
        if (!IglooStructureData120.get(world).claim(chunkX,chunkZ)) return;
        Random random=structureRandom(world.getSeed(),chunkX,chunkZ);

        int rotation = random.nextInt(4);
        int baseY = surface - 1;
        place(world, random, load(TOP), anchorX, baseY, anchorZ, rotation, 0, 0, 0);

        if (random.nextDouble() < 0.5D) {
            int depth = random.nextInt(8) + 4;
            for (int section = 0; section < depth; ++section) {
                place(world, random, load(MIDDLE), anchorX, baseY, anchorZ, rotation,
                        2, -3 - section * 3, 4);
            }
            place(world, random, load(BOTTOM), anchorX, baseY, anchorZ, rotation,
                    0, -6 - depth * 3, -2);
        } else {
            int[] entrance = transform(0, 1, rotation);
            world.setBlock(anchorX + entrance[0], baseY,
                    anchorZ + entrance[1], Blocks.snow, 0, 2);
        }
    }

    private static Random structureRandom(long seed,int chunkX,int chunkZ){
        int regionX=StructurePlacement120.floorDiv(chunkX,StructurePlacement120.IGLOO.spacing);
        int regionZ=StructurePlacement120.floorDiv(chunkZ,StructurePlacement120.IGLOO.spacing);
        return StructurePlacement120.randomAfterCandidate(seed,regionX,regionZ,StructurePlacement120.IGLOO);
    }

    /** Stable plan contract used by headless tests and migration diagnostics. */
    public static int planFingerprint(long seed,int chunkX,int chunkZ){
        Random random=structureRandom(seed,chunkX,chunkZ);int rotation=random.nextInt(4);
        boolean basement=random.nextDouble()<.5D;int depth=basement?random.nextInt(8)+4:0;
        return rotation|(basement?4:0)|(depth<<3);
    }

    private static boolean isCandidate(World world, int chunkX, int chunkZ) {
        return StructurePlacement120.isCandidate(world.getSeed(),chunkX,chunkZ,
                StructurePlacement120.IGLOO);
    }

    /** Minecraft 1.20.2 #has_structure/igloo: snowy_taiga, snowy_plains, snowy_slopes. */
    private static boolean isIglooBiome(BiomeGenBase biome) {
        return biome == BiomeGenBase.icePlains
                || biome == BiomeGenBase.coldTaiga
                || biome == BiomeGenBase.coldTaigaHills
                || biome != null && biome.biomeID
                == com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.SNOWY_SLOPES;
    }

    private static void place(World world, Random random, Template template, int anchorX, int baseY,
            int anchorZ, int rotation, int offsetX, int offsetY, int offsetZ) {
        List<Marker> markers = new ArrayList<Marker>();
        for (BlockEntry entry : template.blocks) {
            if (entry.state < 0 || entry.state >= template.palette.length) continue;
            State state = template.palette[entry.state];
            int localX = offsetX + entry.x - 3;
            int localZ = offsetZ + entry.z - 4;
            int[] point = transform(localX, localZ, rotation);
            int x = anchorX + point[0];
            int y = baseY + offsetY + entry.y;
            int z = anchorZ + point[1];
            if (y < 1 || y >= world.getActualHeight()) continue;

            if ("minecraft:structure_block".equals(state.name)) {
                String marker = entry.tag == null ? null : entry.tag.getString("metadata");
                if (marker != null && !marker.isEmpty()) markers.add(new Marker(marker, x, y, z));
                continue;
            }
            Placed placed = map(state, rotation);
            if (placed == null) continue;
            world.setBlock(x, y, z, placed.block, placed.meta, 2);
            applyBlockData(world, state, entry.tag, x, y, z);
        }

        for (Marker marker : markers) {
            world.setBlockToAir(marker.x, marker.y, marker.z);
            if ("chest".equals(marker.name)) fillChest(world, random, marker.x, marker.y - 1, marker.z);
        }
        for (EntityEntry entry : template.entities) spawnEntity(world, entry, anchorX, baseY,
                anchorZ, rotation, offsetX, offsetY, offsetZ);
    }

    private static Placed map(State state, int rotation) {
        String name = state.name.startsWith("minecraft:") ? state.name.substring(10) : state.name;
        if ("air".equals(name)) return new Placed(Blocks.air, 0);
        if ("snow".equals(name)) return new Placed(Blocks.snow, 0);
        if ("bed".equals(name)) {
            int meta = bedMeta(rotateFacing(state.get("facing"), rotation));
            if ("head".equals(state.get("part"))) meta |= 8;
            return new Placed(Blocks.bed, meta);
        }
        if ("brewing_stand".equals(name)) return new Placed(Blocks.brewing_stand, 0);
        if ("carpet".equals(name)) return new Placed(Blocks.carpet, colorMeta(state.get("color")));
        if ("cauldron".equals(name)) return new Placed(Blocks.cauldron, parseInt(state.get("level"), 0));
        if ("chest".equals(name)) return new Placed(Blocks.chest, horizontalMeta(rotateFacing(state.get("facing"), rotation)));
        if ("crafting_table".equals(name)) return new Placed(Blocks.crafting_table, 0);
        if ("flower_pot".equals(name)) return new Placed(Blocks.flower_pot, 0);
        if ("furnace".equals(name)) return new Placed(Blocks.furnace, horizontalMeta(rotateFacing(state.get("facing"), rotation)));
        if ("ice".equals(name)) return new Placed(Blocks.ice, 0);
        if ("iron_bars".equals(name)) return new Placed(Blocks.iron_bars, 0);
        if ("ladder".equals(name)) return new Placed(Blocks.ladder, horizontalMeta(rotateFacing(state.get("facing"), rotation)));
        if ("monster_egg".equals(name)) return new Placed(Blocks.monster_egg, monsterEggMeta(state.get("variant")));
        if ("redstone_torch".equals(name)) return new Placed(Blocks.redstone_torch, torchMeta(rotateFacing(state.get("facing"), rotation)));
        if ("spruce_stairs".equals(name)) {
            int meta = stairMeta(rotateFacing(state.get("facing"), rotation));
            if ("top".equals(state.get("half"))) meta |= 4;
            return new Placed(Blocks.spruce_stairs, meta);
        }
        if ("stone".equals(name)) {
            return "smooth_andesite".equals(state.get("variant"))
                    ? new Placed(BountifulContent.stone, 5) : new Placed(Blocks.stone, 0);
        }
        if ("stonebrick".equals(name)) return new Placed(Blocks.stonebrick, stoneBrickMeta(state.get("variant")));
        if ("torch".equals(name)) return new Placed(Blocks.torch, torchMeta(rotateFacing(state.get("facing"), rotation)));
        if ("trapdoor".equals(name)) {
            int meta = trapdoorMeta(rotateFacing(state.get("facing"), rotation));
            if ("true".equals(state.get("open"))) meta |= 4;
            if ("top".equals(state.get("half"))) meta |= 8;
            return new Placed(Blocks.trapdoor, meta);
        }
        if ("wall_sign".equals(name)) return new Placed(Blocks.wall_sign, horizontalMeta(rotateFacing(state.get("facing"), rotation)));
        if ("web".equals(name)) return new Placed(Blocks.web, 0);
        if ("wooden_slab".equals(name)) return new Placed(Blocks.wooden_slab,
                1 | ("top".equals(state.get("half")) ? 8 : 0));
        return null;
    }

    private static void applyBlockData(World world, State state, NBTTagCompound tag, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityFlowerPot) {
            ((TileEntityFlowerPot) tile).func_145964_a(Item.getItemFromBlock(Blocks.cactus), 0);
            tile.markDirty();
        } else if (tile instanceof TileEntitySign) {
            TileEntitySign sign = (TileEntitySign) tile;
            sign.signText[0] = "";
            sign.signText[1] = "<----";
            sign.signText[2] = "---->";
            sign.signText[3] = "";
            sign.markDirty();
        } else if ("minecraft:brewing_stand".equals(state.name) && tile instanceof IInventory) {
            ((IInventory) tile).setInventorySlotContents(1,
                    new ItemStack(Items.potionitem, 1, 16392));
            tile.markDirty();
        }
    }

    private static void fillChest(World world, Random random, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof IInventory)) return;
        IInventory inventory = (IInventory) tile;
        putRandomEmpty(inventory, random, new ItemStack(Items.golden_apple));
        Item[] loot = {Items.apple, Items.coal, Items.gold_nugget, Items.stone_axe,
                Items.rotten_flesh, Items.emerald, Items.wheat};
        int count = 2 + random.nextInt(4);
        for (int i = 0; i < count; ++i) {
            Item item = loot[random.nextInt(loot.length)];
            int size = item == Items.wheat || item == Items.coal || item == Items.rotten_flesh
                    ? 1 + random.nextInt(4) : 1;
            putRandomEmpty(inventory, random, new ItemStack(item, size));
        }
        tile.markDirty();
    }

    private static void putRandomEmpty(IInventory inventory, Random random, ItemStack stack) {
        int start = random.nextInt(inventory.getSizeInventory());
        for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            int slot = (start + i) % inventory.getSizeInventory();
            if (inventory.getStackInSlot(slot) == null) {
                inventory.setInventorySlotContents(slot, stack);
                return;
            }
        }
    }

    private static void spawnEntity(World world, EntityEntry entry, int anchorX, int baseY,
            int anchorZ, int rotation, int offsetX, int offsetY, int offsetZ) {
        Entity entity;
        if ("minecraft:villager".equals(entry.id)) {
            EntityVillager villager = new EntityVillager(world);
            villager.setProfession(entry.profession);
            VillagerCareers112.restoreCareer(villager, entry.career, entry.careerLevel, entry.offers);
            villager.func_110163_bv();
            entity = villager;
        } else if ("minecraft:zombie_villager".equals(entry.id)) {
            EntityCombatZombieVillager zombie = new EntityCombatZombieVillager(world);
            zombie.setProfession(entry.profession);
            zombie.restoreConversion(entry.conversionTime, entry.conversionPlayer);
            zombie.func_110163_bv();
            entity = zombie;
        } else return;
        double localX = offsetX + entry.x - 3.0D;
        double localZ = offsetZ + entry.z - 4.0D;
        double[] point = transform(localX, localZ, rotation);
        entity.setLocationAndAngles(anchorX + point[0], baseY + offsetY + entry.y,
                anchorZ + point[1], rotation * 90.0F, 0.0F);
        world.spawnEntityInWorld(entity);
    }

    private static synchronized Template load(String name) {
        Template cached = CACHE.get(name);
        if (cached != null) return cached;
        String path = "/assets/htgt6/structures/igloo/" + name + ".nbt";
        InputStream input = IglooNbtGenerator.class.getResourceAsStream(path);
        if (input == null) throw new IllegalStateException("Missing igloo template: " + path);
        try {
            NBTTagCompound root = CompressedStreamTools.readCompressed(input);
            NBTTagList paletteTag = root.getTagList("palette", 10);
            State[] palette = new State[paletteTag.tagCount()];
            for (int i = 0; i < palette.length; ++i) {
                NBTTagCompound stateTag = paletteTag.getCompoundTagAt(i);
                Map<String, String> properties = new HashMap<String, String>();
                if (stateTag.hasKey("Properties", 10)) {
                    NBTTagCompound propertyTag = stateTag.getCompoundTag("Properties");
                    for (Object key : propertyTag.func_150296_c()) {
                        String value = String.valueOf(key);
                        properties.put(value, propertyTag.getString(value));
                    }
                }
                palette[i] = new State(stateTag.getString("Name"), properties);
            }
            List<BlockEntry> blocks = new ArrayList<BlockEntry>();
            java.util.Set<String> occupied=new java.util.HashSet<String>();
            int[] size=intList(root.getTagList("size",3));
            NBTTagList blockTag = root.getTagList("blocks", 10);
            for (int i = 0; i < blockTag.tagCount(); ++i) {
                NBTTagCompound entry = blockTag.getCompoundTagAt(i);
                int[] pos = intList(entry.getTagList("pos", 3));
                if(pos.length!=3 || size.length!=3 || pos[0]<0 || pos[1]<0 || pos[2]<0
                        || pos[0]>=size[0] || pos[1]>=size[1] || pos[2]>=size[2])
                    throw new IllegalStateException("Invalid igloo block position in "+name);
                if(!occupied.add(pos[0]+","+pos[1]+","+pos[2]))
                    throw new IllegalStateException("Duplicate igloo block position in "+name+": "+java.util.Arrays.toString(pos));
                if(entry.getInteger("state")<0 || entry.getInteger("state")>=palette.length)
                    throw new IllegalStateException("Invalid igloo palette index in "+name);
                blocks.add(new BlockEntry(pos[0], pos[1], pos[2], entry.getInteger("state"),
                        entry.hasKey("nbt", 10) ? entry.getCompoundTag("nbt") : null));
            }
            List<EntityEntry> entities = new ArrayList<EntityEntry>();
            java.util.Set<String> entityPositions=new java.util.HashSet<String>();
            NBTTagList entityTag = root.getTagList("entities", 10);
            for (int i = 0; i < entityTag.tagCount(); ++i) {
                NBTTagCompound entry = entityTag.getCompoundTagAt(i);
                double[] pos = doubleList(entry.getTagList("pos", 6));
                NBTTagCompound data = entry.getCompoundTag("nbt");
                if(pos.length!=3 || !entityPositions.add(data.getString("id")+java.util.Arrays.toString(pos)))
                    throw new IllegalStateException("Duplicate or invalid igloo entity in "+name);
                int profession = VillagerCareers112.professionId(
                        data.getString("ProfessionName"), data.getInteger("Profession"));
                UUID conversionPlayer = null;
                if (data.hasKey("ConversionPlayerMost", 99)
                        && data.hasKey("ConversionPlayerLeast", 99)) {
                    conversionPlayer = new UUID(data.getLong("ConversionPlayerMost"),
                            data.getLong("ConversionPlayerLeast"));
                }
                entities.add(new EntityEntry(data.getString("id"), pos[0], pos[1], pos[2],
                        profession, data.getInteger("Career"), data.getInteger("CareerLevel"),
                        data.hasKey("ConversionTime", 99) ? data.getInteger("ConversionTime") : -1,
                        conversionPlayer, data.hasKey("Offers", 10)
                                ? data.getCompoundTag("Offers") : null));
            }
            Template template = new Template(palette, blocks, entities);
            CACHE.put(name, template);
            return template;
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read igloo template: " + path, error);
        } finally {
            try { input.close(); } catch (IOException ignored) { }
        }
    }

    private static int[] intList(NBTTagList list) {
        List<NBTBase> values = rawList(list);
        int[] result = new int[values.size()];
        for (int i = 0; i < result.length; ++i)
            result[i] = ((NBTBase.NBTPrimitive) values.get(i)).func_150287_d();
        return result;
    }

    private static double[] doubleList(NBTTagList list) {
        List<NBTBase> values = rawList(list);
        double[] result = new double[values.size()];
        for (int i = 0; i < result.length; ++i)
            result[i] = ((NBTBase.NBTPrimitive) values.get(i)).func_150286_g();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<NBTBase> rawList(NBTTagList list) {
        try {
            if (tagListField == null) for (Field field : NBTTagList.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    tagListField = field;
                    break;
                }
            }
            if (tagListField == null) throw new IllegalStateException("NBTTagList backing field not found");
            return (List<NBTBase>) tagListField.get(list);
        } catch (Exception error) {
            throw new IllegalStateException("Cannot access igloo NBT list", error);
        }
    }

    private static int[] transform(int x, int z, int rotation) {
        switch (rotation & 3) {
            case 1: return new int[]{-z, x};
            case 2: return new int[]{-x, -z};
            case 3: return new int[]{z, -x};
            default: return new int[]{x, z};
        }
    }

    private static double[] transform(double x, double z, int rotation) {
        switch (rotation & 3) {
            case 1: return new double[]{-z, x};
            case 2: return new double[]{-x, -z};
            case 3: return new double[]{z, -x};
            default: return new double[]{x, z};
        }
    }

    private static String rotateFacing(String facing, int rotation) {
        String[] values = {"north", "east", "south", "west"};
        for (int i = 0; i < values.length; ++i)
            if (values[i].equals(facing)) return values[(i + rotation) & 3];
        return facing;
    }

    private static int horizontalMeta(String facing) {
        return "south".equals(facing) ? 3 : "west".equals(facing) ? 4 : "east".equals(facing) ? 5 : 2;
    }

    private static int stairMeta(String facing) {
        return "east".equals(facing) ? 0 : "west".equals(facing) ? 1 : "south".equals(facing) ? 2 : 3;
    }

    private static int bedMeta(String facing) {
        return "south".equals(facing) ? 0 : "west".equals(facing) ? 1 : "north".equals(facing) ? 2 : 3;
    }

    private static int torchMeta(String facing) {
        return "east".equals(facing) ? 1 : "west".equals(facing) ? 2
                : "south".equals(facing) ? 3 : "north".equals(facing) ? 4 : 5;
    }

    private static int trapdoorMeta(String facing) {
        return "south".equals(facing) ? 0 : "north".equals(facing) ? 1
                : "east".equals(facing) ? 2 : 3;
    }

    private static int colorMeta(String color) {
        return "red".equals(color) ? 14 : "silver".equals(color) ? 8 : 0;
    }

    private static int monsterEggMeta(String variant) {
        return "mossy_brick".equals(variant) ? 3 : "chiseled_brick".equals(variant) ? 5 : 2;
    }

    private static int stoneBrickMeta(String variant) {
        return "mossy_stonebrick".equals(variant) ? 1 : "cracked_stonebrick".equals(variant) ? 2
                : "chiseled_stonebrick".equals(variant) ? 3 : 0;
    }

    private static int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); } catch (Exception ignored) { return fallback; }
    }

    private static final class Template {
        final State[] palette;
        final List<BlockEntry> blocks;
        final List<EntityEntry> entities;
        Template(State[] palette, List<BlockEntry> blocks, List<EntityEntry> entities) {
            this.palette = palette;
            this.blocks = blocks;
            this.entities = entities;
        }
    }

    private static final class State {
        final String name;
        final Map<String, String> properties;
        State(String name, Map<String, String> properties) { this.name = name; this.properties = properties; }
        String get(String key) { return properties.get(key); }
    }

    private static final class BlockEntry {
        final int x, y, z, state;
        final NBTTagCompound tag;
        BlockEntry(int x, int y, int z, int state, NBTTagCompound tag) {
            this.x = x; this.y = y; this.z = z; this.state = state; this.tag = tag;
        }
    }

    private static final class EntityEntry {
        final String id;
        final double x, y, z;
        final int profession, career, careerLevel, conversionTime;
        final UUID conversionPlayer;
        final NBTTagCompound offers;
        EntityEntry(String id, double x, double y, double z, int profession,
                int career, int careerLevel, int conversionTime, UUID conversionPlayer,
                NBTTagCompound offers) {
            this.id = id; this.x = x; this.y = y; this.z = z; this.profession = profession;
            this.career = career; this.careerLevel = careerLevel;
            this.conversionTime = conversionTime; this.conversionPlayer = conversionPlayer;
            this.offers = offers;
        }
    }

    private static final class Placed {
        final Block block;
        final int meta;
        Placed(Block block, int meta) { this.block = block; this.meta = meta; }
    }

    private static final class Marker {
        final String name;
        final int x, y, z;
        Marker(String name, int x, int y, int z) { this.name = name; this.x = x; this.y = y; this.z = z; }
    }

    private IglooNbtGenerator() { }
}
