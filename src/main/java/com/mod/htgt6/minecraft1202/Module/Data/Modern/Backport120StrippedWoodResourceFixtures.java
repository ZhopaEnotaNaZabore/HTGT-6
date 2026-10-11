package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120AxeStrippingHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resource graph validation for the four metadata-backed stripped wood blocks. */
public final class Backport120StrippedWoodResourceFixtures {
    private static final Charset UTF8=Charset.forName("UTF-8");
    private static final String[] SPECIES={"oak","spruce","birch","jungle","acacia","dark_oak"};

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected src/main/resources path");
        File assets=new File(args[0],"assets/htgt6");
        verifyState(assets,"stripped_log",new String[]{"oak","spruce","birch","jungle"},false);
        verifyState(assets,"stripped_log2",new String[]{"acacia","dark_oak"},false);
        verifyState(assets,"stripped_wood",new String[]{"oak","spruce","birch","jungle"},true);
        verifyState(assets,"stripped_wood2",new String[]{"acacia","dark_oak"},true);
        for(String species:SPECIES){
            requireFile(assets,"models/block/1_20_backport/stripped_"+species+"_log.json");
            requireFile(assets,"models/block/1_20_backport/stripped_"+species+"_wood.json");
            requireFile(assets,"textures/blocks/1_20_backport/stripped_"+species+"_log.png");
            requireFile(assets,"textures/blocks/1_20_backport/stripped_"+species+"_log_top.png");
        }
        verifyLang(assets,"en_US.lang");verifyLang(assets,"ru_RU.lang");
        verifyResourceGraph(assets);
        verifyContentNames(assets);
        verifyTreeSignResources(assets);
        verifyStrippingInteraction(assets);
        System.out.println("Backport120 resources OK: complete blockstate/model/texture graph, content names and StrippedWood variants");
    }

    private static void verifyStrippingInteraction(File assets)throws Exception{
        int[] axes={0,4,8,12};
        for(int species=0;species<4;species++)for(int axis:axes){
            Backport120AxeStrippingHandler.Result result=Backport120AxeStrippingHandler.strippedVanilla(
                    false,species|axis);
            require(result!=null,"vanilla log stripping mapping");
            require(result.block==(axis==12?Backport120Content.strippedWood:Backport120Content.strippedLog),
                    "vanilla log/wood output");
            require(result.metadata==(axis==12?species:species|axis),"vanilla log metadata/axis");
        }
        for(int species=0;species<2;species++)for(int axis:axes){
            Backport120AxeStrippingHandler.Result result=Backport120AxeStrippingHandler.strippedVanilla(
                    true,species|axis);
            require(result!=null,"vanilla log2 stripping mapping");
            require(result.block==(axis==12?Backport120Content.strippedWood2:Backport120Content.strippedLog2),
                    "vanilla log2/wood output species="+species+" axis="+axis+" actual="
                            +result.block.getUnlocalizedName());
            require(result.metadata==(axis==12?species:species|axis),"vanilla log2 metadata/axis");
        }
        for(int axis:axes){
            Backport120AxeStrippingHandler.Result result=Backport120AxeStrippingHandler.stripped(
                    Backport120Content.cherryLog,axis);
            require(result!=null,"cherry log stripping mapping");
            require(result.block==(axis==12?Backport120Content.strippedCherryWood:
                    Backport120Content.strippedCherryLog),"cherry log/wood output");
            require(result.metadata==(axis==12?0:axis),"cherry metadata/axis");
        }
        require(Backport120AxeStrippingHandler.stripped(Backport120Content.cherryWood,7).block
                        ==Backport120Content.strippedCherryWood,"cherry wood stripping");
        if(net.minecraft.init.Blocks.stone!=null)require(
                Backport120AxeStrippingHandler.stripped(net.minecraft.init.Blocks.stone,0)==null,
                "non-log must not strip");
        net.minecraft.item.ItemAxe fixtureAxe=new net.minecraft.item.ItemAxe(
                net.minecraft.item.Item.ToolMaterial.WOOD){};
        require(Backport120AxeStrippingHandler.isAxe(new net.minecraft.item.ItemStack(fixtureAxe)),
                "registered axe detection");
        require(!Backport120AxeStrippingHandler.isAuthoritativeSide(true),
                "client stripping event must remain uncancelled so its packet reaches the server");
        require(Backport120AxeStrippingHandler.isAuthoritativeSide(false),
                "server stripping event must own the block replacement");

        String sounds=read(requireFile(assets,"sounds.json"));
        require(sounds.contains("item.axe.strip"),"axe stripping sound registration");
        String[] hashes={"9B1D64448DC81CADF342D159B5C45FDFCD8C8B0759F54CE51D6D089E7A87D979",
                "1EBC258BB4466291AA6C215ECE4E9235B22D0FB9E9C845417D22FEF0B52CE69A",
                "B0CA8D6062100BD508921091206DD437C8C6E21B2B5E8221550FEA2C6CF51634",
                "4005371832157D497AA0D02E8EE70325D8DF95186EE7F4BB916C85570855D238"};
        for(int i=0;i<4;i++){
            try(InputStream sound=ExternalSoundFixture179.open("assets/htgt6/sounds/item/axe/strip"+(i+1)+".ogg")){
                require(sound!=null,"official axe strip sound "+(i+1));
                MessageDigest digest=MessageDigest.getInstance("SHA-256");byte[] bytes=new byte[4096];
                for(int n;(n=sound.read(bytes))>=0;)digest.update(bytes,0,n);
                StringBuilder value=new StringBuilder();for(byte v:digest.digest())value.append(String.format("%02X",v&255));
                require(hashes[i].equals(value.toString()),"official axe strip sound "+(i+1));
            }
        }
        InputStream bytecode=com.mod.htgt6.minecraft1202.Module.Registry.Modern.Minecraft120Backport.class.getResourceAsStream(
                "Minecraft120Backport.class");
        require(bytecode!=null,"Minecraft120Backport bytecode");
        ByteArrayOutputStream output=new ByteArrayOutputStream();
        try{byte[] buffer=new byte[2048];for(int n;(n=bytecode.read(buffer))>=0;)output.write(buffer,0,n);}
        finally{bytecode.close();}
        require(new String(output.toByteArray(),"ISO-8859-1").contains("Backport120AxeStrippingHandler"),
                "axe stripping event registration");
    }

    private static String hash(File file)throws Exception{
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        InputStream input=Files.newInputStream(file.toPath());
        try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)digest.update(buffer,0,n);}
        finally{input.close();}
        StringBuilder result=new StringBuilder();for(byte value:digest.digest())
            result.append(String.format("%02X",value&255));return result.toString();
    }

    private static void verifyTreeSignResources(File assets){
        requireFile(assets,"textures/items/1_20_backport/cherry_hanging_sign.png");
        requireFile(assets,"textures/entity/1_20_backport/signs/cherry.png");
        requireFile(assets,"textures/entity/1_20_backport/signs/hanging/cherry.png");
        requireFile(assets,"models/item/1_20_backport/cherry_hanging_sign.json");
        requireFile(assets,"blockstates/1_20_backport/cherry_hanging_sign.json");
        requireFile(assets,"blockstates/1_20_backport/cherry_wall_hanging_sign.json");
        requireFile(assets,"textures/blocks/1_20_backport/cherry_sapling.png");
        requireFile(assets,"textures/blocks/1_20_backport/mangrove_propagule.png");
    }

    private static void verifyState(File assets,String block,String[] species,boolean wood)throws IOException{
        String json=read(requireFile(assets,"blockstates/1_20_backport/"+block+".json"));
        for(String value:species)for(String axis:new String[]{"x","y","z"}){
            require(json.contains("species="+value+",axis="+axis),block+" misses "+value+"/"+axis);
            require(json.contains("stripped_"+value+(wood?"_wood":"_log")),block+" model path mismatch");
        }
    }

    private static void verifyLang(File assets,String name)throws IOException{
        String lang=read(requireFile(assets,"lang/"+name));
        String[] blocks={"stripped_log.0","stripped_log.1","stripped_log.2","stripped_log.3",
                "stripped_log2.0","stripped_log2.1","stripped_wood.0","stripped_wood.1",
                "stripped_wood.2","stripped_wood.3","stripped_wood2.0","stripped_wood2.1"};
        for(String block:blocks)require(lang.contains("tile.backport_1_20."+block+".name="),
                name+" misses "+block);
    }

    private static void verifyResourceGraph(File assets)throws IOException{
        File states=new File(assets,"blockstates/1_20_backport");
        File models=new File(assets,"models/block/1_20_backport");
        Pattern modelRef=Pattern.compile("\\\"model\\\"\\s*:\\s*\\\"htgt6:block/1_20_backport/([^\\\"]+)\\\"");
        Pattern parentRef=Pattern.compile("\\\"parent\\\"\\s*:\\s*\\\"htgt6:block/1_20_backport/([^\\\"]+)\\\"");
        Pattern textureRef=Pattern.compile("\\\"htgt6:block/1_20_backport/([^\\\"]+)\\\"");
        for(File state:listJson(states))for(String ref:matches(modelRef,read(state)))
            requireFile(assets,"models/block/1_20_backport/"+ref+".json");
        for(File model:listJson(models)){
            String json=read(model);
            for(String ref:matches(parentRef,json))requireFile(assets,"models/block/1_20_backport/"+ref+".json");
            Matcher matcher=textureRef.matcher(json);
            while(matcher.find()){
                String ref=matcher.group(1);
                if(json.substring(Math.max(0,matcher.start()-12),matcher.start()).contains("parent"))continue;
                requireFile(assets,"textures/blocks/1_20_backport/"+ref+".png");
            }
        }
    }

    private static void verifyContentNames(File assets)throws Exception{
        String en=read(requireFile(assets,"lang/en_US.lang"));
        String ru=read(requireFile(assets,"lang/ru_RU.lang"));
        Class<?> content=Class.forName("com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content");
        Method unlocalizedBlock=Class.forName("net.minecraft.block.Block").getMethod("getUnlocalizedName");
        Method unlocalizedItem=Class.forName("net.minecraft.item.Item").getMethod("getUnlocalizedName");
        java.util.ArrayList<String> missing=new java.util.ArrayList<String>();
        for(Field field:content.getFields()){
            Object value=field.get(null);if(value==null)continue;
            Method method=unlocalizedItem.getDeclaringClass().isInstance(value)?unlocalizedItem:
                    unlocalizedBlock.getDeclaringClass().isInstance(value)?unlocalizedBlock:null;
            if(method==null)continue;
            String key=String.valueOf(method.invoke(value))+".name";
            // Internal wall signs, double slabs and plant bodies have no inventory item/name.
            if(isTechnicalContent(field.getName(),key))continue;
            if(!hasTranslation(en,key))missing.add("en_US:"+field.getName()+" -> "+key);
            if(!hasTranslation(ru,key))missing.add("ru_RU:"+field.getName()+" -> "+key);
        }
        require(missing.isEmpty(),"Missing content translations: "+missing);
    }

    private static boolean hasTranslation(String lang,String key){
        if(lang.contains(key+"="))return true;
        String base=key.substring(0,key.length()-".name".length());
        return lang.contains(base+"."); // metadata/variant ItemStack names
    }

    private static boolean isTechnicalContent(String field,String key){
        return key.endsWith("_block.name")||key.contains("double_")||field.contains("Wall")
                ||field.endsWith("Bush")||field.endsWith("Plant")||field.endsWith("Stem")
                ||"aquiferWater".equals(field)||"caveVines".equals(field)
                ||"cherryStandingSign".equals(field)||"cherryHangingSignBlock".equals(field)
                ||"mangroveStandingSign".equals(field)||"mangroveHangingSignBlock".equals(field)
                ||"bambooStandingSign".equals(field)||"bambooHangingSignBlock".equals(field)
                ||"chainBlock".equals(field)||"bambooSapling".equals(field);
    }

    private static File[] listJson(File directory) {
        File[] files=directory.listFiles();
        require(files!=null,"Cannot list "+directory);
        java.util.ArrayList<File> json=new java.util.ArrayList<File>();
        for(File file:files) {
            if(file.isDirectory())java.util.Collections.addAll(json,listJson(file));
            else if(file.getName().endsWith(".json"))json.add(file);
        }
        return json.toArray(new File[json.size()]);
    }
    private static java.util.List<String> matches(Pattern pattern,String text){
        java.util.ArrayList<String> values=new java.util.ArrayList<String>();Matcher matcher=pattern.matcher(text);
        while(matcher.find())values.add(matcher.group(1));return values;
    }

    private static File requireFile(File root,String path){File file=new File(root,path);
        require(file.isFile(),"Missing resource: "+file);return file;}
    private static String read(File file)throws IOException{return new String(Files.readAllBytes(file.toPath()),UTF8);}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private Backport120StrippedWoodResourceFixtures(){ }
}
