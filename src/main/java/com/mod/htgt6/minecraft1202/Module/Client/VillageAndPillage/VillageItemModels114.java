package com.mod.htgt6.minecraft1202.Module.Client.VillageAndPillage;

import com.mod.htgt6.client.model.json.JsonBlockModelLoader;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.*;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

/** Explicit ownership avoids replacing unrelated mods' item renderers. */
public final class VillageItemModels114 {
    private static final JsonBlockModelLoader.TextureResolver TEXTURES=new JsonBlockModelLoader.TextureResolver(){
        public ResourceLocation resolve(String texture){return new ResourceLocation("htgt6","village_items114/"+texture.substring(texture.indexOf(':')+1));}
    };
    private static IItemRenderer bind(Item item,String name){return JsonBlockModelLoader.install().registerItem(item,new ResourceLocation("htgt6","village_items114/item/"+name),TEXTURES);}
    private static void bind(Block block,String name){bind(Item.getItemFromBlock(block),name);}
    public static IItemRenderer village(){
        Block[] blocks={VillagePillageContent.scaffolding,VillagePillageContent.campfire,VillagePillageContent.lantern,VillagePillageContent.blastFurnace,VillagePillageContent.smoker,VillagePillageContent.cartographyTable,VillagePillageContent.composter,VillagePillageContent.barrel,VillagePillageContent.fletchingTable,VillagePillageContent.lectern,VillagePillageContent.stonecutter,VillagePillageContent.loom,VillagePillageContent.smithingTable,VillagePillageContent.grindstone,VillagePillageContent.bell};
        String[] names={"scaffolding","campfire","lantern","blast_furnace","smoker","cartography_table","composter","barrel","fletching_table","lectern","stonecutter","loom","smithing_table","grindstone","bell"};
        for(int i=0;i<blocks.length;i++)bind(blocks[i],names[i]);
        bind(LeatherHorseArmor151.ITEM,"leather_horse_armor");
        Item[] patterns={BannerPatterns145.flower,BannerPatterns145.creeper,BannerPatterns145.skull,BannerPatterns145.mojang,BannerPatterns145.globe};
        String[] patternNames={"flower","creeper","skull","mojang","globe"};
        for(int i=0;i<patterns.length;i++)bind(patterns[i],patternNames[i]+"_banner_pattern");
        return bind(VillagePillageContent.crossbow,"crossbow");
    }
    public static void modern(){
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.sweetBerries,"sweet_berries");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.bamboo,"bamboo");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.cornflower,"cornflower");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.lilyOfTheValley,"lily_of_the_valley");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.witherRose,"wither_rose");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.whiteDye,"white_dye");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.blueDye,"blue_dye");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.brownDye,"brown_dye");
        bind(com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120.blackDye,"black_dye");
    }
    public static void aquatic(){
        bind(com.mod.htgt6.minecraft1202.Module.World.Aquatic.OceanContent.suspiciousStew,"suspicious_stew");
    }
    private VillageItemModels114(){}
}
