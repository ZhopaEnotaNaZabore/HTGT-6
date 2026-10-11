package com.mod.htgt6.minecraft1202.Module.Events.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.FillBucketEvent;

/** Adds the missing legacy empty-bucket pickup path for the solid powder-snow block. */
public final class Backport120BucketHandler {
    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        if (event.current == null || event.current.getItem() != Items.bucket || event.target == null) return;
        int x = event.target.blockX, y = event.target.blockY, z = event.target.blockZ;
        if (event.world.getBlock(x, y, z) != Backport120Content.powderSnow) return;
        if (!event.world.isRemote) event.world.setBlockToAir(x, y, z);
        event.result = new ItemStack(Backport120Content.powderSnowBucket);
        event.setResult(Event.Result.ALLOW);
    }
}
