package com.mod.htgt6.minecraft1202.Module.Items.Modern;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.ResourceLocation;

/** Simple modern music disc using a namespaced HTGT6 jukebox sound. */
public final class ItemBackport120Record extends ItemRecord {
    private final String sound;
    public ItemBackport120Record(String name){super(name);sound=name;
        setUnlocalizedName("backport_1_20.music_disc_"+name);
        setTextureName("htgt6:1_20_backport/music_disc_"+name);}
    @Override public ResourceLocation getRecordResource(String ignored){return new ResourceLocation("htgt6","records."+sound);}
}
