package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Stairs;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import net.minecraft.item.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;

/** Contract for the smooth-sandstone stair/slab states used by modern village templates. */
public final class SmoothSandstoneFamilyContractFixture120 {
    private static final Charset UTF8=Charset.forName("UTF-8");
    /** Official 1.20.2 sandstone_top.png; smooth sandstone deliberately reuses these pixels. */
    private static final String OFFICIAL_TEXTURE_SHA256=
            "321C39B772533ABF1B6441D484D05E8E7BF1FA017C97ACAFCAEC9E63781CB40C";

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected src/main/resources path");
        File assets=new File(args[0],"assets/htgt6");
        verifyReferenceAssets(assets);
        verifyForgeAdapters();
        verifyLifecycleWiring();
        System.out.println("smooth-sandstone-family-1.20.2: stairs + half/double slab, exact texture, recipes and Forge-1.7 wiring OK");
    }

    private static void verifyReferenceAssets(File assets)throws Exception{
        require(com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.matches(requireFile(assets,
                "textures/blocks/1_20_backport/smooth_sandstone.png"),"textures/block/sandstone_top.png"),"official sandstone_top texture drift");
        for(String path:new String[]{"blockstates/1_20_backport/smooth_sandstone_stairs.json",
                "blockstates/1_20_backport/smooth_sandstone_slab.json",
                "models/block/1_20_backport/smooth_sandstone.json",
                "models/block/1_20_backport/smooth_sandstone_stairs.json",
                "models/block/1_20_backport/smooth_sandstone_stairs_inner.json",
                "models/block/1_20_backport/smooth_sandstone_stairs_outer.json",
                "models/block/1_20_backport/smooth_sandstone_slab.json",
                "models/block/1_20_backport/smooth_sandstone_slab_top.json",
                "models/item/1_20_backport/smooth_sandstone_stairs.json",
                "models/item/1_20_backport/smooth_sandstone_slab.json"}){
            String json=new String(Files.readAllBytes(requireFile(assets,path).toPath()),UTF8);
            require(json.contains("smooth_sandstone"),"wrong model family: "+path);
            require(!json.contains("mossy_cobblestone"),"copied model retained source family: "+path);
        }
    }

    private static void verifyForgeAdapters(){
        require(EtFVanillaGapContent120.smoothSandstoneStairs instanceof BlockBackport120Stairs,"stairs adapter");
        require(EtFVanillaGapContent120.smoothSandstoneSlab instanceof BlockBackport120Slab,"slab adapter");
        require(EtFVanillaGapContent120.doubleSmoothSandstoneSlab instanceof BlockBackport120Slab,"double slab adapter");
        require(close(EtFVanillaGapContent120.smoothSandstoneStairs.getBlockHardness(null,0,0,0),
                EtFVanillaGapContent120.smoothSandstone.getBlockHardness(null,0,0,0)),"stairs hardness/source semantics");
        require(close(EtFVanillaGapContent120.smoothSandstoneSlab.getBlockHardness(null,0,0,0),
                EtFVanillaGapContent120.smoothSandstone.getBlockHardness(null,0,0,0)),"slab hardness/source semantics");
        require(EtFVanillaGapContent120.doubleSmoothSandstoneSlab.getItem(null,0,0,0)==
                Item.getItemFromBlock(EtFVanillaGapContent120.smoothSandstoneSlab),"double slab inventory maps to half slab");
    }

    private static void verifyLifecycleWiring()throws Exception{
        String registry=bytecode(EtFVanillaGapContent120.class);
        require(registry.contains("backport_1_20_smooth_sandstone_slab"),"half slab registration");
        require(registry.contains("backport_1_20_double_smooth_sandstone_slab"),"technical double slab registration");
        require(registry.contains("smooth_sandstone_stairs"),"stairs registration and recipe");
        require(registry.contains("smoothSandstoneStairs")&&registry.contains("smoothSandstoneSlab"),"recipe outputs");
        String client=bytecode(Minecraft120BackportClient.class);
        require(client.contains("smooth_sandstone_stairs")&&client.contains("smooth_sandstone_slab"),"client JSON bindings");
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
    private SmoothSandstoneFamilyContractFixture120(){}
}
