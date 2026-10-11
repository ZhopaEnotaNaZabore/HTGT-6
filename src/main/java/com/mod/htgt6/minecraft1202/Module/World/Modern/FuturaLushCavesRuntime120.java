package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaHeight120;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Bounded codec/runtime for the VEGETAL_DECORATION entries in the official
 * 1.20.2 Lush Caves biome.  The JSON is a normalized lossless projection of
 * the configured/placed features used here, not a second decoration recipe.
 */
public final class FuturaLushCavesRuntime120 {
    private static final String RESOURCE="/assets/htgt6/worldgen/lush_caves/1_20_2.json";
    private static final Config CONFIG=load();
    private static final ThreadLocal<int[]> POPULATION_BOUNDS=new ThreadLocal<int[]>();
    private final FuturaMountainDensityRouter120 terrain;

    public FuturaLushCavesRuntime120(FuturaMountainDensityRouter120 terrain){
        if(terrain==null)throw new NullPointerException("terrain");
        this.terrain=terrain;
    }

    public void populate(World world,Random random,int chunkX,int chunkZ,int generationHeight){
        populateFeature(world,random,chunkX,chunkZ,generationHeight,null);
    }
    public void populateFeature(World world,Random random,int chunkX,int chunkZ,int generationHeight,String selected){
        if(!(world.getWorldChunkManager() instanceof FuturaWorldChunkManager120))return;
        int originX=chunkX<<4,originZ=chunkZ<<4;
        int margin=16*FuturaGenerationRegion120.WRITE_RADIUS;
        POPULATION_BOUNDS.set(new int[]{originX-margin,originX+15+margin,originZ-margin,originZ+15+margin});
        FuturaUndergroundFeatures120 biomes=new FuturaUndergroundFeatures120(terrain);
        try{for(Feature feature:CONFIG.features){
            if(selected!=null&&!selected.equals(feature.id))continue;
            // Shared surface patches have one owner and one RNG stream in every biome.
            if(FuturaVegetationPlacement120.supported(feature.id.substring(10)))continue;
            FuturaPlacementExecutor120.execute(world,chunkX,chunkZ,9,feature.id,biomes,(w,r,x,y,z)->{
                if("minecraft:glow_lichen".equals(feature.id)){
                    if(w.isAirBlock(x,y,z))placeGlowLichen(w,r,x,y,z);
                }else if("minecraft:classic_vines_cave_feature".equals(feature.id)){
                    FuturaMiscFeatures120.generate(w,r,"minecraft:vines",null,x,y,z);
                }else generate(feature.id,w,r,x,y,z);
            });
        }}finally{POPULATION_BOUNDS.remove();}
    }

    public static boolean owns(String key){for(Feature feature:CONFIG.features)if(feature.id.equals(key))return true;return false;}

    private static void generate(String id,World world,Random random,int x,int y,int z){
        if("minecraft:lush_caves_vegetation".equals(id))vegetationPatch(world,random,x,y,z,false,false);
        else if("minecraft:lush_caves_ceiling_vegetation".equals(id))vegetationPatch(world,random,x,y,z,true,false);
        else if("minecraft:lush_caves_clay".equals(id))vegetationPatch(world,random,x,y,z,false,true);
        else if("minecraft:cave_vines".equals(id))caveVine(world,random,x,y,z,false);
        else if("minecraft:spore_blossom".equals(id)&&world.isSideSolid(x,y+1,z,ForgeDirection.DOWN))
            world.setBlock(x,y,z,Backport120Content.sporeBlossom,0,2);
        else if("minecraft:rooted_azalea_tree".equals(id)&&insideMargin(x,z,4))rootSystem(world,random,x,y,z);
    }

    private static void vegetationPatch(World world,Random random,int x,int y,int z,
                                        boolean ceiling,boolean clay){
        Patch p=clay?(random.nextBoolean()?CONFIG.clay:CONFIG.clayPool)
                :(ceiling?CONFIG.mossCeiling:CONFIG.moss);
        // Patch depth extends into the supporting surface: down for floors,
        // up for ceilings. Vegetation occupies the opposite adjacent air.
        int radius=between(random,p.radiusMin,p.radiusMax),sign=ceiling?1:-1;
        for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(!inside(x+dx,z+dz))continue;
            if(dx*dx+dz*dz>radius*radius||isEdge(dx,dz,radius)&&random.nextFloat()>p.edgeChance)continue;
            int surface=findSurface(world,x+dx,y,z+dz,ceiling,p.verticalRange);
            if(surface<1)continue;
            int depth=between(random,p.depthMin,p.depthMax);
            for(int d=0;d<depth;d++){
                int yy=surface+sign*d;
                Block old=world.getBlock(x+dx,yy,z+dz);
                if(replaceableGround(old,clay))world.setBlock(x+dx,yy,z+dz,
                        clay?Blocks.clay:Backport120Content.mossBlock,0,2);
            }
            if(random.nextFloat()<p.extraBottomChance){
                int yy=surface+sign*depth;
                if(replaceableGround(world.getBlock(x+dx,yy,z+dz),clay))
                    world.setBlock(x+dx,yy,z+dz,clay?Blocks.clay:Backport120Content.mossBlock,0,2);
            }
            if(random.nextFloat()<p.vegetationChance){
                int plantY=surface-sign;
                if(ceiling)caveVine(world,random,x+dx,plantY,z+dz,true);
                else if(clay)dripleaf(world,random,x+dx,plantY,z+dz);
                else mossVegetation(world,random,x+dx,plantY,z+dz);
            }
        }
    }

    private static int findSurface(World world,int x,int y,int z,boolean ceiling,int range){
        int dy=ceiling?1:-1,cursor=y;
        for(int i=0;i<=range;i++,cursor+=dy){
            if(!world.isAirBlock(x,cursor,z))return cursor;
        }
        return -1;
    }

    private static boolean replaceableGround(Block b,boolean clay){
        return b==Blocks.stone||b==Backport120Content.deepslate||b==Blocks.dirt||b==Blocks.grass
                ||b==Blocks.clay||b==Blocks.gravel||b==Backport120Content.rootedDirt
                ||(!clay&&b==Backport120Content.mossBlock);
    }

    private static void mossVegetation(World world,Random random,int x,int y,int z){
        if(!world.isAirBlock(x,y,z))return;
        int roll=random.nextInt(96);
        if(roll<4)world.setBlock(x,y,z,Backport120Content.floweringAzalea,0,2);
        else if(roll<11)world.setBlock(x,y,z,Backport120Content.azalea,0,2);
        else if(roll<36)world.setBlock(x,y,z,Backport120Content.mossCarpet,0,2);
        else if(roll<86)world.setBlock(x,y,z,Blocks.tallgrass,1,2);
        else if(y+1<FuturaHeight120.interactionHeight(world)&&world.isAirBlock(x,y+1,z)){
            world.setBlock(x,y,z,Blocks.double_plant,2,2);
            world.setBlock(x,y+1,z,Blocks.double_plant,10,2);
        }
    }

    private static void caveVine(World world,Random random,int x,int y,int z,boolean shortVine){
        if(!world.isAirBlock(x,y,z)||!world.isSideSolid(x,y+1,z,ForgeDirection.DOWN))return;
        int length;
        if(shortVine)length=random.nextInt(6)==0?between(random,1,7):between(random,0,3);
        else{
            int roll=random.nextInt(15);
            length=roll<2?between(random,0,19):roll<5?between(random,0,2):between(random,0,6);
        }
        length=Math.max(1,length+1);
        int placed=0;
        while(placed<length&&world.isAirBlock(x,y-placed,z))placed++;
        if(placed==0)return;
        for(int i=0;i<placed-1;i++)world.setBlock(x,y-i,z,Backport120Content.caveVinesPlant,
                random.nextInt(5)==0?8:0,2);
        world.setBlock(x,y-placed+1,z,Backport120Content.caveVines,
                between(random,23,25)&7|(random.nextInt(5)==0?8:0),2);
    }

    private static void dripleaf(World world,Random random,int x,int y,int z){
        if(!world.isAirBlock(x,y,z))return;
        int facing=random.nextInt(4);
        if(random.nextInt(5)==0&&world.isAirBlock(x,y+1,z)){
            world.setBlock(x,y,z,Backport120Content.smallDripleaf,facing,2);
            world.setBlock(x,y+1,z,Backport120Content.smallDripleaf,facing|4,2);
            return;
        }
        int stems=random.nextInt(3)==0?0:random.nextInt(5),yy=y;
        for(int i=0;i<stems&&world.isAirBlock(x,yy,z);i++,yy++)
            world.setBlock(x,yy,z,Backport120Content.bigDripleafStem,facing,2);
        if(world.isAirBlock(x,yy,z))world.setBlock(x,yy,z,Backport120Content.bigDripleaf,facing,2);
    }

    private static void rootSystem(World world,Random random,int x,int caveY,int z){
        int surface=-1;
        for(int dy=1;dy<=CONFIG.rootColumnMax;dy++){
            int y=caveY+dy;
            if(y+CONFIG.requiredTreeSpace>=FuturaHeight120.interactionHeight(world))break;
            if(world.isAirBlock(x,y,z)&&replaceableGround(world.getBlock(x,y-1,z),false)){
                boolean clear=true;
                for(int space=0;space<CONFIG.requiredTreeSpace;space++)clear&=world.isAirBlock(x,y+space,z);
                if(clear){surface=y;break;}
            }
        }
        if(surface<0)return;
        for(int y=caveY+1;y<surface;y++)if(replaceableGround(world.getBlock(x,y,z),false))
            world.setBlock(x,y,z,Backport120Content.rootedDirt,0,2);
        for(int i=0;i<CONFIG.rootAttempts;i++){
            int rx=x+between(random,-CONFIG.rootRadius,CONFIG.rootRadius);
            int rz=z+between(random,-CONFIG.rootRadius,CONFIG.rootRadius);
            if(!inside(rx,rz))continue;
            int ry=between(random,caveY,Math.max(caveY,surface-1));
            if(replaceableGround(world.getBlock(rx,ry,rz),false))world.setBlock(rx,ry,rz,Backport120Content.rootedDirt,0,2);
        }
        for(int i=0;i<CONFIG.hangingAttempts;i++){
            int rx=x+between(random,-CONFIG.hangingRadius,CONFIG.hangingRadius);
            int rz=z+between(random,-CONFIG.hangingRadius,CONFIG.hangingRadius);
            if(!inside(rx,rz))continue;
            int ry=caveY+between(random,0,CONFIG.hangingSpan);
            if(world.isAirBlock(rx,ry,rz)&&world.isSideSolid(rx,ry+1,rz,ForgeDirection.DOWN))
                world.setBlock(rx,ry,rz,Backport120Content.hangingRoots,0,2);
        }
        // Tree implementation is shared with the official azalea configured_feature runtime.
        FuturaTreeFeatureRuntime120.azalea(world,random,x,surface,z);
    }

    private static void placeGlowLichen(World world,Random random,int x,int y,int z){
        int[] dx={0,-1,0,1},dz={-1,0,1,0},meta={4,2,1,8};
        ForgeDirection[] face={ForgeDirection.SOUTH,ForgeDirection.EAST,
                ForgeDirection.NORTH,ForgeDirection.WEST};
        int start=random.nextInt(4);
        for(int i=0;i<4;i++){
            int n=(start+i)&3;
            if(world.isSideSolid(x+dx[n],y,z+dz[n],face[n])){
                world.setBlock(x,y,z,Backport120Content.glowLichen,meta[n],2);return;
            }
        }
    }

    public static Config config(){return CONFIG;}
    public static long configFingerprint(){
        long h=0xcbf29ce484222325L;
        h=fnv(h,CONFIG.minAboveBottom);h=fnv(h,CONFIG.maxAbsolute);h=fnv(h,CONFIG.scanSteps);
        for(Feature f:CONFIG.features){h=fnv(h,f.id);h=fnv(h,f.min);h=fnv(h,f.max);
            h=fnv(h,f.scan==null?"":f.scan);h=fnv(h,f.offsetY);h=fnv(h,f.oceanFloorOffset);
            h=fnv(h,f.rarity);h=fnv(h,f.heightmap?1:0);}
        h=patchFingerprint(h,CONFIG.moss);h=patchFingerprint(h,CONFIG.mossCeiling);
        h=patchFingerprint(h,CONFIG.clay);h=patchFingerprint(h,CONFIG.clayPool);
        h=fnv(h,CONFIG.rootColumnMax);h=fnv(h,CONFIG.requiredTreeSpace);h=fnv(h,CONFIG.rootRadius);
        h=fnv(h,CONFIG.rootAttempts);h=fnv(h,CONFIG.hangingRadius);h=fnv(h,CONFIG.hangingAttempts);
        return fnv(h,CONFIG.hangingSpan);
    }
    private static long patchFingerprint(long h,Patch p){h=fnv(h,p.depthMin);h=fnv(h,p.depthMax);
        h=fnv(h,p.radiusMin);h=fnv(h,p.radiusMax);h=fnv(h,p.verticalRange);
        h=fnv(h,Float.floatToIntBits(p.edgeChance));h=fnv(h,Float.floatToIntBits(p.extraBottomChance));
        return fnv(h,Float.floatToIntBits(p.vegetationChance));}
    private static long fnv(long h,String value){for(int i=0;i<value.length();i++)h=fnv(h,value.charAt(i));return h;}
    private static long fnv(long h,int value){for(int i=0;i<4;i++){h^=(value>>>(i*8))&255L;h*=0x100000001b3L;}return h;}
    private static boolean isEdge(int x,int z,int r){return Math.abs(x)==r||Math.abs(z)==r;}
    private static boolean inside(int x,int z){int[] b=POPULATION_BOUNDS.get();
        return b==null||(x>=b[0]&&x<=b[1]&&z>=b[2]&&z<=b[3]);}
    private static boolean insideMargin(int x,int z,int margin){int[] b=POPULATION_BOUNDS.get();
        return b==null||(x>=b[0]+margin&&x<=b[1]-margin&&z>=b[2]+margin&&z<=b[3]-margin);}
    private static int between(Random r,int min,int max){return min+r.nextInt(max-min+1);}

    private static Config load(){
        InputStream input=FuturaLushCavesRuntime120.class.getResourceAsStream(RESOURCE);
        if(input==null)throw new IllegalStateException("Missing "+RESOURCE);
        try{
            JsonObject root=new JsonParser().parse(new InputStreamReader(input,"UTF-8")).getAsJsonObject();
            JsonObject height=root.getAsJsonObject("height");
            List<Feature> features=new ArrayList<Feature>();
            for(JsonElement e:root.getAsJsonArray("features"))features.add(new Feature(e.getAsJsonObject()));
            JsonObject configured=root.getAsJsonObject("configured");
            return new Config(height,features,configured);
        }catch(Exception e){throw new IllegalStateException("Invalid official Lush Caves config",e);}
        finally{try{input.close();}catch(Exception ignored){}}
    }

    public static final class Feature{
        public final String id,scan;public final int min,max,offsetY,oceanFloorOffset,rarity;
        public final boolean heightmap;
        Feature(JsonObject o){id=o.get("id").getAsString();scan=o.has("scan")?o.get("scan").getAsString():null;
            min=o.has("count_min")?o.get("count_min").getAsInt():o.get("count").getAsInt();
            max=o.has("count_max")?o.get("count_max").getAsInt():min;
            offsetY=o.has("offset_y")?o.get("offset_y").getAsInt():0;
            oceanFloorOffset=o.has("ocean_floor_max_offset")?o.get("ocean_floor_max_offset").getAsInt():0;
            rarity=o.has("rarity")?o.get("rarity").getAsInt():1;heightmap=o.has("heightmap")&&o.get("heightmap").getAsBoolean();}
        int count(Random r){return between(r,min,max);}
    }
    public static final class Patch{
        final int depthMin,depthMax,radiusMin,radiusMax,verticalRange;
        final float edgeChance,extraBottomChance,vegetationChance;
        Patch(JsonObject o){depthMin=o.has("depth_min")?o.get("depth_min").getAsInt():o.get("depth").getAsInt();
            depthMax=o.has("depth_max")?o.get("depth_max").getAsInt():depthMin;
            radiusMin=o.get("radius_min").getAsInt();radiusMax=o.get("radius_max").getAsInt();
            verticalRange=o.get("vertical_range").getAsInt();edgeChance=o.get("edge_chance").getAsFloat();
            extraBottomChance=o.has("extra_bottom_chance")?o.get("extra_bottom_chance").getAsFloat():0;
            vegetationChance=o.get("vegetation_chance").getAsFloat();}
    }
    public static final class Config{
        public final int minAboveBottom,maxAbsolute,scanSteps,rootColumnMax,requiredTreeSpace,
                rootRadius,rootAttempts,hangingRadius,hangingAttempts,hangingSpan;
        public final List<Feature> features;final Patch moss,mossCeiling,clay,clayPool;
        Config(JsonObject height,List<Feature> features,JsonObject c){
            minAboveBottom=height.get("min_above_bottom").getAsInt();maxAbsolute=height.get("max_absolute").getAsInt();
            scanSteps=height.get("environment_scan_steps").getAsInt();this.features=Collections.unmodifiableList(features);
            moss=new Patch(c.getAsJsonObject("moss_patch"));mossCeiling=new Patch(c.getAsJsonObject("moss_patch_ceiling"));
            clay=new Patch(c.getAsJsonObject("clay_with_dripleaves"));clayPool=new Patch(c.getAsJsonObject("clay_pool_with_dripleaves"));
            JsonObject root=c.getAsJsonObject("root_system");rootColumnMax=root.get("root_column_max_height").getAsInt();
            requiredTreeSpace=root.get("required_vertical_space_for_tree").getAsInt();rootRadius=root.get("root_radius").getAsInt();
            rootAttempts=root.get("root_attempts").getAsInt();hangingRadius=root.get("hanging_root_radius").getAsInt();
            hangingAttempts=root.get("hanging_root_attempts").getAsInt();hangingSpan=root.get("hanging_roots_vertical_span").getAsInt();
        }
    }
    private FuturaLushCavesRuntime120(){terrain=null;}
}
