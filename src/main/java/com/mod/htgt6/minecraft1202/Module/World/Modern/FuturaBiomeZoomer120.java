package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Java 8 port of Minecraft 1.20.2 {@code BiomeManager}'s fiddled-distance
 * block-to-noise-biome lookup. The returned coordinates are quart positions.
 */
public final class FuturaBiomeZoomer120 {
    private static final long MULTIPLIER=6364136223846793005L;
    private static final long INCREMENT=1442695040888963407L;

    public static final class QuartPosition {
        public final int x,y,z;
        QuartPosition(int x,int y,int z){this.x=x;this.y=y;this.z=z;}
    }

    private final long zoomSeed;

    public FuturaBiomeZoomer120(long worldSeed){zoomSeed=obfuscateSeed(worldSeed);}

    public QuartPosition select(int blockX,int blockY,int blockZ){
        int shiftedX=blockX-2,shiftedY=blockY-2,shiftedZ=blockZ-2;
        int baseX=shiftedX>>2,baseY=shiftedY>>2,baseZ=shiftedZ>>2;
        double fractionX=(shiftedX&3)/4.0D;
        double fractionY=(shiftedY&3)/4.0D;
        double fractionZ=(shiftedZ&3)/4.0D;
        int selected=0;
        double closest=Double.POSITIVE_INFINITY;
        for(int corner=0;corner<8;corner++){
            boolean lowX=(corner&4)==0,lowY=(corner&2)==0,lowZ=(corner&1)==0;
            int quartX=lowX?baseX:baseX+1;
            int quartY=lowY?baseY:baseY+1;
            int quartZ=lowZ?baseZ:baseZ+1;
            double deltaX=lowX?fractionX:fractionX-1.0D;
            double deltaY=lowY?fractionY:fractionY-1.0D;
            double deltaZ=lowZ?fractionZ:fractionZ-1.0D;
            double distance=fiddledDistance(zoomSeed,quartX,quartY,quartZ,deltaX,deltaY,deltaZ);
            if(closest>distance){selected=corner;closest=distance;}
        }
        return new QuartPosition((selected&4)==0?baseX:baseX+1,
                (selected&2)==0?baseY:baseY+1,(selected&1)==0?baseZ:baseZ+1);
    }

    public static long obfuscateSeed(long seed){
        try{
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            byte[] input=new byte[8];
            for(int index=0;index<8;index++)input[index]=(byte)(seed>>>(index*8));
            byte[] hash=digest.digest(input);
            long result=0L;
            for(int index=0;index<8;index++)result|=(hash[index]&255L)<<(index*8);
            return result;
        }catch(NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
    }

    private static double fiddledDistance(long seed,int quartX,int quartY,int quartZ,
                                            double deltaX,double deltaY,double deltaZ){
        long mixed=seed;
        mixed=next(mixed,quartX);mixed=next(mixed,quartY);mixed=next(mixed,quartZ);
        mixed=next(mixed,quartX);mixed=next(mixed,quartY);mixed=next(mixed,quartZ);
        double offsetX=fiddle(mixed);
        mixed=next(mixed,seed);
        double offsetY=fiddle(mixed);
        mixed=next(mixed,seed);
        double offsetZ=fiddle(mixed);
        return square(deltaZ+offsetZ)+square(deltaY+offsetY)+square(deltaX+offsetX);
    }

    private static long next(long seed,long salt){return seed*(seed*MULTIPLIER+INCREMENT)+salt;}
    private static double fiddle(long seed){return (Math.floorMod(seed>>24,1024)/1024.0D-0.5D)*0.9D;}
    private static double square(double value){return value*value;}
}
