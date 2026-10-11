package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.CaveDensityRuntime120;

import static com.mod.htgt6.minecraft1202.Module.World.Modern.DensityFunctions120.*;

/**
 * Stage-10 terrain graph. Instances are worker-local because their cache nodes are mutable.
 * Input coordinates always use Futura's translated logical Y=0..383 range.
 */
public final class FuturaMountainDensityRouter120 {
    public static final int MIN_Y = 0;
    /** Inclusive translated upper build coordinate: modern 319 + 64. */
    public static final int MAX_Y = 383;
    public static final int MODERN_TO_FUTURA_Y = 64;
    public static final int SEA_LEVEL = 63 + MODERN_TO_FUTURA_Y;

    private final DensityFunction120 continents;
    private final DensityFunction120 erosion;
    private final DensityFunction120 ridges;
    private final DensityFunction120 weirdness;
    private final DensityFunction120 offset;
    private final DensityFunction120 depth;
    private final DensityFunction120 factor;
    private final DensityFunction120 jaggedness;
    private final DensityFunction120 surfaceDetail;
    private final DensityFunction120 initialDensityWithoutJaggedness;
    private final DensityFunction120 nodeDensity;
    private final DensityFunction120 finalDensity;

    public FuturaMountainDensityRouter120(long seed) {
        this(seed,new VanillaNormalNoiseFactory120(seed));
    }

    public FuturaMountainDensityRouter120(NoiseFactory120 noises) {
        this(0L,noises);
    }

    private FuturaMountainDensityRouter120(long seed,NoiseFactory120 noises) {
        if (noises == null) throw new NullPointerException("noises");
        Noise120 shift=noises.create(NoiseParameters120.OFFSET);
        DensityFunction120 shiftX=cache2d(new ShiftNoise120(shift,false));
        DensityFunction120 shiftZ=cache2d(new ShiftNoise120(shift,true));
        continents = cache2d(new ShiftedNoise120(shiftX,shiftZ,noises.create(NoiseParameters120.CONTINENTALNESS)));
        erosion = cache2d(new ShiftedNoise120(shiftX,shiftZ,noises.create(NoiseParameters120.EROSION)));
        ridges = cache2d(new ShiftedNoise120(shiftX,shiftZ,noises.create(NoiseParameters120.RIDGE)));
        weirdness = ridges;
        // NoiseRouterData.peaksAndValleys: -(abs(abs(w) - 2/3) - 1/3) * 3.
        DensityFunction120 folded=cache2d(mul(add(abs(add(abs(ridges),constant(-2.0/3.0))),
                constant(-1.0/3.0)),constant(-3.0)));

        VanillaCubicSpline120.Coordinate continentsCoordinate=coordinate(continents);
        VanillaCubicSpline120.Coordinate erosionCoordinate=coordinate(erosion);
        VanillaCubicSpline120.Coordinate ridgesCoordinate=coordinate(ridges);
        VanillaCubicSpline120.Coordinate foldedCoordinate=coordinate(folded);
        offset=cache2d(add(constant(-0.5037500262260437D),new SplineDensityFunction120(
                VanillaTerrainProvider120.overworldOffset(continentsCoordinate,erosionCoordinate,foldedCoordinate))));
        factor=cache2d(new SplineDensityFunction120(VanillaTerrainProvider120.overworldFactor(
                continentsCoordinate,erosionCoordinate,ridgesCoordinate,foldedCoordinate)));
        jaggedness=cache2d(new SplineDensityFunction120(VanillaTerrainProvider120.overworldJaggedness(
                continentsCoordinate,erosionCoordinate,ridgesCoordinate,foldedCoordinate)));

        // Reference modern interval is [-64,320]; translated Futura interval is [0,384].
        DensityFunction120 verticalFalloff=yGradient(MIN_Y,MAX_Y+1,1.5D,-1.5D);
        depth=cacheOnce(add(verticalFalloff,offset));
        surfaceDetail = cache2d(clamp(noise(noises.create(NoiseParameters120.SURFACE), 0.0625, 0.0), -1.0, 1.0));

        DensityFunction120 jaggedNoise=halfNegative(noise(noises.create(NoiseParameters120.JAGGED),1500.0,0.0));
        DensityFunction120 densityWithJaggedness=add(depth,mul(jaggedness,jaggedNoise));
        DensityFunction120 untransformedInitial=mul(constant(4.0D),quarterNegative(mul(factor,depth)));
        initialDensityWithoutJaggedness=OverworldDensityTransforms120.initialDensityWithoutJaggedness(untransformedInitial);
        DensityFunction120 shaped=mul(constant(4.0D),quarterNegative(mul(factor,densityWithJaggedness)));
        nodeDensity=add(shaped,new BlendedNoiseDensity120(seed));
        finalDensity = cacheOnce(interpolated(nodeDensity, NoiseChunk120.HORIZONTAL_CELL, NoiseChunk120.VERTICAL_CELL));
    }

    public DensityFunction120 continents() { return continents; }
    public DensityFunction120 erosion() { return erosion; }
    public DensityFunction120 ridges() { return ridges; }
    public DensityFunction120 weirdness() { return weirdness; }
    public DensityFunction120 offset(){return offset;}
    public DensityFunction120 depth() { return depth; }
    public DensityFunction120 factor() { return factor; }
    public DensityFunction120 jaggedness() { return jaggedness; }
    public DensityFunction120 surfaceDetail() { return surfaceDetail; }
    public DensityFunction120 finalDensity() { return finalDensity; }
    public DensityFunction120 initialDensityWithoutJaggedness(){return initialDensityWithoutJaggedness;}

    /** Builds one reusable vanilla-cell grid for a complete, 16x16 chunk. */
    public NoiseChunk120 createNoiseChunk(int chunkX,int chunkZ,int height){
        return new NoiseChunk120(nodeDensity,chunkX<<4,MIN_Y,chunkZ<<4,16,height,16);
    }

    /**
     * Builds the interpolated final-density grid with the cave router inside
     * the cell sampler.  This mirrors the 1.20 NoiseChunk execution boundary:
     * expensive cave noises are evaluated at 4x8x4 grid nodes, not once for
     * every output block.
     */
    public CaveNoiseChunk120 createNoiseChunk(int chunkX,int chunkZ,int height,
                                              final CaveDensityRuntime120 caves){
        if(caves==null)throw new NullPointerException("caves");
        DensityFunction120 routedNodes=new DensityFunction120(){
            @Override public double sample(DensityContext120 context){
                double terrainDensity=nodeDensity.sample(context);
                return caves.samplePreInterpolatedDensityCellBoundary(context.blockX(),context.blockY(),
                        context.blockZ(),terrainDensity);
            }
        };
        DensityFunction120 noodleNodes=new DensityFunction120(){
            @Override public double sample(DensityContext120 context){
                return caves.sampleNoodleCellBoundary(context.blockX(),context.blockY(),context.blockZ());
            }
        };
        return new CaveNoiseChunk120(
                new NoiseChunk120(routedNodes,chunkX<<4,MIN_Y,chunkZ<<4,16,height,16),
                new NoiseChunk120(noodleNodes,chunkX<<4,MIN_Y,chunkZ<<4,16,height,16),height);
    }

    /** Preliminary surface lookup used by the vanilla aquifer cell resolver. */
    public int preliminarySurfaceLevel(int blockX, int blockZ) {
        return preliminarySurfaceLevel(blockX,blockZ,MAX_Y,new MutableDensityContext120());
    }

    /** Same cell-boundary search clipped to the physical height exposed by 1.7.10. */
    public int preliminarySurfaceLevel(int blockX,int blockZ,int maxYInclusive) {
        return preliminarySurfaceLevel(blockX,blockZ,maxYInclusive,new MutableDensityContext120());
    }

    public int preliminarySurfaceLevel(int blockX,int blockZ,int maxYInclusive,
                                       MutableDensityContext120 sample) {
        if(maxYInclusive<MIN_Y||maxYInclusive>MAX_Y)throw new IllegalArgumentException(
                "maxYInclusive outside Futura range: "+maxYInclusive);
        if(sample==null)throw new NullPointerException("sample");
        // NoiseChunk.computePreliminarySurfaceLevel samples only cell boundaries
        // and uses the dedicated transformed initial density with threshold 25/64.
        int firstCellBoundary=Math.floorDiv(maxYInclusive+1,NoiseChunk120.VERTICAL_CELL)*NoiseChunk120.VERTICAL_CELL;
        for (int cellY = firstCellBoundary; cellY >= MIN_Y; cellY -= NoiseChunk120.VERTICAL_CELL) {
            if(initialDensityWithoutJaggedness.sample(sample.set(blockX,cellY,blockZ))>0.390625D)return cellY;
        }
        return Integer.MAX_VALUE;
    }

    public static int modernToFuturaY(int modernY) { return modernY + MODERN_TO_FUTURA_Y; }
    public static int futuraToModernY(int futuraY) { return futuraY - MODERN_TO_FUTURA_Y; }
    public static boolean isLogicalY(int futuraY) { return futuraY >= MIN_Y && futuraY <= MAX_Y; }

    private static VanillaCubicSpline120.Coordinate coordinate(final DensityFunction120 function){
        return new VanillaCubicSpline120.Coordinate(){
            @Override public float apply(DensityContext120 context){return (float)function.sample(context);}
        };
    }
}
