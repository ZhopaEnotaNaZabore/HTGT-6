package com.mod.htgt6.minecraft1202.Module.Data.Exploration;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Exploration.WoodlandMansionLayout112;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;

/** Headless regression for the complete vanilla 1.12 mansion template set. */
public final class WoodlandMansionTemplateFixture112 {
    private WoodlandMansionTemplateFixture112() { }

    public static void main(String[] ignored) {
        check(WoodlandMansionTemplateRuntime112.TEMPLATE_NAMES.length == 73, "template manifest");
        Set<String> names = new HashSet<String>();
        Set<String> states = new HashSet<String>();
        Set<String> markers = new HashSet<String>();
        Set<String> taggedStates = new HashSet<String>();
        int blockCount = 0, taggedBlocks = 0, entityCount = 0;
        long fingerprint = 0xcbf29ce484222325L;
        for (String name : WoodlandMansionTemplateRuntime112.TEMPLATE_NAMES) {
            check(names.add(name), "duplicate template " + name);
            WoodlandMansionTemplateRuntime112.Template template = WoodlandMansionTemplateRuntime112.load(name);
            check(template == WoodlandMansionTemplateRuntime112.load(name), "cache " + name);
            check(template.sizeX > 0 && template.sizeY > 0 && template.sizeZ > 0, "size " + name);
            blockCount += template.blocks.size(); entityCount += template.entities.size();
            fingerprint = mix(fingerprint, name); fingerprint = mix(fingerprint, template.sizeX);
            fingerprint = mix(fingerprint, template.sizeY); fingerprint = mix(fingerprint, template.sizeZ);
            for (WoodlandMansionTemplateRuntime112.State state : template.palette) {
                states.add(state.toString());
                for (WoodlandMansionLayout112.Mirror mirror : WoodlandMansionLayout112.Mirror.values())
                    for (WoodlandMansionLayout112.Rotation rotation : WoodlandMansionLayout112.Rotation.values()) {
                        WoodlandMansionTemplateRuntime112.State transformed =
                                WoodlandMansionTemplateRuntime112.transform(state, mirror, rotation);
                        check("structure_block".equals(transformed.shortName())
                                || WoodlandMansionTemplateRuntime112.supports(transformed),
                                "unsupported " + transformed + " in " + name);
                    }
            }
            for (WoodlandMansionTemplateRuntime112.BlockEntry block : template.blocks) {
                check(block.state >= 0 && block.state < template.palette.size(), "palette index " + name);
                NBTTagCompound first = block.copyNbt();
                if (first != null) {
                    ++taggedBlocks;
                    NBTTagCompound second = block.copyNbt();
                    check(first != second && first.toString().equals(second.toString()), "NBT copy " + name);
                    WoodlandMansionTemplateRuntime112.State state = template.palette.get(block.state);
                    taggedStates.add(state.shortName());
                    if ("structure_block".equals(state.shortName())) markers.add(first.getString("metadata"));
                }
                for (WoodlandMansionLayout112.Mirror mirror : WoodlandMansionLayout112.Mirror.values())
                    for (WoodlandMansionLayout112.Rotation rotation : WoodlandMansionLayout112.Rotation.values()) {
                        WoodlandMansionTemplateRuntime112.Point point = WoodlandMansionTemplateRuntime112.transform(
                                block.x, block.y, block.z, mirror, rotation);
                        int chunkX = floorDiv(point.x, 16), chunkZ = floorDiv(point.z, 16);
                        check(WoodlandMansionTemplateRuntime112.isInsideChunk(point.x, point.z, chunkX, chunkZ),
                                "clip owner " + name);
                        check(!WoodlandMansionTemplateRuntime112.isInsideChunk(point.x, point.z, chunkX + 1, chunkZ),
                                "clip neighbour " + name);
                    }
                fingerprint = mix(fingerprint, block.x); fingerprint = mix(fingerprint, block.y);
                fingerprint = mix(fingerprint, block.z); fingerprint = mix(fingerprint, block.state);
            }
            for (WoodlandMansionTemplateRuntime112.EntityEntry entity : template.entities) {
                check(entity.copyNbt() != entity.copyNbt(), "entity NBT copy " + name);
            }
        }
        check(states.size() == 185, "palette state inventory " + states.size());
        check(markers.contains("ChestNorth") && markers.contains("ChestSouth") && markers.contains("ChestWest")
                && markers.contains("Mage") && markers.contains("Warrior") && markers.size() == 5,
                "marker inventory " + markers);
        check(taggedStates.contains("chest") && taggedStates.contains("trapped_chest")
                && taggedStates.contains("flower_pot") && taggedStates.contains("mob_spawner")
                && taggedStates.contains("wall_banner") && taggedStates.contains("structure_block")
                && taggedStates.size() == 6, "block NBT inventory " + taggedStates);
        check(entityCount == 0, "upstream 1.12 mansion entity list changed");
        check(blockCount == 66365, "block inventory " + blockCount);
        check(taggedBlocks == 150, "block-entity/marker inventory " + taggedBlocks);
        check(fingerprint == 0xfee3b6d251769679L, "template fingerprint 0x" + Long.toHexString(fingerprint));
        check(WoodlandMansionTemplateRuntime112.isInsideChunk(0, 0, 0, 0), "chunk lower edge");
        check(WoodlandMansionTemplateRuntime112.isInsideChunk(15.999, 15.999, 0, 0), "chunk upper edge");
        check(!WoodlandMansionTemplateRuntime112.isInsideChunk(16, 0, 0, 0), "chunk x clip");
        check(WoodlandMansionTemplateRuntime112.isInsideChunk(-0.1, -0.1, -1, -1), "negative chunk clip");
        WoodlandMansionTemplateRuntime112.Point p = WoodlandMansionTemplateRuntime112.transform(2, 3, 5,
                WoodlandMansionLayout112.Mirror.FRONT_BACK,
                WoodlandMansionLayout112.Rotation.CLOCKWISE_90);
        check(p.x == -5 && p.y == 3 && p.z == -2, "mirror/rotation order");
        NBTTagCompound spawner = new NBTTagCompound(), spawnData = new NBTTagCompound();
        spawnData.setString("id", "minecraft:spider"); spawner.setTag("SpawnData", spawnData);
        net.minecraft.nbt.NBTTagList potentials = new net.minecraft.nbt.NBTTagList();
        NBTTagCompound potential = new NBTTagCompound(), potentialEntity = new NBTTagCompound();
        potential.setInteger("Weight", 3); potentialEntity.setString("id", "minecraft:cave_spider");
        potentialEntity.setBoolean("Custom", true); potential.setTag("Entity", potentialEntity);
        potentials.appendTag(potential); spawner.setTag("SpawnPotentials", potentials);
        WoodlandMansionTemplateRuntime112.adaptSpawner112To17(spawner);
        check("Spider".equals(spawner.getString("EntityId")), "spawner selected entity");
        NBTTagCompound legacyPotential = spawner.getTagList("SpawnPotentials", 10).getCompoundTagAt(0);
        check("CaveSpider".equals(legacyPotential.getString("Type"))
                && legacyPotential.getInteger("Weight") == 3
                && legacyPotential.getCompoundTag("Properties").getBoolean("Custom"), "spawner potentials");
        System.out.println("mansion templates=73 blocks=" + blockCount + " tagged=" + taggedBlocks
                + " states=" + states.size() + " entities=" + entityCount
                + " fingerprint=0x" + Long.toHexString(fingerprint));
    }

    private static long mix(long hash, String value) {
        for (int i = 0; i < value.length(); ++i) hash = mix(hash, value.charAt(i));
        return mix(hash, 255);
    }
    private static long mix(long hash, int value) {
        hash ^= value & 255; hash *= 0x100000001b3L;
        hash ^= value >>> 8 & 255; hash *= 0x100000001b3L;
        hash ^= value >>> 16 & 255; hash *= 0x100000001b3L;
        hash ^= value >>> 24 & 255; return hash * 0x100000001b3L;
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static int floorDiv(int value, int divisor) {
        int result = value / divisor;
        return value < 0 && value % divisor != 0 ? result - 1 : result;
    }
}
