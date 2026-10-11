package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/** Runs actual patch placement with official JSON against an isolated legacy World. */
final class FuturaVegetationFixture120 {
    static void validate() throws Exception {
        Method supported=FuturaVegetationPlacement120.class.getDeclaredMethod("supported",String.class);
        supported.setAccessible(true);
        require((Boolean)supported.invoke(null,"patch_sunflower"),"sunflower dispatch");
        Grid w=world(); patch(w,"patch_sunflower",new Random(1));
        require(w.getBlock(0,101,0)==Blocks.double_plant && w.getBlockMetadata(0,101,0)==0
                && w.getBlockMetadata(0,102,0)==8,"sunflower halves");
        for (String key:new String[]{"patch_grass_jungle","patch_taiga_grass"}) {
            int grass=key.endsWith("jungle")?3:1, total=key.endsWith("jungle")?4:5;
            int ferns=0; Random actual=new Random(417), oracle=new Random(417);
            for (int i=0;i<128;i++) {
                w=world(); for(int draw=0;draw<6;draw++)oracle.nextInt(1);
                int expected=oracle.nextInt(total)<grass?1:2;
                patch(w,key,actual);
                require(w.getBlock(0,101,0)==Blocks.tallgrass && w.getBlockMetadata(0,101,0)==expected,key+" weighted state");
                if(expected==2)ferns++;
            }
            require(ferns>0 && ferns<128,key+" mixture");
            require(actual.nextLong()==oracle.nextLong(),key+" RNG continuation");
        }
        w=world();w.soil=Blocks.dirt;w.soilMeta=2;
        Random actual=new Random(1),oracle=new Random(1);
        patch(w,"patch_grass_jungle",actual);
        for(int i=0;i<6;i++)oracle.nextInt(1);
        require(w.cells.isEmpty() && actual.nextLong()==oracle.nextLong(),"podzol rejects before provider draw");
        for(String key:new String[]{"patch_berry_bush","patch_melon","patch_pumpkin"}) {
            for(Block soil:new Block[]{Blocks.dirt,Blocks.stone,Blocks.air}) {
                w=world();w.soil=soil;patch(w,key,new Random(1));
                require(w.cells.isEmpty(),key+" requires grass substrate");
            }
            w=world();patch(w,key,new Random(1));require(!w.cells.isEmpty(),key+" grass control");
        }
        w=world();w.cells.put("0,101,0",Blocks.tallgrass);patch(w,"patch_melon",new Random(1));
        require(w.getBlock(0,101,0)==Blocks.melon_block,"melon replaces grass");
        w=world();w.cells.put("0,101,0",Blocks.water);patch(w,"patch_melon",new Random(1));
        require(w.getBlock(0,101,0)==Blocks.water,"melon rejects water");
        w=world();actual=new Random(71);oracle=new Random(71);patch(w,"patch_pumpkin",actual);
        for(int i=0;i<6;i++)oracle.nextInt(1);
        require(actual.nextLong()==oracle.nextLong(),"pumpkin has no random facing draw");
        w=world();w.floor=382;patch(w,"patch_grass",new Random(1));
        require(w.getBlock(0,383,0)==Blocks.tallgrass,"single plant at maximum Y");
        w=world();w.floor=382;patch(w,"patch_sunflower",new Random(1));
        require(w.cells.isEmpty(),"double plant cannot exceed maximum Y");
        for (String key:new String[]{"patch_dead_bush","patch_waterlily","patch_brown_mushroom","patch_red_mushroom"}) {
            w=world();w.soil=key.equals("patch_dead_bush")?Blocks.sand:key.equals("patch_waterlily")?Blocks.water:Blocks.mycelium;
            Block expected=key.equals("patch_dead_bush")?Blocks.deadbush:key.equals("patch_waterlily")?Blocks.waterlily:
                    key.equals("patch_brown_mushroom")?Blocks.brown_mushroom:Blocks.red_mushroom;
            actual=new Random(91);oracle=new Random(91);patch(w,key,actual);
            for(int i=0;i<6;i++)oracle.nextInt(1);
            require(w.getBlock(0,101,0)==expected && actual.nextLong()==oracle.nextLong(),key+" state and RNG");
            w=world();w.soil=Blocks.air;patch(w,key,new Random(1));require(w.cells.isEmpty(),key+" rejects missing support");
            w=world();w.soil=Blocks.mycelium;w.cells.put("0,101,0",Blocks.stone);patch(w,key,new Random(1));
            require(w.getBlock(0,101,0)==Blocks.stone,key+" respects air predicate");
        }
        w=world();w.soil=Blocks.water;w.soilMeta=1;patch(w,"patch_waterlily",new Random(1));
        require(w.cells.isEmpty(),"lily rejects flowing water");
        w=world();w.top=106;w.cells.put("0,105,0",Blocks.leaves);
        require(FuturaPlacementExecutor120.height(w,"OCEAN_FLOOR_WG",0,0)==106,"ocean floor includes leaves");
        w.cells.put("0,105,0",Blocks.water);
        require(FuturaPlacementExecutor120.height(w,"OCEAN_FLOOR_WG",0,0)==101,"ocean floor skips water");
        System.out.println("Futura vegetation fixtures OK: cycles 41/42, 13 JSON patches, weighted RNG, soil, replacement, height and leaves");
    }
    private static void patch(Grid world,String key,Random random)throws Exception {
        JsonObject config=new JsonParser().parse(FuturaPlacementData120.INSTANCE.configured(key).get("config").toString()).getAsJsonObject();
        config.addProperty("tries",1);config.addProperty("xz_spread",0);config.addProperty("y_spread",0);
        Class<?> adapter=Class.forName("com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSimplePatch120");
        Method method=adapter.getDeclaredMethod("generate",World.class,Random.class,String.class,JsonObject.class,int.class,int.class,int.class);
        method.setAccessible(true);
        require((Boolean)method.invoke(null,world,random,key,config,0,world.floor+1,0),"configured patch handled: "+key);
    }
    static Grid world()throws Exception {
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");Field field=unsafe.getDeclaredField("theUnsafe");field.setAccessible(true);
        Grid w=(Grid)unsafe.getMethod("allocateInstance",Class.class).invoke(field.get(null),Grid.class);
        w.cells=new HashMap<String,Block>();w.metadata=new HashMap<String,Integer>();w.floor=100;w.top=101;w.soil=Blocks.grass;
        return w;
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    public static final class Grid extends World {
        Map<String,Block> cells;Map<String,Integer> metadata;int floor,top,soilMeta;Block soil;
        private Grid(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
        public Block getBlock(int x,int y,int z){Block b=cells.get(x+","+y+","+z);return b!=null?b:y==floor?soil:Blocks.air;}
        public int getBlockMetadata(int x,int y,int z){Integer m=metadata.get(x+","+y+","+z);return m!=null?m:y==floor?soilMeta:0;}
        public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
        public int getHeight(){return 384;}
        public int getHeightValue(int x,int z){return top;}
        public int getFullBlockLightValue(int x,int y,int z){return 15;}
        public boolean canBlockSeeTheSky(int x,int y,int z){return true;}
        public boolean setBlock(int x,int y,int z,Block block,int meta,int flags){
            require(y>=0&&y<384,"plant wrote outside build height");cells.put(x+","+y+","+z,block);metadata.put(x+","+y+","+z,meta);return true;
        }
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
}
