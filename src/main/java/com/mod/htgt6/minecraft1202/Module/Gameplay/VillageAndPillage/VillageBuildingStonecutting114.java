package com.mod.htgt6.minecraft1202.Module.Gameplay.VillageAndPillage;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageBuildingContent114;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent;

/** Exact official input/output/count triples for the newly supplied shapes. */
public final class VillageBuildingStonecutting114 {
    private static JsonArray recipes;
    public static synchronized JsonArray recipes(){
        if(recipes==null)try(InputStream in=VillageBuildingStonecutting114.class.getResourceAsStream("/assets/htgt6/recipes/village_building114_stonecutting.json")){
            if(in==null)throw new IllegalStateException("Missing 1.14 stonecutting data");
            recipes=new JsonParser().parse(new InputStreamReader(in,"UTF-8")).getAsJsonArray();
        }catch(IOException e){throw new IllegalStateException(e);}
        return recipes;
    }
    public static ItemStack ingredient(String id){
        String name=id.substring(id.indexOf(':')+1);Block b;int meta=0;
        switch(name){
            case "stone":b=Blocks.stone;break;
            case "stone_bricks":b=Blocks.stonebrick;break;
            case "mossy_stone_bricks":b=Blocks.stonebrick;meta=1;break;
            case "bricks":b=Blocks.brick_block;break;
            case "nether_bricks":b=Blocks.nether_brick;break;
            case "red_nether_bricks":b=FrostburnContent.redNetherBrick;break;
            case "end_stone":b=Blocks.end_stone;break;
            case "end_stone_bricks":b=CombatContent.endBricks;break;
            case "sandstone":b=Blocks.sandstone;break;
            case "cut_sandstone":b=Blocks.sandstone;meta=2;break;
            case "red_sandstone":b=BountifulContent.redSandstone;break;
            case "cut_red_sandstone":b=BountifulContent.redSandstone;meta=2;break;
            case "smooth_red_sandstone":b=EtFVanillaGapContent120.smoothRedSandstone;break;
            case "smooth_quartz":b=EtFVanillaGapContent120.smoothQuartz;break;
            case "prismarine":b=BountifulContent.prismarine;break;
            default:throw new IllegalArgumentException("Unsupported 1.14 stonecutting input "+id);
        }
        return new ItemStack(b,1,meta);
    }
    public static void append(ItemStack input,List<ItemStack> outputs){
        for(JsonElement e:recipes()){
            JsonObject r=e.getAsJsonObject();ItemStack expected=ingredient(r.getAsJsonObject("ingredient").get("item").getAsString());
            if(expected.getItem()==null||!expected.isItemEqual(input))continue;
            String name=r.get("result").getAsString().substring(10);
            ItemStack result=new ItemStack(VillageBuildingContent114.SHAPES.get(name).block,r.get("count").getAsInt());
            boolean present=false;for(ItemStack old:outputs)if(old.isItemEqual(result)){present=true;break;}
            if(!present)outputs.add(result);
        }
    }
    private VillageBuildingStonecutting114(){}
}
