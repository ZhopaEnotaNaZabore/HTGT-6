package com.mod.htgt6.common.achievements;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;

public class craftingachieve {
    @SubscribeEvent
    public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting.getItem() == Item.getItemFromBlock(Blocks.chest)) {
            event.player.triggerAchievement(Hitech6AchieveDatabase.FirstStorage);
        }
    }
}
