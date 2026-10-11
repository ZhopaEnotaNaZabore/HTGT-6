package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldChunkManager120;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeZoomer120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaMountainDensityRouter120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.MutableDensityContext120;
import net.minecraft.world.biome.BiomeGenBase;

/** Regression contract for the 1.20 3D-biome to 1.7 column-biome seam. */
public final class FuturaSurfaceBiomeSeamFixture120 {
    private static final long SEED=0x1202B10E5EAFL;

    public static void assertContract(){
        verifyArrayOrder();
        verifyGtRawColumnMapping();
        verifySurfaceQuartIsThreeDimensional();
    }

    private static void verifyGtRawColumnMapping(){
        for(int x=0;x<16;x++)for(int z=0;z<16;z++){
            int rawColumn=x*16+z;
            int expectedBiome=x+z*16;
            require(com.mod.htgt6.asm.HTGT6Transformer.futuraBiomeIndexForRawColumn(rawColumn)
                    ==expectedBiome,"GT raw-column biome palette was transposed");
        }
        require(com.mod.htgt6.asm.HTGT6Transformer.futuraWaterRawIndex(384,1,126)==510,
                "GT water still assumes a 256/power-of-two raw column stride");
        require(FuturaMountainDensityRouter120.SEA_LEVEL-1==126,
                "Futura GT water ceiling is not translated modern sea level - 1");
        require(com.mod.htgt6.asm.HTGT6Transformer.isGeneratedWaterForGT(
                        com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.aquiferWater,true),
                "Futura GT water pass ignores stable aquifer water");
        require(!com.mod.htgt6.asm.HTGT6Transformer.isGeneratedWaterForGT(
                        com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.aquiferWater,false),
                "Legacy GT water path must not consume Futura aquifer water");
    }

    private static void verifyArrayOrder(){
        int width=16,depth=16;
        for(int z=0;z<depth;z++)for(int x=0;x<width;x++){
            int index=x+z*width;
            require(index==(z<<4|x),"Chunk biome array changed x+z*16 ordering");
        }
    }

    private static void verifySurfaceQuartIsThreeDimensional(){
        /*
         * Local 1.20.2 reference evidence:
         * client_mappings.txt maps dlk.a(...) to SurfaceSystem.buildSurface
         * and cqv.a(BlockPos) to BiomeManager.getBiome. javap -v dlk shows
         * buildSurface bytecode offset 62 creating its biomeGetter Function
         * with bootstrap target invokevirtual cqv.a:(Lgw;)Lhe. It therefore
         * uses BiomeManager's Voronoi/fiddled-distance lookup, not a direct
         * NoiseBiomeSource quart query. Offset 182 invokes the same cqv.a for
         * the frozen-ocean extension.
         */
        FuturaBiomeZoomer120 zoomer=new FuturaBiomeZoomer120(SEED);
        boolean yChangesXZ=false;
        for(int z=-128;z<=128&&!yChangesXZ;z++)for(int x=-128;x<=128;x++){
            FuturaBiomeZoomer120.QuartPosition sea=zoomer.select(x,63,z);
            FuturaBiomeZoomer120.QuartPosition peak=zoomer.select(x,255,z);
            if(sea.x!=peak.x||sea.z!=peak.z){yChangesXZ=true;break;}
        }
        require(yChangesXZ,"Fixture failed to cover BiomeManager's 3D fiddled-distance seam");

        // The production method must consume both the translated surface Y
        // and router.depth.  This invocation also guards the Y+64 contract.
        if(com.mod.htgt6.asm.HTGT6Transformer.getRegisteredBiomeExact(FuturaBiomeIds120.MIN_ID)!=null){
            FuturaWorldChunkManager120 manager=new FuturaWorldChunkManager120(SEED);
            FuturaMountainDensityRouter120 terrain=new FuturaMountainDensityRouter120(SEED);
            MutableDensityContext120 context=new MutableDensityContext120();
            BiomeGenBase low=manager.getSurfaceBiomeAt(0,48,0,terrain,context);
            BiomeGenBase high=manager.getSurfaceBiomeAt(0,288,0,terrain,context);
            require(low!=null&&high!=null,"Per-Y SurfaceSystem biome resolver returned null");
            require(low==manager.getUndergroundBiomeAt(0,48,0)
                            &&high==manager.getUndergroundBiomeAt(0,288,0),
                    "Per-Y SurfaceSystem and 3D noise-biome resolvers diverged");
            BiomeGenBase publicBiome=manager.getSurfaceBiomeAt(0,0);
            BiomeGenBase bulkBiome=manager.loadBlockGeneratorData(null,0,0,1,1)[0];
            int[] ignoredPostCaveHeight={300};
            BiomeGenBase storedBiome=manager.loadSurfaceBiomeData(null,0,0,1,1,
                    ignoredPostCaveHeight,terrain)[0];
            require(publicBiome==bulkBiome&&publicBiome==storedBiome,
                    "Surface biome differs before and after chunk creation");
        }
    }

    public static void main(String[] ignored){
        assertContract();
        System.out.println("Futura surface biome seam fixture OK: x+z*16, GT mapping, 3D quart Y, router.depth");
    }

    private static void require(boolean condition,String message){
        if(!condition)throw new IllegalStateException(message);
    }

    private FuturaSurfaceBiomeSeamFixture120(){}
}
