package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;
import net.minecraft.world.biome.WorldChunkManager;
public final class WorldProviderMakemake extends AbstractMinorWorldProvider { protected MinorWorlds.Definition definition(){return MinorWorlds.MAKEMAKE;} public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerMakemake.class;} }
