package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.gen.structure.*;

/** Modern placement/height adapters. Piece geometry still uses the legacy component codec. */
public final class FuturaStructureGenerators120 {
    static {
        MapGenStructureIO.registerStructure(MineshaftStart.class,"FuturaMineshaft120");
        MapGenStructureIO.registerStructure(StrongholdStart.class,"FuturaStronghold120");
        MapGenStructureIO.registerStructure(ScatteredStart.class,"FuturaScattered120");
    }
    private static void register(){} // initialize both codecs before saved starts are read
    private FuturaStructureGenerators120(){}
    public static final class Mineshaft extends MapGenMineshaft {
        public Mineshaft(){register();}
        @Override protected boolean canSpawnStructureAtCoords(int x,int z){return FuturaStructurePlacement120.mineshaft(worldObj.getSeed(),x,z);}
        @Override protected StructureStart getStructureStart(int x,int z){
            Random random=WorldgenRandom120.carver(worldObj.getSeed(),0,x,z);
            random.nextDouble(); // MineshaftStructure.findGenerationPoint
            return new MineshaftStart(random,x,z);
        }
    }
    public static final class Stronghold extends MapGenStronghold {
        private final FuturaMountainDensityRouter120 terrain;
        private List<ChunkCoordIntPair> positions;
        public Stronghold(FuturaMountainDensityRouter120 terrain){register();this.terrain=terrain;}
        private List<ChunkCoordIntPair> positions(){
            if(positions==null){
                final FuturaWorldChunkManager120 manager=(FuturaWorldChunkManager120)worldObj.getWorldChunkManager();
                final MutableDensityContext120 sample=new MutableDensityContext120();
                positions=FuturaStructurePlacement120.strongholds(worldObj.getSeed(),(x,z,r)->{
                    int qx=x>>2,qz=z>>2,count=0;ChunkCoordIntPair selected=null;
                    for(int dz=-28;dz<=28;dz++)for(int dx=-28;dx<=28;dx++){
                        int px=(qx+dx)<<2,pz=(qz+dz)<<2;
                        if(!FuturaStructurePlacement120.preferred(manager.noiseBiomeKey(px,64,pz,terrain,sample)))continue;
                        if(selected==null||r.nextInt(count+1)==0)selected=new ChunkCoordIntPair(px>>4,pz>>4);
                        count++;
                    }
                    return selected;
                });
            }
            return positions;
        }
        @Override protected boolean canSpawnStructureAtCoords(int x,int z){
            for(ChunkCoordIntPair p:positions())if(p.chunkXPos==x&&p.chunkZPos==z)return true;
            return false;
        }
        @Override protected List getCoordList(){
            List<ChunkPosition> result=new ArrayList<ChunkPosition>();
            for(ChunkCoordIntPair p:positions())result.add(new ChunkPosition((p.chunkXPos<<4)+8,64,(p.chunkZPos<<4)+8));
            return result;
        }
        @Override protected StructureStart getStructureStart(int x,int z){
            for(int attempt=0;;attempt++){
                StrongholdStart start=new StrongholdStart(WorldgenRandom120.carver(worldObj.getSeed(),attempt,x,z),x,z);
                if(start.portal)return start;
            }
        }
    }
    public static final class Scattered extends MapGenScatteredFeature {
        public Scattered(){register();}
        @Override protected boolean canSpawnStructureAtCoords(int x,int z){
            long seed=worldObj.getSeed();
            boolean desert=FuturaStructurePlacement120.scattered(seed,x,z,"desert_pyramids");
            boolean jungle=FuturaStructurePlacement120.scattered(seed,x,z,"jungle_temples");
            boolean swamp=FuturaStructurePlacement120.scattered(seed,x,z,"swamp_huts");
            if(!desert&&!jungle&&!swamp)return false;
            net.minecraft.world.biome.BiomeGenBase biome=worldObj.getWorldChunkManager().getBiomeGenAt((x<<4)+8,(z<<4)+8);
            return desert&&biome==net.minecraft.world.biome.BiomeGenBase.desert
                    ||jungle&&(biome==net.minecraft.world.biome.BiomeGenBase.jungle||biome.biomeID==FuturaBiomeIds120.BAMBOO_JUNGLE)
                    ||swamp&&biome==net.minecraft.world.biome.BiomeGenBase.swampland;
        }
        @Override protected StructureStart getStructureStart(int x,int z){
            net.minecraft.world.biome.BiomeGenBase biome=worldObj.getWorldChunkManager().getBiomeGenAt((x<<4)+8,(z<<4)+8);
            int type=biome==net.minecraft.world.biome.BiomeGenBase.swampland?2:biome==net.minecraft.world.biome.BiomeGenBase.desert?0:1;
            return new ScatteredStart(WorldgenRandom120.carver(worldObj.getSeed(),0,x,z),x,z,type);
        }
        @Override public boolean func_143030_a(int x,int y,int z){
            StructureStart start=func_143028_c(x,y,z);
            return start!=null&&!start.getComponents().isEmpty()&&start.getComponents().getFirst() instanceof ComponentScatteredFeaturePieces.SwampHut;
        }
    }
    public static class ScatteredStart extends StructureStart {
        public ScatteredStart(){}
        ScatteredStart(Random random,int x,int z,int type){
            super(x,z);
            components.add(type==2?new ComponentScatteredFeaturePieces.SwampHut(random,x<<4,z<<4)
                    :type==1?new ComponentScatteredFeaturePieces.JunglePyramid(random,x<<4,z<<4)
                    :new ComponentScatteredFeaturePieces.DesertPyramid(random,x<<4,z<<4));
            updateBoundingBox();
        }
    }
    public static class MineshaftStart extends StructureStart {
        public MineshaftStart(){}
        MineshaftStart(Random random,int x,int z){
            super(x,z);StructureMineshaftPieces.Room room=new StructureMineshaftPieces.Room(0,random,(x<<4)+2,(z<<4)+2);
            components.add(room);room.buildComponent(room,components,random);updateBoundingBox();move(this,random);
        }
    }
    public static class StrongholdStart extends StructureStart {
        boolean portal;
        public StrongholdStart(){}
        StrongholdStart(Random random,int x,int z){
            super(x,z);StructureStrongholdPieces.prepareStructurePieces();
            StructureStrongholdPieces.Stairs2 stairs=new StructureStrongholdPieces.Stairs2(0,random,(x<<4)+2,(z<<4)+2);
            components.add(stairs);stairs.buildComponent(stairs,components,random);
            while(!stairs.field_75026_c.isEmpty()){
                StructureComponent piece=(StructureComponent)stairs.field_75026_c.remove(random.nextInt(stairs.field_75026_c.size()));
                piece.buildComponent(stairs,components,random);
            }
            updateBoundingBox();move(this,random);portal=stairs.strongholdPortalRoom!=null;
        }
    }
    private static void move(StructureStart start,Random random){
        StructureBoundingBox box=start.getBoundingBox();
        int dy=FuturaStructurePlacement120.undergroundTop(random,box.getYSize())-box.maxY;
        box.offset(0,dy,0);
        for(ListIterator iterator=start.getComponents().listIterator();iterator.hasNext();){
            StructureComponent piece=(StructureComponent)iterator.next();piece.getBoundingBox().offset(0,dy,0);
            if(piece instanceof StructureMineshaftPieces.Room){
                // Room entrances are separate absolute boxes in the legacy codec.
                // Moving only the component box leaves closed doors at the original Y.
                net.minecraft.nbt.NBTTagCompound tag=piece.func_143010_b();
                net.minecraft.nbt.NBTTagList entrances=tag.getTagList("Entrances",11);
                for(int i=0;i<entrances.tagCount();i++){
                    int[] boxValues=entrances.func_150306_c(i);boxValues[1]+=dy;boxValues[4]+=dy;
                }
                iterator.set(MapGenStructureIO.func_143032_b(tag,null));
            }
        }
    }
}
