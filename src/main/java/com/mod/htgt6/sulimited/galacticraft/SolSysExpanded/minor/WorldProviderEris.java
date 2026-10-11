package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;
import net.minecraft.world.biome.WorldChunkManager;
public final class WorldProviderEris extends AbstractMinorWorldProvider { protected MinorWorlds.Definition definition(){return MinorWorlds.ERIS;} public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerEris.class;} }
