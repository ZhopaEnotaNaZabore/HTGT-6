package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;
import net.minecraft.world.biome.WorldChunkManager;
public final class WorldProviderAriel extends AbstractMinorWorldProvider { protected MinorWorlds.Definition definition(){return MinorWorlds.ARIEL;} public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerAriel.class;} }
