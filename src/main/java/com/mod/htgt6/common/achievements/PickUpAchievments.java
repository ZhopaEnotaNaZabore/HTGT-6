package com.mod.htgt6.common.achievements;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;


public class PickUpAchievments {

    @SubscribeEvent
    public void PickUp(final PlayerEvent.ItemPickupEvent event) {
        if (event.pickedUp.getEntityItem().isItemEqual(new ItemStack(Blocks.dirt))) {
            event.player.addStat((StatBase)Hitech6AchieveDatabase.GetStarted, 1);
        }
        if (event.pickedUp.getEntityItem().isItemEqual((new ItemStack(Items.flint)))
        ) {event.player.addStat((StatBase)Hitech6AchieveDatabase.Flint, 2);

        }

            }
        }

