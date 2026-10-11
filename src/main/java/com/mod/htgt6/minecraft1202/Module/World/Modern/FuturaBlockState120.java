package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import java.util.Locale;

/** Explicit legacy state translations shared by configured feature targets and providers. */
public final class FuturaBlockState120 {
    public final Block block;public final int metadata;
    private FuturaBlockState120(Block block,int metadata){this.block=block;this.metadata=metadata;}
    public static FuturaBlockState120 of(String key){
        String name=key.substring(key.indexOf(':')+1);int meta=0;Block block;
        if(name.equals("grass_block"))block=Blocks.grass;
        else if(name.equals("snow_block"))block=Blocks.snow;
        else if(name.equals("coarse_dirt")||name.equals("podzol")){block=Blocks.dirt;meta=name.equals("podzol")?2:1;}
        else if(name.equals("cave_air")||name.equals("void_air"))block=Blocks.air;
        else if(name.equals("granite")||name.equals("diorite")||name.equals("andesite")){block=Blocks.stone;meta=name.equals("granite")?1:name.equals("diorite")?3:5;}
        else if(wood(name)>=0){int wood=wood(name);boolean log=name.endsWith("_log");block=log?(wood<4?Blocks.log:Blocks.log2):(wood<4?Blocks.leaves:Blocks.leaves2);meta=wood<4?wood:wood-4;}
        else{
            Object vanilla=Block.blockRegistry.getObject("minecraft:"+name);block=vanilla instanceof Block?(Block)vanilla:null;
            if(block==null||block==Blocks.air&&!name.equals("air")){
                StringBuilder field=new StringBuilder();boolean uppercase=false;
                for(char ch:name.toCharArray()){if(ch=='_'){uppercase=true;continue;}field.append(uppercase?Character.toUpperCase(ch):ch);uppercase=false;}
                try{block=(Block)Backport120Content.class.getField(field.toString()).get(null);}
                catch(ReflectiveOperationException e){throw new IllegalArgumentException("Unmapped feature state: "+key,e);}
            }
        }
        if(block==null)throw new IllegalStateException("Feature block not initialized: "+key);
        return new FuturaBlockState120(block,meta);
    }
    private static int wood(String name){
        String[] species={"oak","spruce","birch","jungle","acacia","dark_oak"};
        for(int i=0;i<species.length;i++)if(name.equals(species[i]+"_log")||name.equals(species[i]+"_leaves"))return i;
        return -1;
    }
    public static FuturaBlockState120 of(JsonObject state){
        FuturaBlockState120 result=of(state.get("Name").getAsString());JsonObject p=state.getAsJsonObject("Properties");
        if(p==null)return result;int meta=result.metadata;
        if((result.block==Blocks.log||result.block==Blocks.log2)&&p.has("axis"))meta|=p.get("axis").getAsString().equals("x")?4:p.get("axis").getAsString().equals("z")?8:0;
        if((result.block==Blocks.leaves||result.block==Blocks.leaves2)&&p.has("persistent")&&p.get("persistent").getAsBoolean())meta|=4;
        return new FuturaBlockState120(result.block,meta);
    }
    public static boolean predicate(World w,JsonObject p,int x,int y,int z){
        if(p.has("offset")){JsonArray o=p.getAsJsonArray("offset");x+=o.get(0).getAsInt();y+=o.get(1).getAsInt();z+=o.get(2).getAsInt();}
        String type=p.get("type").getAsString();
        if(type.equals("minecraft:true"))return true;
        if(type.equals("minecraft:inside_world_bounds"))return y>=0&&y<w.getHeight();
        if(type.equals("minecraft:not"))return !predicate(w,p.getAsJsonObject("predicate"),x,y,z);
        if(type.equals("minecraft:all_of")){for(JsonElement e:p.getAsJsonArray("predicates"))if(!predicate(w,e.getAsJsonObject(),x,y,z))return false;return true;}
        if(type.equals("minecraft:any_of")){for(JsonElement e:p.getAsJsonArray("predicates"))if(predicate(w,e.getAsJsonObject(),x,y,z))return true;return false;}
        if(type.equals("minecraft:would_survive"))return WorldGenFuturaVanillaTree120.saplingSurvives(w,p.getAsJsonObject("state").get("Name").getAsString(),x,y,z);
        Block actual=w.getBlock(x,y,z);
        if(type.equals("minecraft:has_sturdy_face")){
            ForgeDirection face=ForgeDirection.valueOf(p.get("direction").getAsString().toUpperCase(Locale.ROOT));
            if(face==ForgeDirection.UNKNOWN)throw new IllegalArgumentException("Invalid sturdy face: "+p);
            if(y<0||y>=w.getHeight())return false;
            // Forge 1.7.10 omits the full underside of bottom slabs.
            if(actual instanceof BlockSlab)return actual.isOpaqueCube()
                    ||face==((w.getBlockMetadata(x,y,z)&8)==0?ForgeDirection.DOWN:ForgeDirection.UP);
            return actual.isSideSolid(w,x,y,z,face);
        }
        if(type.equals("minecraft:solid"))return actual.getMaterial().isSolid();
        if(type.equals("minecraft:replaceable"))return actual.getMaterial().isReplaceable();
        if(type.equals("minecraft:matching_blocks")){
            for(JsonElement e:array(p.get("blocks"))){FuturaBlockState120 target=of(e.getAsString());
                if(actual==target.block&&(target.block!=Blocks.dirt&&target.block!=Blocks.stone||w.getBlockMetadata(x,y,z)==target.metadata))return true;}
            return false;
        }
        if(type.equals("minecraft:matching_fluids")){
            for(JsonElement e:array(p.get("fluids"))){String name=e.getAsString();
                if(name.equals("minecraft:empty")&&!actual.getMaterial().isLiquid())return true;
                boolean water=name.endsWith("water"),lava=name.endsWith("lava");
                if((water&&actual.getMaterial()==net.minecraft.block.material.Material.water||lava&&actual.getMaterial()==net.minecraft.block.material.Material.lava)
                        &&(name.contains("flowing_")?w.getBlockMetadata(x,y,z)!=0:w.getBlockMetadata(x,y,z)==0))return true;}
            return false;
        }
        throw new IllegalArgumentException("Unsupported feature predicate: "+p);
    }
    private static JsonArray array(JsonElement element){if(element.isJsonArray())return element.getAsJsonArray();JsonArray a=new JsonArray();a.add(element);return a;}
}
