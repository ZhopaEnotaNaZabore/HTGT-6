package com.mod.htgt6.minecraft1202.Module.World.Aquatic;

import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockCoralBlock120;
import com.mod.htgt6.minecraft1202.Module.Blocks.Aquatic.BlockKelp;

import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** CoralTree, CoralClaw and CoralMushroom shapes ported from Java 1.20.2. */
public final class WorldGenCoralFeature120 {
    public static final int TREE = 0, CLAW = 1, MUSHROOM = 2;
    private static final int[][] HORIZONTAL = {{1,0},{-1,0},{0,1},{0,-1}};

    static boolean generate(World world, Random random, int x, int y, int z, int coralType, int shape) {
        switch (shape) {
            case CLAW: return claw(world, random, x, y, z, coralType);
            case MUSHROOM: return mushroom(world, random, x, y, z, coralType);
            default: return tree(world, random, x, y, z, coralType);
        }
    }

    private static boolean tree(World world, Random random, int x, int y, int z, int type) {
        int height = 1 + random.nextInt(3);
        int top = y;
        for (int i = 0; i < height; i++, top++) if (!place(world, random, x, top, z, type)) return i > 0;
        List<int[]> directions = shuffled(random);
        int branches = 2 + random.nextInt(3);
        for (int i = 0; i < branches; i++) {
            int[] direction = directions.get(i);
            int bx = x + direction[0], bz = z + direction[1], by = top - 1;
            int length = 2 + random.nextInt(5), consecutive = 0;
            for (int step = 0; step < length; step++) {
                if (!place(world, random, bx, by, bz, type)) break;
                consecutive++;
                by++;
                if (step == 0 || consecutive >= 2 && random.nextFloat() < 0.25F) {
                    bx += direction[0]; bz += direction[1]; consecutive = 0;
                }
            }
        }
        return true;
    }

    private static boolean claw(World world, Random random, int x, int y, int z, int type) {
        if (!place(world, random, x, y, z, type)) return false;
        List<int[]> directions = shuffled(random);
        int claws = 2 + random.nextInt(2);
        for (int i = 0; i < claws; i++) {
            int[] direction = directions.get(i);
            int cx = x + direction[0], cz = z + direction[1], cy = y;
            int outward = 1 + random.nextInt(2);
            for (int step = 0; step < outward; step++) {
                if (!place(world, random, cx, cy, cz, type)) break;
                cx += direction[0]; cz += direction[1];
            }
            int rise = 2 + random.nextInt(3);
            for (int step = 0; step < rise; step++, cy++) {
                if (!place(world, random, cx, cy, cz, type)) break;
                if (step > 0 && random.nextFloat() < 0.25F) { cx += direction[0]; cz += direction[1]; }
            }
        }
        return true;
    }

    private static boolean mushroom(World world, Random random, int x, int y, int z, int type) {
        int sizeX = 3 + random.nextInt(3), sizeY = 3 + random.nextInt(3);
        int sizeZ = 3 + random.nextInt(3), lower = 1 + random.nextInt(3);
        boolean placed = false;
        for (int dx = 0; dx <= sizeX; dx++) for (int dy = 0; dy <= sizeY; dy++) {
            for (int dz = 0; dz <= sizeZ; dz++) {
                boolean shell = dx == 0 || dx == sizeX || dy == 0 || dy == sizeY
                        || dz == 0 || dz == sizeZ;
                boolean edge = (dx == 0 || dx == sizeX) && (dy == 0 || dy == sizeY)
                        || (dz == 0 || dz == sizeZ) && (dy == 0 || dy == sizeY)
                        || (dx == 0 || dx == sizeX) && (dz == 0 || dz == sizeZ);
                if (!shell || edge || random.nextFloat() < 0.10F) continue;
                placed |= place(world, random, x + dx, y + dy - lower, z + dz, type);
            }
        }
        return placed;
    }

    private static boolean place(World world, Random random, int x, int y, int z, int type) {
        if (!(BlockKelp.isOpenWater(world.getBlock(x, y, z))
                || BlockCoralBlock120.isLive(world.getBlock(x, y, z),
                        world.getBlockMetadata(x, y, z)))
                || !BlockKelp.isOpenWater(world.getBlock(x, y + 1, z))) return false;
        world.setBlock(x, y, z, OceanContent.coralBlock, type, 2);
        if (random.nextFloat() < 0.25F) {
            world.setBlock(x, y + 1, z, OceanContent.marinePlant, 3 + type, 2);
        } else if (random.nextFloat() < 0.05F) {
            world.setBlock(x, y + 1, z, OceanContent.marinePlant, 8 + random.nextInt(4), 2);
        }
        for (int[] direction : HORIZONTAL) if (random.nextFloat() < 0.20F) {
            int nx = x + direction[0], nz = z + direction[1];
            if (BlockKelp.isOpenWater(world.getBlock(nx, y, nz))) {
                world.setBlock(nx, y, nz, OceanContent.wallCoralFans[type],
                        directionIndex(direction), 2);
            }
        }
        return true;
    }

    private static int directionIndex(int[] direction) {
        for (int index = 0; index < HORIZONTAL.length; index++) {
            if (HORIZONTAL[index][0] == direction[0] && HORIZONTAL[index][1] == direction[1]) return index;
        }
        return 0;
    }

    private static List<int[]> shuffled(Random random) {
        List<int[]> values = new ArrayList<int[]>();
        Collections.addAll(values, HORIZONTAL);
        Collections.shuffle(values, random);
        return values;
    }

    private WorldGenCoralFeature120() { }
}
