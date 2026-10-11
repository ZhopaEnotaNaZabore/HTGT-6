package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.common.util.StructurePlacement120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.VillageJigsawRegistry120;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Futura-only adapter for the official Minecraft 1.20.2 village NBT set. */
public final class FuturaVillageGenerator120 {
    private static final Logger LOGGER=LogManager.getLogger("HTGT6-FuturaVillage120");
    private static final VillageJigsawRegistry120 PLAINS_JIGSAW=new VillageJigsawRegistry120();
    private static final ThreadLocal<LayoutCache> PLAINS_LAYOUTS=new ThreadLocal<LayoutCache>();
    private static final ThreadLocal<Boolean> RECOVER_RESIDENTS=new ThreadLocal<Boolean>();
    private static final String[] STYLE_NAMES={"plains","desert","savanna","snowy","taiga"};
    private static final int MAX_RADIUS_CHUNKS=7;
    private static final String ROOT="/assets/htgt6/structures/village_1_20/";

    public static void validateTemplates(){
        PLAINS_JIGSAW.validateAll();
        for(String id:PLAINS_JIGSAW.loadedTemplateIds()){
            int split=id.indexOf(':');String path=split<0?id:id.substring(split+1);
            FuturaAncientCityGenerator120.validateTemplateResource(ROOT+path.substring("village/".length())+".nbt");
        }
        if(!PLAINS_JIGSAW.unsupportedFeatures().isEmpty())LOGGER.warn(
                "Official plains village feature_pool elements are unavailable and remain empty: {}",
                PLAINS_JIGSAW.unsupportedFeatures());
    }

    public static void loadTemplateNbtForFixture(){
        PLAINS_JIGSAW.validateAll();
        for(String id:PLAINS_JIGSAW.loadedTemplateIds()){
            int split=id.indexOf(':');String path=split<0?id:id.substring(split+1);
            FuturaAncientCityGenerator120.retainTemplateResource(
                    ROOT+path.substring("village/".length())+".nbt");
        }
    }

    static boolean populateChunk(World world,int chunkX,int chunkZ,FuturaMountainDensityRouter120 terrain) {
        return populateChunk(world,chunkX,chunkZ,terrain,false);
    }

    /** Reasserts HTGT6 village blocks after decorators and foreign world generators. */
    static void restoreChunkAfterWorldGenerators(World world,int chunkX,int chunkZ,
                                                 FuturaMountainDensityRouter120 terrain){
        boolean generated=populateChunk(world,chunkX,chunkZ,terrain,true);
        // Entity recovery must observe the final terrain. Several legacy mod
        // decorators run after the first structure pass and can otherwise
        // invalidate the safe positions chosen for residents.
        FuturaVillagePopulation120.ensureResidents(world,chunkX,chunkZ,generated&&Boolean.TRUE.equals(RECOVER_RESIDENTS.get()));
    }

    private static boolean populateChunk(World world,int chunkX,int chunkZ,FuturaMountainDensityRouter120 terrain,
                                         boolean restorationPass) {
        boolean inside=false;
        RECOVER_RESIDENTS.set(false);
        int regionX=StructurePlacement120.floorDiv(chunkX,StructurePlacement120.VILLAGE.spacing);
        int regionZ=StructurePlacement120.floorDiv(chunkZ,StructurePlacement120.VILLAGE.spacing);
        for(int rx=regionX-1;rx<=regionX+1;rx++)for(int rz=regionZ-1;rz<=regionZ+1;rz++){
            Candidate candidate=candidate(world.getSeed(),rx,rz);
            if(Math.abs(chunkX-candidate.chunkX)>MAX_RADIUS_CHUNKS||Math.abs(chunkZ-candidate.chunkZ)>MAX_RADIUS_CHUNKS)continue;
            int startX=candidate.chunkX<<4,startZ=candidate.chunkZ<<4;
            int style=style(world.getWorldChunkManager().getBiomeGenAt(startX,startZ));
            if(style<0)continue;
            inside|=placeJigsaw(world,terrain,candidate,style,chunkX,chunkZ,restorationPass);
        }
        return inside;
    }

    /** Single production entry point: placement and population repair cannot be accidentally disconnected. */
    static boolean populateChunkAndEnsureResidents(final World world,int chunkX,int chunkZ,
                                                    FuturaMountainDensityRouter120 terrain) {
        boolean generated=populateChunk(world,chunkX,chunkZ,terrain);
        return forwardResidents(generated,new ResidentHook(){public void ensure(boolean intersects){
            FuturaVillagePopulation120.ensureResidents(world,chunkX,chunkZ,intersects&&Boolean.TRUE.equals(RECOVER_RESIDENTS.get()));
        }});
    }

    private static boolean placeJigsaw(final World world,final FuturaMountainDensityRouter120 terrain,
                                       Candidate candidate,int style,int chunkX,int chunkZ,
                                       boolean restorationPass){
        LayoutCache cache=PLAINS_LAYOUTS.get();
        long seed=world.getSeed();
        if(cache==null||cache.world.get()!=world||cache.seed!=seed||cache.chunkX!=candidate.chunkX||cache.chunkZ!=candidate.chunkZ||cache.style!=style){
            final FuturaSurfaceEstimator120.TerrainSurfaceSampler surface=
                    FuturaSurfaceEstimator120.sampler(terrain,FuturaHeight120.MAX_BUILD_Y);
            final JigsawPlacementRuntime120.Terrain generatedSurface=new JigsawPlacementRuntime120.Terrain(){
                public int surfaceY(int x,int z){
                    return Math.max(surface.estimate(x,z),FuturaMountainDensityRouter120.SEA_LEVEL+1);
                }
            };
            int startX=candidate.chunkX<<4,startZ=candidate.chunkZ<<4;
            int startY=generatedSurface.surfaceY(startX,startZ);
            JigsawPlansData120 plans=JigsawPlansData120.get(world);
            String key="village:"+candidate.chunkX+","+candidate.chunkZ+":"+style;
            JigsawPlacementRuntime120.Result layout=plans.get(key);
            if(layout==null){
                boolean legacy=JigsawPlansData120.touchesOldTerrain(world,candidate.chunkX,candidate.chunkZ);
                layout=legacy?JigsawPlacementRuntime120.generateLegacy(PLAINS_JIGSAW,generatedSurface,
                    VillageJigsawRegistry120.startPool(STYLE_NAMES[style]),layoutSeed(seed,candidate.chunkX,candidate.chunkZ),startX,startY,startZ,6,80,null,true)
                    :JigsawPlacementRuntime120.generate(PLAINS_JIGSAW,generatedSurface,
                    VillageJigsawRegistry120.startPool(STYLE_NAMES[style]),layoutSeed(seed,candidate.chunkX,candidate.chunkZ),startX,0,startZ,6,80,null,true);
                plans.put(key,layout);
            }
            cache=new LayoutCache(world,seed,candidate.chunkX,candidate.chunkZ,style,layout,generatedSurface);PLAINS_LAYOUTS.set(cache);
        }
        final JigsawPlacementRuntime120.Terrain worldSurface=cache.surface;
        boolean intersects=false;
        // Village structures use terrain_adaptation=beard_thin in 1.20.2. The
        // 1.7 terrain pipeline cannot inject that density beard, so prepare
        // every rigid building footprint before placing any template blocks.
        // Doing this as a separate pass prevents a later house from clearing
        // an already placed adjoining street.
        for(JigsawPlacementRuntime120.Piece piece:cache.layout.pieces){
            if(intersectsChunk(piece.box(),chunkX,chunkZ))
                prepareBuildingTerrain(world,piece,chunkX,chunkZ,restorationPass);
        }
        for(JigsawPlacementRuntime120.Piece piece:cache.layout.pieces){
            JigsawPlacementRuntime120.Box box=piece.box();
            if(piece.element.kind==JigsawPlacementRuntime120.ElementKind.FEATURE){
                if(piece.x+16>=(chunkX<<4)&&piece.x-16<=(chunkX<<4)+15&&piece.z+16>=(chunkZ<<4)&&piece.z-16<=(chunkZ<<4)+15)
                    VillageFeaturePlan120.place(world,piece,chunkX,chunkZ);
                continue;
            }
            if(!intersectsChunk(box,chunkX,chunkZ))continue;
            intersects=true;
            String id=piece.template.id;int split=id.indexOf(':');String path=split<0?id:id.substring(split+1);
            if(!path.startsWith("village/"))throw new IllegalStateException("Non-village template in village pool: "+id);
            String resource=ROOT+path.substring("village/".length())+".nbt";
            // A restoration replay is an integrity pass for the official NBT.
            // Foreign generators may keep their blocks in the prepared terrain
            // around a building, but never at a position occupied by a template
            // block.  Passing restorationPass here used to leave holes in roads,
            // walls and roofs whenever NovaCraft had written that position first.
            FuturaAncientCityGenerator120.placeTemplateAtOrigin(world,resource,piece.x,piece.y,piece.z,
                    piece.rotation,chunkX,chunkZ,piece.element.processors,piece.element.ignoreTemplateAir,
                    piece.element.projection==JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING?worldSurface:null,
                    false);
        }
        if(intersects&&!cache.layout.pieces.get(0).template.id.contains("/zombie/"))RECOVER_RESIDENTS.set(true);
        return intersects;
    }

    /** Post-density equivalent of the thin terrain beard used by vanilla villages. */
    private static void prepareBuildingTerrain(World world,JigsawPlacementRuntime120.Piece piece,
                                               int chunkX,int chunkZ,boolean preserveNovaCraft){
        if(piece.element.kind!=JigsawPlacementRuntime120.ElementKind.SINGLE
                ||piece.element.projection!=JigsawPlacementRuntime120.Projection.RIGID)return;
        String id=piece.template.id;
        if(!id.contains("/houses/")&&!id.contains("/town_centers/"))return;
        JigsawPlacementRuntime120.Box box=piece.box();
        int minChunkX=chunkX<<4,minChunkZ=chunkZ<<4;
        int minX=Math.max(box.minX,minChunkX),maxX=Math.min(box.maxX,minChunkX+15);
        int minZ=Math.max(box.minZ,minChunkZ),maxZ=Math.min(box.maxZ,minChunkZ+15);
        int groundY=piece.y+piece.template.groundLevelDelta;
        int clearTop=Math.min(world.getActualHeight()-1,box.maxY+1);
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){
            for(int y=Math.max(1,groundY);y<=clearTop;y++)if(!world.isAirBlock(x,y,z)){
                Block existing=world.getBlock(x,y,z);
                if(!preserveNovaCraft||!isNovaCraftBlock(existing))world.setBlock(x,y,z,Blocks.air,0,2);
            }
            // Give the NBT foundation a continuous support column on cliffs
            // and shallow water, without constructing pillars into deep caves.
            for(int y=Math.min(groundY-1,world.getActualHeight()-1),depth=0;y>0&&depth<16;y--,depth++){
                Block existing=world.getBlock(x,y,z);
                if(existing.getMaterial().blocksMovement())break;
                if(preserveNovaCraft&&isNovaCraftBlock(existing))break;
                world.setBlock(x,y,z,Blocks.dirt,0,2);
            }
        }
    }

    private static boolean isNovaCraftBlock(Block block){
        Object key=Block.blockRegistry.getNameForObject(block);
        if(key==null)return false;
        String name=String.valueOf(key).toLowerCase(java.util.Locale.ROOT);
        return name.startsWith("novacraft:")||name.startsWith("nova_craft:");
    }

    /** Runtime adapters for the complete official 1.20.2 village feature-pool set. */
    static void placeFeature(World world,JigsawPlacementRuntime120.Piece piece){
        String feature=piece.element.feature;
        Random random=new Random(featureSeed(world.getSeed(),piece.x,piece.y,piece.z,feature));
        if("minecraft:oak".equals(feature)){new WorldGenTrees(false).generate(world,random,piece.x,piece.y,piece.z);return;}
        if("minecraft:spruce".equals(feature)){new WorldGenTaiga2(false).generate(world,random,piece.x,piece.y,piece.z);return;}
        if("minecraft:pine".equals(feature)){new WorldGenTaiga1().generate(world,random,piece.x,piece.y,piece.z);return;}
        if("minecraft:acacia".equals(feature)){new WorldGenSavannaTree(false).generate(world,random,piece.x,piece.y,piece.z);return;}
        if("minecraft:patch_cactus".equals(feature)){new WorldGenCactus().generate(world,random,piece.x,piece.y,piece.z);return;}
        if("minecraft:pile_hay".equals(feature)){placeBlockPile(world,random,piece.x,piece.y,piece.z,Blocks.hay_block,0);return;}
        if("minecraft:pile_melon".equals(feature)){placeBlockPile(world,random,piece.x,piece.y,piece.z,Blocks.melon_block,0);return;}
        if("minecraft:pile_pumpkin".equals(feature)){placeBlockPile(world,random,piece.x,piece.y,piece.z,Blocks.pumpkin,0);return;}
        if("minecraft:pile_snow".equals(feature)){placeBlockPile(world,random,piece.x,piece.y,piece.z,Blocks.snow,0);return;}
        if("minecraft:pile_ice".equals(feature)){placeBlockPile(world,random,piece.x,piece.y,piece.z,Blocks.packed_ice,0);return;}
        if("minecraft:flower_plain".equals(feature)){placePatch(world,random,piece.x,piece.y,piece.z,true,false);return;}
        if("minecraft:patch_taiga_grass".equals(feature)){placePatch(world,random,piece.x,piece.y,piece.z,false,false);return;}
        if("minecraft:patch_berry_bush".equals(feature)){placePatch(world,random,piece.x,piece.y,piece.z,false,true);return;}
        throw new IllegalStateException("Official village feature has no runtime adapter: "+feature);
    }

    /** Direct 1.20.2 BlockPileFeature footprint and probability test. */
    private static void placeBlockPile(World world,Random random,int x,int y,int z,Block block,int meta){
        if(y<5)return;
        int radiusX=2+random.nextInt(2),radiusZ=2+random.nextInt(2);
        for(int dx=-radiusX;dx<=radiusX;dx++)for(int dy=0;dy<=1;dy++)for(int dz=-radiusZ;dz<=radiusZ;dz++){
            if(dx*dx+dz*dz>random.nextFloat()*10.0F-random.nextFloat()*6.0F&&random.nextFloat()>=.031F)continue;
            int wx=x+dx,wy=y+dy,wz=z+dz;
            if(!world.isAirBlock(wx,wy,wz)||!supportsPile(world,random,wx,wy,wz))continue;
            world.setBlock(wx,wy,wz,block,meta,2);
        }
    }

    private static boolean supportsPile(World world,Random random,int x,int y,int z){
        Block below=world.getBlock(x,y-1,z);
        return below==Blocks.snow?random.nextBoolean():World.doesBlockHaveSolidTopSurface(world,x,y-1,z);
    }

    private static void placePatch(World world,Random random,int x,int y,int z,boolean flowers,boolean berries){
        for(int attempt=0;attempt<32;attempt++){
            int wx=x+random.nextInt(7)-3,wy=y+random.nextInt(3)-1,wz=z+random.nextInt(7)-3;
            if(wy<1||wy>=world.getActualHeight()||!world.isAirBlock(wx,wy,wz)
                    ||!World.doesBlockHaveSolidTopSurface(world,wx,wy-1,wz))continue;
            Block block;int meta;
            if(berries){block=Backport120Content.sweetBerryBush;meta=1+random.nextInt(3);}
            else if(flowers){block=random.nextInt(3)==0?Blocks.yellow_flower:Blocks.red_flower;meta=block==Blocks.red_flower?random.nextInt(9):0;}
            else{block=Blocks.tallgrass;meta=random.nextInt(4)==0?2:1;}
            if(block.canPlaceBlockAt(world,wx,wy,wz))world.setBlock(wx,wy,wz,block,meta,2);
        }
    }

    private static long featureSeed(long seed,int x,int y,int z,String feature){
        long value=seed^(long)x*341873128712L^(long)z*132897987541L^(long)y*42317861L^feature.hashCode();
        value=(value^(value>>>33))*0xff51afd7ed558ccdL;
        value=(value^(value>>>33))*0xc4ceb9fe1a85ec53L;
        return value^(value>>>33);
    }

    private static long layoutSeed(long worldSeed,int chunkX,int chunkZ){
        Random random=new Random(worldSeed);long xSalt=random.nextLong(),zSalt=random.nextLong();
        return (long)chunkX*xSalt^(long)chunkZ*zSalt^worldSeed;
    }


    private static int style(BiomeGenBase biome){
        return biome==null?-1:styleForBiomeId(biome.biomeID);
    }

    /**
     * Selects the closest official village style for Futura land-biome families.
     *
     * A literal copy of the 1.20.2 has_structure/village_* tags leaves large
     * Futura climate regions without a single usable random-spread candidate:
     * the candidate is selected first and an ineligible biome then discards the
     * whole 34x34-chunk region.  Keep oceans, rivers, swamps, jungles and peaks
     * excluded, but let compatible variants use the matching official NBT set.
     */
    public static int styleForBiomeId(int biomeId){
        if(biomeId==BiomeGenBase.plains.biomeID||biomeId==BiomeGenBase.forest.biomeID
                ||biomeId==BiomeGenBase.forestHills.biomeID||biomeId==BiomeGenBase.birchForest.biomeID
                ||biomeId==BiomeGenBase.birchForestHills.biomeID||biomeId==BiomeGenBase.roofedForest.biomeID
                ||biomeId==BiomeGenBase.plains.biomeID+128||biomeId==BiomeGenBase.forest.biomeID+128
                ||biomeId==BiomeGenBase.birchForest.biomeID+128
                ||biomeId==BiomeGenBase.birchForestHills.biomeID+128
                ||biomeId==BiomeGenBase.roofedForest.biomeID+128||biomeId==FuturaBiomeIds120.MEADOW
                ||biomeId==FuturaBiomeIds120.CHERRY_GROVE)return 0;
        if(biomeId==BiomeGenBase.desert.biomeID||biomeId==BiomeGenBase.desertHills.biomeID
                ||biomeId==BiomeGenBase.desert.biomeID+128)return 1;
        if(biomeId==BiomeGenBase.savanna.biomeID||biomeId==BiomeGenBase.savannaPlateau.biomeID
                ||biomeId==BiomeGenBase.savanna.biomeID+128
                ||biomeId==BiomeGenBase.savannaPlateau.biomeID+128)return 2;
        if(biomeId==BiomeGenBase.icePlains.biomeID||biomeId==BiomeGenBase.iceMountains.biomeID
                ||biomeId==BiomeGenBase.icePlains.biomeID+128||biomeId==BiomeGenBase.coldTaiga.biomeID
                ||biomeId==BiomeGenBase.coldTaigaHills.biomeID
                ||biomeId==BiomeGenBase.coldTaiga.biomeID+128||biomeId==FuturaBiomeIds120.GROVE
                ||biomeId==FuturaBiomeIds120.SNOWY_SLOPES)return 3;
        if(biomeId==BiomeGenBase.taiga.biomeID||biomeId==BiomeGenBase.taigaHills.biomeID
                ||biomeId==BiomeGenBase.megaTaiga.biomeID||biomeId==BiomeGenBase.megaTaigaHills.biomeID
                ||biomeId==BiomeGenBase.megaTaiga.biomeID+128
                ||biomeId==BiomeGenBase.megaTaigaHills.biomeID+128)return 4;
        return -1;
    }

    /** True only for the single random-spread start chunk of a village region. */
    public static boolean isVillageStartChunk(long seed,int chunkX,int chunkZ){
        return StructurePlacement120.isCandidate(seed,chunkX,chunkZ,StructurePlacement120.VILLAGE);
    }

    /** Headless traversal of the same candidate, biome, jigsaw, intersection and resident-hook path. */
    public static RuntimeAudit runtimeAuditForFixture(long seed,int regionX,int regionZ,int biomeId) {
        Candidate candidate=candidate(seed,regionX,regionZ);
        boolean exactCandidate=isVillageStartChunk(seed,candidate.chunkX,candidate.chunkZ);
        int style=styleForBiomeId(biomeId);
        int startX=candidate.chunkX<<4,startZ=candidate.chunkZ<<4;
        final FuturaMountainDensityRouter120 fixtureTerrain=new FuturaMountainDensityRouter120(seed);
        final FuturaSurfaceEstimator120.TerrainSurfaceSampler fixtureSurface=
                FuturaSurfaceEstimator120.sampler(fixtureTerrain,FuturaHeight120.MAX_BUILD_Y);
        int startY=fixtureSurface.estimate(startX,startZ);
        JigsawPlacementRuntime120.Result layout=style<0?null:JigsawPlacementRuntime120.generate(PLAINS_JIGSAW,
                new JigsawPlacementRuntime120.Terrain(){public int surfaceY(int x,int z){
                    return fixtureSurface.estimate(x,z);}},
                VillageJigsawRegistry120.startPool(STYLE_NAMES[style]),
                layoutSeed(seed,candidate.chunkX,candidate.chunkZ),startX,0,startZ,6,80,null,true);
        int intersectingChunkX=candidate.chunkX,intersectingChunkZ=candidate.chunkZ;
        boolean intersects=false;int housePieces=0,streetPieces=0,terminatorPieces=0,featurePieces=0;
        StringBuilder sampleTemplates=new StringBuilder();
        if(layout!=null)for(JigsawPlacementRuntime120.Piece piece:layout.pieces){
            if(sampleTemplates.length()<500){if(sampleTemplates.length()>0)sampleTemplates.append(',');sampleTemplates.append(piece.template.id);}
            if(piece.template.id.contains("/houses/"))housePieces++;
            if(piece.template.id.contains("/streets/"))streetPieces++;
            if(piece.template.id.contains("/terminators/"))terminatorPieces++;
            if(piece.element.kind==JigsawPlacementRuntime120.ElementKind.FEATURE)featurePieces++;
            JigsawPlacementRuntime120.Box box=piece.box();
            int fromX=StructurePlacement120.floorDiv(box.minX,16),toX=StructurePlacement120.floorDiv(box.maxX,16);
            int fromZ=StructurePlacement120.floorDiv(box.minZ,16),toZ=StructurePlacement120.floorDiv(box.maxZ,16);
            for(int cx=fromX;cx<=toX&&!intersects;cx++)for(int cz=fromZ;cz<=toZ;cz++)
                if(intersectsChunk(box,cx,cz)){intersectingChunkX=cx;intersectingChunkZ=cz;intersects=true;break;}
        }
        final int[] residentCalls={0};final boolean[] residentFlag={false};
        boolean forwarded=forwardResidents(intersects,new ResidentHook(){public void ensure(boolean value){
            residentCalls[0]++;residentFlag[0]=value;
        }});
        return new RuntimeAudit(candidate.chunkX,candidate.chunkZ,startX,startZ,style,exactCandidate,
                layout==null?0:layout.pieces.size(),housePieces,streetPieces,terminatorPieces,featurePieces,
                sampleTemplates.toString(),intersectingChunkX,intersectingChunkZ,
                intersects,forwarded,residentCalls[0],residentFlag[0]);
    }

    private static boolean intersectsChunk(JigsawPlacementRuntime120.Box box,int chunkX,int chunkZ){
        int minX=chunkX<<4,minZ=chunkZ<<4,maxX=minX+15,maxZ=minZ+15;
        return box.maxX>=minX&&box.minX<=maxX&&box.maxZ>=minZ&&box.minZ<=maxZ;
    }

    private static boolean forwardResidents(boolean generated,ResidentHook hook){hook.ensure(generated);return generated;}
    private interface ResidentHook{void ensure(boolean intersects);}

    public static final class RuntimeAudit{
        public final int candidateChunkX,candidateChunkZ,startX,startZ,style,pieces,housePieces,streetPieces,
                terminatorPieces,featurePieces,intersectingChunkX,intersectingChunkZ;
        public final String sampleTemplates;
        public final boolean exactCandidate,intersects,forwarded;
        public final int residentCalls;
        public final boolean residentFlag;
        RuntimeAudit(int candidateChunkX,int candidateChunkZ,int startX,int startZ,int style,boolean exactCandidate,
                     int pieces,int housePieces,int streetPieces,int terminatorPieces,int featurePieces,
                     String sampleTemplates,int intersectingChunkX,int intersectingChunkZ,boolean intersects,boolean forwarded,
                     int residentCalls,boolean residentFlag){this.candidateChunkX=candidateChunkX;this.candidateChunkZ=candidateChunkZ;
            this.startX=startX;this.startZ=startZ;this.style=style;this.exactCandidate=exactCandidate;this.pieces=pieces;
            this.housePieces=housePieces;this.streetPieces=streetPieces;this.terminatorPieces=terminatorPieces;
            this.featurePieces=featurePieces;this.sampleTemplates=sampleTemplates;
            this.intersectingChunkX=intersectingChunkX;this.intersectingChunkZ=intersectingChunkZ;
            this.intersects=intersects;this.forwarded=forwarded;this.residentCalls=residentCalls;this.residentFlag=residentFlag;}
    }

    private static Candidate candidate(long seed,int rx,int rz){int[] point=StructurePlacement120.candidate(seed,rx,rz,StructurePlacement120.VILLAGE);return new Candidate(point[0],point[1]);}
    private static final class Candidate{final int chunkX,chunkZ;Candidate(int x,int z){chunkX=x;chunkZ=z;}}
    private static final class LayoutCache{final java.lang.ref.WeakReference<World> world;final long seed;final int chunkX,chunkZ,style;final JigsawPlacementRuntime120.Result layout;
        final JigsawPlacementRuntime120.Terrain surface;
        LayoutCache(World world,long seed,int chunkX,int chunkZ,int style,JigsawPlacementRuntime120.Result layout,
                    JigsawPlacementRuntime120.Terrain surface){this.world=new java.lang.ref.WeakReference<World>(world);this.seed=seed;this.chunkX=chunkX;this.chunkZ=chunkZ;
            this.style=style;this.layout=layout;this.surface=surface;}}
    private FuturaVillageGenerator120(){}
}
