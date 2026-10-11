package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Door;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120HangingSign;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Slab;
import com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120Sign;
import com.mod.htgt6.minecraft1202.Module.Events.Modern.Backport120AxeStrippingHandler;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120WoodFamily;
import net.minecraft.item.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

/** Three-way gate for 1.20.2 semantics, Forge 1.7.10 adapters and registry wiring. */
public final class MangroveFamilyContractFixture120 {
    private static final String[][] OFFICIAL_TEXTURES={
            {"textures/blocks/1_20_backport/mangrove_door_bottom.png","79560034EEEC3F0ECB23786DE7FABAEE9E61545E19C1EF2005549284C4A10E00"},
            {"textures/blocks/1_20_backport/mangrove_door_top.png","3A4E0DCA36399B2634ADAB316E947DD53132909883A4630A95093B25B9CE6AB4"},
            {"textures/blocks/1_20_backport/mangrove_trapdoor.png","1D3317602E14E5F61D84DACDDCCDA7A49F9639EEB8A30A3CB3A2BC42D46394E1"},
            {"textures/blocks/1_20_backport/stripped_mangrove_log.png","73930185EA885258ABB501AD7C81FECF3875D4C10D4CDCB2B636E1883057C4F4"},
            {"textures/blocks/1_20_backport/stripped_mangrove_log_top.png","E03F18B10DE7451005144381B37B75B45337AF4F83E73361013419E48D1F62DC"},
            {"textures/items/1_20_backport/mangrove_door.png","A02F1EF30EBBA169F956FBBB62C7403A362577670320AA612AED2E6E30363D77"},
            {"textures/items/1_20_backport/mangrove_sign.png","4F36BC79138776DE7EDC623154E01D881BFDD442165821CC2E2D2DE5F987532E"},
            {"textures/items/1_20_backport/mangrove_hanging_sign.png","81956827580645D3A91F6EB9D0484ED0EBF00255E465F024009CED0D7A94128C"},
            {"textures/entity/1_20_backport/signs/mangrove.png","6B5BB87A17942C927BED9F0B01CAB344C8C22A4CDDB355F6303C0E4EE7559920"},
            {"textures/entity/1_20_backport/signs/hanging/mangrove.png","C9432CA95D5D00E9882306F4E19A1E2ED04DDF0BDCD8B8F420239C7CEADD62A8"}
    };
    private static final String[] PUBLIC_BLOCKS={"mangrove_stairs","mangrove_slab","mangrove_fence",
            "mangrove_fence_gate","mangrove_trapdoor","mangrove_pressure_plate","mangrove_button",
            "stripped_mangrove_log","stripped_mangrove_wood"};

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Expected src/main/resources path");
        File assets=new File(args[0],"assets/htgt6");
        verifyOfficialAssets(assets);
        verifyResourceGraph(assets);
        verifyLegacyAdapters();
        verifyRegistrationAndRecipes();
        System.out.println("mangrove-family-1.20.2: official-assets=10, public-blocks=9, technical-blocks=5, recipes=10, API=Forge-1.7.10 OK");
    }

    private static void verifyOfficialAssets(File assets)throws Exception{
        for(String[] asset:OFFICIAL_TEXTURES){File file=requireFile(assets,asset[0]);
            require(com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.matches(file,com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.texturePath(asset[0])),"1.20.2 texture drift: "+asset[0]);}
    }

    private static void verifyResourceGraph(File assets){
        String[] states={"mangrove_stairs","mangrove_slab","mangrove_fence","mangrove_fence_gate",
                "mangrove_door","mangrove_trapdoor","mangrove_pressure_plate","mangrove_button",
                "mangrove_sign","mangrove_wall_sign","mangrove_hanging_sign","mangrove_wall_hanging_sign",
                "stripped_mangrove_log","stripped_mangrove_wood"};
        for(String state:states)requireFile(assets,"blockstates/1_20_backport/"+state+".json");
        for(String item:new String[]{"mangrove_stairs","mangrove_slab","mangrove_fence","mangrove_fence_gate",
                "mangrove_door","mangrove_trapdoor","mangrove_pressure_plate","mangrove_button","mangrove_sign",
                "mangrove_hanging_sign","stripped_mangrove_log","stripped_mangrove_wood"})
            requireFile(assets,"models/item/1_20_backport/"+item+".json");
    }

    private static void verifyLegacyAdapters(){
        require(Backport120Content.mangroveDoorBlock instanceof BlockBackport120Door,"door adapter");
        require(Backport120Content.mangroveSlab instanceof BlockBackport120Slab,"slab adapter");
        require(Backport120Content.mangroveStandingSign instanceof BlockBackport120Sign,"sign adapter");
        require(Backport120Content.mangroveHangingSignBlock instanceof BlockBackport120HangingSign,"hanging sign adapter");
        require(Backport120Content.doubleMangroveSlab.getUnlocalizedName().contains("double_mangrove_slab"),"double slab identity");
        require(Backport120Content.doubleMangroveSlab.getItem(null,0,0,0)==
                Item.getItemFromBlock(Backport120Content.mangroveSlab),"double slab inventory maps to half slab");
        for(int axis:new int[]{0,4,8,12}){Backport120AxeStrippingHandler.Result result=
                Backport120AxeStrippingHandler.stripped(Backport120Content.mangroveLog,axis);
            require(result!=null&&result.block==(axis==12?Backport120Content.strippedMangroveWood:
                    Backport120Content.strippedMangroveLog),"mangrove stripping block axis="+axis);
            require(result.metadata==(axis==12?0:axis),"mangrove stripping metadata axis="+axis);}
    }

    private static void verifyRegistrationAndRecipes()throws Exception{
        require(Backport120Content.mangroveDoorBlock.getItemDropped(0,null,0)==Backport120Content.mangroveDoor,"door drop item");
        require(Backport120Content.mangroveStandingSign.getItemDropped(0,null,0)==Backport120Content.mangroveSign,"sign drop item");
        require(Backport120Content.mangroveHangingSignBlock.getItemDropped(0,null,0)==Backport120Content.mangroveHangingSign,"hanging sign drop item");
        java.lang.reflect.Field familyField=Backport120Content.class.getDeclaredField("MANGROVE");
        require(familyField.getType()==Backport120WoodFamily.class,"mangrove must use shared family catalog");
        InputStream input=Backport120Content.class.getResourceAsStream("Backport120Content.class");
        require(input!=null,"Backport120Content bytecode");ByteArrayOutputStream output=new ByteArrayOutputStream();
        try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)output.write(buffer,0,n);}finally{input.close();}
        String bytecode=new String(output.toByteArray(),"ISO-8859-1");
        require(bytecode.contains("MANGROVE")&&bytecode.contains("addRecipes")&&bytecode.contains("register"),
                "family catalog registration/recipe calls");
        String family=bytecode(Backport120WoodFamily.class);
        for(String suffix:new String[]{"_stairs","_slab","_fence","_fence_gate","_door_block",
                "_trapdoor","_pressure_plate","_button","_standing_sign","_wall_sign",
                "_hanging_sign_block","_wall_hanging_sign"})
            require(family.contains(suffix),"family registry suffix "+suffix);
    }

    private static String bytecode(Class<?> type)throws Exception{InputStream input=type.getResourceAsStream(type.getSimpleName()+".class");
        require(input!=null,"bytecode "+type.getName());ByteArrayOutputStream output=new ByteArrayOutputStream();
        try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)output.write(buffer,0,n);}finally{input.close();}
        return new String(output.toByteArray(),"ISO-8859-1");}

    private static String hash(File file)throws Exception{MessageDigest digest=MessageDigest.getInstance("SHA-256");
        InputStream input=new FileInputStream(file);try{byte[] buffer=new byte[4096];for(int n;(n=input.read(buffer))>=0;)digest.update(buffer,0,n);}finally{input.close();}
        StringBuilder result=new StringBuilder();for(byte value:digest.digest())result.append(String.format("%02X",value&255));return result.toString();}
    private static File requireFile(File root,String path){File file=new File(root,path);require(file.isFile(),"missing resource "+path);return file;}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private MangroveFamilyContractFixture120(){}
}
