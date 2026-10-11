package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Stairs;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.item.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;

/** Headless gate for the two 1.20.2 watchtower mossy-cobblestone shapes missing from 1.7.10. */
public final class MossyCobblestoneFamilyContractFixture120 {
    private static final Charset UTF8=Charset.forName("UTF-8");
    private static final String OFFICIAL_TEXTURE_SHA256=
            "1C069F0513D01272D5E9951E96CA3D9279C34400A88C3C47D52D79B9AC21EF2F";

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected src/main/resources path");
        File assets=new File(args[0],"assets/htgt6");
        verifyReferenceAssets(assets);
        verifyForgeAdapters();
        verifyRegistrationAndClientBinding();
        System.out.println("mossy-cobblestone-family-1.20.2: stairs + half/double slab, official texture, Forge-1.7.10 adapters and wiring OK");
    }

    private static void verifyReferenceAssets(File assets)throws Exception{
        File texture=requireFile(assets,"textures/blocks/1_20_backport/mossy_cobblestone.png");
        require(OFFICIAL_TEXTURE_SHA256.equals(hash(texture)),"official 1.20.2 mossy cobblestone texture drift");
        for(String path:new String[]{"blockstates/1_20_backport/mossy_cobblestone_stairs.json",
                "blockstates/1_20_backport/mossy_cobblestone_slab.json",
                "models/block/1_20_backport/mossy_cobblestone.json",
                "models/block/1_20_backport/mossy_cobblestone_stairs.json",
                "models/block/1_20_backport/mossy_cobblestone_stairs_inner.json",
                "models/block/1_20_backport/mossy_cobblestone_stairs_outer.json",
                "models/block/1_20_backport/mossy_cobblestone_slab.json",
                "models/block/1_20_backport/mossy_cobblestone_slab_top.json",
                "models/item/1_20_backport/mossy_cobblestone_stairs.json",
                "models/item/1_20_backport/mossy_cobblestone_slab.json"}){
            String json=new String(Files.readAllBytes(requireFile(assets,path).toPath()),UTF8);
            require(json.contains("mossy_cobblestone"),"wrong model family: "+path);
            require(!json.contains("cobbled_deepslate"),"copied model retained source texture: "+path);
        }
    }

    private static void verifyForgeAdapters(){
        require(Backport120Content.mossyCobblestoneStairs instanceof BlockBackport120Stairs,"stairs adapter");
        require(Backport120Content.mossyCobblestoneSlab instanceof BlockBackport120Slab,"slab adapter");
        require(Backport120Content.doubleMossyCobblestoneSlab instanceof BlockBackport120Slab,"double slab adapter");
        require(close(Backport120Content.mossyCobblestoneStairs.getBlockHardness(null,0,0,0),2.0F),
                "stairs hardness/source semantics");
        require(close(Backport120Content.mossyCobblestoneSlab.getBlockHardness(null,0,0,0),2.0F),
                "slab hardness/source semantics");
        require(Backport120Content.doubleMossyCobblestoneSlab.getItem(null,0,0,0)==
                Item.getItemFromBlock(Backport120Content.mossyCobblestoneSlab),"double slab inventory maps to half slab");
    }

    private static void verifyRegistrationAndClientBinding()throws Exception{
        String registry=bytecode(Backport120Content.class);
        require(registry.contains("mossy_cobblestone_stairs"),"stairs registration/recipe constant");
        require(registry.contains("mossy_cobblestone"),"slab registration/recipe constant");
        require(registry.contains("mossyCobblestoneStairs")&&registry.contains("mossyCobblestoneSlab"),
                "registry recipe output wiring");
        String client=bytecode(Minecraft120BackportClient.class);
        require(client.contains("mossy_cobblestone_stairs")&&client.contains("mossy_cobblestone_slab"),
                "client JSON binding");
    }

    private static String bytecode(Class<?> type)throws Exception{InputStream input=type.getResourceAsStream(type.getSimpleName()+".class");
        require(input!=null,"bytecode "+type.getName());ByteArrayOutputStream output=new ByteArrayOutputStream();
        try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)output.write(buffer,0,n);}finally{input.close();}
        return new String(output.toByteArray(),"ISO-8859-1");}
    private static String hash(File file)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");
        InputStream input=new FileInputStream(file);try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)digest.update(buffer,0,n);}finally{input.close();}
        StringBuilder result=new StringBuilder();for(byte value:digest.digest())result.append(String.format("%02X",value&255));return result.toString();}
    private static File requireFile(File root,String path){File file=new File(root,path);require(file.isFile(),"missing resource "+path);return file;}
    private static boolean close(float a,float b){return Math.abs(a-b)<.0001F;}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private MossyCobblestoneFamilyContractFixture120(){}
}
