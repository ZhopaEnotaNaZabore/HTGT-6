package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;
import net.minecraft.world.biome.WorldChunkManager;
public final class WorldProviderHaumea extends AbstractMinorWorldProvider { protected MinorWorlds.Definition definition(){return MinorWorlds.HAUMEA;} public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerHaumea.class;} }
