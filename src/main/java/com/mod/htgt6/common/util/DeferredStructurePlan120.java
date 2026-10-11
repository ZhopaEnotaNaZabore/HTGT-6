package com.mod.htgt6.common.util;

/** Pure chunk-footprint helpers for replaying deferred structure starts without loading neighbours. */
public final class DeferredStructurePlan120 {
    public static boolean chunkIntersects(int chunkX,int chunkZ,int minX,int minZ,int maxX,int maxZ){
        int chunkMinX=chunkX<<4,chunkMinZ=chunkZ<<4;
        return maxX>=chunkMinX&&minX<=chunkMinX+15&&maxZ>=chunkMinZ&&minZ<=chunkMinZ+15;
    }

    public static boolean chunkIntersectsRadius(int chunkX,int chunkZ,int centerX,int centerZ,int radius){
        return chunkIntersects(chunkX,chunkZ,centerX-radius,centerZ-radius,centerX+radius,centerZ+radius);
    }

    public static long key(int chunkX,int chunkZ){return((long)chunkX<<32)^(chunkZ&0xffffffffL);}
    private DeferredStructurePlan120(){ }
}
