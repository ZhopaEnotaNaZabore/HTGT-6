package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.AncientCityJigsawRegistry120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure immutable snapshot of official Ancient City pools and available NBT geometry. */
public final class AncientCityJigsawPlan120 implements JigsawPlacementRuntime120.Registry {
    public final String startPool=AncientCityJigsawRegistry120.START_POOL;
    public final String startJigsawName=AncientCityJigsawRegistry120.START_JIGSAW;
    public final int size=AncientCityJigsawRegistry120.SIZE,maxDistanceFromCenter=AncientCityJigsawRegistry120.MAX_DISTANCE;
    public final List<JigsawPlacementRuntime120.Pool> pools;
    public final Set<String> missingTemplates;
    private final Map<String,JigsawPlacementRuntime120.Pool> poolsById;
    private final Map<String,JigsawPlacementRuntime120.Template> templatesById;

    public AncientCityJigsawPlan120(){
        AncientCityJigsawRegistry120 loader=new AncientCityJigsawRegistry120();
        List<JigsawPlacementRuntime120.Pool> loaded=loader.allPools();
        Map<String,JigsawPlacementRuntime120.Pool> poolMap=new LinkedHashMap<String,JigsawPlacementRuntime120.Pool>();
        Map<String,JigsawPlacementRuntime120.Template> templateMap=new LinkedHashMap<String,JigsawPlacementRuntime120.Template>();
        Set<String> missing=new LinkedHashSet<String>();
        for(JigsawPlacementRuntime120.Pool pool:loaded){poolMap.put(pool.id,pool);
            for(JigsawPlacementRuntime120.Weighted weighted:pool.entries)
                collect(weighted.element,loader,templateMap,missing);}
        poolMap.put("minecraft:empty",loader.pool("minecraft:empty"));
        pools=Collections.unmodifiableList(loaded);poolsById=Collections.unmodifiableMap(poolMap);
        templatesById=Collections.unmodifiableMap(templateMap);missingTemplates=Collections.unmodifiableSet(missing);
    }

    private static void collect(JigsawPlacementRuntime120.Element element,AncientCityJigsawRegistry120 loader,
                                Map<String,JigsawPlacementRuntime120.Template> templates,Set<String> missing){
        if(element.empty||element.kind==JigsawPlacementRuntime120.ElementKind.FEATURE)return;
        if(element.kind==JigsawPlacementRuntime120.ElementKind.LIST){
            for(JigsawPlacementRuntime120.Element child:element.children)collect(child,loader,templates,missing);return;}
        if(templates.containsKey(element.template)||missing.contains(element.template))return;
        JigsawPlacementRuntime120.Template template=loader.template(element.template);
        if(template==null)missing.add(element.template);else templates.put(element.template,template);
    }

    @Override public JigsawPlacementRuntime120.Pool pool(String id){return poolsById.get(normalize(id));}
    @Override public JigsawPlacementRuntime120.Template template(String id){return templatesById.get(normalize(id));}
    @Override public boolean tolerateMissingTemplates(){return true;}
    public Map<String,JigsawPlacementRuntime120.Template> templates(){return templatesById;}
    private static String normalize(String id){return id.indexOf(':')<0?"minecraft:"+id:id;}
}
