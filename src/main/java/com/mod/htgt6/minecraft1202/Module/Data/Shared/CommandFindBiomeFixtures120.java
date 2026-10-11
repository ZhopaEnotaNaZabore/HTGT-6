package com.mod.htgt6.minecraft1202.Module.Data.Shared;

import com.mod.htgt6.minecraft1202.Module.World.Shared.CommandFindBiome120;

import com.mod.htgt6.asm.HTGT6Transformer;
import net.minecraft.world.biome.BiomeGenBase;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaWorldChunkManager120;

/** Headless command resolver contract; runtime search uses WorldChunkManager without chunk loads. */
public final class CommandFindBiomeFixtures120 {
    public static void main(String[] args){
        require(CommandFindBiome120.SEARCH_RADIUS==1500,
                "Search radius must stay bounded to 1500 blocks in every horizontal direction");
        require(CommandFindBiome120.resolve("1")==BiomeGenBase.plains,"Vanilla numeric ID lookup failed");
        require(CommandFindBiome120.resolve("plains")==BiomeGenBase.plains,"Vanilla name lookup failed");
        require(CommandFindBiome120.resolve("minecraft:ice_plains")==BiomeGenBase.icePlains,
                "Namespaced/underscore lookup failed");
        require(CommandFindBiome120.resolve("Ice Plains")==BiomeGenBase.icePlains,
                "Multi-word display-name lookup failed");
        require(HTGT6Transformer.getRegisteredBiomesSnapshot120().contains(BiomeGenBase.plains),
                "Registry snapshot omitted vanilla biomes");
        require(CommandFindBiome120.resolve("2147483647")==null,"Unknown numeric biome must be rejected");
        require(CommandFindBiome120.isCaveBiomeId(FuturaBiomeIds120.LUSH_CAVES)
                &&CommandFindBiome120.isCaveBiomeId(FuturaBiomeIds120.DRIPSTONE_CAVES)
                &&CommandFindBiome120.isCaveBiomeId(FuturaBiomeIds120.DEEP_DARK),
                "All three 1.20 cave biomes must use 3D lookup");
        require(!CommandFindBiome120.isCaveBiome(BiomeGenBase.plains),"Surface biome misclassified as cave");
        FuturaWorldChunkManager120 manager=new FuturaWorldChunkManager120(0x1202CAFE1L);
        BiomeGenBase routed=null;
        for(int y=2;y<=382&&routed==null;y+=16)routed=manager.getUndergroundBiomeAt(0,y,0);
        require(routed!=null,"3D terrain.depth lookup returned no resolvable biome at fixture origin");
        CommandFindBiome120.Result found=CommandFindBiome120.findNearestUnderground(manager,routed,
                0,0,1500,16,16,382,System.nanoTime()+10000000000L);
        require(found!=null&&Math.abs(found.x)<=1500&&Math.abs(found.z)<=1500,
                "3D search result escaped the 1500-block horizontal square");
        require(manager.getUndergroundBiomeAt(found.x,found.y,found.z).biomeID==routed.biomeID,
                "3D search result did not resolve back to its selected biome");
        System.out.println("/findbiome fixtures OK; radius=1500 square, surface resolver and 3D cave routing");
    }
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private CommandFindBiomeFixtures120(){ }
}
