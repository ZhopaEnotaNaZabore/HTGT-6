package com.mod.htgt6.asm;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Blocks.Combat.BlockFrostedIce;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldInfo;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public final class ColumnPlantASMFixture120 {
    public static void validate()throws Exception {
        FuturaWorldType120.registerIdentity();
        JsonObject oracle;
        try(Reader reader=new InputStreamReader(ColumnPlantASMFixture120.class.getResourceAsStream("/assets/htgt6/worldgen/futura_column_oracle_1202.json"),"UTF-8")){oracle=new JsonParser().parse(reader).getAsJsonObject();}
        for(JsonElement e:oracle.getAsJsonArray("heights")) {
            JsonObject v=e.getAsJsonObject();Random r=new Random(v.get("seed").getAsLong());
            JsonObject provider=column(v.get("min").getAsInt()==1?"patch_cactus":"patch_sugar_cane").getAsJsonArray("layers").get(0).getAsJsonObject().getAsJsonObject("height");
            for(JsonElement expected:v.getAsJsonArray("values"))require(FuturaPlacementData120.count(r,provider)==expected.getAsInt(),"biased height oracle");
            require(r.nextLong()==v.get("nextLong").getAsLong(),"height RNG continuation");
        }
        for(JsonElement e:oracle.getAsJsonArray("trim")) {
            JsonObject v=e.getAsJsonObject();int[] heights=ints(v.getAsJsonArray("layers"));int total=0;for(int h:heights)total+=h;
            FuturaBlockColumn120.truncate(heights,total,v.get("allowed").getAsInt(),v.get("tip").getAsBoolean());
            require(Arrays.equals(heights,ints(v.getAsJsonArray("expected"))),"independent expanded-layer trim");
        }
        for(String key:new String[]{"patch_cactus","patch_sugar_cane"}) {
            Grid w=world(true);if(key.endsWith("cane"))w.put(1,100,0,Blocks.water,0);
            Random actual=new Random(3),expected=new Random(3);for(int i=0;i<6;i++)expected.nextInt(1);
            int min=key.endsWith("cane")?2:1;int h=min+expected.nextInt(expected.nextInt(3)+1);
            patch(w,actual,key);require(w.getBlock(0,100+h,0)==(min==1?Blocks.cactus:Blocks.reeds),"real patch height");
            require(actual.nextLong()==expected.nextLong(),"patch RNG continuation");
            w=world(true);w.put(0,101,0,Blocks.stone,0);actual=new Random(9);expected=new Random(9);
            for(int i=0;i<6;i++)expected.nextInt(1);patch(w,actual,key);
            require(actual.nextLong()==expected.nextLong()&&w.getBlock(0,101,0)==Blocks.stone,"filter rejects before height draw");
        }
        Grid w=world(true);w.put(1,100,0,new BlockFrostedIce(),0);
        require(FuturaColumnPlantSurvival120.cane(false,w,0,101,0),"frost ice supports runtime cane");
        patch(w,new Random(1),"patch_sugar_cane");require(w.getBlock(0,101,0)==Blocks.air,"worldgen additionally requires actual water");
        for(int[] side:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})for(int level:new int[]{0,3}) {
            w=world(true);w.put(side[0],100,side[1],Blocks.water,level);patch(w,new Random(1),"patch_sugar_cane");
            require(w.getBlock(0,101,0)==Blocks.reeds,"all water sides/source and flow");
        }
        JsonObject config=column("patch_cactus");config.getAsJsonArray("layers").get(0).getAsJsonObject().addProperty("height",3);
        w=world(true);w.put(0,103,0,Blocks.stone,0);FuturaBlockColumn120.place(w,new Random(1),config,0,101,0);
        require(w.getBlock(0,101,0)==Blocks.cactus&&w.getBlock(0,102,0)==Blocks.air,"lookahead leaves headroom below obstacle");
        w=world(true);FuturaBlockColumn120.place(w,new Random(1),config,0,382,0);
        require(w.getBlock(0,382,0)==Blocks.cactus&&w.getBlock(0,383,0)==Blocks.air,"extended ceiling headroom");
        w=world(false);FuturaBlockColumn120.place(w,new Random(1),config,0,254,0);
        require(w.getBlock(0,254,0)==Blocks.cactus&&w.getBlock(0,255,0)==Blocks.air,"legacy ceiling headroom");
        for(String direction:new String[]{"up","down","east","west","north","south"}) {
            w=world(true);config.addProperty("direction",direction);FuturaBlockColumn120.place(w,new Random(1),config,5,150,5);
            require(w.cells.size()==3,"column direction "+direction);
        }
        config.addProperty("direction","up");JsonObject second=new JsonParser().parse(config.getAsJsonArray("layers").get(0).toString()).getAsJsonObject();
        second.addProperty("height",2);second.getAsJsonObject("provider").getAsJsonObject("state").addProperty("Name","minecraft:sugar_cane");
        second.getAsJsonObject("provider").getAsJsonObject("state").getAsJsonObject("Properties").addProperty("age","7");config.getAsJsonArray("layers").add(second);
        for(boolean tip:new boolean[]{false,true}) {
            config.addProperty("prioritize_tip",tip);w=world(true);w.put(0,105,0,Blocks.stone,0);
            FuturaBlockColumn120.place(w,new Random(1),config,0,101,0);
            require(w.getBlock(0,103,0)==(tip?Blocks.reeds:Blocks.cactus),"real multi-layer tip priority");
            if(tip)require(w.getBlockMetadata(0,103,0)==7,"column provider age");
        }
        providerDrawOrder();
        lifecycle();
        System.out.println("Column 46 OK: 224 height draws, 44 trims, filters/RNG, real columns, two heights, layers, age and block lifecycle");
    }
    private static void providerDrawOrder()throws Exception {
        JsonObject c=column("patch_cactus"),layer=c.getAsJsonArray("layers").get(0).getAsJsonObject();
        JsonObject weighted=new JsonObject();weighted.addProperty("type","minecraft:weighted_state_provider");JsonArray entries=new JsonArray();
        for(int age:new int[]{0,5}) {
            JsonObject entry=new JsonObject(),state=new JsonParser().parse(layer.getAsJsonObject("provider").get("state").toString()).getAsJsonObject();
            state.getAsJsonObject("Properties").addProperty("age",Integer.toString(age));entry.add("data",state);entry.addProperty("weight",1);entries.add(entry);
        }
        weighted.add("entries",entries);layer.add("provider",weighted);
        c.getAsJsonArray("layers").add(new JsonParser().parse(layer.toString()));
        Random actual=new Random(7890),expected=new Random(7890);int total=0;
        for(int i=0;i<2;i++)total+=1+expected.nextInt(expected.nextInt(3)+1);
        Grid w=world(true);FuturaBlockColumn120.place(w,actual,c,0,150,0);
        for(int i=0;i<total;i++)require(w.getBlockMetadata(0,150+i,0)==(expected.nextInt(2)==0?0:5),"all layer heights precede state draws");
        require(actual.nextLong()==expected.nextLong(),"layer provider RNG continuation");
        w=world(true);w.put(0,151,0,Blocks.stone,0);actual=new Random(7890);expected=new Random(7890);
        for(int i=0;i<2;i++)expected.nextInt(expected.nextInt(3)+1);
        FuturaBlockColumn120.place(w,actual,c,0,150,0);
        require(w.getBlock(0,150,0)==Blocks.air&&actual.nextLong()==expected.nextLong(),"truncated providers consume no RNG");
        for(JsonElement e:c.getAsJsonArray("layers"))e.getAsJsonObject().addProperty("height",0);
        w=world(true);actual=new Random(4);expected=new Random(4);
        require(!FuturaBlockColumn120.place(w,actual,c,0,150,0)&&w.cells.isEmpty()&&actual.nextLong()==expected.nextLong(),"zero-height column is empty and consumes no RNG");
    }
    private static void lifecycle()throws Exception {
        for(String simple:new String[]{"BlockCactus","BlockReed"}) {
            boolean cactus=simple.equals("BlockCactus");String name="net.minecraft.block."+simple;
            byte[] raw=read("/"+name.replace('.','/')+".class");HTGT6Transformer transformer=new HTGT6Transformer();byte[] bytes=transformer.transform(name,name,raw);
            String helper=cactus?"cactusSurvival120":"caneSurvival120";
            int calls=PlantHeightASMFixture120.callCount(bytes,helper);require(calls>0&&calls==PlantHeightASMFixture120.callCount(transformer.transform(name,name,bytes),helper),"column ASM idempotence");
            Constructor<?> constructor=new Loader().define(name,bytes).getDeclaredConstructor();constructor.setAccessible(true);Block plant=(Block)constructor.newInstance();
            Grid w=world(true);w.put(0,101,0,plant,0);if(!cactus)w.put(1,100,0,Blocks.water,0);
            require(plant.canBlockStay(w,0,101,0),"valid original Forge support");
            if(cactus) {
                w.put(1,101,0,Blocks.lava,0);require(!plant.canBlockStay(w,0,101,0),"cactus side lava");
                plant.onNeighborBlockChange(w,0,101,0,Blocks.lava);require(w.getBlock(0,101,0)==Blocks.air,"cactus neighbor removes invalid plant");
                w.put(1,101,0,Blocks.air,0);w.put(0,102,0,Blocks.water,0);require(!plant.canBlockStay(w,0,101,0),"cactus top fluid");
                w.put(0,102,0,Blocks.air,0);w.soil=Backport120Content.suspiciousSand;require(plant.canBlockStay(w,0,101,0),"suspicious sand cactus");
            } else {
                for(Block soil:new Block[]{Blocks.mycelium,Backport120Content.rootedDirt,Backport120Content.mossBlock,Backport120Content.mud,Backport120Content.muddyMangroveRoots,Backport120Content.suspiciousSand}) {
                    w.soil=soil;require(plant.canBlockStay(w,0,101,0),"modern cane substrate");
                }
                w.soil=Blocks.sand;w.put(1,100,0,new BlockFrostedIce(),3);require(plant.canBlockStay(w,0,101,0),"frosted ice cane lifecycle");
                w.put(1,100,0,Blocks.ice,0);require(!plant.canBlockStay(w,0,101,0),"ordinary ice excluded");
                plant.onNeighborBlockChange(w,0,101,0,Blocks.ice);require(w.getBlock(0,101,0)==Blocks.air,"cane lost water removal");
                require(FuturaColumnPlantSurvival120.cane(true,w,0,101,0),"custom Forge accepted substrate preserved");
            }
            boolean enabled=HTGT6Config.RUN_MINECRAFT_1_7_9999;
            try {HTGT6Config.RUN_MINECRAFT_1_7_9999=false;
                require(FuturaColumnPlantSurvival120.cactus(true,w,0,999,0)&&!FuturaColumnPlantSurvival120.cane(false,w,0,101,0),"disabled runtime preserves legacy result");
            } finally {HTGT6Config.RUN_MINECRAFT_1_7_9999=enabled;}
        }
    }
    private static void patch(Grid w,Random random,String key) {
        JsonObject p=new JsonParser().parse(FuturaPlacementData120.INSTANCE.configured(key).get("config").toString()).getAsJsonObject();
        p.addProperty("tries",1);p.addProperty("xz_spread",0);p.addProperty("y_spread",0);
        require(FuturaBlockColumn120.generate(w,random,key,p,0,101,0),"column dispatch");
    }
    private static JsonObject column(String key){return new JsonParser().parse(FuturaPlacementData120.INSTANCE.configured(key).getAsJsonObject("config").getAsJsonObject("feature").getAsJsonObject("feature").get("config").toString()).getAsJsonObject();}
    private static int[] ints(JsonArray values){int[] out=new int[values.size()];for(int i=0;i<out.length;i++)out[i]=values.get(i).getAsInt();return out;}
    private static byte[] read(String path)throws Exception{try(InputStream in=ColumnPlantASMFixture120.class.getResourceAsStream(path)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);return out.toByteArray();}}
    private static Grid world(boolean futura)throws Exception {
        Class<?> u=Class.forName("sun.misc.Unsafe");Field f=u.getDeclaredField("theUnsafe");f.setAccessible(true);Grid w=(Grid)u.getMethod("allocateInstance",Class.class).invoke(f.get(null),Grid.class);
        w.cells=new HashMap<String,Block>();w.meta=new HashMap<String,Integer>();w.soil=Blocks.sand;w.height=futura?384:256;
        w.info=new WorldInfo(new WorldSettings(1,WorldSettings.GameType.SURVIVAL,false,false,futura?FuturaWorldType120.instance():WorldType.DEFAULT),"column fixture");
        w.rules=new GameRules();w.rules.setOrCreateGameRule("doTileDrops","false");w.rand=new Random(1);return w;
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    private static final class Loader extends ClassLoader {Loader(){super(ColumnPlantASMFixture120.class.getClassLoader());}Class<?> define(String n,byte[] b){return defineClass(n,b,0,b.length);}}
    public static final class Grid extends World {
        Map<String,Block> cells;Map<String,Integer> meta;Block soil;int height;WorldInfo info;GameRules rules;
        private Grid(){super(null,"fixture",(WorldProvider)null,(WorldSettings)null,null);}
        public void playAuxSFXAtEntity(net.minecraft.entity.player.EntityPlayer player,int event,int x,int y,int z,int data){}
        public WorldInfo getWorldInfo(){return info;}public GameRules getGameRules(){return rules;}public int getHeight(){return height;}
        public Block getBlock(int x,int y,int z){Block b=cells.get(x+","+y+","+z);return b!=null?b:y==100?soil:Blocks.air;}
        public int getBlockMetadata(int x,int y,int z){Integer n=meta.get(x+","+y+","+z);return n==null?0:n;}
        public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==Blocks.air;}
        void put(int x,int y,int z,Block b,int metadata){cells.put(x+","+y+","+z,b);meta.put(x+","+y+","+z,metadata);}
        public boolean setBlock(int x,int y,int z,Block b,int metadata,int flags){require(y>=0&&y<height,"out of bounds column write");put(x,y,z,b,metadata);return true;}
        public boolean setBlockToAir(int x,int y,int z){put(x,y,z,Blocks.air,0);return true;}
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
}
