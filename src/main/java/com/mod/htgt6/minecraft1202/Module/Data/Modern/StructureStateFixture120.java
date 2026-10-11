package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillagePillageContent;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/** Tables derived from the 1.7 block API; exercised through the production NBT mapper. */
public final class StructureStateFixture120 {
    private static int checks;
    public static void assertContract(){
        checks=0;
        String[] directions={"north","east","south","west"};
        int[] trap={0,3,1,2},horizontal={2,3,0,1},wall={4,1,3,2},side={2,5,3,4};
        for(int d=0;d<4;d++)for(int r=0;r<4;r++)for(boolean lit:new boolean[]{false,true})for(boolean wet:new boolean[]{false,true})
            check("campfire",props("facing",directions[d],"lit",String.valueOf(lit),"waterlogged",String.valueOf(wet)),r,
                    VillagePillageContent.campfire,horizontal[(d+r)&3]|(wet?8:lit?4:0));
        for(int d=0;d<4;d++)for(int r=0;r<4;r++)for(int powered=0;powered<2;powered++){
            Map<String,String> p=props("facing",directions[d],"powered",String.valueOf(powered==1));
            int index=(d+r)&3;
            for(int top=0;top<2;top++)for(int open=0;open<2;open++){
                p.put("half",top==1?"top":"bottom");p.put("open",String.valueOf(open==1));
                check("oak_trapdoor",p,r,Blocks.trapdoor,trap[index]|top*8|open*4);
                check("oak_fence_gate",p,r,Blocks.fence_gate,horizontal[index]|open*4);
            }
            for(int delay=1;delay<=4;delay++){
                p.put("delay",String.valueOf(delay));
                check("repeater",p,r,powered==1?Blocks.powered_repeater:Blocks.unpowered_repeater,horizontal[index]|(delay-1)*4);
            }
            for(int subtract=0;subtract<2;subtract++){
                p.put("mode",subtract==1?"subtract":"compare");
                check("comparator",p,r,Blocks.unpowered_comparator,horizontal[index]|subtract*4|powered*8);
            }
            p.put("face","wall");
            check("stone_button",p,r,Blocks.stone_button,wall[index]|powered*8);
            check("oak_button",p,r,Blocks.wooden_button,wall[index]|powered*8);
            check("wall_torch",p,r,Blocks.torch,wall[index]);
            p.put("lit",String.valueOf(powered==1));
            check("redstone_wall_torch",p,r,powered==1?Blocks.redstone_torch:Blocks.unlit_redstone_torch,wall[index]);
            check("furnace",p,r,powered==1?Blocks.lit_furnace:Blocks.furnace,side[index]);
            for(String face:new String[]{"wall","floor","ceiling"}){
                p.put("face",face);
                int expected=face.equals("wall")?wall[index]:face.equals("floor")?((index&1)==1?6:5):((index&1)==1?0:7);
                check("lever",p,r,Blocks.lever,expected|powered*8);
            }
            p.put("extended",String.valueOf(powered==1));p.put("type",powered==1?"sticky":"normal");
            check("piston",p,r,Blocks.piston,side[index]|powered*8);
            check("sticky_piston",p,r,Blocks.sticky_piston,side[index]|powered*8);
            check("piston_head",p,r,Blocks.piston_head,side[index]|powered*8);
        }
        for(int level=0;level<16;level++){
            Map<String,String> p=props("level",""+level,"power",""+level,"age",""+level);
            check("water",p,0,Blocks.water,level);check("lava",p,0,Blocks.lava,level);
            check("redstone_wire",p,0,Blocks.redstone_wire,level);
            check("light_weighted_pressure_plate",p,0,Blocks.light_weighted_pressure_plate,level);
            check("heavy_weighted_pressure_plate",p,0,Blocks.heavy_weighted_pressure_plate,level);
            check("cactus",p,0,Blocks.cactus,level);check("sugar_cane",p,0,Blocks.reeds,level);
        }
        for(int level=0;level<=8;level++)check("composter",props("level",""+level),0,VillagePillageContent.composter,level);
        for(int level=0;level<8;level++)check("farmland",props("moisture",""+level),0,Blocks.farmland,level);
        for(int r=0;r<4;r++){
            check("hay_block",props("axis","x"),r,Blocks.hay_block,(r&1)==0?4:8);
            check("hay_block",props("axis","z"),r,Blocks.hay_block,(r&1)==0?8:4);
            check("hay_block",props("axis","y"),r,Blocks.hay_block,0);
        }
        for(boolean powered:new boolean[]{false,true}){
            Map<String,String> p=props("lit",""+powered,"powered",""+powered);
            check("redstone_lamp",p,0,powered?Blocks.lit_redstone_lamp:Blocks.redstone_lamp,0);
            check("redstone_torch",p,0,powered?Blocks.redstone_torch:Blocks.unlit_redstone_torch,5);
            check("stone_pressure_plate",p,0,Blocks.stone_pressure_plate,powered?1:0);
            check("oak_pressure_plate",p,0,Blocks.wooden_pressure_plate,powered?1:0);
        }
        check("torch",props(),0,Blocks.torch,5);
        check("repeater",props("facing","north"),0,Blocks.unpowered_repeater,2);
        check("repeater",props("facing","north","delay","99"),0,Blocks.unpowered_repeater,14);
        check("water",props("level","-9"),0,Blocks.water,0);
        check("cactus",props("age","bad"),0,Blocks.cactus,0);
        check("cobbled_deepslate",props(),0,Backport120Content.cobbledDeepslate,0);
        System.out.println("Structure state 48 OK: "+checks+" block/metadata contracts through production mapper");
    }
    private static Map<String,String> props(String... values){Map<String,String> p=new HashMap<String,String>();for(int i=0;i<values.length;i+=2)p.put(values[i],values[i+1]);return p;}
    private static void check(String name,Map<String,String> p,int rotation,Block block,int meta){
        Block actual=FuturaAncientCityGenerator120.mappedStateBlockForFixture(name,p,rotation);
        int got=FuturaAncientCityGenerator120.mappedStateMetaForFixture(name,p,rotation);
        if(actual!=block||got!=meta)throw new IllegalStateException(name+p+" rotation="+rotation+" expected meta="+meta+", actual="+got);
        checks++;
    }
    private StructureStateFixture120(){}
}
