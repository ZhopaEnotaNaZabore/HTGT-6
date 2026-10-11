package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Data.Aquatic.AquaticStructureData;
import net.minecraft.block.material.MapColor;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.common.BiomeDictionary;

import java.util.Random;

/** 1.20.2 buried-treasure exploration-map semantics on the vanilla 1.7 map item. */
public final class VanillaTreasureMap120 {
    public static final byte MAP_SCALE = 1;
    public static final byte RED_X_ICON = 7;
    private static final String RED_X_KEY = "htgt6:buried_treasure";
    private static final String TAG_MARKER = "HTGT6BuriedTreasureMap";
    private static final String TAG_SCROLL = "HTGT6BuriedTreasureScroll";

    public static ItemStack create(World world, int sourceX, int sourceZ) {
        int[] target = AquaticStructureData.findTarget(world, AquaticStructureData.BURIED_TREASURE,
                sourceX, sourceZ);
        // ExplorationMapFunction returns its input map unchanged when locateStructure fails.
        if (target == null) return new ItemStack(Items.map);
        // Project adaptation: keep the exploration result sealed until the
        // player opens it. The resulting filled map follows the 1.20.2 map
        // scale, target decoration and biome-preview rules below.
        ItemStack scroll = new ItemStack(OceanContent.treasureMapScroll);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(TAG_SCROLL, true);
        tag.setString("TargetKind", AquaticStructureData.BURIED_TREASURE);
        tag.setInteger("TargetX", target[0]);
        tag.setInteger("TargetZ", target[1]);
        tag.setInteger("SourceX", sourceX);
        tag.setInteger("SourceZ", sourceZ);
        tag.setInteger("TargetDimension",world.provider.dimensionId);
        scroll.setTagCompound(tag);
        return scroll;
    }

    /** Opens the sealed loot into the real vanilla ItemMap/MapData implementation. */
    public static ItemStack open(ItemStack scroll, World world) {
        if (!isSealedScroll(scroll) || world == null || world.isRemote) return scroll;
        NBTTagCompound tag = scroll.getTagCompound();
        int dimension=targetDimension(scroll);
        World targetWorld=world.provider.dimensionId==dimension?world:net.minecraftforge.common.DimensionManager.getWorld(dimension);
        if(targetWorld==null)return scroll;
        return createAt(targetWorld, tag.getInteger("SourceX"), tag.getInteger("SourceZ"),
                tag.getInteger("TargetX"), tag.getInteger("TargetZ"));
    }
    /** Legacy scrolls originated in the Overworld and have no dimension tag. */
    public static int targetDimension(ItemStack scroll) {
        return scroll.hasTagCompound()?scroll.getTagCompound().getInteger("TargetDimension"):0;
    }

    public static boolean isSealedScroll(ItemStack stack) {
        return stack != null && stack.getItem() == OceanContent.treasureMapScroll
                && stack.hasTagCompound() && stack.getTagCompound().getBoolean(TAG_SCROLL)
                && AquaticStructureData.BURIED_TREASURE.equals(
                stack.getTagCompound().getString("TargetKind"));
    }

    private static ItemStack createAt(World world, int sourceX, int sourceZ, int targetX, int targetZ) {
        int mapId = world.getUniqueDataId("map");
        ItemStack stack = new ItemStack(Items.filled_map, 1, mapId);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(TAG_MARKER, true);
        tag.setString("TargetKind", AquaticStructureData.BURIED_TREASURE);
        tag.setInteger("TargetX", targetX);
        tag.setInteger("TargetZ", targetZ);
        tag.setInteger("SourceX", sourceX);
        tag.setInteger("SourceZ", sourceZ);
        tag.setBoolean("TargetResolved", true);
        // Canonical MapItemSavedData target decoration written by
        // MapItem.addTargetDecoration in Java 1.20.2.
        NBTTagCompound decoration = new NBTTagCompound();
        decoration.setString("id", "+");
        decoration.setByte("type", RED_X_ICON);
        decoration.setDouble("x", targetX);
        decoration.setDouble("z", targetZ);
        decoration.setDouble("rot", 180.0D);
        NBTTagList decorations = new NBTTagList();
        decorations.appendTag(decoration);
        tag.setTag("Decorations", decorations);
        stack.setTagCompound(tag);
        String dataName = "map_" + mapId;
        MapData data = new MapData(dataName);
        int mapSize = 128 * (1 << MAP_SCALE);
        data.xCenter = Math.floorDiv(targetX + 64, mapSize) * mapSize + mapSize / 2 - 64;
        data.zCenter = Math.floorDiv(targetZ + 64, mapSize) * mapSize + mapSize / 2 - 64;
        data.scale = MAP_SCALE;
        data.dimension = world.provider.dimensionId;
        data.markDirty();
        world.setItemData(dataName, data);
        renderBiomePreview(world, data);
        refresh(stack, world);
        return stack;
    }

    public static boolean isTreasureMap(ItemStack stack) {
        return stack != null && stack.getItem() == Items.filled_map && stack.hasTagCompound()
                && stack.getTagCompound().getBoolean(TAG_MARKER)
                && AquaticStructureData.BURIED_TREASURE.equals(
                stack.getTagCompound().getString("TargetKind"));
    }

    /** Keeps the RED_X adaptation intact while retaining vanilla ItemMap packets/rendering. */
    public static void refresh(ItemStack stack, World world) {
        if (!isTreasureMap(stack) || world == null) return;
        MapData data = ((ItemMap)Items.filled_map).getMapData(stack, world);
        if (data == null) return;
        NBTTagCompound tag = stack.getTagCompound();
        if (!world.isRemote) {
            addRedXDecoration(data, tag);
        }
    }

    public static void putInInventory(IInventory inventory, ItemStack map, Random random) {
        if (inventory == null || map == null) return;
        int start = random.nextInt(Math.max(1, inventory.getSizeInventory()));
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            int slot = (start + i) % inventory.getSizeInventory();
            if (inventory.getStackInSlot(slot) == null) {
                inventory.setInventorySlotContents(slot, map);
                inventory.markDirty();
                return;
            }
        }
    }

    private static void addRedXDecoration(MapData data, NBTTagCompound tag) {
        int scale = 1 << data.scale;
        int markerX = Math.round((tag.getInteger("TargetX") - data.xCenter) * 2.0F / scale);
        int markerZ = Math.round((tag.getInteger("TargetZ") - data.zCenter) * 2.0F / scale);
        markerX = Math.max(-128, Math.min(127, markerX));
        markerZ = Math.max(-128, Math.min(127, markerZ));
        data.playersVisibleOnMap.put(RED_X_KEY, data.new MapCoord(
                RED_X_ICON, (byte)markerX, (byte)markerZ, (byte)0));
    }

    /** Direct port of MapItem.renderBiomePreviewMap from the 1.20.2 reference. */
    public static void renderBiomePreview(World world, MapData data) {
        int scale = 1 << data.scale;
        int baseX = data.xCenter / scale - 64;
        int baseZ = data.zCenter / scale - 64;
        boolean[] watery = new boolean[128 * 128];
        for (int z = 0; z < 128; z++) {
            for (int x = 0; x < 128; x++) {
                BiomeGenBase biome = world.getWorldChunkManager().getBiomeGenAt(
                        (baseX + x) * scale, (baseZ + z) * scale);
                watery[z * 128 + x] = waterOnMap(biome);
            }
        }

        for (int z = 1; z < 127; z++) {
            for (int x = 1; x < 127; x++) {
                int neighbors = 0;
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if ((dx != 0 || dz != 0) && watery[(z + dz) * 128 + x + dx]) neighbors++;
                    }
                }

                MapColor color = MapColor.airColor;
                int brightness = 3;
                if (watery[z * 128 + x]) {
                    color = MapColor.adobeColor;
                    if (neighbors > 7 && (z & 1) == 0) {
                        int pattern = Math.floorMod((x + (int)(Math.sin(z) * 7.0D)) / 8, 5);
                        brightness = pattern == 2 ? 2 : (pattern == 1 || pattern == 3 ? 1 : 0);
                    } else if (neighbors > 7) {
                        color = MapColor.airColor;
                    } else if (neighbors > 5) {
                        brightness = 1;
                    } else if (neighbors > 1) {
                        brightness = 0;
                    }
                } else if (neighbors > 0) {
                    color = MapColor.brownColor;
                    brightness = neighbors > 3 ? 1 : 3;
                }
                if (color != MapColor.airColor) {
                    data.colors[z * 128 + x] = (byte)(color.colorIndex * 4 + brightness);
                }
            }
        }
        data.markDirty();
    }

    public static boolean waterOnMap(BiomeGenBase biome) {
        return BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.OCEAN)
                ||BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.RIVER)
                ||BiomeDictionary.isBiomeOfType(biome,BiomeDictionary.Type.SWAMP);
    }
    private VanillaTreasureMap120() { }
}
