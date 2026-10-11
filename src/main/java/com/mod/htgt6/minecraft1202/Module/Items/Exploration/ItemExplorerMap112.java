package com.mod.htgt6.minecraft1202.Module.Items.Exploration;

import com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionGenerator112;

import com.mod.htgt6.minecraft1202.Module.World.Aquatic.VanillaTreasureMap120;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;

/** Filled explorer map whose target is immutable after the trade is created. */
public final class ItemExplorerMap112 extends ItemMap {
    public ItemExplorerMap112() {
        setUnlocalizedName("explorer_map");
        setTextureName("minecraft:map_filled");
    }

    public ItemStack create(World world, String type, int sourceX, int sourceZ) {
        if (world == null || world.isRemote) return null;
        int[] target = "mansion".equals(type)
                ? WoodlandMansionGenerator112.findNearest(world, sourceX, sourceZ, 12)
                : com.mod.htgt6.minecraft1202.Module.World.Bountiful.OceanMonumentGenerator.findNearest(
                        world, sourceX, sourceZ, 100);
        if (target == null) return null;
        int id = world.getUniqueDataId("map");
        ItemStack stack = new ItemStack(this, 1, id);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("TargetKind", type);
        tag.setInteger("TargetX", target[0]); tag.setInteger("TargetZ", target[1]);
        stack.setTagCompound(tag);
        MapData data = new MapData("map_" + id);
        data.scale = 2;
        int mapSize = 128 * (1 << data.scale);
        data.xCenter = Math.floorDiv(target[0] + 64, mapSize) * mapSize + mapSize / 2 - 64;
        data.zCenter = Math.floorDiv(target[1] + 64, mapSize) * mapSize + mapSize / 2 - 64;
        data.dimension = world.provider.dimensionId; data.markDirty();
        world.setItemData("map_" + id, data);
        VanillaTreasureMap120.renderBiomePreview(world, data);
        mark(data, stack);
        return stack;
    }

    @Override public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
        super.onUpdate(stack, world, entity, slot, held);
        if (world.isRemote) return;
        MapData data = getMapData(stack, world);
        if (data != null) mark(data, stack);
    }

    private static void mark(MapData data, ItemStack stack) {
        if (!stack.hasTagCompound()) return;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("TargetX", 99) || !tag.hasKey("TargetZ", 99)) return;
        // Old builds stored the target kind as a string under Decorations.
        // Keep map IDs, terrain and centers intact when upgrading their NBT.
        if (tag.hasKey("Decorations", 8)) {
            tag.setString("TargetKind", tag.getString("Decorations"));
            tag.removeTag("Decorations");
        }
        if (!tag.hasKey("Decorations", 9)) {
            NBTTagCompound decoration = new NBTTagCompound();
            decoration.setString("id", "+");
            // The 1.7 atlas currently has a RED_X adaptation, but no mansion/monument sprites.
            decoration.setByte("type", VanillaTreasureMap120.RED_X_ICON);
            decoration.setDouble("x", tag.getInteger("TargetX"));
            decoration.setDouble("z", tag.getInteger("TargetZ"));
            decoration.setDouble("rot", 180.0D);
            NBTTagList decorations = new NBTTagList();
            decorations.appendTag(decoration);
            tag.setTag("Decorations", decorations);
        }
        int scale = 1 << data.scale;
        int x = Math.round((tag.getInteger("TargetX") - data.xCenter) * 2.0F / scale);
        int z = Math.round((tag.getInteger("TargetZ") - data.zCenter) * 2.0F / scale);
        data.playersVisibleOnMap.put("htgt6:explorer_target", data.new MapCoord(
                VanillaTreasureMap120.RED_X_ICON,
                (byte)Math.max(-128, Math.min(127, x)),
                (byte)Math.max(-128, Math.min(127, z)), (byte)0));
    }

}
