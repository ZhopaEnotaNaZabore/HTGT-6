package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.google.gson.*;
import com.mod.htgt6.minecraft1202.Module.World.Modern.*;
import java.io.*;
import java.util.*;

/** Independent vanilla RNG vectors and placement boundary/order regression. */
public final class FuturaWorldgenParityFixture120 {
    public static void main(String[] args)throws Exception{
        // Same headless GameData setup as GroundContentFixture120; no LaunchWrapper/GUI.
        java.lang.reflect.Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,allocate(cpw.mods.fml.common.Loader.class));
        net.minecraft.init.Bootstrap.func_151354_b();
        JsonObject oracle;
        try(Reader r=new InputStreamReader(FuturaWorldgenParityFixture120.class.getResourceAsStream(
                "/assets/htgt6/worldgen/futura_random_oracle_1202.json"),"UTF-8")){
            oracle=new JsonParser().parse(r).getAsJsonObject();
        }
        for(JsonElement e:oracle.getAsJsonArray("decoration")){
            JsonObject v=e.getAsJsonObject();long seed=v.get("seed").getAsLong();
            long actual=WorldgenRandom120.decorationSeed(seed,v.get("x").getAsInt(),v.get("z").getAsInt());
            require(actual==v.get("value").getAsLong(),"vanilla decoration seed");
            Random random=new WorldgenRandom120(actual);
            for(JsonElement value:v.getAsJsonArray("draws"))require(random.nextLong()==value.getAsLong(),"WorldgenRandom bit consumption");
        }
        for(JsonElement e:oracle.getAsJsonArray("position")){
            JsonObject v=e.getAsJsonObject();int x=v.get("x").getAsInt(),y=v.get("y").getAsInt(),z=v.get("z").getAsInt();
            require(PositionalRandomFactory120.coordinateSeed(x,y,z)==v.get("coordinate").getAsLong(),"Mth coordinate overflow");
            PositionalRandomFactory120 factory=new PositionalRandomFactory120(v.get("seed").getAsLong());
            if(!v.get("name").isJsonNull())factory=factory.fork(v.get("name").getAsString());
            Xoroshiro128PlusPlus120 random=factory.at(x,y,z);
            require(random.nextLong()==v.get("draw").getAsLong(),"positional xoroshiro seed");
            require(random.nextFloat()==v.get("float").getAsFloat(),"xoroshiro float uses high bits");
        }
        FuturaPlacementData120 data=FuturaPlacementData120.INSTANCE;
        NoiseFixture120.validate();
        for(String key:data.features(6))if(key.startsWith("minecraft:ore_")){
            String configured=data.placed(key).get("feature").getAsString();
            new WorldGenBackport120Ore(data.configured(configured).getAsJsonObject("config"));
        }
        oreGeometry(data);
        FuturaGenerationRegionFixture120.validate();
        FuturaSelectorFixture120.validate();
        FuturaStructureFixture120.validate();
        FuturaVegetationFixture120.validate();
        FuturaStraightTreeFixture120.validate();
        FuturaGiantTreeFixture120.validate();
        FuturaPlacementFixture120.validate();
        FuturaFlowerFixture120.validate();
        com.mod.htgt6.asm.PlantHeightASMFixture120.validate();
        com.mod.htgt6.asm.ColumnPlantASMFixture120.validate();
        require(data.index(6,"ore_coal_upper")<data.index(6,"ore_coal_lower"),"coal feature order");
        require(data.features(6).contains("minecraft:ore_diamond_medium"),"1.20.2 diamond medium pass");
        require(data.has(OverworldBiomeParameterMap120.Biome.DRIPSTONE_CAVES,6,"ore_copper_large"),"dripstone copper");
        require(!data.has(OverworldBiomeParameterMap120.Biome.PLAINS,6,"ore_copper_large"),"copper biome filter");
        JsonObject range=null;
        for(JsonElement e:data.placed("ore_diamond").getAsJsonArray("placement"))
            if(e.getAsJsonObject().has("height"))range=e.getAsJsonObject().getAsJsonObject("height");
        require(FuturaPlacementData120.anchor(range.getAsJsonObject("min_inclusive"),384)==-144,"above_bottom -80");
        Random actual=new Random(90210),reference=new Random(90210);boolean below=false;
        for(int i=0;i<4096;i++){
            int y=FuturaPlacementData120.sampleHeight(actual,range,384);
            int vanilla=-144+reference.nextInt(81)+reference.nextInt(81);
            require(y==vanilla+64,"trapezoid sampling +64 without clipping");below|=y<0;
        }
        require(below,"out-of-world distribution was clipped");
        FuturaSurfaceSystem120 surface=new FuturaSurfaceSystem120(1234);
        PositionalRandomFactory120 bedrock=new PositionalRandomFactory120(1234).fork("minecraft:bedrock_floor");
        for(int x=-3;x<=3;x++)for(int y=0;y<=5;y++){
            boolean vanilla=y==0||(y<5&&bedrock.at(x,y-64,7).nextFloat()<(5-y)/5.0D);
            require(surface.verticalGradient("minecraft:bedrock_floor",x,y,7,-64,-59)==vanilla,"bedrock Y translation");
        }
        System.out.println("Futura vanilla parity fixtures OK; RNG=75 vectors, +64 anchors, ore order and bedrock");
    }
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}

    private static Object allocate(Class<?> type)throws Exception{
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");java.lang.reflect.Field field=unsafe.getDeclaredField("theUnsafe");
        field.setAccessible(true);return unsafe.getMethod("allocateInstance",Class.class).invoke(field.get(null),type);
    }
    private static void oreGeometry(FuturaPlacementData120 data)throws Exception{
        OreWorld solid=(OreWorld)allocate(OreWorld.class);solid.cells=new HashMap<String,net.minecraft.block.Block>();
        WorldGenBackport120Ore ore=new WorldGenBackport120Ore(net.minecraft.init.Blocks.iron_ore,
                com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.deepslateIronOre,64);
        require(ore.generate(solid,new Random(1234),0,100,0),"ore in solid volume");
        boolean negative=false;
        for(String key:solid.cells.keySet()){String[] xyz=key.split(",");negative|=Integer.parseInt(xyz[0])<0||Integer.parseInt(xyz[2])<0;}
        require(negative,"ore origin retained legacy +8 X/Z offset");
        OreWorld exposed=(OreWorld)allocate(OreWorld.class);exposed.cells=new HashMap<String,net.minecraft.block.Block>();exposed.thin=true;
        JsonObject config=new JsonParser().parse(data.configured("ore_iron").getAsJsonObject("config").toString()).getAsJsonObject();
        config.addProperty("size",64);config.addProperty("discard_chance_on_air_exposure",1.0F);
        new WorldGenBackport120Ore(config).generate(exposed,new Random(1234),0,100,0);
        require(exposed.cells.isEmpty(),"buried ore exposed to air");
        require(ore.generate(exposed,new Random(1234),0,100,0),"air-exposure control placed no ore");
        // A missing neighbour must never be queried: on a real server this would
        // provide it and recursively run population until the stack overflows.
        for(int origin:new int[]{-16,0,16}){
            OreWorld edge=(OreWorld)allocate(OreWorld.class);
            edge.cells=new HashMap<String,net.minecraft.block.Block>();
            edge.bounded=true;edge.minChunk=origin>>4;
            WorldGenBackport120Ore buried=new WorldGenBackport120Ore(config);
            for(int i=0;i<32;i++)buried.generate(edge,new Random(i),origin,100,origin);
            require(!edge.cells.isEmpty(),"unknown neighbour incorrectly treated as air");
            edge.cells.clear();
            for(int i=0;i<32;i++)ore.generate(edge,new Random(i),origin+15,300,origin+15);
            boolean neighbour=false;
            for(String key:edge.cells.keySet()){
                String[] xyz=key.split(",");
                neighbour|=(Integer.parseInt(xyz[0])>>4)>edge.minChunk||(Integer.parseInt(xyz[2])>>4)>edge.minChunk;
            }
            require(neighbour,"ore stopped crossing into resident neighbour above Y=256");
        }
    }
    public static final class OreWorld extends net.minecraft.world.World{
        Map<String,net.minecraft.block.Block> cells;boolean thin,bounded;int minChunk;
        private boolean resident(int cx,int cz){return !bounded||(cx>=minChunk&&cx<=minChunk+1&&cz>=minChunk&&cz<=minChunk+1);}
        private void checkResident(int x,int z){require(resident(x>>4,z>>4),"ore forced an unloaded chunk at "+x+","+z);}
        public net.minecraft.world.chunk.IChunkProvider getChunkProvider(){
            return (net.minecraft.world.chunk.IChunkProvider)java.lang.reflect.Proxy.newProxyInstance(
                    getClass().getClassLoader(),new Class<?>[]{net.minecraft.world.chunk.IChunkProvider.class},
                    (proxy,method,args)->{
                        if(method.getName().equals("chunkExists"))return resident((Integer)args[0],(Integer)args[1]);
                        throw new AssertionError("Unexpected provider call: "+method.getName());
                    });
        }
        private OreWorld(){super(null,"fixture",(net.minecraft.world.WorldProvider)null,(net.minecraft.world.WorldSettings)null,null);}
        public net.minecraft.block.Block getBlock(int x,int y,int z){
            checkResident(x,z);
            net.minecraft.block.Block stored=cells.get(x+","+y+","+z);
            return stored!=null?stored:y<0||y>=384||(thin&&y!=100)?net.minecraft.init.Blocks.air:net.minecraft.init.Blocks.stone;
        }
        public boolean isAirBlock(int x,int y,int z){return getBlock(x,y,z)==net.minecraft.init.Blocks.air;}
        public int getHeight(){return 384;}
        public int getTopSolidOrLiquidBlock(int x,int z){checkResident(x,z);return thin?101:384;}
        public boolean setBlock(int x,int y,int z,net.minecraft.block.Block block,int meta,int flags){
            checkResident(x,z);
            require(y>=0&&y<384,"ore wrote outside translated build height");cells.put(x+","+y+","+z,block);return true;
        }
        protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
        protected int func_152379_p(){return 0;}
        public net.minecraft.entity.Entity getEntityByID(int id){return null;}
    }
}
