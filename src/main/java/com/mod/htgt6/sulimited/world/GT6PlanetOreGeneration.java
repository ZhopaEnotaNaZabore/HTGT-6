package com.mod.htgt6.sulimited.world;

import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792b.blocks.B_database;
import com.mod.htgt6.sulimited.galacticraft.Glise792.planets.Gliese792c.blocks.blocksGliese792C;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.saturn.SaturnMoonBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor.MinorWorldBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.mars.MarsMoonBlocks;
import com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.uranus.UranusMoonBlocks;
import gregapi.code.ItemStackContainer;
import gregapi.data.CS;
import gregapi.worldgen.GT6WorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.world.World;

/** Runs GregTech 6's normal planetary ore tables for SU-Limited dimensions. */
public final class GT6PlanetOreGeneration {
    private static final int GT6_PLANET_DIMENSION_TYPE = 5;

    private GT6PlanetOreGeneration() { }

    /**
     * GT6 only replaces blocks explicitly registered as ore host rocks.  The
     * Gliese terrain uses its own stone blocks, so without these mappings the
     * GT6 generators run but cannot place either normal or small ores.
     */
    public static void registerOreHosts() {
        for (com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology.Profile profile
                : com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.SolSysGeology.Profile.values()) {
            registerOreHost(profile.strata);
        }
        registerOreHost(B_database.Gliese792bBasalt);
        registerOreHost(blocksGliese792C.GlieseCstone);
        registerOreHost(SaturnMoonBlocks.mimasCoreRock);
        registerOreHost(SaturnMoonBlocks.tethysAncientIce);
        registerOreHost(SaturnMoonBlocks.dioneRockyIce);
        registerOreHost(SaturnMoonBlocks.rheaRockyIce);
        registerOreHost(SaturnMoonBlocks.iapetusDarkRock);
        registerOreHost(MarsMoonBlocks.phobosStone);
        registerOreHost(MarsMoonBlocks.deimosCoreRock);
        registerOreHost(UranusMoonBlocks.umbrielCoreRock);
        registerOreHost(UranusMoonBlocks.titaniaCoreRock);
        registerOreHost(UranusMoonBlocks.oberonDarkRock);
        registerOreHost(MinorWorldBlocks.arielCore);
        registerOreHost(MinorWorldBlocks.haumeaCore);
        registerOreHost(MinorWorldBlocks.makemakeCore);
        registerOreHost(MinorWorldBlocks.erisCore);
        registerOreHost(MinorWorldBlocks.deeDeeCore);
    }

    private static void registerOreHost(Block block) {
        if (block == null) return;

        ItemStackContainer source = new ItemStackContainer(block, 1L, 0L);
        CS.BlocksGT.stoneToNormalOres.put(source, CS.BlocksGT.ore);
        CS.BlocksGT.stoneToSmallOres.put(source, CS.BlocksGT.oreSmall);
    }

    public static void generate(World world, int chunkX, int chunkZ) {
        if (world == null || world.isRemote) return;
        GT6WorldGenerator.generate(new GT6WorldGenerator.WorldGenContainer(
                CS.GEN_PLANETS, CS.ORE_PLANETS, GT6_PLANET_DIMENSION_TYPE,
                world, chunkX << 4, chunkZ << 4));
    }
}
