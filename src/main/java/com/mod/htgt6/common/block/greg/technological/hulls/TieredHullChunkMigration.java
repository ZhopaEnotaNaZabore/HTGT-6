package com.mod.htgt6.common.block.greg.technological.hulls;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.event.world.ChunkEvent;

/** Attach missing TEs to legacy placed hulls without replacing blocks or loading neighbors. */
public final class TieredHullChunkMigration {
    @SubscribeEvent public void onLoad(ChunkEvent.Load event) {
        if (event.world.isRemote) return;
        Chunk chunk=event.getChunk();
        boolean[] candidates=new boolean[256];
        for (Block b : new Block[] {blockHullDatabase.hullULV,blockHullDatabase.hullLV,
                blockHullDatabase.hullMV,blockHullDatabase.hullHV,blockHullDatabase.HullEV,
                blockHullDatabase.HullIV,blockHullDatabase.HullLUV,blockHullDatabase.HullZPM,
                blockHullDatabase.HullUV,blockHullDatabase.HullPUV,blockHullDatabase.HullMAX}) {
            candidates[Block.getIdFromBlock(b) & 255]=true;
        }
        for (ExtendedBlockStorage section:chunk.getBlockStorageArray()) {
            if (section == null) continue;
            byte[] ids=section.getBlockLSBArray();
            for (int i=0;i<ids.length;i++) {
                if (!candidates[ids[i] & 255]) continue;
                int x=i & 15,z=(i >> 4) & 15,y=i >> 8;
                if (!(section.getBlockByExtId(x,y,z) instanceof BlockTieredHull)) continue;
                int wy=section.getYLocation()+y;
                // Chunk lookup creates a missing TE through Block#createTileEntity.
                chunk.func_150806_e(x,wy,z);
            }
        }
    }
}
