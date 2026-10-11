package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.common.util.*;
import com.mod.htgt6.minecraft1202.Module.Data.Modern.TrailRuinsData120;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.*;
import net.minecraftforge.event.world.ChunkEvent;

/** Official 1.20.2 Trail Ruins jigsaw with persistent chunk-slice replay. */
public final class TrailRuinsGenerator120 implements IWorldGenerator{
    public static final String START_POOL="minecraft:trail_ruins/tower";public static final int START_HEIGHT=-15,SIZE=7,MAX_DISTANCE=80;
    private static final OfficialJigsawRegistry120 REGISTRY=new OfficialJigsawRegistry120("trail_ruins",0);
    @Override public void generate(Random ignored,int cx,int cz,World world,IChunkProvider generator,IChunkProvider provider){if(world.isRemote||world.provider.dimensionId!=0||!world.getWorldInfo().isMapFeaturesEnabled())return;
        TrailRuinsData120 data=TrailRuinsData120.get(world);TrailRuinsData120.Start start=data.start(cx,cz);
        if(start==null&&StructurePlacement120.isCandidate(world.getSeed(),cx,cz,StructurePlacement120.TRAIL_RUINS)){
            Chunk chunk=WorldSafety.getLoadedChunk(world,cx,cz);int centerX=(cx<<4)+7,centerZ=(cz<<4)+7;
            if(chunk!=null&&supports(world.getBiomeGenForCoords(centerX,centerZ))){int surface=chunk.getHeightValue(7,7);if(surface>=24&&surface<world.getActualHeight()-16)start=data.record(cx,cz,surface,layoutSeed(world.getSeed(),cx,cz));}}
        replay(world,data,cx,cz);if(start!=null)backfill(world,data,start,cx,cz);
    }
    @SubscribeEvent public void chunkLoad(ChunkEvent.Load event){World world=event.world;if(world==null||world.isRemote||world.provider.dimensionId!=0||!event.getChunk().isTerrainPopulated)return;replay(world,TrailRuinsData120.get(world),event.getChunk().xPosition,event.getChunk().zPosition);}
    private static void replay(World world,TrailRuinsData120 data,int tx,int tz){for(TrailRuinsData120.Start start:data.near(tx,tz,6)){if(data.complete(start,tx,tz))continue;JigsawPlacementRuntime120.Result layout=layout(start);if(!intersects(layout,tx,tz))continue;
        for(JigsawPlacementRuntime120.Piece piece:layout.pieces)if(intersects(piece.box(),tx,tz))place(world,piece,tx,tz);data.markComplete(start,tx,tz);}}
    private static void backfill(World world,TrailRuinsData120 data,TrailRuinsData120.Start start,int activeX,int activeZ){for(ChunkSlice slice:slices(layout(start))){if(slice.chunkX==activeX&&slice.chunkZ==activeZ)continue;Chunk loaded=WorldSafety.getLoadedChunk(world,slice.chunkX,slice.chunkZ);if(loaded!=null&&loaded.isTerrainPopulated)replay(world,data,slice.chunkX,slice.chunkZ);}}
    private static void place(World world,JigsawPlacementRuntime120.Piece piece,int cx,int cz){placeElement(world,piece.element,piece.x,piece.y,piece.z,piece.rotation,cx,cz);}
    private static void placeElement(World world,JigsawPlacementRuntime120.Element e,int x,int y,int z,int rotation,int cx,int cz){if(e.kind==JigsawPlacementRuntime120.ElementKind.EMPTY||e.kind==JigsawPlacementRuntime120.ElementKind.FEATURE)return;if(e.kind==JigsawPlacementRuntime120.ElementKind.LIST){for(JigsawPlacementRuntime120.Element child:e.children)placeElement(world,child,x,y,z,rotation,cx,cz);return;}
        FuturaAncientCityGenerator120.placeTemplateAtOrigin(world,OfficialJigsawRegistry120.templateResource(e.template),x,y,z,rotation,cx,cz,e.processors,e.ignoreTemplateAir);}
    public static JigsawPlacementRuntime120.Result layout(long worldSeed,int cx,int cz,int surface){return layoutFromSeed(layoutSeed(worldSeed,cx,cz),cx,cz,surface);}
    public static JigsawPlacementRuntime120.Result layout(TrailRuinsData120.Start start){return layoutFromSeed(start.layoutSeed,start.chunkX,start.chunkZ,start.surfaceY);}
    private static JigsawPlacementRuntime120.Result layoutFromSeed(long seed,int cx,int cz,final int surface){
        // 1.20.2 samples WORLD_SURFACE_WG and then adds the configured absolute
        // start height (-15). Feeding -15 into the generic projection branch
        // cancelled that offset against the root box minimum and placed the
        // complete tower on the grass. Every Trail Ruins pool element is rigid,
        // so supplying the already projected origin is equivalent and preserves
        // the intended fifteen-block burial without affecting other structures.
        return JigsawPlacementRuntime120.generateLegacy(REGISTRY,new JigsawPlacementRuntime120.Terrain(){
            public int surfaceY(int x,int z){return surface;}
        },START_POOL,seed,(cx<<4)+7,surface+START_HEIGHT,(cz<<4)+7,SIZE,MAX_DISTANCE,null,false);
    }
    public static long layoutSeed(long seed,int x,int z){long v=seed^(long)x*341873128712L^(long)z*132897987541L^83469867L;v^=v>>>33;v*=0xff51afd7ed558ccdL;return v^(v>>>33);}
    private static boolean supports(BiomeGenBase b){return b==BiomeGenBase.taiga||b==BiomeGenBase.taigaHills||b==BiomeGenBase.coldTaiga||b==BiomeGenBase.coldTaigaHills||b==BiomeGenBase.megaTaiga||b==BiomeGenBase.megaTaigaHills||b==BiomeGenBase.birchForest||b==BiomeGenBase.birchForestHills||b==BiomeGenBase.jungle||b==BiomeGenBase.jungleHills;}
    public static List<ChunkSlice> slices(JigsawPlacementRuntime120.Result layout){Map<Long,Integer> map=new TreeMap<Long,Integer>();for(JigsawPlacementRuntime120.Piece p:layout.pieces){JigsawPlacementRuntime120.Box b=p.box();for(int x=StructurePlacement120.floorDiv(b.minX,16);x<=StructurePlacement120.floorDiv(b.maxX,16);x++)for(int z=StructurePlacement120.floorDiv(b.minZ,16);z<=StructurePlacement120.floorDiv(b.maxZ,16);z++){long k=((long)x<<32)^(z&0xffffffffL);Integer n=map.get(k);map.put(k,n==null?1:n+1);}}List<ChunkSlice> out=new ArrayList<ChunkSlice>();for(Map.Entry<Long,Integer> e:map.entrySet())out.add(new ChunkSlice((int)(e.getKey()>>32),(int)(long)e.getKey(),e.getValue()));return out;}
    private static boolean intersects(JigsawPlacementRuntime120.Result result,int x,int z){for(JigsawPlacementRuntime120.Piece p:result.pieces)if(intersects(p.box(),x,z))return true;return false;}private static boolean intersects(JigsawPlacementRuntime120.Box b,int x,int z){int bx=x<<4,bz=z<<4;return b.maxX>=bx&&b.minX<=bx+15&&b.maxZ>=bz&&b.minZ<=bz+15;}
    public static void validateTemplates(){REGISTRY.validateGraph(START_POOL);if(REGISTRY.loadedPoolCount()!=7||REGISTRY.loadedTemplateCount()!=84)throw new IllegalStateException("Trail Ruins graph incomplete: pools="+REGISTRY.loadedPoolCount()+", templates="+REGISTRY.loadedTemplateCount());for(String id:REGISTRY.loadedTemplateIds())FuturaAncientCityGenerator120.validateTemplateResource(OfficialJigsawRegistry120.templateResource(id));}
    public static final class ChunkSlice{public final int chunkX,chunkZ,pieceCount;ChunkSlice(int x,int z,int n){chunkX=x;chunkZ=z;pieceCount=n;}}
}
