package com.mod.htgt6.common.multiblock.glass;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;

/** In-place migration of loaded chunk storage: no adjacent chunk loads, no tile entities to lose. */
public final class ObservationGlassMigration {
    @SubscribeEvent public void onLoad(ChunkEvent.Load event) {
        if (event.world.isRemote) return;
        boolean[] candidates = new boolean[256];
        for (Block block : ObservationGlassContent.legacy) candidates[Block.getIdFromBlock(block) & 255] = true;
        boolean changed = false;
        for (ExtendedBlockStorage section : event.getChunk().getBlockStorageArray()) {
            if (section == null) continue;
            byte[] ids = section.getBlockLSBArray();
            for (int i=0;i<ids.length;i++) {
                if (!candidates[ids[i] & 255]) continue;
                int x=i & 15, z=(i >> 4) & 15, y=i >> 8;
                Block block = section.getBlockByExtId(x,y,z);
                if (!(block instanceof BlockObservationGlass) || block == ObservationGlassContent.glass) continue;
                section.func_150818_a(x,y,z,ObservationGlassContent.glass);
                changed = true;
            }
        }
        if (changed) event.getChunk().setChunkModified();
    }
}
