package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.mod.htgt6.minecraft1202.Module.Blocks.VillageAndPillage.BlockColoredBed120;

import com.mod.htgt6.minecraft1202.Module.Gameplay.Combat.VillagerCareers112;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Exact Minecraft 1.20.2 acquirable_job_site mapping, adapted to 1.7 block identities. */
public final class VillagePoiType120 {
    public static String forBlock(Block block) {
        if (block == null) return null;
        if (block instanceof IVillagePoiBlock120) return ((IVillagePoiBlock120)block).getPoiType();
        if (block instanceof BlockColoredBed120) return "home";
        if (block == Blocks.brewing_stand) return "cleric";
        if (block == Blocks.cauldron) return "leatherworker";
        if (block == Blocks.bed) return "home";
        return null;
    }

    public static boolean isProfession(String value) {
        return "armorer".equals(value) || "butcher".equals(value) || "cartographer".equals(value)
                || "cleric".equals(value) || "farmer".equals(value) || "fisherman".equals(value)
                || "fletcher".equals(value) || "leatherworker".equals(value)
                || "librarian".equals(value) || "mason".equals(value) || "shepherd".equals(value)
                || "toolsmith".equals(value) || "weaponsmith".equals(value);
    }

    public static boolean isPoi(String value) {
        return isProfession(value) || "meeting".equals(value) || "home".equals(value);
    }

    /** HOME is represented by the head only, matching the single-ticket modern bed POI. */
    public static boolean isHomeHead(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return "home".equals(forBlock(block)) && BlockBed.isBlockHeadOfBed(world.getBlockMetadata(x, y, z));
    }

    public static void applyProfession(net.minecraft.entity.passive.EntityVillager villager, String type) {
        VillagerCareers112.setModernProfession(villager, type);
    }

    private VillagePoiType120() { }
}
