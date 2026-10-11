package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.CaveDensityRuntime120;

/** Exact two-interpolator final-density seam used by the 1.20.2 overworld router. */
public final class CaveNoiseChunk120 {
    private final NoiseChunk120 terrainAndCaves;
    private final NoiseChunk120 noodle;
    private final double[] noodleColumn;

    public CaveNoiseChunk120(NoiseChunk120 terrainAndCaves,NoiseChunk120 noodle,int height){
        if(terrainAndCaves==null||noodle==null)throw new NullPointerException("noise chunks");
        this.terrainAndCaves=terrainAndCaves;
        this.noodle=noodle;
        this.noodleColumn=new double[height];
    }

    public int fillColumn(int localX,int localZ,int fromY,double[] output){
        terrainAndCaves.fillColumn(localX,localZ,fromY,output);
        noodle.fillColumn(localX,localZ,fromY,noodleColumn);
        int surface=fromY;
        for(int y=fromY;y<output.length;y++){
            output[y]=CaveDensityRuntime120.finishInterpolatedDensity(output[y],noodleColumn[y]);
            if(output[y]>0.0D)surface=y;
        }
        return surface;
    }

    public Statistics statistics(){
        NoiseChunk120.Statistics a=terrainAndCaves.statistics(),b=noodle.statistics();
        return new Statistics(a.evaluatedNodes+b.evaluatedNodes,
                a.interpolatedSamples+b.interpolatedSamples);
    }

    public static final class Statistics{
        public final int evaluatedNodes;
        public final long interpolatedSamples;
        private Statistics(int nodes,long samples){evaluatedNodes=nodes;interpolatedSamples=samples;}
    }
}
