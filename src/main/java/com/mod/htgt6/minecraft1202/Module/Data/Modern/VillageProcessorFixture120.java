package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Rule-processor regression over states present in the official plains village NBT set. */
public final class VillageProcessorFixture120 {
    private static final long EXPECTED=0x84e89385c9e36ff8L;
    public static long assertContract(){
        long hash=0xcbf29ce484222325L;
        String[] processors={"minecraft:mossify_10_percent","minecraft:mossify_20_percent",
                "minecraft:mossify_70_percent","minecraft:farm_plains","minecraft:farm_desert",
                "minecraft:farm_savanna","minecraft:farm_snowy","minecraft:farm_taiga",
                "minecraft:street_plains","minecraft:street_savanna","minecraft:street_snowy_or_taiga",
                "minecraft:zombie_plains","minecraft:zombie_desert","minecraft:zombie_savanna",
                "minecraft:zombie_snowy","minecraft:zombie_taiga"};
        for(String id:processors)hash=fnv(hash,StructureProcessorRuntime120.load(id).size());
        StructureProcessorRuntime120.State cobble=new StructureProcessorRuntime120.State("minecraft:cobblestone");
        StructureProcessorRuntime120.State air=new StructureProcessorRuntime120.State("minecraft:air");
        for(int x=0;x<128;x++)hash=fnv(hash,StructureProcessorRuntime120.apply(
                Arrays.asList("minecraft:mossify_10_percent"),cobble,air,x,70,11).name.hashCode());
        StructureProcessorRuntime120.State street=StructureProcessorRuntime120.apply(
                Arrays.asList("minecraft:street_plains"),new StructureProcessorRuntime120.State("minecraft:dirt_path"),
                new StructureProcessorRuntime120.State("minecraft:water"),4,64,9);
        if(!"minecraft:oak_planks".equals(street.name))throw new IllegalStateException("street_plains water rule failed");
        hash=fnv(hash,street.name.hashCode());
        StructureProcessorRuntime120.State door=StructureProcessorRuntime120.apply(
                Arrays.asList("minecraft:zombie_plains"),new StructureProcessorRuntime120.State("minecraft:oak_door"),
                air,7,70,3);
        if(!"minecraft:air".equals(door.name))throw new IllegalStateException("zombie doors tag rule failed");
        hash=fnv(hash,door.name.hashCode());
        Map<String,String> paneProperties=new HashMap<String,String>();paneProperties.put("east","false");
        paneProperties.put("north","true");paneProperties.put("south","true");paneProperties.put("waterlogged","false");
        paneProperties.put("west","false");
        StructureProcessorRuntime120.State pane=StructureProcessorRuntime120.apply(
                Arrays.asList("minecraft:zombie_plains"),new StructureProcessorRuntime120.State("minecraft:glass_pane",paneProperties),
                air,19,71,-5);
        hash=fnv(hash,pane.name.hashCode());hash=fnv(hash,pane.properties.hashCode());
        if(EXPECTED!=0L&&hash!=EXPECTED)throw new IllegalStateException("Village processor fixture changed: expected "
                +Long.toHexString(EXPECTED)+", got "+Long.toHexString(hash));
        return hash;
    }
    private static long fnv(long hash,int value){return(hash^value)*0x100000001b3L;}
    public static void main(String[] ignored){System.out.println(Long.toHexString(assertContract()));}
    private VillageProcessorFixture120(){ }
}
