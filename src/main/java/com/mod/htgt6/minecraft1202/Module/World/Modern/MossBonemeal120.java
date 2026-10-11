package com.mod.htgt6.minecraft1202.Module.World.Modern;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import com.mod.htgt6.minecraft1202.Module.Registry.Bountiful.BountifulContent;

/** VegetationPatchFeature specialized to the official moss_patch_bonemeal configuration. */
public final class MossBonemeal120 {
    public interface Access {
        boolean valid(int x, int y, int z);
        boolean air(int x, int y, int z);
        boolean solidTop(int x, int y, int z);
        Block block(int x, int y, int z);
        int metadata(int x, int y, int z);
        void set(int x, int y, int z, Block block, int metadata);
    }
    public static int generate(final World world, Random random, int x, int y, int z) {
        return generate(new Access() {
            public boolean valid(int x, int y, int z) {
                return y >= 0 && y < world.getHeight() && world.getChunkProvider().chunkExists(x >> 4, z >> 4);
            }
            public boolean air(int x, int y, int z) { return world.isAirBlock(x, y, z); }
            public boolean solidTop(int x, int y, int z) { return world.isSideSolid(x, y, z, ForgeDirection.UP); }
            public Block block(int x, int y, int z) { return world.getBlock(x, y, z); }
            public int metadata(int x, int y, int z) { return world.getBlockMetadata(x, y, z); }
            public void set(int x, int y, int z, Block block, int metadata) { world.setBlock(x, y, z, block, metadata, 3); }
        }, random, x, y, z);
    }
    public static int generate(Access world, Random random, int x, int y, int z) {
        // Feature adds one to each independently sampled configured radius (1..2).
        int radiusX = 2 + random.nextInt(2), radiusZ = 2 + random.nextInt(2);
        List<int[]> ground = new ArrayList<int[]>();
        for (int dx = -radiusX; dx <= radiusX; dx++) for (int dz = -radiusZ; dz <= radiusZ; dz++) {
            boolean edgeX = Math.abs(dx) == radiusX, edgeZ = Math.abs(dz) == radiusZ;
            if (edgeX && edgeZ || (edgeX || edgeZ) && random.nextFloat() > 0.75F) continue;
            int px = x + dx, pz = z + dz, cursor = y;
            for (int i = 0; i < 5 && world.valid(px, cursor, pz) && world.air(px, cursor, pz); i++) cursor--;
            for (int i = 0; i < 5 && world.valid(px, cursor, pz) && !world.air(px, cursor, pz); i++) cursor++;
            int surface = cursor - 1;
            if (!world.valid(px, cursor, pz) || !world.valid(px, surface, pz)
                    || !world.air(px, cursor, pz) || !world.solidTop(px, surface, pz)) continue;
            Block old = world.block(px, surface, pz);
            if (!replaceable(old, world.metadata(px, surface, pz))) continue;
            if (old != Backport120Content.mossBlock) world.set(px, surface, pz, Backport120Content.mossBlock, 0);
            ground.add(new int[]{px, cursor, pz});
        }
        // Finish ground replacement first, as in the source feature.
        for (int[] position : ground) if (random.nextFloat() < 0.6F)
            vegetation(world, random, position[0], position[1], position[2]);
        return ground.size();
    }
    public static boolean replaceable(Block block, int metadata) {
        if (block == null) return false;
        return block == Blocks.stone || block == Blocks.dirt || block == Blocks.grass || block == Blocks.mycelium
                || block == BountifulContent.coarseDirt || block == BountifulContent.stone && (metadata == 0 || metadata == 2 || metadata == 4)
                || block == Backport120Content.tuff || block == Backport120Content.deepslate
                || block == Backport120Content.rootedDirt || block == Backport120Content.mossBlock
                || block == Backport120Content.mud || block == Backport120Content.muddyMangroveRoots
                || block == Backport120Content.caveVines || block == Backport120Content.caveVinesPlant;
    }
    private static void vegetation(Access world, Random random, int x, int y, int z) {
        if (!world.valid(x, y, z) || !world.air(x, y, z)) return;
        int roll = random.nextInt(96);
        if (roll < 4) world.set(x, y, z, Backport120Content.floweringAzalea, 0);
        else if (roll < 11) world.set(x, y, z, Backport120Content.azalea, 0);
        else if (roll < 36) world.set(x, y, z, Backport120Content.mossCarpet, 0);
        else if (roll < 86) world.set(x, y, z, Blocks.tallgrass, 1);
        else if (world.valid(x, y + 1, z) && world.air(x, y + 1, z)) {
            world.set(x, y, z, Blocks.double_plant, 2);
            world.set(x, y + 1, z, Blocks.double_plant, 10);
        }
    }
    private MossBonemeal120() { }
}
