package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

/** Repaints the target after vanilla terrain updates when the map is held near the treasure. */
public final class VanillaTreasureMapHandler120 {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player == null) return;
        ItemStack held = event.player.getHeldItem();
        if (VanillaTreasureMap120.isTreasureMap(held)) {
            VanillaTreasureMap120.refresh(held, event.player.worldObj);
        }
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!VanillaTreasureMap120.isTreasureMap(event.itemStack) || event.toolTip.isEmpty()) return;
        event.toolTip.set(0, StatCollector.translateToLocal("filled_map.buried_treasure"));
    }
}
