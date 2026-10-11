package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.*;
import com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.PillagerOutpostGenerator;

/** Regression checks for the formerly failing audit counterexamples and saved plans. */
public final class JigsawStructureAudit120 {
    static final JigsawPlacementRuntime120.Terrain FLAT=(x,z)->100;
    static void require(boolean value,String label){if(!value)throw new AssertionError(label);}
    static JigsawPlacementRuntime120.Connector c(int x,int y,int z,JigsawPlacementRuntime120.Direction front,String pool){
        return new JigsawPlacementRuntime120.Connector(x,y,z,front,JigsawPlacementRuntime120.Direction.UP,
                JigsawPlacementRuntime120.Joint.ROLLABLE,"socket","socket",pool);
    }
    static JigsawPlacementRuntime120.Element element(String id){return new JigsawPlacementRuntime120.Element(id,JigsawPlacementRuntime120.Projection.RIGID,Collections.emptyList());}
    static final class Registry implements JigsawPlacementRuntime120.Registry {
        Map<String,JigsawPlacementRuntime120.Template> t=new HashMap<>();Map<String,JigsawPlacementRuntime120.Pool> p=new HashMap<>();
        public JigsawPlacementRuntime120.Template template(String id){return t.get(id);}
        public JigsawPlacementRuntime120.Pool pool(String id){return p.get(id);}
        void template(String id,int size,JigsawPlacementRuntime120.Connector... connectors){t.put(id,new JigsawPlacementRuntime120.Template(id,size,size,size,0,Arrays.asList(connectors)));}
        void pool(String id,String fallback,JigsawPlacementRuntime120.Element e){p.put(id,new JigsawPlacementRuntime120.Pool(id,fallback,Collections.singletonList(new JigsawPlacementRuntime120.Weighted(e,1))));}
    }
    public static void main(String[] args){
        Registry r=new Registry();
        r.template("base",10,c(2,2,2,JigsawPlacementRuntime120.Direction.UP,"child"));
        r.template("child",3,c(1,0,1,JigsawPlacementRuntime120.Direction.DOWN,"minecraft:empty"),c(1,1,1,JigsawPlacementRuntime120.Direction.UP,"grandchild"));
        r.template("grandchild",1,c(0,0,0,JigsawPlacementRuntime120.Direction.DOWN,"minecraft:empty"));
        r.pool("base","minecraft:empty",element("base"));r.pool("child","minecraft:empty",element("child"));r.pool("grandchild","minecraft:empty",element("grandchild"));
        JigsawPlacementRuntime120.Result nested=JigsawPlacementRuntime120.generate(r,FLAT,"base",1,0,100,0,3,80);
        require(nested.pieces.size()==3,"nested free spaces");
        require(JigsawPlacementRuntime120.generateLegacy(r,FLAT,"base",1,0,100,0,3,80,null,false).pieces.size()==2,"legacy reconstruction changed");
        System.out.println("AUDIT internal free space: expected 3 nested pieces, actual="+nested.pieces.size()+"; rejected="+nested.firstRejectionByPool);
        r.template("primary",1);r.template("overlay",1,c(0,0,0,JigsawPlacementRuntime120.Direction.UP,"grandchild"));
        r.pool("list","minecraft:empty",JigsawPlacementRuntime120.Element.list(JigsawPlacementRuntime120.Projection.RIGID,Arrays.asList(element("primary"),element("overlay"))));
        require(JigsawPlacementRuntime120.generate(r,FLAT,"list",1,0,100,0,2,80).pieces.size()==1,"list primary connectors");
        r.pool("empty_first_list","minecraft:empty",JigsawPlacementRuntime120.Element.list(JigsawPlacementRuntime120.Projection.RIGID,Arrays.asList(JigsawPlacementRuntime120.Element.empty(),element("overlay"))));
        require(JigsawPlacementRuntime120.generate(r,FLAT,"empty_first_list",1,0,100,0,2,80).pieces.size()==1,"empty primary must not expose overlay connectors");
        System.out.println("AUDIT list connectors: expected primary only (1 piece), actual="+JigsawPlacementRuntime120.generate(r,FLAT,"list",1,0,100,0,2,80).pieces.size());
        r.pool("child","grandchild",element("child"));
        require(JigsawPlacementRuntime120.generate(r,FLAT,"base",1,0,100,0,0,80).pieces.size()==1,"zero depth");
        require(JigsawPlacementRuntime120.generate(r,FLAT,"list",1,0,-15,0,0,80,null,true).pieces.get(0).y==85,"relative start height");
        System.out.println("AUDIT size zero: expected root only (1 piece), actual="+JigsawPlacementRuntime120.generate(r,FLAT,"base",1,0,100,0,0,80).pieces.size());
        System.out.println("AUDIT projected start -15: expected Y=85, actual="+JigsawPlacementRuntime120.generate(r,FLAT,"list",1,0,-15,0,0,80,null,true).pieces.get(0).y);
        VillageJigsawRegistry120 villages=new VillageJigsawRegistry120();villages.validateAll();
        System.out.println("AUDIT village resources: pools="+villages.loadedPoolCount()+", templates="+villages.loadedTemplateCount()+", features="+villages.featureTypes().size());
        for(String style:new String[]{"plains","desert","savanna","snowy","taiga"}){
            int min=Integer.MAX_VALUE,max=0,empty=0;
            for(int seed=0;seed<40;seed++){
                JigsawPlacementRuntime120.Result layout=JigsawPlacementRuntime120.generate(villages,FLAT,VillageJigsawRegistry120.startPool(style),seed,-32,0,-32,6,80,null,true);
                require(JigsawPlacementRuntime120.save(layout).equals(JigsawPlacementRuntime120.save(JigsawPlacementRuntime120.load(JigsawPlacementRuntime120.save(layout)))),"plan NBT roundtrip "+style+seed);
                int houses=0;for(JigsawPlacementRuntime120.Piece piece:layout.pieces)if(piece.template.id.contains("/houses/"))houses++;
                min=Math.min(min,houses);max=Math.max(max,houses);if(houses==0)empty++;
            }
            System.out.println("AUDIT village "+style+": 40 seeds, houses="+min+".."+max+", zero-house layouts="+empty);
        }
        Set<String> features=new TreeSet<>();int min=Integer.MAX_VALUE,max=0;
        for(int seed=0;seed<200;seed++){
            JigsawPlacementRuntime120.Result layout=PillagerOutpostGenerator.layout(seed,-3,2,150);
            min=Math.min(min,layout.pieces.size());max=Math.max(max,layout.pieces.size());
            for(JigsawPlacementRuntime120.Piece piece:layout.pieces)if(piece.template.id.contains("/feature_"))features.add(piece.template.id);
        }
        System.out.println("AUDIT outpost: 200 seeds, pieces="+min+".."+max+", reached="+features);
        System.out.println("AUDIT outpost height Y=300 Futura accepted="+PillagerOutpostGenerator.validSurfaceHeight(300,true));
        require(PillagerOutpostGenerator.validSurfaceHeight(300,true),"high outpost rejected");
        require(PillagerOutpostGenerator.fitsHeight(PillagerOutpostGenerator.layout(3,0,0,300),384),"high layout should fit");
        require(!PillagerOutpostGenerator.fitsHeight(PillagerOutpostGenerator.layout(3,0,0,380),384),"layout roof must not be truncated");
        JigsawPlacementRuntime120.Result slope=JigsawPlacementRuntime120.generate(new PillagerOutpostJigsawRegistry120(),(x,z)->150+Math.floorDiv(x,4),PillagerOutpostJigsawRegistry120.START_POOL,7,7,0,7,7,80,null,true);
        Set<Integer> heights=new HashSet<>();for(JigsawPlacementRuntime120.Piece p:slope.pieces)if(p.template.id.contains("feature_"))heights.add(p.y);
        require(heights.size()>1,"camp layout ignored slope");
        com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120 data=new com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120();
        com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage.PillagerOutpostData120.Start s=data.recordNewStart(0,0,150,7);s.plan=slope;data.recordSlice(s,-1,0);
        net.minecraft.nbt.NBTTagCompound saved=new net.minecraft.nbt.NBTTagCompound();data.writeToNBT(saved);data.readFromNBT(saved);
        require(data.start(0,0).version==2&&data.hasSlice(data.start(0,0),-1,0),"versioned slice persistence");
        require(JigsawPlacementRuntime120.save(slope).equals(JigsawPlacementRuntime120.save(PillagerOutpostGenerator.layout(data.start(0,0)))),"saved plan changed on reload");
        System.out.println("PASS Jigsaw v2: nested/list/depth/projection, 400 layouts, saved plans, slope and build-height bounds");
    }
}
