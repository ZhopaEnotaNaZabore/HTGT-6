package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.concurrent.atomic.AtomicLong;
import java.util.LinkedHashMap;
import java.util.Map;

/** Worker-local terrain heightmap sampler shared by surface structure adapters. */
public final class FuturaSurfaceEstimator120 {
    private static final long EXPECTED_FINGERPRINT=0xeaf6f665db1d382cL;
    private static final ThreadLocal<State> STATES=new ThreadLocal<State>();
    private static final AtomicLong ROUTERS_CREATED=new AtomicLong();

    public static int estimate(long seed,int x,int z,int maxYInclusive) {
        State state=STATES.get();
        if(state==null||state.seed!=seed){
            state=new State(seed);
            STATES.set(state);
            ROUTERS_CREATED.incrementAndGet();
        }
        return state.sampler(maxYInclusive).estimate(x,z);
    }

    public static int estimate(FuturaMountainDensityRouter120 router,int x,int z,int maxYInclusive) {
        if(router==null)throw new NullPointerException("router");
        return new TerrainSurfaceSampler(router,maxYInclusive).estimate(x,z);
    }

    public static int estimate(FuturaMountainDensityRouter120 router,MutableDensityContext120 context,
                               int x,int z,int maxYInclusive) {
        if(router==null)throw new NullPointerException("router");
        if(context==null)throw new NullPointerException("context");
        return new TerrainSurfaceSampler(router,maxYInclusive).estimate(x,z);
    }

    public static TerrainSurfaceSampler sampler(FuturaMountainDensityRouter120 router,int maxYInclusive){
        return new TerrainSurfaceSampler(router,maxYInclusive);
    }

    /**
     * Samples the same interpolated base-terrain density used to build chunks.
     * The preliminary aquifer estimate is intentionally not used: in steep
     * terrain it can resolve to an enclosed cave floor and bury a village.
     */
    public static final class TerrainSurfaceSampler {
        private final FuturaMountainDensityRouter120 router;
        private final int height;
        private final double[] column;
        private final LinkedHashMap<Long,NoiseChunk120> chunks=new LinkedHashMap<Long,NoiseChunk120>(16,.75F,true){
            @Override protected boolean removeEldestEntry(Map.Entry<Long,NoiseChunk120> eldest){return size()>16;}
        };
        private final LinkedHashMap<Long,Integer> heights=new LinkedHashMap<Long,Integer>(4096,.75F,true){
            @Override protected boolean removeEldestEntry(Map.Entry<Long,Integer> eldest){return size()>4096;}
        };

        private TerrainSurfaceSampler(FuturaMountainDensityRouter120 router,int maxYInclusive){
            if(router==null)throw new NullPointerException("router");
            if(maxYInclusive<7||maxYInclusive>FuturaMountainDensityRouter120.MAX_Y
                    ||(maxYInclusive+1)%NoiseChunk120.VERTICAL_CELL!=0)
                throw new IllegalArgumentException("Surface height must contain complete density cells: "+maxYInclusive);
            this.router=router;
            height=maxYInclusive+1;
            column=new double[height];
        }

        /** Equivalent to 1.20's WORLD_SURFACE_WG first-free height. */
        public int estimate(int x,int z){
            long columnKey=((long)x<<32)^(z&0xffffffffL);
            Integer cachedHeight=heights.get(columnKey);
            if(cachedHeight!=null)return cachedHeight;
            int chunkX=Math.floorDiv(x,16),chunkZ=Math.floorDiv(z,16);
            long key=((long)chunkX<<32)^(chunkZ&0xffffffffL);
            NoiseChunk120 noise=chunks.get(key);
            if(noise==null){noise=router.createNoiseChunk(chunkX,chunkZ,height);chunks.put(key,noise);}
            int highestSolid=noise.fillColumn(Math.floorMod(x,16),Math.floorMod(z,16),1,column);
            int firstFree=Math.min(height,highestSolid+1);
            heights.put(columnKey,firstFree);
            return firstFree;
        }
    }

    /** Headless cache and equivalence regression used by startup validation and CI. */
    public static long assertContract() {
        STATES.remove();
        long before=ROUTERS_CREATED.get();
        long hash=0xcbf29ce484222325L;
        long[] seeds={0L,0x1234abcd5678ef90L};
        int[][] points={{0,0},{31,-47},{-257,513},{2048,-4096}};
        for(long seed:seeds){
            FuturaMountainDensityRouter120 reference=new FuturaMountainDensityRouter120(seed);
            for(int[] point:points){
                int cached=estimate(seed,point[0],point[1],255);
                int duplicate=estimate(seed,point[0],point[1],255);
                int bruteForce=bruteForce(reference,point[0],point[1],255);
                if(cached!=duplicate||cached!=bruteForce)throw new IllegalStateException(
                        "Futura structure surface cache mismatch for seed="+seed+" at "
                                +point[0]+","+point[1]);
                hash=(hash^cached)*0x100000001b3L;
            }
        }
        long created=ROUTERS_CREATED.get()-before;
        if(created!=seeds.length)throw new IllegalStateException(
                "Futura structure surface cache created "+created+" routers for "+seeds.length+" seeds");
        STATES.remove();
        if(hash!=EXPECTED_FINGERPRINT)throw new IllegalStateException(
                "Futura structure surface fixture changed: expected "
                        +Long.toHexString(EXPECTED_FINGERPRINT)+", got "+Long.toHexString(hash));
        return hash;
    }

    public static long routersCreated(){return ROUTERS_CREATED.get();}

    private static int bruteForce(FuturaMountainDensityRouter120 router,int x,int z,int maxYInclusive){
        int height=maxYInclusive+1;
        int chunkX=Math.floorDiv(x,16),chunkZ=Math.floorDiv(z,16);
        NoiseChunk120 noise=router.createNoiseChunk(chunkX,chunkZ,height);
        int highestSolid=noise.fillColumn(Math.floorMod(x,16),Math.floorMod(z,16),1,new double[height]);
        return Math.min(height,highestSolid+1);
    }

    public static int[] compareWithPreliminaryForFixture(long seed,int x,int z){
        FuturaMountainDensityRouter120 router=new FuturaMountainDensityRouter120(seed);
        int terrain=new TerrainSurfaceSampler(router,FuturaMountainDensityRouter120.MAX_Y).estimate(x,z);
        int preliminary=router.preliminarySurfaceLevel(x,z,FuturaMountainDensityRouter120.MAX_Y);
        return new int[]{terrain,preliminary};
    }

    public static void main(String[] ignored){
        System.out.println(Long.toHexString(assertContract()));
    }

    private static final class State {
        final long seed;
        final FuturaMountainDensityRouter120 router;
        TerrainSurfaceSampler surface;
        int surfaceMaxY=Integer.MIN_VALUE;
        State(long seed){this.seed=seed;router=new FuturaMountainDensityRouter120(seed);}
        TerrainSurfaceSampler sampler(int maxY){
            if(surface==null||surfaceMaxY!=maxY){surface=new TerrainSurfaceSampler(router,maxY);surfaceMaxY=maxY;}
            return surface;
        }
    }

    private FuturaSurfaceEstimator120(){ }
}
