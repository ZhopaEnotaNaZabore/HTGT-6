package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.Noise120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.NoiseParameters120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VanillaNormalNoiseFactory120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.PositionalRandomFactory120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.Xoroshiro128PlusPlus120;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.Arrays;

/**
 * Scalar inputs used by the 1.20.2 overworld {@code SurfaceSystem}.
 *
 * <p>This is deliberately not another feature/decorator implementation.  It
 * ports the two column inputs from vanilla's SurfaceSystem verbatim so the
 * later rule graph can consume the same values.</p>
 */
public final class FuturaSurfaceSystem120 {
    // Keep these as primitive selectors. Some 1.7.10 transformer chains fail
    // late-loading a nested enum while a server chunk is being generated.
    public static final int NOISE_SWAMP=0;
    public static final int NOISE_CALCITE=1;
    public static final int NOISE_GRAVEL=2;
    public static final int NOISE_POWDER_SNOW=3;
    public static final int NOISE_PACKED_ICE=4;
    public static final int NOISE_ICE=5;
    private static final int Y_TRANSLATION=64;
    private final Noise120 surfaceNoise;
    private final Noise120 surfaceSecondaryNoise;
    private final Noise120 clayBandsOffsetNoise,badlandsPillarNoise,badlandsPillarRoofNoise,badlandsSurfaceNoise;
    private final Noise120 icebergPillarNoise,icebergPillarRoofNoise,icebergSurfaceNoise;
    private final Noise120 swampNoise,calciteNoise,gravelNoise,powderSnowNoise,packedIceNoise,iceNoise;
    private final PositionalRandomFactory120 positionalRandom;
    private final PositionalRandomFactory120 deepslateRandom;
    private final PositionalRandomFactory120 bedrockRandom;
    private final FuturaSurfaceRules120.State[] clayBands;

    public FuturaSurfaceSystem120(long worldSeed) {
        VanillaNormalNoiseFactory120 noises = new VanillaNormalNoiseFactory120(worldSeed);
        surfaceNoise = noises.create(NoiseParameters120.SURFACE);
        surfaceSecondaryNoise = noises.create(NoiseParameters120.SURFACE_SECONDARY);
        clayBandsOffsetNoise=noises.create(NoiseParameters120.CLAY_BANDS_OFFSET);
        badlandsPillarNoise=noises.create(NoiseParameters120.BADLANDS_PILLAR);
        badlandsPillarRoofNoise=noises.create(NoiseParameters120.BADLANDS_PILLAR_ROOF);
        badlandsSurfaceNoise=noises.create(NoiseParameters120.BADLANDS_SURFACE);
        icebergPillarNoise=noises.create(NoiseParameters120.ICEBERG_PILLAR);
        icebergPillarRoofNoise=noises.create(NoiseParameters120.ICEBERG_PILLAR_ROOF);
        icebergSurfaceNoise=noises.create(NoiseParameters120.ICEBERG_SURFACE);
        swampNoise=noises.create(NoiseParameters120.SURFACE_SWAMP);
        calciteNoise=noises.create(NoiseParameters120.CALCITE);
        gravelNoise=noises.create(NoiseParameters120.GRAVEL);
        powderSnowNoise=noises.create(NoiseParameters120.POWDER_SNOW);
        packedIceNoise=noises.create(NoiseParameters120.PACKED_ICE);
        iceNoise=noises.create(NoiseParameters120.ICE);
        positionalRandom = new PositionalRandomFactory120(worldSeed);
        deepslateRandom = positionalRandom.fork("minecraft:deepslate");
        bedrockRandom = positionalRandom.fork("minecraft:bedrock_floor");
        clayBands=generateBands(positionalRandom.fromHashOf("minecraft:clay_bands"));
    }

    /** Exact SurfaceSystem.surfaceDepth formula from Minecraft 1.20.2. */
    public int surfaceDepth(int blockX, int blockZ) {
        double depth = surfaceNoise.sample(blockX, 0.0D, blockZ) * 2.75D + 3.0D
                + positionalRandom.at(blockX, 0, blockZ).nextDouble() * 0.25D;
        return (int)depth;
    }

    /** Exact SurfaceSystem surface-secondary noise lookup. */
    public double surfaceSecondary(int blockX, int blockZ) {
        return surfaceSecondaryNoise.sample(blockX, 0.0D, blockZ);
    }

    public boolean surfaceNoiseAbove(int x,int z,double threshold){
        return surfaceNoise.sample(x,0.0D,z)>=threshold/8.25D;
    }

    public boolean surfaceNoiseBetween(int x,int z,double minimum,double maximum){
        double value=surfaceNoise.sample(x,0.0D,z);
        return value>=minimum&&value<=maximum;
    }

    public boolean noiseBetween(int type,int x,int z,double minimum,double maximum){
        double value=noise(type).sample(x,0.0D,z);
        return value>=minimum&&value<=maximum;
    }

    public FuturaSurfaceRules120.State clayBand(int x,int y,int z){
        int offset=(int)Math.round(clayBandsOffsetNoise.sample(x,0.0D,z)*4.0D);
        return clayBands[Math.floorMod(y-Y_TRANSLATION+offset,clayBands.length)];
    }

    /** Exact named vertical-gradient probability, with modern anchors translated by +64. */
    public boolean verticalGradient(String name,int x,int y,int z,int modernTrueAtOrBelow,int modernFalseAtOrAbove){
        int low=modernTrueAtOrBelow+Y_TRANSLATION,high=modernFalseAtOrAbove+Y_TRANSLATION;
        if(y<=low)return true;
        if(y>=high)return false;
        double chance=(high-y)/(double)(high-low);
        PositionalRandomFactory120 factory="minecraft:deepslate".equals(name)
                ?deepslateRandom:"minecraft:bedrock_floor".equals(name)?bedrockRandom:positionalRandom.fork(name);
        Xoroshiro128PlusPlus120 random=factory.at(x,y-Y_TRANSLATION,z);
        float sample=random.nextFloat();
        return sample<chance;
    }

    /** SurfaceSystem.erodedBadlandsExtension, translated from modern Y to physical Y. */
    public void erodedBadlandsExtension(Block[] blocks,int base,int x,int z,int surfaceY,int height){
        double pillar=Math.min(Math.abs(badlandsSurfaceNoise.sample(x,0.0D,z)*8.25D),
                badlandsPillarNoise.sample(x*0.2D,0.0D,z*0.2D)*15.0D);
        if(pillar<=0.0D)return;
        double roof=Math.abs(badlandsPillarRoofNoise.sample(x*0.75D,0.0D,z*0.75D)*1.5D);
        int top=floor(128.0D+Math.min(pillar*pillar*2.5D,Math.ceil(roof*50.0D)+24.0D));
        if(surfaceY>top)return;
        for(int y=Math.min(top,height-1);y>=0;y--){
            Block state=blocks[base+y];
            if(state!=null&&state.getMaterial().isSolid())break;
            if(state==Blocks.water||state==Blocks.flowing_water)return;
        }
        for(int y=Math.min(top,height-1);y>=0&&blocks[base+y]==null;y--)blocks[base+y]=Blocks.stone;
    }

    /** SurfaceSystem.frozenOceanExtension with sea level already in physical coordinates. */
    public void frozenOceanExtension(Block[] blocks,byte[] metadata,int base,int x,int z,
                                     int surfaceY,int seaLevel,int height,boolean coldEnoughToSnow){
        double pillar=Math.min(Math.abs(icebergSurfaceNoise.sample(x,0.0D,z)*8.25D),
                icebergPillarNoise.sample(x*1.28D,0.0D,z*1.28D)*15.0D);
        if(pillar<=1.8D)return;
        double roof=Math.abs(icebergPillarRoofNoise.sample(x*1.17D,0.0D,z*1.17D)*1.5D);
        double top=Math.min(pillar*pillar*1.2D,Math.ceil(roof*40.0D)+14.0D);
        if(coldEnoughToSnow)top-=2.0D;
        double bottom;
        if(top>2.0D){bottom=seaLevel-top-7.0D;top+=seaLevel;}else{top=0.0D;bottom=0.0D;}
        Xoroshiro128PlusPlus120 random=positionalRandom.at(x,0,z);
        int snowLimit=2+random.nextInt(4),snowFloor=seaLevel+18+random.nextInt(10),snowCount=0;
        for(int y=Math.min(height-1,(int)top+1);y>=surfaceY;y--){
            Block state=blocks[base+y];
            boolean fillAir=state==null&&y<(int)top&&random.nextDouble()>0.01D;
            boolean fillWater=(state==Blocks.water||state==Blocks.flowing_water||state==Backport120Content.aquiferWater)&&y>(int)bottom
                    &&y<seaLevel&&bottom!=0.0D&&random.nextDouble()>0.15D;
            if(fillAir||fillWater){
                if(snowCount<=snowLimit&&y>snowFloor){blocks[base+y]=Blocks.snow;snowCount++;}
                else blocks[base+y]=Blocks.packed_ice;
                metadata[base+y]=0;
            }
        }
    }

    private Noise120 noise(int type){switch(type){
        case NOISE_SWAMP:return swampNoise;case NOISE_CALCITE:return calciteNoise;case NOISE_GRAVEL:return gravelNoise;
        case NOISE_POWDER_SNOW:return powderSnowNoise;case NOISE_PACKED_ICE:return packedIceNoise;
        case NOISE_ICE:return iceNoise;default:throw new IllegalArgumentException("Unknown material noise: "+type);}}

    private static FuturaSurfaceRules120.State[] generateBands(Xoroshiro128PlusPlus120 random){
        FuturaSurfaceRules120.State terracotta=new FuturaSurfaceRules120.State(Blocks.hardened_clay,0);
        FuturaSurfaceRules120.State[] bands=new FuturaSurfaceRules120.State[192];
        Arrays.fill(bands,terracotta);
        for(int index=0;index<bands.length;index++){
            index+=random.nextInt(5)+1;
            if(index<bands.length)bands[index]=clay(1);
        }
        makeBands(random,bands,1,clay(4));
        makeBands(random,bands,2,clay(12));
        makeBands(random,bands,1,clay(14));
        int whiteCount=9+random.nextInt(7),placed=0;
        for(int index=0;placed<whiteCount&&index<bands.length;index+=random.nextInt(16)+4){
            bands[index]=clay(0);
            if(index-1>0&&random.nextInt()<0)bands[index-1]=clay(8);
            if(index+1<bands.length&&random.nextInt()<0)bands[index+1]=clay(8);
            placed++;
        }
        return bands;
    }

    private static void makeBands(Xoroshiro128PlusPlus120 random,FuturaSurfaceRules120.State[] bands,
                                  int baseLength,FuturaSurfaceRules120.State state){
        int count=6+random.nextInt(10);
        for(int band=0;band<count;band++){
            int length=baseLength+random.nextInt(3),start=random.nextInt(bands.length);
            for(int offset=0;start+offset<bands.length&&offset<length;offset++)bands[start+offset]=state;
        }
    }
    private static FuturaSurfaceRules120.State clay(int metadata){
        return new FuturaSurfaceRules120.State(Blocks.stained_hardened_clay,metadata);}

    /**
     * SurfaceRules.Context preliminary-surface threshold. Vanilla samples the
     * four surrounding 16-block noise cells, bilinearly interpolates them,
     * floors the result, then adds surfaceDepth - 8.
     */
    public int preliminarySurface(int blockX, int blockZ, int northWest, int northEast,
                                  int southWest, int southEast, int surfaceDepth) {
        double xFraction = (double)((float)(blockX & 15) / 16.0F);
        double zFraction = (double)((float)(blockZ & 15) / 16.0F);
        double north = lerp(xFraction, northWest, northEast);
        double south = lerp(xFraction, southWest, southEast);
        return floor(lerp(zFraction, north, south)) + surfaceDepth - 8;
    }

    /** Exact SurfaceRules.steep heightmap comparison for an in-chunk column. */
    public boolean steep(int localX, int localZ, int[] worldSurfaceHeights) {
        int northZ = Math.max(localZ - 1, 0);
        int southZ = Math.min(localZ + 1, 15);
        int north = worldSurfaceHeights[localX + northZ * 16];
        int south = worldSurfaceHeights[localX + southZ * 16];
        if (south >= north + 4) return true;
        int westX = Math.max(localX - 1, 0);
        int eastX = Math.min(localX + 1, 15);
        int west = worldSurfaceHeights[westX + localZ * 16];
        int east = worldSurfaceHeights[eastX + localZ * 16];
        return west >= east + 4;
    }

    private static double lerp(double amount, double first, double second) {
        return first + amount * (second - first);
    }

    private static int floor(double value) {
        int integer = (int)value;
        return value < integer ? integer - 1 : integer;
    }
}
