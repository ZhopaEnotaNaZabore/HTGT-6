package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaGenerationRegion120;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;

/** Executes real World constructor and region access against a provider that forbids missing loads. */
final class FuturaGenerationRegionFixture120 {
    static void validate(){
        Grid source=new Grid();
        FuturaGenerationRegion120 region=new FuturaGenerationRegion120(source,-1,1);
        require(region.getBlock(-17,300,16)==Blocks.stone,"prepared negative neighbour above Y=256");
        require(region.getHeight()==384&&region.getActualHeight()==384,"region height");
        require(region.getSeed()==1234,"region seed");
        require(region.setBlock(-17,300,16,Blocks.iron_ore,0,2),"cross-boundary write");
        require(region.getBlock(-17,300,16)==Blocks.iron_ore,"write visibility");
        require(!region.setBlock(-48,300,16,Blocks.iron_ore,0,2),"write radius is wider than one chunk");
        require(region.getBlock(-48,300,16)==Blocks.stone,"read halo unavailable");
        boolean rejected=false;
        try{region.getBlock(-65,100,16);}catch(IllegalStateException expected){rejected=true;}
        require(rejected,"out-of-region read silently provided a chunk");
        require(source.missingLoads==0,"region forced a missing chunk");
        source.missingX=-3;source.missingZ=-1;
        rejected=false;
        try{new FuturaGenerationRegion120(source,-1,1);}catch(IllegalStateException expected){rejected=true;}
        require(rejected&&source.missingLoads==0,"incomplete region generated a missing neighbour");
        require(FuturaGenerationRegion120.unwrap(region)==source,"Forge event world changed");
        Grid legacy=new Grid();legacy.worldHeight=256;
        FuturaGenerationRegion120 limited=new FuturaGenerationRegion120(legacy,-1,1);
        require(limited.getHeight()==256&&limited.getBlock(-17,300,16)==Blocks.air&&!limited.setBlock(-17,300,16,Blocks.stone,0,2),"region ignored inactive extended storage");
        features();
        heights();
        queue();
        System.out.println("Futura region OK: real constructor, fixed read/write halo, negative coordinates, 384 height, no missing loads");
    }
    private static void heights(){
        String[] types={"WORLD_SURFACE_WG","OCEAN_FLOOR_WG","MOTION_BLOCKING"};
        Block[] blocks={Blocks.air,Blocks.stone,Blocks.water,Blocks.tallgrass,Blocks.leaves};
        Random random=new Random(120);
        for(int height:new int[]{256,384}){
            Grid source=new Grid();source.worldHeight=height;source.top=0;
            FuturaGenerationRegion120 region=new FuturaGenerationRegion120(source,-1,1);
            for(int iteration=0;iteration<100;iteration++){
                int y=random.nextInt(height);
                region.setBlock(-17,y,16,blocks[random.nextInt(blocks.length)],0,2);
                // External generators may bypass the region; no stale height cache is allowed.
                source.setBlock(-17,random.nextInt(height),16,blocks[random.nextInt(blocks.length)],0,2);
                for(String type:types){
                    int expected=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPlacementExecutor120.height(source,type,-17,16);
                    int actual=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPlacementExecutor120.height(region,type,-17,16);
                    require(actual==expected,"height mismatch after local/external write: "+type);
                }
                int expected=0;
                for(int scan=source.getHeight()-1;scan>=0;scan--){Block b=source.getBlock(-17,scan,16);
                    if((b.getMaterial().blocksMovement()||b.getMaterial().isLiquid())&&!b.isLeaves(source,-17,scan,16)){expected=scan+1;break;}}
                require(region.getTopSolidOrLiquidBlock(-17,16)==expected,"top solid/liquid mismatch");
            }
        }
        System.out.println("Futura height scans OK: 800 comparisons, negative coordinates, 256/384 height, local and external writes");
    }
    private static void queue(){
        Grid w=new Grid();com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPopulationQueue120 queue=new com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPopulationQueue120();
        Chunk deferred=w.getChunkFromChunkCoords(-2,1);deferred.isTerrainPopulated=true;
        require(queue.begin(w,-1,1),"root population rejected");
        require(!queue.begin(w,-2,1)&&!deferred.isTerrainPopulated,"reentry was not deferred or saved as populated");
        require(!queue.begin(w,-2,1),"duplicate reentry ran");queue.end();
        int[] calls={0};IChunkProvider server=(IChunkProvider)java.lang.reflect.Proxy.newProxyInstance(Grid.class.getClassLoader(),new Class<?>[]{IChunkProvider.class},(proxy,method,args)->{
            require(method.getName().equals("populate"),"queue tried to load terrain");
            Chunk c=w.getChunkFromChunkCoords((Integer)args[1],(Integer)args[2]);
            require(!c.isTerrainPopulated,"duplicate population");c.isTerrainPopulated=true;calls[0]++;return null;
        });
        w.missingX=-4;w.missingZ=-1;queue.drain(w,server);
        require(calls[0]==0&&!deferred.isTerrainPopulated&&w.missingLoads==0,"drain expanded an incomplete region");
        w.missingX=Integer.MIN_VALUE;queue.drain(w,server);queue.drain(w,server);
        require(calls[0]==1&&deferred.isTerrainPopulated,"deferred chunk was lost or populated twice");
        System.out.println("Futura population queue OK: reentry, persisted pending flag, duplicate request, bounded drain and retry");
    }
    private static void features(){
        com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPlacementData120 data=com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaPlacementData120.INSTANCE;
        java.util.Set<Block> geodeBlocks=new java.util.HashSet<Block>();
        for(int seed=0;seed<8;seed++){
            Grid w=new Grid();FuturaGenerationRegion120 region=new FuturaGenerationRegion120(w,-1,1);
            w.cells.put("3,64,35",Blocks.bedrock);
            new com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaGeodeFeature120(1234).generate(region,new Random(seed),data.configured("amethyst_geode").getAsJsonObject("config"),-1,60,31);
            require(w.getBlock(3,64,35)==Blocks.bedrock,"geode replaced protected bedrock");
            geodeBlocks.addAll(w.cells.values());require(w.missingLoads==0,"geode loaded a neighbour");
            w=new Grid();region=new FuturaGenerationRegion120(w,-1,1);
            com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnFossilGenerator.generateConfigured(region,new Random(seed),data.configured(data.placed("fossil_lower").get("feature").getAsString()).getAsJsonObject("config"),-1,60,31);
            require(w.cells.containsValue(com.mod.htgt6.minecraft1202.Module.Registry.Frostburn.FrostburnContent.boneBlock),"fossil NBT did not place bones");
            require(w.missingLoads==0,"fossil loaded a neighbour");
            w=new Grid();w.fill=Blocks.water;w.top=127;region=new FuturaGenerationRegion120(w,-1,1);
            com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaIcebergFeature120.generate(region,new Random(seed),-1,31,Blocks.packed_ice);
            require(w.cells.containsValue(Blocks.packed_ice),"iceberg did not place ice");
            require(w.missingLoads==0,"iceberg loaded a neighbour");
            w=new Grid();region=new FuturaGenerationRegion120(w,-1,1);
            require(com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaLakeFeature120.generate(region,new Random(seed),data.configured("lake_lava").getAsJsonObject("config"),-1,60,31),"lake rejected solid terrain");
            require(w.cells.containsValue(Blocks.lava),"lake has no lava");
            for(String key:w.cells.keySet()){
                String[] coordinates=key.split(",");int px=Integer.parseInt(coordinates[0]),pz=Integer.parseInt(coordinates[2]);
                require(px>=-1&&px<=14&&pz>=31&&pz<=46,"lake retained legacy horizontal offset");
            }
            require(w.missingLoads==0,"lake loaded a neighbour");
        }
        require(geodeBlocks.contains(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.amethystBlock)
                &&geodeBlocks.contains(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.calcite)
                &&geodeBlocks.contains(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.smoothBasalt)
                &&geodeBlocks.contains(Blocks.air),"geode layers missing");
        System.out.println("Futura feature region OK: 32 actual geodes, fossils, icebergs and lakes at a chunk corner, protected bedrock, no missing loads");
    }
    private static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    private static final class Grid extends World {
        final Map<String,Block> cells=new HashMap<String,Block>();final Map<Long,Chunk> chunks=new HashMap<Long,Chunk>();int worldHeight=384,top=384;Block fill=Blocks.stone;int missingLoads;int missingX=Integer.MIN_VALUE,missingZ=Integer.MIN_VALUE;
        Grid(){super(null,"region fixture",new WorldProviderSurface(),new WorldSettings(1234,WorldSettings.GameType.SURVIVAL,true,false,WorldType.DEFAULT),new net.minecraft.profiler.Profiler());this.provider.registerWorld(this);this.chunkProvider=createChunkProvider();}
        public Block getBlock(int x,int y,int z){Block b=cells.get(x+","+y+","+z);return b!=null?b:y>=0&&y<top?fill:Blocks.air;}
        public boolean setBlock(int x,int y,int z,Block b,int meta,int flags){cells.put(x+","+y+","+z,b);return true;}
        public int getActualHeight(){return worldHeight;}
        public int getHeight(){return worldHeight;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
        protected IChunkProvider createChunkProvider(){
            return (IChunkProvider)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{IChunkProvider.class},(proxy,method,args)->{
                if(method.getName().equals("chunkExists"))return (Integer)args[0]!=missingX||(Integer)args[1]!=missingZ;
                if(method.getName().equals("provideChunk")){
                    if((Integer)args[0]==missingX&&(Integer)args[1]==missingZ){missingLoads++;throw new AssertionError("Missing neighbour was provided");}
                    long key=ChunkCoordIntPair.chunkXZ2Int((Integer)args[0],(Integer)args[1]);
                    Chunk result=chunks.get(key);if(result==null){result=new CellChunk(this,(Integer)args[0],(Integer)args[1]);chunks.put(key,result);}return result;
                }
                missingLoads++;throw new AssertionError("Unexpected provider call: "+method.getName());
            });
        }
    }
    private static final class CellChunk extends Chunk {
        final Grid grid;
        CellChunk(Grid grid,int x,int z){super(grid,x,z);this.grid=grid;}
        public Block getBlock(int x,int y,int z){return grid.getBlock((xPosition<<4)+x,y,(zPosition<<4)+z);}
        public int getBlockMetadata(int x,int y,int z){return 0;}
    }
}
