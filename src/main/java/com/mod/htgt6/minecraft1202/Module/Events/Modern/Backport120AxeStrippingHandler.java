package com.mod.htgt6.minecraft1202.Module.Events.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.Set;

/** 1.20.2 axe stripping interaction adapted to the metadata-backed 1.7 logs. */
public final class Backport120AxeStrippingHandler {
    public static final class Result {
        public final Block block;
        public final int metadata;
        private Result(Block block, int metadata) {
            this.block = block;
            this.metadata = metadata;
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.entityPlayer == null)
            return;
        EntityPlayer player = event.entityPlayer;
        ItemStack tool = player.getHeldItem();
        if (!isAxe(tool)) return;
        Block source = event.world.getBlock(event.x, event.y, event.z);
        int sourceMeta = event.world.getBlockMetadata(event.x, event.y, event.z);
        Result result = stripped(source, sourceMeta);
        if (result == null || !player.canPlayerEdit(event.x, event.y, event.z,
                event.face, tool)) return;

        // PlayerControllerMP posts this event before it sends the right-click
        // packet. Cancelling the client event therefore prevents the server
        // from ever seeing the stripping interaction. Only the authoritative
        // server event may be cancelled and mutate the block.
        if (!isAuthoritativeSide(event.world.isRemote)) return;
        event.setCanceled(true);
        player.swingItem();
        if (!event.world.setBlock(event.x, event.y, event.z, result.block, result.metadata, 3)) return;
        event.world.playSoundEffect(event.x + 0.5D, event.y + 0.5D, event.z + 0.5D,
                "htgt6:item.axe.strip", 1.0F, 1.0F);
        if (!player.capabilities.isCreativeMode) tool.damageItem(1, player);
    }

    public static boolean isAxe(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        if (stack.getItem() instanceof ItemAxe) return true;
        Set<String> classes = stack.getItem().getToolClasses(stack);
        return classes != null && classes.contains("axe");
    }

    public static boolean isAuthoritativeSide(boolean remote) {
        return !remote;
    }

    /** Pure mapping used by world interaction and by the fixed metadata fixture. */
    public static Result stripped(Block source, int metadata) {
        for(com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.Family family:com.mod.htgt6.minecraft1202.Module.Registry.Modern.NetherWoodContent116.FAMILIES){
            if(source==family.stem)return new Result(family.strippedStem,metadata&12);
            if(source==family.hyphae)return new Result(family.strippedHyphae,metadata&12);
        }
        int species = metadata & 3;
        int axis = metadata & 12;
        if (Blocks.log != null && source == Blocks.log) return strippedVanilla(false, metadata);
        if (Blocks.log2 != null && source == Blocks.log2) return strippedVanilla(true, metadata);
        if (source == Backport120Content.cherryLog)
            return axis == 12
                    ? new Result(Backport120Content.strippedCherryWood, 0)
                    : new Result(Backport120Content.strippedCherryLog, axis);
        if (source == Backport120Content.cherryWood)
            return new Result(Backport120Content.strippedCherryWood, 0);
        if (source == Backport120Content.mangroveLog)
            return axis == 12
                    ? new Result(Backport120Content.strippedMangroveWood, 0)
                    : new Result(Backport120Content.strippedMangroveLog, axis);
        if (source == Backport120Content.mangroveWood)
            return new Result(Backport120Content.strippedMangroveWood, 0);
        return null;
    }

    public static Result strippedVanilla(boolean secondSet, int metadata) {
        int species = metadata & 3;
        int axis = metadata & 12;
        if (secondSet && species >= 2) return null;
        Block log = secondSet ? Backport120Content.strippedLog2 : Backport120Content.strippedLog;
        Block wood = secondSet ? Backport120Content.strippedWood2 : Backport120Content.strippedWood;
        return axis == 12 ? new Result(wood, species) : new Result(log, species | axis);
    }
}
