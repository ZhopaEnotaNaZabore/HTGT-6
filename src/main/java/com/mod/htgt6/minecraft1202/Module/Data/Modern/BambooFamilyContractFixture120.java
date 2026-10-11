package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Door;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120HangingSign;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Stairs;
import com.mod.htgt6.minecraft1202.Module.Client.Modern.Minecraft120BackportClient;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.EtFVanillaGapContent120;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120WoodFamily;
import net.minecraft.item.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;

/** Three-way 1.20.2 asset, Forge 1.7 physics and lifecycle gate for the bamboo construction family. */
public final class BambooFamilyContractFixture120 {
    private static final Charset UTF8=Charset.forName("UTF-8");
    private static final String[][] OFFICIAL_TEXTURES={
            {"textures/blocks/1_20_backport/bamboo_planks.png","0FD66FC81A0E7FFE5FBBBB03A297E46255BDE0ABDC9EAC348846BBC60F9A11BD"},
            {"textures/blocks/1_20_backport/bamboo_mosaic.png","6F03DC54869F8691C8B6F04DF06A2F8CF2436FDB29E9E64D9D8E72C11C9FEE8A"},
            {"textures/blocks/1_20_backport/bamboo_block.png","15521F47BC3CAB94718810F2BE7F0656D26CB57AC433526F3963F20A1AFB231E"},
            {"textures/blocks/1_20_backport/bamboo_block_top.png","F63C2A692F7817A7C229E115261F164D4F806439FBDA1F5AE847C109D303E4C4"},
            {"textures/blocks/1_20_backport/stripped_bamboo_block.png","0A079883095AE21F783C3B4A89CDDF2CC1F5E5396307AE2053DC620866C6FF00"},
            {"textures/blocks/1_20_backport/stripped_bamboo_block_top.png","F8320DEA583FBAEAC22C06389FC304C583D733123CAC2A1FD9176DEAC560A20E"},
            {"textures/blocks/1_20_backport/bamboo_fence.png","B82E5B109DE7ADC495CF3977A9BC6026BAFE4686381C211B6A8C1EC28604EE80"},
            {"textures/blocks/1_20_backport/bamboo_fence_particle.png","BD453EE3BB6639C2FA9D67EDD4A3FC432CA9C416FDF5F261E639E262D11A3914"},
            {"textures/blocks/1_20_backport/bamboo_fence_gate.png","05A3B0A43E3843359E2E16F0C44E6A4625676D09DE9E74E618F33BAF6445CA0A"},
            {"textures/blocks/1_20_backport/bamboo_fence_gate_particle.png","BD453EE3BB6639C2FA9D67EDD4A3FC432CA9C416FDF5F261E639E262D11A3914"},
            {"textures/blocks/1_20_backport/bamboo_door_bottom.png","6C91F9E2A7388B4A502EC5F1E6120ABA1FEE784B2B033FA72DA6D0AEF59824B6"},
            {"textures/blocks/1_20_backport/bamboo_door_top.png","4568E4C54B440005B8183F9DDA7969359A73157A8BD86510E0A61EBFD6992CCA"},
            {"textures/blocks/1_20_backport/bamboo_trapdoor.png","91436B21C28C5AB45F8EDAFB66914C97BF728CB32A143EC215A6881E94050D39"},
            {"textures/items/1_20_backport/bamboo_door.png","560D8DCEC322EAC348274A5420218EF038E0F5A4EFEA3246B1AC77D9D9A48C05"},
            {"textures/items/1_20_backport/bamboo_sign.png","22F270D7A51DC23326D9F7D626150EE6B3EE049C4CC7B964BD34D88B13393874"},
            {"textures/items/1_20_backport/bamboo_hanging_sign.png","C21D83F9425409F317C32576F9E9D930DE3E0071F9DC7FFE752589402C00CBD3"},
            {"textures/entity/1_20_backport/signs/bamboo.png","2DF85E73147271CE3742741B728C660578DE51EBFD19EBA7B33C48CA0FF1D6C1"},
            {"textures/entity/1_20_backport/signs/hanging/bamboo.png","EE48B26BDD4364293B46F1F6C032AA4CAACCC879EB4ACEB26E1B531034D4D9CA"}
    };

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected src/main/resources path");
        File assets=new File(args[0],"assets/htgt6");
        verifyOfficialAssets(assets);verifyResourceGraph(assets);verifyForgeAdapters();verifyRuntimeWiring();
        System.out.println("bamboo-family-1.20.2: shapes=13, official-textures=18, custom-fence-models, recipes and Forge-1.7 wiring OK");
    }

    private static void verifyOfficialAssets(File assets)throws Exception{for(String[] asset:OFFICIAL_TEXTURES)
        require(com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.matches(requireFile(assets,asset[0]),com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.texturePath(asset[0])),"official texture drift: "+asset[0]);}

    private static void verifyResourceGraph(File assets)throws Exception{
        String[] states={"bamboo_stairs","bamboo_slab","bamboo_mosaic_stairs","bamboo_mosaic_slab","bamboo_fence",
                "bamboo_fence_gate","bamboo_door","bamboo_trapdoor","bamboo_pressure_plate","bamboo_button",
                "bamboo_sign","bamboo_wall_sign","bamboo_hanging_sign","bamboo_wall_hanging_sign"};
        for(String state:states){String path="blockstates/1_20_backport/"+state+".json";
            String json=new String(Files.readAllBytes(requireFile(assets,path).toPath()),UTF8);
            require(!json.contains("mangrove"),"copied state retained mangrove reference: "+path);}
        for(String model:new String[]{"custom_fence_post","custom_fence_side_north","custom_fence_side_east",
                "custom_fence_side_south","custom_fence_side_west","custom_fence_inventory",
                "template_custom_fence_gate","template_custom_fence_gate_open","template_custom_fence_gate_wall",
                "template_custom_fence_gate_wall_open","bamboo_fence_post","bamboo_fence_side_north",
                "bamboo_fence_side_east","bamboo_fence_side_south","bamboo_fence_side_west","bamboo_fence_inventory"})
            requireFile(assets,"models/block/1_20_backport/"+model+".json");
        for(String item:new String[]{"bamboo_stairs","bamboo_slab","bamboo_mosaic_stairs","bamboo_mosaic_slab",
                "bamboo_fence","bamboo_fence_gate","bamboo_door","bamboo_trapdoor","bamboo_pressure_plate",
                "bamboo_button","bamboo_sign","bamboo_hanging_sign"})
            requireFile(assets,"models/item/1_20_backport/"+item+".json");
    }

    private static void verifyForgeAdapters(){
        require(EtFVanillaGapContent120.bambooStairs instanceof BlockBackport120Stairs,"stairs adapter");
        require(EtFVanillaGapContent120.bambooMosaicStairs instanceof BlockBackport120Stairs,"mosaic stairs adapter");
        require(EtFVanillaGapContent120.bambooSlab instanceof BlockBackport120Slab,"slab adapter");
        require(EtFVanillaGapContent120.bambooDoorBlock instanceof BlockBackport120Door,"door adapter");
        require(EtFVanillaGapContent120.bambooHangingSignBlock instanceof BlockBackport120HangingSign,"hanging sign adapter");
        require(EtFVanillaGapContent120.doubleBambooSlab.getItem(null,0,0,0)==Item.getItemFromBlock(EtFVanillaGapContent120.bambooSlab),"double bamboo slab item");
        require(EtFVanillaGapContent120.doubleBambooMosaicSlab.getItem(null,0,0,0)==Item.getItemFromBlock(EtFVanillaGapContent120.bambooMosaicSlab),"double mosaic slab item");
        require(EtFVanillaGapContent120.bambooDoorBlock.getItemDropped(0,null,0)==EtFVanillaGapContent120.bambooDoor,"door item wiring");
        require(EtFVanillaGapContent120.bambooStandingSign.getItemDropped(0,null,0)==EtFVanillaGapContent120.bambooSign,"sign item wiring");
        require(EtFVanillaGapContent120.bambooHangingSignBlock.getItemDropped(0,null,0)==EtFVanillaGapContent120.bambooHangingSign,"hanging sign item wiring");
    }

    private static void verifyRuntimeWiring()throws Exception{
        java.lang.reflect.Field familyField=EtFVanillaGapContent120.class.getDeclaredField("BAMBOO");
        require(familyField.getType()==Backport120WoodFamily.class,"bamboo must use shared family catalog");
        String registry=bytecode(EtFVanillaGapContent120.class);
        require(registry.contains("BAMBOO")&&registry.contains("addRecipes")&&registry.contains("register"),
                "shared bamboo family registration/recipes");
        require(bytecode(Backport120WoodFamily.class).contains("_hanging_sign_block"),
                "family technical-block registration");
        String client=bytecode(Minecraft120BackportClient.class);
        for(String value:new String[]{"bamboo_stairs","bamboo_slab","bamboo_mosaic_stairs","bamboo_mosaic_slab",
                "bamboo_fence","bamboo_fence_gate","bamboo_door","bamboo_trapdoor","bamboo_pressure_plate","bamboo_button"})
            require(client.contains(value),"client binding: "+value);
    }

    private static String bytecode(Class<?> type)throws Exception{InputStream input=type.getResourceAsStream(type.getSimpleName()+".class");require(input!=null,"bytecode "+type.getName());
        ByteArrayOutputStream output=new ByteArrayOutputStream();try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)output.write(buffer,0,n);}finally{input.close();}return new String(output.toByteArray(),"ISO-8859-1");}
    private static String hash(File file)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");InputStream input=new FileInputStream(file);
        try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)digest.update(buffer,0,n);}finally{input.close();}StringBuilder result=new StringBuilder();for(byte value:digest.digest())result.append(String.format("%02X",value&255));return result.toString();}
    private static File requireFile(File root,String path){File file=new File(root,path);require(file.isFile(),"missing resource "+path);return file;}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private BambooFamilyContractFixture120(){}
}
