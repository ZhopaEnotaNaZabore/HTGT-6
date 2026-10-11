package com.mod.htgt6.sulimited.galacticraft.SolSysExpanded.minor;
import net.minecraft.world.biome.WorldChunkManager;
public final class WorldProviderDeeDee extends AbstractMinorWorldProvider { protected MinorWorlds.Definition definition(){return MinorWorlds.DEEDEE;} public Class<? extends WorldChunkManager> getWorldChunkManagerClass(){return WorldChunkManagerDeeDee.class;} }
