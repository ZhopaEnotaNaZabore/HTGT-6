package com.mod.htgt6.minecraft1202.Module.World.Modern;

import com.mod.htgt6.minecraft1202.Module.Registry.Modern.Backport120Content;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorSimplex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 1.20.2 TreeFeature primitives used by both saplings and placed features.
 * Constants mirror the vanilla configured_feature JSON; algorithms mirror the
 * corresponding trunk, foliage and root placers without importing modern APIs.
 */
public final class FuturaTreeFeatureRuntime120 {
    public static final Config CHERRY = new Config("cherry", 7, 1, 0, 4, 5, 0);
    public static final Config AZALEA = new Config("azalea_tree", 4, 2, 0, 3, 2, 50);
    public static final Config MANGROVE = new Config("mangrove", 2, 1, 4, 3, 2, 70);
    public static final Config TALL_MANGROVE = new Config("tall_mangrove", 4, 1, 9, 3, 2, 70);
    public static final int CHERRY_MAX_ABOVE = 11;
    public static final int AZALEA_MAX_ABOVE = 8;
    public static final int MANGROVE_MAX_ABOVE = 18;
    public static final int TALL_MANGROVE_MAX_ABOVE = 36;
    public static final int CHERRY_BRANCH_MIN = 2, CHERRY_BRANCH_MAX = 4;
    public static final float CHERRY_WIDE_HOLE = .25F, CHERRY_CORNER_HOLE = .25F;
    public static final float CHERRY_HANGING = 1F / 6F, CHERRY_HANGING_EXTENSION = 1F / 3F;
    public static final int MANGROVE_ROOT_WIDTH = 8, MANGROVE_ROOT_LENGTH = 15;
    public static final float MANGROVE_ROOT_SKEW = .2F, MANGROVE_BRANCH_CHANCE = .5F;
    public static final float MANGROVE_VINE_CHANCE = .125F, MANGROVE_PROPAGULE_CHANCE = .14F;
    public static final int BAMBOO_MIN_HEIGHT = 5, BAMBOO_HEIGHT_RANGE = 12;
    public static final boolean BEEHIVE_DECORATOR_SUPPORTED = true;
    public static final float CHERRY_BEEHIVE_CHANCE = .05F, MANGROVE_BEEHIVE_CHANCE = .01F;
    private static final NoiseGeneratorSimplex BIOME_INFO_NOISE = new NoiseGeneratorSimplex(new Random(2345L));

    /** NoiseBasedCountPlacement(160,80,.3), using Biome.BIOME_INFO_NOISE. */
    public static int bambooPlacedCount(int x, int z) {
        return noiseBasedCount(x,z,80D,.3D,160);
    }

    /** Exact shared implementation of 1.20.2 NoiseBasedCountPlacement. */
    public static int noiseBasedCount(int x,int z,double noiseFactor,double noiseOffset,int ratio){
        return Math.max(0,(int)Math.ceil((BIOME_INFO_NOISE.func_151605_a(
                x/noiseFactor,z/noiseFactor)+noiseOffset)*ratio));
    }

    /** NoiseThresholdCountPlacement(-.8,5,10), using Biome.BIOME_INFO_NOISE. */
    public static int cherryFlowerPatchCount(int x, int z) {
        return BIOME_INFO_NOISE.func_151605_a(x / 200D, z / 200D) < -.8D ? 5 : 10;
    }

    public static int thresholdCount(int x,int z,double threshold,int below,int above){
        return BIOME_INFO_NOISE.func_151605_a(x/200D,z/200D)<threshold?below:above;
    }

    public static long placementNoiseFixtureFingerprint() {
        long hash = 0xcbf29ce484222325L;
        for (int z = -2048; z <= 2048; z += 257) for (int x = -2048; x <= 2048; x += 251) {
            hash ^= bambooPlacedCount(x, z); hash *= 0x100000001b3L;
            hash ^= cherryFlowerPatchCount(x, z); hash *= 0x100000001b3L;
        }
        return hash;
    }

    public static boolean cherry(World world, Random random, int x, int y, int z) {
        if (!soil(world, x, y - 1, z, false)) return false;
        int height = CHERRY.height(random);
        int firstStart = Math.max(0, height - 1 + between(random, -4, -3));
        int secondStart = Math.max(0, height - 1 - 4);
        if (secondStart >= firstStart) secondStart++;
        int branches = 1 + random.nextInt(3);
        int trunkHeight = branches == 3 ? height : branches >= 2
                ? Math.max(firstStart, secondStart) + 1 : firstStart + 1;
        List<Pos> logs = new ArrayList<Pos>();
        List<Pos> foliage = new ArrayList<Pos>();
        for (int i = 0; i < trunkHeight; i++) logs.add(new Pos(x, y + i, z, 0));
        if (branches == 3) foliage.add(new Pos(x, y + trunkHeight, z, 0));
        int direction = random.nextInt(4);
        foliage.add(cherryBranch(random, logs, x, y, z, height, direction, firstStart,
                firstStart < trunkHeight - 1));
        if (branches >= 2) foliage.add(cherryBranch(random, logs, x, y, z, height,
                direction ^ 1, secondStart, secondStart < trunkHeight - 1));
        if (!validateLogs(world, logs, x, y, z)) return false;
        world.setBlock(x, y - 1, z, Blocks.dirt, 0, 2);
        placeLogs(world, logs, Backport120Content.cherryLog);
        for (Pos attachment : foliage) cherryFoliage(world, random, attachment);
        beeNest(world,random,CHERRY_BEEHIVE_CHANCE,logs);
        return true;
    }

    private static Pos cherryBranch(Random random, List<Pos> logs, int x, int y, int z,
                                     int height, int direction, int start, boolean insideTrunk) {
        int cx = x, cy = y + start, cz = z;
        int endY = y + height - 1 + between(random, -1, 0);
        boolean needsExtra = insideTrunk || endY < cy;
        int length = between(random, 2, 4) + (needsExtra ? 1 : 0);
        int tx = x + dx(direction) * length, tz = z + dz(direction) * length;
        int initial = needsExtra ? 2 : 1;
        for (int i = 0; i < initial; i++) {
            cx += dx(direction); cz += dz(direction);
            logs.add(new Pos(cx, cy, cz, axis(direction)));
        }
        int distance;
        while ((distance = Math.abs(tx - cx) + Math.abs(endY - cy) + Math.abs(tz - cz)) != 0) {
            float verticalChance = (float)Math.abs(endY - cy) / (float)distance;
            boolean vertical = random.nextFloat() < verticalChance;
            if (vertical) cy += endY > cy ? 1 : -1;
            else { cx += dx(direction); cz += dz(direction); }
            logs.add(new Pos(cx, cy, cz, vertical ? 0 : axis(direction)));
        }
        return new Pos(tx, endY, tz, 0);
    }

    private static void cherryFoliage(World world, Random random, Pos p) {
        leavesRow(world, random, p, 2, 1, true, Backport120Content.cherryLeaves, Layer.CHERRY);
        leavesRow(world, random, p, 1, 2, true, Backport120Content.cherryLeaves, Layer.CHERRY);
        leavesRow(world, random, p, 0, 3, true, Backport120Content.cherryLeaves, Layer.CHERRY);
        hangingRow(world, random, p, 3, -1, true, Backport120Content.cherryLeaves, 1F / 6F, 1F / 3F);
        hangingRow(world, random, p, 2, -2, true, Backport120Content.cherryLeaves, 1F / 6F, 1F / 3F);
    }

    public static boolean azalea(World world, Random random, int x, int y, int z) {
        if (!soil(world, x, y - 1, z, true)) return false;
        int height = AZALEA.height(random), direction = random.nextInt(4);
        int cx = x, cz = z;
        List<Pos> logs = new ArrayList<Pos>();
        List<Pos> attachments = new ArrayList<Pos>();
        for (int i = 0; i < height; i++) {
            if (i + 1 >= height - 1 + random.nextInt(2)) { cx += dx(direction); cz += dz(direction); }
            logs.add(new Pos(cx, y + i, cz, 0));
            if (i >= 3) attachments.add(new Pos(cx, y + i, cz, 0));
        }
        int bend = between(random, 1, 2);
        for (int i = 0; i <= bend; i++) {
            logs.add(new Pos(cx, y + height + i, cz, 0));
            attachments.add(new Pos(cx, y + height + i, cz, 0));
            cx += dx(direction); cz += dz(direction);
        }
        if (!validateLogs(world, logs, x, y, z)) return false;
        world.setBlock(x, y - 1, z, Backport120Content.rootedDirt, 0, 2);
        placeLogs(world, logs, Blocks.log);
        for (Pos p : attachments) randomSpreadFoliage(world, random, p, 3, 2, 50, true);
        return true;
    }

    public static boolean mangrove(World world, Random random, int x, int y, int z, boolean tall) {
        Config config = tall ? TALL_MANGROVE : MANGROVE;
        if (!soil(world, x, y - 1, z, true)) return false;
        int offset = tall ? between(random, 3, 7) : between(random, 1, 3);
        int trunkY = y + offset;
        List<Pos> roots = new ArrayList<Pos>();
        for (int yy = y; yy < trunkY; yy++) roots.add(new Pos(x, yy, z, 0));
        for (int direction = 0; direction < 4; direction++) {
            List<Pos> branch = new ArrayList<Pos>();
            if (!mangroveRoot(new RootAccess() {
                        @Override public boolean replaceable(int px, int py, int pz) {
                            return rootReplaceable(world, px, py, pz);
                        }
                    }, random, x + dx(direction), trunkY, z + dz(direction),
                    direction, x, trunkY, z, branch, 0)) return false;
            roots.addAll(branch);
            roots.add(new Pos(x + dx(direction), trunkY, z + dz(direction), 0));
        }
        int height = config.height(random);
        List<Pos> logs = new ArrayList<Pos>();
        List<Pos> attachments = new ArrayList<Pos>();
        for (int i = 0; i < height; i++) {
            int yy = trunkY + i;
            logs.add(new Pos(x, yy, z, 0));
            if (i < height - 1 && random.nextFloat() < .5F) {
                int direction = random.nextInt(4);
                int branchLength = random.nextInt(2);
                int start = Math.max(0, branchLength - random.nextInt(2) - 1);
                int steps = between(random, 1, tall ? 6 : 4);
                mangroveBranch(logs, attachments, x, yy, z, height, direction, start, steps);
            }
            if (i == height - 1) attachments.add(new Pos(x, yy + 1, z, 0));
        }
        if (!validateLogs(world, logs, x, trunkY, z)) return false;
        for (Pos root : roots) if (!rootReplaceable(world, root.x, root.y, root.z)) return false;
        for (Pos root : roots) {
            Block old = world.getBlock(root.x, root.y, root.z);
            world.setBlock(root.x, root.y, root.z,
                    old == Backport120Content.mud || old == Backport120Content.muddyMangroveRoots
                            ? Backport120Content.muddyMangroveRoots : Backport120Content.mangroveRoots, 0, 2);
            if(random.nextFloat()<.5F&&world.isAirBlock(root.x,root.y+1,root.z))
                world.setBlock(root.x,root.y+1,root.z,Backport120Content.mossCarpet,0,2);
        }
        placeLogs(world, logs, Backport120Content.mangroveLog);
        List<Long> leaves = new ArrayList<Long>();
        for (Pos p : attachments) randomSpreadFoliage(world, random, p, 3, 2, 70, false, leaves);
        decorateMangrove(world, random, leaves);
        beeNest(world,random,MANGROVE_BEEHIVE_CHANCE,logs);
        return true;
    }

    private static boolean mangroveRoot(RootAccess access, Random random, int x, int y, int z, int direction,
                                        int ox, int oy, int oz, List<Pos> out, int depth) {
        if (depth == 15 || out.size() > 15) return false;
        int distance = Math.abs(x - ox) + Math.abs(y - oy) + Math.abs(z - oz);
        List<Pos> next = new ArrayList<Pos>(2);
        if (distance > 5 && distance <= 8) {
            next.add(new Pos(x, y - 1, z, 0));
            if (random.nextFloat() < .2F) next.add(new Pos(x + dx(direction), y - 1, z + dz(direction), 0));
        } else if (distance > 8 || random.nextFloat() < .2F) next.add(new Pos(x, y - 1, z, 0));
        else if (random.nextBoolean()) next.add(new Pos(x + dx(direction), y, z + dz(direction), 0));
        else next.add(new Pos(x, y - 1, z, 0));
        for (Pos p : next) {
            if (!access.replaceable(p.x, p.y, p.z)) continue;
            out.add(p);
            if (!mangroveRoot(access, random, p.x, p.y, p.z, direction, ox, oy, oz, out, depth + 1)) return false;
        }
        return true;
    }

    /** Headless regression for ordinary roots stopping on dirt below a shallow mud layer. */
    public static long mangroveRootFixtureFingerprint() {
        long hash = 0xcbf29ce484222325L;
        RootAccess access = new RootAccess() {
            @Override public boolean replaceable(int x, int y, int z) { return y >= 0; }
        };
        int successfulSeeds = 0;
        for (int seed = 0; seed < 64; seed++) {
            Random random = new Random(seed);
            boolean seedSucceeded = true;
            for (int direction = 0; direction < 4; direction++) {
                List<Pos> roots = new ArrayList<Pos>();
                if (!mangroveRoot(access, random, dx(direction), 5, dz(direction), direction,
                        0, 5, 0, roots, 0)) {
                    seedSucceeded = false;
                }
                hash ^= roots.size(); hash *= 0x100000001b3L;
                for (Pos p : roots) { hash ^= Pos.pack(p.x, p.y, p.z); hash *= 0x100000001b3L; }
            }
            if (seedSucceeded) successfulSeeds++;
            hash ^= seedSucceeded ? 1L : 0L; hash *= 0x100000001b3L;
        }
        if (successfulSeeds < 32) throw new IllegalStateException("Too few successful mangrove root seeds: " + successfulSeeds);
        hash ^= successfulSeeds; hash *= 0x100000001b3L;
        return hash;
    }

    private static void mangroveBranch(List<Pos> logs, List<Pos> foliage, int x, int y, int z,
                                       int height, int direction, int start, int steps) {
        int cx = x, cz = z, lastY = y + start;
        for (int i = start; i < height && steps > 0; i++, steps--) {
            if (i < 1) continue;
            lastY = y + i; cx += dx(direction); cz += dz(direction);
            logs.add(new Pos(cx, lastY, cz, axis(direction)));
            foliage.add(new Pos(cx, lastY, cz, 0));
        }
        if (lastY - y > 1) {
            foliage.add(new Pos(cx, lastY, cz, 0));
            foliage.add(new Pos(cx, lastY + 2, cz, 0));
        }
    }

    private static void decorateMangrove(World world, Random random, List<Long> leafPositions) {
        for (long packed : leafPositions) {
            Pos p = Pos.unpack(packed);
            for (int d = 0; d < 4; d++) if (random.nextFloat() < .125F) {
                int vx = p.x + dx(d), vz = p.z + dz(d);
                if (world.isAirBlock(vx, p.y, vz)) world.setBlock(vx, p.y, vz, Blocks.vine, vineMeta(d), 2);
            }
        }
        Collections.shuffle(leafPositions, random);
        Set<Long> propaguleExclusion = new HashSet<Long>();
        for(long packed:leafPositions){
            Pos p=Pos.unpack(packed);
            int targetY=p.y-1;long target=Pos.pack(p.x,targetY,p.z);
            if(propaguleExclusion.contains(target)||random.nextFloat()>=MANGROVE_PROPAGULE_CHANCE
                    ||!world.isAirBlock(p.x,targetY,p.z)||!world.isAirBlock(p.x,targetY-1,p.z))continue;
            for(int ex=-1;ex<=1;ex++)for(int ez=-1;ez<=1;ez++)
                propaguleExclusion.add(Pos.pack(p.x+ex,targetY,p.z+ez));
            world.setBlock(p.x,targetY,p.z,Backport120Content.mangrovePropagule,8|random.nextInt(5),2);
        }
    }

    public static boolean bamboo(World world, Random random, int x, int y, int z, float podzolChance) {
        if (!world.isAirBlock(x, y, z) || !soil(world, x, y - 1, z, false)) return false;
        if (random.nextFloat() < podzolChance) {
            int radius = random.nextInt(4) + 1;
            for (int px = x - radius; px <= x + radius; px++) for (int pz = z - radius; pz <= z + radius; pz++) {
                if ((px - x) * (px - x) + (pz - z) * (pz - z) > radius * radius) continue;
                int py = world.getTopSolidOrLiquidBlock(px, pz) - 1;
                Block old = world.getBlock(px, py, pz);
                if (old == Blocks.grass || old == Blocks.dirt) world.setBlock(px, py, pz, Blocks.dirt, 2, 2);
            }
        }
        int height = random.nextInt(BAMBOO_HEIGHT_RANGE) + BAMBOO_MIN_HEIGHT, placed = 0;
        while (placed < height && y + placed < world.getHeight() && world.isAirBlock(x, y + placed, z)) {
            world.setBlock(x, y + placed, z, Backport120Content.bamboo,
                    com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120BambooAccess.state(0, 0, 0), 2);
            placed++;
        }
        if (placed >= 3) {
            world.setBlockMetadataWithNotify(x, y + placed - 1, z,
                    com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120BambooAccess.state(1, 2, 1), 2);
            world.setBlockMetadataWithNotify(x, y + placed - 2, z,
                    com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120BambooAccess.state(1, 2, 0), 2);
            world.setBlockMetadataWithNotify(x, y + placed - 3, z,
                    com.mod.htgt6.minecraft1202.Module.Blocks.Modern.BlockBackport120BambooAccess.state(1, 1, 0), 2);
        }
        return placed > 0;
    }

    private static void randomSpreadFoliage(World world, Random random, Pos p, int radius,
                                            int height, int attempts, boolean azalea) {
        randomSpreadFoliage(world, random, p, radius, height, attempts, azalea, null);
    }

    private static void randomSpreadFoliage(World world, Random random, Pos p, int radius,
                                            int height, int attempts, boolean azalea, List<Long> positions) {
        for (int i = 0; i < attempts; i++) {
            int px = p.x + random.nextInt(radius) - random.nextInt(radius);
            int py = p.y + random.nextInt(height) - random.nextInt(height);
            int pz = p.z + random.nextInt(radius) - random.nextInt(radius);
            if (replaceable(world, px, py, pz)) {
                Block leaves = azalea && random.nextInt(4) == 0
                        ? Backport120Content.floweringAzaleaLeaves
                        : azalea ? Backport120Content.azaleaLeaves : Backport120Content.mangroveLeaves;
                if (world.setBlock(px, py, pz, leaves, 0, 2) && positions != null)
                    positions.add(Pos.pack(px, py, pz));
            }
        }
    }

    private static void leavesRow(World world, Random random, Pos p, int radius, int dy,
                                  boolean cherry, Block leaves, Layer layer) {
        for (int ox = -radius; ox <= radius; ox++) for (int oz = -radius; oz <= radius; oz++) {
            int ax = Math.abs(ox), az = Math.abs(oz);
            boolean skip = dy == -1 && (ax == radius || az == radius) && random.nextFloat() < .25F;
            boolean corner = ax == radius && az == radius;
            if (radius > 2) skip |= corner || ax + az > radius * 2 - 2 && random.nextFloat() < .25F;
            else skip |= corner && random.nextFloat() < .25F;
            if (!skip && replaceable(world, p.x + ox, p.y + dy, p.z + oz))
                world.setBlock(p.x + ox, p.y + dy, p.z + oz, leaves, 0, 2);
        }
    }

    private static void hangingRow(World world, Random random, Pos p, int radius, int dy,
                                   boolean cherry, Block leaves, float chance, float extension) {
        leavesRow(world, random, p, radius, dy, cherry, leaves, Layer.CHERRY);
        for (int direction = 0; direction < 4; direction++) for (int line = -radius; line < radius; line++) {
            int px, pz;
            if (direction < 2) { px = p.x + (direction == 0 ? radius : -radius); pz = p.z + line; }
            else { px = p.x + line; pz = p.z + (direction == 2 ? radius : -radius); }
            if (world.getBlock(px, p.y + dy, pz) != leaves || random.nextFloat() > chance) continue;
            if (replaceable(world, px, p.y + dy - 1, pz)) {
                world.setBlock(px, p.y + dy - 1, pz, leaves, 0, 2);
                if (random.nextFloat() <= extension && replaceable(world, px, p.y + dy - 2, pz))
                    world.setBlock(px, p.y + dy - 2, pz, leaves, 0, 2);
            }
        }
    }

    private static boolean validateLogs(World world, List<Pos> logs, int x, int y, int z) {
        for (Pos p : logs) if (!(p.x == x && p.y == y && p.z == z) && !replaceableOrWater(world, p.x, p.y, p.z)) return false;
        return true;
    }

    private static void placeLogs(World world, List<Pos> logs, Block log) {
        for (Pos p : logs) world.setBlock(p.x, p.y, p.z, log, p.meta, 2);
    }

    private static boolean soil(World world, int x, int y, int z, boolean rooted) {
        Block b = world.getBlock(x, y, z);
        return b == Blocks.grass || b == Blocks.dirt || b == Blocks.farmland || b == Blocks.clay
                || b == Backport120Content.mossBlock || b == Backport120Content.mud
                || rooted && b == Backport120Content.rootedDirt;
    }

    private static boolean replaceable(World world, int x, int y, int z) {
        if (y < 0 || y >= world.getHeight()) return false;
        Block b = world.getBlock(x, y, z);
        return b.isAir(world, x, y, z) || b.isLeaves(world, x, y, z)
                || b == Blocks.vine || b == Blocks.tallgrass || b == Blocks.snow_layer;
    }

    private static boolean replaceableOrWater(World world, int x, int y, int z) {
        Block b = world.getBlock(x, y, z);
        return replaceable(world, x, y, z) || b == Blocks.water || b == Blocks.flowing_water;
    }

    private static boolean rootReplaceable(World world, int x, int y, int z) {
        if (y < 0 || y >= world.getHeight()) return false;
        Block b = world.getBlock(x, y, z);
        // minecraft:mangrove_roots_can_grow_through. Dirt/clay deliberately stop a root.
        return replaceableOrWater(world, x, y, z) || b == Backport120Content.mud
                || b == Backport120Content.muddyMangroveRoots || b == Backport120Content.mangroveRoots
                || b == Backport120Content.mossCarpet || b == Backport120Content.mangrovePropagule;
    }

    private static int between(Random random, int min, int max) { return min + random.nextInt(max - min + 1); }
    private static int dx(int direction) { return direction == 0 ? 1 : direction == 1 ? -1 : 0; }
    private static int dz(int direction) { return direction == 2 ? 1 : direction == 3 ? -1 : 0; }
    private static int axis(int direction) { return direction < 2 ? 4 : 8; }
    private static int vineMeta(int outwardDirection) { return outwardDirection == 0 ? 2 : outwardDirection == 1 ? 8 : outwardDirection == 2 ? 4 : 1; }

    private enum Layer { CHERRY }
    private interface RootAccess { boolean replaceable(int x, int y, int z); }

    public static final class Config {
        public final String name;
        public final int baseHeight, heightRandA, heightRandB, foliageRadius, foliageHeight, leafAttempts;
        Config(String name, int baseHeight, int heightRandA, int heightRandB,
               int foliageRadius, int foliageHeight, int leafAttempts) {
            this.name = name; this.baseHeight = baseHeight; this.heightRandA = heightRandA;
            this.heightRandB = heightRandB; this.foliageRadius = foliageRadius;
            this.foliageHeight = foliageHeight; this.leafAttempts = leafAttempts;
        }
        int height(Random random) { return baseHeight + random.nextInt(heightRandA + 1) + random.nextInt(heightRandB + 1); }
    }

    private static void beeNest(World w,Random r,float chance,List<Pos> logs){
        List<int[]> trunk=new ArrayList<int[]>(),leaves=new ArrayList<int[]>();for(Pos p:logs){trunk.add(new int[]{p.x,p.y,p.z});for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(w.getBlock(p.x+dx,p.y,p.z+dz).isLeaves(w,p.x+dx,p.y,p.z+dz))leaves.add(new int[]{p.x+dx,p.y,p.z+dz});}
        BeeNestDecorator115.decorate(w,r,chance,trunk,leaves);
    }

    private static final class Pos {
        final int x, y, z, meta;
        Pos(int x, int y, int z, int meta) { this.x = x; this.y = y; this.z = z; this.meta = meta; }
        static long pack(int x, int y, int z) { return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (y & 0xFFFL); }
        static Pos unpack(long p) { int x = (int)(p >> 38), y = (int)(p & 0xFFFL), z = (int)(p << 26 >> 38); return new Pos(x, y, z, 0); }
    }

    private FuturaTreeFeatureRuntime120() { }
}
