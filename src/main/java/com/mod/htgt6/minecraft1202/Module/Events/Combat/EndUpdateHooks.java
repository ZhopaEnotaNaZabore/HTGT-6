package com.mod.htgt6.minecraft1202.Module.Events.Combat;

import com.mod.htgt6.minecraft1202.Module.World.Combat.ChunkProviderCombatEnd;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

public final class EndUpdateHooks {
    public static IChunkProvider createChunkProvider(WorldProvider provider) {
        if (provider == null || provider.worldObj == null) throw new IllegalStateException("End provider has no world");
        return new ChunkProviderCombatEnd(provider.worldObj, provider.worldObj.getSeed());
    }
    private EndUpdateHooks() { }
}
