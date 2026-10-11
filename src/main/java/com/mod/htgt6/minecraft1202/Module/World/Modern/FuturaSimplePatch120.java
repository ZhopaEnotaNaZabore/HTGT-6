package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Bounded adapter for the simple-block random patches in the bundled 1.20.2 data. */
final class FuturaSimplePatch120 {
    private FuturaSimplePatch120() {}

    /** True means the configured feature was handled, even when no attempt survived. */
    static boolean generate(World w, Random r, String key, JsonObject config, int x, int y, int z) {
        String name=key.replace("minecraft:", "");
        if(name.equals("forest_flowers")) {
            JsonArray choices=config.getAsJsonArray("features");
            JsonObject selected=choices.get(r.nextInt(choices.size())).getAsJsonObject();
            if(selected.getAsJsonArray("placement").size()!=0)throw new IllegalStateException("Unexpected forest selector modifier");
            return generate(w,r,"flower_selected",selected.getAsJsonObject("feature").getAsJsonObject("config"),x,y,z);
        }
        if (!(name.startsWith("flower_") || name.equals("patch_sunflower") || name.equals("patch_grass")
                || name.equals("patch_grass_jungle") || name.equals("patch_taiga_grass")
                || name.equals("patch_berry_bush") || name.equals("patch_melon")
                || name.equals("patch_pumpkin") || name.equals("patch_tall_grass")
                || name.equals("patch_large_fern") || name.equals("patch_dead_bush")
                || name.equals("patch_waterlily") || name.equals("patch_brown_mushroom")
                || name.equals("patch_red_mushroom"))) return false;
        JsonObject placed=config.getAsJsonObject("feature");
        JsonObject provider=placed.getAsJsonObject("feature").getAsJsonObject("config").getAsJsonObject("to_place");
        int spread=config.get("xz_spread").getAsInt()+1, vertical=config.get("y_spread").getAsInt()+1;
        for (int i=0;i<config.get("tries").getAsInt();i++) {
            int px=x+r.nextInt(spread)-r.nextInt(spread);
            int py=y+r.nextInt(vertical)-r.nextInt(vertical);
            int pz=z+r.nextInt(spread)-r.nextInt(spread);
            boolean accepted=true;
            for (JsonElement entry:placed.getAsJsonArray("placement")) {
                JsonObject modifier=entry.getAsJsonObject();
                if (!modifier.get("type").getAsString().equals("minecraft:block_predicate_filter"))
                    throw new IllegalStateException("Unsupported simple patch modifier: "+modifier);
                if (!matches(w,modifier.getAsJsonObject("predicate"),px,py,pz)) { accepted=false; break; }
            }
            if (!accepted) continue;
            // Vanilla samples the state after placement filters, before survival/height checks.
            JsonObject state=FuturaFlowerProvider120.state(r,provider,px,py,pz);
            String blockName=state.get("Name").getAsString();
            Block block; int meta=0;
            if (blockName.equals("minecraft:grass") || blockName.equals("minecraft:fern")) {
                block=Blocks.tallgrass; meta=blockName.endsWith(":fern")?2:1;
            } else if (blockName.equals("minecraft:sunflower") || blockName.equals("minecraft:tall_grass")
                    || blockName.equals("minecraft:large_fern") || blockName.equals("minecraft:lilac")
                    || blockName.equals("minecraft:rose_bush") || blockName.equals("minecraft:peony")) {
                block=Blocks.double_plant; meta=blockName.endsWith(":sunflower")?0:blockName.endsWith(":large_fern")?3:blockName.endsWith(":lilac")?1:
                        blockName.endsWith(":rose_bush")?4:blockName.endsWith(":peony")?5:2;
            } else if (blockName.equals("minecraft:sweet_berry_bush")) {
                block=Backport120Content.sweetBerryBush; meta=state.getAsJsonObject("Properties").get("age").getAsInt();
            } else if (blockName.equals("minecraft:melon")) block=Blocks.melon_block;
            else if (blockName.equals("minecraft:pumpkin")) block=Blocks.pumpkin;
            else if (blockName.equals("minecraft:dead_bush")) block=Blocks.deadbush;
            else if (blockName.equals("minecraft:lily_pad")) block=Blocks.waterlily;
            else if (blockName.equals("minecraft:brown_mushroom")) block=Blocks.brown_mushroom;
            else if (blockName.equals("minecraft:red_mushroom")) block=Blocks.red_mushroom;
            else if(blockName.equals("minecraft:cornflower"))block=Backport120Content.cornflower;
            else if(blockName.equals("minecraft:lily_of_the_valley"))block=Backport120Content.lilyOfTheValley;
            else if(blockName.equals("minecraft:dandelion"))block=Blocks.yellow_flower;
            else if(blockName.equals("minecraft:pink_petals")) {
                block=Backport120Content.pinkPetals;JsonObject props=state.getAsJsonObject("Properties");
                String face=props.get("facing").getAsString();
                meta=(face.equals("north")?2:face.equals("east")?3:face.equals("south")?0:1)
                        |((props.get("flower_amount").getAsInt()-1)<<2);
            } else {
                String[] flowers={"poppy","blue_orchid","allium","azure_bluet","red_tulip","orange_tulip","white_tulip","pink_tulip","oxeye_daisy"};
                block=null;for(int f=0;f<flowers.length;f++)if(blockName.equals("minecraft:"+flowers[f])){block=Blocks.red_flower;meta=f;break;}
                if(block==null)throw new IllegalStateException("Unsupported simple patch state: "+state);
            }
            if (py<0 || py>=w.getHeight()) continue;
            if (block instanceof BlockBush && !((BlockBush)block).canBlockStay(w,px,py,pz)) continue;
            if (block==Backport120Content.pinkPetals && !block.canPlaceBlockAt(w,px,py,pz))continue;
            if (block==Blocks.double_plant) {
                if (py+1>=w.getHeight() || !w.isAirBlock(px,py+1,pz)) continue;
                w.setBlock(px,py,pz,block,meta,2);
                w.setBlock(px,py+1,pz,block,8,2);
            } else w.setBlock(px,py,pz,block,meta,2);
        }
        return true;
    }

    private static JsonArray values(JsonElement value) {
        if (value.isJsonArray()) return value.getAsJsonArray();
        JsonArray result=new JsonArray(); result.add(value); return result;
    }

    static boolean matches(World w,JsonObject p,int x,int y,int z) {
        if (p.has("offset")) {
            JsonArray offset=p.getAsJsonArray("offset"); x+=offset.get(0).getAsInt();
            y+=offset.get(1).getAsInt(); z+=offset.get(2).getAsInt();
        }
        String type=p.get("type").getAsString();
        if (type.equals("minecraft:all_of")) {
            for (JsonElement e:p.getAsJsonArray("predicates")) if (!matches(w,e.getAsJsonObject(),x,y,z)) return false;
            return true;
        }
        if (type.equals("minecraft:any_of")) {
            for(JsonElement e:p.getAsJsonArray("predicates"))if(matches(w,e.getAsJsonObject(),x,y,z))return true;
            return false;
        }
        if(type.equals("minecraft:would_survive")) {
            String name=p.getAsJsonObject("state").get("Name").getAsString();
            if(name.equals("minecraft:cactus"))return FuturaColumnPlantSurvival120.cactus(Blocks.cactus.canBlockStay(w,x,y,z),w,x,y,z);
            if(name.equals("minecraft:sugar_cane"))return FuturaColumnPlantSurvival120.cane(Blocks.reeds.canBlockStay(w,x,y,z),w,x,y,z);
            throw new IllegalArgumentException("Unsupported survival state: "+name);
        }
        if (type.equals("minecraft:not")) return !matches(w,p.getAsJsonObject("predicate"),x,y,z);
        Block block=w.getBlock(x,y,z);
        if (type.equals("minecraft:replaceable")) return block.getMaterial().isReplaceable();
        if (type.equals("minecraft:matching_fluids")) {
            for (JsonElement e:values(p.get("fluids"))) {
                String name=e.getAsString();
                if (name.equals("minecraft:empty") && !block.getMaterial().isLiquid()) return true;
                if(block.getMaterial()==net.minecraft.block.material.Material.water
                        && (name.equals("minecraft:water")&&w.getBlockMetadata(x,y,z)==0
                        ||name.equals("minecraft:flowing_water")&&w.getBlockMetadata(x,y,z)!=0))return true;
            }
            return false;
        }
        if (type.equals("minecraft:matching_blocks")) {
            for (JsonElement e:values(p.get("blocks"))) {
                String name=e.getAsString();
                if (name.equals("minecraft:air") && block==Blocks.air
                        || name.equals("minecraft:grass_block") && block==Blocks.grass
                        || name.equals("minecraft:podzol") && block==Blocks.dirt && w.getBlockMetadata(x,y,z)==2) return true;
            }
            return false;
        }
        throw new IllegalStateException("Unsupported simple patch predicate: "+type);
    }
}
