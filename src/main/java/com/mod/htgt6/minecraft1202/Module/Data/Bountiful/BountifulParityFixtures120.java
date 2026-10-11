package com.mod.htgt6.minecraft1202.Module.Data.Bountiful;

import com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelBountifulArmorStand;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.ModelBountifulEndermite;
import com.mod.htgt6.minecraft1202.Module.Client.Shared.TileEntityBountifulBannerRenderer;
import com.mod.htgt6.client.inventory112.OffhandRenderer112;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.World.Bountiful.BountifulRockGenerator120;
import com.mod.htgt6.minecraft1202.Module.World.Bountiful.OceanMonumentGenerator;
import com.mod.htgt6.minecraft1202.Module.Data.Bountiful.OceanMonumentData120;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionHelper;
import net.minecraftforge.client.IItemRenderer;

/** Headless acceptance gate for the HTGT6-owned Bountiful/1.20.2 compatibility layer. */
public final class BountifulParityFixtures120 {
    private static final List<String[]> ASSETS=new ArrayList<String[]>();
    static {
        block("granite","granite");block("polished_granite","polished_granite");
        block("diorite","diorite");block("polished_diorite","polished_diorite");
        block("andesite","andesite");block("polished_andesite","polished_andesite");
        block("prismarine","prismarine");block("prismarine_bricks","prismarine_bricks");
        block("dark_prismarine","dark_prismarine");block("sea_lantern","sea_lantern");
        block("slime_block","slime_block");block("sponge","sponge");block("wet_sponge","sponge_wet");
        block("iron_trapdoor","iron_trapdoor");block("red_sandstone","red_sandstone_normal");
        block("chiseled_red_sandstone","red_sandstone_carved");block("cut_red_sandstone","red_sandstone_smooth");
        block("red_sandstone_top","red_sandstone_top");block("red_sandstone_bottom","red_sandstone_bottom");
        item("rabbit","rabbit_raw");item("cooked_rabbit","rabbit_cooked");item("rabbit_hide","rabbit_hide");
        item("rabbit_foot","rabbit_foot");item("rabbit_stew","rabbit_stew");item("mutton","mutton_raw");
        item("cooked_mutton","mutton_cooked");item("prismarine_shard","prismarine_shard");
        item("prismarine_crystals","prismarine_crystals");item("armor_stand","wooden_armorstand");
        entity("guardian.png","guardian.png");entity("guardian_elder.png","guardian_elder.png");
        entity("guardian_beam.png","guardian_beam.png");entity("endermite.png","endermite.png");
        for(String rabbit:new String[]{"brown","white","black","white_splotched","gold","salt","toast","caerbannog"})
            ASSETS.add(pair("assets/minecraft/textures/entity/rabbit/"+rabbit+".png","textures/entity/bountiful/rabbit/"+rabbit+".png"));
        ASSETS.add(pair("assets/minecraft/textures/entity/armorstand/wood.png","textures/entity/bountiful/armor_stand.png"));
        for(String banner:new String[]{"base","bricks","circle","creeper","cross","flower","globe","gradient","mojang","piglin","skull"})
            ASSETS.add(pair("assets/minecraft/textures/entity/banner/"+banner+".png","textures/entity/bountiful/banner/"+banner+".png"));
    }

    public static void main(String[] args)throws Exception {
        require(BountifulRockGenerator120.VEIN_SIZE==64&&BountifulRockGenerator120.LOWER_COUNT==2
                &&BountifulRockGenerator120.UPPER_RARITY==6,"six modern rock-placement contracts");
        require(OceanMonumentGenerator.baseY(false)==39&&OceanMonumentGenerator.baseY(true)==103,
                "monument physical Y translation");
        verifyMonumentPersistence();
        require(BountifulContent.stone!=null&&BountifulContent.prismarine!=null&&BountifulContent.coarseDirt!=null,
                "Bountiful registry identities");
        verifyBrewing(); verifyModels(); verifyBannerRendering(); verifyConfigs(args); verifyAssets(args);
        System.out.println("Bountiful 1.20.2 parity OK: registry, brewing, worldgen, entities, client assets and Et Futurum ownership");
    }

    private static void verifyMonumentPersistence(){OceanMonumentData120 first=new OceanMonumentData120();
        first.recordStart(-17,29);require(first.claimMob(-17,29,1),"first monument mob claim");
        require(!first.claimMob(-17,29,1),"monument mob slot idempotence");
        net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();first.writeToNBT(tag);
        OceanMonumentData120 loaded=new OceanMonumentData120();loaded.readFromNBT(tag);
        int centreX=(-17<<4)+8,centreZ=(29<<4)+8;
        require(loaded.hasStart(-17,29)&&!loaded.claimMob(-17,29,1),"monument persistence round-trip");
        require(loaded.isInsideMonument(centreX-29,centreZ-29)
                &&loaded.isInsideMonument(centreX+28,centreZ+28)
                &&!loaded.isInsideMonument(centreX-30,centreZ)
                &&!loaded.isInsideMonument(centreX+29,centreZ),"monument guardian spawn bounds");}

    @SuppressWarnings("unchecked") private static void verifyBrewing(){
        BountifulContent.installLeapingBrewing();
        int result=PotionHelper.applyIngredient(16,"+0+1-2+3&4-4+13");
        List<PotionEffect> effects=PotionHelper.getPotionEffects(result,false);boolean jump=false;
        if(effects!=null)for(PotionEffect effect:effects)if(effect.getPotionID()==Potion.jump.id)jump=true;
        require(jump,"rabbit foot must brew a vanilla jump potion (result="+result+", effects="+effects+")");
    }

    private static void verifyModels()throws Exception {
        ModelBountifulArmorStand stand=new ModelBountifulArmorStand();
        require(stand.bipedHead.cubeList.size()==1&&stand.bipedBody.cubeList.size()==1
                &&stand.bipedRightArm.cubeList.size()==1&&stand.bipedLeftArm.cubeList.size()==1,
                "complete armor stand limb geometry");
        ModelBountifulEndermite mite=new ModelBountifulEndermite();
        java.lang.reflect.Field field=ModelBountifulEndermite.class.getDeclaredField("body");field.setAccessible(true);
        require(((ModelRenderer[])field.get(mite)).length==4,"four-segment endermite model");
    }

    private static void verifyBannerRendering()throws Exception {
        require(IItemRenderer.class.isAssignableFrom(TileEntityBountifulBannerRenderer.class),
                "patterned banners must use the layered renderer as items");
        TileEntityBountifulBannerRenderer renderer=new TileEntityBountifulBannerRenderer();
        require(renderer.handleRenderType(new net.minecraft.item.ItemStack(BountifulContent.banner),
                        IItemRenderer.ItemRenderType.INVENTORY),
                "banner inventory must use its dynamic layered NBT renderer");
        require(renderer.shouldUseRenderHelper(IItemRenderer.ItemRenderType.INVENTORY,
                        new net.minecraft.item.ItemStack(BountifulContent.banner),
                        IItemRenderer.ItemRendererHelper.INVENTORY_BLOCK),
                "banner inventory must request the Forge 3D projection helper");
        require(renderer.handleRenderType(new net.minecraft.item.ItemStack(BountifulContent.banner),
                        IItemRenderer.ItemRenderType.EQUIPPED),
                "equipped banners must retain the layered 3D renderer");
        require(OffhandRenderer112.offhandRenderPassCount(true,2)==1,
                "custom banner renderer must own its layers in the offhand");
        require(OffhandRenderer112.offhandRenderPassCount(false,2)==2,
                "ordinary multi-pass items retain both offhand icon layers");
        String[] values={"bl","square_bottom_left","br","square_bottom_right","tl","square_top_left",
                "tr","square_top_right","bs","stripe_bottom","ts","stripe_top","ls","stripe_left",
                "rs","stripe_right","cs","stripe_center","ms","stripe_middle","drs","stripe_downright",
                "dls","stripe_downleft","ss","small_stripes","cr","cross","sc","straight_cross",
                "bt","triangle_bottom","tt","triangle_top","bts","triangles_bottom","tts","triangles_top",
                "ld","diagonal_left","rud","diagonal_up_right","lud","diagonal_up_left","rd","diagonal_right",
                "mc","circle","mr","rhombus","vh","half_vertical","hh","half_horizontal",
                "vhr","half_vertical_right","hhb","half_horizontal_bottom","bo","border","cbo","curly_border",
                "gra","gradient","gru","gradient_up","bri","bricks","cre","creeper","sku","skull",
                "flo","flower","moj","mojang","glb","globe","pig","piglin"};
        for(int i=0;i<values.length;i+=2){
            String mapped=TileEntityBountifulBannerRenderer.textureName(values[i]);
            require(values[i+1].equals(mapped),"wrong banner mask mapping for "+values[i]);
            InputStream stream=resource("textures/entity/bountiful/banner/"+mapped+".png");
            require(stream!=null,"missing layered banner mask "+mapped);stream.close();
        }
    }

    private static void verifyConfigs(String[] args)throws Exception {
        for(int i=0;i<Math.min(2,args.length);i++){
            String text=new String(read(new FileInputStream(args[i])),"UTF-8");
            for(String key:new String[]{"enableArmorStand","enableIronTrapdoor","enableSlimeBlock","enableSponge",
                    "enableBanners","enableBarrier","enableMutton","enableCoarseDirt","enablePrismarine",
                    "enableRedSandstone","enableStones"})require(text.contains("B:"+key+"=false"),"Et Futurum duplicate "+key);
        }
    }

    private static void verifyAssets(String[] args)throws Exception {
        for(String[] mapping:ASSETS){InputStream stream=resource(mapping[1]);require(stream!=null,"missing "+mapping[1]);stream.close();}
        if(args.length<3||!new File(args[2]).isFile())return;
        ZipFile reference=new ZipFile(args[2]);try{for(String[] mapping:ASSETS){
            ZipEntry entry=reference.getEntry(mapping[0]);require(entry!=null,"reference "+mapping[0]);
            require(com.mod.htgt6.minecraft1202.Module.Data.Shared.ResourceOracle120.matches(read(resource(mapping[1])),mapping[0].substring("assets/minecraft/".length())),"pixel mismatch "+mapping[1]);
        }}finally{reference.close();}
    }

    private static void block(String source,String target){ASSETS.add(pair("assets/minecraft/textures/block/"+source+".png","textures/blocks/bountiful/"+target+".png"));}
    private static void item(String source,String target){ASSETS.add(pair("assets/minecraft/textures/item/"+source+".png","textures/items/bountiful/"+target+".png"));}
    private static void entity(String source,String target){ASSETS.add(pair("assets/minecraft/textures/entity/"+source,"textures/entity/bountiful/"+target));}
    private static String[] pair(String a,String b){return new String[]{a,b};}
    private static InputStream resource(String path){return BountifulParityFixtures120.class.getResourceAsStream("/assets/htgt6/"+path);}
    private static byte[] read(InputStream stream)throws Exception{require(stream!=null,"resource stream");try{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];for(int n;(n=stream.read(buffer))>=0;)out.write(buffer,0,n);return out.toByteArray();}finally{stream.close();}}
    private static void require(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    private BountifulParityFixtures120(){}
}
