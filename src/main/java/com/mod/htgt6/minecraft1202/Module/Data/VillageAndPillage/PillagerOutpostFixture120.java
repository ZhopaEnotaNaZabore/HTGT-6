package com.mod.htgt6.minecraft1202.Module.Data.VillageAndPillage;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mod.htgt6.common.util.StructurePlacement120;
import com.mod.htgt6.minecraft1202.Module.Data.Modern.StructureProcessorRuntime120;
import com.mod.htgt6.minecraft1202.Module.Registry.VillageAndPillage.VillageAndPillageUpdate;
import com.mod.htgt6.minecraft1202.Module.World.Modern.JigsawPlacementRuntime120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.PillagerOutpostJigsawRegistry120;
import com.mod.htgt6.minecraft1202.Module.World.VillageAndPillage.PillagerOutpostGenerator;
import cpw.mods.fml.common.IWorldGenerator;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.nbt.NBTTagCompound;

/** Resource, registration and deterministic-worldgen contract for the 1.20.2 outpost port. */
public final class PillagerOutpostFixture120 {
    private static final String[] NBT={"base_plate","feature_cage1","feature_cage2",
            "feature_cage_with_allays","feature_logs","feature_plate","feature_targets",
            "feature_tent1","feature_tent2","watchtower","watchtower_overgrown"};
    private static final String[] JSON={"worldgen/structure/pillager_outpost.json",
            "worldgen/structure_set/pillager_outposts.json","worldgen/processor_list/outpost_rot.json",
            "worldgen/template_pool/pillager_outpost/base_plates.json",
            "worldgen/template_pool/pillager_outpost/feature_plates.json",
            "worldgen/template_pool/pillager_outpost/features.json",
            "worldgen/template_pool/pillager_outpost/towers.json"};

    public static void main(String[] args)throws Exception{com.mod.htgt6.minecraft1202.Module.Data.Modern.VillageBootstrapFixture114.initialize();
        require(args.length==2,"expected <resources> <1.20.2 client jar>");
        File resources=new File(args[0]);ZipFile reference=new ZipFile(args[1]);
        try{verifyReference(resources,reference);}finally{reference.close();}
        verifyOfficialRules(resources);verifyRuntime();verifyPersistence();verifyRegistration();
        System.out.println("pillager-outpost-120 fixture OK");
    }

    private static void verifyReference(File resources,ZipFile reference)throws Exception{
        for(String name:NBT)equal(resources,reference,"structures/pillager_outpost/"+name+".nbt");
        for(String name:JSON)equal(resources,reference,name);
    }
    private static void equal(File resources,ZipFile reference,String relative)throws Exception{
        byte[] local=read(new FileInputStream(new File(resources,"assets/htgt6/"+relative)));
        ZipEntry entry=reference.getEntry("data/minecraft/"+relative);
        require(entry!=null,"reference missing "+relative);
        byte[] expected=read(reference.getInputStream(entry));
        if(relative.endsWith(".json")){
            JsonParser parser=new JsonParser();
            require(parser.parse(new String(local,java.nio.charset.StandardCharsets.UTF_8)).equals(
                    parser.parse(new String(expected,java.nio.charset.StandardCharsets.UTF_8))),
                    "resource differs from 1.20.2: "+relative);
            return;
        }
        if(relative.endsWith(".nbt")){
            local=read(new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(local)));
            expected=read(new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(expected)));
        }
        require(Arrays.equals(local,expected),"resource differs from 1.20.2: "+relative);
    }

    private static void verifyOfficialRules(File resources)throws Exception{
        JsonObject set=json(new File(resources,"assets/htgt6/worldgen/structure_set/pillager_outposts.json"));
        JsonObject placement=set.getAsJsonObject("placement");
        require(placement.get("spacing").getAsInt()==32&&placement.get("separation").getAsInt()==8,
                "outpost spacing/separation");
        require(placement.get("salt").getAsInt()==165745296,"outpost salt");
        require(Math.abs(placement.get("frequency").getAsFloat()-0.2F)<0.00001F,"outpost frequency");
        require("legacy_type_1".equals(placement.get("frequency_reduction_method").getAsString()),
                "outpost frequency reducer");
        require(placement.getAsJsonObject("exclusion_zone").get("chunk_count").getAsInt()==10,
                "outpost village exclusion");
        JsonObject structure=json(new File(resources,"assets/htgt6/worldgen/structure/pillager_outpost.json"));
        require(structure.get("size").getAsInt()==7&&structure.get("max_distance_from_center").getAsInt()==80,
                "outpost jigsaw settings");
        JsonObject features=json(new File(resources,"assets/htgt6/worldgen/template_pool/pillager_outpost/features.json"));
        int total=0;for(com.google.gson.JsonElement e:features.getAsJsonArray("elements"))
            total+=e.getAsJsonObject().get("weight").getAsInt();
        require(total==13,"outpost feature pool weight must be 7 feature + 6 empty");
    }

    private static void verifyRuntime(){
        // 1.20.2 StructurePlacement legacy_type_1 seeds on chunk coordinates >> 4.
        for(int cx=-33;cx<=33;cx++)for(int cz=-33;cz<=33;cz++){
            java.util.Random reference=new java.util.Random((long)((cx>>4)^((cz>>4)<<4))^918273645L);
            reference.nextInt();boolean expected=reference.nextInt(5)==0;
            require(StructurePlacement120.pillagerOutpostFrequencyPasses(918273645L,cx,cz)==expected,
                    "legacy_type_1 coordinate reduction at "+cx+","+cz);
        }
        net.minecraft.world.chunk.Chunk snapshotChunk=new net.minecraft.world.chunk.Chunk(null,-2,3);
        Arrays.fill(snapshotChunk.heightMap,150);
        JigsawPlacementRuntime120.Terrain snapshot=PillagerOutpostGenerator.snapshotSurface(snapshotChunk);
        Arrays.fill(snapshotChunk.heightMap,180); // Earlier template writes changed the live heightmap.
        require(snapshot.surfaceY(-32,48)==150&&snapshot.surfaceY(-17,63)==150,"gravity must retain original column heights");
        require(snapshot.surfaceY(-16,48)==0,"gravity must not read neighbouring chunks");
        require(IWorldGenerator.class.isAssignableFrom(PillagerOutpostGenerator.class),"generator interface");
        require(FuturaAncientCityGenerator120.supportsStructureLootTable("minecraft:chests/pillager_outpost"),
                "outpost chest loot adapter");
        require(FuturaAncientCityGenerator120.supportsTemplateEntity("minecraft:allay"),
                "feature_cage_with_allays entity adapter");
        PillagerOutpostGenerator.validateTemplates();
        PillagerOutpostJigsawRegistry120 registry=new PillagerOutpostJigsawRegistry120();registry.validateAll();
        require(registry.loadedPoolCount()==4&&registry.loadedTemplateCount()==11,"registered pools/templates");
        require(StructurePlacement120.PILLAGER_OUTPOST.spacing==32
                &&StructurePlacement120.PILLAGER_OUTPOST.separation==8
                &&StructurePlacement120.PILLAGER_OUTPOST.salt==165745296,"runtime placement settings");
        for(int rx=-8;rx<=8;rx++)for(int rz=-8;rz<=8;rz++){
            int[] p=StructurePlacement120.candidate(918273645L,rx,rz,StructurePlacement120.PILLAGER_OUTPOST);
            require(p[0]>=rx*32&&p[0]<rx*32+24&&p[1]>=rz*32&&p[1]<rz*32+24,"candidate region bounds");
        }
        JigsawPlacementRuntime120.Result first=PillagerOutpostGenerator.layout(123456789L,11,-9,74);
        JigsawPlacementRuntime120.Result repeat=PillagerOutpostGenerator.layout(123456789L,11,-9,74);
        require(fingerprint(first)==fingerprint(repeat),"layout is not deterministic");
        require(first.pieces.size()>=2,"official base plate did not attach its tower");
        for(JigsawPlacementRuntime120.Piece piece:first.pieces){JigsawPlacementRuntime120.Box box=piece.box();
            int sx=(11<<4)+7,sz=(-9<<4)+7;
            require(box.minX>=sx-80&&box.maxX<=sx+80&&box.minZ>=sz-80&&box.maxZ<=sz+80,
                    "jigsaw max-distance boundary");}
        require(StructurePlacement120.isOwnedByChunk(16,31,1,1)
                &&!StructurePlacement120.isOwnedByChunk(32,31,1,1),"chunk ownership boundary");
        List<PillagerOutpostGenerator.ChunkSlice> slices=PillagerOutpostGenerator.slices(first);
        require(slices.size()>1,"official outpost layout was reduced to one chunk");
        Set<String> unique=new HashSet<String>();for(PillagerOutpostGenerator.ChunkSlice slice:slices)
            require(unique.add(slice.chunkX+","+slice.chunkZ),"duplicate chunk in slice plan");
        require(simulateReplay(slices,false).equals(simulateReplay(slices,true)),
                "slice completion depends on chunk generation order");
        require(simulateReplay(slices,false).size()==slices.size(),"multi-chunk layout coverage is incomplete");
        StructureProcessorRuntime120.State state=new StructureProcessorRuntime120.State("minecraft:dark_oak_planks");
        boolean a=StructureProcessorRuntime120.apply(Collections.singletonList("minecraft:outpost_rot"),state,state,4,70,9)!=null;
        boolean b=StructureProcessorRuntime120.apply(Collections.singletonList("minecraft:outpost_rot"),state,state,4,70,9)!=null;
        require(a==b,"outpost rot must use deterministic positional random");
    }

    private static void verifyPersistence(){
        PillagerOutpostData120 data=new PillagerOutpostData120();PillagerOutpostData120.Start start=
                data.recordStart(-3,7,91,0x123456789abcdefL);
        data.recordSlice(start,-4,7);data.recordSlice(start,-4,7);
        require(data.claimGuards(-3,7)&&!data.claimGuards(-3,7),"one-shot guard claim");
        NBTTagCompound tag=new NBTTagCompound();data.writeToNBT(tag);
        require(tag.getTagList("Slices",8).tagCount()==1,"duplicate slice claim was persisted");
        PillagerOutpostData120 restored=new PillagerOutpostData120();restored.readFromNBT(tag);
        PillagerOutpostData120.Start loaded=restored.start(-3,7);
        require(loaded!=null&&loaded.surfaceY==91&&loaded.layoutSeed==0x123456789abcdefL,
                "saved outpost descriptor");
        require(restored.hasSlice(loaded,-4,7)&&!restored.claimGuards(-3,7),"saved outpost claims");
    }

    private static void verifyRegistration()throws Exception{
        Method replay=PillagerOutpostGenerator.class.getMethod("chunkLoad",net.minecraftforge.event.world.ChunkEvent.Load.class);
        require(replay.getAnnotation(SubscribeEvent.class)!=null,"chunk-load replay is not subscribed");
        InputStream in=VillageAndPillageUpdate.class.getResourceAsStream("/"+VillageAndPillageUpdate.class.getName().replace('.','/')+".class");
        String bytes=new String(read(in),"ISO-8859-1");
        require(bytes.contains("PillagerOutpostGenerator")&&bytes.contains("validateTemplates")
                        &&bytes.contains("EVENT_BUS"),
                "outpost generator is not validated and registered in module lifecycle");
    }

    private static long fingerprint(JigsawPlacementRuntime120.Result result){long hash=0xcbf29ce484222325L;
        for(JigsawPlacementRuntime120.Piece p:result.pieces){hash=(hash^p.template.id.hashCode())*0x100000001b3L;
            hash=(hash^p.rotation)*0x100000001b3L;hash=(hash^p.x)*0x100000001b3L;
            hash=(hash^p.y)*0x100000001b3L;hash=(hash^p.z)*0x100000001b3L;}return hash;}
    private static Set<String> simulateReplay(List<PillagerOutpostGenerator.ChunkSlice> slices,boolean neighbourFirst){
        Set<String> loaded=new HashSet<String>(),claimed=new HashSet<String>();
        if(neighbourFirst)for(int i=slices.size()-1;i>=0;i-=2){PillagerOutpostGenerator.ChunkSlice s=slices.get(i);
            loaded.add(s.chunkX+","+s.chunkZ);}
        // Creating the start backfills every already-loaded populated target.
        claimed.addAll(loaded);
        for(PillagerOutpostGenerator.ChunkSlice s:slices){String key=s.chunkX+","+s.chunkZ;
            loaded.add(key);claimed.add(key);claimed.add(key);}
        return claimed;
    }
    private static JsonObject json(File file)throws Exception{InputStreamReader reader=new InputStreamReader(new FileInputStream(file),"UTF-8");
        try{return new JsonParser().parse(reader).getAsJsonObject();}finally{reader.close();}}
    private static byte[] read(InputStream input)throws Exception{require(input!=null,"missing stream");try{
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
        while((count=input.read(buffer))>=0)out.write(buffer,0,count);return out.toByteArray();}finally{input.close();}}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
}
