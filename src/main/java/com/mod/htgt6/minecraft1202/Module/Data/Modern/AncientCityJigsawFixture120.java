package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.AncientCityJigsawRegistry120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;

import java.util.List;

/** Official-pool, named-anchor and deterministic clipping checks for the Ancient City plan. */
public final class AncientCityJigsawFixture120 {
    public static long assertContract(){
        AncientCityJigsawPlan120 registry=new AncientCityJigsawPlan120();
        List<JigsawPlacementRuntime120.Pool> pools=registry.pools;
        if(pools.size()!=7||AncientCityJigsawRegistry120.SIZE!=7
                ||AncientCityJigsawRegistry120.MAX_DISTANCE!=116
                ||!"minecraft:city_anchor".equals(AncientCityJigsawRegistry120.START_JIGSAW))
            throw new IllegalStateException("Ancient City structure settings differ from 1.20.2");
        int singles=0,lists=0,empty=0,features=0,totalWeight=0;long hash=0xcbf29ce484222325L;
        for(JigsawPlacementRuntime120.Pool pool:pools){hash=mix(hash,pool.id.hashCode());
            for(JigsawPlacementRuntime120.Weighted weighted:pool.entries){totalWeight+=weighted.weight;
                hash=mix(hash,weighted.weight);hash=mix(hash,weighted.element.kind.ordinal());
                switch(weighted.element.kind){case SINGLE:singles++;break;case LIST:lists++;break;
                    case EMPTY:empty++;break;case FEATURE:features++;break;default:throw new AssertionError();}
                if(weighted.element.projection!=JigsawPlacementRuntime120.Projection.RIGID)
                    throw new IllegalStateException("Ancient City pool contains non-rigid element");
            }
        }
        if(singles!=60||lists!=2||empty!=2||features!=1||totalWeight!=107)
            throw new IllegalStateException("Ancient City pool inventory changed: single="+singles+", list="+lists
                    +", empty="+empty+", feature="+features+", weight="+totalWeight);
        JigsawPlacementRuntime120.Pool start=registry.pool(AncientCityJigsawRegistry120.START_POOL);
        for(JigsawPlacementRuntime120.Weighted weighted:start.entries){
            JigsawPlacementRuntime120.Template template=registry.template(weighted.element.template);
            boolean anchor=false;for(JigsawPlacementRuntime120.Connector connector:template.connectors)
                anchor|=AncientCityJigsawRegistry120.START_JIGSAW.equals(connector.name);
            if(!anchor)throw new IllegalStateException("City center lacks minecraft:city_anchor: "+template.id);
        }
        // The official 1.20.2 pool has a dangling stairs_5 reference absent from
        // the same client.jar. Keep production on fixed-slot until the planner can
        // deterministically skip that candidate without changing pool RNG.
        if(!registry.missingTemplates.equals(java.util.Collections.singleton(
                "minecraft:ancient_city/walls/intact_horizontal_wall_stairs_5")))
            throw new IllegalStateException("Unexpected missing template set: "+registry.missingTemplates);
        if(registry.templates().size()!=57)throw new IllegalStateException("Unexpected available template count: "
                +registry.templates().size());
        hash=mix(hash,processorFingerprint());
        long first=layoutFingerprint(registry,0x1202AC17C17EL),second=layoutFingerprint(registry,0x1202AC17C17EL);
        if(first!=second)throw new IllegalStateException("Ancient City chunk-clipped plan is not deterministic");
        hash=mix(hash,first);
        return hash;
    }
    private static long processorFingerprint(){
        String[] ids={"minecraft:ancient_city_start_degradation","minecraft:ancient_city_generic_degradation",
                "minecraft:ancient_city_walls_degradation"};
        int[] rules={3,3,4};long hash=0xcbf29ce484222325L;
        for(int i=0;i<ids.length;i++){
            if(StructureProcessorRuntime120.load(ids[i]).size()!=rules[i])
                throw new IllegalStateException("Ancient City processor rule count changed: "+ids[i]);
            for(int x=-32;x<=32;x++){
                StructureProcessorRuntime120.State input=new StructureProcessorRuntime120.State("minecraft:deepslate_bricks");
                StructureProcessorRuntime120.State first=StructureProcessorRuntime120.apply(
                        java.util.Collections.singletonList(ids[i]),input,
                        new StructureProcessorRuntime120.State("minecraft:stone"),x,37,-x);
                StructureProcessorRuntime120.State second=StructureProcessorRuntime120.apply(
                        java.util.Collections.singletonList(ids[i]),input,
                        new StructureProcessorRuntime120.State("minecraft:stone"),x,37,-x);
                if(first==null?second!=null:second==null||!first.name.equals(second.name))
                    throw new IllegalStateException("Processor is not positional-deterministic");
                hash=mix(hash,first==null?0:first.name.hashCode());
            }
        }
        StructureProcessorRuntime120.State protectedResult=StructureProcessorRuntime120.apply(
                java.util.Collections.singletonList(ids[0]),new StructureProcessorRuntime120.State("minecraft:deepslate"),
                new StructureProcessorRuntime120.State("minecraft:reinforced_deepslate"),0,37,0);
        if(protectedResult!=null)throw new IllegalStateException("Protected block processor replaced reinforced deepslate");
        return hash;
    }
    private static long layoutFingerprint(AncientCityJigsawPlan120 plan,long seed){
        JigsawPlacementRuntime120.Result result=JigsawPlacementRuntime120.generate(plan,
                new JigsawPlacementRuntime120.Terrain(){public int surfaceY(int x,int z){return 37;}},
                plan.startPool,seed,0,37,0,plan.size,plan.maxDistanceFromCenter,plan.startJigsawName);
        long hash=0xcbf29ce484222325L;
        for(JigsawPlacementRuntime120.Piece piece:result.pieces){
            JigsawPlacementRuntime120.Box box=piece.box();
            if(box.minX < -116||box.maxX > 116||box.minY < 37-116||box.maxY > 37+116
                    ||box.minZ < -116||box.maxZ > 116)throw new IllegalStateException("Piece escaped max distance");
            hash=mix(hash,piece.template.id.hashCode());hash=mix(hash,piece.rotation);
            hash=mix(hash,piece.x);hash=mix(hash,piece.y);hash=mix(hash,piece.z);
            // A production adapter may invoke the same plan from any affected
            // chunk; intersection must depend only on immutable piece boxes.
            for(int chunkZ=-8;chunkZ<=8;chunkZ++)for(int chunkX=-8;chunkX<=8;chunkX++){
                int minX=chunkX<<4,minZ=chunkZ<<4;
                if(box.maxX>=minX&&box.minX<=minX+15&&box.maxZ>=minZ&&box.minZ<=minZ+15)
                    hash=mix(hash,(chunkX&0xffff)|((long)(chunkZ&0xffff)<<16));
            }
        }
        return mix(hash,result.rejectedByBoundsOrCollision);
    }
    public static void main(String[] arguments){System.out.println(Long.toHexString(assertContract()));}
    private static long mix(long hash,long value){return(hash^value)*0x100000001b3L;}
    private AncientCityJigsawFixture120(){ }
}
