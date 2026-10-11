package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaAncientCityGenerator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaBiomeIds120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaVillageGenerator120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaVillagePopulation120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaRuinedPortalGenerator120;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;
import com.mod.htgt6.minecraft1202.Module.Registry.Combat.CombatContent;
import com.mod.htgt6.minecraft1202.Module.World.Combat.IglooNbtGenerator;
import com.mod.htgt6.minecraft1202.Module.World.Combat.EndCityGenerator;
import com.mod.htgt6.minecraft1202.Module.World.Exploration.WoodlandMansionGenerator112;
import com.mod.htgt6.minecraft1202.Module.World.Frostburn.FrostburnFossilGenerator;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenOceanRuin;
import com.mod.htgt6.minecraft1202.Module.World.Aquatic.WorldGenVanillaShipwreck;
import com.mod.htgt6.minecraft1202.Module.Data.Modern.VillageJigsawFixture120;

/** Groups the structure integration regressions for a single development JVM. */
public final class FuturaStructureIntegrationFixtureRunner120 {
    public static void main(String[] ignored) throws Exception {
        // Standalone Forge registry bootstrap, matching the worldgen parity fixture.
        Class<?> unsafe=Class.forName("sun.misc.Unsafe");
        java.lang.reflect.Field singleton=unsafe.getDeclaredField("theUnsafe");singleton.setAccessible(true);
        java.lang.reflect.Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);
        loader.set(null,unsafe.getMethod("allocateInstance",Class.class).invoke(singleton.get(null),cpw.mods.fml.common.Loader.class));
        net.minecraft.init.Bootstrap.func_151354_b();
        // Standalone JavaExec fixtures do not run the coremod that expands
        // Potion.potionTypes to 65536 slots. Keep content class initializers
        // inside the vanilla 32-slot registry, as the dedicated combat
        // fixtures do, so village block-mapping checks can run headlessly.
        com.mod.htgt6.asm.HTGT6Config.GLOWING_POTION_ID=24;
        com.mod.htgt6.asm.HTGT6Config.LEVITATION_POTION_ID=25;
        com.mod.htgt6.asm.HTGT6Config.LUCK_POTION_ID=26;
        com.mod.htgt6.asm.HTGT6Config.UNLUCK_POTION_ID=27;
        EndCityGenerator.validateTemplates();
        FrostburnFossilGenerator.validateTemplates();
        IglooNbtGenerator.validateTemplates();
        WoodlandMansionGenerator112.validateTemplates();
        WorldGenOceanRuin.validateTemplates();
        WorldGenVanillaShipwreck.validateTemplates();
        FuturaAncientCityGenerator120.validateTemplates();
        FuturaRuinedPortalGenerator120.validateTemplates();
        require(FuturaRuinedPortalGenerator120.mappedMetadataForFixture(
                "stone_brick_stairs","north","bottom",1)==0,
                "ruined portal stair facing must rotate with the template");
        require(FuturaRuinedPortalGenerator120.mappedMetadataForFixture(
                "stone_brick_stairs","west","top",2)==4,
                "ruined portal upside-down stair state was discarded");
        require(FuturaRuinedPortalGenerator120.mappedMetadataForFixture(
                "stone_brick_slab","north","top",0)==13,
                "ruined portal upper slab state was discarded");
        require(FuturaRuinedPortalGenerator120.mappedMetadataForFixture(
                "chest","north","bottom",1)==5,
                "ruined portal chest facing must rotate with the template");
        assertRuinedPortalSpread(0x1202f00dL,0,0);
        assertRuinedPortalSpread(0x1202f00dL,-1,-1);
        FuturaVillageGenerator120.validateTemplates();
        for(int color=0;color<Backport120Content.TERRACOTTA_COLORS.length;color++){
            String name=Backport120Content.TERRACOTTA_COLORS[color]+"_glazed_terracotta";
            require(FuturaAncientCityGenerator120.mappedBlockForFixture(name)
                    ==Backport120Content.glazedTerracotta[color],"glazed terracotta color mapping: "+name);
        }
        require(FuturaAncientCityGenerator120.mappedMetadataForFixture(
                "white_glazed_terracotta","north",0)==0,"glazed terracotta north facing");
        require(FuturaAncientCityGenerator120.mappedMetadataForFixture(
                "white_glazed_terracotta","north",1)==1,"glazed terracotta jigsaw rotation");
        require(FuturaAncientCityGenerator120.mappedBlockForFixture("cornflower")==Backport120Content.cornflower,
                "village cornflower must use the HTGT6 backport block");
        require(FuturaAncientCityGenerator120.mappedBlockForFixture("lily_of_the_valley")==Backport120Content.lilyOfTheValley,
                "village lily of the valley must use the HTGT6 backport block");
        require(FuturaAncientCityGenerator120.mappedBlockForFixture("beetroots")==CombatContent.beetrootCrop,
                "village beetroot must not degrade to carrots");
        require(FuturaAncientCityGenerator120.mappedBlockForFixture("coarse_dirt")==BountifulContent.coarseDirt,
                "village coarse dirt must use the HTGT6 1.8 block");
        require(FuturaAncientCityGenerator120.mappedBlockForFixture("spruce_door")==BountifulContent.woodDoors[0]
                        &&FuturaAncientCityGenerator120.mappedBlockForFixture("dark_oak_door")==BountifulContent.woodDoors[4],
                "village wood doors must preserve their backported wood species");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.plains.biomeID)==0,
                "plains village biome tag");
        require(FuturaVillageGenerator120.styleForBiomeId(FuturaBiomeIds120.MEADOW)==0,
                "meadow must use the official plains village pool");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.plains.biomeID+128)==0,
                "sunflower plains must use the plains village pool");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.birchForest.biomeID)==0,
                "birch forest must use the plains village pool");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.coldTaiga.biomeID)==3,
                "snowy taiga must use the snowy village pool");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.desertHills.biomeID)==1,
                "desert hills must use the desert village pool");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.savannaPlateau.biomeID)==2,
                "savanna plateau must use the savanna village pool");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.megaTaiga.biomeID)==4,
                "old-growth taiga must use the taiga village pool");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.ocean.biomeID)<0,
                "ocean must remain ineligible for villages");
        require(FuturaVillageGenerator120.styleForBiomeId(net.minecraft.world.biome.BiomeGenBase.river.biomeID)<0,
                "river must remain ineligible for villages");
        int[] villageBiomes={net.minecraft.world.biome.BiomeGenBase.plains.biomeID,
                net.minecraft.world.biome.BiomeGenBase.desert.biomeID,
                net.minecraft.world.biome.BiomeGenBase.savanna.biomeID,
                net.minecraft.world.biome.BiomeGenBase.icePlains.biomeID,
                net.minecraft.world.biome.BiomeGenBase.taiga.biomeID};
        for(int style=0;style<villageBiomes.length;style++){
            FuturaVillageGenerator120.RuntimeAudit audit=FuturaVillageGenerator120.runtimeAuditForFixture(
                    0x1202f00dL,-3+style,2-style,villageBiomes[style]);
            require(audit.exactCandidate,"runtime candidate does not match production random-spread placement");
            require(FuturaVillageGenerator120.isVillageStartChunk(
                            0x1202f00dL,audit.candidateChunkX,audit.candidateChunkZ),
                    "village placement does not recognize the unique start chunk");
            require(!FuturaVillageGenerator120.isVillageStartChunk(
                            0x1202f00dL,audit.candidateChunkX+1,audit.candidateChunkZ),
                    "village placement leaked into a non-start chunk");
            require(audit.style==style,"runtime biome gate selected the wrong official village style");
            require(audit.startX==(audit.candidateChunkX<<4)&&audit.startZ==(audit.candidateChunkZ<<4),
                    "jigsaw start must use the official candidate chunk minimum, without +8 offset");
            require(audit.pieces>1,"runtime jigsaw produced no connected village pieces");
            require(audit.intersects,"runtime jigsaw has no production chunk intersection");
            require(audit.forwarded&&audit.residentCalls==1&&audit.residentFlag,
                    "ensureResidents is disconnected from the production placement result");
        }
        require(FuturaVillagePopulation120.shouldRepairForFixture(true,false,true),
                "resident recovery rejects an intersecting house/POI chunk");
        require(!FuturaVillagePopulation120.shouldRepairForFixture(true,false,false),
                "resident recovery accepts a village chunk without a POI anchor");
        require(!FuturaVillagePopulation120.shouldRepairForFixture(false,false,true),
                "resident recovery accepts a non-village chunk");
        // Regression seeds copied from an actual Futura world where only the
        // town-center bell survived and the rest of the village was absent.
        long reportedSeed=5840949192149946755L;
        int[][] reportedRegions={{-1,-1},{-2,-1}};
        for(int[] region:reportedRegions){
            FuturaVillageGenerator120.RuntimeAudit audit=FuturaVillageGenerator120.runtimeAuditForFixture(
                    reportedSeed,region[0],region[1],net.minecraft.world.biome.BiomeGenBase.plains.biomeID);
            require(audit.pieces>1,"reported seed produced only the village start piece at region "
                    +region[0]+","+region[1]);
            require(audit.housePieces>=4,"reported seed produced a fragmented village at region "
                    +region[0]+","+region[1]+" (pieces="+audit.pieces+", houses="+audit.housePieces+", streets="+audit.streetPieces
                    +", terminators="+audit.terminatorPieces+", features="+audit.featurePieces
                    +", templates="+audit.sampleTemplates+")");
        }
        FuturaVillageGenerator120.RuntimeAudit currentWorld=FuturaVillageGenerator120.runtimeAuditForFixture(
                -8092085661795312214L,5,0,net.minecraft.world.biome.BiomeGenBase.plains.biomeID);
        require(currentWorld.candidateChunkX==191&&currentWorld.candidateChunkZ==3,
                "DEV 1 reported village candidate moved away from chunk 191,3");
        require(currentWorld.housePieces>=4,"DEV 1 reported village layout is fragmented (pieces="
                +currentWorld.pieces+", houses="+currentWorld.housePieces+", streets="
                +currentWorld.streetPieces+", terminators="+currentWorld.terminatorPieces
                +", templates="+currentWorld.sampleTemplates+")");
        FuturaVillageGenerator120.RuntimeAudit currentWorldSecond=FuturaVillageGenerator120.runtimeAuditForFixture(
                -8092085661795312214L,7,0,net.minecraft.world.biome.BiomeGenBase.plains.biomeID);
        require(currentWorldSecond.candidateChunkX==245&&currentWorldSecond.candidateChunkZ==23,
                "DEV 1 second reported village candidate moved away from chunk 245,23");
        require(currentWorldSecond.housePieces>=4&&currentWorldSecond.streetPieces>0,
                "DEV 1 second village has an incomplete jigsaw layout (pieces="+currentWorldSecond.pieces
                        +", houses="+currentWorldSecond.housePieces+", streets="+currentWorldSecond.streetPieces
                        +", templates="+currentWorldSecond.sampleTemplates+")");
        VillageJigsawFixture120.assertContract();
        VillageTemplateNbtFixture120.assertContract();
        FuturaStructureHeightFixture120.assertContract();
        FuturaAncientCityOfficialGateFixture120.assertContract();
        StructurePlacementExecutionFixture120.assertContract();
        StructureStateFixture120.assertContract();
        ModernStoneShapesFixture120.assertContract();
        System.out.println("Futura structure integration fixtures OK");
    }
    private static void assertRuinedPortalSpread(long seed,int regionX,int regionZ){
        int starts=0;
        for(int x=regionX*40;x<regionX*40+40;x++)for(int z=regionZ*40;z<regionZ*40+40;z++)
            if(FuturaRuinedPortalGenerator120.candidate(seed,x,z))starts++;
        require(starts==1,"ruined portal region must contain exactly one deterministic start, got "+starts);
    }
    private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
    private FuturaStructureIntegrationFixtureRunner120(){ }
}
