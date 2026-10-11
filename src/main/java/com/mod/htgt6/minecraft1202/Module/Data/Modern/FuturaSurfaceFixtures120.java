package com.mod.htgt6.minecraft1202.Module.Data.Modern;

import com.mod.htgt6.minecraft1202.Module.World.Modern.ChunkProviderFutura120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaOverworldSurfaceRules120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceContext120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceRules120;
import com.mod.htgt6.minecraft1202.Module.World.Modern.FuturaSurfaceSystem120;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.BiomeGenBase;

/** Fixed-seed regression vectors for the ported 1.20.2 SurfaceSystem inputs. */
public final class FuturaSurfaceFixtures120 {
    private static final long SEED = 0x1234ABCD5678EF90L;
    private static final long EXPECTED_FINGERPRINT = 0xFB2AD584BF227F37L;
    // Modern-Y positional RNG and nextFloat high bits, covered by the independent oracle fixture.
    private static final long EXPECTED_MATERIAL_FINGERPRINT = 0x1F23564139737C4BL;

    private FuturaSurfaceFixtures120() { }

    public static void main(String[] arguments) {
        FuturaSurfaceSystem120 surface = new FuturaSurfaceSystem120(SEED);
        int[][] columns = {{0, 0}, {17, -33}, {-1024, 2048}, {30000000, -30000000}};
        long fingerprint = 0xcbf29ce484222325L;
        for (int[] column : columns) {
            fingerprint = mix(fingerprint, surface.surfaceDepth(column[0], column[1]));
            fingerprint = mix(fingerprint, Double.doubleToLongBits(
                    surface.surfaceSecondary(column[0], column[1])));
        }
        if (fingerprint != EXPECTED_FINGERPRINT)
            throw new IllegalStateException("SurfaceSystem fixture changed: expected 0x"
                    + Long.toHexString(EXPECTED_FINGERPRINT) + ", got 0x" + Long.toHexString(fingerprint));
        validateConditions(surface);
        validateSteepReference(surface);
        validateColumnDepth(surface);
        long materialFingerprint=materialFingerprint(surface);
        if(materialFingerprint!=EXPECTED_MATERIAL_FINGERPRINT)
            throw new IllegalStateException("Surface material fixture changed: expected 0x"
                    +Long.toHexString(EXPECTED_MATERIAL_FINGERPRINT)+", got 0x"+Long.toHexString(materialFingerprint));
        validateMaterials(surface);
        System.out.println("SurfaceSystem fixture OK; fingerprint=0x" + Long.toHexString(fingerprint)
                +", material=0x"+Long.toHexString(materialFingerprint));
    }

    private static long materialFingerprint(FuturaSurfaceSystem120 system){
        long hash=0xcbf29ce484222325L;
        int[][] columns={{0,0},{17,-33},{-1024,2048}};
        for(int[] column:columns)for(int y=0;y<384;y++){
            FuturaSurfaceRules120.State band=system.clayBand(column[0],y,column[1]);
            hash=mix(hash,band.block==Blocks.hardened_clay?16:32);
            hash=mix(hash,band.metadata);
            hash=mix(hash,system.verticalGradient("minecraft:deepslate",column[0],y,column[1],0,8)?1:0);
        }
        Block[] eroded=new Block[256];byte[] erodedMeta=new byte[256];
        system.erodedBadlandsExtension(eroded,0,112,-208,100,256);
        Block[] frozen=new Block[256];byte[] frozenMeta=new byte[256];
        for(int y=0;y<=127;y++)frozen[y]=Blocks.water;
        system.frozenOceanExtension(frozen,frozenMeta,0,-96,80,100,127,256,true);
        for(int y=0;y<256;y++){
            hash=mix(hash,materialCode(eroded[y]));hash=mix(hash,materialCode(frozen[y]));
        }
        return hash;
    }

    private static void validateMaterials(FuturaSurfaceSystem120 system){
        FuturaSurfaceContext120 context=new FuturaSurfaceContext120().setColumn(3,0.0D,100,false);
        context.setBlock(140,1,6,Integer.MIN_VALUE,false);
        assertState("desert floor",FuturaOverworldSurfaceRules120.select(system,context,BiomeGenBase.desert,0,0),Blocks.sand,0);
        context.setBlock(140,10,20,Integer.MIN_VALUE,false);
        assertState("desert deep",FuturaOverworldSurfaceRules120.select(system,context,BiomeGenBase.desert,0,0),Blocks.sandstone,0);
        context.setBlock(130,1,6,Integer.MIN_VALUE,false);
        assertState("badlands red sand",FuturaOverworldSurfaceRules120.select(system,context,BiomeGenBase.mesa,0,0),Blocks.sand,1);
        context.setBlock(150,1,6,Integer.MIN_VALUE,false);
        FuturaSurfaceRules120.State band=FuturaOverworldSurfaceRules120.select(system,context,BiomeGenBase.mesa,0,0);
        require(band!=null&&(band.block==Blocks.hardened_clay||band.block==Blocks.stained_hardened_clay),"badlands band");
    }

    private static int materialCode(Block block){
        if(block==null)return 0;if(block==Blocks.stone)return 1;if(block==Blocks.water)return 2;
        if(block==Blocks.packed_ice)return 3;if(block==Blocks.snow)return 4;return 5;
    }

    private static void assertState(String name,FuturaSurfaceRules120.State state,Block block,int metadata){
        if(state==null||state.block!=block||state.metadata!=metadata)throw new IllegalStateException(name+" mismatch");
    }

    private static void validateConditions(FuturaSurfaceSystem120 system) {
        assertInt("preliminary surface", 119,
                system.preliminarySurface(8, 8, 100, 116, 132, 148, 3));

        int[] heights = new int[256];
        heights[8 + 7 * 16] = 100;
        heights[8 + 9 * 16] = 104;
        if (!system.steep(8, 8, heights)) throw new IllegalStateException("steep condition missed 4-block rise");
        heights[8 + 9 * 16] = 103;
        heights[7 + 8 * 16] = 103;
        heights[9 + 8 * 16] = 100;
        if (system.steep(8, 8, heights)) throw new IllegalStateException("steep condition accepted 3-block rise");

        FuturaSurfaceContext120 context = new FuturaSurfaceContext120()
                .setColumn(3, 0.0D, 100, true)
                .setBlock(100, 1, 5, Integer.MIN_VALUE, true);
        require(context.onFloor(), "on-floor");
        require(context.underFloor(), "under-floor");
        require(!context.onCeiling(), "on-ceiling depth");
        require(context.abovePreliminarySurface(), "preliminary surface");
        require(context.waterBlock(-1, 0, false), "no-water sentinel");
        require(context.steep(), "steep flag");
        require(context.cold(), "temperature flag");
        require(!context.hole(), "hole condition");
        require(context.mayApply(true, false), "allowlisted surface host");
        require(!context.mayApply(false, false), "non-host surface guard");
        require(!context.mayApply(true, true), "protected block");
        // Blocks is populated by Minecraft Bootstrap under LaunchWrapper. The
        // standalone fixture intentionally runs without that loader.
        if(Blocks.stone!=null)
            require(ChunkProviderFutura120.isSurfaceReplaceable(Blocks.stone,0,false), "default stone host");
        if(com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.deepslate!=null)
            require(ChunkProviderFutura120.isSurfaceReplaceable(
                    com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content.deepslate,0,true),
                    "external deepslate host");
        require(!ChunkProviderFutura120.isSurfaceReplaceable(Blocks.dirt,0,true),
                "arbitrary external solid protection");
        require(!ChunkProviderFutura120.isSurfaceReplaceable(Blocks.packed_ice,0,true),
                "iceberg packed ice protection");
        require(!ChunkProviderFutura120.isSurfaceReplaceable(Blocks.snow,0,true),
                "iceberg snow protection");

        context.setBlock(120, 1, 1, 128, false);
        require(!context.waterBlock(-1, 0, false), "submerged water check");
        require(context.stoneDepth(FuturaSurfaceContext120.CaveSurface.FLOOR,
                0, false, 6), "secondary depth mapping");
    }

    /**
     * Regression against 1.20.2 SurfaceRules.Context.SteepMaterialCondition.
     * Vanilla deliberately clamps neighbours to the current chunk at 0/15;
     * sampling the adjacent chunk here would introduce non-vanilla seams and
     * generation-order dependence.
     */
    private static void validateSteepReference(FuturaSurfaceSystem120 system) {
        int[] heights = new int[256];
        long state = 0x9E3779B97F4A7C15L;
        for (int i = 0; i < heights.length; i++) {
            state ^= state << 13;
            state ^= state >>> 7;
            state ^= state << 17;
            heights[i] = 80 + (int)(state & 63L);
        }
        for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
            boolean expected = referenceSteep1202(x, z, heights);
            boolean actual = system.steep(x, z, heights);
            if (actual != expected)
                throw new IllegalStateException("1.20.2 steep mismatch at local " + x + "," + z
                        + ": expected " + expected + ", got " + actual);
        }

        // At an edge 1.20.2 compares the edge column with the single in-chunk
        // neighbour. The unavailable outside height must not affect the result.
        int[] edge = new int[256];
        java.util.Arrays.fill(edge, 100);
        edge[8] = 100;
        edge[8 + 16] = 104;
        require(system.steep(8, 0, edge), "steep north-edge clamp");
        edge[8 + 16] = 100;
        edge[8] = 104;
        require(!system.steep(8, 0, edge), "steep direction is asymmetric");

        edge = new int[256];
        java.util.Arrays.fill(edge, 100);
        edge[15 + 8 * 16] = 100;
        edge[14 + 8 * 16] = 104;
        require(system.steep(15, 8, edge), "steep east-edge clamp");
    }

    /** A surfaceDepth of three is floor + three filler blocks, not a dirt column. */
    private static void validateColumnDepth(FuturaSurfaceSystem120 system) {
        FuturaSurfaceContext120 column = new FuturaSurfaceContext120()
                .setColumn(3, 0.0D, 120, false);
        for (int depth = 1; depth <= 6; depth++) {
            column.setBlock(161 - depth, depth, 80 - depth, Integer.MIN_VALUE, false);
            FuturaSurfaceRules120.State selected = FuturaOverworldSurfaceRules120.select(
                    system, column, BiomeGenBase.plains, 32, -48);
            if (depth == 1) assertState("column floor", selected, Blocks.grass, 0);
            else if (depth <= 4) assertState("column filler " + depth, selected, Blocks.dirt, 0);
            else require(selected == null, "dirt exceeded 1.20.2 surface depth at " + depth);
        }

        int y = 160;
        int uninterruptedDepthBelow = y - ChunkProviderFutura120.WAY_BELOW_MIN_Y_1202 + 1;
        column.setBlock(y, 1, uninterruptedDepthBelow, Integer.MIN_VALUE, false);
        require(!column.onCeiling(), "uninterrupted column became a ceiling through sentinel overflow");

        Block[] split = new Block[192];
        Block fixtureStone = new Block(Material.rock) { };
        for (int blockY = 90; blockY <= 100; blockY++) split[blockY] = fixtureStone;
        for (int blockY = 70; blockY <= 84; blockY++) split[blockY] = fixtureStone;
        assertInt("upper sequence bottom", 90,
                ChunkProviderFutura120.findStoneSequenceBottom(split, 0, 100));
        assertInt("lower sequence bottom after air gap", 70,
                ChunkProviderFutura120.findStoneSequenceBottom(split, 0, 84));
        column.setBlock(84, 1, 84 - 70 + 1, Integer.MIN_VALUE, false);
        require(!column.onCeiling(), "air-gap sequence used the previous ceiling depth");
        column.setBlock(70, 15, 1, Integer.MIN_VALUE, false);
        require(column.onCeiling(), "air-gap sequence bottom was not a cave ceiling");
    }

    private static boolean referenceSteep1202(int localX, int localZ, int[] heights) {
        int north = heights[localX + Math.max(localZ - 1, 0) * 16];
        int south = heights[localX + Math.min(localZ + 1, 15) * 16];
        if (south >= north + 4) return true;
        int west = heights[Math.max(localX - 1, 0) + localZ * 16];
        int east = heights[Math.min(localX + 1, 15) + localZ * 16];
        return west >= east + 4;
    }

    private static void assertInt(String name, int expected, int actual) {
        if (expected != actual) throw new IllegalStateException(name + ": expected " + expected + ", got " + actual);
    }

    private static void require(boolean condition, String name) {
        if (!condition) throw new IllegalStateException("SurfaceRules condition failed: " + name);
    }

    private static long mix(long hash, long value) {
        hash ^= value;
        return hash * 0x100000001b3L;
    }
}
