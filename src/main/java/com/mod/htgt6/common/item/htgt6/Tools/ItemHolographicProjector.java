package com.mod.htgt6.common.item.htgt6.Tools;

import com.mod.htgt6.HTGT6;
import com.mod.htgt6.api.hitech6.multiblock.MultiTileEntityBase;
import com.mod.htgt6.client.nei.structure.HTGT6MultiblockRegistry;
import com.mod.htgt6.client.nei.structure.HTGT6MultiblockStructure;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

/** Client-side construction aid for every HTGT6 multiblock registered in the structure viewer. */
public final class ItemHolographicProjector extends Item {
    public ItemHolographicProjector() {
        setUnlocalizedName("holographic_projector");
        setTextureName("htgt6:holographic_projector");
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.tabTools);
    }

    @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                                       int x, int y, int z, int side,
                                       float hitX, float hitY, float hitZ) {
        if (!(world.getTileEntity(x, y, z) instanceof MultiTileEntityBase)) return false;
        if (player.isSneaking() && player.capabilities.isCreativeMode) {
            if (!world.isRemote) autoBuild(world, x, y, z, player,
                    (MultiTileEntityBase) world.getTileEntity(x, y, z));
            return true;
        }
        if (world.isRemote) HTGT6.proxy.toggleMultiblockHologram(world, x, y, z, player);
        return true;
    }

    private static void autoBuild(World world, int x, int y, int z, EntityPlayer player,
                                  MultiTileEntityBase controller) {
        HTGT6MultiblockStructure structure =
                HTGT6MultiblockRegistry.findByController(world.getBlock(x, y, z));
        if (structure == null) {
            player.addChatMessage(new ChatComponentTranslation("message.htgt6.hologram.unsupported"));
            return;
        }
        int facing = world.getBlockMetadata(x, y, z);
        int placed = 0;
        // Validate first: Creative assembly never destroys an unrelated block or TileEntity.
        for (int sy = 0; sy < structure.getHeight(); sy++) for (int sx = 0; sx < structure.getWidth(); sx++) {
            for (int sz = 0; sz < structure.getDepth(); sz++) {
                HTGT6MultiblockStructure.StructureBlock expected = structure.getBlock(sx, sy, sz);
                int[] p = localToWorld(x, y, z, facing, sx - structure.getControllerX(),
                        sy - structure.getControllerY(), sz - structure.getControllerZ());
                if (p[0] == x && p[1] == y && p[2] == z) continue;
                Block actual = world.getBlock(p[0], p[1], p[2]);
                if (expected == null) {
                    net.minecraft.tileentity.TileEntity internal = world.getTileEntity(p[0], p[1], p[2]);
                    if (controller instanceof com.mod.htgt6.common.crusher.MultiTileIndustrialCrusher
                            && internal instanceof com.mod.htgt6.common.crusher.TileCrusherCollision
                            && ((com.mod.htgt6.common.crusher.TileCrusherCollision) internal).belongsTo(
                                    (com.mod.htgt6.common.crusher.MultiTileIndustrialCrusher) controller)) continue;
                    if (actual != null && !actual.isAir(world, p[0], p[1], p[2])) {
                        player.addChatMessage(new ChatComponentTranslation(
                                "message.htgt6.hologram.autobuild_blocked", p[0], p[1], p[2]));
                        return;
                    }
                    continue;
                }
                if (expected.matches(actual, world.getBlockMetadata(p[0], p[1], p[2]))) continue;
                if (actual != null && !actual.isAir(world, p[0], p[1], p[2])
                        && !actual.isReplaceable(world, p[0], p[1], p[2])) {
                    player.addChatMessage(new ChatComponentTranslation(
                            "message.htgt6.hologram.autobuild_blocked", p[0], p[1], p[2]));
                    return;
                }
            }
        }
        for (int sy = 0; sy < structure.getHeight(); sy++) for (int sx = 0; sx < structure.getWidth(); sx++) {
            for (int sz = 0; sz < structure.getDepth(); sz++) {
                HTGT6MultiblockStructure.StructureBlock expected = structure.getBlock(sx, sy, sz);
                if (expected == null) continue;
                int[] p = localToWorld(x, y, z, facing, sx - structure.getControllerX(),
                        sy - structure.getControllerY(), sz - structure.getControllerZ());
                if (p[0] == x && p[1] == y && p[2] == z) continue;
                if (expected.matches(world.getBlock(p[0], p[1], p[2]),
                        world.getBlockMetadata(p[0], p[1], p[2]))) continue;
                // Client update only; one explicit structure validation follows the bulk placement.
                if (world.setBlock(p[0], p[1], p[2], expected.block, expected.metadata, 2)) placed++;
            }
        }
        controller.checkStructure();
        player.addChatMessage(new ChatComponentTranslation(
                "message.htgt6.hologram.autobuild_complete", placed));
    }

    private static int[] localToWorld(int x, int y, int z, int facing, int u, int dy, int v) {
        if (facing == 3) return new int[] { x - u, y + dy, z - v };
        if (facing == 4) return new int[] { x + v, y + dy, z - u };
        if (facing == 5) return new int[] { x - v, y + dy, z + u };
        return new int[] { x + u, y + dy, z + v };
    }

    @Override @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(StatCollector.translateToLocal("tooltip.htgt6.holographic_projector.use"));
        tooltip.add(StatCollector.translateToLocal("tooltip.htgt6.holographic_projector.toggle"));
        tooltip.add(StatCollector.translateToLocal("tooltip.htgt6.holographic_projector.autobuild"));
    }
}
