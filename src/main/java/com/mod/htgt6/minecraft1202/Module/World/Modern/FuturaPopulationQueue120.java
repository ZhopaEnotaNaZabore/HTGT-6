package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.common.util.WorldSafety;
import java.util.*;
import net.minecraft.world.World;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.*;

/** Defers reentrant server population without expanding the generation frontier when draining. */
public final class FuturaPopulationQueue120 {
    private boolean active,draining;
    private final SortedSet<Long> pending=new TreeSet<Long>();
    public boolean begin(World world,int x,int z){
        if(!active){active=true;return true;}
        Chunk chunk=WorldSafety.getLoadedChunk(world,x,z);
        if(chunk==null)throw new IllegalStateException("Population before chunk residency");
        // ChunkProviderServer sets this flag before invoking the generator.
        // Save terrain-only neighbours as unpopulated so reload can retry them.
        chunk.isTerrainPopulated=false;chunk.setChunkModified();
        pending.add(ChunkCoordIntPair.chunkXZ2Int(x,z));return false;
    }
    public void end(){active=false;}
    public void drain(World world,IChunkProvider server){
        if(draining)return;draining=true;
        try{
            boolean progressed;
            do{progressed=false;
                for(Long key:new ArrayList<Long>(pending)){
                    int x=(int)(long)key,z=(int)(key>>32);
                    Chunk chunk=WorldSafety.getLoadedChunk(world,x,z);
                    if(chunk==null||chunk.isTerrainPopulated){pending.remove(key);continue;}
                    boolean ready=true;
                    for(int dx=-FuturaGenerationRegion120.READ_RADIUS;dx<=FuturaGenerationRegion120.READ_RADIUS&&ready;dx++)
                        for(int dz=-FuturaGenerationRegion120.READ_RADIUS;dz<=FuturaGenerationRegion120.READ_RADIUS;dz++)
                            if(!world.getChunkProvider().chunkExists(x+dx,z+dz)){ready=false;break;}
                    if(!ready)continue;
                    pending.remove(key);server.populate(server,x,z);progressed=true;
                }
            }while(progressed);
        }finally{draining=false;}
    }
}
