package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import java.util.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Deterministic 1.7 adapter for the three capped Trail Ruins archaeology processors. */
public final class TrailRuinsProcessor120 {
    public static final String LOOT_PROPERTY="htgt6_archaeology_loot";
    public static Context context(List<String> ids){for(String id:ids)if(id!=null&&id.contains("trail_ruins_"))return new Context(id);return null;}
    public static final class Context{
        private final String id;private int common,rare;
        Context(String id){this.id=id;}
        public StructureProcessorRuntime120.State apply(StructureProcessorRuntime120.State input,int x,int y,int z){
            String name=input.name;long seed=StructureProcessorRuntime120.positionalSeed(x,y,z);Random random=new Random(seed);
            if("minecraft:gravel".equals(name)){if(random.nextFloat()<.2F)name="minecraft:dirt";else if(random.nextFloat()<.1F)name="minecraft:coarse_dirt";}
            else if("minecraft:mud_bricks".equals(name)&&random.nextFloat()<.1F)name="minecraft:packed_mud";
            int commonLimit=id.contains("houses")?6:2,rareLimit=id.contains("houses")?3:0;
            if(replaceable(name)&&common<commonLimit){common++;return suspicious("minecraft:archaeology/trail_ruins_common");}
            if(replaceable(name)&&rare<rareLimit){rare++;return suspicious("minecraft:archaeology/trail_ruins_rare");}
            return new StructureProcessorRuntime120.State(name,input.properties);
        }
        public int commonCount(){return common;}public int rareCount(){return rare;}
    }
    private static StructureProcessorRuntime120.State suspicious(String loot){Map<String,String> p=new HashMap<String,String>();p.put("dusted","0");p.put(LOOT_PROPERTY,loot);return new StructureProcessorRuntime120.State("minecraft:suspicious_gravel",p);}
    private static boolean replaceable(String name){return "minecraft:gravel".equals(name)||"minecraft:dirt".equals(name)||"minecraft:coarse_dirt".equals(name)||"minecraft:mud_bricks".equals(name)||"minecraft:packed_mud".equals(name);}
    public static ItemStack loot(String table,long seed){Random random=new Random(seed);
        if(table!=null&&table.endsWith("_rare")){int[] indices={5,6,8,9,10,11,16};return new ItemStack(Backport120Content.potterySherds[indices[random.nextInt(indices.length)]]);}
        ItemStack[] common={new ItemStack(Items.brick),new ItemStack(Items.coal),new ItemStack(Items.wheat),new ItemStack(Items.emerald),new ItemStack(Items.string),new ItemStack(Items.gold_nugget)};
        return common[random.nextInt(common.length)].copy();
    }
    private TrailRuinsProcessor120(){}
}
