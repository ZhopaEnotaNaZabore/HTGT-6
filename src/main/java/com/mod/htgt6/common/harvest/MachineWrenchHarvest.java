package com.mod.htgt6.common.harvest;

import com.mod.htgt6.api.hitech6.BaseMachines.BlockMachine;
import com.mod.htgt6.common.block.greg.technological.hulls.blockHullStatbase;
import com.mod.htgt6.common.block.hitech6.*;
import com.mod.htgt6.common.centrifuge.*;
import com.mod.htgt6.common.chemical.*;
import com.mod.htgt6.common.crusher.*;
import com.mod.htgt6.common.electronics.*;
import com.mod.htgt6.common.mining.*;
import com.mod.htgt6.common.mixer.*;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import gregapi.item.multiitem.MultiItemTool;
import gregapi.item.multiitem.tools.IToolStats;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;

/** Shared client prediction and authoritative server rule for dismantling HiTech6 machines. */
public final class MachineWrenchHarvest {
    private static boolean installed;

    public static boolean requiresWrench(Block block) {
        return block instanceof blockHullStatbase || block instanceof BlockMachine
                || block instanceof BlockAssembler || block instanceof BlockUniversalEngine
                || block instanceof BlockUniversalGasTurbine || block instanceof BlockRadioisotopeGenerator
                || block instanceof BlockMagicEnergyGenerator || block instanceof BlockFluidSolidifier
                || block instanceof BlockSuperMasicCompressor || block instanceof BlockSuperMassiveCompressorCasing
                || block instanceof BlockChemicalPlantController || block instanceof BlockChemicalPlantCasing
                || block instanceof BlockElectronicFactoryController || block instanceof BlockElectronicFactoryCasing
                || block instanceof BlockIndustrialCrusherController || block instanceof BlockIndustrialCrusherCasing
                || block instanceof BlockIndustrialMixerController || block instanceof BlockIndustrialMixerCasing
                || block instanceof com.mod.htgt6.common.flotation.BlockFlotationChamberController
                || block instanceof com.mod.htgt6.common.flotation.BlockFlotationChamberCasing
                || block instanceof BlockIndustrialCentrifugeController || block instanceof BlockIndustrialCentrifugeCasing
                || block instanceof BlockMiningUnitController || block instanceof BlockMiningUnitCasing
                || block instanceof BlockMiningUnitFrame
                || block instanceof com.mod.htgt6.common.multiblock.BlockMultiblockTrim
                || block instanceof com.mod.htgt6.common.multiblock.gateway.BlockMultiblockGateway
                || block instanceof com.mod.htgt6.common.TE.BlockFluidSolidifierCasing
                || block instanceof com.mod.htgt6.common.turbine.BlockSteamTurbineController;
    }

    public static void install() {
        if (installed) return;
        int count = 0;
        for (Object entry : Block.blockRegistry) {
            Block block = (Block) entry;
            if (!requiresWrench(block)) continue;
            // No material tier gate: a usable GT wrench of any tier is sufficient.
            block.setHarvestLevel("wrench", 0);
            count++;
        }
        MinecraftForge.EVENT_BUS.register(new MachineWrenchHarvest());
        installed = true;
        org.apache.logging.log4j.LogManager.getLogger("HTGT6-Harvest")
                .info("Registered GT wrench harvesting for {} machine/casing blocks", count);
    }

    public static boolean isUsableWrench(ItemStack stack, Block block, int metadata) {
        if (stack == null || stack.stackSize <= 0 || !(stack.getItem() instanceof MultiItemTool)) return false;
        MultiItemTool tool = (MultiItemTool) stack.getItem();
        IToolStats stats = tool.getToolStats(stack);
        return stats != null && stats.isWrench() && tool.getDigSpeed(stack, block, metadata) > 0;
    }

    private static boolean allowed(EntityPlayer player, Block block, int metadata) {
        return player != null && (player.capabilities.isCreativeMode
                || isUsableWrench(player.getCurrentEquippedItem(), block, metadata));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (requiresWrench(event.block) && !allowed(event.entityPlayer, event.block, event.metadata))
            event.newSpeed = 0;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (requiresWrench(event.block)) event.success = allowed(event.entityPlayer, event.block, 0);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBreak(BlockEvent.BreakEvent event) {
        if (requiresWrench(event.block) && !allowed(event.getPlayer(), event.block, event.blockMetadata))
            event.setCanceled(true);
    }
}
