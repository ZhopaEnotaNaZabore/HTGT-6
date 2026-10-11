package com.mod.htgt6.minecraft1202.Module.World.Modern;

/**
 * Worker-local batch sampler matching the 1.20 NoiseChunk cell walk.
 * It evaluates every shared density-grid node once, then serves block samples
 * through the same trilinear operation order as {@link DensityFunctions120#interpolated}.
 */
public final class NoiseChunk120 {
    public static final int HORIZONTAL_CELL=4;
    public static final int VERTICAL_CELL=8;

    private final int minBlockX, minBlockY, minBlockZ;
    private final int width, height, depth;
    private final int gridX, gridY, gridZ;
    private final double[] nodes;
    private long interpolatedSamples;

    public NoiseChunk120(DensityFunction120 nodeDensity, int minBlockX, int minBlockY, int minBlockZ,
                  int width, int height, int depth) {
        if(nodeDensity==null)throw new NullPointerException("nodeDensity");
        if(width<=0||height<=0||depth<=0||width%HORIZONTAL_CELL!=0
                ||height%VERTICAL_CELL!=0||depth%HORIZONTAL_CELL!=0)
            throw new IllegalArgumentException("NoiseChunk dimensions must contain complete cells");
        if(Math.floorMod(minBlockX,HORIZONTAL_CELL)!=0
                ||Math.floorMod(minBlockY,VERTICAL_CELL)!=0
                ||Math.floorMod(minBlockZ,HORIZONTAL_CELL)!=0)
            throw new IllegalArgumentException("NoiseChunk origin must be cell-aligned");
        this.minBlockX=minBlockX;this.minBlockY=minBlockY;this.minBlockZ=minBlockZ;
        this.width=width;this.height=height;this.depth=depth;
        gridX=width/HORIZONTAL_CELL+1;
        gridY=height/VERTICAL_CELL+1;
        gridZ=depth/HORIZONTAL_CELL+1;
        nodes=new double[gridX*gridY*gridZ];
        MutableDensityContext120 context=new MutableDensityContext120();
        for(int gx=0;gx<gridX;gx++)for(int gz=0;gz<gridZ;gz++)for(int gy=0;gy<gridY;gy++){
            int x=minBlockX+gx*HORIZONTAL_CELL;
            int y=minBlockY+gy*VERTICAL_CELL;
            int z=minBlockZ+gz*HORIZONTAL_CELL;
            nodes[nodeIndex(gx,gy,gz)]=nodeDensity.sample(context.set(x,y,z));
        }
    }

    /** Fills relative Y indices [fromY,height), returning the highest positive-density Y. */
    public int fillColumn(int localX,int localZ,int fromY,double[] output){
        if(localX<0||localX>=width||localZ<0||localZ>=depth)
            throw new IllegalArgumentException("Column outside NoiseChunk: "+localX+","+localZ);
        if(fromY<0||fromY>=height||output==null||output.length<height)
            throw new IllegalArgumentException("Invalid density-column target");
        int surface=fromY;
        for(int localY=fromY;localY<height;localY++){
            double density=sampleLocal(localX,localY,localZ);
            output[localY]=density;
            if(density>0.0D)surface=localY;
        }
        return surface;
    }

    public double sampleLocal(int localX,int localY,int localZ){
        if(localX<0||localX>=width||localY<0||localY>=height||localZ<0||localZ>=depth)
            throw new IllegalArgumentException("Position outside NoiseChunk");
        int cellX=localX/HORIZONTAL_CELL,cellY=localY/VERTICAL_CELL,cellZ=localZ/HORIZONTAL_CELL;
        double tx=(localX%HORIZONTAL_CELL)/(double)HORIZONTAL_CELL;
        double ty=(localY%VERTICAL_CELL)/(double)VERTICAL_CELL;
        double tz=(localZ%HORIZONTAL_CELL)/(double)HORIZONTAL_CELL;
        double n000=node(cellX,cellY,cellZ),n001=node(cellX,cellY,cellZ+1);
        double n010=node(cellX,cellY+1,cellZ),n011=node(cellX,cellY+1,cellZ+1);
        double n100=node(cellX+1,cellY,cellZ),n101=node(cellX+1,cellY,cellZ+1);
        double n110=node(cellX+1,cellY+1,cellZ),n111=node(cellX+1,cellY+1,cellZ+1);
        double x00=lerp(tx,n000,n100),x01=lerp(tx,n001,n101);
        double x10=lerp(tx,n010,n110),x11=lerp(tx,n011,n111);
        interpolatedSamples++;
        return lerp(ty,lerp(tz,x00,x01),lerp(tz,x10,x11));
    }

    public Statistics statistics(){return new Statistics(nodes.length,interpolatedSamples);}

    private int nodeIndex(int gx,int gy,int gz){return (gx*gridZ+gz)*gridY+gy;}
    private double node(int gx,int gy,int gz){return nodes[nodeIndex(gx,gy,gz)];}
    private static double lerp(double t,double a,double b){return a+t*(b-a);}

    public static final class Statistics {
        public final int evaluatedNodes;
        public final long interpolatedSamples;
        private Statistics(int evaluatedNodes,long interpolatedSamples){
            this.evaluatedNodes=evaluatedNodes;this.interpolatedSamples=interpolatedSamples;
        }
        public long reusedSamples(){return Math.max(0L,interpolatedSamples-evaluatedNodes);}
    }
}
