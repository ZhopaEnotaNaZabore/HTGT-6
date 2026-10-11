package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Headless connector/rotation/depth/collision regression for the shared jigsaw runtime. */
public final class JigsawPlacementFixture120 {
    private static final long EXPECTED=0xcbc210c18c84113cL;

    public static long assertContract(){
        final Map<String,JigsawPlacementRuntime120.Template> templates=
                new HashMap<String,JigsawPlacementRuntime120.Template>();
        final Map<String,JigsawPlacementRuntime120.Pool> pools=
                new HashMap<String,JigsawPlacementRuntime120.Pool>();
        JigsawPlacementRuntime120.Connector west=connector(0,JigsawPlacementRuntime120.Direction.WEST);
        JigsawPlacementRuntime120.Connector east=connector(2,JigsawPlacementRuntime120.Direction.EAST);
        templates.put("fixture:start",new JigsawPlacementRuntime120.Template("fixture:start",3,3,3,0,
                Arrays.asList(west,east)));
        templates.put("fixture:road",new JigsawPlacementRuntime120.Template("fixture:road",3,3,3,0,
                Arrays.asList(west,east)));
        JigsawPlacementRuntime120.Element start=new JigsawPlacementRuntime120.Element("fixture:start",
                JigsawPlacementRuntime120.Projection.RIGID,Collections.<String>emptyList());
        JigsawPlacementRuntime120.Element road=new JigsawPlacementRuntime120.Element("fixture:road",
                JigsawPlacementRuntime120.Projection.RIGID,Collections.singletonList("fixture:processor"));
        pools.put("fixture:start_pool",new JigsawPlacementRuntime120.Pool("fixture:start_pool","minecraft:empty",
                Collections.singletonList(new JigsawPlacementRuntime120.Weighted(start,1))));
        pools.put("fixture:road_pool",new JigsawPlacementRuntime120.Pool("fixture:road_pool","minecraft:empty",
                Collections.singletonList(new JigsawPlacementRuntime120.Weighted(road,1))));
        JigsawPlacementRuntime120.Registry registry=new JigsawPlacementRuntime120.Registry(){
            public JigsawPlacementRuntime120.Pool pool(String id){return pools.get(id);}
            public JigsawPlacementRuntime120.Template template(String id){return templates.get(id);}
        };
        JigsawPlacementRuntime120.Result result=JigsawPlacementRuntime120.generate(registry,
                new JigsawPlacementRuntime120.Terrain(){public int surfaceY(int x,int z){return 64;}},
                "fixture:start_pool",0x1202L,0,64,0,3,32);
        long hash=0xcbf29ce484222325L;
        for(JigsawPlacementRuntime120.Piece piece:result.pieces){
            hash=fnv(hash,piece.template.id.hashCode());hash=fnv(hash,piece.x);hash=fnv(hash,piece.y);
            hash=fnv(hash,piece.z);hash=fnv(hash,piece.rotation);hash=fnv(hash,piece.depth);
        }
        hash=fnv(hash,result.rejectedByBoundsOrCollision);
        if(EXPECTED!=0L&&hash!=EXPECTED)throw new IllegalStateException("Jigsaw fixture changed: expected "
                +Long.toHexString(EXPECTED)+", got "+Long.toHexString(hash));
        assertTerrainMatchingParentProjectsRigidChild(templates,pools,registry,road);
        JigsawPlacementRuntime120.Terrain blockSlope=new JigsawPlacementRuntime120.Terrain(){
            public int surfaceY(int x,int z){return 80+x-z;}
        };
        if(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120
                .terrainMatchedBlockY(blockSlope,2,5,3)!=79)throw new IllegalStateException(
                "terrain-matching blocks were not individually gravity projected");
        return hash;
    }

    /** 1.20.2: either side being terrain_matching selects the heightmap branch. */
    private static void assertTerrainMatchingParentProjectsRigidChild(
            Map<String,JigsawPlacementRuntime120.Template> templates,
            Map<String,JigsawPlacementRuntime120.Pool> pools,
            JigsawPlacementRuntime120.Registry registry,JigsawPlacementRuntime120.Element rigidChild){
        JigsawPlacementRuntime120.Element projectedStart=new JigsawPlacementRuntime120.Element("fixture:start",
                JigsawPlacementRuntime120.Projection.TERRAIN_MATCHING,Collections.<String>emptyList());
        pools.put("fixture:projected_start",new JigsawPlacementRuntime120.Pool("fixture:projected_start","minecraft:empty",
                Collections.singletonList(new JigsawPlacementRuntime120.Weighted(projectedStart,1))));
        final JigsawPlacementRuntime120.Terrain slope=new JigsawPlacementRuntime120.Terrain(){
            public int surfaceY(int x,int z){return 70+x*3+z*5;}
        };
        JigsawPlacementRuntime120.Result projected=JigsawPlacementRuntime120.generate(registry,slope,
                "fixture:projected_start",0x1202L,0,70,0,1,32);
        if(projected.pieces.size()<2)throw new IllegalStateException("terrain-matching parent rejected its rigid child");
        JigsawPlacementRuntime120.Piece parent=projected.pieces.get(0),child=projected.pieces.get(1);
        boolean matched=false;
        for(JigsawPlacementRuntime120.Connector from:parent.worldConnectors())
            for(JigsawPlacementRuntime120.Connector to:child.worldConnectors())
                if(from.x+from.front.dx==to.x&&from.z+from.front.dz==to.z
                        &&from.front.opposite()==to.front){
                    matched=true;
                    if(to.y!=slope.surfaceY(from.x,from.z))throw new IllegalStateException(
                            "rigid child was not projected from its terrain-matching parent");
                }
        if(!matched)throw new IllegalStateException("projected fixture child junction missing");
    }

    private static JigsawPlacementRuntime120.Connector connector(int x,JigsawPlacementRuntime120.Direction front){
        return new JigsawPlacementRuntime120.Connector(x,1,1,front,JigsawPlacementRuntime120.Direction.UP,
                JigsawPlacementRuntime120.Joint.ALIGNED,"minecraft:road","minecraft:road","fixture:road_pool");
    }

    private static long fnv(long hash,int value){return(hash^value)*0x100000001b3L;}
    public static void main(String[] ignored){System.out.println(Long.toHexString(assertContract()));}
    private JigsawPlacementFixture120(){ }
}
