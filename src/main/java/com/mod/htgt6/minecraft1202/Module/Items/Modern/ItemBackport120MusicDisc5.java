package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.ResourceLocation;

/** Minecraft 1.20.2 music disc 5 with a namespaced 1.7.10 jukebox sound. */
public final class ItemBackport120MusicDisc5 extends ItemRecord {
    public ItemBackport120MusicDisc5() {
        super("5");
        setUnlocalizedName("backport_1_20.music_disc_5");
        setTextureName("htgt6:1_20_backport/music_disc_5");
    }

    @Override
    public ResourceLocation getRecordResource(String ignored) {
        return new ResourceLocation("htgt6", "records.5");
    }
}
