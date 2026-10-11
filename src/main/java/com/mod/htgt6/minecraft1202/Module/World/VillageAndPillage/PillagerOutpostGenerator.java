package com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage;

import com.mod.htgt6.common.util.StructurePlacement120;
import com.mod.htgt6.common.util.WorldSafety;
import com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120;
import com.mod.htgt6.minecraft1202.Module.Entity.VillageAndPillage.EntityPillager;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldType120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.PillagerOutpostJigsawRegistry120;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.world.ChunkEvent;

/** Chunk-safe adapter for the official Minecraft 1.20.2 pillager-outpost pools. */
public final class PillagerOutpostGenerator implements IWorldGenerator {
    private static final PillagerOutpostJigsawRegistry120 REGISTRY=new PillagerOutpostJigsawRegistry120();
    private static final PillagerOutpostJigsawRegistry120 LEGACY_REGISTRY=new PillagerOutpostJigsawRegistry120(0);
    private static final long LAYOUT_SALT=0x50494C4C41474552L;

    @Override public void generate(Random ignored,int chunkX,int chunkZ,World world,
                                   IChunkProvider chunkGenerator,IChunkProvider chunkProvider){
        if(world.provider.dimensionId!=0||world.isRemote||!world.getWorldInfo().isMapFeaturesEnabled())return;
        PillagerOutpostData120 data=PillagerOutpostData120.get(world);
        PillagerOutpostData120.Start start=data.start(chunkX,chunkZ);
        if(start==null&&isCandidate(world.getSeed(),chunkX,chunkZ)){
            int centerX=(chunkX<<4)+7,centerZ=(chunkZ<<4)+7;
            BiomeGenBase biome=world.getBiomeGenForCoords(centerX,centerZ);
            int y=world.getChunkFromChunkCoords(chunkX,chunkZ).getHeightValue(7,7);
            if(supportsVillage(biome)&&validSurfaceHeight(y,FuturaWorldType120.isFutura(world)))
                start=data.recordNewStart(chunkX,chunkZ,y,layoutSeed(world.getSeed(),chunkX,chunkZ));
        }
        replayTarget(world,data,chunkX,chunkZ);
        if(start!=null)backfillLoaded(world,data,start,chunkX,chunkZ);
    }

    /** Replays persisted slices only after an already-populated chunk is loaded from disk. */
    @SubscribeEvent public void chunkLoad(ChunkEvent.Load event){
        World world=event.world;if(world==null||world.isRemote||world.provider.dimensionId!=0
                ||!event.getChunk().isTerrainPopulated)return;
        replayTarget(world,PillagerOutpostData120.get(world),event.getChunk().xPosition,event.getChunk().zPosition);
    }

    private static void replayTarget(World world,PillagerOutpostData120 data,int targetX,int targetZ){
        Chunk target=WorldSafety.getLoadedChunk(world,targetX,targetZ);
        if(target==null)return;
        // WORLD_SURFACE_WG must not rise as earlier template blocks are written.
        JigsawPlacementRuntime120.Terrain surface=snapshotSurface(target);
        for(PillagerOutpostData120.Start start:data.startsNear(targetX,targetZ,6)){
            if(data.hasSlice(start,targetX,targetZ)){
                if(targetX==start.chunkX&&targetZ==start.chunkZ)spawnGuards(world,data,start);
                continue;
            }
            boolean pending=start.version>=2&&start.plan==null;
            JigsawPlacementRuntime120.Result layout=resolvePlan(world,data,start);
            if(layout==null)continue;
            if(!intersects(layout,targetX,targetZ)){if(pending)backfillLoaded(world,data,start,targetX,targetZ);continue;}
            // Match rigid attachment planning and terrain-matching placement in Futura.
            // A populated chunk heightmap can include trees and previously placed structures.
            JigsawPlacementRuntime120.Terrain placementSurface=start.version>=2&&FuturaWorldType120.isFutura(world)
                    ?(x,z)->com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceEstimator120.estimate(world.getSeed(),x,z,FuturaHeight120.MAX_BUILD_Y):surface;
            for(JigsawPlacementRuntime120.Piece piece:layout.pieces)if(intersects(piece.box(),targetX,targetZ))
                placeElement(world,piece.element,piece.x,piece.y,piece.z,piece.rotation,targetX,targetZ,placementSurface);
            data.recordSlice(start,targetX,targetZ);
            if(targetX==start.chunkX&&targetZ==start.chunkZ)spawnGuards(world,data,start);
            if(pending)backfillLoaded(world,data,start,targetX,targetZ);
        }
    }

    /** Completes only loaded/populated neighbours, never asking the provider to generate one. */
    private static void backfillLoaded(World world,PillagerOutpostData120 data,PillagerOutpostData120.Start start,
                                       int currentChunkX,int currentChunkZ){
        JigsawPlacementRuntime120.Result plan=resolvePlan(world,data,start);if(plan==null)return;
        for(ChunkSlice slice:slices(plan)){
            if(slice.chunkX==currentChunkX&&slice.chunkZ==currentChunkZ)continue;
            Chunk chunk=WorldSafety.getLoadedChunk(world,slice.chunkX,slice.chunkZ);
            if(chunk!=null&&chunk.isTerrainPopulated)replayTarget(world,data,slice.chunkX,slice.chunkZ);
        }
    }

    private static void placeElement(World world,JigsawPlacementRuntime120.Element element,
                                     int x,int y,int z,int rotation,int chunkX,int chunkZ,
                                     JigsawPlacementRuntime120.Terrain surface){
        if(element.kind==JigsawPlacementRuntime120.ElementKind.EMPTY
                ||element.kind==JigsawPlacementRuntime120.ElementKind.FEATURE)return;
        if(element.kind==JigsawPlacementRuntime120.ElementKind.LIST){
            for(JigsawPlacementRuntime120.Element child:element.children)
                placeElement(world,child,x,y,z,rotation,chunkX,chunkZ,surface);
            return;
        }
        JigsawPlacementRuntime120.Terrain gravity=element.projection==JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING?surface:null;
        FuturaAncientCityGenerator120.placeTemplateAtOrigin(world,
                PillagerOutpostJigsawRegistry120.templateResource(element.template),x,y,z,rotation,
                chunkX,chunkZ,element.processors,element.ignoreTemplateAir,gravity,false);
    }

    public static JigsawPlacementRuntime120.Terrain snapshotSurface(final Chunk chunk){
        final int[] heights=new int[256];
        for(int z=0;z<16;z++)for(int x=0;x<16;x++)heights[z*16+x]=chunk.getHeightValue(x,z);
        final int chunkX=chunk.xPosition,chunkZ=chunk.zPosition;
        return new JigsawPlacementRuntime120.Terrain(){public int surfaceY(int x,int z){
            // The template processor computes coordinates before clipping ownership.
            // Unowned cells are discarded; never load neighbouring chunks to sample them.
            return (x>>4)==chunkX&&(z>>4)==chunkZ?heights[(z&15)*16+(x&15)]:0;
        }};
    }

    public static JigsawPlacementRuntime120.Result layout(long seed,int chunkX,int chunkZ,final int surfaceY){
        return layoutFromSeed(layoutSeed(seed,chunkX,chunkZ),chunkX,chunkZ,surfaceY);
    }
    public static JigsawPlacementRuntime120.Result layout(PillagerOutpostData120.Start start){
        if(start.plan!=null)return start.plan;
        if(start.version>=2)return null;
        start.plan=JigsawPlacementRuntime120.generateLegacy(LEGACY_REGISTRY,(x,z)->start.surfaceY,
                PillagerOutpostJigsawRegistry120.START_POOL,start.layoutSeed,(start.chunkX<<4)+7,0,(start.chunkZ<<4)+7,
                PillagerOutpostJigsawRegistry120.SIZE,PillagerOutpostJigsawRegistry120.MAX_DISTANCE,null,true);
        return start.plan;
    }
    private static final class PendingTerrain extends RuntimeException { }
    private static JigsawPlacementRuntime120.Result resolvePlan(final World world,final PillagerOutpostData120 data,final PillagerOutpostData120.Start start){
        if(start.rejected)return null;if(start.version<2||start.plan!=null)return layout(start);
        JigsawPlacementRuntime120.Terrain terrain=new JigsawPlacementRuntime120.Terrain(){public int surfaceY(int x,int z){
            long key=((long)x<<32)^(z&0xffffffffL);Integer saved=start.heights.get(key);if(saved!=null)return saved;
            int height;
            if(FuturaWorldType120.isFutura(world))height=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceEstimator120.estimate(world.getSeed(),x,z,FuturaHeight120.MAX_BUILD_Y);
            else {Chunk c=WorldSafety.getLoadedChunk(world,x>>4,z>>4);if(c==null)throw new PendingTerrain();height=c.getHeightValue(x&15,z&15);}
            start.heights.put(key,height);data.markDirty();return height;
        }};
        try{
            JigsawPlacementRuntime120.Result result=JigsawPlacementRuntime120.generate(REGISTRY,terrain,
                    PillagerOutpostJigsawRegistry120.START_POOL,start.layoutSeed,(start.chunkX<<4)+7,0,(start.chunkZ<<4)+7,
                    PillagerOutpostJigsawRegistry120.SIZE,PillagerOutpostJigsawRegistry120.MAX_DISTANCE,null,true);
            if(!fitsHeight(result,world.getActualHeight())){start.rejected=true;data.markDirty();return null;}
            start.plan=result;data.markDirty();return result;
        }catch(PendingTerrain pending){return null;}
    }
    public static boolean fitsHeight(JigsawPlacementRuntime120.Result layout,int height){
        for(JigsawPlacementRuntime120.Piece p:layout.pieces)if(p.box().minY<1||p.box().maxY>=height)return false;return true;
    }
    private static JigsawPlacementRuntime120.Result layoutFromSeed(long savedLayoutSeed,int chunkX,int chunkZ,final int surfaceY){
        return JigsawPlacementRuntime120.generate(REGISTRY,new JigsawPlacementRuntime120.Terrain(){
            @Override public int surfaceY(int x,int z){return surfaceY;}
        },PillagerOutpostJigsawRegistry120.START_POOL,savedLayoutSeed,
                (chunkX<<4)+7,0,(chunkZ<<4)+7,PillagerOutpostJigsawRegistry120.SIZE,
                PillagerOutpostJigsawRegistry120.MAX_DISTANCE,null,true);
    }

    public static List<ChunkSlice> slices(JigsawPlacementRuntime120.Result layout){
        Map<Long,Integer> counts=new LinkedHashMap<Long,Integer>();
        for(JigsawPlacementRuntime120.Piece piece:layout.pieces){JigsawPlacementRuntime120.Box box=piece.box();
            int minX=StructurePlacement120.floorDiv(box.minX,16),maxX=StructurePlacement120.floorDiv(box.maxX,16);
            int minZ=StructurePlacement120.floorDiv(box.minZ,16),maxZ=StructurePlacement120.floorDiv(box.maxZ,16);
            for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){long key=((long)x<<32)^(z&0xffffffffL);
                Integer old=counts.get(key);counts.put(key,old==null?1:old+1);}}
        List<ChunkSlice> result=new ArrayList<ChunkSlice>();for(Map.Entry<Long,Integer> entry:counts.entrySet())
            result.add(new ChunkSlice((int)(entry.getKey()>>32),(int)(long)entry.getKey(),entry.getValue()));
        Collections.sort(result,new Comparator<ChunkSlice>(){@Override public int compare(ChunkSlice a,ChunkSlice b){
            int x=Integer.compare(a.chunkX,b.chunkX);return x!=0?x:Integer.compare(a.chunkZ,b.chunkZ);}});return result;
    }
    private static boolean intersects(JigsawPlacementRuntime120.Result layout,int chunkX,int chunkZ){
        for(JigsawPlacementRuntime120.Piece piece:layout.pieces)if(intersects(piece.box(),chunkX,chunkZ))return true;return false;
    }
    private static boolean intersects(JigsawPlacementRuntime120.Box box,int chunkX,int chunkZ){
        int minX=chunkX<<4,minZ=chunkZ<<4;return box.maxX>=minX&&box.minX<=minX+15
                &&box.maxZ>=minZ&&box.minZ<=minZ+15;
    }

    public static boolean isCandidate(long seed,int chunkX,int chunkZ){
        if(!StructurePlacement120.isCandidate(seed,chunkX,chunkZ,StructurePlacement120.PILLAGER_OUTPOST)
                ||!StructurePlacement120.pillagerOutpostFrequencyPasses(seed,chunkX,chunkZ))return false;
        return !excludedByVillage(seed,chunkX,chunkZ);
    }

    /** 1.20.2 exclusion_zone: no outpost start within ten chunks of a village start. */
    public static boolean excludedByVillage(long seed,int chunkX,int chunkZ){
        int minRegionX=StructurePlacement120.floorDiv(chunkX-10,StructurePlacement120.VILLAGE.spacing);
        int maxRegionX=StructurePlacement120.floorDiv(chunkX+10,StructurePlacement120.VILLAGE.spacing);
        int minRegionZ=StructurePlacement120.floorDiv(chunkZ-10,StructurePlacement120.VILLAGE.spacing);
        int maxRegionZ=StructurePlacement120.floorDiv(chunkZ+10,StructurePlacement120.VILLAGE.spacing);
        for(int rx=minRegionX;rx<=maxRegionX;rx++)for(int rz=minRegionZ;rz<=maxRegionZ;rz++){
            int[] village=StructurePlacement120.candidate(seed,rx,rz,StructurePlacement120.VILLAGE);
            if(Math.abs(village[0]-chunkX)<=10&&Math.abs(village[1]-chunkZ)<=10)return true;
        }
        return false;
    }

    public static long layoutSeed(long seed,int chunkX,int chunkZ){
        long value=seed^(long)chunkX*341873128712L^(long)chunkZ*132897987541L^LAYOUT_SALT;
        value^=value>>>33;value*=0xff51afd7ed558ccdL;value^=value>>>33;return value;
    }

    public static boolean validSurfaceHeight(int y,boolean futura){
        return y>0&&y<(futura?FuturaHeight120.PHYSICAL_HEIGHT:FuturaHeight120.LEGACY_HEIGHT);
    }

    public static boolean supportsVillage(BiomeGenBase biome){
        if(biome==null)return false;
        int id=biome.biomeID;
        return biome==BiomeGenBase.plains||biome==BiomeGenBase.desert||biome==BiomeGenBase.savanna
                ||biome==BiomeGenBase.taiga||biome==BiomeGenBase.icePlains
                ||id==com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.MEADOW
                ||id==com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.CHERRY_GROVE
                ||id==com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.GROVE
                ||id==com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.SNOWY_SLOPES
                ||id==com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.JAGGED_PEAKS
                ||id==com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.FROZEN_PEAKS
                ||id==com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120.STONY_PEAKS;
    }

    private static void spawnGuards(World world,PillagerOutpostData120 data,PillagerOutpostData120.Start start){
        if(start.version>=2||world.isRemote||world.difficultySetting==net.minecraft.world.EnumDifficulty.PEACEFUL
                ||!world.getGameRules().getGameRuleBooleanValue("doMobSpawning"))return;
        for(int i=0;i<4;i++){
            if(data.hasGuard(start.chunkX,start.chunkZ,i))continue;
            Random random=new Random(start.layoutSeed^0x475541524453L^(long)i*341873128712L);
            EntityPillager pillager=new EntityPillager(world);
            boolean placed=false;
            for(int attempt=0;attempt<16&&!placed;attempt++){
                int x=(start.chunkX<<4)+4+random.nextInt(7),z=(start.chunkZ<<4)+4+random.nextInt(7);
                Chunk chunk=WorldSafety.getLoadedChunk(world,start.chunkX,start.chunkZ);
                if(chunk==null)break;
                int y=chunk.getHeightValue(x&15,z&15);
                pillager.setLocationAndAngles(x+.5D,y,z+.5D,random.nextFloat()*360F,0);
                if(!safeGuardPosition(world,pillager,x,y,z))continue;
                pillager.onSpawnWithEgg(null);pillager.setCaptain(i==0);
                if(world.spawnEntityInWorld(pillager)){data.recordGuard(start.chunkX,start.chunkZ,i);placed=true;}
                else break;
            }
        }
    }

    public static boolean safeGuardPosition(World world,EntityPillager mob,int x,int y,int z){
        return world.blockExists(x,y-1,z)&&world.blockExists(x,y+1,z)
                &&world.getBlock(x,y-1,z).isSideSolid(world,x,y-1,z,net.minecraftforge.common.util.ForgeDirection.UP)
                &&world.checkNoEntityCollision(mob.boundingBox)
                &&world.getCollidingBoundingBoxes(mob,mob.boundingBox).isEmpty()
                &&!world.isAnyLiquid(mob.boundingBox);
    }

    public static void validateTemplates(){
        REGISTRY.validateAll();
        String[] names={"base_plate","feature_cage1","feature_cage2","feature_cage_with_allays",
                "feature_logs","feature_plate","feature_targets","feature_tent1","feature_tent2",
                "watchtower","watchtower_overgrown"};
        for(String name:names)FuturaAncientCityGenerator120.validateTemplateResource(
                "/assets/htgt6/structures/pillager_outpost/"+name+".nbt");
    }

    public static final class ChunkSlice{
        public final int chunkX,chunkZ,pieceCount;
        ChunkSlice(int chunkX,int chunkZ,int pieceCount){this.chunkX=chunkX;this.chunkZ=chunkZ;this.pieceCount=pieceCount;}
    }
}
