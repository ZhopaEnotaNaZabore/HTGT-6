package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeRegistry120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaOverworldSurfaceRules120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceContext120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceRules120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceSystem120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldChunkManager120;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.World.Modern.OverworldBiomeParameterMap120;
import net.minecraft.world.biome.BiomeGenBase;

/** Climate-to-biome seam and peak SurfaceRules material contract. */
public final class FuturaBiomeFeatureWiringFixture120 {
    public static void assertContract(){
        verifyFamilies();verifyUndergroundLookup();verifyMountainSurfaces();
    }

    public static void main(String[] ignored){
        if(com.mod.htgt6.asm.HTGT6Transformer.getRegisteredBiomeExact(FuturaBiomeIds120.MIN_ID)==null){
            verifyFamilies();
            System.out.println("Futura biome parameter families fixture OK; full material wiring runs after extended-biome ASM startup");
            return;
        }
        assertContract();
        System.out.println("Futura biome feature wiring fixture OK");
    }

    private static void verifyFamilies(){
        boolean lush=false,snowy=false,jagged=false,frozen=false,stony=false;
        for(OverworldBiomeParameterMap120.Entry entry:new OverworldBiomeParameterMap120().entries()){
            lush|=entry.biome==OverworldBiomeParameterMap120.Biome.LUSH_CAVES;
            snowy|=entry.biome==OverworldBiomeParameterMap120.Biome.SNOWY_SLOPES;
            jagged|=entry.biome==OverworldBiomeParameterMap120.Biome.JAGGED_PEAKS;
            frozen|=entry.biome==OverworldBiomeParameterMap120.Biome.FROZEN_PEAKS;
            stony|=entry.biome==OverworldBiomeParameterMap120.Biome.STONY_PEAKS;
        }
        require(lush&&snowy&&jagged&&frozen&&stony,"Climate map lost cave/peak parameter families");
    }

    private static void verifyUndergroundLookup(){
        FuturaWorldChunkManager120 manager=new FuturaWorldChunkManager120(0x1202CAFE1L);
        boolean found=false;
        for(int z=-8192;z<=8192&&!found;z+=257)for(int x=-8192;x<=8192;x+=251){
            BiomeGenBase biome=manager.getUndergroundBiomeAt(x,48,z,.5F);
            if(biome!=null&&biome.biomeID==FuturaBiomeIds120.LUSH_CAVES){found=true;break;}
        }
        require(found,"3D climate lookup never resolves registered Lush Caves");
    }

    /** Exact mountain portion of 1.20.2 SurfaceRuleData.overworldLike. */
    private static void verifyMountainSurfaces(){
        FuturaSurfaceSystem120 system=new FuturaSurfaceSystem120(0x1202FACE5EEDL);
        BiomeGenBase slopes=FuturaBiomeRegistry120.get(FuturaBiomeIds120.SNOWY_SLOPES);
        BiomeGenBase grove=FuturaBiomeRegistry120.get(FuturaBiomeIds120.GROVE);
        BiomeGenBase frozen=FuturaBiomeRegistry120.get(FuturaBiomeIds120.FROZEN_PEAKS);
        BiomeGenBase jagged=FuturaBiomeRegistry120.get(FuturaBiomeIds120.JAGGED_PEAKS);
        BiomeGenBase stony=FuturaBiomeRegistry120.get(FuturaBiomeIds120.STONY_PEAKS);
        int[] powder=findNoise(system,FuturaSurfaceSystem120.NOISE_POWDER_SNOW,.45D,.58D,null,0,0);
        FuturaSurfaceContext120 context=new FuturaSurfaceContext120().setColumn(3,0,100,false)
                .setBlock(120,1,8,Integer.MIN_VALUE,true);
        assertBlock("snowy-slopes floor powder_snow [.35,.60]",select(system,context,slopes,powder),Backport120Content.powderSnow);
        assertBlock("grove floor powder_snow [.35,.60]",select(system,context,grove,powder),Backport120Content.powderSnow);
        context.setBlock(120,2,8,Integer.MIN_VALUE,true);
        assertBlock("snowy-slopes under-floor powder_snow [.45,.58]",select(system,context,slopes,powder),Backport120Content.powderSnow);
        assertBlock("grove under-floor powder_snow [.45,.58]",select(system,context,grove,powder),Backport120Content.powderSnow);

        // Vanilla nests both powder-snow bands in waterBlockCheck(0, 0).
        // water(-1) is true here but water(0) is false, reproducing the
        // shallow-water edge which used to receive an invalid powder layer.
        context.setBlock(120,1,8,121,true);
        require(select(system,context,slopes,powder)==null,"Snowy Slopes leaked powder snow below WATER_BLOCK_CHECK(0,0)");
        require(select(system,context,grove,powder)==null,"Grove leaked powder snow below WATER_BLOCK_CHECK(0,0)");
        context.setBlock(120,2,8,121,true);
        require(select(system,context,slopes,powder)==null,"Snowy Slopes under-floor powder snow ignored water check");
        assertBlock("grove submerged under-floor fallback",select(system,context,grove,powder),net.minecraft.init.Blocks.dirt);

        int[] packed=findNoise(system,FuturaSurfaceSystem120.NOISE_PACKED_ICE,0.0D,.2D,null,0,0);
        context.setColumn(3,0,100,false).setBlock(120,1,8,Integer.MIN_VALUE,true);
        assertBlock("frozen-peaks floor packed_ice [0,.2]",select(system,context,frozen,packed),net.minecraft.init.Blocks.packed_ice);
        context.setColumn(3,0,100,true).setBlock(120,1,8,Integer.MIN_VALUE,true);
        assertBlock("frozen-peaks steep floor",select(system,context,frozen,packed),net.minecraft.init.Blocks.packed_ice);
        context.setBlock(120,2,8,Integer.MIN_VALUE,true);
        assertBlock("frozen-peaks steep under-floor",select(system,context,frozen,packed),net.minecraft.init.Blocks.packed_ice);

        int[] ice=findNoise(system,FuturaSurfaceSystem120.NOISE_ICE,0.0D,.025D,
                FuturaSurfaceSystem120.NOISE_PACKED_ICE,0.0D,.2D);
        context.setColumn(3,0,100,false).setBlock(120,1,8,Integer.MIN_VALUE,true);
        assertBlock("frozen-peaks floor ice [0,.025]",select(system,context,frozen,ice),net.minecraft.init.Blocks.ice);

        context.setColumn(3,0,100,false).setBlock(120,1,8,Integer.MIN_VALUE,true);
        assertBlock("jagged-peaks floor snow",select(system,context,jagged,new int[]{0,0}),net.minecraft.init.Blocks.snow);
        context.setColumn(3,0,100,true);
        assertBlock("jagged-peaks steep floor",select(system,context,jagged,new int[]{0,0}),net.minecraft.init.Blocks.stone);
        context.setColumn(3,0,100,false).setBlock(120,2,8,Integer.MIN_VALUE,true);
        assertBlock("jagged-peaks under-floor",select(system,context,jagged,new int[]{0,0}),net.minecraft.init.Blocks.stone);

        int[] calcite=findNoise(system,FuturaSurfaceSystem120.NOISE_CALCITE,-.0125D,.0125D,null,0,0);
        context.setBlock(120,1,8,Integer.MIN_VALUE,true);
        assertBlock("stony-peaks calcite [-.0125,.0125]",select(system,context,stony,calcite),Backport120Content.calcite);
    }

    private static FuturaSurfaceRules120.State select(FuturaSurfaceSystem120 system,FuturaSurfaceContext120 context,
                                                       BiomeGenBase biome,int[] point){
        return FuturaOverworldSurfaceRules120.select(system,context,biome,point[0],point[1]);
    }

    private static int[] findNoise(FuturaSurfaceSystem120 system,int wanted,
                                   double minimum,double maximum,Integer excluded,
                                   double excludedMinimum,double excludedMaximum){
        for(int z=-1024;z<=1024;z+=3)for(int x=-1024;x<=1024;x+=3){
            if(!system.noiseBetween(wanted,x,z,minimum,maximum))continue;
            if(excluded!=null&&system.noiseBetween(excluded,x,z,excludedMinimum,excludedMaximum))continue;
            return new int[]{x,z};
        }
        throw new IllegalStateException("Could not find deterministic "+wanted+" mountain-noise fixture point");
    }

    private static void assertBlock(String name,FuturaSurfaceRules120.State state,net.minecraft.block.Block expected){
        require(state!=null&&state.block==expected,name+" selected "+(state==null?"null":state.block)+" instead of "+expected);
    }

    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private FuturaBiomeFeatureWiringFixture120(){ }
}
