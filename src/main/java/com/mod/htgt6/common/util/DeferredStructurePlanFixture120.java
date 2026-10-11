package com.mod.htgt6.common.util;

import java.util.HashSet;
import java.util.Set;

/** Idempotence/order fixture for the persisted deferred-replay contract. */
public final class DeferredStructurePlanFixture120 {
    private static final long EXPECTED=0xd8d6d090ffe84223L;

    public static long assertContract(){
        int minX=91,minZ=-37,maxX=119,maxZ=-18;
        Set<Long> loaded=new HashSet<Long>();boolean persisted=false;int generated=0;
        long hash=0xcbf29ce484222325L;
        int[][] order={{7,-2},{5,-3},{6,-2},{5,-2},{7,-3},{6,-3},{6,-2},{5,-2}};
        for(int[] chunk:order){
            loaded.add(DeferredStructurePlan120.key(chunk[0],chunk[1]));
            if(!DeferredStructurePlan120.chunkIntersects(chunk[0],chunk[1],minX,minZ,maxX,maxZ))continue;
            boolean ready=true;
            for(int x=minX>>4;x<=maxX>>4;x++)for(int z=minZ>>4;z<=maxZ>>4;z++)
                ready&=loaded.contains(DeferredStructurePlan120.key(x,z));
            if(ready&&!persisted){persisted=true;generated++;hash=fnv(hash,chunk[0]);hash=fnv(hash,chunk[1]);}
        }
        if(generated!=1)throw new IllegalStateException("Deferred structure generated "+generated+" times");
        if(!DeferredStructurePlan120.chunkIntersectsRadius(6,-2,104,-28,40))
            throw new IllegalStateException("Deferred radius intersection failed");
        hash=fnv(hash,generated);hash=fnv(hash,loaded.size());
        if(EXPECTED!=0L&&hash!=EXPECTED)throw new IllegalStateException("Deferred fixture changed: expected "
                +Long.toHexString(EXPECTED)+", got "+Long.toHexString(hash));
        return hash;
    }
    private static long fnv(long hash,int value){return(hash^value)*0x100000001b3L;}
    public static void main(String[] ignored){System.out.println(Long.toHexString(assertContract()));}
    private DeferredStructurePlanFixture120(){ }
}
